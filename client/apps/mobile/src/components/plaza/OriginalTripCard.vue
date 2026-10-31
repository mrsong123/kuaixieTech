<script setup lang="ts">
import OriginalMdiIcon from '../OriginalMdiIcon.vue';
import type {PublicTrip} from '@kuaixie/sdk';
import {formatAmount,formatDate,transportLabel} from '@kuaixie/i18n';
const props=defineProps<{trip:PublicTrip}>();
const emit=defineEmits<{open:[trip:PublicTrip]}>();
const hasFee=()=>props.trip.fee_value!==undefined&&props.trip.fee_value!==null;
</script>
<template>
 <view class="original-trip-card" @click="emit('open',trip)">
   <view class="route-top">
     <view class="route-main">
       <view class="travel-symbol"><view class="travel-icon"><OriginalMdiIcon name="airplane"/></view></view>
       <view class="route-copy">
         <text class="route-name">{{trip.destination||$t('common.notProvided')}}</text>
         <text class="route-date">{{formatDate(trip.depart_date)}}<template v-if="trip.return_date"> · {{formatDate(trip.return_date)}}</template></text>
       </view>
     </view>
     <view class="route-tags">
       <text v-if="trip.trip_type" class="route-tag" :class="{domestic:trip.trip_type==='domestic'}">{{trip.trip_type==='domestic'?$t('portal.square.domestic'):$t('portal.square.international')}}</text>
       <text v-if="trip.direction" class="route-tag direction">{{trip.direction==='inbound'?$t('home.dirIn'):trip.direction==='outbound'?$t('home.dirOut'):trip.direction}}</text>
     </view>
   </view>
   <!-- Original TripCard has a separate owner trust row. The verified backend's public trip DTO
        does not return owner rating, so no fabricated rating/avatar/deal count is rendered. -->
   <view class="route-secondary" v-if="trip.entry_port||trip.transport_mode">
     <text>{{trip.entry_port||$t('common.notProvided')}}</text>
     <text v-if="trip.transport_mode">{{transportLabel(trip.transport_mode)}}</text>
   </view>
   <view class="route-footer" v-if="hasFee()||trip.capacity_qty!=null||trip.remaining_slots!=null">
     <text v-if="hasFee()" class="fee-text">{{trip.fee_mode==='percent'?String(trip.fee_value)+'%':formatAmount(trip.fee_value)}}</text>
     <text v-if="trip.remaining_slots!=null||trip.capacity_qty!=null" class="capacity-text">{{trip.remaining_slots??$t('common.notProvided')}} / {{trip.capacity_qty??$t('common.notProvided')}}</text>
   </view>
 </view>
</template>
<style scoped>
/* Original components/trip-card.tsx: rounded-xl + border + p-4 + mb-3; no invented trust scores. */
.original-trip-card{background:var(--card,#fff);border:1rpx solid var(--border,#e2e8f0);border-radius:20rpx;padding:32rpx;margin-bottom:24rpx;color:var(--foreground,#0f172a)}
.route-top{display:flex;justify-content:space-between;align-items:flex-start;gap:14rpx}.route-main{display:flex;align-items:center;gap:16rpx;min-width:0;flex:1}.travel-symbol{width:64rpx;height:64rpx;flex:0 0 64rpx;border-radius:14rpx;background:var(--primary-10,#e0f2fe);color:var(--primary,#0ea5e9);display:flex;align-items:center;justify-content:center;font-size:33rpx}.travel-icon{width:39rpx;height:39rpx}.route-copy{flex:1;min-width:0}.route-name{display:block;font-size:30rpx;font-weight:700;line-height:1.45;overflow-wrap:anywhere}.route-date{display:block;font-size:22rpx;color:var(--muted-foreground,#64748b);margin-top:8rpx;line-height:1.5;overflow-wrap:anywhere}
.route-tags{display:flex;flex-direction:column;align-items:flex-end;gap:9rpx;flex:0 0 auto}.route-tag{padding:8rpx 13rpx;font-size:20rpx;border-radius:8rpx;background:var(--primary-15,#e0f2fe);color:var(--primary,#0ea5e9)}.route-tag.domestic{background:#10b98124;color:#047857}.route-tag.direction{background:#77777714;color:var(--muted-foreground,#64748b)}
.route-secondary{border-top:1rpx solid var(--border,#e2e8f0);margin-top:24rpx;padding-top:20rpx;display:flex;justify-content:space-between;gap:12rpx;color:var(--muted-foreground,#64748b);font-size:22rpx;overflow-wrap:anywhere}.route-footer{display:flex;align-items:center;justify-content:space-between;gap:15rpx;margin-top:24rpx;color:var(--muted-foreground,#64748b);font-size:22rpx;overflow-wrap:anywhere}.fee-text{color:var(--primary,#0ea5e9);font-weight:600}.capacity-text{text-align:right}
.kx-dark .original-trip-card{background:#202629;border-color:#475569;color:#f2f4f4}
@media(max-width:355px){.original-trip-card{padding:24rpx}.route-tags{gap:5rpx}.route-tag{padding:6rpx 9rpx;font-size:18rpx}}
</style>
