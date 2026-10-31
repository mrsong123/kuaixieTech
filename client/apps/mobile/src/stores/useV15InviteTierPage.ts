import {ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,type OriginalInviteTier} from '@kuaixie/sdk';
import {api} from '../api/client';
import {t} from '@kuaixie/i18n';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15InviteTierPage(){
const prefs=preferenceRef,summary=ref<OriginalInviteTier|null>(null),busy=ref(false),error=ref('');onShow(()=>{void load()});async function load(){error.value='';summary.value=null;if(!getBearer()){error.value=t('v15.loginRequired');return}busy.value=true;try{const r=await api.originalV15.inviteTier();if(!r||typeof r!=='object'||typeof r.tier!=='string')throw new Error(t('v15.loadFail'));summary.value=r}catch(e){error.value=e instanceof Error?e.message:t('v15.loadFail')}finally{busy.value=false}}
return {prefs,summary,busy,error,load,mobilePreferenceStyle};
}
