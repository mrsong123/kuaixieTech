<script setup lang="ts">import {useOriginalReviews} from '../../stores/useOriginalReviews';
const s=useOriginalReviews();</script>
<template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':s.prefs.theme==='dark','kx-care':s.prefs.care}" :style="s.mobilePreferenceStyle(s.prefs)">
<text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{$t('v14.reviews')}}</view>
<view v-if="s.loading" class="op-empty">{{$t('account.loading')}}</view>
<view v-else-if="s.error" class="op-error">{{$t('v14.missingPublicEndpoint')}} · {{s.error}}<button class="op-action op-secondary" @click="s.load()">{{$t('common.retry')}}</button></view>
<view v-else-if="!s.rows.length" class="op-empty">{{$t('v14.noReviews')}}</view>
<view v-for="row in s.rows" :key="row.id" class="op-card">
<view class="v14-between"><text class="op-card-title">{{row.reviewer_name||$t('v14.unknownAuthor')}}</text><text class="v14-stars">{{'★'.repeat(Math.round(row.stars))}}<text class="op-muted">{{'☆'.repeat(5-Math.round(row.stars))}}</text></text></view>
<text class="op-muted">{{s.formatDate(row.created_at)}}</text><text v-if="row.content" class="v14-body">{{row.content}}</text>
<view v-if="row.reply" class="op-banner">{{$t('v14.reviewReply')}}：{{row.reply}}</view>
</view></scroll-view></template>