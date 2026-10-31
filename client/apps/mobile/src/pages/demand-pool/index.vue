<script setup lang="ts">
import {useDemandPoolPage} from '../../stores/useDemandPoolPage';
import {onMounted,onUnmounted} from 'vue';
import {t} from '@kuaixie/i18n';
const {prefs,style,loading,error,category,shown,safeUrl,reload,publish,openDetail,formatDate} = useDemandPoolPage();
const tabs=[
 {key:'all' as const,label:'portal.community.discover'},
 {key:'video' as const,label:'portal.community.video'},
 {key:'image' as const,label:'portal.community.image'}
];
function setCategory(key:'all'|'video'|'image'){category.value=key}
function excerpt(value?:string){
 const text=(value||'').trim();
 if(!text)return t('portal.community.noCaption');
 return text.length>90?text.slice(0,90)+'…':text;
}
function kindLabel(kind?:string){
 if(kind==='video')return t('portal.community.video');
 if(kind==='image')return t('portal.community.image');
 return '';
}
function lockDocumentScroll(locked:boolean){
 if(typeof document==='undefined')return;
 document.documentElement.style.overflow=locked?'hidden':'';
 document.body.style.overflow=locked?'hidden':'';
}
onMounted(()=>lockDocumentScroll(true));
onUnmounted(()=>lockDocumentScroll(false));
</script>
<template>
 <view class="min-h-screen bg-background community-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="style">
  <view class="flex flex-row items-center px-4 pt-4 pb-2">
   <view v-for="tab in tabs" :key="tab.key" class="mr-2 px-3 py-2 rounded-full border border-border" :style="{backgroundColor:category===tab.key?'var(--primary-15)':'transparent'}" @click="setCategory(tab.key)">
    <text class="text-xs" :style="{color:category===tab.key?'var(--primary)':'var(--muted-foreground)',fontWeight:category===tab.key?700:400}">{{ $t(tab.label) }}</text>
   </view>
   <view class="ml-auto px-3 py-2 rounded-full" style="background-color:var(--primary)" @click="publish">
    <text class="text-xs" style="color:#ffffff">{{ $t('portal.publish.title') }}</text>
   </view>
  </view>
  <scroll-view scroll-y class="community-scroll pb-6" :show-scrollbar="false">
   <view class="w-full px-4">
    <view v-if="loading" class="flex flex-col items-center py-12">
     <text class="mt-3 text-sm font-medium text-muted-foreground">{{ $t('portal.community.loading') }}</text>
    </view>
    <view v-else-if="error" class="flex flex-col items-center py-12" @click="reload">
     <text class="mt-3 text-sm font-medium text-muted-foreground">{{ $t('portal.community.notReady') }}</text>
     <text class="text-xs text-primary mt-2">{{ $t('portal.community.refresh') }}</text>
    </view>
    <view v-else-if="!shown.length" class="flex flex-col items-center py-12">
     <text class="mt-3 text-sm font-medium text-muted-foreground">{{ $t('portal.community.empty') }}</text>
     <text class="text-xs text-muted-foreground mt-1 px-8">{{ $t('portal.community.postReview') }}</text>
    </view>
    <view v-for="post in shown" :key="post.id" class="bg-card rounded-xl border border-border p-4 mb-3" @click="openDetail(post)">
     <view class="flex flex-row items-center justify-between mb-2">
      <view class="flex flex-row items-center flex-1">
       <view v-if="kindLabel(post.media_kind)" class="mr-2 px-2 py-1 rounded" style="background-color:var(--primary-10)">
        <text class="text-xs" style="color:var(--primary)">{{ kindLabel(post.media_kind) }}</text>
       </view>
       <text class="text-sm font-bold text-foreground flex-1">{{ excerpt(post.caption) }}</text>
      </view>
      <text class="text-xs text-muted-foreground">{{ formatDate(post.created_at) }}</text>
     </view>
     <view v-if="post.media_kind==='video'&&safeUrl(post.media_url)" class="video-frame mb-2" @click.stop>
      <video :src="safeUrl(post.media_url)" :poster="safeUrl(post.poster_url)" :controls="true" :autoplay="false" :show-center-play-btn="true" :show-fullscreen-btn="true" object-fit="contain"/>
     </view>
     <view v-else-if="post.media_kind==='image'&&safeUrl(post.media_url)" class="flex flex-row mb-2">
      <image :src="safeUrl(post.media_url)" mode="aspectFill" class="mr-2 rounded-md" style="width:90px;height:90px"/>
     </view>
     <view class="flex flex-row items-center justify-between">
      <view class="flex flex-row items-center">
       <view class="avatar mr-2"><image v-if="safeUrl(post.author_avatar)" :src="safeUrl(post.author_avatar)" mode="aspectFill"/><text v-else class="text-xs text-muted-foreground">◯</text></view>
       <text class="text-xs text-muted-foreground">{{ post.author_name||$t('common.notProvided') }}</text>
      </view>
     </view>
    </view>
    <text v-if="!loading&&!error" class="block text-xs text-muted-foreground content-note">{{ $t('portal.community.contentWarning') }}</text>
   </view>
  </scroll-view>
 </view>
</template>
<style scoped>
.community-page{position:fixed;top:var(--window-top,0px);right:0;bottom:var(--window-bottom,0px);left:0;display:flex;flex-direction:column;overflow:hidden;max-width:1100rpx;margin:0 auto;color:var(--foreground,#0f172a);--primary:var(--prototype-accent,#0ea5e9)}
.min-h-screen{min-height:0}
.bg-background{background:var(--background,#ffffff)}
.bg-card{background:var(--card,#ffffff)}
.flex{display:flex}.flex-row{flex-direction:row}.flex-col{flex-direction:column}
.items-center{align-items:center}.justify-between{justify-content:space-between}
.flex-1{flex:1 1 0%;min-width:0}
.px-4{padding-left:32rpx;padding-right:32rpx}
.px-3{padding-left:24rpx;padding-right:24rpx}
.px-2{padding-left:16rpx;padding-right:16rpx}
.px-8{padding-left:64rpx;padding-right:64rpx}
.pt-4{padding-top:32rpx}.pb-2{padding-bottom:16rpx}.pb-6{padding-bottom:48rpx}
.py-1{padding-top:8rpx;padding-bottom:8rpx}.py-2{padding-top:16rpx;padding-bottom:16rpx}
.py-12{padding-top:96rpx;padding-bottom:96rpx}
.p-4{padding:32rpx}
.mr-2{margin-right:16rpx}.mb-2{margin-bottom:16rpx}.mb-3{margin-bottom:24rpx}.mt-1{margin-top:8rpx}.mt-2{margin-top:16rpx}.mt-3{margin-top:24rpx}
.ml-auto{margin-left:auto}
.rounded-full{border-radius:999px}.rounded-xl{border-radius:24rpx}.rounded{border-radius:8rpx}.rounded-md{border-radius:12rpx}
.border{border-width:1px;border-style:solid}.border-border{border-color:var(--border,#e2e8f0)}
.text-xs{font-size:24rpx}.text-sm{font-size:28rpx}.font-bold{font-weight:700}.font-medium{font-weight:500}
.text-foreground{color:var(--foreground,#0f172a)}.text-muted-foreground{color:var(--muted-foreground,#64748b)}.text-primary{color:var(--primary,#0ea5e9)}
.block{display:block}.w-full{width:100%}
.community-scroll{flex:1;height:1px;min-height:0}
.avatar{width:48rpx;height:48rpx;border-radius:50%;overflow:hidden;background:var(--primary-10,#e0f2fe);display:flex;align-items:center;justify-content:center;flex:0 0 48rpx}
.avatar image{width:100%;height:100%}
.video-frame{width:100%;background:#0f172a;border-radius:16rpx;overflow:hidden}
.video-frame video{width:100%;height:360rpx}
.content-note{padding:8rpx 0 32rpx;line-height:1.5}
.kx-dark{background:#0f172a;color:#e2e8f0}
.kx-dark .bg-card{background:#1e293b}
</style>
<style>
.community-scroll,
.community-scroll .uni-scroll-view{scrollbar-width:none;-ms-overflow-style:none}
.community-scroll::-webkit-scrollbar,
.community-scroll .uni-scroll-view::-webkit-scrollbar{width:0;height:0;display:none}
</style>
