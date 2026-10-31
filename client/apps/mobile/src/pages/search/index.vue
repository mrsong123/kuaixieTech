<script setup lang="ts">
import SettingsLayout from '../../components/SettingsLayout.vue';
import {useUnifiedSearchPage} from '../../stores/useUnifiedSearchPage';
import {formatAmount} from '@kuaixie/i18n';
const {term,busy,failed,kind,filterKinds,chooseKind,productMatches,tripMatches,mobileProductName,formatDate,openProduct,openTrip,reload}=useUnifiedSearchPage();
</script>
<template>
 <SettingsLayout titleKey="settings2.search" subtitleKey="settings2.searchDesc">
 <view class="settings-group"><view class="settings-control"><input v-model="term" class="settings-input" confirm-type="search" :placeholder="$t('settings2.searchPlaceholder')" /></view>
 <view class="search-kinds"><view v-for="opt in filterKinds" :key="opt.value" class="search-kind" :class="{chosen:kind===opt.value}" @click="chooseKind(opt.value)">{{$t(opt.labelKey)}}</view></view></view>
 <view v-if="busy" class="settings-empty">{{$t('m2.mobile.loading')}}</view>
 <view v-else-if="failed" class="settings-note">{{$t('common.apiLoadError')}} <button class="settings-action settings-action-secondary" @click="reload">{{$t('settings2.refresh')}}</button></view>
 <view class="settings-section-title">{{$t('settings2.search.products')}} · {{productMatches.length}}</view>
 <view class="settings-group"><view v-for="item in productMatches" :key="String(item.id)" class="settings-row" @click="openProduct(item)"><view class="settings-row-main"><view class="settings-row-label">{{mobileProductName(item)}}</view><view class="settings-row-desc">{{item.region||$t('common.notProvided')}} · {{formatAmount(item.price,item.currency)}}</view></view><text class="settings-row-arrow">›</text></view><view v-if="!productMatches.length" class="settings-row-desc search-empty">{{$t('settings2.search.empty')}}</view></view>
 <view class="settings-section-title">{{$t('settings2.search.trips')}} · {{tripMatches.length}}</view>
 <view class="settings-group"><view v-for="trip in tripMatches" :key="String(trip.id)" class="settings-row" @click="openTrip(trip)"><view class="settings-row-main"><view class="settings-row-label">{{trip.destination||$t('common.notProvided')}}</view><view class="settings-row-desc">{{formatDate(trip.depart_date)}} · {{trip.entry_port||$t('common.notProvided')}}</view></view><text class="settings-row-arrow">›</text></view><view v-if="!tripMatches.length" class="settings-row-desc search-empty">{{$t('settings2.search.empty')}}</view></view>
 <view class="settings-note">{{$t('settings2.searchLimit')}}</view>
 </SettingsLayout>
</template>
<style scoped>.search-kinds{display:flex;flex-wrap:wrap;gap:12rpx;padding:14rpx 20rpx 20rpx}.search-kind{border:1rpx solid #dce4ec;border-radius:100rpx;padding:11rpx 25rpx;font-size:25rpx;line-height:1.5;background:#fff;color:#475569}.search-kind.chosen{background:var(--kx-accent,#0ea5e9);color:var(--kx-accent-text,#fff);border-color:var(--kx-accent,#0ea5e9)}.search-empty{padding:32rpx;text-align:center}</style>
