<script setup lang="ts">
import {computed,ref,watch,onBeforeUnmount} from 'vue';
import {getBearer,subscribeSession} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';
import SupportAgentPanel from '../components/SupportAgentPanel.vue';
import PortalModerationPanel from '../components/PortalModerationPanel.vue';
import {ADMIN_GROUPS} from '../console/tabs';
import {useAdminContext} from '../stores/context';
const {api,section,stats,statsError,busy,loadStats,sessionScope}=useAdminContext();
const rows=ref<Record<string,unknown>[]>([]);
const loadError=ref('');
const loading=ref(false);
const keyword=ref('');
const notice=ref('');
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
const openKey=ref('');
const pageSize=80,pageOffset=ref(0),hasNextPage=ref(false),filterPending=ref(false);
const paged=computed(()=>!!listKind.value);
const pageNumber=computed(()=>Math.floor(pageOffset.value/pageSize)+1);

const isConsole=computed(()=>ADMIN_GROUPS.some(g=>g.tabs.some(tb=>tb.key===section.value)));
const listKind=computed(()=>{
  const s=section.value;
  if(s==='overview'||s==='support'||s==='distribution'||s==='violations') return '';
  if(s==='broadcast') return 'adminLogs';
  return s as string;
});
function cell(row:Record<string,unknown>,k:string){const v=row[k];if(v==null)return '—';if(typeof v==='object')return JSON.stringify(v);return String(v)}
function ask(title:string,fallback=''){const v=window.prompt(title,fallback);return v==null?null:v.trim()}
function requireNote(){const n=ask(t('admin.console.noteRequired'));if(!n)throw new Error(t('admin.console.noteRequired'));return n}
function requireAmount(key:string){const n=Number(ask(t(key)));if(!Number.isFinite(n)||n===0)throw new Error(t('admin.console.noteRequired'));return n}
const mutationBusy=ref(false);let alive=true,listVersion=0,evidenceVersion=0;
function capture(){return {token:getBearer(),session:sessionScope.value,section:section.value}}
function current(owner:ReturnType<typeof capture>){return alive&&owner.token===getBearer()&&owner.session===sessionScope.value&&owner.section===section.value}
function clearPrivate(){listVersion++;evidenceVersion++;rows.value=[];commissions.value=[];tiers.value=[];evidenceText.value='';notice.value='';loadError.value='';loading.value=false;keyword.value='';pageOffset.value=0;hasNextPage.value=false;filterPending.value=false;openKey.value='';bcTitle.value='';bcBody.value='';inviteNote.value='';recipeNote.value=''}
const unsubscribe=subscribeSession(clearPrivate);
onBeforeUnmount(()=>{alive=false;unsubscribe();clearPrivate()});
async function showEvidence(id:unknown){const owner=capture(),version=++evidenceVersion;const ev=await api.rpc('get_order_evidence',{orderId:id});if(current(owner)&&version===evidenceVersion)evidenceText.value=JSON.stringify(ev,null,2)}
async function run(fn:()=>Promise<unknown>,okKey='admin.console.actOk'){
 if(!alive||mutationBusy.value||!getBearer())return;
 const owner=capture();mutationBusy.value=true;notice.value='';
 try{const result=await fn();if(!current(owner))return;if(typeof result==='string'&&result.startsWith('ERR'))throw new Error(result);notice.value=t(okKey)+(typeof result==='string'&&result.startsWith('OK')?' '+result:'');await refresh();if(current(owner))await loadStats()}
 catch(e){if(current(owner))notice.value=uiErrorMessage(e)}finally{if(alive)mutationBusy.value=false}
}
async function refresh(){
 if(!alive||!getBearer())return;const owner=capture(),version=++listVersion,kind=listKind.value,offset=pageOffset.value,query=keyword.value;loadError.value='';filterPending.value=false;
 if(owner.section==='overview'){await loadStats();return}
 if(!kind&&!['distribution','violations'].includes(owner.section)){rows.value=[];return}
 loading.value=true;
 try{
  if(owner.section==='distribution'){const [entries,levels]=await Promise.all([api.rpc('admin_commission_rows',{limit:80}),api.rpc('admin_invite_tier_distribution',{})]);if(!current(owner)||version!==listVersion)return;commissions.value=entries as Record<string,unknown>[];tiers.value=levels as Record<string,unknown>[]}
  else{const result=owner.section==='violations'?await api.rpc('admin_list_violations',{limit:80}):await api.rpc('admin_console_list',{kind,limit:pageSize+1,offset,keyword:query});if(!current(owner)||version!==listVersion||offset!==pageOffset.value||query!==keyword.value)return;const values=result as Record<string,unknown>[];hasNextPage.value=!!kind&&values.length>pageSize;rows.value=kind?values.slice(0,pageSize):values}
 }catch(e){if(current(owner)&&version===listVersion){loadError.value=uiErrorMessage(e);rows.value=[];hasNextPage.value=false}}finally{if(current(owner)&&version===listVersion)loading.value=false}
}
async function changePage(direction:-1|1){
 if(!paged.value||loading.value||mutationBusy.value||direction===1&&!hasNextPage.value||direction===-1&&pageOffset.value===0)return;
 pageOffset.value=Math.max(0,pageOffset.value+direction*pageSize);rows.value=[];evidenceText.value='';openKey.value='';await refresh();
}
watch(keyword,()=>{if(section.value!=='users')return;listVersion++;pageOffset.value=0;hasNextPage.value=false;rows.value=[];evidenceText.value='';openKey.value='';loading.value=false;filterPending.value=true},{flush:'sync'});
watch(()=>section.value,()=>{clearPrivate();void refresh()},{immediate:true});
const moneyKeys=['gmv_total','gmv_week','service_fee_mtd','commission_mtd'];
const countKeys=['users_total','users_week','carriers_approved','buyers_verified','trips_active','orders_open','orders_done'];
function metric(k:string){return t(k==='todo_products'?'admin.metric.pendingProducts':'v15.adminMetric.'+k)}
function scopeLabel(s:string){return t('admin.console.scope.'+s)}
const COLUMNS:Record<string,string[]>={
  carrierKyc:['nickname','phone','carrier_verify_status','city'],
  buyerKyc:['nickname','phone','buyer_verify_status','city'],
  enterpriseKyc:['nickname','enterprise_name','enterprise_verify_status'],
  trips:['origin_city','destination','depart_date','verify_status','visibility'],
  orders:['order_no','item_name','status','goods_amount'],
  cancels:['order_id','status','reason_detail'],
  disputes:['order_id','stage','type'],
  invoices:['status','amount','title_name'],
  demands:['title','city','visibility','status','expect_price'],
  users:['nickname','phone','user_role','banned_until'],
  products:['name','region','stock','status','price'],
  categories:['name','level','active'],
  community:['title','status','kind'],
  pools:['destination','pool_status','depart_date'],
  withdrawals:['user_id','amount','status'],
  errorLogs:['scope','message','status','created_at'],
  channelConfigs:['channel_type','provider','enabled'],
  locales:['code','native_name','active'],
  proofRecipes:['trip_type','transport_mode','active'],
  finance:['kind','amount','created_at'],
  notifyQueue:['kind','title','created_at'],
  privacyPool:['virtual_number','status'],
  invite:['code','note','used_by'],
  broadcast:['title','created_at'],
  violations:['kind','reviewed','created_at'],
};
function rowKey(row:Record<string,unknown>){return String(row.id??row.code??row.virtual_number??JSON.stringify(row))}
function cols(row:Record<string,unknown>){
  const pref=COLUMNS[String(section.value)]||[];
  const have=pref.filter(k=>k in row);
  return have.length?have:Object.keys(row).filter(k=>k!=='id').slice(0,4);
}
function toggle(row:Record<string,unknown>){const k=rowKey(row);openKey.value=openKey.value===k?'':k}
</script>
<template>
 <div v-if="isConsole" class="console-view" :aria-busy="mutationBusy">
 <fieldset class="console-action-scope" :disabled="mutationBusy">
  <p v-if="notice" class="console-notice">{{ notice }}</p>
  <pre v-if="evidenceText" class="row-card">{{ evidenceText }}</pre>
  <p v-if="loadError" class="console-error">{{ loadError }}</p>
  <p v-if="loading" class="console-muted">{{ $t('admin.console.loading') }}</p>

  <template v-if="section==='overview'">
    <p v-if="statsError" class="console-error">{{ statsError }}</p>
    <div v-if="stats" class="stat-grid">
      <div v-for="k in countKeys" :key="k" class="stat-card"><span>{{ metric(k) }}</span><strong>{{ stats[k] ?? '—' }}</strong></div>
    </div>
    <div v-if="stats" class="stat-grid">
      <div v-for="k in moneyKeys" :key="k" class="stat-card"><span>{{ metric(k) }}</span><strong>{{ stats[k] ?? '—' }}</strong></div>
    </div>
    <button class="soft-button" :disabled="busy" @click="loadStats">{{ $t('admin.console.refresh') }}</button>
  </template>

  <SupportAgentPanel v-else-if="section==='support'"/>

  <template v-else-if="section==='distribution'">
    <p class="console-muted">{{$t('admin.pagination.specializedLimit')}}</p>
    <div class="stat-grid">
      <div v-for="row in tiers" :key="String(row.tier)" class="stat-card"><span>{{ row.tier }}</span><strong>{{ row.people ?? 0 }}</strong><small>{{ row.paid_cny }}</small></div>
    </div>
    <article v-for="(row,i) in commissions" :key="i" class="row-card"><pre>{{ JSON.stringify(row) }}</pre></article>
    <p v-if="!commissions.length && !loading" class="console-muted">{{ $t('admin.console.empty') }}</p>
  </template>

  <template v-else>
    <div class="console-toolbar">
      <button class="soft-button" :disabled="loading" @click="refresh">{{ $t('admin.console.refresh') }}</button>
      <input v-if="section==='users'" v-model="keyword" :placeholder="$t('admin.console.search')" @keydown.enter="refresh"/><button v-if="section==='users'" class="soft-button" :disabled="loading" @click="refresh">{{$t('admin.pagination.search')}}</button>
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

    <p v-if="filterPending" class="console-muted">{{$t('admin.pagination.filterPending')}}</p>
    <p v-if="!rows.length && !loading && !loadError && !filterPending" class="console-muted">{{ $t('admin.console.empty') }}</p>
    <nav v-if="paged" class="console-pagination" :aria-label="$t('admin.pagination.label')">
      <button type="button" class="soft-button" :disabled="loading||mutationBusy||pageOffset===0||filterPending" @click="changePage(-1)">{{$t('admin.pagination.previous')}}</button>
      <span>{{$t('admin.pagination.page',{page:pageNumber})}} · {{$t('admin.pagination.rows',{count:rows.length})}}</span>
      <button type="button" class="soft-button" :disabled="loading||mutationBusy||!hasNextPage||filterPending" @click="changePage(1)">{{$t('admin.pagination.next')}}</button>
    </nav>
    <p v-else-if="section==='violations'" class="console-muted">{{$t('admin.pagination.specializedLimit')}}</p>
    <table v-if="rows.length" class="desk-table">
      <thead>
        <tr>
          <th v-for="k in cols(rows[0])" :key="k">{{ k }}</th>
          <th>{{ $t('admin.desk.cases') }}</th>
        </tr>
      </thead>
      <tbody>
        <template v-for="row in rows" :key="rowKey(row)">
          <tr :class="{'case-open':openKey===rowKey(row)}" @click="toggle(row)">
            <td v-for="k in cols(rows[0])" :key="k">{{ cell(row,k) }}</td>
            <td>
              <button class="soft-button" type="button">{{ openKey===rowKey(row)?$t('admin.desk.close'):$t('admin.desk.open') }}</button>
            </td>
          </tr>
          <tr v-if="openKey===rowKey(row)">
            <td :colspan="cols(rows[0]).length+1" class="case-actions" @click.stop>
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
        <p v-if="section==='products' && row.status==='pending'" class="console-warn">{{ $t('admin.console.productReviewUnavailable') }}</p>
        <button v-if="section==='products' && row.status==='on'" class="danger-button" @click="run(()=>api.rpc('admin_force_off_shelf',{id:row.id}))">{{ $t('admin.console.forceOff') }}</button>
        <button v-if="section==='categories' && row.active" class="danger-button" @click="run(()=>api.rpc('admin_deactivate_category',{id:row.id}))">{{ $t('admin.console.deactivate') }}</button>
        <button v-if="section==='proofRecipes'" class="soft-button" @click="run(()=>api.rpc('admin_toggle_recipe',{id:row.id,active:!row.active}))">{{ $t('admin.console.toggleRecipe') }}</button>
        <template v-if="section==='cancels' && row.status==='pending'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_resolve_cancel',{orderId:row.order_id,approve:true,reason:requireNote()}))">{{ $t('admin.console.approve') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_resolve_cancel',{orderId:row.order_id,approve:false,reason:requireNote()}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <template v-if="section==='invoices' && row.status==='pending'">

          <p class="console-warn">{{ $t('admin.console.invoiceProviderUnavailable') }}</p>
          <button class="danger-button" @click="run(()=>api.rpc('admin_issue_invoice',{invoiceId:row.id,approve:false,invoiceNo:'',fileUrl:'',reason:t('admin.console.reject')}))">{{ $t('admin.console.reject') }}</button>
        </template>
        <button v-if="section==='violations' && !row.reviewed" class="primary-button" @click="run(()=>api.rpc('admin_review_violation',{id:row.id}))">{{ $t('admin.console.markReviewed') }}</button>
        <template v-if="section==='pools' && row.pool_status!=='formed'">
          <button class="primary-button" @click="run(()=>api.rpc('admin_force_form_pool',{tripId:row.id}))">{{ $t('admin.console.forceForm') }}</button>
          <button class="danger-button" @click="run(()=>api.rpc('admin_dissolve_pool',{tripId:row.id,reason:requireNote()}))">{{ $t('admin.console.dissolve') }}</button>
        </template>
        <template v-if="section==='demands' && row.status==='open' && row.visibility==='hidden'">
          <button class="primary-button" @click="run(()=>api.portal.adminReviewDemand(String(row.id),true))">{{ $t('admin.console.approve') }}</button>
          <button class="danger-button" @click="run(()=>api.portal.adminReviewDemand(String(row.id),false))">{{ $t('admin.console.reject') }}</button>
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
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </template>
 </fieldset>
 </div>
</template>

<style scoped>
.console-action-scope{border:0;padding:0;margin:0;min-width:0}
.console-pagination{display:flex;align-items:center;gap:12px;flex-wrap:wrap;margin:12px 0}
</style>
