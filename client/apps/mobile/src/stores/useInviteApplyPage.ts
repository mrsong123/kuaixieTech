import {ref} from 'vue';
import {getBearer} from '@kuaixie/sdk';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {t} from '@kuaixie/i18n';
export function useInviteApplyPage(){
const prefs=preferenceRef,code=ref(''),owner=ref(''),busy=ref(false),error=ref(''),finished=ref(false);
async function lookup(){if(!getBearer()){error.value=t('account.signinRequired');return}const value=code.value.trim().toUpperCase();if(!value){error.value=t('migration.codeRequired');return}busy.value=true;error.value='';owner.value='';try{const result=await api.rpc('get_invite_code_owner',{code:value});owner.value=result===null||result===false||result===undefined?'':String(result)}catch(e){error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{busy.value=false}}
async function accept(){if(!owner.value||busy.value)return;const result=await uni.showModal({title:t('migration.confirmBind'),content:t('migration.bindPermanent')});if(!result.confirm)return;busy.value=true;error.value='';try{const res=await api.rpc('consume_invite_code',{code:code.value.trim().toUpperCase()});if(res!==true&&String(res)!=='true')throw new Error(t('migration.bindFailed'));finished.value=true}catch(e){error.value=e instanceof Error?e.message:t('migration.bindFailed')}finally{busy.value=false}}
return {prefs,code,owner,busy,error,finished,lookup,accept,mobilePreferenceStyle};
}
