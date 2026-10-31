import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import type {PublicDemand} from '@kuaixie/sdk';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {formatAmount,formatDate} from '@kuaixie/i18n';
export function useDemandDetailPage(){
const prefs=preferenceRef,id=ref(''),item=ref<PublicDemand|null>(null),loading=ref(false),error=ref('');
onLoad((params)=>{id.value=String(params?.id||'');void load()});
async function load(){loading.value=true;error.value='';try{const rows=await api.portal.demands(100);item.value=rows.find(d=>String(d.id)===id.value)||null}catch{item.value=null;error.value='migration.demandUnavailable'}finally{loading.value=false}}
return {prefs,id,item,loading,error,load,formatAmount,formatDate,mobilePreferenceStyle};
}
