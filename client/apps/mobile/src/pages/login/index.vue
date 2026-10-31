<script setup lang="ts">
import {useV15LoginPage} from '../../stores/useV15LoginPage';
const {prefs,mode,channel,contact,countryCode,password,code,inviteCode,role,withCode,agreed,busy,error,notice,countryCodes,needCode,sendCode,submit,agreement,changeMode,mobilePreferenceStyle} = useV15LoginPage();
</script>
<template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><view class="v15-login-content">
 <view class="op-title">{{$t(mode==='signin'?'v15.login':'v15.signup')}}</view><text class="op-desc">{{$t(mode==='signin'?'v15.welcome':'v15.createAccount')}}</text>
 <view class="v15-choices"><view class="v15-choice" :class="{selected:channel==='phone'}" @click="channel='phone';code=''"><text>{{$t('v15.phone')}}</text></view><view class="v15-choice" :class="{selected:channel==='email'}" @click="channel='email';code=''"><text>{{$t('v15.email')}}</text></view></view>
 <view v-if="channel==='phone'" class="v15-flow-row"><picker :range="countryCodes" :value="countryCodes.indexOf(countryCode)" @change="countryCode=countryCodes[Number($event.detail.value)]"><view class="v15-selector">{{countryCode}} ⌄</view></picker><input class="v15-field" v-model="contact" type="number" maxlength="15" :placeholder="$t('v15.phone')"/></view>
 <input v-else class="v15-field" v-model="contact" type="text" maxlength="80" :placeholder="$t('v15.email')"/>
 <template v-if="needCode"><view class="v15-flow-row"><input class="v15-field" v-model="code" maxlength="12" :placeholder="$t('v15.code')"/><button class="op-action op-secondary" :disabled="busy" @click="sendCode">{{$t('v15.sendCode')}}</button></view></template>
 <input v-if="mode==='signup'||!withCode" class="v15-field" v-model="password" type="text" password maxlength="128" :placeholder="$t('v15.password')"/>
 <view v-if="mode==='signin'" class="v15-switch" @click="withCode=!withCode;code='';password=''">{{$t(withCode?'v15.passwordLogin':'v15.codeLogin')}}</view>
 <template v-if="mode==='signup'"><view class="v15-choices"><view class="v15-choice" :class="{selected:role==='buyer'}" @click="role='buyer'">{{$t('v15.buyer')}}</view><view class="v15-choice" :class="{selected:role==='carrier'}" @click="role='carrier'">{{$t('v15.carrier')}}</view></view><input v-model="inviteCode" class="v15-field" maxlength="40" :placeholder="$t('v15.inviteCode')"/></template>
 <view class="v15-flow-row"><checkbox :checked="agreed" @click="agreed=!agreed" color="#0ea5e9"/><text class="op-muted" @click="agreed=!agreed">{{$t('v15.agree')}}</text></view><view class="v15-choices"><text class="v15-linked" @click="agreement('agreement')">{{$t('v15.agreement')}}</text><text class="v15-linked" @click="agreement('privacy')">{{$t('v15.privacy')}}</text></view>
 <button class="op-action" :disabled="busy" @click="submit">{{$t(mode==='signin'?'v15.login':'v15.signup')}}</button><view class="v15-switch" @click="changeMode">{{$t(mode==='signin'?'v15.noAccount':'v15.hasAccount')}}</view>
 <view v-if="error" class="op-error">{{error}}</view><view v-if="notice" class="op-banner">{{notice}}</view><view class="op-muted">{{$t('v15.authContract')}}</view>
 </view></scroll-view></template>
