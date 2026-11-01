<script setup lang="ts">
import {onBeforeUnmount,ref} from 'vue';
import {getBearer,subscribeSession,type ApiClient} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';
const props=defineProps<{client:ApiClient;kind:'demand'|'trip'|'life'}>();
const emit=defineEmits<{published:[];login:[]}>();
const initial=getBearer();let active=true;const busy=ref(false),notice=ref('');
const draft=ref({title:'',description:'',city:'',expectPrice:'',originCity:'',destination:'',departDate:'',returnDate:'',capacityQty:'1',transportMode:'air' as 'road'|'rail'|'air'|'other',tripType:'international' as 'domestic'|'international',caption:''});
const current=()=>active&&getBearer()===initial;
function clear(){draft.value={title:'',description:'',city:'',expectPrice:'',originCity:'',destination:'',departDate:'',returnDate:'',capacityQty:'1',transportMode:'air',tripType:'international',caption:''};notice.value=''}
const unsubscribe=subscribeSession(()=>{if(!current()){active=false;clear()}});onBeforeUnmount(()=>{active=false;unsubscribe();clear()});
async function submit(){if(!current()||busy.value)return;if(!getBearer()){emit('login');notice.value=t('portal.publish.login');return}const d={...draft.value};busy.value=true;notice.value='';try{
 if(props.kind==='demand'){
  if(!d.title.trim()||(d.expectPrice.trim()&&!/^\d{1,10}(\.\d{1,2})?$/.test(d.expectPrice.trim())))throw new Error(t('portal.publish.needFields'));
  const result=await props.client.portal.publishDemand({title:d.title.trim(),description:d.description.trim(),city:d.city.trim(),expectPrice:d.expectPrice.trim()||null,tripType:d.tripType});
  if(!current())return;clear();notice.value=t(result.status==='open'?'portal.publish.success':'portal.publish.review');
 }else if(props.kind==='trip'){
  const qty=Number(d.capacityQty);if(!d.originCity.trim()||!d.destination.trim()||!d.departDate||!d.returnDate||!Number.isInteger(qty)||qty<1||qty>100)throw new Error(t('portal.publish.needFields'));if(d.returnDate<d.departDate)throw new Error(t('portal.publish.dateError'));
  await props.client.portal.publishTrip({originCity:d.originCity.trim(),destination:d.destination.trim(),departDate:d.departDate,returnDate:d.returnDate,capacityQty:qty,transportMode:d.transportMode,tripType:d.tripType});
  if(!current())return;clear();notice.value=t('portal.publish.review');
 }else{
  if(!d.caption.trim())throw new Error(t('portal.publish.needFields'));await props.client.portal.postCommunity({caption:d.caption.trim(),mediaKind:'text'});if(!current())return;clear();notice.value=t('portal.publish.review');
 }if(current())emit('published');
 }catch(e){if(current())notice.value=uiErrorMessage(e)}finally{if(current())busy.value=false}}
</script>
<template><form class="hall-form" @submit.prevent="submit"><h3>{{t(kind==='trip'?'portal.publish.trip':kind==='demand'?'portal.publish.demand':'portal.publish.life')}}</h3><p>{{t('portal.publish.review')}}</p><fieldset :disabled="busy"><template v-if="kind==='demand'"><label>{{t('portal.publish.demandTitle')}}<input v-model="draft.title" maxlength="200" required/></label><label>{{t('portal.publish.demandDescription')}}<textarea v-model="draft.description" maxlength="4000" rows="3"/></label><label>{{t('portal.publish.city')}}<input v-model="draft.city" maxlength="64"/></label><label>{{t('portal.publish.budget')}}<input v-model="draft.expectPrice" inputmode="decimal"/></label></template><template v-else-if="kind==='trip'"><label>{{t('portal.publish.origin')}}<input v-model="draft.originCity" maxlength="64" required/></label><label>{{t('portal.publish.destination')}}<input v-model="draft.destination" maxlength="128" required/></label><label>{{t('portal.publish.depart')}}<input v-model="draft.departDate" type="date" required/></label><label>{{t('portal.publish.return')}}<input v-model="draft.returnDate" type="date" required/></label><label>{{t('portal.publish.capacity')}}<input v-model="draft.capacityQty" type="number" min="1" max="100"/></label><label>{{t('portal.publish.transport')}}<select v-model="draft.transportMode"><option value="air">{{t('portal.publish.transportAir')}}</option><option value="rail">{{t('portal.publish.transportRail')}}</option><option value="road">{{t('portal.publish.transportRoad')}}</option><option value="other">{{t('common.notProvided')}}</option></select></label></template><template v-else><label>{{t('portal.publish.caption')}}<textarea v-model="draft.caption" maxlength="4000" rows="3" required/></label></template><label v-if="kind!=='life'">{{t('portal.publish.tripType')}}<select v-model="draft.tripType"><option value="international">{{t('plaza.reach.international')}}</option><option value="domestic">{{t('plaza.reach.domestic')}}</option></select></label><button class="hall-cta" type="submit">{{t('portal.publish.submit')}}</button></fieldset><p v-if="notice" class="hall-note" role="status">{{notice}}</p></form></template>
<style scoped>fieldset{border:0;padding:0;display:grid;gap:12px}select{border:1px solid #c9b896;background:#fffaf0;padding:10px 11px}</style>
