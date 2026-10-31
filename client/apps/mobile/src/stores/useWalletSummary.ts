import {ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {api} from '../api/client';
import {getBearer,type MyWallet} from '@kuaixie/sdk';
import {formatAmount} from '@kuaixie/i18n';
import {mobilePreferenceStyle} from '../utils/prototype';
export function useWalletSummary(){
 const prefs=preferenceRef,wallet=ref<MyWallet|null>(null),error=ref(''),loading=ref(false);
 async function load(){if(!getBearer()){error.value='account.signinRequired';wallet.value=null;return}loading.value=true;error.value='';try{wallet.value=await api.account.wallet()}catch{wallet.value=null;error.value='account.backendUnavailable'}finally{loading.value=false}}
 onShow(()=>{void load()});return {prefs,wallet,error,loading,load,formatAmount,mobilePreferenceStyle};
}
