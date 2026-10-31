<script setup lang="ts">
import {computed,ref,watch} from 'vue';
import {t,uiErrorMessage} from '@kuaixie/i18n';
import SupportAgentPanel from '../components/SupportAgentPanel.vue';
import PortalModerationPanel from '../components/PortalModerationPanel.vue';
import {ADMIN_GROUPS,type AdminGroupKey,type AdminTabKey} from '../console/tabs';
import {useAdminContext} from '../stores/context';
const {api,section,stats,statsError,busy,go,loadStats}=useAdminContext();
const rows=ref<Record<string,unknown>[]>([]);
const loadError=ref('');
const loading=ref(false);
const keyword=ref('');
const notice=ref('');
const invoiceNo=ref<Record<string,string>>({});
const catName=ref('');
const inviteCode=ref('');
const inviteNote=ref('');
const bcTitle=ref('');
const bcBody=ref('');
const bcScope=ref<'all'|'buyer'|'carrier'>('all');
const localeCode=ref('');
const localeNative=ref('');
const localeZh=ref('');
const localeSort=ref('100');
const localeFallback=ref('zh');
const recipeMode=ref('flight');
const recipeType=ref('international');
const recipeCountry=ref('*');
const recipeItems=ref('[{"key":"ticket","label":"ticket","field":"ticket_proof_urls","min_count":1,"camera_only":false}]');
const recipeNote=ref('');
const commissions=ref<Record<string,unknown>[]>([]);
const tiers=ref<Record<string,unknown>[]>([]);
const evidenceText=ref('');

const group=computed<AdminGroupKey>(()=>{
  const g=ADMIN_GROUPS.find(x=>x.tabs.some(tb=>tb.key===section.value));
  return g?.key??'core';
});
const tabs=computed(()=>ADMIN_GROUPS.find(g=>g.key===group.value)?.tabs??[]);
const isConsole=computed(()=>ADMIN_GROUPS.some(g=>g.tabs.some(tb=>tb.key===section.value)));
const listKind=computed(()=>{
  const s=section.value;
  if(s==='overview'||s==='support'||s==='distribution'||s==='violations') return '';
  if(s==='broadcast') return 'adminLogs';
  return s as string;
});
function badge(key?:string){if(!key||!stats.value)return 0;return Number((stats.value as Record<string,unknown>)[key]??0)}
function cell(row:Record<string,unknown>,k:string){const v=row[k];if(v==null)return '—';if(typeof v==='object')return JSON.stringify(v);return String(v)}
function ask(title:string,fallback=''){const v=window.prompt(title,fallback);return v==null?null:v.trim()}
function requireNote(){const n=ask(t('admin.console.noteRequired'));if(!n)throw new Error(t('admin.console.noteRequired'));return n}
function requireAmount(key:string){const n=Number(ask(t(key)));if(!Number.isFinite(n)||n===0)throw new Error(t('admin.console.noteRequired'));return n}
async function showEvidence(id:unknown){
  const ev=await api.rpc('get_order_evidence',{orderId:id});
  evidenceText.value=JSON.stringify(ev,null,2);
}
async function run(fn:()=>Promise<unknown>,okKey='admin.console.actOk'){
  notice.value='';
  try{
    const r=await fn();
    if(typeof r==='string'&&r.startsWith('ERR')) throw new Error(r);
    notice.value=t(okKey)+(typeof r==='string'&&r.startsWith('OK')?' '+r:'');
    await refresh();
    await loadStats();
  }catch(e){notice.value=uiErrorMessage(e)}
}
async function refresh(){
  loadError.value='';
  const kind=listKind.value;
  if(section.value==='overview'){await loadStats();return}
  if(section.value==='distribution'){
    loading.value=true;
    try{
      commissions.value=await api.rpc('admin_commission_rows',{limit:80}) as Record<string,unknown>[];
      tiers.value=await api.rpc('admin_invite_tier_distribution',{}) as Record<string,unknown>[];
    }catch(e){loadError.value=uiErrorMessage(e)}finally{loading.value=false}
    return;
  }
  if(section.value==='violations'){
    loading.value=true;
    try{rows.value=await api.rpc('admin_list_violations',{limit:80}) as Record<string,unknown>[]}
    catch(e){loadError.value=uiErrorMessage(e);rows.value=[]}finally{loading.value=false}
    return;
  }
  if(!kind){rows.value=[];return}
  loading.value=true;
  try{rows.value=await api.rpc('admin_console_list',{kind,limit:80,keyword:keyword.value}) as Record<string,unknown>[]}
  catch(e){loadError.value=uiErrorMessage(e);rows.value=[]}finally{loading.value=false}
}
watch(()=>section.value,()=>{keyword.value='';notice.value='';void refresh()},{immediate:true});
const todoJump:[string,AdminTabKey][]=[
  ['todo_carrier_kyc','carrierKyc'],['todo_buyer_kyc','buyerKyc'],['todo_trips','trips'],
  ['todo_disputes','disputes'],['todo_cancels','cancels'],['todo_pools','pools'],['todo_demands','demands'],
  ['todo_support','support'],['todo_invoices','invoices'],['todo_violations','violations'],['risk_orders','orders'],
];
const moneyKeys=['gmv_total','gmv_week','service_fee_mtd','commission_mtd'];
const countKeys=['users_total','users_week','carriers_approved','buyers_verified','trips_active','orders_open','orders_done'];
function metric(k:string){return t('v15.adminMetric.'+k)}
function scopeLabel(s:string){return t('admin.console.scope.'+s)}
</script>
<template>
 <div v-if="isConsole" class="console-view">
  <div class="console-tabs">
    <button v-for="tb in tabs" :key="tb.key" :class="{on:section===tb.key}" @click="go(tb.key)">
      {{ $t(tb.labelKey) }}
      <em v-if="badge(tb.badge)">{{ badge(tb.badge) }}</em>
    </button>
  </div>
  <p v-if="notice" class="console-notice">{{ notice }}</p>
  <pre v-if="evidenceText" class="row-card">{{ evidenceText }}</pre>
  <p v-if="loadError" class="console-error">{{ loadError }}</p>
  <p v-if="loading" class="console-muted">{{ $t('admin.console.loading') }}</p>

  <template v-if="section==='overview'">
    <p v-if="statsError" class="console-error">{{ statsError }}</p>
    <div v-if="stats" class="stat-grid">
      <div v-for="k in countKeys" :key="k" class="stat-card"><span>{{ metric(k) }}</span><strong>{{ stats[k] ?? 0 }}</strong></div>
    </div>
    <div v-if="stats" class="stat-grid">
      <div v-for="k in moneyKeys" :key="k" class="stat-card"><span>{{ metric(k) }}</span><strong>{{ stats[k] ?? 0 }}</strong></div>
    </div>
    <section class="surface">
      <div class="surface-title"><div><h2>{{ $t('admin.console.todo') }}</h2></div><button class="soft-button" :disabled="busy" @click="loadStats">{{ $t('admin.console.refresh') }}</button></div>
      <button v-for="[k,tab] in todoJump" :key="k" class="todo-row" @click="go(tab)"><span>{{ metric(k) }}</span><b>{{ badge(k) }}</b></button>
    </section>
  </template>

  <SupportAgentPanel v-else-if="section==='support'"/>

  <template v-else-if="section==='distribution'">
    <div class="stat-grid">
      <div v-for="row in tiers" :key="String(row.tier)" class="stat-card"><span>{{ row.tier }}</span><strong>{{ row.people ?? 0 }}</strong><small>{{ row.paid_cny }}</small></div>
    </div>
    <article v-for="(row,i) in commissions" :key="i" class="row-card"><pre>{{ JSON.stringify(row) }}</pre></article>
    <p v-if="!commissions.length && !loading" class="console-muted">{{ $t('admin.console.empty') }}</p>
  </template>

  <template v-else>
    <div class="console-toolbar">
      <button class="soft-button" :disabled="loading" @click="refresh">{{ $t('admin.console.refresh') }}</button>
      <input v-if="section==='users'" v-model="keyword" :placeholder="$t('admin.console.search')" @keydown.enter="refresh"/>
    </div>
    <p v-if="section==='withdrawals'" class="console-warn">{{ $t('admin.console.withdrawReadonly') }}</p>
    <p v-if="section==='channelConfigs'" class="console-warn">{{ $t('admin.console.channelSecretOmit') }}</p>
    <PortalModerationPanel v-if="section==='community'"/>

    <section v-if="section==='categories'" class="surface form-card">
      <input v-model="catName" :placeholder="$t('admin.console.catName')"/>
      <button class="primary-button" @click="run(()=>api.rpc('admin_create_category',{name:catName,parentId:null,level:1}))">{{ $t('admin.console.createCategory') }}</button>
    </section>
    <section v-if="section==='invite'" class="surface form-card">
      <input v-model="inviteCode" :placeholder="$t('admin.console.inviteCode')"/>
      <input v-model="inviteNote" :placeholder="$t('admin.console.inviteNote')"/>
      <button class="primary-button" @click="run(()=>api.rpc('admin_create_invite',{code:inviteCode,note:inviteNote}))">{{ $t('admin.console.createInvite') }}</button>
    </section>
    <section v-if="section==='broadcast'" class="surface form-card">
      <input v-model="bcTitle" maxlength="40" :placeholder="$t('admin.console.broadcastTitle')"/>
      <textarea v-model="bcBody" maxlength="500" :placeholder="$t('admin.console.broadcastBody')"/>
      <div class="chip-row">
        <button v-for="s in ['all','buyer','carrier']" :key="s" :class="{on:bcScope===s}" @click="bcScope=s as typeof bcScope">{{ scopeLabel(s) }}</button>
      </div>
      <button class="primary-button" @click="run(()=>api.rpc('admin_broadcast',{title:bcTitle,body:bcBody,scope:bcScope,userIds:[]}))">{{ $t('admin.console.sendBroadcast') }}</button>
    </section>
    <section v-if="section==='locales'" class="surface form-card">
      <input v-model="localeCode" :placeholder="$t('admin.console.localeCode')"/>
      <input v-model="localeNative" :placeholder="$t('admin.console.localeNative')"/>
      <input v-model="localeZh" :placeholder="$t('admin.console.localeZh')"/>
      <input v-model="localeSort"/>
      <input v-model="localeFallback"/>
      <button class="primary-button" @click="run(()=>api.rpc('admin_locale_upsert_rpc',{code:localeCode,nativeName:localeNative,labelZh:localeZh,sort:Number(localeSort)||100,fallback:localeFallback}))">{{ $t('admin.console.localeSave') }}</button>
    </section>
    <section v-if="section==='proofRecipes'" class="surface form-card">
      <input v-model="recipeType" :placeholder="$t('admin.console.recipeType')"/>
      <input v-model="recipeMode" :placeholder="$t('admin.console.recipeMode')"/>
      <input v-model="recipeCountry"/>
      <textarea v-model="recipeItems"/>
      <input v-model="recipeNote"/>
      <button class="primary-button" @click="run(()=>api.rpc('admin_upsert_recipe',{id:'',tripType:recipeType,transportMode:recipeMode,countryCode:recipeCountry,items:recipeItems,note:recipeNote}))">{{ $t('admin.console.recipeSave') }}</button>
    </section>
    <section v-if="section==='privacyPool'" class="surface form-card">
      <button class="primary-button" @click="run(()=>api.rpc('admin_release_privacy',{}))">{{ $t('admin.console.releasePrivacy') }}</button>
    </section>

    <p v-if="!rows.length && !loading" class="console-muted">{{ $t('admin.console.empty') }}</p>
    <article v-for="row in rows" :key="String(row.id??row.code??row.virtual_number??JSON.stringify(row))" class="row-card">
      <div class="kv-grid">
        <div v-for="(v,k) in row" :key="String(k)" class="kv"><span>{{ k }}</span><b>{{ cell(row,String(k)) }}</b></div>
      </div>
      <div class="act-row">
        <template v-if="section==='carrierKyc' && row.carrier_verify_status==='pending'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_set_carrier_verify',{userId:row.id,approve:true,note:''}))">{{ $t('admin.console.approve') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_set_carrier_verify',{userId:row.id,approve:false,note:requireNote()}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <template v-if="section==='buyerKyc' && row.buyer_verify_status==='pending'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_review_buyer_kyc',{userId:row.id,approve:true,reason:''}))">{{ $t('admin.console.approve') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_review_buyer_kyc',{userId:row.id,approve:false,reason:requireNote()}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <template v-if="section==='enterpriseKyc' && row.enterprise_verify_status==='pending'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_review_enterprise',{userId:row.id,approve:true,note:requireNote()}))">{{ $t('admin.console.approve') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_review_enterprise',{userId:row.id,approve:false,note:requireNote()}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <template v-if="section==='trips' && row.verify_status==='pending'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_set_trip_verify',{tripId:row.id,approve:true,note:''}))">{{ $t('admin.console.approve') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_set_trip_verify',{tripId:row.id,approve:false,note:t('admin.console.reject')}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <button v-if="section==='orders'" class="soft-button" @click="run(()=>showEvidence(row.id))">{{ $t('admin.console.reviewChain') }}</button>
        <button v-if="section==='products' && row.status==='on'" class="danger-button" @click="run(()=>api.rpc('admin_force_off_shelf',{id:row.id}))">{{ $t('admin.console.forceOff') }}</button>
        <button v-if="section==='categories' && row.active" class="danger-button" @click="run(()=>api.rpc('admin_deactivate_category',{id:row.id}))">{{ $t('admin.console.deactivate') }}</button>
        <button v-if="section==='proofRecipes'" class="soft-button" @click="run(()=>api.rpc('admin_toggle_recipe',{id:row.id,active:!row.active}))">{{ $t('admin.console.toggleRecipe') }}</button>
        <template v-if="section==='cancels' && row.status==='pending'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_resolve_cancel',{orderId:row.order_id,approve:true,reason:requireNote()}))">{{ $t('admin.console.approve') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_resolve_cancel',{orderId:row.order_id,approve:false,reason:requireNote()}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <template v-if="section==='invoices' && row.status==='pending'">
          <input v-model="invoiceNo[String(row.id)]" :placeholder="$t('admin.console.invoiceNo')"/>
          <button class="primary-button" @click="run(()=>api.rpc('admin_issue_invoice',{invoiceId:row.id,approve:true,invoiceNo:invoiceNo[String(row.id)],fileUrl:'',reason:''}))">{{ $t('admin.console.issueInvoice') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_issue_invoice',{invoiceId:row.id,approve:false,invoiceNo:'',fileUrl:'',reason:t('admin.console.reject')}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <button v-if="section==='violations' && !row.reviewed" class="primary-button" @click="run(()=>api.rpc('admin_review_violation',{id:row.id}))">{{ $t('admin.console.markReviewed') }}</button>
        <template v-if="section==='pools' && row.pool_status!=='formed'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_force_form_pool',{tripId:row.id}))">{{ $t('admin.console.forceForm') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_dissolve_pool',{tripId:row.id,reason:requireNote()}))">{{ $t('admin.console.dissolve') }}</button>
        </template>
        <button v-if="section==='demands' && (row.status==='open'||row.status==='awarded_pending')" class="danger-button" @click="run(()=>api.rpc('admin_close_demand',{demandId:row.id,reason:requireNote()}))">{{ $t('admin.console.closeDemand') }}</button>
        <button v-if="section==='community' && row.status!=='removed'" class="danger-button" @click="run(()=>api.rpc('admin_remove_post',{postId:row.id,reason:requireNote()}))">{{ $t('admin.console.removePost') }}</button>
        <button v-if="section==='errorLogs' && row.status!=='resolved'" class="soft-button" @click="run(()=>api.rpc('admin_resolve_error',{id:row.id}))">{{ $t('admin.console.resolveLog') }}</button>
        <button v-if="section==='channelConfigs'" class="soft-button" @click="run(()=>api.rpc('admin_toggle_channel',{id:row.id,enabled:!row.enabled}))">{{ $t('admin.console.toggleChannel') }}</button>
        <template v-if="section==='locales'">
          <button class="soft-button" @click="run(()=>api.rpc('admin_locale_set_active_rpc',{code:row.code,active:!row.active,note:t('admin.console.localeToggle')}))">{{ $t('admin.console.localeToggle') }}</button>
          <button v-if="row.code!=='zh' && row.code!=='en'" class="danger-button" @click="run(()=>api.rpc('admin_locale_remove_rpc',{code:row.code,note:t('admin.console.localeRemove')}))">{{ $t('admin.console.localeRemove') }}</button>
        </template>
        <template v-if="section==='users'">
          <button class="danger-button" @click="run(()=>api.rpc('admin_ban_user',{userId:row.id,ban:!row.banned_until,reason:row.banned_until?t('admin.console.unban'):requireNote()}))">{{ row.banned_until ? $t('admin.console.unban') : $t('admin.console.ban') }}</button>
          <button class="soft-button" @click="run(()=>api.rpc('admin_adjust_balance',{userId:row.id,delta:requireAmount('admin.console.adjustBalance'),note:requireNote()}))">{{ $t('admin.console.adjustBalance') }}</button>
          <button class="soft-button" @click="run(()=>api.rpc('admin_fix_score',{userId:row.id,delta:requireAmount('admin.console.fixScore'),note:requireNote()}))">{{ $t('admin.console.fixScore') }}</button>
          <button class="soft-button" @click="run(()=>api.rpc('admin_grant_points',{userId:row.id,points:requireAmount('admin.console.grantPoints'),note:requireNote()}))">{{ $t('admin.console.grantPoints') }}</button>
        </template>
      </div>
    </article>
  </template>
 </div>
</template>
