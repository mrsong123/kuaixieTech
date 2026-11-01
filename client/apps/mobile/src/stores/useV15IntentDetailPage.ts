import {ref} from 'vue';
import {onLoad,onShow} from '@dcloudio/uni-app';
import {getBearer,type MyOrder} from '@kuaixie/sdk';
import {formatAmount,formatDate,t} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {usePrivatePageSession} from '../utils/usePrivatePageSession';

export function useV15IntentDetailPage(){
const prefs=preferenceRef,id=ref(''),order=ref<MyOrder|null>(null),busy=ref(false),error=ref('');let generation=0;
const scope=usePrivatePageSession(()=>{generation++;order.value=null;busy.value=false;error.value=t('v15.loginRequired')});
onLoad(q=>{id.value=String(q?.id||'');void load()});onShow(()=>{if(id.value&&!order.value&&!busy.value)void load()});
async function load(){const owner=scope.capture(),request=++generation,target=id.value;error.value='';order.value=null;if(!owner.token){error.value=t('v15.loginRequired');return}if(!target){error.value=t('v15.invalidId');return}busy.value=true;try{const r=await api.account.order(target);if(!scope.current(owner)||request!==generation)return;if(!r||String(r.id)!==target)throw new Error(t('v15.loadFail'));order.value=r}catch(e){if(scope.current(owner)&&request===generation)error.value=e instanceof Error?e.message:t('v15.loadFail')}finally{if(scope.current(owner)&&request===generation)busy.value=false}}
function goto(path:string){uni.navigateTo({url:'/pages/'+path+'/index?id='+encodeURIComponent(id.value)})}
return {prefs,id,order,busy,error,load,goto,formatAmount,formatDate,mobilePreferenceStyle};
}
