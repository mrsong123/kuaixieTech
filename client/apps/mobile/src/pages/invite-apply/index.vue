<script setup lang="ts">
import {useInviteApplyPage} from '../../stores/useInviteApplyPage';
const {prefs,code,owner,busy,error,finished,lookup,accept,mobilePreferenceStyle} = useInviteApplyPage();
</script>
<template><view class="original-subpage" :class="{'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
 <text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{$t('migration.inviteApplyTitle')}}</view>
 <view class="op-card"><text class="op-field-label">{{$t('migration.inviteCode')}}</text><input class="op-input" v-model="code" maxlength="40" :placeholder="$t('migration.codePlaceholder')" @input="owner='';finished=false"/><button class="op-action" :disabled="busy||!code.trim()" @click="lookup">{{$t('migration.verifyCode')}}</button></view>
 <view v-if="owner" class="op-card"><view class="op-card-title">{{$t('migration.ownerConfirmed')}}</view><view class="op-muted">{{$t('migration.bindPermanent')}}</view><button v-if="!finished" class="op-action" :disabled="busy" @click="accept">{{$t('migration.confirmBind')}}</button><view v-else class="op-banner">{{$t('migration.bindSuccess')}}</view></view>
 <view v-if="error" class="op-error">{{error}}</view>
 </view></template>
