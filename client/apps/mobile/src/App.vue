<script lang="ts">
import {getLocale,setLocale,t,subscribeLocale} from '@kuaixie/i18n';
const pageTitleKeys:Record<string,string>={
  'pages/index/index':'prototype.nav.square',
  'pages/demand-pool/index':'portal.tabs.community',
  'pages/publish-hub/index':'prototype.nav.publish',
  'pages/messages/index':'prototype.nav.messages',
  'pages/route/index':'mobile.tab.route',
  'pages/discover/index':'mobile.tab.community',
  'pages/tools/index':'mobile.tab.tools',
  'pages/me/index':'mobile.tab.me',
  'pages/catalog/index':'m2.mobile.homeCatalog',
  'pages/trips/index':'m2.mobile.homeTrips',
  'pages/smart/index':'ai.title',
  'pages/settings/index':'settings2.title',
  'pages/settings/appearance/index':'settings2.appearance',
  'pages/settings/general/index':'settings2.general',
  'pages/settings/storage/index':'settings2.storage',
  'pages/settings/account/index':'settings2.account',
  'pages/settings/profile/index':'settings2.profile',
  'pages/settings/notification/index':'settings2.notification',
  'pages/settings/privacy/index':'settings2.privacy',
  'pages/settings/help/index':'settings2.help',
  'pages/settings/about/index':'settings2.about',
  'pages/search/index':'settings2.search',
  'pages/login/index':'v15.login',
  'pages/create-intent/index':'v15.createIntent',
  'pages/intent-detail/index':'v15.intentDetail',
  'pages/dispute-create/index':'v15.disputeCreate',
  'pages/dispute-detail/index':'v15.disputeDetail',
  'pages/invite-share/index':'v15.inviteShare',
  'pages/invite-tier/index':'v15.inviteTier',
  'pages/admin/index':'v15.mobileAdmin'
};
export default {onLaunch(){const previous=uni.getStorageSync('kx.locale');if(previous)setLocale(String(previous));subscribeLocale(()=>this.updateTabs());this.updateTabs()},onShow(){this.updateTabs()},onHide(){},updateTabs(){const pages=getCurrentPages();const current=pages[pages.length-1];const key=pageTitleKeys[current?.route??''];if(key)try{uni.setNavigationBarTitle({title:t(key)})}catch{};['prototype.nav.square','portal.tabs.community','prototype.nav.publish','prototype.nav.messages','mobile.tab.me'].forEach((key,index)=>{try{uni.setTabBarItem({index,text:t(key)})}catch{}})}};
</script>





<style>
@import "./styles/app.css";
</style>
