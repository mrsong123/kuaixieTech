import {ref,computed} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {getBearer,type PublicTrip,type PublicCategory} from '@kuaixie/sdk';
import {t,formatAmount} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15CreateIntentPage(){
const prefs=preferenceRef,id=ref(''),trip=ref<PublicTrip|null>(null),categories=ref<PublicCategory[]>([]),loading=ref(false),busy=ref(false),error=ref(''),done=ref('');
const itemName=ref(''),itemSpec=ref(''),quantity=ref('1'),unit=ref('piece'),categoryId=ref(''),unitPrice=ref(''),note=ref(''),contactName=ref(''),contactPhone=ref(''),address=ref(''),expectDate=ref(''),insurance=ref<'yes'|'waived'|''>('');
const units=['piece','box','bottle','kg'];const unitLabels=['v15.unitPiece','v15.unitBox','v15.unitBottle','v15.unitKg'];const unitIndex=computed(()=>Math.max(0,units.indexOf(unit.value)));
const goodsAmount=computed(()=>Number(quantity.value)*Number(unitPrice.value));
onLoad((q)=>{id.value=String(q?.tripId||q?.id||'');void load()});
async function load(){if(!id.value){error.value=t('v15.invalidId');return}loading.value=true;error.value='';try{const [trips,cats]=await Promise.all([api.catalog.trips(100),api.catalog.categories(100)]);trip.value=trips.find(x=>String(x.id)===id.value)||null;categories.value=cats.filter(c=>!trip.value?.destination_flag||!c.country_code||c.country_code===trip.value.destination_flag);if(!trip.value)error.value=t('v15.loadFail')}catch(e){error.value=e instanceof Error?e.message:t('v15.loadFail')}finally{loading.value=false}}
async function submit(){if(busy.value)return;error.value='';if(!getBearer()){error.value=t('v15.loginRequired');return}if(!id.value||!trip.value){error.value=t('v15.invalidId');return}if(!itemName.value.trim()||!contactName.value.trim()||!contactPhone.value.trim()||!address.value.trim()||!insurance.value){error.value=t('v15.formMissing');return}if(!/^\+?[0-9 \-]{6,20}$/.test(contactPhone.value.trim())){error.value=t('v15.phoneInvalid');return}if(!Number.isFinite(goodsAmount.value)||goodsAmount.value<=0||!Number.isInteger(Number(quantity.value))||Number(quantity.value)<1){error.value=t('v15.invalidAmount');return}
const modal=await uni.showModal({title:t('v15.createIntent'),content:t('v15.intentPostUnavailable')});if(!modal.confirm)return;
busy.value=true;try{const r=await api.originalV15.createIntent({tripId:id.value,itemName:itemName.value.trim(),itemSpec:itemSpec.value.trim(),quantity:Number(quantity.value),unit:unit.value,categoryId:categoryId.value||undefined,expectUnitPrice:Number(unitPrice.value),note:note.value.trim(),contactName:contactName.value.trim(),contactPhone:contactPhone.value.trim(),address:address.value.trim(),expectDate:expectDate.value||undefined,insuranceChoice:insurance.value as 'yes'|'waived'});if(!r?.id)throw new Error(t('v15.loadFail'));done.value=String(r.id);uni.showToast({title:t('v15.intentCreated'),icon:'success'})}catch(e){error.value=e instanceof Error?e.message:t('v15.intentPostUnavailable')}finally{busy.value=false}}
function unitChange(e:any){unit.value=units[Number(e.detail.value)]||'piece'}function categoryChange(e:any){categoryId.value=String(categories.value[Number(e.detail.value)]?.id||'')}
return {prefs,id,trip,categories,loading,busy,error,done,itemName,itemSpec,quantity,unit,categoryId,unitPrice,note,contactName,contactPhone,address,expectDate,insurance,unitLabels,unitIndex,goodsAmount,load,submit,unitChange,categoryChange,formatAmount,mobilePreferenceStyle};
}
