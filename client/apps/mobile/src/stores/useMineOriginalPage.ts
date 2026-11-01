import {computed,ref} from 'vue';
import {primaryNavigation} from '@kuaixie/sdk';
import {onShow} from '@dcloudio/uni-app';
import {api} from '../api/client';
import {getBearer,clearBearer,type MyProfile,type MyWallet,type MyNotification} from '@kuaixie/sdk';
import {formatAmount,t,getLocale,setLocale} from '@kuaixie/i18n';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

// Based on src/pages/mine/index.tsx from the user's original Taro prototype.
// All business values come from actual authenticated endpoints, never seeded defaults.
export function useMineOriginalPage(){
  const prefs=preferenceRef;
  const profile=ref<MyProfile|null>(null),wallet=ref<MyWallet|null>(null);
  const unread=ref<number|null>(null),loading=ref(false),error=ref('');
  const signedIn=computed(()=>Boolean(getBearer()));
  const nickname=computed(()=>profile.value?.nickname||t('mine.wechatUser'));
  const score=computed(()=>profile.value?.performance_score);
  const verified=computed(()=>profile.value?.carrier_verify_status==='approved');
  const buyerVerified=computed(()=>profile.value?.buyer_verify_status==='approved');
  const isEnterprise=computed(()=>profile.value?.user_role==='enterprise');
  const isAdmin=computed(()=>Boolean((profile.value as (MyProfile & {is_admin?:boolean})|null)?.is_admin));
  async function reload(){
    if(!getBearer()){profile.value=null;wallet.value=null;unread.value=null;error.value='';return}
    loading.value=true;error.value='';
    const results=await Promise.allSettled([api.account.profile(),api.account.wallet(),api.account.notifications(100,0)]);
    if(results[0].status==='fulfilled')profile.value=results[0].value;else{profile.value=null;error.value='mine.profileUnavailable'}
    wallet.value=results[1].status==='fulfilled'?results[1].value:null;
    unread.value=results[2].status==='fulfilled'?results[2].value.filter((x:MyNotification)=>!x.read_flag).length:null;
    loading.value=false;
  }
  onShow(()=>{void reload()});
  function open(path:string,tab=false){
    const url='/pages/'+path+'/index';
    if(tab)uni.switchTab({url});else uni.navigateTo({url});
  }
  function unavailable(){uni.showToast({title:t('mine.unavailable'),icon:'none',duration:2400})}
  function select(item:string){
    const routes:Record<string,[string,boolean?]>={
      orders:['my-orders'],wallet:['my-wallet'],notice:['my-notifications'],support:['support-center'],
      profile:['settings/profile'],publishTrip:['publish-hub',true],myTrips:['my-trips'],
      publishDemand:['publish-hub',true],buyerKyc:['buyer-verify'],carrierKyc:['carrier-verify'],enterpriseKyc:['enterprise-verify'],
      assist:['assist'],invoice:['invoice-request'],community:['demand-pool',true],myProducts:['my-products'],favorites:['favorites'],cart:['cart'],address:['address-book'],
      invites:['invite'],team:['invite-team'],inviteShare:['invite-share'],inviteApply:['invite-apply'],announcements:['announcements'],shipments:['my-shipments'],
      settings:['settings'],appearance:['settings/appearance'],admin:['admin'],login:['login']
    };
    const route=routes[item];if(!route){unavailable();return}
    open(route[0],route[1]);
  }
  function statusLabel(status?:string){return t(status==='approved'?'mine.kycApproved':status==='pending'?'mine.kycReviewing':status==='rejected'?'mine.kycRejected':'mine.kycNone')}
  function toggleLanguage(){const lang=getLocale()==='zh-CN'?'en-US':'zh-CN';setLocale(lang);uni.setStorageSync('kx.locale',lang);primaryNavigation.map(item=>item.titleKey).forEach((key,index)=>{try{uni.setTabBarItem({index,text:t(key)})}catch{}})}
  function signOut(){clearBearer();profile.value=null;wallet.value=null;unread.value=null;uni.showToast({title:t('mine.loggedOut'),icon:'none'})}
  return {prefs,profile,wallet,unread,loading,error,signedIn,nickname,score,verified,buyerVerified,isEnterprise,isAdmin,reload,select,statusLabel,toggleLanguage,signOut,formatAmount,mobilePreferenceStyle};
}
