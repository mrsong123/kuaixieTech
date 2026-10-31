<script setup lang="ts">
import SettingsLayout from '../../../components/SettingsLayout.vue';
import {useAccountProfile} from '../../../stores/useAccountProfile';
const {profile,nickname,city,loading,saving,error,load,save}=useAccountProfile();
</script>
<template><SettingsLayout titleKey="settings2.profile" subtitleKey="account.profileDesc">
 <view v-if="loading" class="account-message">{{$t('account.loading')}}</view>
 <view v-else-if="error" class="account-message">{{$t(error)}}<button class="profile-btn" @click="load">{{$t('account.retry')}}</button></view>
 <view v-else-if="profile" class="profile-form">
  <view class="form-label">{{$t('account.nickname')}}</view><input v-model="nickname" class="form-input" maxlength="64"/>
  <view class="form-label">{{$t('account.city')}}</view><input v-model="city" class="form-input" maxlength="64"/>
  <view class="form-label">{{$t('account.verificationStatus')}}</view><text class="form-description">{{profile.buyer_verify_status||$t('account.notKnown')}}</text>
  <button class="profile-btn" :disabled="saving" @click="save">{{$t('account.save')}}</button>
 </view>
 </SettingsLayout></template>
<style scoped>.account-message{padding:24rpx;text-align:center;color:#64748b}.profile-form{padding:18rpx 0}.form-label{margin:22rpx 0 13rpx;color:#475569;font-size:25rpx}.form-input{min-height:74rpx;border:1rpx solid #d9e1ea;background:#fff;border-radius:13rpx;padding:0 20rpx;font-size:28rpx}.form-description{color:#64748b;font-size:24rpx}.profile-btn{margin-top:28rpx;color:#fff;background:var(--kx-accent,#2476ce);border-radius:15rpx;font-size:25rpx}</style>
