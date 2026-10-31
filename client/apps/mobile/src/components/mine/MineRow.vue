<script setup lang="ts">
import OriginalMdiIcon from '../OriginalMdiIcon.vue';
defineProps<{icon?:string;label:string;value?:string;desc?:string;unavailable?:boolean}>();
defineEmits<{press:[]}>();
</script>
<template><view class="mine-row" @click="$emit('press')"><view class="mine-row-icon"><view class="mdi-image"><OriginalMdiIcon :name="icon||'information-outline'"/></view></view><view class="mine-row-main"><text class="mine-row-label">{{label}}</text><text v-if="desc" class="mine-row-desc">{{desc}}</text></view><text v-if="value" class="mine-row-value">{{value}}</text><text class="mine-row-arrow">›</text></view></template>
<style scoped>
.mine-row{display:flex;align-items:center;gap:0;padding:24rpx 32rpx;min-height:96rpx;border-bottom:1rpx solid var(--kx-border,#edf0f4)}.mine-row:last-child{border-bottom:0}.mine-row-icon{width:64rpx;height:64rpx;flex:0 0 64rpx;border-radius:12rpx;margin-right:24rpx;display:flex;align-items:center;justify-content:center;background:var(--kx-icon-bg,#e9f6fd);color:var(--kx-primary,#0ea5e9);font-size:40rpx} .mine-row-icon .mdi-image{width:44rpx;height:44rpx;display:block}.mine-row-main{display:flex;flex-direction:column;gap:5rpx;flex:1;min-width:0}.mine-row-label{font-size:28rpx;font-weight:400;line-height:1.35;color:var(--kx-fg,#0f172a);overflow-wrap:anywhere}.mine-row-desc{font-size:24rpx;color:var(--kx-fg-secondary,#64748b);line-height:1.4;overflow-wrap:anywhere}.mine-row-value{color:var(--kx-fg-secondary,#64748b);font-size:24rpx;margin-right:8rpx;max-width:40%;text-align:right;overflow-wrap:anywhere}.mine-row-arrow{font-size:32rpx;line-height:1;color:var(--kx-fg-secondary,#64748b);flex:0 0 32rpx;text-align:center}
</style>
