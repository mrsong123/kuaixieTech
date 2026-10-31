<script setup lang="ts">
import {useOriginalCommunityDetail} from '../../stores/useOriginalCommunityDetail';
const s=useOriginalCommunityDetail();
</script>
<template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':s.prefs.theme==='dark','kx-care':s.prefs.care}" :style="s.mobilePreferenceStyle(s.prefs)">
<text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{$t('v14.communityDetail')}}</view>
<view v-if="s.loading" class="op-empty">{{$t('account.loading')}}</view>
<view v-else-if="s.error" class="op-error">{{s.error}}<button class="op-action op-secondary" @click="s.load()">{{$t('common.retry')}}</button></view>
<view v-else-if="!s.post" class="op-empty">{{$t('v14.postNotFound')}}</view>
<view v-else class="op-card"><view class="op-card-title">{{s.post.author_name||$t('v14.unknownAuthor')}}</view>
<text class="op-muted">{{s.formatDate(s.post.created_at)}}</text>
<text class="v14-body">{{s.post.caption}}</text>
<image v-if="s.post.media_kind==='image'&&s.safeUrl(s.post.media_url)" :src="s.safeUrl(s.post.media_url)" class="v14-community-image" mode="widthFix"/>
<video v-else-if="s.post.media_kind==='video'&&s.safeUrl(s.post.media_url)" :src="s.safeUrl(s.post.media_url)" :poster="s.safeUrl(s.post.poster_url)" :autoplay="false" :controls="true" class="v14-community-video"/>
<image v-else-if="s.safeUrl(s.post.poster_url)" :src="s.safeUrl(s.post.poster_url)" class="v14-community-image" mode="widthFix"/>
<view class="op-banner">{{$t('v14.communityReadOnly')}}</view></view>
</scroll-view></template>