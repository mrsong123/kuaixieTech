<script setup lang="ts">
import MineGroup from '../../components/mine/MineGroup.vue';
import MineRow from '../../components/mine/MineRow.vue';
import OriginalMdiIcon from '../../components/OriginalMdiIcon.vue';
import {useMineOriginalPage} from '../../stores/useMineOriginalPage';
const {prefs,profile,wallet,unread,loading,error,signedIn,nickname,score,verified,buyerVerified,isEnterprise,isAdmin,reload,select,statusLabel,toggleLanguage,signOut,formatAmount,mobilePreferenceStyle}=useMineOriginalPage();
</script>
<template>
<scroll-view scroll-y class="mine" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="mobilePreferenceStyle(prefs)">
 <view v-if="!signedIn" class="guest">
  <view class="guest-icon"><OriginalMdiIcon name="account-circle-outline"/></view><view class="guest-title">{{ $t('mine.guestTitle') }}</view>
  <view class="guest-desc">{{ $t('mine.guestDesc') }}</view>
  <button class="guest-action" @click="select('login')">{{ $t('common.goLogin') }}</button>
 </view>
 <template v-else>
  <!-- Original prototype: large full-width primary-colored profile header. -->
  <view class="mine-hero"><view class="mine-lang" @click="toggleLanguage">{{ $t('mine.languageSwitch') }}</view>
    <view class="mine-profile" @click="select('profile')">
      <image v-if="profile?.avatar_url" class="mine-avatar" :src="profile.avatar_url" mode="aspectFill"/>
      <view v-else class="mine-avatar mine-avatar-empty"><view class="avatar-symbol"><OriginalMdiIcon name="account" tone="white"/></view></view>
      <view class="mine-identity">
        <view class="mine-name-line"><text class="mine-nickname">{{nickname}}</text><text v-if="verified" class="mine-tier">{{ $t('mine.verifiedPro') }}</text></view>
        <text class="mine-stars" v-if="profile?.star_avg!=null || profile?.deal_count!=null || score!=null">{{ $t('mine.starDeals',{star:profile?.star_avg??$t('mine.ratingMissing'),deals:profile?.deal_count??$t('mine.ratingMissing'),score:score??$t('mine.ratingMissing')}) }}</text>
        <text class="mine-stars" v-else>{{ $t('mine.profileCard') }}</text>
      </view><text class="mine-profile-chevron">›</text>
    </view>
    <view class="mine-performance">
      <view class="mine-score">{{ $t('mine.performance') }} <text class="mine-score-value">{{score??$t('mine.ratingMissing')}}</text></view>
      <view class="mine-balance" @click="select('wallet')"><text>{{ $t('mine.availBalance') }}</text><text class="mine-balance-amount">{{wallet?formatAmount(wallet.balance_amount,wallet.currency):$t('mine.unavailableAmount')}}</text><text class="mine-profile-chevron">›</text></view>
    </view>
  </view>
  <view class="mine-body">
    <view v-if="error" class="mine-error" @click="reload">{{ $t(error) }} · {{ $t('common.retry') }}</view>
    <!-- Original 4 equal-width quick service buttons, in the original order. -->
    <view class="mine-quick">
      <view class="mine-quick-item" @click="select('orders')"><view class="quick-icon"><OriginalMdiIcon name="format-list-bulleted"/></view><text>{{ $t('mine.quickOrders') }}</text></view>
      <view class="mine-quick-item" @click="select('wallet')"><view class="quick-icon"><OriginalMdiIcon name="wallet"/></view><text>{{ $t('mine.quickWallet') }}</text></view>
      <view class="mine-quick-item" @click="select('notice')"><view class="quick-icon-wrapper"><view class="quick-icon"><OriginalMdiIcon name="bell-outline"/></view><text v-if="unread!==null && unread>0" class="mine-badge">{{unread}}</text></view><text>{{ $t('mine.quickNotice') }}</text></view>
      <view class="mine-quick-item" @click="select('support')"><view class="quick-icon"><OriginalMdiIcon name="headset"/></view><text>{{ $t('mine.quickSupport') }}</text></view>
    </view>
    <MineGroup :title="$t('mine.groupTrade')">
      <MineRow icon="i-mdi-airplane" :label="$t('mine.publishTrip')" :value="!verified?$t('mine.needCarrierVerifyShort'):undefined" @press="select('publishTrip')"/>
      <MineRow icon="i-mdi-map-marker-path" :label="$t('hub.myTrips')" @press="select('myTrips')"/>
      <MineRow icon="i-mdi-clipboard-search-outline" :label="$t('mine.publishDemand')" :value="!buyerVerified?$t('mine.needRealnameShort'):undefined" @press="select('publishDemand')"/>
      <MineRow icon="i-mdi-clipboard-text-outline" :label="$t('account.myDemands')" @press="select('myDemands')"/>
      <MineRow icon="i-mdi-account-check-outline" :label="$t('mine.buyerKyc')" :value="statusLabel(profile?.buyer_verify_status)" @press="select('buyerKyc')"/>
      <MineRow icon="i-mdi-card-account-details-outline" :label="$t('mine.carrierKyc')" :value="statusLabel(profile?.carrier_verify_status)" @press="select('carrierKyc')"/>
      <MineRow v-if="isEnterprise" icon="i-mdi-domain" :label="$t('mine.enterpriseKyc')" :value="statusLabel(profile?.enterprise_verify_status)" @press="select('enterpriseKyc')"/>
      <MineRow icon="i-mdi-package-variant" :label="$t('mine.myProducts')" :desc="verified?$t('mine.stockMgmt'):$t('mine.needVerifyFirst')" @press="select('myProducts')"/>
      <MineRow icon="i-mdi-heart-outline" :label="$t('mine.favoritesRow')" :desc="$t('mine.favoritesDesc')" @press="select('favorites')"/>
      <MineRow icon="i-mdi-cart-outline" :label="$t('mine.cartRow')" :desc="$t('mine.cartDesc')" @press="select('cart')"/>
      <MineRow icon="i-mdi-map-marker-outline" :label="$t('mine.addressRow')" @press="select('address')"/>
      <MineRow icon="i-mdi-receipt-text-outline" :label="$t('mine.invoiceRow')" @press="select('invoice')"/>
      <MineRow icon="i-mdi-hand-back-right-outline" :label="$t('mine.assistRow')" :desc="$t('mine.assistDesc')" @press="select('assist')"/>
    </MineGroup>
    <MineGroup :title="$t('mobileBusiness.group')">
      <MineRow icon="i-mdi-storefront-outline" :label="$t('shop.title')" :desc="$t('mobileBusiness.shopDesc')" @press="select('shop')"/>
      <MineRow icon="i-mdi-shield-check-outline" :label="$t('insurance.application')" :desc="$t('mobileBusiness.insuranceDesc')" @press="select('insurance')"/>
      <MineRow icon="i-mdi-file-document-outline" :label="$t('case.title')" :desc="$t('mobileBusiness.disputesDesc')" @press="select('disputes')"/>
      <MineRow icon="i-mdi-cash-refund" :label="$t('refund.title')" :desc="$t('mobileBusiness.refundsDesc')" @press="select('refunds')"/>
    </MineGroup>
    <MineGroup :title="$t('mine.groupCommunity')">
      <MineRow icon="i-mdi-forum-outline" :label="$t('mine.communityRow')" :desc="$t('mine.communityDesc')" @press="select('community')"/>
      <MineRow icon="i-mdi-bullhorn-outline" :label="$t('mine.announceRow')" :desc="$t('mine.announceDesc')" @press="select('announcements')"/>
    </MineGroup>
    <MineGroup :title="$t('mine.groupInvite')">
      <MineRow icon="i-mdi-account-plus-outline" :label="$t('mine.inviteRewards')" @press="select('invites')"/>
      <MineRow icon="i-mdi-crown-outline" :label="$t('v15.inviteTier')" @press="select('inviteTier')"/>
      <MineRow icon="i-mdi-crown-outline" :label="$t('mine.myTeam')" @press="select('team')"/>
      <MineRow icon="i-mdi-share-variant" :label="$t('mine.sendInvite')" @press="select('inviteShare')"/>
      <MineRow icon="i-mdi-email-open-outline" :label="$t('mine.joinApply')" @press="select('inviteApply')"/>
    </MineGroup>
    <MineGroup :title="$t('mine.groupOther')">
      <MineRow icon="i-mdi-view-grid-outline" :label="$t('fast.myServices')" @press="select('services')"/>
      <MineRow v-if="isAdmin" icon="i-mdi-shield-account" :label="$t('mine.adminEntry')" :desc="$t('mine.adminDesc')" @press="select('admin')"/>
      <MineRow icon="i-mdi-palette-swatch-outline" :label="$t('settings.appearance')" :desc="$t('mine.appearanceDesc')" @press="select('appearance')"/>
      <MineRow icon="i-mdi-cog-outline" :label="$t('mine.settingsRow')" :desc="$t('mine.settingsDesc')" @press="select('settings')"/>
    </MineGroup>
    <view class="mine-logout" @click="signOut">{{ $t('mine.logout') }}</view>
    <text class="mine-footnote">{{ $t('mine.sandboxNote') }}</text>
  </view>
 </template>
</scroll-view>
</template>
<style scoped>
.mine{--kx-primary:var(--kx-accent,#0ea5e9);--kx-border:#e2e8f0;--kx-fg:#0f172a;--kx-fg-secondary:#64748b;--kx-surface:#fff;--kx-icon-bg:var(--muted,#f1f5f9);min-height:100vh;background:var(--kx-page-bg,var(--background,#ffffff));color:var(--kx-fg);font-size:27rpx;padding-bottom:calc(110rpx + env(safe-area-inset-bottom))}
/* #ifndef MP */
.mine{font-size:calc(27rpx * var(--kx-user-scale,1))}
/* #endif */
.mine-lang{display:flex;justify-content:flex-end;color:#fff;font-size:22rpx;min-height:35rpx;align-items:center;margin-bottom:15rpx}.mine-hero{background:var(--kx-primary);padding:48rpx 32rpx 40rpx;color:#fff}.mine-profile{display:flex;align-items:center;gap:23rpx}.mine-avatar{width:128rpx;height:128rpx;flex:0 0 128rpx;border-radius:50%;background:rgba(255,255,255,.22)}.mine-avatar-empty{display:flex;align-items:center;justify-content:center;font-size:65rpx;color:#fff}.mine-identity{flex:1;min-width:0}.mine-name-line{display:flex;align-items:center;gap:13rpx;flex-wrap:wrap}.mine-nickname{font-size:36rpx;font-weight:700;color:#fff;overflow-wrap:anywhere}.mine-tier{font-size:20rpx;padding:7rpx 11rpx;color:#fff;background:rgba(255,255,255,.23);border-radius:8rpx}.mine-stars{display:block;color:rgba(255,255,255,.86);font-size:21rpx;margin-top:10rpx;line-height:1.4;overflow-wrap:anywhere}.mine-profile-chevron{font-size:37rpx;color:rgba(255,255,255,.85)}.mine-performance{display:flex;justify-content:space-between;align-items:center;gap:15rpx;margin-top:26rpx}.mine-score{font-size:22rpx;color:#fff;border:1rpx solid rgba(255,255,255,.45);border-radius:40rpx;padding:8rpx 17rpx}.mine-score-value{font-weight:700}.mine-balance{display:flex;align-items:center;gap:8rpx;color:#fff;font-size:21rpx}.mine-balance-amount{font-size:29rpx;font-weight:700;overflow-wrap:anywhere}
.mine-body{padding:32rpx 32rpx 40rpx}.mine-quick{width:100%;background:var(--kx-surface);border:1rpx solid var(--kx-border);border-radius:24rpx;display:flex;padding:32rpx 0;gap:0}.mine-quick-item{flex:1;min-width:0;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:11rpx;text-align:center;font-size:21rpx;color:var(--kx-fg)}.quick-icon{width:48rpx;height:48rpx;line-height:1;display:flex;color:var(--kx-primary)}.avatar-symbol{width:76rpx;height:76rpx}.guest-icon{width:90rpx;height:90rpx;margin:auto}.quick-icon-wrapper{position:relative}.mine-badge{position:absolute;top:-10rpx;right:-20rpx;min-width:30rpx;border-radius:18rpx;background:#dc2626;color:#fff;padding:2rpx 6rpx;font-size:18rpx}.mine-error{color:#b91c1c;background:#fef2f2;border-radius:12rpx;padding:20rpx;margin-bottom:15rpx;font-size:23rpx}.mine-logout{margin:32rpx 0 0;border:1rpx solid var(--kx-border);background:var(--kx-surface);border-radius:24rpx;padding:24rpx;text-align:center;color:var(--kx-fg-secondary);font-size:28rpx}.mine-footnote{display:block;text-align:center;color:var(--kx-fg-secondary);font-size:24rpx;margin:32rpx 0 48rpx;line-height:1.65}.guest{padding:190rpx 40rpx;text-align:center}.guest-icon{font-size:86rpx;color:var(--kx-primary)}.guest-title{font-size:33rpx;font-weight:700;margin-top:30rpx}.guest-desc{font-size:23rpx;color:var(--kx-fg-secondary);margin-top:12rpx}.guest-action{margin-top:28rpx;background:var(--kx-primary);color:#fff;border-radius:16rpx;font-size:26rpx}
.kx-dark{--kx-page-bg:#171c1f;--kx-surface:#202629;--kx-fg:#f2f4f4;--kx-fg-secondary:#cbd5e1;--kx-border:#475569;--kx-icon-bg:#334155}.kx-care .mine-quick-item,.kx-care .mine-row-label{font-size:26rpx}
@media(max-width:355px){.mine-hero{padding:33rpx 22rpx}.mine-body{padding:21rpx 20rpx}.mine-balance{gap:4rpx}.mine-quick-item{font-size:20rpx}}@media(min-width:768px){.mine{max-width:920rpx;margin:auto}.mine-body{padding:30rpx}}
</style>
