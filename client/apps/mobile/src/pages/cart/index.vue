<script setup lang="ts">
import {useUnavailableOriginalPage} from '../../stores/useUnavailableOriginalPage';
const {prefs,mobilePreferenceStyle}=useUnavailableOriginalPage();
</script>
<template><view class="original-subpage" :class="{'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text>
<view class="op-title">{{$t('migration.cart')}}</view><view class="op-card"><view class="op-empty">{{$t('migration.noBackend')}}</view></view></view></template>
