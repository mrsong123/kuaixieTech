<script setup lang="ts">
import InsuranceSelection from '@kuaixie/ui/InsuranceSelection.vue';
import InsuranceApplication from '@kuaixie/ui/InsuranceApplication.vue';
import {goodsForInsurance} from '@kuaixie/ui/useInsurance';
import AddressSelection from '@kuaixie/ui/AddressSelection.vue';
import {ref,reactive,watch,onBeforeUnmount} from 'vue';
import {ApiError,getBearer,type ApiClient,type OriginalIntentInput,type ConfirmedIntentInput,type IntentQuote} from '@kuaixie/sdk';
import {t,formatAmount,uiErrorMessage} from '@kuaixie/i18n';
const props=defineProps<{client:ApiClient;tripId:string}>();
const emit=defineEmits<{pending:[value:boolean]}>();
const form=reactive({itemName:'',itemSpec:'',quantity:1,expectUnitPrice:'',contactName:'',contactPhone:'',address:'',note:'',insuranceChoice:'' as ''|'yes'|'waived',insuranceQuoteId:undefined as string|undefined});
const busy=ref(false),error=ref(''),done=ref(''),quote=ref<IntentQuote|null>(null),pending=ref<ConfirmedIntentInput|null>(null);
let quoted:OriginalIntentInput|null=null;
watch(()=>form.insuranceChoice,()=>{form.insuranceQuoteId=undefined});
watch(form,()=>{if(!pending.value){quote.value=null;quoted=null}});
watch(()=>busy.value||!!pending.value,value=>emit('pending',value));
function guardUnload(event:BeforeUnloadEvent){if(pending.value){event.preventDefault();event.returnValue=''}}
window.addEventListener('beforeunload',guardUnload);
onBeforeUnmount(()=>window.removeEventListener('beforeunload',guardUnload));
async function preview(){
 if(busy.value||pending.value)return;
 error.value='';quote.value=null;
 if(!getBearer()){error.value=t('v15.loginRequired');return}
 if(!form.insuranceChoice)return;
 if(form.insuranceChoice==='yes'&&!form.insuranceQuoteId){error.value=t('insurance.selectionRequired');return}
 busy.value=true;
 try{
  quoted={tripId:props.tripId,itemName:form.itemName.trim(),itemSpec:form.itemSpec.trim(),quantity:form.quantity,unit:'piece',expectUnitPrice:Number(form.expectUnitPrice),contactName:form.contactName.trim(),contactPhone:form.contactPhone.trim(),address:form.address.trim(),note:form.note.trim(),insuranceChoice:form.insuranceChoice,insuranceQuoteId:form.insuranceChoice==='yes'?form.insuranceQuoteId:undefined};
  const result=await props.client.originalV15.quoteIntent(quoted);
  if(result.pricingVersion!=='escrow_v2_external_insurance'||result.currency!=='CNY'||!Number.isFinite(Number(result.buyerEscrow))||Number(result.buyerEscrow)<=0)throw new Error(t('order.pricingUnavailable'));
  quote.value=result;
 }catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}
}
async function confirm(){
 if(busy.value||done.value)return;
 error.value='';busy.value=true;
 try{
  if(!pending.value){
   if(!quote.value||!quoted)return;
   const key=globalThis.crypto?.randomUUID?.()??'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const n=Math.floor(Math.random()*16);return(c==='x'?n:(n&3)|8).toString(16)});
   pending.value={...quoted,requestId:key,expectedBuyerEscrow:quote.value.buyerEscrow};
  }
  const result=await props.client.originalV15.createIntent(pending.value);
  if(!result?.id)throw new Error(t('v15.loadFail'));
  done.value=result.id;pending.value=null;
 }catch(e){
  error.value=uiErrorMessage(e);
  if(e instanceof ApiError&&[400,403,404,409,422].includes(e.status))pending.value=null;
 }finally{busy.value=false}
}
</script>
<template>
 <section class="intent-form">
  <h3>{{$t('v15.createIntent')}}</h3>
  <p>{{$t('order.escrowNotice')}}</p>
  <form v-if="!done" @submit.prevent="preview">
   <fieldset :disabled="busy||!!pending">
    <label>{{$t('v15.itemName')}}<input v-model="form.itemName" maxlength="120" required/></label>
    <label>{{$t('v15.itemSpec')}}<input v-model="form.itemSpec" maxlength="160"/></label>
    <label>{{$t('v15.quantity')}}<input v-model.number="form.quantity" type="number" min="1" max="100" step="1" required/></label>
    <label>{{$t('v15.unitPrice')}}<input v-model="form.expectUnitPrice" type="number" min="0.01" max="1000000" step="0.01" required/></label>
    <AddressSelection :client="client" @selected="Object.assign(form,$event)"/>
    <label>{{$t('v15.receiver')}}<input v-model="form.contactName" maxlength="64" required/></label>
    <label>{{$t('v15.phoneContact')}}<input v-model="form.contactPhone" maxlength="24" required/></label>
    <label class="full">{{$t('v15.address')}}<textarea v-model="form.address" maxlength="512" required/></label>
    <label class="full">{{$t('v15.note')}}<textarea v-model="form.note" maxlength="1024"/></label>
    <label class="full">{{$t('v15.insurance')}}<select v-model="form.insuranceChoice" required><option disabled value="">{{$t('v15.insurance')}}</option><option value="yes">{{$t('v15.insure')}}</option><option value="waived">{{$t('v15.waive')}}</option></select></label>
    <InsuranceSelection v-if="form.insuranceChoice==='yes'" :client="client" subject-type="trip" :subject-id="tripId" :item-name="form.itemName" :declared-value="goodsForInsurance(form.expectUnitPrice,form.quantity)" @selected="form.insuranceQuoteId=$event?.id"/>
   </fieldset>
   <button class="btn-outline" type="submit" :disabled="busy||!!pending">{{$t('order.previewAction')}}</button>
  </form>
  <dl v-if="quote&&!done"><div><dt>{{$t('v15.amount')}}</dt><dd>{{formatAmount(quote.goodsAmount,quote.currency)}}</dd></div><div><dt>{{$t('order.serviceFee')}}</dt><dd>{{formatAmount(quote.serviceFee,quote.currency)}}</dd></div><div><dt>{{$t('order.insurancePremium')}}</dt><dd>{{formatAmount(quote.insurancePremium,quote.currency)}}</dd></div><div><dt>{{$t('order.buyerEscrow')}}</dt><dd>{{formatAmount(quote.buyerEscrow,quote.currency)}}</dd></div></dl>
  <p v-if="pending">{{$t('order.pendingRetry')}}</p>
  <button v-if="(quote||pending)&&!done" class="btn-green" :disabled="busy" @click="confirm">{{$t(pending?'v15.retry':'order.confirmAction')}}</button>
  <InsuranceApplication v-if="done" :client="client" :order-id="done"/>
  <p v-if="done" role="status">{{$t('v15.intentCreated')}} · {{done}}</p>
  <p v-if="error" class="intent-error" role="alert">{{error}}</p>
 </section>
</template>
<style scoped>
.intent-form{margin-top:24px;border-top:1px solid #dfe8e3;padding-top:20px}.intent-form p{font-size:13px;line-height:1.7}.intent-form fieldset{display:grid;grid-template-columns:1fr 1fr;gap:12px;border:0;padding:0;margin:16px 0}.intent-form label{display:grid;gap:6px;font-size:13px}.intent-form .full{grid-column:1/-1}.intent-form input,.intent-form textarea,.intent-form select{width:100%;box-sizing:border-box;border:1px solid #ccd9d2;border-radius:7px;padding:10px;font:inherit}.intent-form dl>div{display:flex;justify-content:space-between;padding:6px 0}.intent-form dd{margin:0}.intent-error{color:#ad302c}@media(max-width:600px){.intent-form fieldset{grid-template-columns:1fr}}
</style>
