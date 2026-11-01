import {ref} from 'vue';
import {onLoad,onShow} from '@dcloudio/uni-app';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {formatAmount,formatDate,formatBusinessStatus} from '@kuaixie/i18n';
import {api} from '../api/client';
import {mobilePreferenceStyle} from '../utils/prototype';
import type {MyOrder} from '@kuaixie/sdk';
import {usePrivatePageSession} from '../utils/usePrivatePageSession';
export function useOrderSummary(){
 const prefs=preferenceRef,detail=ref<MyOrder|null>(null),loading=ref(false),error=ref(''),id=ref('');
 let generation=0;
 const scope=usePrivatePageSession(()=>{generation++;detail.value=null;loading.value=false;error.value='account.signinRequired'});
 async function load(){if(!id.value)return;const owner=scope.capture(),request=++generation,target=id.value;detail.value=null;if(!owner.token){error.value='account.signinRequired';return}loading.value=true;error.value='';try{const result=await api.account.order(target);if(!scope.current(owner)||request!==generation)return;if(String(result.id)!==target)throw new Error('ORDER_MISMATCH');detail.value=result}catch{if(scope.current(owner)&&request===generation){detail.value=null;error.value='account.backendUnavailable'}}finally{if(scope.current(owner)&&request===generation)loading.value=false}}
 onLoad(query=>{id.value=String(query?.id||'');void load()});
 onShow(()=>{if(id.value&&!detail.value&&!loading.value)void load()});
 return {prefs,detail,loading,error,load,formatAmount,formatDate,formatBusinessStatus,mobilePreferenceStyle};
}
