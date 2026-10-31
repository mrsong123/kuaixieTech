<script setup lang="ts">
import OriginalMdiIcon from '../OriginalMdiIcon.vue';
import type {PublicDemand} from '@kuaixie/sdk';
import {formatAmount,formatDate} from '@kuaixie/i18n';
const props=defineProps<{demand:PublicDemand}>();
const emit=defineEmits<{open:[demand:PublicDemand]}>();
</script>
<template>
 <view class="original-demand-card" @click="emit('open',demand)">
   <view class="demand-top"><view class="demand-left"><view class="demand-icon"><view class="demand-icon-img"><OriginalMdiIcon name="clipboard-text-outline"/></view></view><text class="demand-title">{{demand.title}}</text></view><text v-if="demand.trip_type" class="demand-region">{{demand.trip_type==='domestic'?$t('portal.square.domestic'):demand.trip_type==='international'?$t('portal.square.international'):$t('portal.square.allRegions')}}</text></view>
   <text v-if="demand.description" class="demand-description">{{demand.description}}</text>
   <view class="demand-bottom"><text v-if="demand.expect_price!=null" class="demand-price">{{ $t('portal.square.price') }} {{formatAmount(demand.expect_price)}}</text><text v-if="demand.city" class="demand-muted">{{demand.city}}</text><text v-if="demand.deadline" class="demand-muted">{{formatDate(demand.deadline)}}</text></view>
 </view>
</template>
<style scoped>
/* Original index.tsx carrier request cards: rounded-xl border p-3, budget/deadline footer. */
.original-demand-card{background:var(--card,#fff);border:1rpx solid var(--border,#e2e8f0);border-radius:19rpx;padding:26rpx;margin-bottom:20rpx;color:var(--foreground,#0f172a)}
.demand-top{display:flex;align-items:flex-start;justify-content:space-between;gap:15rpx}.demand-left{display:flex;align-items:center;gap:13rpx;min-width:0;flex:1}.demand-icon{background:var(--primary-10,#e0f2fe);color:var(--primary,#0ea5e9);width:54rpx;height:54rpx;border-radius:12rpx;display:flex;align-items:center;justify-content:center;font-size:30rpx;flex:0 0 54rpx}.demand-icon-img{width:35rpx;height:35rpx}.demand-title{font-size:28rpx;font-weight:700;overflow-wrap:anywhere;min-width:0;line-height:1.45}.demand-region{color:var(--primary,#0ea5e9);background:var(--primary-10,#e0f2fe);border-radius:9rpx;padding:7rpx 12rpx;font-size:21rpx}.demand-description{display:block;color:var(--muted-foreground,#64748b);font-size:24rpx;line-height:1.6;margin-top:17rpx;white-space:pre-wrap;overflow-wrap:anywhere}.demand-bottom{border-top:1rpx solid var(--border,#e2e8f0);display:flex;flex-wrap:wrap;gap:15rpx;align-items:center;justify-content:space-between;margin-top:22rpx;padding-top:19rpx;font-size:21rpx}.demand-price{color:var(--primary,#0ea5e9);font-weight:700}.demand-muted{color:var(--muted-foreground,#64748b)}.kx-dark .original-demand-card{background:#202629;border-color:#475569;color:#f2f4f4}
</style>
