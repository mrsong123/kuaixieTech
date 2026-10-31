<script setup lang="ts">
import {useOriginalOrderList} from '../../stores/useOriginalOrderLists';
const s=useOriginalOrderList('open');
function verify(){uni.navigateTo({url:'/pages/carrier-verify/index'})}
</script>
<template>
 <scroll-view scroll-y class="original-subpage" :class="{'kx-dark':s.prefs.theme==='dark','kx-care':s.prefs.care}" :style="s.mobilePreferenceStyle(s.prefs)">
  <text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text>
  <view class="op-title">{{$t('v14.openOrderTitle')}}</view>
  <view class="op-banner">{{$t('v14.openOrderScope')}}</view>
  <view class="op-card"><text class="op-card-title">{{$t('v14.verification')}}</text><text class="op-muted">{{s.verified===true?$t('v14.kycVerified'):s.verified===false?$t('v14.kycNotVerified'):$t('v14.verifyDesc')}}</text><text v-if="s.verificationError" class="op-muted">{{s.verificationError}}</text><button v-if="s.verified!==true" class="op-action op-secondary" @click="verify">{{$t('v14.goVerify')}}</button></view>
  <view v-if="s.busy&&!s.loaded" class="op-empty">{{$t('account.loading')}}</view>
  <view v-else-if="s.error" class="op-error">{{s.error}}<button class="op-action op-secondary" :disabled="s.busy" @click="s.load(true)">{{$t('common.retry')}}</button></view>
  <view v-else-if="s.loaded&&!s.rows.length" class="op-empty">{{$t('account.empty')}}</view>
  <view v-for="o in s.rows" :key="String(o.id)" class="op-card" @click="s.detail(o)">
    <view class="v14-between"><text class="op-card-title">{{o.item_name}}</text><text class="v14-status">{{s.formatBusinessStatus(o.status)}}</text></view>
    <text class="op-muted">{{$t('account.orderNumber')}}：{{o.order_no||o.id}}</text>
    <view class="op-row"><text>{{$t('account.goodsAmount')}}</text><text>{{s.formatAmount(o.goods_amount,'CNY')}}</text></view>
    <view class="op-row"><text>{{$t('account.created')}}</text><text>{{s.formatDate(o.created_at)}}</text></view>
  </view>
  <button v-if="s.hasMore&&!s.busy" class="op-action op-secondary" @click="s.load(false)">{{$t('account.more')}}</button>
  <view v-if="s.busy&&s.loaded" class="op-empty">{{$t('account.loading')}}</view>
 </scroll-view>
</template>