import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {getBearer,type MyOrder,type OriginalDisputeInput} from '@kuaixie/sdk';
import {t,formatAmount} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15DisputeCreatePage(){
const prefs=preferenceRef,id=ref(''),order=ref<MyOrder|null>(null),type=ref<OriginalDisputeInput['type']>('quality'),resolution=ref<OriginalDisputeInput['expectResolution']>('refund_part'),description=ref(''),evidence=ref(''),amount=ref(''),error=ref(''),busy=ref(false),loading=ref(false),done=ref('');
const types:OriginalDisputeInput['type'][]=['quality','wrong_item','missing','damaged'];const labels=['v15.quality','v15.wrong','v15.missing','v15.damaged'];const resolutions:OriginalDisputeInput['expectResolution'][]=['refund_part','refund_full','resend','compensate'];const rl=['v15.partial','v15.full','v15.resend','v15.compensate'];
onLoad(q=>{id.value=String(q?.id||'');void load()});async function load(){if(!getBearer()){error.value=t('v15.loginRequired');return}if(!id.value){error.value=t('v15.invalidId');return}loading.value=true;error.value='';try{order.value=await api.account.order(id.value)}catch(e){error.value=e instanceof Error?e.message:t('v15.loadFail')}finally{loading.value=false}}
async function submit(){if(!order.value||busy.value)return;error.value='';const urls=evidence.value.split(/[\n,]+/).map(s=>s.trim()).filter(Boolean);if(!description.value.trim()||!urls.length||urls.length>4||urls.some(x=>!/^https:\/\//i.test(x))){error.value=t('v15.evidenceNeeded');return}const refund=resolution.value==='refund_part'?Number(amount.value):undefined;if(refund!==undefined&&(!Number.isFinite(refund)||refund<=0||refund>Number(order.value.goods_amount))){error.value=t('v15.invalidAmount');return}const check=await uni.showModal({title:t('v15.disputeCreate'),content:t('v15.confirmDispute')});if(!check.confirm)return;busy.value=true;try{const r=await api.originalV15.createDispute({orderId:id.value,type:type.value,description:description.value.trim(),evidenceUrls:urls,expectResolution:resolution.value,refundAmount:refund});if(!r?.id)throw new Error(t('v15.loadFail'));done.value=r.id;uni.navigateTo({url:'/pages/dispute-detail/index?id='+encodeURIComponent(r.id)})}catch(e){error.value=e instanceof Error?e.message:t('v15.disputeUnavailable')}finally{busy.value=false}}
return {prefs,id,order,type,resolution,description,evidence,amount,error,busy,loading,done,types,labels,resolutions,rl,submit,load,formatAmount,mobilePreferenceStyle};
}
