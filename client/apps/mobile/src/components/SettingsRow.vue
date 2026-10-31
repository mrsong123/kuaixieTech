<script setup lang="ts">
defineProps<{labelKey:string;descKey?:string;value?:string;arrow?:boolean;disabled?:boolean}>();
const emit=defineEmits<{(e:'press'):void}>();
</script>
<template>
 <view class="settings-row" :class="{'settings-disabled':disabled}" @click="!disabled&&emit('press')">
  <view class="settings-row-main"><view class="settings-row-label">{{ $t(labelKey) }}</view><view v-if="descKey" class="settings-row-desc">{{ $t(descKey) }}</view></view>
  <text v-if="value" class="settings-row-value">{{ value }}</text><text v-if="arrow!==false" class="settings-row-arrow">›</text>
 </view>
</template>
