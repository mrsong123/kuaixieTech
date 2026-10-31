import {ref,computed} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,type MyProfile} from '@kuaixie/sdk';
import {api} from '../api/client';
import {t} from '@kuaixie/i18n';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15InviteSharePage(){
const prefs=preferenceRef,profile=ref<MyProfile|null>(null),code=ref(''),busy=ref(false),error=ref('');const carrier=computed(()=>profile.value?.carrier_verify_status==='approved');
onShow(()=>{void load()});async function load(){code.value='';error.value='';if(!getBearer()){error.value=t('v15.loginRequired');return}busy.value=true;try{profile.value=await api.account.profile();if(carrier.value){const r=await api.rpc('get_operator_invite_code',{operatorId:profile.value?.id});if(typeof r==='string'&&/^[a-zA-Z0-9_-]{4,40}$/.test(r))code.value=r;else if(typeof r==='object'&&r!==null&&typeof (r as Record<string,unknown>).code==='string'){const value=String((r as Record<string,unknown>).code);if(/^[a-zA-Z0-9_-]{4,40}$/.test(value))code.value=value;}if(!code.value)throw new Error(t('v15.loadFail'))}}catch(e){error.value=e instanceof Error?e.message:t('v15.loadFail')}finally{busy.value=false}}
// The source requires fresh on-server face verification. No endpoint is available, so never enable share/copy/QR on local-only checks.
function verify(){uni.navigateTo({url:'/pages/carrier-verify/index'})}
return {prefs,profile,code,busy,error,carrier,load,verify,mobilePreferenceStyle};
}
