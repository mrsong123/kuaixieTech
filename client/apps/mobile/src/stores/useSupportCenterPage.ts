import {ref} from 'vue';
import {getBearer} from '@kuaixie/sdk';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {t} from '@kuaixie/i18n';
export function useSupportCenterPage(){
const prefs=preferenceRef,busy=ref(false),error=ref('');
async function open(){if(!getBearer()){error.value=t('account.signinRequired');return}busy.value=true;error.value='';try{const session=await api.support.open();if(!session.id)throw new Error(t('migration.invalidResponse'));uni.navigateTo({url:'/pages/support-session/index?id='+encodeURIComponent(session.id)})}catch(e){error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{busy.value=false}}
return {prefs,busy,error,open,mobilePreferenceStyle};
}
