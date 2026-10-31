<script setup lang="ts">
import {useDemandDetailPage} from '../../stores/useDemandDetailPage';
const {prefs,id,item,loading,error,load,formatAmount,formatDate,mobilePreferenceStyle} = useDemandDetailPage();
</script>
<template><view class="original-subpage" :class="{'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
<text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{$t('migration.demandDetail')}}</view>
<view v-if="loading" class="op-empty">{{$t('account.loading')}}</view>
<view v-else-if="error" class="op-error">{{$t(error)}}<button class="op-action op-secondary" @click="load">{{$t('common.retry')}}</button></view>
<view v-else-if="!item" class="op-empty">{{$t('fast.notInFirstPage')}}</view>
<template v-else><view class="op-card"><view class="op-card-title">{{item.title}}</view>
<text v-if="item.description" class="op-desc">{{item.description}}</text>
<view v-if="item.city" class="op-row"><text>{{$t('portal.square.city')}}</text><text>{{item.city}}</text></view>
<view v-if="item.trip_type" class="op-row"><text>{{$t('portal.publish.region')}}</text><text>{{item.trip_type==='domestic'?$t('portal.square.domestic'):$t('portal.square.international')}}</text></view>
<view v-if="item.expect_price!=null" class="op-row"><text>{{$t('portal.square.price')}}</text><text>{{formatAmount(item.expect_price)}}</text></view>
<view v-if="item.deadline" class="op-row"><text>{{$t('portal.square.deadline')}}</text><text>{{formatDate(item.deadline)}}</text></view>
</view><view class="op-banner">{{$t('migration.noDemandTrade')}}</view></template>
</view></template>
