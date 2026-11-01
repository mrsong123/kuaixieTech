<script setup lang="ts">
import {computed,ref} from 'vue';
import type {PaymentPlatform} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';
import {usePaymentWallet,paymentStateKey} from '@kuaixie/ui/usePaymentWallet';
import {api} from '../api/client';
import {uniEventValue} from '../utils/uniEvent';
const emit=defineEmits<{paid:[]}>();
let platform:PaymentPlatform='h5';
// #ifdef MP-WEIXIN
platform='mp_weixin';
// #endif
// #ifdef APP-PLUS
platform='app';
// #endif
const p=usePaymentWallet(api,platform,()=>emit('paid'),{get:key=>String(uni.getStorageSync(key)||''),set:(key,value)=>uni.setStorageSync(key,value),remove:key=>uni.removeStorageSync(key)});
const methodIndex=computed(()=>Math.max(0,p.activeMethods.value.findIndex(m=>m.id===p.merchantId.value)));
const currencyIndex=computed(()=>Math.max(0,p.availableCurrencies.value.findIndex(c=>c.code===p.currency.value)));
function chooseMethod(event:unknown){p.merchantId.value=p.activeMethods.value[Number(uniEventValue(event))]?.id||''}
function chooseCurrency(event:unknown){p.currency.value=p.availableCurrencies.value[Number(uniEventValue(event))]?.code||''}
function chooseReference(event:unknown){p.referenceTarget.value=p.currencies.value[Number(uniEventValue(event))]?.code||''}
function amountChanged(event:unknown){p.amountCny.value=String(uniEventValue(event)??'')}
const confirming=ref(false);
async function confirm(){
 if(!p.isCurrent()||confirming.value||p.busy.value)return;
 const expected=p.pending.value?.quoteId||p.quote.value?.id;if(!expected)return;
 confirming.value=true;
 try{if(!p.pending.value){const snapshot=p.quote.value;if(!snapshot)return;const answer=await uni.showModal({title:t('pay.title'),content:`${t('pay.payAmount')}: ${snapshot.paymentAmount} ${snapshot.paymentCurrency}\n${t('pay.creditAmount')}: ${snapshot.baseAmount} CNY`});if(!p.isCurrent()||!answer.confirm||p.quote.value!==snapshot)return}
  if(p.isCurrent())await p.confirm(expected);
 }finally{confirming.value=false}
}
async function openCheckout(){
 if(!p.isCurrent())return;const source=p.payment.value,checkout=source?.checkout;if(!checkout||!source)return;
 const paymentId=source.id;
 const current=()=>p.isCurrent()&&p.payment.value?.id===paymentId&&p.payment.value?.checkout?.value===checkout.value;
 try{
  if(checkout.kind==='wechat_jsapi'){
   const fields=JSON.parse(checkout.value) as {timeStamp:string;nonceStr:string;package:string;signType:'RSA';paySign:string};
   await uni.requestPayment({provider:'wxpay',...fields});if(current())await p.refresh(paymentId);return;
  }
  if(checkout.kind==='wechat_app'||checkout.kind==='alipay_app'){
   // App SDKs must be enabled in the release manifest; client success is never proof of receipt.
   await uni.requestPayment({provider:checkout.kind==='wechat_app'?'wxpay':'alipay',orderInfo:checkout.kind==='wechat_app'?JSON.parse(checkout.value):checkout.value});if(current())await p.refresh(paymentId);return;
  }
  if(checkout.kind==='redirect'){
   // #ifdef H5
   window.location.assign(checkout.value);return;
   // #endif
   // #ifndef H5
   uni.navigateTo({url:'/pages/payment-checkout/index?id='+encodeURIComponent(paymentId)});return;
   // #endif
  }
 }catch(e){if(current())p.error.value=e instanceof Error?uiErrorMessage(e):t('pay.notConfirmed')}
}
</script>
<template>
 <view class="funding-mobile">
  <view class="title">{{$t('pay.title')}}</view><text class="note">{{$t('pay.cnySettlement')}}</text>
  <view v-if="p.loading.value" class="note">{{$t('account.loading')}}</view><view v-else-if="!p.activeMethods.value.length" class="note">{{$t('pay.noMethods')}}</view>
  <view class="label">{{$t('pay.creditAmount')}}</view><input class="field" type="digit" :value="p.amountCny.value" :disabled="p.locked.value||!!p.payment.value" @input="amountChanged"/>
  <view class="label">{{$t('pay.method')}}</view><picker :disabled="p.locked.value||!!p.payment.value" :range="p.activeMethods.value.map(m=>m.name)" :value="methodIndex" @change="chooseMethod"><view class="field">{{p.selected.value?.name||$t('pay.choose')}}</view></picker>
  <view class="label">{{$t('pay.currency')}}</view><picker :disabled="p.locked.value||!!p.payment.value" :range="p.availableCurrencies.value.map(p.label)" :value="currencyIndex" @change="chooseCurrency"><view class="field">{{p.currency.value||$t('pay.choose')}}</view></picker>
  <button :disabled="p.locked.value||!!p.payment.value" @click="p.preview">{{$t('order.previewAction')}}</button>
  <view v-if="p.quote.value" class="receipt"><view>{{$t('pay.payAmount')}}: {{p.quote.value.paymentAmount}} {{p.quote.value.paymentCurrency}}</view><view>{{$t('pay.creditAmount')}}: {{p.quote.value.baseAmount}} CNY</view><view>{{$t('pay.rate')}}: 1 CNY = {{p.quote.value.rate}} {{p.quote.value.paymentCurrency}}</view><view>{{$t('pay.fxMarkup')}}: {{(p.quote.value.spreadBps/100).toFixed(2)}}%</view><view>{{$t('pay.issuerFees')}}</view><view>{{$t('pay.rateAsOf')}}: {{p.quote.value.rateAsOf}}</view><view>{{$t('pay.expires')}}: {{p.quote.value.expiresAt}}</view></view>
  <button v-if="(p.quote.value||p.pending.value)&&!p.payment.value" :disabled="p.busy.value||confirming" @click="confirm">{{$t(p.pending.value?'v15.retry':'pay.confirm')}}</button>
  <view v-if="p.payment.value" class="receipt"><view>{{$t(paymentStateKey(p.payment.value.status))}}</view><view>{{p.payment.value.paymentAmount}} {{p.payment.value.paymentCurrency}}</view><button v-if="p.payment.value.checkout&&p.payment.value.status!=='paid'" :disabled="p.busy.value" @click="openCheckout">{{$t('pay.openCheckout')}}</button><button :disabled="p.busy.value" @click="p.refresh()">{{$t('pay.refresh')}}</button><button v-if="p.payment.value.status==='paid'" @click="p.again">{{$t('pay.newPayment')}}</button><text class="note">{{$t('pay.verifiedOnly')}}</text></view>
  <view v-if="p.pending.value" class="note">{{$t('pay.pending')}}</view><view v-if="p.error.value" class="error">{{p.error.value}}</view>
  <view v-if="p.history.value.length"><view class="label">{{$t('pay.history')}}</view><button v-for="row in p.history.value" :key="row.id" :disabled="p.locked.value" @click="p.resume(row)">{{row.paymentAmount}} {{row.paymentCurrency}} · {{$t(paymentStateKey(row.status))}}</button></view>
  <view class="label">{{$t('pay.referenceTitle')}}</view><text class="note">CNY →</text><picker :range="p.currencies.value.map(p.label)" @change="chooseReference"><view class="field">{{p.referenceTarget.value||$t('pay.choose')}}</view></picker><button :disabled="p.busy.value" @click="p.rate">{{$t('pay.query')}}</button><view v-if="p.reference.value" class="note">1 CNY = {{p.reference.value.rate}} {{p.reference.value.currency}} · {{p.reference.value.asOf}}</view><text class="note">{{$t('pay.referenceOnly')}}</text>
 </view>
</template>
<style scoped>
.funding-mobile{background:#fff;border:1px solid #dce4e1;border-radius:20rpx;padding:26rpx;margin:25rpx 0;color:#24352d}.title{font-size:32rpx;font-weight:700}.note{display:block;font-size:23rpx;color:#64748b;line-height:1.8;overflow-wrap:anywhere}.label{font-size:25rpx;margin:20rpx 0 8rpx}.field{border:1px solid #d1ded8;border-radius:9rpx;padding:16rpx;font-size:26rpx;min-height:32rpx}.funding-mobile button{font-size:25rpx;background:#0e7490;color:white;margin:18rpx 0}.receipt{padding:18rpx;background:#f2f7f4;border-radius:9rpx;font-size:24rpx;line-height:1.8;overflow-wrap:anywhere}.error{color:#a52e28;font-size:25rpx;overflow-wrap:anywhere}
</style>
