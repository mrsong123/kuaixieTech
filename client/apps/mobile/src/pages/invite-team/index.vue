<script setup lang="ts">
import {useInviteTeamPage} from '../../stores/useInviteTeamPage';
const {prefs,rows,loading,error,loaded,field,load,mobilePreferenceStyle} = useInviteTeamPage();
</script>
<template><view class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)">
 <text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text>
 <view class="op-title">{{$t('migration.teamTitle')}}</view><text class="op-desc">{{$t('migration.teamDesc')}}</text>
 <view v-if="loading" class="op-empty">{{$t('account.loading')}}</view>
 <view v-else-if="error" class="op-error">{{error}}<button class="op-action op-secondary" @click="load">{{$t('common.retry')}}</button></view>
 <view v-else-if="loaded&&!rows.length" class="op-empty">{{$t('account.empty')}}</view>
 <view v-for="(row,index) in rows" :key="String(row.id||index)" class="op-card">
 <view class="op-card-title">{{field(row,['nickname','display_name','name','member_name'])||$t('migration.memberUnknown')}}</view>
 <view class="op-row"><text>{{$t('migration.memberLevel')}}</text><text>{{field(row,['level','tier'])||$t('common.notProvided')}}</text></view>
 <view v-if="field(row,['created_at','joined_at'])" class="op-row"><text>{{$t('account.created')}}</text><text>{{field(row,['created_at','joined_at'])}}</text></view>
 </view><text class="op-muted">{{$t('migration.teamLimit')}}</text>
 </view></template>
