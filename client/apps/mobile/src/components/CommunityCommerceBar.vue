<script setup lang="ts">
import OriginalMdiIcon from './OriginalMdiIcon.vue';

const props = defineProps<{anchor?: Record<string, unknown> | null}>();
const deadStatus = new Set(['hidden','removed','deleted','offline','expired','invalid','taken_down','unavailable','closed']);
const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function text(value: unknown){return typeof value === 'string' ? value.trim() : ''}
function kind(){const type = text(props.anchor?.type); return type && type !== 'none' ? type : ''}
function dead(){return deadStatus.has(text(props.anchor?.status).toLowerCase())}
function linkedId(){const id = text(props.anchor?.id); return uuid.test(id) ? id : ''}
function chosen(type: string){return kind() === type}
function label(type: string, name: string){return chosen(type) && dead() ? 'community.expired' : name}
function open(url: string){uni.navigateTo({url})}
function press(type: 'product' | 'demand' | 'trip' | 'order'){
 if(chosen(type) && dead()) return;
 const id = chosen(type) ? linkedId() : '';
 if(type === 'product') open(id ? '/pages/my-products/index?id=' + encodeURIComponent(id) : '/pages/my-products/index');
 else if(type === 'demand') open(id ? '/pages/demand-detail/index?id=' + encodeURIComponent(id) : '/pages/demand-detail/index');
 else if(type === 'trip') open(id ? '/pages/trip-detail/index?id=' + encodeURIComponent(id) : '/pages/trips/index');
 else open('/pages/my-orders/index');
}
</script>
<template>
 <view class="commerce-bar" @click.stop>
  <text v-if="text(anchor?.title) && !dead()" class="commerce-title">{{ text(anchor?.title) }}</text>
  <view class="commerce-row">
   <view class="commerce-entry" :class="{chosen:chosen('product'),dead:chosen('product')&&dead()}" @click="press('product')"><view class="commerce-icon"><OriginalMdiIcon name="storefront-outline" tone="accent"/></view><text>{{ $t(label('product','community.bar.shop')) }}</text></view>
   <view class="commerce-entry" :class="{chosen:chosen('demand'),dead:chosen('demand')&&dead()}" @click="press('demand')"><view class="commerce-icon"><OriginalMdiIcon name="format-list-bulleted" tone="accent"/></view><text>{{ $t(label('demand','community.bar.demand')) }}</text></view>
   <view class="commerce-entry" :class="{chosen:chosen('trip'),dead:chosen('trip')&&dead()}" @click="press('trip')"><view class="commerce-icon"><OriginalMdiIcon name="map-marker-path" tone="accent"/></view><text>{{ $t(label('trip','community.bar.trip')) }}</text></view>
   <view class="commerce-entry" :class="{chosen:chosen('order'),dead:chosen('order')&&dead()}" @click="press('order')"><view class="commerce-icon"><OriginalMdiIcon name="cart-outline" tone="accent"/></view><text>{{ $t(label('order','community.bar.orders')) }}</text></view>
  </view>
 </view>
</template>
<style scoped>
.commerce-bar{margin-top:12rpx}
.commerce-title{display:block;margin-bottom:8rpx;font-size:22rpx;line-height:1.4;color:inherit;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.commerce-row{display:flex;gap:8rpx}
.commerce-entry{flex:1;min-width:0;min-height:72rpx;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4rpx;border:1rpx solid var(--border,#e2e8f0);border-radius:14rpx;background:var(--card,#fff);color:var(--foreground,#0f172a);font-size:20rpx;line-height:1.2}
.commerce-entry text{max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.commerce-icon{width:28rpx;height:28rpx}
.commerce-entry.chosen{border-color:var(--primary,#0ea5e9);background:var(--primary-08,rgba(14,165,233,.08));color:var(--primary,#0284c7);font-weight:700}
.commerce-entry.dead{border-color:var(--border,#e2e8f0);background:transparent;color:var(--muted-foreground,#64748b);font-weight:500}
</style>
