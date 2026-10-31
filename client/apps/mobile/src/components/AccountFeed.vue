<script setup lang="ts">
import {useAccountFeeds,type AccountFeedKind} from '../stores/useAccountFeeds';
const props=defineProps<{kind:AccountFeedKind}>();
const {prefs,items,loading,error,more,role,roleOptions,roleLabel,titleKey,fetchPage,chooseRole,select,read,headline,state,summary,when,mobilePreferenceStyle}=useAccountFeeds(props.kind);
</script>
<template>
<view class="app-page account-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
  <text class="account-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text>
  <view class="account-heading">{{$t(titleKey)}}</view>
  <view v-if="kind==='orders'" class="account-filters"><button v-for="option in roleOptions" :key="option" class="account-filter" :class="{'selected':role===option}" @click="chooseRole(option)">{{$t(roleLabel(option))}}</button></view>
  <view v-if="loading&&items.length===0" class="account-placeholder">{{$t('account.loading')}}</view>
  <view v-else-if="error" class="account-placeholder"><text>{{$t(error)}}</text><button class="account-action" @click="fetchPage()">{{$t('account.retry')}}</button></view>
  <view v-else-if="items.length===0" class="account-placeholder">{{$t('account.empty')}}</view>
  <view v-for="item in items" :key="item.id" class="account-entry" @click="select(item)">
   <view class="account-entry-top"><text class="account-entry-title">{{headline(item)}}</text><text class="account-entry-status">{{state(item)}}</text></view>
   <view class="account-entry-summary">{{summary(item)}}</view>
   <view class="account-entry-bottom"><text>{{when(item)}}</text><text v-if="kind==='orders'">{{$t('account.viewDetails')}} ›</text><text v-if="kind==='notifications'" class="account-read" @click.stop="read(item)">{{$t('account.notificationRead')}}</text></view>
  </view>
  <button v-if="more&&!loading" class="account-more" @click="fetchPage(false)">{{$t('account.more')}}</button>
  <view v-if="loading&&items.length>0" class="account-placeholder">{{$t('account.loading')}}</view>
</view>
</template>
<style scoped>
.account-page{background:#f7f9fc;padding:24rpx 26rpx 110rpx;min-height:100vh;box-sizing:border-box}.account-back{display:block;padding:12rpx 0;color:var(--kx-accent,#2476ce)}.account-heading{font-size:36rpx;font-weight:720;margin:25rpx 0;line-height:1.35}.account-filters{display:flex;gap:12rpx;margin-bottom:20rpx;flex-wrap:wrap}.account-filter{margin:0;line-height:1.5;font-size:23rpx;padding:10rpx 22rpx;background:#fff;color:#475569;border-radius:50rpx}.account-filter.selected{background:var(--kx-accent,#2476ce);color:#fff}.account-entry{border:1rpx solid #e1e7ef;border-radius:22rpx;background:#fff;padding:26rpx 24rpx;margin-bottom:17rpx;min-width:0}.account-entry-top{display:flex;gap:15rpx;justify-content:space-between;align-items:flex-start}.account-entry-title{font-weight:650;font-size:29rpx;min-width:0;flex:1;overflow-wrap:anywhere}.account-entry-status{font-size:21rpx;color:#64748b;max-width:38%;text-align:right;overflow-wrap:anywhere}.account-entry-summary{font-size:27rpx;margin:20rpx 0;color:#334155;overflow-wrap:anywhere}.account-entry-bottom{display:flex;justify-content:space-between;gap:12rpx;color:#64748b;font-size:22rpx;flex-wrap:wrap}.account-read{color:var(--kx-accent,#2476ce)}.account-placeholder{margin:60rpx auto;padding:36rpx 20rpx;text-align:center;color:#667085;line-height:1.7}.account-action,.account-more{margin:20rpx auto;background:var(--kx-accent,#2476ce);color:#fff;font-size:24rpx;border-radius:16rpx}.kx-dark .account-entry{background:#202629;border-color:#44515d}.kx-dark .account-entry-summary,.kx-dark .account-entry-title{color:#f1f5f9}
</style>
