<script setup lang="ts">
import {useRoutePage} from '../../stores/useRoutePage';
const {prefs,trips,error,loading,reload,draft,confirmed,listening,voiceError,recognizer,supported,startVoice,stopVoice,mobilePreferenceStyle} = useRoutePage();
</script>
<template>
 <view class="route-content" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-system':prefs.theme==='system','kx-reduce-motion':prefs.reduceMotion}" :style="mobilePreferenceStyle(prefs)">
  <view class="route-header"><text class="kicker">{{ $t('text.a1a20f6b6e7a') }}</text><text class="heading">{{ $t('text.56674a81f84d') }}</text><text class="sub">{{ $t('text.26b8100351ed') }}</text></view>
  <view class="notice"><text class="notice-title">{{ $t('text.f4f7c1191fdb') }}</text><text class="notice-body">{{ $t('text.fc0df28d74f2') }}</text></view>
  <view class="white-card mobile-voice-draft"><view class="mini-title">{{ $t('voice.title') }}</view>
    <view class="subline">{{ $t(supported?'voice.permission':'voice.unavailable') }}</view>
    <view class="button-row"><button class="secondary-btn" :disabled="!supported||listening" @click="startVoice">{{ $t('voice.start') }}</button><button class="secondary-btn" :disabled="!listening" @click="stopVoice">{{ $t('voice.stop') }}</button></view>
    <view class="form-label">{{ $t('voice.draft') }}</view><textarea class="form-input form-textarea" v-model="draft" :placeholder="$t('voice.placeholder')" @input="confirmed=false"/>
    <view class="subline" v-if="listening">{{ $t('voice.listening') }}</view><view class="subline" v-if="voiceError">{{ $t(voiceError) }}</view>
    <view class="care-row"><text>{{ $t('voice.confirm') }}</text><switch :checked="confirmed" :disabled="!draft.trim()" @change="confirmed=!!$event.detail.value"/></view>
    <view class="subline">{{ $t(confirmed?'voice.ready':'voice.noSubmit') }}</view>
  </view>
  <view class="list-head"><text>{{ $t('text.74169c440420') }}</text><button size="mini" :disabled="loading" @click="reload">{{ $t('text.38108eaa1d32') }}</button></view>
  <view v-if="error" class="hint">{{error}}</view>
  <view v-else-if="loading" class="hint">{{ $t('text.300ee3dee4dc') }}</view>
  <view v-else-if="!trips.length" class="hint">{{ $t('text.d35ee994274e') }}</view>
  <view v-else class="trip" v-for="t in trips" :key="String(t.id)"><text class="destination">{{t.destination || $t('text.a83c1a23ae9c')}}</text><text class="meta">{{t.depart_date || $t('text.8333c7bb5cda')}} · {{t.transport_mode || $t('text.aa01b1033f7f')}}</text></view>
 </view>
</template>
<style scoped>
.route-content{padding:25rpx 24rpx 60rpx;background:#f6f8f5;min-height:100vh}.route-header{background:#255d47;color:#fff;border-radius:27rpx;padding:39rpx 34rpx;display:flex;flex-direction:column;gap:17rpx}.kicker{color:#a6d1b9;font-size:22rpx;letter-spacing:3rpx}.heading{font-size:43rpx;font-weight:800}.sub{color:#d4e8d8;font-size:24rpx;line-height:1.7}.notice{margin-top:25rpx;padding:30rpx;background:#fef9ea;border:1rpx solid #f0dfba;border-radius:22rpx;display:flex;flex-direction:column;gap:13rpx}.notice-title{color:#9b722c;font-size:26rpx;font-weight:700}.notice-body{color:#816b45;font-size:23rpx;line-height:1.8}.list-head{margin:30rpx 5rpx 20rpx;display:flex;justify-content:space-between;align-items:center;font-size:27rpx;font-weight:700}.list-head button{font-size:22rpx}.hint{padding:45rpx 18rpx;text-align:center;color:#8b9993;font-size:24rpx}.trip{margin-bottom:17rpx;padding:29rpx;border-radius:18rpx;background:white;display:flex;flex-direction:column;gap:15rpx}.destination{font-size:28rpx;font-weight:700;color:#315a47}.meta{font-size:22rpx;color:#889b90}
</style>

<style scoped>
.mobile-voice-draft{margin:22rpx}.mobile-voice-draft .button-row{display:flex;gap:12rpx;margin-top:24rpx;flex-wrap:wrap}.mobile-voice-draft .button-row button{flex:1;min-width:180rpx}.mobile-voice-draft .care-row{display:flex;align-items:center;justify-content:space-between;gap:16rpx;margin:22rpx 0}.mobile-voice-draft textarea{width:100%;height:170rpx;}
</style>
