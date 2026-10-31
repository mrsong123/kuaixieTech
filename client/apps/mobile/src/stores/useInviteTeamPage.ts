import {ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {api} from '../api/client';
import {getBearer} from '@kuaixie/sdk';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {t} from '@kuaixie/i18n';
export function useInviteTeamPage(){
const prefs=preferenceRef;
const rows=ref<Array<Record<string,unknown>>>([]),loading=ref(false),error=ref(''),loaded=ref(false);
const field=(row:Record<string,unknown>,names:string[])=>{for(const k of names){if(row[k]!=null)return String(row[k])}return ''};
async function load(){if(!getBearer()){error.value=t('account.signinRequired');rows.value=[];return}loading.value=true;error.value='';try{
 const p=await api.account.profile();const raw=await api.rpc('list_team_members',{root:p.id});
 if(!Array.isArray(raw))throw new Error(t('migration.invalidResponse'));
 rows.value=raw.filter((r):r is Record<string,unknown>=>typeof r==='object'&&r!==null&&!Array.isArray(r));loaded.value=true;
}catch(e){error.value=e instanceof Error?e.message:t('common.apiLoadError');rows.value=[]}finally{loading.value=false}}
onShow(()=>{void load()});
return {prefs,rows,loading,error,loaded,field,load,mobilePreferenceStyle};
}
