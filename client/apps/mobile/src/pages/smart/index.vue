<script setup lang="ts">
import {useSmartPage} from '../../stores/useSmartPage';
const {prefs,products,trips,failure,busy,a,b,left,right,names,pickA,pickB,load,name,price,mobilePreferenceStyle} = useSmartPage();
</script>
<template>
<view class="app-page smart-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-system':prefs.theme==='system','kx-reduce-motion':prefs.reduceMotion}" :style="mobilePreferenceStyle(prefs)">
  <view class="page-heading">{{ $t('ai.title') }}</view>
  <view class="white-card"><view class="mini-title">{{ $t('ai.buyer') }}</view><view class="subline">{{ $t('ai.rulesOnly') }}</view><view class="smart-count">{{ $t('ai.browseProducts') }}：{{ products.length }}</view><view class="smart-count">{{ $t('ai.browseTrips') }}：{{ trips.length }}</view></view>
  <view class="white-card"><view class="mini-title">{{ $t('ai.carrier') }}</view><view class="subline">{{ $t('ai.noDemands') }}</view><view class="subline">{{ $t('ai.needRoute') }}</view></view>
  <view class="white-card"><view class="mini-title">{{ $t('ai.compare') }}</view><view class="subline">{{ $t('ai.compareHint') }}</view>
    <view v-if="products.length>=2">
      <view class="form-label">{{ $t('ai.selectA') }}</view>
      <picker :range="names" :value="a" @change="pickA"><view class="form-input">{{ name(left) }}</view></picker>
      <view class="form-label">{{ $t('ai.selectB') }}</view>
      <picker :range="names" :value="b" @change="pickB"><view class="form-input">{{ name(right) }}</view></picker>
      <view class="compare-line"><text>{{ $t('ai.price') }}</text><text>{{ price(left) }}</text><text>{{ price(right) }}</text></view>
      <view class="compare-line"><text>{{ $t('ai.region') }}</text><text>{{ left?.region||$t('common.notProvided') }}</text><text>{{ right?.region||$t('common.notProvided') }}</text></view>
      <view class="compare-line"><text>{{ $t('ai.spec') }}</text><text>{{ left?.spec||$t('common.notProvided') }}</text><text>{{ right?.spec||$t('common.notProvided') }}</text></view>
      <view class="compare-line"><text>{{ $t('ai.stock') }}</text><text>{{ left?.stock??$t('common.notProvided') }}</text><text>{{ right?.stock??$t('common.notProvided') }}</text></view>
    </view>
    <view v-else class="subline">{{ $t('ai.empty') }}</view>
    <view class="subline">{{ $t('ai.noAI') }}</view>
    <view v-if="failure" class="error-box">{{failure}}</view>
    <button class="secondary-btn" :disabled="busy" @click="load">{{ $t('text.5982c44c18df') }}</button>
  </view>
</view>
</template>
<style scoped>
.smart-page{padding-top:55rpx}.smart-count{font-size:25rpx;margin-top:18rpx;color:var(--kx-accent,#0ea5e9)}
.compare-line{display:grid;grid-template-columns:1fr 1fr 1fr;gap:12rpx;padding:20rpx 0;border-bottom:1rpx solid #e5eaed;font-size:23rpx;overflow-wrap:anywhere}
.smart-page .white-card{margin-top:20rpx}.smart-page .secondary-btn{margin-top:20rpx}
</style>
