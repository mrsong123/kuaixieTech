import {ref,watch,onMounted,getCurrentScope,onScopeDispose} from 'vue';
import {getBearer,subscribeSession,type ApiClient,type InsuranceCompany,type InsuranceProduct,type InsuranceQuote,type InsuranceQuoteInput,type InsuranceApplication,type MyOrder,type BackendRecord,type PaymentPlatform} from '@kuaixie/sdk';
import {uiErrorMessage,t} from '@kuaixie/i18n';

/** An old view never adopts a replacement account, even if its callbacks outlive the page. */
function insuranceSession(clear:()=>void,error:{value:string}){
 const initial=getBearer();let invalid=false,disposed=false;
 function isCurrentSession(){
  if(disposed)return false;
  if(invalid||getBearer()!==initial){if(!invalid){invalid=true;clear()}error.value=t('case.sessionChanged');return false}
  return true;
 }
 const unsubscribe=subscribeSession(()=>{if(getBearer()!==initial)isCurrentSession()});
 if(getCurrentScope())onScopeDispose(()=>{disposed=true;unsubscribe();clear()});
 return isCurrentSession;
}
export function goodsForInsurance(price:string,quantity:number){if(!/^\d{1,7}(\.\d{1,2})?$/.test(price)||!Number.isInteger(quantity)||quantity<1||quantity>100)return '';const [major,fraction='']=price.split('.');const minor=(BigInt(major)*100n+BigInt(fraction.padEnd(2,'0')))*BigInt(quantity);return (minor/100n).toString()+'.'+(minor%100n).toString().padStart(2,'0')}
export function useInsuranceSelection(api:ApiClient,subject:()=>Omit<InsuranceQuoteInput,'productId'>,selected:(quote:InsuranceQuote|null)=>void,platform:PaymentPlatform='web'){
 const companies=ref<InsuranceCompany[]>([]),products=ref<InsuranceProduct[]>([]),product=ref(''),quote=ref<InsuranceQuote|null>(null),busy=ref(false),error=ref('');let generation=0,catalogGeneration=0;
 function invalidate(){generation++;busy.value=false;quote.value=null;selected(null)}
 function clear(){catalogGeneration++;invalidate();product.value='';error.value=''}
 const isCurrentSession=insuranceSession(clear,error);
 watch(()=>JSON.stringify(subject()),invalidate,{flush:'sync'});watch(product,invalidate,{flush:'sync'});
 async function load(){
  if(!isCurrentSession())return;const current=++catalogGeneration;
  const [directory,catalog]=await Promise.allSettled([api.insurance.companies(),api.insurance.products(platform)]);
  if(!isCurrentSession()||current!==catalogGeneration)return;
  if(directory.status==='fulfilled')companies.value=directory.value;
  if(catalog.status==='fulfilled')products.value=catalog.value;else error.value=uiErrorMessage(catalog.reason);
 }
 async function preview(){
  if(!isCurrentSession()||busy.value||!product.value)return;
  invalidate();const current=generation,input={...subject(),platform,productId:product.value};busy.value=true;error.value='';
  try{
   const result=await api.insurance.quote(input);
   if(!isCurrentSession()||current!==generation)return;
   const expiry=Date.parse(result.expiresAt);
   if(!Number.isFinite(expiry)||expiry<=Date.now()||result.protectionStatus!=='not_issued'||result.paymentMode!=='insurer_direct')throw new Error(t('insurance.unavailable'));
   quote.value=result;selected(result);
  }catch(e){if(isCurrentSession()&&current===generation)error.value=uiErrorMessage(e)}
  finally{if(current===generation)busy.value=false}
 }
 onMounted(()=>{void load()});return {companies,products,product,quote,busy,error,load,preview,isCurrentSession};
}
export function useInsuranceApplication(api:ApiClient,orderId:()=>string){
 const risk=ref({itemName:'',declaredValue:''});
 const quote=ref<InsuranceQuote|null>(null),application=ref<InsuranceApplication|null>(null),termsAccepted=ref(false),disclosuresAccepted=ref(false),busy=ref(false),error=ref('');let requestId='',generation=0;
 function reset(){generation++;risk.value={itemName:'',declaredValue:''};quote.value=null;application.value=null;requestId='';termsAccepted.value=false;disclosuresAccepted.value=false;busy.value=false;error.value=''}
 const isCurrentSession=insuranceSession(reset,error);
 function current(version:number,id:string){return isCurrentSession()&&version===generation&&id===orderId()}
 function checked(value:InsuranceApplication,id:string){if(!value||value.orderId!==id||value.paymentMode!=='insurer_direct')throw new Error(t('insurance.unavailable'));return value}
 async function load(){
  if(!isCurrentSession()||busy.value||!orderId())return;const id=orderId(),version=++generation;busy.value=true;error.value='';
  try{
   const order=await api.account.order(id);if(!current(version,id))return;
   risk.value={itemName:order.item_name,declaredValue:String(order.goods_amount)};
   const quoteId=String(order.insurance_quote_id||'');
   if(!quoteId){quote.value=null;application.value=null;return}
   const [storedQuote,result]=await Promise.all([api.insurance.ownQuote(quoteId),api.insurance.forOrder(id)]);
   if(!current(version,id))return;
   quote.value=storedQuote;application.value=result.application?checked(result.application,id):null;
  }catch(e){if(current(version,id))error.value=uiErrorMessage(e)}finally{if(current(version,id))busy.value=false}
 }
 async function apply(){
  if(!isCurrentSession()||busy.value||!quote.value||!orderId())return;
  if(!termsAccepted.value||!disclosuresAccepted.value){error.value=t('insurance.consentRequired');return}
  if(!requestId)requestId=globalThis.crypto?.randomUUID?.()??'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const n=Math.floor(Math.random()*16);return(c==='x'?n:(n&3)|8).toString(16)});
  const id=orderId(),version=++generation,input={orderId:id,quoteId:quote.value.id,requestId,termsAccepted:true,disclosuresAccepted:true};busy.value=true;error.value='';
  try{const result=await api.insurance.apply(input);if(current(version,id))application.value=checked(result,id)}
  catch(e){if(current(version,id))error.value=uiErrorMessage(e)}finally{if(current(version,id))busy.value=false}
 }
 async function refresh(){
  if(!isCurrentSession()||busy.value||!application.value)return;
  const id=orderId(),applicationId=application.value.id,version=++generation;busy.value=true;error.value='';
  try{const result=await api.insurance.status(applicationId);if(current(version,id)){const verified=checked(result,id);if(verified.id!==applicationId)throw new Error(t('insurance.unavailable'));application.value=verified}}
  catch(e){if(current(version,id))error.value=uiErrorMessage(e)}finally{if(current(version,id))busy.value=false}
 }
 watch(orderId,()=>{reset();if(isCurrentSession())void load()},{flush:'sync'});onMounted(()=>{void load()});
 function replaceQuote(value:InsuranceQuote|null){if(!isCurrentSession()||application.value||busy.value)return;generation++;quote.value=value;requestId='';termsAccepted.value=false;disclosuresAccepted.value=false}
 return {risk,replaceQuote,quote,application,termsAccepted,disclosuresAccepted,busy,error,load,apply,refresh,isCurrentSession};
}

export function useInsuranceCentre(api:ApiClient){
 const orders=ref<MyOrder[]>([]),applications=ref<InsuranceApplication[]>([]),consents=ref<BackendRecord[]>([]),selected=ref(''),busy=ref(false),error=ref(''),more=ref(false);let offset=0,generation=0;
 function clear(){generation++;offset=0;orders.value=[];applications.value=[];consents.value=[];selected.value='';busy.value=false;more.value=false;error.value=''}
 const isCurrentSession=insuranceSession(clear,error);
 async function load(append=false){
  if(!isCurrentSession()||busy.value)return;const current=++generation;busy.value=true;error.value='';
  try{
   const next=append?offset+100:0;
   const [rows,policies,receipts]=await Promise.all([api.account.orders('buyer',100,next),api.insurance.mine(),api.insurance.consents()]);
   if(!isCurrentSession()||current!==generation)return;
   orders.value=append?[...orders.value,...rows.filter(row=>row.insurance_quote_id)]:rows.filter(row=>row.insurance_quote_id);applications.value=policies;consents.value=receipts;more.value=rows.length===100;offset=next;
  }catch(e){if(isCurrentSession()&&current===generation)error.value=uiErrorMessage(e)}finally{if(current===generation)busy.value=false}
 }
 function selectOrder(id:string){if(isCurrentSession()&&(!id||orders.value.some(row=>row.id===id)||applications.value.some(row=>row.orderId===id)))selected.value=id}
 onMounted(()=>{void load()});return {orders,applications,consents,selected,busy,error,more,load,selectOrder,isCurrentSession};
}
export function insuranceStatusKey(state:string){return ({reserved:'insurance.stateReserved',dispatching:'insurance.stateDispatching',awaiting_provider:'insurance.stateAwaiting',scheduled:'insurance.stateScheduled',active:'insurance.stateActive',rejected:'insurance.stateRejected',cancelled:'insurance.stateCancelled',expired:'insurance.stateExpired'} as Record<string,string>)[state]||'insurance.stateUnknown'}
export function insurancePlatformKey(platform:string){return ({web:'insurance.platformWeb',h5:'insurance.platformH5',app:'insurance.platformApp',mp_weixin:'insurance.platformMini'} as Record<string,string>)[platform]||'insurance.platformOriginal'}
