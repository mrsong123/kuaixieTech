<script setup lang="ts">
import {useDemandPoolPage} from '../../stores/useDemandPoolPage';
const {prefs,style,posts,loading,error,category,shown,safeUrl,reload,publish,openDetail,formatDate} = useDemandPoolPage();
</script>
<template>
 <view class="community-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="style">
  <view class="community-header"><view><view class="community-heading">{{ $t('portal.community.title') }}</view><view class="community-subtitle">{{ $t('portal.community.subtitle') }}</view></view><text class="header-refresh" @click="reload">↻</text></view>
  <view class="feed-filters"><view class="filter" :class="{active:category==='all'}" @click="category='all'">{{ $t('portal.community.discover') }}</view><view class="filter" :class="{active:category==='video'}" @click="category='video'">{{ $t('portal.community.video') }}</view><view class="filter" :class="{active:category==='image'}" @click="category='image'">{{ $t('portal.community.image') }}</view></view>
  <scroll-view scroll-y class="community-scroll">
   <view v-if="loading" class="community-empty">{{ $t('portal.community.loading') }}</view>
   <view v-else-if="error" class="community-empty"><text>{{ $t('portal.community.notReady') }}</text><button class="retry" @click="reload">{{ $t('portal.community.refresh') }}</button></view>
   <view v-else-if="!shown.length" class="community-empty"><text>{{ $t('portal.community.empty') }}</text><view class="empty-note">{{ $t('portal.community.postReview') }}</view></view>
   <view v-for="post in shown" :key="post.id" class="post-card">
    <view class="author-line"><view class="avatar"><image v-if="safeUrl(post.author_avatar)" :src="safeUrl(post.author_avatar)" mode="aspectFill"/><text v-else>◯</text></view><view class="author-copy"><text class="author-name">{{post.author_name||$t('common.notProvided')}}</text><text class="date">{{formatDate(post.created_at)}}</text></view></view>
    <view class="post-caption" @click="openDetail(post)">{{post.caption||$t('portal.community.noCaption')}}</view>
    <view v-if="post.media_kind==='video'&&safeUrl(post.media_url)" class="video-frame"><video :src="safeUrl(post.media_url)" :poster="safeUrl(post.poster_url)" :controls="true" :autoplay="false" :show-center-play-btn="true" :show-fullscreen-btn="true" object-fit="contain"/></view>
    <view v-else-if="post.media_kind==='image'&&safeUrl(post.media_url)" class="image-frame"><image :src="safeUrl(post.media_url)" mode="widthFix"/></view>
    <view v-else-if="post.media_kind!=='text'" class="media-unavailable">{{ $t('portal.community.noMedia') }}</view>
   </view>
  </scroll-view>
  <view class="community-bottom"><text>{{ $t('portal.community.contentWarning') }}</text><view class="floating-create" @click="publish">＋</view></view>
 </view>
</template>
<style scoped>
.community-page{background:#f8fafc;min-height:100vh;max-width:1100rpx;margin:0 auto;padding:31rpx 26rpx calc(140rpx + env(safe-area-inset-bottom));color:#0f172a}
.community-header{display:flex;justify-content:space-between;align-items:center;gap:16rpx;padding:13rpx 2rpx 22rpx}.community-heading{font-size:41rpx;font-weight:760}.community-subtitle{font-size:23rpx;color:#64748b;line-height:1.6;margin-top:8rpx}.header-refresh{color:var(--prototype-accent);font-size:38rpx;padding:10rpx}.feed-filters{display:flex;gap:24rpx;border-bottom:1rpx solid #e2e8f0;margin-bottom:25rpx}.filter{padding:16rpx 8rpx;font-size:27rpx;color:#64748b;border-bottom:5rpx solid transparent;flex-shrink:0}.filter.active{font-weight:730;color:var(--prototype-accent);border-color:var(--prototype-accent)}.community-scroll{height:auto}
.post-card{background:white;border:1rpx solid #e2e8f0;border-radius:21rpx;overflow:hidden;margin-bottom:25rpx;padding:23rpx}.author-line{display:flex;gap:14rpx;align-items:center}.avatar{width:70rpx;height:70rpx;border-radius:50%;background:#e0f2fe;color:var(--prototype-accent);display:flex;align-items:center;justify-content:center;overflow:hidden;flex:0 0 70rpx}.avatar image{width:100%;height:100%}.author-copy{display:flex;flex-direction:column;gap:4rpx;min-width:0}.author-name{font-size:26rpx;font-weight:740}.date{font-size:20rpx;color:#94a3b8}.post-caption{font-size:27rpx;line-height:1.72;margin:21rpx 2rpx;white-space:pre-wrap;word-break:break-word}.video-frame{width:100%;background:#0f172a;border-radius:16rpx;overflow:hidden;display:flex;align-items:center;justify-content:center;max-height:900rpx}.video-frame video{width:100%;height:740rpx}.image-frame image{width:100%;border-radius:16rpx}.media-unavailable{padding:30rpx;color:#64748b;text-align:center;font-size:24rpx}.community-empty{background:white;border-radius:19rpx;border:1rpx solid #e2e8f0;padding:75rpx 25rpx;color:#64748b;text-align:center;font-size:25rpx;line-height:1.75}.empty-note{font-size:22rpx;margin-top:13rpx}.retry{margin-top:18rpx;color:var(--prototype-accent);background:#f0f9ff;font-size:25rpx}
.community-bottom{position:fixed;bottom:calc(115rpx + env(safe-area-inset-bottom));right:24rpx;left:24rpx;max-width:1020rpx;margin:auto;display:flex;align-items:center;justify-content:space-between;gap:16rpx;pointer-events:none}.community-bottom text{font-size:18rpx;color:#64748b;max-width:70%;line-height:1.45}.floating-create{pointer-events:auto;display:flex;align-items:center;justify-content:center;width:86rpx;height:86rpx;border-radius:50%;background:var(--prototype-accent);color:white;font-size:49rpx;box-shadow:0 12rpx 30rpx #0ea5e933;flex:0 0 86rpx}
.kx-dark{background:#171c1f;color:#f2f4f4}.kx-dark .post-card,.kx-dark .community-empty{background:#202629;border-color:#475569}.kx-dark .community-subtitle,.kx-dark .date{color:#cbd5e1}.kx-care .post-caption{font-size:30rpx}@media(min-width:768px){.community-page{max-width:920rpx;padding-left:40rpx;padding-right:40rpx}}
</style>
