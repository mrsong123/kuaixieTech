import {ref,watch,onMounted} from 'vue';
import type {ApiClient,InsuranceCompany,InsuranceProduct,InsuranceQuote,InsuranceQuoteInput,InsuranceApplication,MyOrder,BackendRecord,PaymentPlatform} from '@kuaixie/sdk';
import {uiErrorMessage,t} from '@kuaixie/i18n';
export function goodsForInsurance(price:string,quantity:number){if(!/^\d{1,7}(\.\d{1,2})?$/.test(price)||!Number.isInteger(quantity)||quantity<1||quantity>100)return '';const [major,fraction='']=price.split('.');const minor=(BigInt(major)*100n+BigInt(fraction.padEnd(2,'0')))*BigInt(quantity);return (minor/100n).toString()+'.'+(minor%100n).toString().padStart(2,'0')}
export function useInsuranceSelection(api:ApiClient,subject:()=>Omit<InsuranceQuoteInput,'productId'>,selected:(quote:InsuranceQuote|null)=>void,platform:PaymentPlatform='web'){
 const companies=ref<InsuranceCompany[]>([]),products=ref<InsuranceProduct[]>([]),product=ref(''),quote=ref<InsuranceQuote|null>(null),busy=ref(false),error=ref('');let generation=0;
 function invalidate(){generation++;quote.value=null;selected(null)}
 watch(()=>JSON.stringify(subject()),invalidate);watch(product,invalidate);
 async function load(){const [directory,catalog]=await Promise.allSettled([api.insurance.companies(),api.insurance.products(platform)]);if(directory.status==='fulfilled')companies.value=directory.value;if(catalog.status==='fulfilled')products.value=catalog.value;else error.value=uiErrorMessage(catalog.reason)}
 async function preview(){if(busy.value||!product.value)return;invalidate();const current=generation;busy.value=true;error.value='';try{const result=await api.insurance.quote({...subject(),platform,productId:product.value});if(current!==generation)return;if(Date.parse(result.expiresAt)<=Date.now()||result.protectionStatus!=='not_issued'||result.paymentMode!=='insurer_direct')throw new Error(t('insurance.unavailable'));quote.value=result;selected(result)}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 onMounted(()=>{void load()});return {companies,products,product,quote,busy,error,load,preview};
}
export function useInsuranceApplication(api:ApiClient,orderId:()=>string){
 const risk=ref({itemName:'',declaredValue:''});
 const quote=ref<InsuranceQuote|null>(null),application=ref<InsuranceApplication|null>(null),termsAccepted=ref(false),disclosuresAccepted=ref(false),busy=ref(false),error=ref('');let requestId='';
 async function load(){if(busy.value||!orderId())return;busy.value=true;error.value='';try{const order=await api.account.order(orderId());risk.value={itemName:order.item_name,declaredValue:String(order.goods_amount)};const id=String(order.insurance_quote_id||'');if(!id){quote.value=null;return}quote.value=await api.insurance.ownQuote(id);application.value=(await api.insurance.forOrder(orderId())).application}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 async function apply(){if(busy.value||!quote.value)return;if(!termsAccepted.value||!disclosuresAccepted.value){error.value=t('insurance.consentRequired');return}if(!requestId)requestId=globalThis.crypto?.randomUUID?.()??'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const n=Math.floor(Math.random()*16);return(c==='x'?n:(n&3)|8).toString(16)});busy.value=true;error.value='';try{application.value=await api.insurance.apply({orderId:orderId(),quoteId:quote.value.id,requestId,termsAccepted:termsAccepted.value,disclosuresAccepted:disclosuresAccepted.value})}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 async function refresh(){if(busy.value||!application.value)return;busy.value=true;error.value='';try{application.value=await api.insurance.status(application.value.id)}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 watch(orderId,()=>{quote.value=null;application.value=null;requestId='';termsAccepted.value=false;disclosuresAccepted.value=false;void load()});onMounted(()=>{void load()});
 function replaceQuote(value:InsuranceQuote|null){if(application.value)return;quote.value=value;requestId='';termsAccepted.value=false;disclosuresAccepted.value=false}
 return {risk,replaceQuote,quote,application,termsAccepted,disclosuresAccepted,busy,error,load,apply,refresh};
}

export function useInsuranceCentre(api:ApiClient){
 const orders=ref<MyOrder[]>([]),applications=ref<InsuranceApplication[]>([]),consents=ref<BackendRecord[]>([]),selected=ref(''),busy=ref(false),error=ref(''),more=ref(false);let offset=0;
 async function load(append=false){if(busy.value)return;busy.value=true;error.value='';try{const next=append?offset+100:0;const [rows,policies,receipts]=await Promise.all([api.account.orders('buyer',100,next),api.insurance.mine(),api.insurance.consents()]);orders.value=append?[...orders.value,...rows.filter(row=>row.insurance_quote_id)]:rows.filter(row=>row.insurance_quote_id);applications.value=policies;consents.value=receipts;more.value=rows.length===100;offset=next}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 onMounted(()=>{void load()});return {orders,applications,consents,selected,busy,error,more,load};
}
export function insuranceStatusKey(state:string){return ({reserved:'insurance.stateReserved',dispatching:'insurance.stateDispatching',awaiting_provider:'insurance.stateAwaiting',scheduled:'insurance.stateScheduled',active:'insurance.stateActive',rejected:'insurance.stateRejected',cancelled:'insurance.stateCancelled',expired:'insurance.stateExpired'} as Record<string,string>)[state]||'insurance.stateUnknown'}
export function insurancePlatformKey(platform:string){return ({web:'insurance.platformWeb',h5:'insurance.platformH5',app:'insurance.platformApp',mp_weixin:'insurance.platformMini'} as Record<string,string>)[platform]||'insurance.platformOriginal'}
