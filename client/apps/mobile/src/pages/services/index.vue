<script setup lang="ts">
import {useServicesPage} from '../../stores/useServicesPage';
const {prefs,groups,open,mobilePreferenceStyle}=useServicesPage();
</script>
<template><view class="app-page service-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
 <text class="service-back" @click="uni.navigateBack()">‹ {{ $t('m2.mobile.back') }}</text>
 <view class="page-heading">{{ $t('fast.myServices') }}</view><view class="subline">{{ $t('fast.servicesDesc') }}</view>
 <view v-for="g in groups" :key="g.id" class="service-group"><view class="service-heading">{{ $t(g.title) }}</view><view class="service-grid"><view v-for="item in g.items" :key="item.id" class="service-tile" @click="open(item.id)"><view>{{ $t(item.titleKey) }}</view><text>›</text></view></view></view>
 </view></template>
<style scoped>.service-page{padding:28rpx 27rpx 110rpx;background:#f8fafc}.service-back{display:block;color:var(--kx-accent,#0ea5e9);padding:12rpx 0}.service-group{margin-top:33rpx}.service-heading{font-size:27rpx;color:#334155;font-weight:750;margin-bottom:15rpx}.service-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:13rpx}.service-tile{background:white;border:1rpx solid #e2e8f0;border-radius:17rpx;padding:21rpx 16rpx;display:flex;gap:12rpx;align-items:center;justify-content:space-between;font-size:24rpx;line-height:1.5;min-width:0;overflow-wrap:anywhere}.service-tile text{color:var(--kx-accent,#0ea5e9)}.kx-dark .service-heading{color:#f2f4f4}.kx-dark .service-tile{background:#202629;color:#f2f4f4;border-color:#475569}@media(max-width:355px){.service-grid{grid-template-columns:1fr}}</style>
