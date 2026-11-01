import {computed, ref, watch, proxyRefs,getCurrentScope,onScopeDispose} from 'vue';
import {operationLabel,t,uiErrorMessage,formatAmount} from '@kuaixie/i18n';
import {getBearer,subscribeSession,type ApiOperation,type IntentQuote} from '@kuaixie/sdk';
import {getFlow,type ApiClient} from '@kuaixie/sdk';
export function useBusinessWorkflow(api:ApiClient,id:string,confirm:(title:string,content:string)=>Promise<boolean>){
 const flow=getFlow(id);
 const active=ref(0);
 const current=computed(()=>flow.operations[active.value]);
 const fields=computed(()=>(current.value?.bodyFields??[]).filter(field=>!['expectedBuyerEscrow','expectedFee','deposit','insuranceQuoteId'].includes(field.name)));
 const form=ref<Record<string,unknown>>({});
 const busy=ref(false), error=ref(''), completed=ref(false),receipt=ref<[string,string][]>([]);
 const insuranceSubject=ref<{subjectId:string;subjectType:'pk_quote';itemName:string;declaredValue:string}|null>(null);
 const routeContext=ref<Record<string,unknown>>({});
 const initial=getBearer();let disposed=false,invalidSession=false,generation=0;
 function clear(){generation++;busy.value=false;form.value={};routeContext.value={};insuranceSubject.value=null;completed.value=false;receipt.value=[];error.value=''}
 function isCurrentSession(){if(disposed)return false;if(invalidSession||getBearer()!==initial){if(!invalidSession){invalidSession=true;clear()}error.value=t('fast.sessionChanged');return false}return true}
 const unsubscribe=subscribeSession(()=>{if(getBearer()!==initial)isCurrentSession()});
 if(getCurrentScope())onScopeDispose(()=>{disposed=true;unsubscribe();clear()});
 watch(()=>JSON.stringify([form.value.demandId,form.value.quoteId,form.value.insuranceChoice]),()=>{insuranceSubject.value=null;delete form.value.insuranceQuoteId},{flush:'sync'});
 function fingerprint(){return JSON.stringify([active.value,current.value?.id,form.value,routeContext.value])}
 function contextMatches(request:number,snapshot:string){if(!isCurrentSession()||request!==generation)return false;if(snapshot!==fingerprint()){error.value=t('fast.contextChanged');return false}return true}
 async function loadInsuranceSubject(){if(!isCurrentSession()||busy.value)return;const request=++generation,snapshot=fingerprint(),demand=String(form.value.demandId||''),quote=String(form.value.quoteId||'');busy.value=true;error.value='';try{const value=await api.insurance.pkSubject(demand,quote);if(contextMatches(request,snapshot))insuranceSubject.value=value}catch(e){if(contextMatches(request,snapshot))error.value=uiErrorMessage(e)}finally{if(isCurrentSession()&&request===generation)busy.value=false}}
 function reset(){generation++;busy.value=false;form.value={...routeContext.value};error.value='';completed.value=false;receipt.value=[];insuranceSubject.value=null}
 watch(active,reset,{flush:'sync'});
 function pick(index:number){if(isCurrentSession()&&!busy.value&&Number.isInteger(index)&&index>=0 && index<flow.operations.length)active.value=index}
 function label(op:ApiOperation){return operationLabel(op)}
 function fieldLabel(name:string){return t('fast.field.'+name)}
 function required(name:string){return ['applicationId','realName','idCardNo','newPassword','oldPassword','channel','target','newTarget','code','tripId','orderId','demandId','quoteId','postId','content','title','enterpriseName','businessLicenseNo','tracking','carrier','price','fee','amount','inviteCode','purchasePlace','reasonType','insuranceChoice'].includes(name)}
 function isLong(name:string){return ['note','reason','proofUrls','evidence','meta','labelUrls','urlsJson','content','address','remark'].includes(name)}
 function isSecret(name:string){return /password|idCardNo|passportNo|challengeCode|code$/i.test(name)}
 function update(name:string,value:unknown){if(isCurrentSession()&&!busy.value)form.value={...form.value,[name]:value}}
 function prefill(params:Record<string,unknown>){if(!isCurrentSession())return;const target:Record<string,unknown>={};for(const k of ['orderId','tripId','demandId','quoteId','postId','id','callId'])if(params[k]!=null)target[k]=String(params[k]);routeContext.value={...target};reset()}
 async function submit(){
  if(!isCurrentSession()||busy.value)return;
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
  const request=++generation,snapshot=fingerprint();
  busy.value=true;
  try {
   if(!['/rpc/accept_order_atomic','/rpc/award_pk','/rpc/respond_shipping_fee'].includes(op.path)&&(op.dangerous || /\/rpc\/(confirm_|award_|close_|request_cancel|accept_order|create_publish_deposit|refund_publish_deposit)/.test(op.path))){
    const accepted=await confirm(t('fast.confirmTitle'),t('fast.confirmDescription'));
    if(!contextMatches(request,snapshot)||!accepted)return;
   }
   if(op.path==='/rpc/accept_order_atomic'){
    const order=await api.account.order(String(body.orderId));
    if(!contextMatches(request,snapshot))return;
    if(order.vendor_deposit==null||!Number.isFinite(Number(order.vendor_deposit))||Number(order.vendor_deposit)<0)throw new Error(t('order.pricingUnavailable'));
    const accepted=await confirm(t('order.quoteTitle'),t('order.confirmCarrierDeposit',{amount:formatAmount(String(order.vendor_deposit),'CNY')}));
    if(!contextMatches(request,snapshot)||!accepted)return;
    body.deposit=order.vendor_deposit;
   }
   if(op.path==='/rpc/award_pk'){
    if(body.insuranceChoice==='yes'&&!body.insuranceQuoteId)throw new Error(t('insurance.selectionRequired'));
    if(body.insuranceChoice!=='yes')delete body.insuranceQuoteId;
    const quote=await api.rpc('quote_pk_award',body) as IntentQuote;
    if(!contextMatches(request,snapshot))return;
    if(quote.pricingVersion!=='escrow_v2_external_insurance'||quote.currency!=='CNY')throw new Error(t('order.pricingUnavailable'));
    const accepted=await confirm(t('order.quoteTitle'),t('order.confirmEscrow',{goods:formatAmount(quote.goodsAmount,quote.currency),fee:formatAmount(quote.serviceFee,quote.currency),insurance:formatAmount(quote.insurancePremium,quote.currency),total:formatAmount(quote.buyerEscrow,quote.currency)}));
    if(!contextMatches(request,snapshot)||!accepted)return;
    body.expectedBuyerEscrow=quote.buyerEscrow;
   }
   if(op.path==='/rpc/respond_shipping_fee'&&body.accept===true){
    const order=await api.account.order(String(body.orderId));
    if(!contextMatches(request,snapshot))return;
    if(order.shipping_fee==null||!Number.isFinite(Number(order.shipping_fee)))throw new Error(t('order.pricingUnavailable'));
    const accepted=await confirm(t('order.quoteTitle'),t('order.confirmShipping',{fee:formatAmount(order.shipping_fee,'CNY')}));
    if(!contextMatches(request,snapshot)||!accepted)return;
    body.expectedFee=order.shipping_fee;
   }
   if(!contextMatches(request,snapshot))return;
   const result:unknown=await api.invoke(op,{body});
   if(!contextMatches(request,snapshot))return;
   if(result===false){error.value=t('fast.rejected');return}
   completed.value=true;
   if(result!==null&&result!==undefined){
    if(typeof result==='object'&&!Array.isArray(result))receipt.value=Object.entries(result as Record<string,unknown>).slice(0,14).map(([k,v])=>[k,v===null?t('common.notProvided'):typeof v==='object'?t('fast.complexResult'):String(v)]);
    else if(typeof result==='string'||typeof result==='number')receipt.value=[['result',String(result)]];
    else if(Array.isArray(result))receipt.value=[['count',String(result.length)]];
   }
  } catch(e) {if(contextMatches(request,snapshot))error.value=uiErrorMessage(e)}
  finally{if(isCurrentSession()&&request===generation)busy.value=false}
 }
 return proxyRefs({flow,active,current,fields,form,busy,error,completed,receipt,pick,label,fieldLabel,required,isLong,isSecret,update,submit,prefill,insuranceSubject,loadInsuranceSubject,isCurrentSession});
}
