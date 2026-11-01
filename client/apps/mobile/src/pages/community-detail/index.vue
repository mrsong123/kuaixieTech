<script setup lang="ts">
import {computed} from 'vue';
import type {PublicCommunityPost} from '@kuaixie/sdk';
import {useOriginalCommunityDetail} from '../../stores/useOriginalCommunityDetail';
import CommunityCommerceBar from '../../components/CommunityCommerceBar.vue';
const s=useOriginalCommunityDetail();
const anchor=computed(()=>{const post=s.post as (PublicCommunityPost & {anchor?:Record<string,unknown>})|null;const raw=post?.anchor;return raw&&typeof raw==='object'?raw:null});
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
<video v-else-if="s.post.media_kind==='video'&&s.safeUrl(s.post.media_url)" :src="s.safeUrl(s.post.media_url)" :poster="s.safeUrl(s.post.poster_url)" :autoplay="false" :loop="false" :controls="true" class="v14-community-video"/>
<image v-else-if="s.safeUrl(s.post.poster_url)" :src="s.safeUrl(s.post.poster_url)" class="v14-community-image" mode="widthFix"/>
<view v-else-if="s.post.media_kind==='video'||s.post.media_kind==='image'" class="op-empty">{{$t('community.pendingUpload')}}</view>
<view class="detail-counts"><text>{{$t('portal.community.likes')}} —</text><text>{{$t('portal.community.follow')}} —</text><text>{{$t('portal.community.comments')}} —</text></view>
<CommunityCommerceBar :anchor="anchor"/>
<view class="op-banner">{{$t('v14.communityReadOnly')}}</view></view>
</scroll-view></template>
<style scoped>
.detail-counts{display:flex;gap:24rpx;margin:16rpx 0;font-size:24rpx;color:var(--muted-foreground,#64748b)}
</style>