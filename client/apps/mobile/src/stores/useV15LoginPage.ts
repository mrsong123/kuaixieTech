import {onLoad} from '@dcloudio/uni-app';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {useBusinessAuth} from '@kuaixie/ui/useBusinessAuth';
import {t} from '@kuaixie/i18n';
import {api} from '../api/client';
export function useV15LoginPage(){
 let returnTrip='';
 const model=useBusinessAuth(api,{
  persistLocale:value=>uni.setStorageSync('kx.locale',value),
  sessionAccepted:()=>{uni.showToast({title:t('v15.loggedIn'),icon:'success'});if(returnTrip)uni.redirectTo({url:'/pages/create-intent/index?tripId='+encodeURIComponent(returnTrip)});else uni.switchTab({url:'/pages/me/index'})},
  openLegal:type=>uni.navigateTo({url:'/pages/legal-doc/index?type='+encodeURIComponent(type)}),
  openSupport:()=>uni.navigateTo({url:'/pages/support-center/index'}),
  wechatCode:()=>new Promise<string>((resolve,reject)=>uni.login({provider:'weixin',success:r=>resolve(r.code||''),fail:reject})),
 });
 onLoad(q=>{model.inviteCode.value=String(q?.code||'').replace(/[^a-zA-Z0-9_-]/g,'').slice(0,40);const match=String(q?.returnPath||'').match(/^\/pages\/create-intent\/index\?tripId=([a-zA-Z0-9_-]{1,80})$/);returnTrip=match?match[1]:''});
 return {...model,prefs:preferenceRef,mobilePreferenceStyle};
}
