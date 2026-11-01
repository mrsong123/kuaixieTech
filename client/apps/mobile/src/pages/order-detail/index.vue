<script setup lang="ts">
import InsuranceApplicationMobile from '../../components/InsuranceApplicationMobile.vue';
import {useOrderSummary} from '../../stores/useOrderSummary';
const {prefs,detail,loading,error,load,formatAmount,formatDate,formatBusinessStatus,mobilePreferenceStyle}=useOrderSummary();
</script>
<template><view class="app-page account-order" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
 <text class="account-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text>
 <view class="account-title">{{$t('account.orderDetails')}}</view>
 <view v-if="loading" class="account-hint">{{$t('account.loading')}}</view>
 <view v-else-if="error" class="account-hint">{{$t(error)}}<button @click="load">{{$t('account.retry')}}</button></view>
 <view v-else-if="detail" class="account-card">
  <view class="account-name">{{detail.item_name}}</view>
  <view class="account-line"><text>{{$t('account.orderNumber')}}</text><text>{{detail.order_no}}</text></view>
  <view class="account-line"><text>{{$t('account.status')}}</text><text>{{formatBusinessStatus(detail.status)}}</text></view>
  <view class="account-line"><text>{{$t('account.quantity')}}</text><text>{{detail.quantity}} {{detail.unit}}</text></view>
  <view class="account-line"><text>{{$t('account.goodsAmount')}}</text><text>{{formatAmount(detail.goods_amount,'CNY')}}</text></view>
  <view class="account-line"><text>{{$t('account.serviceFee')}}</text><text>{{formatAmount(detail.service_fee,'CNY')}}</text></view>
  <view v-if="detail.shipping_fee!==undefined&&detail.shipping_fee!==null" class="account-line"><text>{{$t('account.shippingFee')}}</text><text>{{formatAmount(detail.shipping_fee,'CNY')}}</text></view>
  <view v-if="detail.tracking_no" class="account-line"><text>{{$t('account.trackingNo')}}</text><text>{{detail.tracking_no}}</text></view>
  <view class="account-line"><text>{{$t('account.created')}}</text><text>{{formatDate(detail.created_at)}}</text></view>
  <InsuranceApplicationMobile v-if="detail.insurance_quote_id&&detail.my_role==='buyer'" :order-id="String(detail.id)"/>
  <view class="account-note">{{$t('account.noCheckout')}}</view><button v-if="detail.status==='requesting'" class="account-migration-link" @click="uni.navigateTo({url:'/pages/intent-detail/index?id='+encodeURIComponent(String(detail.id))})">{{$t('v15.intentDetail')}}</button><button v-if="detail.my_role==='buyer'&&['purchased','shipped','delivered','completed'].includes(detail.status)" class="account-migration-link" @click="uni.navigateTo({url:'/pages/dispute-create/index?id='+encodeURIComponent(String(detail.id))})">{{$t('v15.disputeCreate')}}</button>
 </view>
 </view></template>
<style scoped>.account-order{padding:26rpx;min-height:100vh;background:#f7f9fc;box-sizing:border-box}.account-back{color:var(--kx-accent,#2476ce);padding:18rpx 0;display:block}.account-title{font-weight:700;font-size:36rpx;margin:24rpx 0}.account-card{background:#fff;border:1rpx solid #e2e8f0;border-radius:24rpx;padding:28rpx 25rpx}.account-name{font-size:32rpx;font-weight:700;margin-bottom:23rpx;overflow-wrap:anywhere}.account-line{display:flex;justify-content:space-between;gap:22rpx;padding:18rpx 0;border-bottom:1rpx solid #edf2f6;font-size:24rpx}.account-line text:last-child{max-width:62%;text-align:right;overflow-wrap:anywhere}.account-note,.account-hint{padding:26rpx 0;color:#64748b;font-size:23rpx;line-height:1.6}.kx-dark .account-card{background:#202629;border-color:#44515d}.account-migration-link{background:var(--kx-accent,#2476ce);color:#fff;border-radius:15rpx;font-size:24rpx;margin-top:20rpx}</style>
