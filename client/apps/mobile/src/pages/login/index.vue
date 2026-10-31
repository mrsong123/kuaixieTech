<script setup lang="ts">
import {computed,ref} from 'vue';
import {useV15LoginPage} from '../../stores/useV15LoginPage';
import OriginalMdiIcon from '../../components/OriginalMdiIcon.vue';
const {prefs,mode,channel,contact,countryCode,password,code,inviteCode,role,enterpriseName,businessLicenseNo,withCode,agreed,busy,error,notice,passwordFailed,captchaImage,captchaText,codeWait,countryCodes,needCode,needPassword,sendCode,submit,wechatLogin,agreement,changeMode,openRecover,askSupport,refreshCaptcha,chooseCountry,mobilePreferenceStyle} = useV15LoginPage();
const passwordShown=ref(false);
const contactPlaceholder=computed(()=>channel.value==='email'?'xxx@xxx.com':'13800000000');
const titleKey=computed(()=>mode.value==='recover'?'v15.resetTitle':mode.value==='signin'?'v15.login':'v15.signup');
const descKey=computed(()=>mode.value==='recover'?'v15.resetHint':mode.value==='signin'?'v15.welcome':'v15.createAccount');
function pickCountry(event:{detail:{value:string|number}}){const next=countryCodes[Number(event.detail.value)];if(next)chooseCountry(next)}
function requestCode(){if(codeWait.value>0)return;void sendCode()}
function togglePassword(){withCode.value=!withCode.value;code.value='';password.value='';passwordFailed.value=false}
</script>
<template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><view class="v15-login-content">
 <view class="op-title">{{$t(titleKey)}}</view><text class="op-desc">{{$t(descKey)}}</text>
 <view class="v15-choices"><view class="v15-choice" :class="{selected:channel==='phone'}" @click="channel='phone';code=''"><text>{{$t('v15.phone')}}</text></view><view class="v15-choice" :class="{selected:channel==='email'}" @click="channel='email';code=''"><text>{{$t('v15.email')}}</text></view></view>
 <view v-if="channel==='phone'" class="v15-affix"><view class="v15-lock"><OriginalMdiIcon name="lock-outline" tone="accent"/></view><picker class="v15-prefix" :range="countryCodes" :value="countryCodes.indexOf(countryCode)" @change="pickCountry"><text class="v15-prefix-label">{{countryCode}}</text></picker><input class="v15-field v15-affix-input" v-model="contact" type="number" maxlength="15" :placeholder="contactPlaceholder"/></view>
 <view v-else class="v15-affix"><view class="v15-lock"><OriginalMdiIcon name="lock-outline" tone="accent"/></view><input class="v15-field v15-affix-input" v-model="contact" type="text" maxlength="80" :placeholder="contactPlaceholder"/></view>
 <view v-if="needCode" class="v15-affix"><image v-if="captchaImage" class="v15-captcha" :src="captchaImage" mode="aspectFit" @click="refreshCaptcha(true)"/><view v-else class="v15-captcha v15-captcha-empty" @click="refreshCaptcha(true)">↻</view><input class="v15-field v15-affix-input" v-model="captchaText" maxlength="4" placeholder="ABCD"/></view>
 <view v-if="needCode" class="v15-affix"><input class="v15-field v15-affix-input" v-model="code" maxlength="6" placeholder="000000"/><view class="v15-code-action" :class="{disabled:busy||codeWait>0}" @click="requestCode">{{codeWait>0?codeWait+'s':$t('v15.sendCode')}}</view></view>
 <view v-if="needPassword" class="v15-affix"><view class="v15-lock"><OriginalMdiIcon name="lock-outline" tone="accent"/></view><input class="v15-field v15-affix-input" v-model="password" :password="!passwordShown" maxlength="20" placeholder="******"/><view class="v15-suffix" @click="passwordShown=!passwordShown"><OriginalMdiIcon :name="passwordShown?'eye-off-outline':'eye-outline'" tone="accent"/></view></view>
 <view v-if="mode==='signin'" class="v15-switch" @click="togglePassword">{{$t(withCode?'v15.passwordLogin':'v15.codeLogin')}}</view>
 <view v-if="passwordFailed&&mode==='signin'&&!withCode" class="v15-switch" @click="openRecover">{{$t('v15.forgot')}}</view>
 <template v-if="mode==='signup'">
  <view class="v15-choices"><view class="v15-choice" :class="{selected:role==='buyer'}" @click="role='buyer'">{{$t('v15.buyer')}}</view><view class="v15-choice" :class="{selected:role==='carrier'}" @click="role='carrier'">{{$t('v15.carrier')}}</view><view class="v15-choice" :class="{selected:role==='enterprise'}" @click="role='enterprise'">{{$t('v15.enterpriseAccount')}}</view></view>
  <view v-if="role!=='enterprise'" class="role-note"><text class="role-note-title">{{$t('v15.enterpriseAccount')}}</text><text class="role-note-body">{{$t('v15.sameAccount')}}{{$t('v15.enterpriseFields')}}</text></view>
  <template v-else>
   <text class="enterprise-title">{{$t('v15.enterpriseRegister')}}</text>
   <view class="v15-affix"><view class="v15-lock"><OriginalMdiIcon name="lock-outline" tone="accent"/></view><text class="v15-prefix-label">{{$t('fast.field.enterpriseName')}}</text><input class="v15-field v15-affix-input" v-model="enterpriseName" maxlength="80"/></view>
   <view class="v15-affix"><view class="v15-lock"><OriginalMdiIcon name="lock-outline" tone="accent"/></view><input class="v15-field v15-affix-input" v-model="businessLicenseNo" maxlength="18" placeholder="91350000MA00000000"/></view>
   <view class="role-note"><text class="role-note-body">{{$t('v15.licenseImageNote')}}</text></view>
  </template>
  <input v-model="inviteCode" class="v15-field" maxlength="40" :placeholder="$t('v15.inviteCode')"/>
 </template>
 <view v-if="mode!=='recover'" class="agree-row"><checkbox :checked="agreed" @click="agreed=!agreed" color="#0ea5e9"/><view class="agree-copy"><text class="op-muted" @click="agreed=!agreed">{{$t('v15.agreeBefore')}}</text><text class="agree-link" @click.stop="agreement('agreement')">{{$t('v15.agreement')}}</text><text class="op-muted">{{$t('v15.agreeAnd')}}</text><text class="agree-link" @click.stop="agreement('privacy')">{{$t('v15.privacy')}}</text></view></view>
 <button class="op-action" :disabled="busy" @click="submit">{{$t(mode==='recover'?'v15.resetAction':mode==='signin'?'v15.login':'v15.signup')}}</button>
 <view v-if="mode!=='recover'" class="wechat-login" @click="wechatLogin"><OriginalMdiIcon name="wechat" tone="accent"/><text>{{$t('v15.wechatLogin')}}</text></view>
 <text v-if="mode==='recover'" class="v15-support" @click="askSupport">{{$t('v15.supportLink')}}</text>
 <view class="v15-switch" @click="changeMode">{{$t(mode==='signin'?'v15.noAccount':'v15.hasAccount')}}</view>
 <view v-if="error" class="op-error">{{error}}</view><view v-if="notice" class="op-banner">{{notice}}</view><view class="op-muted">{{$t('v15.authContract')}}</view>
 </view></scroll-view></template>
<style>
.original-subpage .v15-login-content .v15-affix{display:flex;align-items:center;width:100%;box-sizing:border-box;min-height:80rpx;margin-bottom:18rpx;border:1rpx solid var(--border,#e2e8f0);border-radius:16rpx;background:var(--card,#fff);overflow:hidden}
.original-subpage .v15-login-content .v15-affix .v15-field{flex:1;width:auto;min-width:0;margin:0;border:0;background:transparent;min-height:80rpx}
.original-subpage .v15-login-content .v15-lock{flex:0 0 56rpx;width:56rpx;height:80rpx;display:flex;align-items:center;justify-content:center}
.original-subpage .v15-login-content .v15-lock .mdi-asset{width:28rpx;height:28rpx}
.original-subpage .v15-login-content .v15-prefix{flex:0 0 auto;display:flex;align-items:center;align-self:stretch}
.original-subpage .v15-login-content .v15-prefix-label{display:flex;align-items:center;height:80rpx;padding:0 14rpx;border-right:1rpx solid var(--border,#e2e8f0);color:var(--foreground,#0f172a);font-size:22rpx;line-height:80rpx;white-space:nowrap}
.original-subpage .v15-login-content .v15-suffix{flex:0 0 72rpx;width:72rpx;height:80rpx;display:flex;align-items:center;justify-content:center}
.original-subpage .v15-login-content .v15-suffix .mdi-asset{width:36rpx;height:36rpx}
.original-subpage .v15-login-content .v15-captcha{flex:0 0 180rpx;width:180rpx;height:80rpx;border-right:1rpx solid var(--border,#e2e8f0);background:#f8fafc}
.original-subpage .v15-login-content .v15-captcha-empty{display:flex;align-items:center;justify-content:center;color:#64748b;font-size:22rpx;letter-spacing:2rpx}
.original-subpage .v15-login-content .v15-code-action{flex:0 0 auto;align-self:stretch;display:flex;align-items:center;padding:0 22rpx;border-left:1rpx solid var(--border,#e2e8f0);color:var(--primary,#0284c7);font-size:24rpx;line-height:1.2;white-space:nowrap;background:transparent}
.original-subpage .v15-login-content .v15-code-action.disabled{opacity:.45}
.original-subpage .v15-login-content .enterprise-title{display:block;margin:-4rpx 0 14rpx;font-size:28rpx;font-weight:700;line-height:1.4;color:var(--foreground,#0f172a)}
.original-subpage .v15-login-content .role-note{margin:-6rpx 0 18rpx;padding:16rpx 18rpx;border-radius:16rpx;background:var(--primary-08,rgba(14,165,233,.08))}
.original-subpage .v15-login-content .role-note-title{display:block;font-size:24rpx;font-weight:700;line-height:1.4;color:var(--foreground,#0f172a)}
.original-subpage .v15-login-content .role-note-body{display:block;margin-top:6rpx;font-size:22rpx;line-height:1.55;color:var(--muted-foreground,#64748b)}
.original-subpage .v15-login-content .agree-row{display:flex;align-items:flex-start;gap:12rpx;margin:8rpx 0 22rpx}
.original-subpage .v15-login-content .agree-copy{flex:1;min-width:0;font-size:24rpx;line-height:1.6}
.original-subpage .v15-login-content .agree-link{color:var(--primary,#0284c7);text-decoration:underline}
.original-subpage .v15-login-content .wechat-login{display:flex;align-items:center;justify-content:center;gap:12rpx;min-height:80rpx;margin-top:18rpx;border:1rpx solid #07c160;border-radius:16rpx;color:var(--foreground,#0f172a);font-size:28rpx;font-weight:650}
.original-subpage .v15-login-content .wechat-login .mdi-asset{width:36rpx;height:36rpx}
.original-subpage .v15-login-content .v15-support{display:block;margin-top:18rpx;color:var(--primary,#0284c7);font-size:24rpx;line-height:1.5;text-decoration:underline}
</style>
