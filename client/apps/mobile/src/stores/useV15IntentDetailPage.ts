import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {getBearer,type MyOrder} from '@kuaixie/sdk';
import {formatAmount,formatDate,t} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15IntentDetailPage(){
const prefs=preferenceRef,id=ref(''),order=ref<MyOrder|null>(null),busy=ref(false),error=ref('');onLoad(q=>{id.value=String(q?.id||'');void load()});
async function load(){error.value='';order.value=null;if(!getBearer()){error.value=t('v15.loginRequired');return}if(!id.value){error.value=t('v15.invalidId');return}busy.value=true;try{const r=await api.account.order(id.value);if(!r||String(r.id)!==id.value)throw new Error(t('v15.loadFail'));order.value=r}catch(e){error.value=e instanceof Error?e.message:t('v15.loadFail')}finally{busy.value=false}}
function goto(path:string){uni.navigateTo({url:'/pages/'+path+'/index?id='+encodeURIComponent(id.value)})}
return {prefs,id,order,busy,error,load,goto,formatAmount,formatDate,mobilePreferenceStyle};
}
