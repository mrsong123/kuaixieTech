import {ref,computed,watch,onMounted,getCurrentScope,onScopeDispose} from 'vue';
import {ApiError,getBearer,subscribeSession,type ApiClient,type PaymentPlatform,type CurrencyInfo,type PaymentMethod,type FundingQuote,type FundingPayment,type ExchangeRate} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';

export function paymentStateKey(status:string){return ({creating:'pay.state.creating',pending:'pay.state.pending',paid:'pay.state.paid'} as Record<string,string>)[status]??'pay.state.unknown'}
export interface PaymentStorage {get(key:string):string|null;set(key:string,value:string):void;remove(key:string):void}
const UUID=/^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i;
const PAYMENT_ID=/^[a-f0-9]{32}$/i;
function amount(value:unknown){if(typeof value!=='string'||!/^\d{1,24}(\.\d{1,9})?$/.test(value))return null;const [whole,fraction='']=value.split('.');return BigInt(whole).toString()+'.'+fraction.replace(/0+$/,'')}
function positive(value:unknown){const normalized=amount(value);return normalized!==null&&normalized!== '0.'}
function paymentRow(row:FundingPayment,id?:string,expected?:FundingQuote){
 if(!row||!PAYMENT_ID.test(row.id)||id&&row.id!==id||!['creating','pending','paid'].includes(row.status)||row.baseCurrency!=='CNY'||!positive(row.baseAmount)||!positive(row.paymentAmount)||!(/^[A-Z]{3}$/).test(row.paymentCurrency)||typeof row.merchantName!=='string')throw new Error(t('pay.invalidResponse'));
 if(expected&&(amount(row.baseAmount)!==amount(expected.baseAmount)||amount(row.paymentAmount)!==amount(expected.paymentAmount)||row.paymentCurrency!==expected.paymentCurrency))throw new Error(t('pay.invalidResponse'));
 if(row.checkout){if(!['redirect','qr_text','wechat_jsapi','wechat_app','alipay_app'].includes(row.checkout.kind)||typeof row.checkout.value!=='string'||!row.checkout.value)throw new Error(t('pay.invalidResponse'));if(row.checkout.kind==='redirect'){try{const url=new URL(row.checkout.value);if(url.protocol!=='https:'||url.username||url.password)throw new Error()}catch{throw new Error(t('pay.invalidResponse'))}}}
 return row;
}
function quoteRow(row:FundingQuote,platform:PaymentPlatform){if(!row||!UUID.test(row.id)||row.platform!==platform||row.baseCurrency!=='CNY'||row.settlementCurrency!=='CNY'||!positive(row.baseAmount)||!positive(row.paymentAmount)||!Number.isFinite(Date.parse(row.expiresAt)))throw new Error(t('pay.invalidResponse'));return row}

/** One account/session owns this model. Only verified server profile IDs scope durable recovery. */
export function usePaymentWallet(client:ApiClient,platform:PaymentPlatform,onPaid?:()=>void,storage?:PaymentStorage){
 const initialSession=getBearer();let disposed=false,invalidSession=false,loadGeneration=0,quoteGeneration=0,statusGeneration=0,rateGeneration=0,restored=false;
 const ownerId=ref(''),invalidated=ref(false),currencies=ref<CurrencyInfo[]>([]),methods=ref<PaymentMethod[]>([]),merchantId=ref(''),currency=ref(''),amountCny=ref('');
 const quote=ref<FundingQuote|null>(null),payment=ref<FundingPayment|null>(null),error=ref(''),busy=ref(false),loading=ref(false),history=ref<FundingPayment[]>([]);
 const reference=ref<ExchangeRate|null>(null),referenceBase=ref('CNY'),referenceTarget=ref('');
 const selected=computed(()=>methods.value.find(m=>m.id===merchantId.value));
 const availableCurrencies=computed(()=>currencies.value.filter(c=>selected.value?.currencies.includes(c.code)));
 const activeMethods=computed(()=>methods.value.filter(m=>m.status==='configured'&&m.currencies.length));
 const pending=ref<{quoteId:string;requestId:string}|null>(null);
 const locked=computed(()=>invalidated.value||loading.value||busy.value||!!pending.value||!!payment.value&&payment.value.status!=='paid');
 let timer:ReturnType<typeof setInterval>|undefined;
 const notified=new Set<string>(),legacyStorageKey='kx.payment.pending.'+platform;
 const storageKey=()=>ownerId.value?'kx.payment.pending.v2.'+platform+'.'+ownerId.value:'';
 function stop(){if(timer)clearInterval(timer);timer=undefined}
 function clear(){loadGeneration++;quoteGeneration++;statusGeneration++;rateGeneration++;stop();quote.value=null;payment.value=null;pending.value=null;history.value=[];ownerId.value='';currencies.value=[];methods.value=[];merchantId.value='';currency.value='';amountCny.value='';reference.value=null;referenceBase.value='CNY';referenceTarget.value='';busy.value=false;loading.value=false;error.value='';notified.clear()}
 function isCurrent(){if(disposed)return false;if(invalidSession||getBearer()!==initialSession){if(!invalidSession){invalidSession=true;invalidated.value=true;clear()}error.value=t('pay.sessionChanged');return false}return true}
 const unsubscribe=subscribeSession(()=>{if(getBearer()!==initialSession)isCurrent()});
 if(getCurrentScope())onScopeDispose(()=>{disposed=true;unsubscribe();clear()});
 function persist(){if(!isCurrent()||!storageKey())return;try{if(pending.value||payment.value&&payment.value.status!=='paid')storage?.set(storageKey(),JSON.stringify({ownerId:ownerId.value,platform,pending:pending.value,quote:pending.value?quote.value:null,paymentId:payment.value?.id}));else storage?.remove(storageKey())}catch{}}
 function notifyPaid(row:FundingPayment){if(isCurrent()&&row.status==='paid'&&!notified.has(row.id)){notified.add(row.id);onPaid?.()}}
 function poll(){if(payment.value?.status==='paid'){stop();return}if(payment.value&&!timer)timer=setInterval(()=>{if(isCurrent()&&!busy.value)void refresh()},10000)}
 function resume(row:FundingPayment){if(!isCurrent()||!ownerId.value||locked.value||!history.value.some(item=>item.id===row.id))return;try{statusGeneration++;payment.value=paymentRow(row);quote.value=null;persist();poll()}catch(e){error.value=uiErrorMessage(e)}}
 function resetQuote(){if(!pending.value&&!payment.value)quote.value=null}
 watch([currency,amountCny],resetQuote);watch(merchantId,()=>{if(!pending.value&&!payment.value){quote.value=null;currency.value=''}});
 function label(item:CurrencyInfo){return `${item.code} · ${localeRef.value.startsWith('zh')?item.name:item.nameEn}`}
 async function restore(){if(restored||!isCurrent()||!ownerId.value)return;restored=true;let saved:{ownerId?:string;platform?:string;pending?:{quoteId:string;requestId:string};quote?:FundingQuote;paymentId?:string}|null=null;
  try{const raw=storage?.get(storageKey());if(raw)saved=JSON.parse(raw)}catch{try{storage?.remove(storageKey())}catch{}}
  if(!saved)return;
  if(saved.ownerId!==ownerId.value||saved.platform!==platform||saved.pending&&saved.paymentId){try{storage?.remove(storageKey())}catch{};return}
  if(saved.pending){try{if(!UUID.test(saved.pending.quoteId)||!UUID.test(saved.pending.requestId)||!saved.quote||saved.quote.id!==saved.pending.quoteId)throw new Error();const snapshot=quoteRow(saved.quote,platform);pending.value={quoteId:saved.pending.quoteId,requestId:saved.pending.requestId};quote.value=snapshot;error.value=t('pay.recoveredPending')}catch{try{storage?.remove(storageKey())}catch{};error.value=t('pay.invalidRecovery')}return}
  if(saved.paymentId){if(!PAYMENT_ID.test(saved.paymentId)){try{storage?.remove(storageKey())}catch{};return}const generation=++statusGeneration;try{const row=paymentRow(await client.payments.status(saved.paymentId),saved.paymentId);if(!isCurrent()||generation!==statusGeneration)return;payment.value=row;persist();poll();notifyPaid(row)}catch(e){if(isCurrent()&&generation===statusGeneration)error.value=uiErrorMessage(e)}}
 }
 async function load(){if(!isCurrent())return;const generation=++loadGeneration;loading.value=true;error.value='';try{
   const [catalog,capabilities,identity]=await Promise.all([client.payments.currencies(),client.payments.methods(platform),initialSession?client.account.profile():Promise.resolve(null)]);
   if(!isCurrent()||generation!==loadGeneration)return;currencies.value=catalog;methods.value=capabilities;
   if(identity){if(!UUID.test(identity.id)||ownerId.value&&ownerId.value!==identity.id.toLowerCase())throw new Error(t('pay.ownerUnverified'));ownerId.value=identity.id.toLowerCase();const rows=(await client.payments.history()).map(row=>paymentRow(row));if(!isCurrent()||generation!==loadGeneration)return;history.value=rows;await restore()}
 }catch(e){if(isCurrent()&&generation===loadGeneration)error.value=uiErrorMessage(e)}finally{if(generation===loadGeneration)loading.value=false}}
 async function preview(){
  if(!isCurrent()||locked.value||payment.value)return;error.value='';quote.value=null;
  if(!initialSession||!ownerId.value){error.value=t('pay.ownerUnverified');return}
  if(!merchantId.value||!currency.value||!/^\d{1,7}(\.\d{1,2})?$/.test(amountCny.value)){error.value=t('pay.fillFields');return}
  const body={merchantId:merchantId.value,paymentCurrency:currency.value,amountCny:amountCny.value},generation=++quoteGeneration;
  busy.value=true;try{const result=quoteRow(await client.payments.quote(body),platform);if(!isCurrent()||generation!==quoteGeneration)return;if(merchantId.value!==body.merchantId||currency.value!==body.paymentCurrency||amountCny.value!==body.amountCny)return;if(result.merchantId!==body.merchantId||result.paymentCurrency!==body.paymentCurrency||amount(result.baseAmount)!==amount(body.amountCny))throw new Error(t('pay.invalidResponse'));quote.value=result}catch(e){if(isCurrent()&&generation===quoteGeneration)error.value=uiErrorMessage(e)}finally{if(generation===quoteGeneration)busy.value=false}
 }
 function key(){return globalThis.crypto?.randomUUID?.()??'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const n=Math.floor(Math.random()*16);return(c==='x'?n:(n&3)|8).toString(16)})}
 async function confirm(expectedQuoteId?:string){
  if(!isCurrent()||!ownerId.value||loading.value||busy.value||payment.value)return;error.value='';
  if(expectedQuoteId&&expectedQuoteId!==(pending.value?.quoteId||quote.value?.id)){error.value=t('pay.changed');return}
  if(!pending.value){if(!quote.value)return;if(Date.parse(quote.value.expiresAt)<=Date.now()){error.value=t('pay.expired');return}pending.value={quoteId:quote.value.id,requestId:key()};persist()}
  const request={...pending.value},expected=quote.value;busy.value=true;try{
   const row=paymentRow(await client.payments.create(request),undefined,expected||undefined);if(!isCurrent())return;payment.value=row;pending.value=null;persist();poll();notifyPaid(row);
  }catch(e){if(isCurrent()){error.value=uiErrorMessage(e);if(e instanceof ApiError&&[400,403,404,409,422].includes(e.status)){pending.value=null;quote.value=null;persist()}}}finally{if(isCurrent())busy.value=false}
 }
 async function refresh(expectedId?:string){
  if(!isCurrent()||!payment.value||busy.value||expectedId&&expectedId!==payment.value.id)return;const id=payment.value.id,expected=payment.value,generation=++statusGeneration;busy.value=true;error.value='';
  try{const row=paymentRow(await client.payments.status(id),id);if(!isCurrent()||generation!==statusGeneration||payment.value?.id!==id)return;if(amount(row.baseAmount)!==amount(expected.baseAmount)||amount(row.paymentAmount)!==amount(expected.paymentAmount)||row.paymentCurrency!==expected.paymentCurrency)throw new Error(t('pay.invalidResponse'));payment.value=row;persist();poll();notifyPaid(row)}
  catch(e){if(isCurrent()&&generation===statusGeneration)error.value=uiErrorMessage(e)}finally{if(generation===statusGeneration)busy.value=false}
 }
 async function rate(){if(!isCurrent()||busy.value||!referenceTarget.value)return;const generation=++rateGeneration,base=referenceBase.value,target=referenceTarget.value;busy.value=true;error.value='';reference.value=null;try{const value=await client.payments.rate(base,target);if(isCurrent()&&generation===rateGeneration&&base===referenceBase.value&&target===referenceTarget.value)reference.value=value}catch(e){if(isCurrent()&&generation===rateGeneration)error.value=uiErrorMessage(e)}finally{if(generation===rateGeneration)busy.value=false}}
 function again(){if(!isCurrent()||payment.value?.status!=='paid')return;payment.value=null;quote.value=null;pending.value=null;persist()}
 onMounted(()=>{try{storage?.remove(legacyStorageKey)}catch{};void load()});
 return {isCurrent,isCurrentSession:isCurrent,history,resume,currencies,methods,activeMethods,merchantId,currency,amountCny,quote,payment,error,busy,loading,availableCurrencies,selected,pending,locked,reference,referenceBase,referenceTarget,label,load,preview,confirm,refresh,rate,again};
}
