import {ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {api} from '../api/client';
import {getBearer,type MyWallet} from '@kuaixie/sdk';
import {formatAmount} from '@kuaixie/i18n';
import {mobilePreferenceStyle} from '../utils/prototype';
import {usePrivatePageSession} from '../utils/usePrivatePageSession';
export function useWalletSummary(){
 const prefs=preferenceRef,wallet=ref<MyWallet|null>(null),error=ref(''),loading=ref(false);
 let generation=0;const scope=usePrivatePageSession(()=>{generation++;wallet.value=null;loading.value=false;error.value='account.signinRequired'});
 async function load(){const owner=scope.capture(),request=++generation;wallet.value=null;if(!owner.token){error.value='account.signinRequired';return}loading.value=true;error.value='';try{const result=await api.account.wallet();if(scope.current(owner)&&request===generation)wallet.value=result}catch{if(scope.current(owner)&&request===generation){wallet.value=null;error.value='account.backendUnavailable'}}finally{if(scope.current(owner)&&request===generation)loading.value=false}}
 onShow(()=>{void load()});return {prefs,wallet,error,loading,load,scope:scope.revision,formatAmount,mobilePreferenceStyle};
}
