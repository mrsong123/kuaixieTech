<script setup lang="ts">
import {ref,computed} from 'vue';
import {t,uiErrorMessage} from '@kuaixie/i18n';
import type {MerchantConfig,FxConfig,PaymentSettings} from '@kuaixie/sdk';
import {useAdminContext} from '../stores/context';
const {api}=useAdminContext();
const state=ref<PaymentSettings|null>(null),busy=ref(false),error=ref(''),notice=ref('');
const merchant=ref<MerchantConfig>({name:'',adapter:'wechat_native_v3',method:'wechat',platform:'web',currencies:['CNY'],credentialPrefix:null,spreadBps:0,quoteTtlSeconds:300,enabled:false,settings:{}});
const source=ref<FxConfig>({name:'',kind:'open_er_api',endpoint:null,credentialPrefix:null,maxAgeSeconds:86400,enabled:false,transactionEnabled:false});
const currencySearch=ref('');
const filtered=computed(()=>state.value?.currencies.filter(item=>(item.code+' '+item.name+' '+item.nameEn).toLowerCase().includes(currencySearch.value.toLowerCase()))??[]);
const methodLabels:Record<string,string>={wechat:'pay.method.wechat',alipay:'pay.method.alipay',bank_card:'pay.method.bank_card',credit_card:'pay.method.credit_card'};
const chosen=computed(()=>state.value?.adapters.find(a=>a.id===merchant.value.adapter));
async function load(){busy.value=true;error.value='';try{state.value=await api.payments.settings()}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
function edit(row:MerchantConfig){merchant.value=JSON.parse(JSON.stringify(row))}
function adapterChanged(){const adapter=chosen.value;if(!adapter)return;merchant.value.method=adapter.methods[0];merchant.value.platform=adapter.platforms[0];merchant.value.currencies=adapter.currencies.length?[...adapter.currencies]:[];merchant.value.enabled=false;merchant.value.settings={}}
async function saveMerchant(){busy.value=true;error.value='';notice.value='';try{
 const value={...merchant.value,credentialPrefix:merchant.value.credentialPrefix||null};
 const result=await api.payments.saveMerchant(value);merchant.value.id=result.id;notice.value=t('pay.saved');await load();
}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
async function saveFx(){busy.value=true;error.value='';notice.value='';try{const result=await api.payments.saveFx({...source.value,credentialPrefix:source.value.credentialPrefix||null});source.value.id=result.id;notice.value=t('pay.saved');await load()}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
function toggleCurrency(code:string,event:Event){const checked=(event.target as HTMLInputElement).checked;merchant.value.currencies=checked?[...new Set([...merchant.value.currencies,code])]:merchant.value.currencies.filter(value=>value!==code)}
</script>
<template>
 <section class="payment-settings surface">
  <h2>{{$t('pay.adminTitle')}}</h2><p>{{$t('pay.cnySettlement')}}</p><button :disabled="busy" @click="load">{{$t('pay.loadSettings')}}</button>
  <p v-if="error" role="alert">{{error}}</p><p v-if="notice" role="status">{{notice}}</p>
  <template v-if="state">
   <h3>{{$t('pay.merchants')}}</h3>
   <button v-for="row in state.merchants" :key="row.id" :disabled="busy" @click="edit(row)">{{row.name}} · {{row.platform}} · {{row.enabled?$t('pay.enabled'):$t('pay.disabled')}}</button>
   <form @submit.prevent="saveMerchant"><fieldset :disabled="busy">
    <label>{{$t('pay.name')}}<input v-model="merchant.name" maxlength="80" required/></label>
    <label>{{$t('pay.adapter')}}<select v-model="merchant.adapter" @change="adapterChanged"><option v-for="adapter in state.adapters" :key="adapter.id" :value="adapter.id">{{adapter.id}}</option></select></label>
    <label>{{$t('pay.method')}}<select v-model="merchant.method"><option v-for="method in chosen?.methods" :key="method" :value="method">{{$t(methodLabels[method]||'pay.method')}}</option></select></label>
    <label>{{$t('pay.platform')}}<select v-model="merchant.platform"><option v-for="platform in chosen?.platforms" :key="platform" :value="platform">{{platform}}</option></select></label>
    <label>{{$t('pay.credentialPrefix')}}<input v-model="merchant.credentialPrefix" pattern="[A-Z][A-Z0-9_]{2,39}"/></label>
    <label>{{$t('pay.spread')}}<input v-model.number="merchant.spreadBps" type="number" min="0" max="2000" required/></label>
    <label>{{$t('pay.quoteSeconds')}}<input v-model.number="merchant.quoteTtlSeconds" type="number" min="30" max="900" required/></label>
    <label>{{$t('pay.notifyUrl')}}<input v-model="merchant.settings.notifyUrl" type="url"/></label>
    <label v-if="merchant.adapter==='alipay_rsa2'">{{$t('pay.returnUrl')}}<input v-model="merchant.settings.returnUrl" type="url"/></label>
    <template v-if="merchant.adapter==='hosted_bank_v1'"><label>{{$t('pay.gatewayUrl')}}<input v-model="merchant.settings.gatewayUrl" type="url" required/></label><label>{{$t('pay.checkoutHosts')}}<input v-model="merchant.settings.checkoutHosts" required/></label></template>
    <label class="wide">{{$t('pay.currency')}}<input v-model="currencySearch" :placeholder="$t('pay.searchCurrency')"/></label>
    <div class="currency-options wide"><label v-for="item in filtered" :key="item.code"><input type="checkbox" :checked="merchant.currencies.includes(item.code)" :disabled="!!chosen?.currencies.length&&!chosen.currencies.includes(item.code)" @change="toggleCurrency(item.code,$event)"/>{{item.code}} · {{item.name}}</label></div>
    <label><input v-model="merchant.enabled" type="checkbox"/>{{$t('pay.enabled')}}</label><p class="wide">{{$t('pay.credentialsNotice')}}</p>
   </fieldset><button type="submit" :disabled="busy">{{$t('pay.saveMerchant')}}</button></form>
   <h3>{{$t('pay.fxSources')}}</h3><button v-for="row in state.fxSources" :key="row.id" @click="source={...row}">{{row.name}}</button>
   <form @submit.prevent="saveFx"><fieldset :disabled="busy">
    <label>{{$t('pay.name')}}<input v-model="source.name" maxlength="80" required/></label>
    <label>{{$t('pay.rateSource')}}<select v-model="source.kind"><option value="open_er_api">ExchangeRate-API Open</option><option value="rates_json_v1">HTTPS JSON v1</option></select></label>
    <label v-if="source.kind==='rates_json_v1'">{{$t('pay.gatewayUrl')}}<input v-model="source.endpoint" type="url" required/></label>
    <label v-if="source.kind==='rates_json_v1'">{{$t('pay.credentialPrefix')}}<input v-model="source.credentialPrefix"/></label>
    <label>{{$t('pay.rateAge')}}<input v-model.number="source.maxAgeSeconds" type="number" min="60" max="172800" required/></label>
    <label><input v-model="source.enabled" type="checkbox"/>{{$t('pay.enabled')}}</label><label class="wide"><input v-model="source.transactionEnabled" type="checkbox"/>{{$t('pay.transactionRates')}}</label>
   </fieldset><button type="submit" :disabled="busy">{{$t('pay.saveFx')}}</button></form>
  </template>
 </section>
</template>
<style scoped>
.payment-settings{padding:24px;margin-bottom:24px}.payment-settings p{font-size:13px;line-height:1.7;overflow-wrap:anywhere}.payment-settings fieldset{border:0;padding:0;margin:18px 0;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px}.payment-settings label{display:block;font-size:13px}.payment-settings input:not([type=checkbox]),.payment-settings select{display:block;width:100%;box-sizing:border-box;padding:10px;border:1px solid #d9e3df;border-radius:8px;margin-top:6px}.payment-settings button{border:0;border-radius:8px;padding:10px 14px;margin:5px;background:#0e7490;color:white}.wide{grid-column:1/-1}.currency-options{display:flex;flex-wrap:wrap;gap:10px;max-height:200px;overflow:auto;padding:10px;border:1px solid #e2e8f0}.currency-options label{min-width:150px}@media(max-width:650px){.payment-settings fieldset{grid-template-columns:1fr}}
</style>
