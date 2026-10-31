import {computed, ref, watch, proxyRefs} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {operationLabel,t,uiErrorMessage} from '@kuaixie/i18n';
import {getBearer,type ApiOperation} from '@kuaixie/sdk';
import {api} from '../api/client';
import {getFlow} from '../features/flows/definitions';
export function useWorkflowPage(id:string){
 const flow=getFlow(id);
 const active=ref(0);
 const current=computed(()=>flow.operations[active.value]);
 const fields=computed(()=>current.value?.bodyFields??[]);
 const form=ref<Record<string,unknown>>({});
 const busy=ref(false), error=ref(''), completed=ref(false),receipt=ref<[string,string][]>([]);
 const routeContext=ref<Record<string,unknown>>({});
 function reset(){form.value={...routeContext.value};error.value='';completed.value=false;receipt.value=[]}
 watch(active,reset);
 function pick(index:number){if(index>=0 && index<flow.operations.length)active.value=index}
 function label(op:ApiOperation){return operationLabel(op)}
 function fieldLabel(name:string){return t('fast.field.'+name)}
 function required(name:string){return ['realName','idCardNo','newPassword','oldPassword','channel','target','newTarget','code','tripId','orderId','demandId','quoteId','postId','content','title','enterpriseName','businessLicenseNo','tracking','carrier','price','fee','amount','inviteCode','purchasePlace','reasonType'].includes(name)}
 function isLong(name:string){return ['note','reason','proofUrls','evidence','meta','labelUrls','urlsJson','content','address','remark'].includes(name)}
 function isSecret(name:string){return /password|idCardNo|passportNo|challengeCode|code$/i.test(name)}
 function update(name:string,value:unknown){form.value={...form.value,[name]:value}}
 function prefill(params:Record<string,unknown>){const target:Record<string,unknown>={};for(const k of ['orderId','tripId','demandId','quoteId','postId','id','callId'])if(params[k]!=null)target[k]=String(params[k]);routeContext.value={...target};form.value={...form.value,...target}}
 onLoad((params)=>prefill(params??{}));
 async function execute(){
  if(busy.value)return;
  error.value='';completed.value=false;receipt.value=[];
  if(!getBearer()){error.value=t('fast.needLogin');return}
  const op=current.value; if(!op)return;
  const body:Record<string,unknown>={};
  for(const field of op.bodyFields){
   const v=form.value[field.name];
   if(required(field.name)&&(v==null||String(v).trim()==='')){error.value=t('fast.fillRequired',{field:fieldLabel(field.name)});return}
   if((v==null||v==='')&&field.type==='boolean'){body[field.name]=false;continue}
   if(v==null||v==='')continue;
   if(field.type==='number'){
    const n=Number(v);if(!Number.isFinite(n)){error.value=t('fast.invalidNumber',{field:fieldLabel(field.name)});return}body[field.name]=n;
   }else if(field.type==='boolean')body[field.name]=v===true;
   else body[field.name]=String(v).trim();
  }
  busy.value=true;
  try {
   const result:unknown=await api.invoke(op,{body});
   if(result===false){error.value=t('fast.rejected');return}
   completed.value=true;
   if(result!==null&&result!==undefined){
    if(typeof result==='object'&&!Array.isArray(result))receipt.value=Object.entries(result as Record<string,unknown>).slice(0,14).map(([k,v])=>[k,v===null?t('common.notProvided'):typeof v==='object'?t('fast.complexResult'):String(v)]);
    else if(typeof result==='string'||typeof result==='number')receipt.value=[['result',String(result)]];
    else if(Array.isArray(result))receipt.value=[['count',String(result.length)]];
   }
  } catch(e) {error.value=uiErrorMessage(e)}
  finally{busy.value=false}
 }
 function submit(){
  if(busy.value)return;
  if(current.value.dangerous || /\/rpc\/(confirm_|award_|close_|request_cancel|accept_order|create_publish_deposit|refund_publish_deposit)/.test(current.value.path)){
   uni.showModal({title:t('fast.confirmTitle'),content:t('fast.confirmDescription'),success:r=>{if(r.confirm)void execute()}});
  }else void execute();
 }
 function back(){uni.navigateBack()}
 return proxyRefs({flow,active,current,fields,form,busy,error,completed,receipt,pick,label,fieldLabel,required,isLong,isSecret,update,submit,back});
}
