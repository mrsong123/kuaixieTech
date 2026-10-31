<script setup lang="ts">
import {useIndexPage} from '../../stores/useIndexPage';
import OriginalMdiIcon from '../../components/OriginalMdiIcon.vue';
import OriginalCategoryExplorer from '../../components/plaza/OriginalCategoryExplorer.vue';
import OriginalTripCard from '../../components/plaza/OriginalTripCard.vue';
import OriginalDemandCard from '../../components/plaza/OriginalDemandCard.vue';
import {computed,onMounted,onUnmounted,ref,watch} from 'vue';
import {setLocale,t} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';
import {prototypeAccent} from '../../utils/prototype';
const {
 prefs,active,search,loading,tripError,demandError,rootStyle,reload,
 trips,demands,viewTrip,viewDemand,
 country,countryChoices,countryIndex,countryChanged,
 matchingProducts,productError
} = useIndexPage();
const languageLabel=computed(()=>{void localeRef.value;return t('prototype.locale.switch')});
function mixChannel(from:number,to:number,weight:number){return Math.round(from*weight+to*(1-weight))}
function relativeLuminance(rgb:number[]){const linear=rgb.map(value=>{const channel=value/255;return channel<=0.04045?channel/12.92:((channel+0.055)/1.055)**2.4});return 0.2126*linear[0]+0.7152*linear[1]+0.0722*linear[2]}
function contrast(foreground:number[],background:number[]){const left=relativeLuminance(foreground),right=relativeLuminance(background);return (Math.max(left,right)+0.05)/(Math.min(left,right)+0.05)}
const kickerColor=computed(()=>{
 const accent=prototypeAccent(prefs.value.accent);
 const dark=prefs.value.theme==='dark';
 const background=dark?[23,28,31]:[255,255,255];
 const ink=dark?[242,244,244]:[15,23,42];
 const source=[1,3,5].map(index=>parseInt(accent.slice(index,index+2),16));
 let color=ink;
 for(let weight=0.62;weight>=0;weight-=0.04){
  const next=source.map((channel,index)=>mixChannel(channel,ink[index],weight));
  color=next;
  if(contrast(next,background)>=4.5)break;
 }
 return '#'+color.map(channel=>channel.toString(16).padStart(2,'0')).join('');
});
function toggleLanguage(){const value=localeRef.value==='zh-CN'?'en-US':'zh-CN';setLocale(value);uni.setStorageSync('kx.locale',value)}
const reaches=['international','domestic','intercity','city','village','neighbor'] as const;
type Reach=typeof reaches[number];
const reach=ref<Reach|''>('');
function setReach(value:Reach){reach.value=reach.value===value?'':value}
function reachOf(value?:string|null):Reach|''{
 const key=String(value||'').toLowerCase();
 if(['international','cross_border','overseas','crossborder'].includes(key))return 'international';
 if(key==='intercity')return 'intercity';
 if(['local','same_city','intracity','within_city'].includes(key))return 'city';
 if(['domestic','inland','within_country'].includes(key))return 'domestic';
 return '';
}
const shownTrips=computed(()=>trips.value.filter(item=>!reach.value||reachOf(item.trip_type)===reach.value));
const shownDemands=computed(()=>demands.value.filter(item=>!reach.value||reachOf(item.trip_type)===reach.value));
const discovery=ref(false),discoveryEmpty=ref(false);
let hideTimer:ReturnType<typeof setTimeout>|undefined;
const productCategories=computed(()=>matchingProducts.value.map(item=>String(item.category_id||'')).filter(Boolean));
function syncDiscovery(){
 clearTimeout(hideTimer);
 const probe=Boolean(country.value)||Boolean(search.value.trim());
 if(!probe){discovery.value=false;discoveryEmpty.value=false;return}
 discovery.value=true;
 if(loading.value){discoveryEmpty.value=false;return}
 if(productError.value||matchingProducts.value.length===0){
  discoveryEmpty.value=true;
  hideTimer=setTimeout(()=>{discovery.value=false;discoveryEmpty.value=false},1600);
 }else discoveryEmpty.value=false;
}
watch([country,search,matchingProducts,loading,productError],syncDiscovery);
function lockDocumentScroll(locked:boolean){
 if(typeof document==='undefined')return;
 document.documentElement.style.overflow=locked?'hidden':'';
 document.body.style.overflow=locked?'hidden':'';
}
onMounted(()=>lockDocumentScroll(true));
onUnmounted(()=>{clearTimeout(hideTimer);lockDocumentScroll(false)});
</script>
<template>
 <view class="min-h-screen bg-background" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="rootStyle">
  <view class="px-4 pt-6 pb-4" style="background-image:var(--brand-gradient)">
   <view class="flex flex-row items-center justify-between">
    <view class="flex-1">
     <view class="leading-7">
      <text class="text-xl font-bold" style="color:#ffffff">{{ $t('home.brand') }}</text>
     </view>
     <view class="mt-1 leading-5">
      <text class="text-xs" style="color:rgba(255,255,255,0.85)">{{ $t('home.slogan') }}</text>
     </view>
    </view>
    <view class="flex flex-row items-center gap-1 px-3 py-1 rounded-full max-w-40" style="border-width:1px;border-style:solid;border-color:rgba(255,255,255,0.6);background-color:rgba(255,255,255,0.15)" @click="toggleLanguage">
     <view class="i-mdi-translate w-4 h-4 shrink-0" style="color:#ffffff"><OriginalMdiIcon name="translate" tone="white"/></view>
     <text class="text-xs font-bold truncate" style="color:#ffffff">{{ languageLabel }}</text>
     <text class="text-xs shrink-0" style="color:rgba(255,255,255,0.75)">▾</text>
    </view>
   </view>
   <view id="nav-search" class="nav-searchbar">
    <view class="nav-left">
     <picker id="nav-search-dropdown-card" class="nav-search-scope" mode="selector" :range="countryChoices" range-key="name" :value="countryIndex" @change="countryChanged">
      <view class="nav-search-facade">
       <text id="nav-search-label-id" class="nav-search-label">{{ countryChoices[countryIndex]?.name||$t('prototype.plaza.countryAll') }}</text>
       <view class="nav-icon"></view>
      </view>
     </picker>
    </view>
    <view class="nav-fill">
     <view class="nav-search-field">
      <input class="nav-input" v-model="search" :placeholder="$t('home.searchPlaceholder')" placeholder-style="color:#6f7373" confirm-type="search"/>
     </view>
    </view>
    <view class="nav-right">
     <view class="nav-search-submit" aria-label="Go">
      <view class="nav-search-submit-text"></view>
     </view>
    </view>
   </view>
   <view class="reach-row" :class="{even:localeRef==='zh-CN'}">
    <text v-for="item in reaches" :key="item" class="reach-word" :class="{chosen:reach===item}" @click="setReach(item)">{{ $t('plaza.reach.'+item) }}</text>
   </view>
  </view>

  <scroll-view scroll-y class="py-4" style="height:70vh" :show-scrollbar="false">
   <view class="w-full px-4 pb-4">
    <view v-if="discovery" class="menu-slot">
     <OriginalCategoryExplorer v-if="!discoveryEmpty" :country="country" :query="search.trim()" :focus="productCategories" compact/>
     <text v-else class="menu-empty">{{ $t('migration.searchEmpty') }}</text>
    </view>
    <text class="feed-kicker" :style="{color:kickerColor}">{{ $t(active==='demands'?'plaza.feed.demands':'plaza.feed.trips') }}</text>
    <template v-if="active==='demands'">
     <view v-if="loading" class="flex flex-col items-center py-10">
      <text class="text-xs text-muted-foreground mt-2">{{ $t('portal.square.loading') }}</text>
     </view>
     <view v-else-if="demandError" class="flex flex-col items-center justify-center py-12">
      <text class="mt-3 text-sm font-medium text-muted-foreground">{{ $t('portal.square.noDemandApi') }}</text>
     </view>
     <view v-else-if="!shownDemands.length" class="flex flex-col items-center justify-center py-12">
      <text class="mt-3 text-sm font-medium text-muted-foreground">{{ $t(reach?'portal.square.noScope':'portal.square.emptyDemands') }}</text>
     </view>
     <OriginalDemandCard v-for="demand in shownDemands" :key="demand.id" :demand="demand" @open="viewDemand"/>
    </template>
    <template v-else>
     <view v-if="loading" class="flex flex-col items-center py-10">
      <text class="text-xs text-muted-foreground mt-2">{{ $t('portal.square.loading') }}</text>
     </view>
     <view v-else-if="tripError" class="flex flex-col items-center justify-center py-12">
      <text class="mt-3 text-sm font-medium text-muted-foreground">{{ $t('common.apiLoadError') }}</text>
     </view>
     <view v-else-if="!shownTrips.length" class="flex flex-col items-center justify-center py-12">
      <text class="mt-3 text-sm font-medium text-muted-foreground">{{ $t('portal.square.noScope') }}</text>
     </view>
     <view v-for="trip in shownTrips" :key="String(trip.id)">
      <OriginalTripCard :trip="trip" @open="viewTrip"/>
     </view>
    </template>

    <view class="mt-6 rounded-xl p-4" style="background-color:var(--primary-08)">
      <view class="flex flex-row items-center gap-2 mb-2">
       <view class="i-mdi-handshake-outline w-5 h-5" style="color:var(--primary)"><OriginalMdiIcon name="handshake-outline" tone="accent"/></view>
       <text class="text-sm font-bold" style="color:var(--primary)">{{ $t('home.depositTitle') }}</text>
      </view>
      <text class="text-xs leading-relaxed" style="color:var(--muted-foreground)">{{ $t('home.depositBody') }}</text>
     </view>
   </view>
  </scroll-view>
 </view>
</template>
<style scoped>
/* weapp-tailwindcss, designWidth 750: 1px = 2rpx. Class names are the original index.tsx utilities. */
.min-h-screen{min-height:100vh;display:flex;flex-direction:column;overflow:hidden;box-sizing:border-box;height:calc(100vh - var(--window-top,0px) - var(--window-bottom,0px));color:var(--foreground,#0f172a)}
.bg-background{background:var(--background,#ffffff)}
.px-4{padding-left:32rpx;padding-right:32rpx}
.pt-6{padding-top:48rpx}
.pb-4{padding-bottom:32rpx}
.flex{display:flex}
.flex-row{flex-direction:row}
.flex-col{flex-direction:column}
.items-center{align-items:center}
.items-stretch{align-items:stretch}
.justify-between{justify-content:space-between}
.justify-center{justify-content:center}
.flex-1{flex:1 1 0%;min-width:0}
.leading-7{line-height:56rpx}
.leading-5{line-height:40rpx}
.leading-relaxed{line-height:1.625}
.text-xl{font-size:40rpx}
.text-base{font-size:32rpx}
.text-sm{font-size:28rpx}
.text-xs{font-size:24rpx}
.font-bold{font-weight:700}
.text-sm.font-medium{font-size:28rpx;font-weight:500}
.font-medium{font-weight:500}
.mt-1{margin-top:8rpx}
.mt-2{margin-top:16rpx}
.mt-3{margin-top:24rpx}
.mt-6{margin-top:48rpx}
.mb-2{margin-bottom:16rpx}
.mb-3{margin-bottom:24rpx}
.gap-1{gap:8rpx}
.gap-2{gap:16rpx}
.gap-3{gap:24rpx}
.px-2{padding-left:16rpx;padding-right:16rpx}
.px-3{padding-left:24rpx;padding-right:24rpx}
.py-1{padding-top:8rpx;padding-bottom:8rpx}
.py-2{padding-top:16rpx;padding-bottom:16rpx}
.py-4{padding-top:32rpx;padding-bottom:32rpx}
.py-10{padding-top:80rpx;padding-bottom:80rpx}
.py-12{padding-top:96rpx;padding-bottom:96rpx}
.p-3{padding:24rpx}
.p-4{padding:32rpx}
.rounded-lg{border-radius:16rpx}
.rounded-xl{border-radius:24rpx}
.rounded-full{border-radius:9999px}
.max-w-40{max-width:320rpx}
.w-full{width:100%}
.w-3{width:24rpx}.h-3{height:24rpx}
.w-4{width:32rpx}.h-4{height:32rpx}
.w-5{width:40rpx}.h-5{height:40rpx}
.w-6{width:48rpx}.h-6{height:48rpx}
.w-14{width:112rpx}.h-14{height:112rpx}
.shrink-0{flex-shrink:0}
.min-w-0{min-width:0}
.truncate{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.block{display:block}
.border{border-width:1px;border-style:solid;border-color:var(--border,#e2e8f0)}
.border-border{border-color:var(--border,#e2e8f0)}
.bg-card{background:var(--card,#ffffff)}
.bg-transparent{background:transparent}
.text-foreground{color:var(--foreground,#0f172a)}
.text-muted-foreground{color:var(--muted-foreground,#64748b)}
.text-primary{color:var(--primary,#0ea5e9)}
.nav-searchbar{margin-top:16px;display:flex;align-items:stretch;height:40px;border-radius:8px;overflow:hidden;background:#fff}
.nav-searchbar:focus-within{box-shadow:0 0 0 2px #fff,0 0 0 4px #ff9900}
.nav-left{flex:0 0 auto;display:flex;align-items:stretch;background:#e6e6e6;border-right:1px solid #cdcdcd}
.nav-search-scope{display:flex;height:40px}
.nav-search-facade{position:relative;display:flex;align-items:center;height:40px;padding:0 26px 0 12px}
.nav-search-label{width:auto;font-size:12px;font-weight:400;line-height:40px;color:#555;white-space:nowrap}
.nav-icon{position:absolute;right:10px;top:18px;width:0;height:0;border-left:4px solid transparent;border-right:4px solid transparent;border-top:5px solid #666}
.nav-fill{flex:1;min-width:0;display:flex;background:#fff}
.nav-search-field{flex:1;min-width:0;display:flex;align-items:center;background:#fff}
.nav-input{flex:1;width:100%;height:40px;min-height:40px;border:0;outline:none;background:#fff;font-size:15px;padding:0 10px;color:#0f1111}
.nav-right{flex:0 0 45px;display:flex}
.nav-search-submit{flex:1;display:flex;align-items:center;justify-content:center;background:#febd69}
.nav-search-submit-text{width:20px;height:20px;background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%230F1111' stroke-width='2.6' stroke-linecap='round'%3E%3Ccircle cx='10' cy='10' r='6'/%3E%3Cpath d='M14.8 14.8L20 20'/%3E%3C/svg%3E") center/18px 18px no-repeat}
.menu-slot{margin:4px 0 12px}
.menu-empty{display:block;padding:12px 0;color:var(--muted-foreground,#64748b);font-size:12px;line-height:1.5}
.reach-row{display:flex;flex-wrap:wrap;gap:10px 14px;margin-top:12px}
.reach-row.even{flex-wrap:nowrap;justify-content:space-between;gap:0}
.reach-word{color:rgba(255,255,255,.78);font-size:13px;line-height:1.4;background:transparent;border:0;padding:0}
.reach-word.chosen{color:#fff;font-weight:700}
.feed-kicker{display:block;margin:0 0 12px;font-size:16px;font-weight:700;line-height:1.4;color:var(--foreground,#0f172a)}
.py-4{flex:1;height:70vh;min-height:0;box-sizing:border-box}
</style>
<style>
#nav-search .uni-input-wrapper,
#nav-search .uni-input-form,
#nav-search .uni-input-input{height:40px;min-height:40px;line-height:40px;font-size:15px;background:transparent;border:0;padding:0}
#nav-search .uni-input-placeholder{line-height:40px;font-size:15px;color:#6f7373}
.nav-left:hover{background:#d4d4d4}
.nav-search-submit:hover{background:#f3a847}
.py-4,
.py-4 .uni-scroll-view{scrollbar-width:none;-ms-overflow-style:none}
.py-4::-webkit-scrollbar,
.py-4 .uni-scroll-view::-webkit-scrollbar{width:0;height:0;display:none}
</style>
