<script setup lang="ts">
import ProductPublishingMobile from '../../components/ProductPublishingMobile.vue';
import {usePublishHubPage} from '../../stores/usePublishHubPage';
const {prefs,rootStyle,selected,region,busy,notice,error,unconfirmed,sessionScope,acknowledgeUnconfirmed,login,demand,trip,post,transportOptions,transportTitle,changeMode,changeRegion,pickDate,transportChange,submit} = usePublishHubPage();
</script>
<template>
<view class="publish-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="rootStyle">
 <view class="header"><view class="heading">{{ $t('portal.publish.title') }}</view><view class="subtitle">{{ $t('portal.publish.subtitle') }}</view></view>
 <view class="choice-list">
  <view class="choice" :class="{selected:selected==='demand'}" @click="changeMode('demand')"><view class="choice-icon">⌕</view><view class="choice-copy"><view>{{ $t('portal.publish.demand') }}</view><text>{{ $t('portal.publish.demandDesc') }}</text></view><view class="choice-radio"/></view>
  <view class="choice" :class="{selected:selected==='trip'}" @click="changeMode('trip')"><view class="choice-icon">✈</view><view class="choice-copy"><view>{{ $t('portal.publish.trip') }}</view><text>{{ $t('portal.publish.tripDesc') }}</text></view><view class="choice-radio"/></view>
  <view class="choice" :class="{selected:selected==='product'}" @click="changeMode('product')"><view class="choice-icon">▣</view><view class="choice-copy"><view>{{ $t('product.title') }}</view><text>{{ $t('product.help') }}</text></view><view class="choice-radio"/></view>
  <view class="choice" :class="{selected:selected==='life'}" @click="changeMode('life')"><view class="choice-icon">♡</view><view class="choice-copy"><view>{{ $t('portal.publish.life') }}</view><text>{{ $t('portal.publish.lifeDesc') }}</text></view><view class="choice-radio"/></view>
 </view>
 <ProductPublishingMobile v-if="selected==='product'" :key="sessionScope"/>
 <view v-else class="editor-card">
  <template v-if="selected!=='life'"><view class="label">{{ $t('portal.publish.region') }}</view><view class="region-options"><view class="region-option" :class="{active:region==='domestic'}" @click="changeRegion('domestic')">{{ $t('portal.square.domestic') }}</view><view class="region-option" :class="{active:region==='international'}" @click="changeRegion('international')">{{ $t('portal.square.international') }}</view></view></template>
  <template v-if="selected==='demand'">
   <view class="label">{{ $t('portal.publish.demandTitle') }} *</view><input :disabled="busy||unconfirmed" v-model="demand.title" class="field" maxlength="200" :placeholder="$t('portal.publish.demandTitle')"/>
   <view class="label">{{ $t('portal.publish.demandDescription') }}</view><textarea :disabled="busy||unconfirmed" v-model="demand.description" class="field description" maxlength="4000" :placeholder="$t('portal.publish.demandDescription')"/>
   <view class="label">{{ $t('portal.publish.city') }}</view><input :disabled="busy||unconfirmed" v-model="demand.city" class="field" maxlength="64" :placeholder="$t('portal.publish.city')"/>
   <view class="label">{{ $t('portal.publish.budget') }}</view><input :disabled="busy||unconfirmed" v-model="demand.expectPrice" class="field" type="digit" :placeholder="$t('portal.publish.budget')"/>
  </template>
  <template v-else-if="selected==='trip'">
   <view class="label">{{ $t('portal.publish.origin') }} *</view><input :disabled="busy||unconfirmed" v-model="trip.originCity" maxlength="64" class="field" :placeholder="$t('portal.publish.origin')"/>
   <view class="label">{{ $t('portal.publish.destination') }} *</view><input :disabled="busy||unconfirmed" v-model="trip.destination" maxlength="128" class="field" :placeholder="$t('portal.publish.destination')"/>
   <view class="form-row"><view class="form-column"><view class="label">{{ $t('portal.publish.depart') }} *</view><picker :disabled="busy||unconfirmed" mode="date" @change="pickDate('departDate',$event)"><view class="field select-field">{{trip.departDate||$t('portal.publish.depart')}}</view></picker></view><view class="form-column"><view class="label">{{ $t('portal.publish.return') }} *</view><picker :disabled="busy||unconfirmed" mode="date" @change="pickDate('returnDate',$event)"><view class="field select-field">{{trip.returnDate||$t('portal.publish.return')}}</view></picker></view></view>
   <view class="form-row"><view class="form-column"><view class="label">{{ $t('portal.publish.transport') }}</view><picker :disabled="busy||unconfirmed" :range="transportOptions.map(transportTitle)" :value="transportOptions.indexOf(trip.transportMode)" @change="transportChange"><view class="field select-field">{{transportTitle(trip.transportMode)}}</view></picker></view><view class="form-column"><view class="label">{{ $t('portal.publish.capacity') }}</view><input :disabled="busy||unconfirmed" v-model="trip.capacityQty" class="field" type="number"/></view></view>
   <view class="hint">{{ $t('portal.publish.tripEligibility') }}</view>
  </template>
  <template v-else><view class="hint">{{$t('portal.publish.textOnly')}}</view><view class="label">{{ $t('portal.publish.life') }}</view><textarea :disabled="busy||unconfirmed" v-model="post.caption" class="field description" maxlength="4000" :placeholder="$t('portal.publish.lifeDesc')"/></template>
  <view class="hint">{{ $t('portal.publish.note') }}</view>
  <button class="submit" :disabled="busy||unconfirmed" @click="submit">{{busy?$t('portal.chat.sending'):$t('portal.publish.submit')}}</button>
  <view v-if="notice" class="notice" role="status">{{ $t(notice) }}</view>
  <view v-if="error" class="notice" role="alert">{{error}}</view>
  <button v-if="unconfirmed" class="submit" @click="acknowledgeUnconfirmed">{{$t('portal.publish.checkedRecords')}}</button>
  <button v-if="notice==='portal.publish.login'" class="submit" @click="login">{{$t('portal.publish.goLogin')}}</button>
  <view class="review-note">{{ $t('portal.publish.review') }}</view>
 </view>
</view>
</template>
<style scoped>
.publish-page{min-height:100vh;background:#f8fafc;max-width:1100rpx;margin:auto;padding:35rpx 28rpx calc(110rpx + env(safe-area-inset-bottom));color:#0f172a;--prototype-accent:#0ea5e9}.header{margin-bottom:24rpx}.heading{font-size:41rpx;font-weight:770}.subtitle{font-size:24rpx;line-height:1.65;color:#64748b;margin-top:9rpx}.choice-list{display:flex;flex-direction:column;gap:16rpx}.choice{display:flex;align-items:center;gap:19rpx;background:#fff;border:2rpx solid #e2e8f0;border-radius:19rpx;padding:20rpx 21rpx;min-height:118rpx}.choice.selected{border-color:var(--prototype-accent);background:#f0f9ff}.choice-icon{background:#e0f2fe;border-radius:16rpx;color:var(--prototype-accent);width:72rpx;height:72rpx;flex:0 0 72rpx;display:flex;align-items:center;justify-content:center;font-size:38rpx}.choice-copy{flex:1;min-width:0}.choice-copy view{font-size:27rpx;font-weight:720}.choice-copy text{font-size:21rpx;color:#64748b;line-height:1.6;display:block;margin-top:5rpx;word-break:break-word}.choice-radio{width:22rpx;height:22rpx;border:2rpx solid #cbd5e1;border-radius:50%;flex:0 0 22rpx}.choice.selected .choice-radio{background:var(--prototype-accent);border-color:var(--prototype-accent);box-shadow:inset 0 0 0 4rpx #fff}
.editor-card{background:#fff;border:1rpx solid #e2e8f0;margin-top:25rpx;border-radius:23rpx;padding:28rpx}.label{display:block;font-size:24rpx;font-weight:650;margin:22rpx 0 10rpx;color:#475569}.region-options{display:flex;gap:12rpx;flex-wrap:wrap}.region-option{border:1rpx solid #cbd5e1;color:#475569;border-radius:15rpx;padding:15rpx 25rpx;font-size:24rpx}.region-option.active{background:var(--prototype-accent);color:#fff;border-color:var(--prototype-accent)}.field{background:#f8fafc;border:1rpx solid #e2e8f0;border-radius:13rpx;width:100%;height:82rpx;padding:15rpx 18rpx;color:#0f172a;font-size:26rpx;min-width:0}.description{height:190rpx;line-height:1.6}.select-field{display:flex;align-items:center;overflow-wrap:anywhere;min-height:82rpx;height:auto}.form-row{display:flex;gap:17rpx}.form-column{flex:1;min-width:0}.hint{font-size:21rpx;line-height:1.7;color:#94a3b8;margin-top:20rpx}.submit{margin-top:28rpx;background:var(--prototype-accent);color:#fff;border-radius:15rpx;font-size:28rpx;padding:12rpx 0;font-weight:700}.submit[disabled]{opacity:.65}.notice{background:#eff6ff;border-radius:13rpx;padding:18rpx;color:#075985;font-size:24rpx;margin-top:15rpx;line-height:1.6}.review-note{color:#94a3b8;font-size:21rpx;text-align:center;line-height:1.6;margin-top:20rpx}
.kx-dark{background:#171c1f;color:#f2f4f4}.kx-dark .choice,.kx-dark .editor-card{background:#202629;border-color:#475569}.kx-dark .choice.selected{background:#1d384a}.kx-dark .choice-copy text,.kx-dark .subtitle{color:#cbd5e1}.kx-dark .label{color:#e2e8f0}.kx-care .label,.kx-care .field{font-size:29rpx}@media(max-width:350px){.editor-card{padding:18rpx}.form-row{gap:10rpx}}
</style>
