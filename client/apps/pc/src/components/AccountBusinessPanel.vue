<script setup lang="ts">
import {onMounted,ref,watch} from 'vue';
import type {ApiClient,BackendRecord,MyProfile,MyWallet} from '@kuaixie/sdk';
import {clearBearer} from '@kuaixie/sdk';
import {formatAmount,formatBusinessStatus,formatDate,uiErrorMessage} from '@kuaixie/i18n';
import InsuranceApplication from '@kuaixie/ui/InsuranceApplication.vue';
import WalletFunding from '@kuaixie/ui/WalletFunding.vue';
const props=defineProps<{client:ApiClient;kind?:string}>();
const emit=defineEmits<{open:[id:string];signedOut:[]}>();
const profile=ref<MyProfile|null>(null),wallet=ref<MyWallet|null>(null),rows=ref<BackendRecord[]>([]),busy=ref(false),error=ref(''),more=ref(false),nickname=ref(''),city=ref(''),selected=ref<BackendRecord|null>(null);
let generation=0;
async function load(append=false){
 const request=++generation;busy.value=true;error.value='';selected.value=null;
 try{
  const offset=append?rows.value.length:0;let data:unknown[]=[];
  if(props.kind==='my-orders')data=await props.client.account.orders('all',20,offset);
  else if(props.kind==='my-demands')data=await props.client.account.demands(20,offset);
  else if(props.kind==='my-trips')data=await props.client.account.trips(20,offset);
  else if(props.kind==='my-notifications')data=await props.client.account.notifications(20,offset);
  else if(props.kind==='wallet-ledger')data=await props.client.account.ledger(20,offset);
  else {const [p,w]=await Promise.all([props.client.account.profile(),props.client.account.wallet()]);if(request!==generation)return;profile.value=p;wallet.value=w;nickname.value=p.nickname||'';city.value=p.city||'';}
  if(request!==generation)return;rows.value=append?[...rows.value,...data as BackendRecord[]]:data as BackendRecord[];more.value=data.length===20;
 }catch(e){if(request===generation){error.value=uiErrorMessage(e);if(!append){rows.value=[];profile.value=null;wallet.value=null}}}finally{if(request===generation)busy.value=false}
}
async function save(){busy.value=true;error.value='';try{profile.value=await props.client.account.updateProfile({nickname:nickname.value,city:city.value})}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
async function select(row:BackendRecord){busy.value=true;error.value='';try{if(props.kind==='my-orders')selected.value=await props.client.account.order(String(row.id));else if(props.kind==='my-notifications'){await props.client.account.markNotificationRead(String(row.id));row.read_flag=true}}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
function signOut(){clearBearer();profile.value=null;wallet.value=null;rows.value=[];emit('signedOut')}
watch(()=>props.kind,()=>{void load()});onMounted(()=>{void load()});
</script>
<template><section class="business-card"><p v-if="error" role="alert">{{error}}</p><button :disabled="busy" @click="load(false)">{{$t('common.retry')}}</button>
 <template v-if="profile"><h2>{{profile.nickname||$t('account.title')}}</h2><p>{{profile.user_role}} · {{formatBusinessStatus(profile.carrier_verify_status||'none')}}</p><form @submit.prevent="save"><label>{{$t('account.nickname')}}<input v-model="nickname" maxlength="64" required/></label><label>{{$t('account.city')}}<input v-model="city" maxlength="64"/></label><button :disabled="busy">{{$t('account.save')}}</button></form><button @click="signOut">{{$t('mine.signOut')}}</button></template>
 <template v-if="wallet"><h2>{{$t('account.balance')}} · {{formatAmount(wallet.balance_amount,wallet.currency)}}</h2><p>{{$t('account.points')}} · {{wallet.points??0}}</p><button @click="emit('open','wallet-ledger')">{{$t('account.ledger')}}</button><WalletFunding :client="client" @paid="load(false)"/></template>
 <article v-for="row in rows" :key="String(row.id)" class="business-item"><h3>{{row.item_name||row.title||row.destination||row.kind}}</h3><p>{{row.body||row.note||row.item_desc||''}}</p><p>{{formatBusinessStatus(String(row.status||row.verify_status||''))}} · {{formatDate(String(row.created_at||''))}}</p><p v-if="row.goods_amount!=null||row.amount!=null">{{formatAmount(String(row.goods_amount??row.amount),'CNY')}}</p><button v-if="kind==='my-orders'||kind==='my-notifications'" :disabled="busy" @click="select(row)">{{$t(kind==='my-orders'?'account.details':'account.markRead')}}</button></article>
 <section v-if="selected" class="business-card"><InsuranceApplication v-if="selected.insurance_quote_id&&selected.my_role==='buyer'" :client="client" :order-id="String(selected.id)"/><h2>{{selected.item_name}}</h2><p>{{formatBusinessStatus(String(selected.status))}} · {{selected.order_no}}</p><p>{{formatAmount(String(selected.goods_amount),'CNY')}}</p><nav class="business-tabs"><button v-if="selected.my_role==='buyer'&&['purchased','shipped','delivered','completed'].includes(String(selected.status))" @click="emit('open','my-disputes?orderId='+encodeURIComponent(String(selected.id)))">{{$t('case.title')}}</button><button @click="emit('open','order-actions?orderId='+encodeURIComponent(String(selected.id)))">{{$t('fast.flow.order_actions')}}</button><button @click="emit('open','shipping-fee?orderId='+encodeURIComponent(String(selected.id)))">{{$t('fast.flow.shipping_fee')}}</button><button @click="emit('open','cancel-request?orderId='+encodeURIComponent(String(selected.id)))">{{$t('fast.flow.cancel_request')}}</button></nav></section>
 <button v-if="more" :disabled="busy" @click="load(true)">{{$t('account.more')}}</button><p v-if="!busy&&!error&&!rows.length&&!profile&&!wallet">{{$t('account.empty')}}</p></section></template>
