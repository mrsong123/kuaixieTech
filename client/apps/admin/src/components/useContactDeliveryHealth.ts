import {computed,getCurrentScope,onScopeDispose,ref} from 'vue';
import {getBearer,subscribeSession,type ApiClient} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';

export const deliveryStates=['ready','idle','paused','disabled','not_checked','migration_required','database_unavailable','database_error','credentials_missing','credentials_unreadable','configuration_invalid','dispatch_unconfirmed','dispatch_failed','unexpected_error'] as const;
export type DeliveryState=typeof deliveryStates[number];
export interface DeliveryDiagnostic {channel:string;state:DeliveryState;stage:string;reasonCode:string;exceptionType?:string|null;sqlState?:string|null;databaseError?:number|null;missingTable?:string|null;observedAt:string}
export interface DeliveryHealth {
 checkedAt:string;state:string;dispatchConfigured:boolean;providerDeliveryVerified:boolean;
 configuration:{sms:DeliveryDiagnostic;email:DeliveryDiagnostic};
 lastRun:{checkedAt:string|null;state:string;sms:DeliveryDiagnostic;email:DeliveryDiagnostic};
 queue:{channel:string;status:string;total:number}[];queueDiagnostic?:DeliveryDiagnostic;
}
type JsonObject=Record<string,unknown>;
function object(value:unknown):JsonObject{if(!value||typeof value!=='object'||Array.isArray(value))throw new Error(t('contactHealth.invalid'));return value as JsonObject}
function date(value:unknown){return typeof value==='string'&&Number.isFinite(Date.parse(value))}
function diagnostic(raw:unknown):DeliveryDiagnostic{
 const value=object(raw);
 if(typeof value.channel!=='string'||!['sms','email','queue','legacy','native_ownership'].includes(value.channel)||!deliveryStates.includes(value.state as DeliveryState)||typeof value.stage!=='string'||!['configuration','dispatch','inspection'].includes(value.stage)||typeof value.reasonCode!=='string'||!/^(?:CONTACT|SMS|EMAIL|INTEGRATION|COUPLER|SERVICE)_[A-Z0-9_]{1,100}$/.test(value.reasonCode)||!date(value.observedAt))throw new Error(t('contactHealth.invalid'));
 if(value.exceptionType!=null&&(typeof value.exceptionType!=='string'||!/^[A-Za-z_$][A-Za-z0-9_$]{0,100}$/.test(value.exceptionType)))throw new Error(t('contactHealth.invalid'));
 if(value.sqlState!=null&&(typeof value.sqlState!=='string'||!/^[A-Za-z0-9]{5}$/.test(value.sqlState)))throw new Error(t('contactHealth.invalid'));
 if(value.missingTable!=null&&(typeof value.missingTable!=='string'||!/^t_[A-Za-z0-9_]{1,62}$/.test(value.missingTable)))throw new Error(t('contactHealth.invalid'));
 if(value.databaseError!=null&&!Number.isSafeInteger(value.databaseError))throw new Error(t('contactHealth.invalid'));
 return {channel:value.channel,state:value.state as DeliveryState,stage:value.stage,reasonCode:value.reasonCode,observedAt:value.observedAt as string,
  ...(value.exceptionType!=null?{exceptionType:value.exceptionType as string}:{}),...(value.sqlState!=null?{sqlState:value.sqlState as string}:{}),...(value.databaseError!=null?{databaseError:value.databaseError as number}:{}),...(value.missingTable!=null?{missingTable:value.missingTable as string}:{})};
}
export function checkedDeliveryHealth(raw:unknown):DeliveryHealth{
 const value=object(raw),configuration=object(value.configuration),lastRun=object(value.lastRun);
 if(!date(value.checkedAt)||typeof value.state!=='string'||!['ready','idle','paused','degraded','requires_reconciliation'].includes(value.state)||typeof value.dispatchConfigured!=='boolean'||typeof value.providerDeliveryVerified!=='boolean'||!Array.isArray(value.queue)||lastRun.checkedAt!==null&&!date(lastRun.checkedAt)||typeof lastRun.state!=='string')throw new Error(t('contactHealth.invalid'));
 const sms=diagnostic(configuration.sms),email=diagnostic(configuration.email);
 if(sms.channel!=='sms'||email.channel!=='email'||value.dispatchConfigured!==[sms,email].some(row=>row.state==='ready')||value.state==='ready'&&!value.dispatchConfigured)throw new Error(t('contactHealth.invalid'));
 const lastSms=diagnostic(lastRun.sms),lastEmail=diagnostic(lastRun.email);
 if(lastSms.channel!=='sms'||lastEmail.channel!=='email')throw new Error(t('contactHealth.invalid'));
 const queue=value.queue.map(rawRow=>{const row=object(rawRow),total=typeof row.total==='number'?row.total:typeof row.total==='string'&&/^\d+$/.test(row.total)?Number(row.total):NaN;
  if(!['phone','email'].includes(String(row.channel))||typeof row.status!=='string'||!/^[a-z_]{1,32}$/.test(row.status)||!Number.isSafeInteger(total)||total<0)throw new Error(t('contactHealth.invalid'));
  return {channel:String(row.channel),status:row.status,total};
 });
 return {checkedAt:value.checkedAt as string,state:value.state,dispatchConfigured:value.dispatchConfigured,providerDeliveryVerified:value.providerDeliveryVerified,configuration:{sms,email},lastRun:{checkedAt:lastRun.checkedAt as string|null,state:lastRun.state,sms:lastSms,email:lastEmail},queue,...(value.queueDiagnostic?{queueDiagnostic:diagnostic(value.queueDiagnostic)}:{})};
}
export function useContactDeliveryHealth(api:Pick<ApiClient,'request'>){
 const health=ref<DeliveryHealth|null>(null),error=ref(''),loading=ref(false);
 const initial=getBearer();let invalid=false,disposed=false,generation=0;
 function clear(){generation++;health.value=null;error.value='';loading.value=false}
 function current(){if(disposed)return false;if(invalid||getBearer()!==initial){invalid=true;clear();error.value=t('contactHealth.sessionChanged');return false}return true}
 const unsubscribe=subscribeSession(()=>{if(getBearer()!==initial)current()});
 if(getCurrentScope())onScopeDispose(()=>{disposed=true;unsubscribe();clear()});
 async function refresh(){
  if(!current())return;const request=++generation;loading.value=true;error.value='';health.value=null;
  try{const result=checkedDeliveryHealth(await api.request<unknown>('GET','/portal/admin/contact-delivery/health'));if(request===generation&&current())health.value=result}
  catch(e){if(request===generation&&current())error.value=uiErrorMessage(e)}
  finally{if(request===generation)loading.value=false}
 }
 const blocked=computed(()=>health.value?.queue.filter(row=>row.status==='blocked').reduce((sum,row)=>sum+row.total,0)||0);
 return {health,error,loading,blocked,refresh};
}
