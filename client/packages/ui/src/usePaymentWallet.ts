import {ref,computed,watch,onMounted,onBeforeUnmount} from 'vue';
import {ApiError,getBearer,type ApiClient,type PaymentPlatform,type CurrencyInfo,type PaymentMethod,type FundingQuote,type FundingPayment,type ExchangeRate} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';

export function paymentStateKey(status:string){return ({creating:'pay.state.creating',pending:'pay.state.pending',paid:'pay.state.paid'} as Record<string,string>)[status]??'pay.state.unknown'}

/** Shared state across PC, H5, mini-program and App; only the server prices or confirms money. */
export interface PaymentStorage {get(key:string):string|null;set(key:string,value:string):void;remove(key:string):void}
export function usePaymentWallet(client:ApiClient,platform:PaymentPlatform,onPaid?:()=>void,storage?:PaymentStorage){
 const currencies=ref<CurrencyInfo[]>([]),methods=ref<PaymentMethod[]>([]),merchantId=ref(''),currency=ref(''),amountCny=ref('');
 const quote=ref<FundingQuote|null>(null),payment=ref<FundingPayment|null>(null),error=ref(''),busy=ref(false),loading=ref(false);
 const history=ref<FundingPayment[]>([]);
 const reference=ref<ExchangeRate|null>(null),referenceBase=ref('CNY'),referenceTarget=ref('');
 const selected=computed(()=>methods.value.find(m=>m.id===merchantId.value));
 const availableCurrencies=computed(()=>currencies.value.filter(c=>selected.value?.currencies.includes(c.code)));
 const activeMethods=computed(()=>methods.value.filter(m=>m.status==='configured'&&m.currencies.length));
 const pending=ref<{quoteId:string;requestId:string}|null>(null);
 const locked=computed(()=>busy.value||!!pending.value||!!payment.value&&payment.value.status!=='paid');
 let timer:ReturnType<typeof setInterval>|undefined;
 const storageKey='kx.payment.pending.'+platform;
 function persist(){try{if(pending.value||payment.value&&payment.value.status!=='paid')storage?.set(storageKey,JSON.stringify({pending:pending.value,paymentId:payment.value?.id}));else storage?.remove(storageKey)}catch{}}
 function resume(row:FundingPayment){if(pending.value||payment.value&&payment.value.status!=='paid')return;payment.value=row;quote.value=null;persist();if(row.status!=='paid'&&!timer)timer=setInterval(()=>{if(!busy.value)void refresh()},10000)}
 function resetQuote(){if(!pending.value&&!payment.value)quote.value=null}
 watch([currency,amountCny],resetQuote);watch(merchantId,()=>{if(!pending.value&&!payment.value){quote.value=null;currency.value=''}});
 function label(item:CurrencyInfo){return `${item.code} · ${localeRef.value.startsWith('zh')?item.name:item.nameEn}`}
 async function load(){loading.value=true;error.value='';try{
   const [catalog,capabilities]=await Promise.all([client.payments.currencies(),client.payments.methods(platform)]);
   currencies.value=catalog;methods.value=capabilities;
   if(getBearer())history.value=await client.payments.history();
 }catch(e){error.value=uiErrorMessage(e)}finally{loading.value=false}}
 async function preview(){
  if(locked.value)return;error.value='';quote.value=null;
  if(!merchantId.value||!currency.value||!/^\d{1,7}(\.\d{1,2})?$/.test(amountCny.value)){error.value=t('pay.fillFields');return}
  busy.value=true;try{quote.value=await client.payments.quote({merchantId:merchantId.value,paymentCurrency:currency.value,amountCny:amountCny.value})}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}
 }
 function key(){return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const n=Math.floor(Math.random()*16);return(c==='x'?n:(n&3)|8).toString(16)})}
 async function confirm(){
  if(busy.value||payment.value)return;error.value='';
  if(!pending.value){if(!quote.value)return;if(Date.parse(quote.value.expiresAt)<=Date.now()){error.value=t('pay.expired');return}pending.value={quoteId:quote.value.id,requestId:key()};persist()}
  busy.value=true;try{
   payment.value=await client.payments.create(pending.value);pending.value=null;persist();
   if(payment.value.status==='paid')onPaid?.();else if(!timer)timer=setInterval(()=>{if(!busy.value)void refresh()},10000);
  }catch(e){error.value=uiErrorMessage(e);if(e instanceof ApiError&&[400,403,404,409,422].includes(e.status)){pending.value=null;persist()}}finally{busy.value=false}
 }
 async function refresh(){
  if(!payment.value||busy.value)return;busy.value=true;error.value='';
  try{payment.value=await client.payments.status(payment.value.id);persist();if(payment.value.status==='paid'){if(timer)clearInterval(timer);timer=undefined;onPaid?.()}}
  catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}
 }
 async function rate(){if(!referenceTarget.value)return;busy.value=true;error.value='';reference.value=null;try{reference.value=await client.payments.rate(referenceBase.value,referenceTarget.value)}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 function again(){if(payment.value?.status!=='paid')return;payment.value=null;quote.value=null;pending.value=null;persist()}
 onMounted(()=>{void load();try{const raw=storage?.get(storageKey);if(raw){const saved=JSON.parse(raw) as {pending?:{quoteId:string;requestId:string};paymentId?:string};if(saved.pending&&typeof saved.pending.quoteId==='string'&&typeof saved.pending.requestId==='string')pending.value=saved.pending;if(saved.paymentId&&/^[a-f0-9]{32}$/.test(saved.paymentId))void client.payments.status(saved.paymentId).then(row=>{payment.value=row;persist()}).catch(e=>{error.value=uiErrorMessage(e)})}}catch{}});onBeforeUnmount(()=>{if(timer)clearInterval(timer)});
 return {history,resume,currencies,methods,activeMethods,merchantId,currency,amountCny,quote,payment,error,busy,loading,availableCurrencies,selected,pending,locked,reference,referenceBase,referenceTarget,label,load,preview,confirm,refresh,rate,again};
}
