
import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';


import {ref,reactive,computed,getCurrentScope,onScopeDispose} from 'vue';

import {t,operationLabel,groupLabel,uiErrorMessage} from '@kuaixie/i18n';

import {operations,getBearer,subscribeSession,type ApiOperation,type ApiField} from '@kuaixie/sdk';

import {api,showError} from '../api/client';

export function useToolsPage(){

const prefs=preferenceRef;
const search=ref('');const group=ref('__all__');const chosen=ref('');const working=ref(false);const payload=reactive<Record<string,string>>({});const raw=ref('[]');const result=ref('');const error=ref('');
const categories=['__all__',...new Set(operations.map(o=>o.group))];
const filtered=computed(()=>operations.filter(o=>(group.value==='__all__'||o.group===group.value)&&(o.name+o.path).toLowerCase().includes(search.value.toLowerCase())));
const selected=computed(()=>operations.find(o=>o.id===chosen.value));
const initial=getBearer();let invalidSession=false,disposed=false,generation=0;
function clear(){generation++;chosen.value='';working.value=false;result.value='';error.value='';raw.value='[]';Object.keys(payload).forEach(key=>delete payload[key]);search.value='';group.value='__all__'}
function isCurrentSession(){if(disposed)return false;if(invalidSession||getBearer()!==initial){if(!invalidSession){invalidSession=true;clear()}error.value=t('fast.sessionChanged');return false}return true}
const unsubscribe=subscribeSession(()=>{if(getBearer()!==initial)isCurrentSession()});
if(getCurrentScope())onScopeDispose(()=>{disposed=true;unsubscribe();clear()});
function open(op:ApiOperation){if(!isCurrentSession())return;generation++;working.value=false;chosen.value=op.id;result.value='';error.value='';Object.keys(payload).forEach(k=>delete payload[k]);for(const f of op.bodyFields)payload[f.name]=f.type==='boolean'?'false':'';for(const k of [...op.pathParams,...op.queryParams])payload[k]='';raw.value='[]'}
function convert(f:ApiField){const v=payload[f.name]??'';if(f.type==='number')return v.trim()?Number(v):null;if(f.type==='boolean')return v==='true';if(f.type==='array')return JSON.parse(v||'[]');return v}
function fingerprint(){return JSON.stringify([chosen.value,payload,raw.value])}
function current(request:number,snapshot:string){if(!isCurrentSession()||request!==generation)return false;if(snapshot!==fingerprint()){error.value=t('fast.contextChanged');return false}return true}
async function invoke(){
 if(!isCurrentSession()||working.value)return;
 const op=selected.value;if(!op)return;
 if(op.role==='admin'){uni.showModal({title:t('common.adminOnlyTitle'),content:t('common.adminOnlyDescription'),showCancel:false});return;}
 if(op.access==='protected'&&!getBearer()){error.value=t('fast.needLogin');return}
 // Capture the operation and every request field before the native confirmation.
 // Existing requestId/idempotency fields remain in this exact body; no key is regenerated.
 const pathParams=Object.fromEntries(op.pathParams.map(x=>[x,payload[x]]));
 const query=Object.fromEntries(op.queryParams.filter(x=>payload[x]).map(x=>[x,payload[x]]));
 let body:unknown={};
 try{body=op.rawBody?JSON.parse(raw.value):Object.fromEntries(op.bodyFields.map(f=>[f.name,convert(f)]))}
 catch(e){error.value=t('common.invalidJson')+String(e);return}
 const request=++generation,snapshot=fingerprint();working.value=true;error.value='';result.value='';
 try{
  if(op.dangerous){
   const accepted=await new Promise<boolean>((resolve,reject)=>uni.showModal({title:t('common.confirmDanger'),content:t('common.dangerDescription'),success:response=>resolve(Boolean(response.confirm)),fail:reject}));
   if(!current(request,snapshot)||!accepted)return;
  }
  if(!current(request,snapshot))return;
  const data=await api.invoke(op,{pathParams,query,body});
  if(current(request,snapshot))result.value=typeof data==='string'?data:JSON.stringify(data,null,2);
 }catch(e){if(current(request,snapshot)){error.value=uiErrorMessage(e);showError(e)}}
 finally{if(isCurrentSession()&&request===generation)working.value=false}
}

return {prefs,search,group,chosen,working,payload,raw,result,error,categories,filtered,selected,open,convert,invoke,mobilePreferenceStyle,operationLabel,groupLabel,operations,api,isCurrentSession};
}
