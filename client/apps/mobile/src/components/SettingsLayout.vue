<script setup lang="ts">
import { preferenceRef } from '@kuaixie/preferences/vue';
import { mobilePreferenceStyle } from '../utils/prototype';
import { t } from '@kuaixie/i18n';
import { localeRef } from '@kuaixie/i18n/vue';
import { watch } from 'vue';
const props=defineProps<{titleKey:string;subtitleKey?:string}>();
function updateTitle(){try{uni.setNavigationBarTitle({title:t(props.titleKey)})}catch{/* H5 custom nav */}}
updateTitle();watch(localeRef,updateTitle);
const prefs=preferenceRef;
</script>
<template>
 <view class="settings-screen" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
  <view class="settings-screen-header">
   <text class="settings-back" @click="uni.navigateBack()">‹ {{ $t('m2.mobile.back') }}</text>
   <view class="settings-heading">{{ $t(titleKey) }}</view>
   <view v-if="subtitleKey" class="settings-subtitle">{{ $t(subtitleKey) }}</view>
  </view>
  <slot />
 </view>
</template>
<style src="../styles/settings.css"></style>
