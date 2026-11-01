import {ref,proxyRefs} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {getBearer,type PublicReview} from '@kuaixie/sdk';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {formatDate,t} from '@kuaixie/i18n';
/** Public review data comes from the server; ratings are never fabricated. */
export function useOriginalReviews(){
const prefs=preferenceRef,target=ref(''),rows=ref<PublicReview[]>([]),loading=ref(false),error=ref('');
async function load(){if(!target.value){error.value=t('v14.reviewTargetMissing');return}loading.value=true;error.value='';try{
 const result=await api.prototype.reviews(target.value,50);
 if(!Array.isArray(result))throw new Error(t('migration.invalidResponse'));
 rows.value=result.filter((r):r is PublicReview=>r!=null&&r.hidden!==true&&String(r.target_id)===target.value&&Number.isFinite(Number(r.stars))&&Number(r.stars)>=1&&Number(r.stars)<=5);
}catch(e){rows.value=[];error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{loading.value=false}}
onLoad(async params=>{target.value=String(params?.id||'');if(!target.value&&getBearer()){
 try{const p=await api.account.profile();target.value=String(p.id||'')}catch{error.value=t('account.backendUnavailable')}
 }if(target.value)await load();else if(!error.value)error.value=t('v14.reviewTargetMissing');});
return proxyRefs({prefs,target,rows,loading,error,load,mobilePreferenceStyle,formatDate});
}
