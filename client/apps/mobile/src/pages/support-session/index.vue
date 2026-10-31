<script setup lang="ts">
import {useSupportSessionPage} from '../../stores/useSupportSessionPage';
const {prefs,id,conversation,messages,draft,busy,loading,error,load,send,handoff,mobilePreferenceStyle} = useSupportSessionPage();
</script>
<template><view class="original-subpage" :class="{'kx-dark':prefs.theme==='dark'}" :style="mobilePreferenceStyle(prefs)">
<text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{$t('migration.supportTitle')}}</view>
<view class="op-row"><text>{{conversation?.status||$t('account.loading')}}</text><text @click="load">{{$t('common.retry')}}</text></view>
<view v-if="loading" class="op-empty">{{$t('account.loading')}}</view><view v-else-if="!messages.length&&!error" class="op-empty">{{$t('migration.emptyChat')}}</view>
<view v-for="message in messages" :key="message.id" class="op-chat-bubble" :class="{mine:message.sender_type==='user'}">{{message.content}}</view>
<view v-if="error" class="op-error">{{error}}</view>
<button v-if="conversation?.status==='ai'" class="op-action op-secondary" :disabled="busy" @click="handoff">{{$t('migration.handoff')}}</button>
<view class="op-composer"><input class="op-input" v-model="draft" :placeholder="$t('portal.chat.placeholder')" @confirm="send"/><button class="op-action" :disabled="busy||!draft.trim()||conversation?.status==='closed'" @click="send">{{$t('portal.chat.send')}}</button></view>
<text class="op-muted">{{$t('migration.supportLimit')}}</text>
</view></template>
