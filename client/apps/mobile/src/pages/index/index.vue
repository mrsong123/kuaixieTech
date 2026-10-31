<script setup lang="ts">
import {useIndexPage} from '../../stores/useIndexPage';
import OriginalMdiIcon from '../../components/OriginalMdiIcon.vue';
import OriginalCategoryExplorer from '../../components/plaza/OriginalCategoryExplorer.vue';
import OriginalTripCard from '../../components/plaza/OriginalTripCard.vue';
import OriginalDemandCard from '../../components/plaza/OriginalDemandCard.vue';
import {formatAmount} from '@kuaixie/i18n';
import {mobileProductName} from '../../utils/catalog';
import {navigate} from '../../router';
import {computed} from 'vue';
import {setLocale,t} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';
const {prefs,trips,demands,role,scopeOptions,active,region,search,loading,tripError,demandError,accent,rootStyle,typeOf,reload,roleMessageKey,visibleTrips,visibleDemands,matchingProducts,matchingTrips,matchingDemands,searchTotal,productError,productImage,viewProduct,viewTrip,viewDemand,openCatalog,choose,formatDate,transportLabel,country,countryChoices,countryIndex,countryChanged,direction,nearbyHint} = useIndexPage();
const languageLabel=computed(()=>{void localeRef.value;return t('prototype.locale.switch')});
function toggleLanguage(){const value=localeRef.value==='zh-CN'?'en-US':'zh-CN';setLocale(value);uni.setStorageSync('kx.locale',value)}
</script>
<template>
 <view class="plaza" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="rootStyle">
  <view class="hero">
    <view class="hero-top"><view class="hero-brand"><view class="hero-title">{{ $t('portal.square.title') }}</view><view class="hero-subtitle">{{ $t('portal.square.subtitle') }}</view></view><view class="hero-lang" @click="toggleLanguage">{{ languageLabel }}</view></view>
    <!-- Source layout: country picker on the left, search box on the right. -->
    <view class="hero-find-row">
      <picker class="country-picker" mode="selector" :range="countryChoices" range-key="name" :value="countryIndex" @change="countryChanged">
        <view class="country-control"><view class="country-globe"><OriginalMdiIcon name="earth" tone="muted"/></view><text class="country-label">{{ countryChoices[countryIndex]?.name||$t('prototype.plaza.countryAll') }}</text><text class="country-down">⌄</text></view>
      </picker>
      <view class="search-field"><view class="search-symbol"><OriginalMdiIcon name="magnify" tone="muted"/></view><input class="search-input" v-model="search" confirm-type="search" :placeholder="$t('portal.square.search')"/><view v-if="search" class="clear" @click="search=''"><OriginalMdiIcon name="close-circle" tone="muted"/></view></view>
    </view>
  </view>
  <view class="plaza-content">
   <template v-if="search.trim()">
     <view class="original-section-heading search-heading"><text>{{ $t('migration.searchResult',{query:search.trim()}) }}</text><text class="line-trust" @click="reload">{{ $t('common.retry') }}</text></view>
     <view v-if="loading" class="plaza-state">{{ $t('portal.square.loading') }}</view>
     <template v-else>
       <view class="search-group-heading">{{ $t('migration.searchProducts') }} · {{matchingProducts.length}}</view>
       <view v-if="productError" class="search-hint">{{ $t('common.apiLoadError') }}</view>
       <view v-for="product in matchingProducts" :key="String(product.id)" class="search-product" @click="viewProduct(product)">
          <image v-if="productImage(product)" class="search-product-image" :src="productImage(product)" mode="aspectFill"/>
          <view v-else class="search-product-image search-product-empty"><view class="search-product-placeholder"><OriginalMdiIcon name="package-variant" tone="muted"/></view></view>
          <view class="search-product-main"><text class="search-product-name">{{mobileProductName(product)}}</text><text v-if="product.region" class="search-product-region">{{product.region}}</text><text v-if="product.price!=null" class="search-product-price">{{formatAmount(product.price,product.currency)}}</text></view>
          <text class="search-product-arrow">›</text>
       </view>
       <view class="search-group-heading">{{ $t('migration.searchDemands') }} · {{matchingDemands.length}}</view>
       <view v-if="demandError" class="search-hint">{{ $t('portal.square.noDemandApi') }}</view>
       <OriginalDemandCard v-for="demand in matchingDemands" :key="String(demand.id)" :demand="demand" @open="viewDemand"/>
       <view class="search-group-heading">{{ $t('migration.searchTrips') }} · {{matchingTrips.length}}</view>
       <view v-if="tripError" class="search-hint">{{ $t('common.apiLoadError') }}</view>
       <OriginalTripCard v-for="trip in matchingTrips" :key="String(trip.id)" :trip="trip" @open="viewTrip"/>
       <view v-if="searchTotal===0" class="plaza-state">{{ $t('migration.searchEmpty') }}</view>
       <view class="search-hint">{{ $t('migration.searchLimited') }}</view>
     </template>
   </template>
   <template v-else>
   <view class="feed-tabs"><view class="feed-tab" :class="{current:active==='trips'}" @click="choose('trips')">{{ $t('portal.square.trips') }}</view><view class="feed-tab" :class="{current:active==='demands'}" @click="choose('demands')">{{ $t('portal.square.demands') }}</view><text class="refresh" @click="reload">↻</text></view>
   <view class="role-hint"><text class="role-dot"/>{{ $t(roleMessageKey) }}</view>
   <scroll-view scroll-x class="region-scroll"><view class="region-track"><view v-for="item in scopeOptions" :key="item" class="region-chip" :class="{chosen:region===item}" @click="region=item">{{ $t(item==='all'?'portal.square.allRegions':item==='domestic'?'portal.square.domestic':'portal.square.international') }}</view></view></scroll-view>
   <OriginalCategoryExplorer v-if="active==='trips'" :country="country"/>
   <view v-if="active==='trips'" class="original-route-section">
    <view class="original-section-heading"><text>{{ $t('home.linePlaza') }}</text><text class="line-trust">{{ $t('home.sortTrust') }}</text></view>
    <view class="direction-row"><view class="direction-chip" :class="{chosen:region==='all'}" @click="region='all'">{{ $t('home.allLines') }}</view><view class="direction-chip" :class="{chosen:region==='international'}" @click="region='international'">{{ $t('home.intlLine') }}</view><view class="direction-chip" :class="{chosen:region==='domestic'}" @click="region='domestic'">{{ $t('home.domesticLine') }}</view></view>
    <view class="direction-row soft-row"><view class="direction-chip soft" :class="{chosen:direction==='all'}" @click="direction='all'">{{ $t('home.dirAny') }}</view><view class="direction-chip soft" :class="{chosen:direction==='outbound'}" @click="direction='outbound'">{{ $t('home.dirOut') }}</view><view class="direction-chip soft" :class="{chosen:direction==='inbound'}" @click="direction='inbound'">{{ $t('home.dirIn') }}</view><view class="nearby-chip" @click="nearbyHint">⌾ {{ $t('home.nearMe') }}</view></view>
   </view>
   <view class="feed-count"><text>{{ $t('portal.square.listLimit') }}</text><text>{{active==='trips'?visibleTrips.length:visibleDemands.length}}</text></view>
   <view v-if="loading" class="plaza-state">{{ $t('portal.square.loading') }}</view>
   <template v-else-if="active==='trips'">
     <view v-if="tripError" class="plaza-state">{{ $t('common.apiLoadError') }}</view>
     <view v-else-if="!visibleTrips.length" class="plaza-state">{{ $t('portal.square.noScope') }}</view>
     <OriginalTripCard v-for="trip in visibleTrips" :key="String(trip.id)" :trip="trip" @open="viewTrip"/>
   </template>
   <template v-else>
     <view v-if="demandError" class="plaza-state">{{ $t('portal.square.noDemandApi') }}</view>
     <view v-else-if="!visibleDemands.length" class="plaza-state">{{ $t('portal.square.emptyDemands') }}</view>
     <OriginalDemandCard v-for="demand in visibleDemands" :key="demand.id" :demand="demand" @open="viewDemand"/>
   </template>
   <view class="plaza-shortcuts"><view class="shortcut" @click="navigate('publish-hub')"><text>＋</text>{{ $t('prototype.nav.publish') }}</view><view class="shortcut" @click="openCatalog"><text>▦</text>{{ $t('portal.square.browse') }}</view><view class="shortcut" @click="navigate('demand-pool')"><text>▶</text>{{ $t('portal.tabs.community') }}</view></view>
  </template>
  </view>
 </view>
</template>
<style scoped>
.plaza{--prototype-accent:#0ea5e9;--prototype-accent-text:white;min-height:100vh;background:#f8fafc;color:#0f172a;max-width:1100rpx;margin:auto;padding-bottom:calc(70rpx + env(safe-area-inset-bottom));font-size:27rpx}
/* #ifndef MP */
.plaza{font-size:calc(27rpx * var(--kx-user-scale,1))}
/* #endif */
.hero{padding:48rpx 30rpx 34rpx;background:linear-gradient(125deg,var(--prototype-accent),#38bdf8);color:var(--prototype-accent-text)}.hero-top{display:flex;align-items:center;justify-content:space-between;gap:16rpx}.hero-title{font-size:42rpx;font-weight:750;letter-spacing:-.5rpx;line-height:1.35}.hero-subtitle{font-size:23rpx;opacity:.9;margin-top:10rpx;line-height:1.55}.hero-mark{font-size:52rpx;font-weight:600}
.search-field{margin-top:34rpx;display:flex;align-items:center;height:86rpx;border-radius:17rpx;background:white;padding:0 20rpx;color:#64748b;gap:16rpx}.search-symbol{font-size:39rpx}.search-input{flex:1;min-width:0;font-size:26rpx;height:83rpx;color:#0f172a}.clear{width:36rpx;height:36rpx;flex:0 0 36rpx;display:flex;align-items:center;justify-content:center}
.plaza-content{padding:24rpx 28rpx}.feed-tabs{display:flex;align-items:center;gap:29rpx;min-height:77rpx;border-bottom:1rpx solid #e2e8f0}.feed-tab{font-size:30rpx;font-weight:600;padding:15rpx 0;border-bottom:5rpx solid transparent}.feed-tab.current{color:var(--prototype-accent);border-color:var(--prototype-accent)}.refresh{margin-left:auto;padding:14rpx;color:var(--prototype-accent);font-size:33rpx}.role-hint{display:flex;align-items:center;gap:10rpx;color:#64748b;font-size:22rpx;margin:23rpx 0 20rpx;line-height:1.55}.role-dot{height:12rpx;width:12rpx;border-radius:50%;background:var(--prototype-accent);flex:0 0 auto}.region-scroll{width:100%;white-space:nowrap}.region-track{display:flex;gap:13rpx;padding:3rpx 0 12rpx}.region-chip{border:1rpx solid #dce5ed;background:#fff;border-radius:99rpx;min-height:59rpx;min-width:123rpx;padding:9rpx 20rpx;color:#475569;text-align:center;font-size:24rpx;white-space:normal;display:flex;align-items:center;justify-content:center}.region-chip.chosen{background:var(--prototype-accent);border-color:var(--prototype-accent);color:var(--prototype-accent-text)}.feed-count{display:flex;align-items:center;justify-content:space-between;color:#94a3b8;font-size:20rpx;margin:22rpx 0 18rpx;gap:12rpx;line-height:1.5}
.feed-card{background:white;border:1rpx solid #e2e8f0;padding:25rpx 20rpx;display:flex;align-items:center;gap:17rpx;border-radius:19rpx;margin-bottom:17rpx;min-height:137rpx}.feed-icon{display:flex;align-items:center;justify-content:center;flex:0 0 72rpx;width:72rpx;height:72rpx;border-radius:19rpx;background:#e0f2fe;color:var(--prototype-accent);font-size:36rpx}.feed-info{flex:1;min-width:0}.feed-heading{font-size:29rpx;font-weight:710;overflow-wrap:anywhere;line-height:1.45}.feed-meta{font-size:22rpx;color:#64748b;margin-top:6rpx;overflow-wrap:anywhere}.feed-price{font-size:23rpx;margin-top:6rpx;color:var(--prototype-accent)}.feed-arrow{font-size:40rpx;color:#94a3b8}.plaza-state{padding:48rpx 28rpx;background:white;border:1rpx solid #e2e8f0;color:#64748b;text-align:center;border-radius:18rpx;font-size:25rpx;line-height:1.8}.plaza-shortcuts{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14rpx;margin-top:31rpx}.shortcut{min-width:0;border-radius:18rpx;border:1rpx solid #e2e8f0;background:white;display:flex;flex-direction:column;gap:11rpx;align-items:center;justify-content:center;text-align:center;padding:18rpx 9rpx;font-size:23rpx;line-height:1.4;overflow-wrap:anywhere}.shortcut text{font-size:39rpx;color:var(--prototype-accent)}
.kx-dark{background:#171c1f;color:#f2f4f4}.kx-dark .feed-card,.kx-dark .region-chip,.kx-dark .shortcut,.kx-dark .plaza-state{background:#202629;border-color:#475569;color:#f2f4f4}.kx-dark .feed-meta,.kx-dark .role-hint{color:#cbd5e1}.kx-care .feed-tab,.kx-care .feed-heading,.kx-care .region-chip{font-size:30rpx}.kx-care .feed-card{min-height:156rpx}
@media(max-width:355px){.plaza-content{padding:18rpx}.feed-tabs{gap:18rpx}.region-chip{min-width:113rpx}}@media(min-width:768px){.plaza{max-width:920rpx}.plaza-content{padding:32rpx}}

/* Original Taro index.tsx: 16px side spacing, 24px top, 16px bottom, brand gradient. */
.plaza{max-width:1100rpx;background:#fff;--primary:var(--prototype-accent,#0ea5e9);--ring:var(--prototype-accent,#0ea5e9)}.hero{padding:48rpx 32rpx 32rpx;background:var(--brand-gradient,linear-gradient(135deg,#0284c7,#0ea5e9 55%,#38bdf8))}.hero-title{font-size:39rpx;font-weight:700;letter-spacing:0;line-height:1.35}.hero-subtitle{font-size:23rpx;opacity:.85;margin-top:7rpx}.hero-lang{flex:0 0 auto;padding:10rpx 15rpx;font-size:23rpx;color:white;border:1rpx solid #ffffff66;border-radius:14rpx}.hero-find-row{margin-top:32rpx;display:flex;align-items:stretch;gap:16rpx}.country-picker{flex:0 0 230rpx;width:230rpx;max-width:37%;min-width:0}.country-control{min-height:72rpx;height:100%;display:flex;align-items:center;gap:8rpx;background:rgba(255,255,255,.9);border-radius:16rpx;padding:0 24rpx;color:#0f172a;min-width:0}.country-globe{color:#0ea5e9;font-size:31rpx;width:31rpx;height:31rpx;flex:0 0 31rpx}.country-label{flex:1;min-width:0;font-size:28rpx;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.country-down{font-size:32rpx;color:#64748b}.hero .search-field{margin:0;min-width:0;flex:1;height:auto;min-height:72rpx;border-radius:16rpx;padding:0 24rpx;gap:16rpx;background:rgba(255,255,255,.9)}.hero .search-symbol{font-size:31rpx;width:31rpx;height:31rpx;flex:0 0 31rpx}.hero .search-input{font-size:28rpx;height:72rpx;min-width:0}.plaza-content{padding:25rpx 32rpx}.original-section-heading{display:flex;align-items:center;justify-content:space-between;margin:32rpx 0 18rpx;gap:14rpx;font-size:29rpx;font-weight:700}.line-trust{font-size:21rpx;color:#64748b;font-weight:400}.direction-row{display:flex;gap:14rpx;margin-bottom:12rpx;min-width:0}.direction-chip{display:flex;align-items:center;justify-content:center;flex:1;min-width:0;min-height:60rpx;border:1rpx solid var(--border,#e2e8f0);border-radius:12rpx;background:var(--card,#fff);font-size:23rpx;color:var(--muted-foreground,#64748b);padding:9rpx 12rpx;text-align:center;overflow-wrap:anywhere;line-height:1.35}.direction-chip.chosen{background:var(--primary,#0ea5e9);color:white;border-color:var(--primary,#0ea5e9)}.direction-chip.soft{min-height:54rpx;border-radius:99rpx;padding:7rpx 8rpx;font-size:21rpx}.direction-chip.soft.chosen{background:var(--primary-10,#e0f2fe);color:var(--primary,#0ea5e9)}.nearby-chip{display:flex;align-items:center;justify-content:center;min-height:54rpx;flex:0 0 auto;border:1rpx solid var(--border,#e2e8f0);border-radius:99rpx;padding:7rpx 12rpx;font-size:20rpx;color:var(--muted-foreground,#64748b)}
/* Original trip-card layout: rounded-xl border p-4 and distinct route / rating / fee rows. */
.plaza .feed-card{padding:28rpx 28rpx;border-radius:19rpx;margin-bottom:20rpx;min-height:145rpx}.plaza .feed-icon{border-radius:15rpx;width:66rpx;height:66rpx;flex-basis:66rpx}.plaza .feed-heading{font-size:29rpx}.plaza .feed-meta{font-size:22rpx}
.kx-dark .country-control,.kx-dark .hero .search-field,.kx-dark .direction-chip,.kx-dark .nearby-chip{background:#202629;color:#f2f4f4;border-color:#475569}.kx-dark .hero .search-input{color:#f2f4f4}
@media(max-width:355px){.hero{padding:36rpx 23rpx 25rpx}.plaza-content{padding:20rpx}.country-picker{flex-basis:197rpx;width:197rpx}.country-label{font-size:21rpx}.direction-row{gap:7rpx}.direction-chip{font-size:21rpx;padding:7rpx}.nearby-chip{font-size:18rpx;padding:6rpx}}
@media(min-width:768px){.plaza{max-width:920rpx}.plaza-content{padding:30rpx 32rpx}}

/* Original index.tsx search panel: product/demand/trip groups, 1px=2rpx. */
.search-heading{flex-wrap:wrap}.search-group-heading{font-weight:700;font-size:23rpx;margin:28rpx 0 17rpx;color:var(--muted-foreground,#64748b)}
.search-product{display:flex;align-items:center;gap:24rpx;padding:24rpx;margin-bottom:16rpx;background:var(--card,#fff);border:1rpx solid var(--border,#e2e8f0);border-radius:24rpx;min-width:0}
.search-product-image{width:112rpx;height:112rpx;flex:0 0 112rpx;border-radius:16rpx;object-fit:cover}.search-product-empty{background:var(--muted,#f1f5f9);display:flex;justify-content:center;align-items:center}.search-product-placeholder{width:48rpx;height:48rpx}
.search-product-main{flex:1;min-width:0;display:flex;flex-direction:column;gap:8rpx}.search-product-name{font-size:27rpx;font-weight:700;line-height:1.35;overflow-wrap:anywhere}.search-product-region{color:var(--muted-foreground,#64748b);font-size:22rpx;overflow-wrap:anywhere}.search-product-price{color:var(--primary,#0ea5e9);font-size:26rpx;font-weight:700}.search-product-arrow{color:var(--muted-foreground,#64748b);font-size:35rpx}.search-hint{font-size:22rpx;line-height:1.65;color:var(--muted-foreground,#64748b);margin:12rpx 0 26rpx}
</style>
