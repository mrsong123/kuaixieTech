import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {getBearer,type OriginalDispute} from '@kuaixie/sdk';
import {t,formatAmount,formatDate} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15DisputeDetailPage(){
const prefs=preferenceRef,id=ref(''),entry=ref<OriginalDispute|null>(null),busy=ref(false),error=ref('');onLoad(q=>{id.value=String(q?.id||'');void load()});async function load(){if(!getBearer()){error.value=t('v15.loginRequired');return}if(!id.value){error.value=t('v15.invalidId');return}busy.value=true;error.value='';try{const r=await api.originalV15.dispute(id.value);if(!r||String(r.id)!==id.value)throw new Error(t('v15.loadFail'));entry.value=r}catch(e){error.value=e instanceof Error?e.message:t('v15.loadFail')}finally{busy.value=false}}
return {prefs,id,entry,busy,error,load,formatAmount,formatDate,mobilePreferenceStyle};
}
