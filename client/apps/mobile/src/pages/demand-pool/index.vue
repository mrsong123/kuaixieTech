<script setup lang="ts">
import {onMounted,onUnmounted,ref,watch} from 'vue';
import type {PublicCommunityPost} from '@kuaixie/sdk';
import {useDemandPoolPage} from '../../stores/useDemandPoolPage';
import CommunityCommerceBar from '../../components/CommunityCommerceBar.vue';
import OriginalMdiIcon from '../../components/OriginalMdiIcon.vue';
const {prefs,style,loading,error,category,shown,safeUrl,reload,publish,openDetail,formatDate} = useDemandPoolPage();
const tabs=[
 {key:'all' as const,label:'portal.community.discover'},
 {key:'video' as const,label:'portal.community.video'},
 {key:'image' as const,label:'portal.community.image'}
];
const scenes=[
 {key:'browse' as const,label:'community.scene.browse'},
 {key:'buy' as const,label:'community.scene.buy'},
 {key:'carry' as const,label:'community.scene.carry'},
 {key:'shop' as const,label:'community.scene.shop'}
];
const scene=ref<'browse'|'buy'|'carry'|'shop'>('browse');
const current=ref(0);
function setCategory(key:'all'|'video'|'image'){category.value=key}
function anchorOf(post:PublicCommunityPost){
 const raw=(post as PublicCommunityPost & {anchor?:Record<string,unknown>}).anchor;
 return raw&&typeof raw==='object'?raw:null;
}
function onSlide(event:{detail?:{current?:number}}){current.value=Number(event.detail?.current||0)}
function lockDocumentScroll(locked:boolean){
 if(typeof document==='undefined')return;
 document.documentElement.style.overflow=locked?'hidden':'';
 document.body.style.overflow=locked?'hidden':'';
}
watch(category,()=>{current.value=0});
watch(()=>shown.value.length,(count)=>{if(current.value>=count)current.value=0});
onMounted(()=>lockDocumentScroll(true));
onUnmounted(()=>lockDocumentScroll(false));
</script>
<template>
 <view class="community-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="style">
  <view class="community-top">
   <view class="community-filters">
    <view v-for="tab in tabs" :key="tab.key" class="filter-pill" :class="{chosen:category===tab.key}" @click="setCategory(tab.key)"><text>{{ $t(tab.label) }}</text></view>
    <view class="publish-pill" @click="publish"><text>{{ $t('portal.publish.title') }}</text></view>
   </view>
   <view class="scene-row">
    <view v-for="item in scenes" :key="item.key" class="scene-word" :class="{chosen:scene===item.key}" @click="scene=item.key"><text>{{ $t(item.label) }}</text></view>
   </view>
  </view>
  <view class="community-stage">
   <view v-if="loading" class="stage-note"><text>{{ $t('portal.community.loading') }}</text></view>
   <view v-else-if="error" class="stage-note" @click="reload"><text>{{ $t('portal.community.notReady') }}</text><text class="stage-action">{{ $t('portal.community.refresh') }}</text></view>
   <view v-else-if="!shown.length" class="stage-note"><text>{{ $t('portal.community.empty') }}</text><text class="stage-sub">{{ $t('portal.community.postReview') }}</text></view>
   <swiper v-else class="feed-swiper" vertical :current="current" :circular="false" :indicator-dots="false" @change="onSlide">
    <swiper-item v-for="post in shown" :key="post.id">
     <view class="slide" :class="post.media_kind==='video'?'video-slide':'light-slide'" @click="openDetail(post)">
      <view class="media-frame">
       <video v-if="post.media_kind==='video'&&safeUrl(post.media_url)" class="media-fill" :src="safeUrl(post.media_url)" :poster="safeUrl(post.poster_url)||''" :autoplay="false" :loop="false" :muted="false" :controls="true" :show-center-play-btn="true" object-fit="contain" @click.stop/>
       <image v-else-if="post.media_kind==='image'&&safeUrl(post.media_url)" class="media-fill" :src="safeUrl(post.media_url)" mode="aspectFit"/>
       <view v-else-if="post.media_kind==='text'" class="text-caption"><text>{{ post.caption||$t('portal.community.noCaption') }}</text></view>
       <view v-else class="pending-upload"><text>{{ $t('community.pendingUpload') }}</text></view>
       <view class="side-rail" @click.stop>
        <view class="rail-avatar"><image v-if="safeUrl(post.author_avatar)" :src="safeUrl(post.author_avatar)" mode="aspectFill"/><text v-else>○</text></view>
        <view class="rail-action"><view class="rail-icon"><OriginalMdiIcon name="account-plus-outline" tone="accent"/></view><text>{{ $t('portal.community.follow') }}</text><text>—</text></view>
        <view class="rail-action"><view class="rail-icon"><OriginalMdiIcon name="heart-outline" tone="accent"/></view><text>{{ $t('portal.community.likes') }}</text><text>—</text></view>
        <view class="rail-action" @click="openDetail(post)"><view class="rail-icon"><OriginalMdiIcon name="chat-processing-outline" tone="accent"/></view><text>{{ $t('portal.community.comments') }}</text><text>—</text></view>
        <view class="rail-action"><text class="rail-mark">{{ $t('community.music') }}</text><text>—</text></view>
        <view class="rail-action"><text class="rail-mark">{{ $t('community.mention') }}</text><text>—</text></view>
       </view>
      </view>
      <view class="slide-copy" @click.stop>
       <text class="author-name">{{ post.author_name||$t('common.notProvided') }}</text>
       <text v-if="post.media_kind!=='text'" class="caption">{{ post.caption||$t('portal.community.noCaption') }}</text>
       <text class="posted-at">{{ formatDate(post.created_at) }}</text>
       <CommunityCommerceBar :anchor="anchorOf(post)"/>
      </view>
     </view>
    </swiper-item>
   </swiper>
  </view>
 </view>
</template>
<style scoped>
.community-page{position:fixed;top:var(--window-top,0px);right:0;bottom:var(--window-bottom,0px);left:0;display:flex;flex-direction:column;overflow:hidden;max-width:1100rpx;margin:0 auto;background:var(--background,#fff);color:var(--foreground,#0f172a);--primary:var(--prototype-accent,#0ea5e9)}
.community-top{flex:0 0 auto;padding:24rpx 32rpx 8rpx}
.community-filters{display:flex;align-items:center;gap:12rpx}
.filter-pill{padding:12rpx 22rpx;border:1rpx solid var(--border,#e2e8f0);border-radius:999px}
.filter-pill text{font-size:24rpx;color:var(--muted-foreground,#64748b)}
.filter-pill.chosen{background:var(--primary-15,rgba(14,165,233,.15));border-color:transparent}
.filter-pill.chosen text{color:var(--primary,#0284c7);font-weight:700}
.publish-pill{margin-left:auto;padding:12rpx 22rpx;border-radius:999px;background:var(--primary,#0ea5e9)}
.publish-pill text{font-size:24rpx;color:#fff;font-weight:700}
.scene-row{display:flex;justify-content:space-between;margin-top:18rpx}
.scene-word text{font-size:28rpx;color:var(--muted-foreground,#64748b)}
.scene-word.chosen text{color:var(--foreground,#0f172a);font-weight:700}
.community-stage{flex:1;min-height:0}
.stage-note{height:100%;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:48rpx;text-align:center}
.stage-note text{font-size:28rpx;line-height:1.5;color:var(--muted-foreground,#64748b)}
.stage-action{margin-top:12rpx;color:var(--primary,#0284c7)}
.stage-sub{margin-top:8rpx;font-size:24rpx}
.feed-swiper{height:100%}
.slide{height:100%;display:flex;flex-direction:column;box-sizing:border-box}
.video-slide{background:#0f172a;color:#f8fafc}
.light-slide{background:var(--background,#fff);color:var(--foreground,#0f172a)}
.media-frame{position:relative;flex:1;min-height:0;display:flex;align-items:center;justify-content:center}
.media-fill{width:100%;height:100%}
.text-caption{padding:48rpx 120rpx 48rpx 32rpx;font-size:34rpx;line-height:1.55;font-weight:650}
.pending-upload{width:100%;height:100%;display:flex;align-items:center;justify-content:center;background:#e2e8f0;color:#64748b;font-size:28rpx}
.video-slide .pending-upload{background:#1e293b;color:#cbd5e1}
.side-rail{position:absolute;right:16rpx;bottom:24rpx;display:flex;flex-direction:column;align-items:center;gap:18rpx;width:88rpx}
.rail-avatar{width:72rpx;height:72rpx;border-radius:50%;overflow:hidden;background:rgba(255,255,255,.16);display:flex;align-items:center;justify-content:center;color:#fff;font-size:28rpx}
.rail-avatar image{width:100%;height:100%}
.rail-action{display:flex;flex-direction:column;align-items:center;gap:2rpx;color:inherit;font-size:18rpx;line-height:1.2}
.rail-icon{width:40rpx;height:40rpx}
.rail-mark{font-size:20rpx;font-weight:700}
.slide-copy{flex:0 0 auto;padding:16rpx 24rpx 20rpx;background:var(--card,#fff);color:var(--foreground,#0f172a)}
.video-slide .slide-copy{background:rgba(15,23,42,.92);color:#f8fafc}
.author-name{display:block;font-size:26rpx;font-weight:700;line-height:1.4}
.caption{display:block;margin-top:6rpx;font-size:24rpx;line-height:1.45;max-height:84rpx;overflow:hidden}
.posted-at{display:block;margin-top:6rpx;font-size:20rpx;color:var(--muted-foreground,#64748b)}
.video-slide .posted-at{color:#94a3b8}
.kx-dark{background:#0f172a;color:#e2e8f0}
.kx-dark .light-slide,.kx-dark .slide-copy{background:#1e293b;color:#e2e8f0}
</style>
<style>
.feed-swiper,
.feed-swiper .uni-swiper-wrapper,
.feed-swiper .uni-swiper-slides{scrollbar-width:none}
.feed-swiper::-webkit-scrollbar{width:0;height:0;display:none}
</style>
