<script setup lang="ts">
import {computed} from 'vue';
import qrcode from 'qrcode-generator';
import type {ApiClient} from '@kuaixie/sdk';
import {usePaymentWallet,paymentStateKey} from './usePaymentWallet';
const props=defineProps<{client:ApiClient}>();
const p=usePaymentWallet(props.client,'web',undefined,{get:key=>localStorage.getItem(key),set:(key,value)=>localStorage.setItem(key,value),remove:key=>localStorage.removeItem(key)});
const qr=computed(()=>{if(p.payment.value?.checkout?.kind!=='qr_text')return '';try{const image=qrcode(0,'M');image.addData(p.payment.value.checkout.value);image.make();return image.createDataURL(6,8)}catch{return ''}});
</script>
<template>
 <section class="funding">
  <h2>{{$t('pay.title')}}</h2><p>{{$t('pay.cnySettlement')}}</p>
  <p v-if="p.loading.value">{{$t('account.loading')}}</p>
  <p v-else-if="!p.activeMethods.value.length">{{$t('pay.noMethods')}}</p>
  <form @submit.prevent="p.preview">
   <fieldset :disabled="p.locked.value||!!p.payment.value">
    <label>{{$t('pay.creditAmount')}}<input v-model="p.amountCny.value" inputmode="decimal" pattern="[0-9]+([.][0-9]{1,2})?" required/></label>
    <label>{{$t('pay.method')}}<select v-model="p.merchantId.value" required><option disabled value="">{{$t('pay.choose')}}</option><option v-for="method in p.activeMethods.value" :key="method.id" :value="method.id">{{method.name}}</option></select></label>
    <label>{{$t('pay.currency')}}<select v-model="p.currency.value" required><option disabled value="">{{$t('pay.choose')}}</option><option v-for="item in p.availableCurrencies.value" :key="item.code" :value="item.code">{{p.label(item)}}</option></select></label>
   </fieldset>
   <button type="submit" :disabled="p.locked.value||!!p.payment.value">{{$t('order.previewAction')}}</button>
  </form>
  <div v-if="p.quote.value" class="quote">
   <p>{{$t('pay.creditAmount')}}: {{p.quote.value.baseAmount}} CNY</p>
   <p><strong>{{$t('pay.payAmount')}}: {{p.quote.value.paymentAmount}} {{p.quote.value.paymentCurrency}}</strong></p>
   <p>{{$t('pay.rate')}}: 1 CNY = {{p.quote.value.rate}} {{p.quote.value.paymentCurrency}} · {{$t('pay.fxMarkup')}}: {{(p.quote.value.spreadBps/100).toFixed(2)}}%</p>
   <p>{{$t('pay.issuerFees')}}</p><p>{{$t('pay.rateAsOf')}}: {{p.quote.value.rateAsOf}}</p><p>{{$t('pay.expires')}}: {{p.quote.value.expiresAt}}</p>
  </div>
  <button v-if="(p.quote.value||p.pending.value)&&!p.payment.value" :disabled="p.busy.value" @click="p.confirm()">{{$t(p.pending.value?'v15.retry':'pay.confirm')}}</button>
  <div v-if="p.payment.value" class="quote">
   <p>{{$t(paymentStateKey(p.payment.value.status))}} · {{p.payment.value.paymentAmount}} {{p.payment.value.paymentCurrency}}</p>
   <img v-if="qr" :src="qr" :alt="$t('pay.scan')"/>
   <a v-if="p.payment.value.status!=='paid'&&p.payment.value.checkout?.kind==='redirect'" :href="p.payment.value.checkout.value" target="_blank" rel="noopener noreferrer">{{$t('pay.openCheckout')}}</a>
   <button :disabled="p.busy.value" @click="p.refresh()">{{$t('pay.refresh')}}</button>
   <button v-if="p.payment.value.status==='paid'" @click="p.again">{{$t('pay.newPayment')}}</button>
   <p>{{$t('pay.verifiedOnly')}}</p>
  </div>
  <p v-if="p.pending.value">{{$t('pay.pending')}}</p><p v-if="p.error.value" role="alert">{{p.error.value}}</p>
<div v-if="p.history.value.length"><h3>{{$t('pay.history')}}</h3><button v-for="row in p.history.value" :key="row.id" :disabled="p.locked.value" @click="p.resume(row)">{{row.paymentAmount}} {{row.paymentCurrency}} · {{$t(paymentStateKey(row.status))}}</button></div>
  <details><summary>{{$t('pay.referenceTitle')}}</summary><div class="reference">
   <select v-model="p.referenceBase.value"><option v-for="item in p.currencies.value" :key="item.code" :value="item.code">{{p.label(item)}}</option></select>
   <select v-model="p.referenceTarget.value"><option disabled value="">{{$t('pay.choose')}}</option><option v-for="item in p.currencies.value" :key="item.code" :value="item.code">{{p.label(item)}}</option></select>
   <button :disabled="p.busy.value" @click="p.rate">{{$t('pay.query')}}</button>
   <p v-if="p.reference.value">1 {{p.reference.value.base}} = {{p.reference.value.rate}} {{p.reference.value.currency}} · {{p.reference.value.asOf}}</p><p>{{$t('pay.referenceOnly')}}</p>
  </div></details>
 </section>
</template>
<style scoped>
.funding{background:var(--kx-surface,#fff);border:1px solid #dce6df;border-radius:16px;padding:24px;margin:20px 0;color:var(--kx-text,#24352d)}.funding p{font-size:13px;line-height:1.7;overflow-wrap:anywhere}.funding fieldset{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;border:0;padding:0;margin:16px 0}.funding label{display:grid;gap:8px;font-size:13px}.funding input,.funding select{padding:10px;border:1px solid #ccd8cf;border-radius:8px;max-width:100%;width:100%;box-sizing:border-box}.funding button,.funding a{display:inline-block;margin:8px 8px 8px 0;padding:10px 15px;border:0;border-radius:8px;background:#245b45;color:white;font:inherit;font-size:13px}.funding .quote{background:#f4f8f5;border-radius:10px;padding:12px;margin-top:12px}.funding img{display:block;width:220px;max-width:100%;image-rendering:pixelated}.reference{padding-top:12px}.reference select{width:auto;max-width:40%;margin:5px}summary{cursor:pointer;margin-top:20px}@media(max-width:650px){.funding fieldset{grid-template-columns:1fr}}
</style>
