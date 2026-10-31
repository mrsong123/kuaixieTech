<script setup lang="ts">
import {useSupportCenterPage} from '../../stores/useSupportCenterPage';
const {prefs,busy,error,open,mobilePreferenceStyle} = useSupportCenterPage();
</script>
<template><view class="original-subpage" :class="{'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
<text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{$t('migration.supportTitle')}}</view>
<view class="op-card"><view class="op-card-title">{{$t('migration.supportSubtitle')}}</view><text class="op-desc">{{$t('migration.supportDesc')}}</text><button class="op-action" :disabled="busy" @click="open">{{$t('migration.startConversation')}}</button></view>
<view v-if="error" class="op-error">{{error}}</view><view class="op-banner">{{$t('migration.supportLimit')}}</view>
</view></template>
