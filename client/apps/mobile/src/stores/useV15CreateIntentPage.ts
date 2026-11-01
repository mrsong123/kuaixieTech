import {ref,computed,watch} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {getBearer,ApiError,type PublicTrip,type PublicCategory,type OriginalIntentInput,type ConfirmedIntentInput,type IntentQuote} from '@kuaixie/sdk';
import {t,formatAmount,uiErrorMessage} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15CreateIntentPage(){
 const prefs=preferenceRef,id=ref(''),trip=ref<PublicTrip|null>(null),categories=ref<PublicCategory[]>([]),loading=ref(false),busy=ref(false),error=ref(''),done=ref('');
 const itemName=ref(''),itemSpec=ref(''),quantity=ref('1'),unit=ref('piece'),categoryId=ref(''),unitPrice=ref(''),note=ref(''),contactName=ref(''),contactPhone=ref(''),address=ref(''),expectDate=ref(''),insurance=ref<'yes'|'waived'|''>('');
 const insuranceQuoteId=ref<string|undefined>();
 watch(insurance,()=>{insuranceQuoteId.value=undefined});
 const quote=ref<IntentQuote|null>(null),pending=ref<ConfirmedIntentInput|null>(null);
 const units=['piece','box','bottle','kg'];const unitLabels=['v15.unitPiece','v15.unitBox','v15.unitBottle','v15.unitKg'];const unitIndex=computed(()=>Math.max(0,units.indexOf(unit.value)));
 const goodsAmount=computed(()=>Number(quantity.value)*Number(unitPrice.value));
 onLoad((q)=>{id.value=String(q?.tripId||q?.id||'');void load()});
 async function load(){
  if(!id.value){error.value=t('v15.invalidId');return}
  loading.value=true;error.value='';
  try{
   const [trips,cats]=await Promise.all([api.catalog.trips(100),api.catalog.categories(100)]);
   trip.value=trips.find(x=>String(x.id)===id.value)||null;categories.value=cats;
   if(!trip.value)error.value=t('v15.loadFail');
  }catch(e){error.value=uiErrorMessage(e)}finally{loading.value=false}
 }
 // Uniqueness key, not a credential. The server scopes it to the authenticated buyer.
 function requestId(){return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const n=Math.floor(Math.random()*16);return(c==='x'?n:(n&3)|8).toString(16)})}
 async function submit(){
  if(busy.value||done.value)return;
  error.value='';
  if(!getBearer()){error.value=t('v15.loginRequired');return}
  if(!pending.value){
   if(!id.value||!trip.value){error.value=t('v15.invalidId');return}
   if(!itemName.value.trim()||!contactName.value.trim()||!contactPhone.value.trim()||!address.value.trim()||!insurance.value){error.value=t('v15.formMissing');return}
   if(insurance.value==='yes'&&!insuranceQuoteId.value){error.value=t('insurance.selectionRequired');return}
   if(!/^\+?[0-9 \-]{6,24}$/.test(contactPhone.value.trim())){error.value=t('v15.phoneInvalid');return}
   if(!Number.isFinite(goodsAmount.value)||goodsAmount.value<=0||!Number.isInteger(Number(quantity.value))||Number(quantity.value)<1){error.value=t('v15.invalidAmount');return}
  }
  busy.value=true;
  try{
   if(!pending.value){
    const body:OriginalIntentInput={tripId:id.value,itemName:itemName.value.trim(),itemSpec:itemSpec.value.trim(),quantity:Number(quantity.value),unit:unit.value,categoryId:categoryId.value||undefined,expectUnitPrice:Number(unitPrice.value),note:note.value.trim(),contactName:contactName.value.trim(),contactPhone:contactPhone.value.trim(),address:address.value.trim(),expectDate:expectDate.value||undefined,insuranceChoice:insurance.value as 'yes'|'waived',insuranceQuoteId:insurance.value==='yes'?insuranceQuoteId.value:undefined};
    const priced=await api.originalV15.quoteIntent(body);
    if(priced.pricingVersion!=='escrow_v2_external_insurance'||priced.currency!=='CNY'||!Number.isFinite(Number(priced.buyerEscrow))||Number(priced.buyerEscrow)<=0)throw new Error(t('order.pricingUnavailable'));
    quote.value=priced;
    const answer=await uni.showModal({title:t('order.quoteTitle'),content:t('order.confirmEscrow',{goods:formatAmount(priced.goodsAmount,priced.currency),fee:formatAmount(priced.serviceFee,priced.currency),insurance:formatAmount(priced.insurancePremium,priced.currency),total:formatAmount(priced.buyerEscrow,priced.currency)})});
    if(!answer.confirm)return;
    pending.value={...body,requestId:requestId(),expectedBuyerEscrow:priced.buyerEscrow};
   }
   // Ambiguous network failures keep the exact request and key; retry cannot create a new charge.
   const result=await api.originalV15.createIntent(pending.value);
   if(!result?.id)throw new Error(t('v15.loadFail'));
   done.value=String(result.id);pending.value=null;
   uni.showToast({title:t('v15.intentCreated'),icon:'success'});
  }catch(e){
   error.value=uiErrorMessage(e);
   if(e instanceof ApiError&&[400,403,404,409,422].includes(e.status))pending.value=null;
  }finally{busy.value=false}
 }
 function selectAddress(value:{contactName:string;contactPhone:string;address:string}){if(busy.value||pending.value||done.value)return;contactName.value=value.contactName;contactPhone.value=value.contactPhone;address.value=value.address;quote.value=null;}
 function unitChange(e:any){unit.value=units[Number(e.detail.value)]||'piece'}
 function categoryChange(e:any){categoryId.value=String(categories.value[Number(e.detail.value)]?.id||'')}
 return {prefs,id,trip,categories,loading,busy,error,done,itemName,itemSpec,quantity,unit,categoryId,unitPrice,note,contactName,contactPhone,address,expectDate,insurance,unitLabels,unitIndex,goodsAmount,insuranceQuoteId,quote,pending,load,submit,selectAddress,unitChange,categoryChange,formatAmount,mobilePreferenceStyle};
}
