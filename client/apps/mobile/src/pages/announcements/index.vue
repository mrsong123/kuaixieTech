<script setup lang="ts">
import {useAnnouncements} from '../../stores/useOriginalDocuments';
const s=useAnnouncements();
</script>
<template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':s.prefs.theme==='dark','kx-care':s.prefs.care}" :style="s.mobilePreferenceStyle(s.prefs)">
 <text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{$t('v14.announcements')}}</view>
 <view v-if="s.loading" class="op-empty">{{$t('account.loading')}}</view>
 <view v-else-if="s.error" class="op-error">{{$t('v14.missingPublicEndpoint')}} · {{s.error}}<button class="op-action op-secondary" @click="s.load()">{{$t('common.retry')}}</button></view>
 <view v-else-if="!s.rows.length" class="op-empty">{{$t('v14.noAnnouncements')}}</view>
 <view v-for="a in s.rows" :key="a.id" class="op-card" @click="s.expanded=s.expanded===a.id?'':a.id">
   <view class="v14-between"><view class="v14-kind">{{a.kind==='notice'?$t('v14.notice'):a.kind==='activity'?$t('v14.activity'):a.kind==='rule'?$t('v14.rule'):a.kind}}</view><text v-if="a.pinned" class="v14-pinned">{{$t('v14.pinned')}}</text></view>
   <view class="op-card-title">{{s.localized(a.title,a.title_i18n)}}</view><view class="op-muted">{{s.formatDate(a.created_at)}}</view>
   <text class="v14-body">{{s.expanded===a.id?s.localized(a.body,a.body_i18n):s.localized(a.body,a.body_i18n).slice(0,60)+(s.localized(a.body,a.body_i18n).length>60?'…':'')}}</text>
   <view v-if="s.expanded!==a.id&&s.localized(a.body,a.body_i18n).length>60" class="v14-expand">{{$t('v14.expand')}}</view>
 </view>
 </scroll-view></template>