import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {formatAmount,formatDate,formatBusinessStatus} from '@kuaixie/i18n';
import {api} from '../api/client';
import {mobilePreferenceStyle} from '../utils/prototype';
import type {MyOrder} from '@kuaixie/sdk';
export function useOrderSummary(){
 const prefs=preferenceRef,detail=ref<MyOrder|null>(null),loading=ref(false),error=ref(''),id=ref('');
 async function load(){if(!id.value)return;loading.value=true;error.value='';try{detail.value=await api.account.order(id.value)}catch{detail.value=null;error.value='account.backendUnavailable'}finally{loading.value=false}}
 onLoad(query=>{id.value=String(query?.id||'');void load()});
 return {prefs,detail,loading,error,load,formatAmount,formatDate,formatBusinessStatus,mobilePreferenceStyle};
}
