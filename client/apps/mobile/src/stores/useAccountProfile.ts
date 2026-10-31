import {ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {api} from '../api/client';
import type {MyProfile} from '@kuaixie/sdk';
import {getBearer} from '@kuaixie/sdk';
import {t} from '@kuaixie/i18n';
export function useAccountProfile(){
 const profile=ref<MyProfile|null>(null),nickname=ref(''),city=ref(''),loading=ref(false),saving=ref(false),error=ref('');
 async function load(){
  if(!getBearer()){error.value='account.signinRequired';profile.value=null;return}
  loading.value=true;error.value='';try{const value=await api.account.profile();profile.value=value;nickname.value=value.nickname||'';city.value=value.city||''}
  catch{profile.value=null;error.value='account.backendUnavailable'}finally{loading.value=false}
 }
 async function save(){
  const name=nickname.value.trim();if(!name||Array.from(name).length>64||Array.from(city.value).length>64){uni.showToast({title:t('account.invalid'),icon:'none'});return}
  saving.value=true;try{profile.value=await api.account.updateProfile({nickname:name,city:city.value.trim()});uni.showToast({title:t('account.saved'),icon:'none'})}
  catch{uni.showToast({title:t('account.backendUnavailable'),icon:'none'})}finally{saving.value=false}
 }
 onShow(()=>{void load()});return {profile,nickname,city,loading,saving,error,load,save};
}
