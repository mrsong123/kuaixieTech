<script setup lang="ts">defineProps<{title:string}>()</script>
<template><view class="mine-group"><view class="mine-group-heading">{{title}}</view><view class="mine-group-card"><slot/></view></view></template>
<style scoped>
.mine-group{margin-top:32rpx}.mine-group-heading{font-size:24rpx;font-weight:400;color:var(--kx-fg-secondary,#64748b);margin:0 8rpx 16rpx;line-height:1.45}.mine-group-card{background:var(--kx-surface,#fff);border:1rpx solid var(--kx-border,#e2e8f0);border-radius:24rpx;overflow:hidden}
</style>
