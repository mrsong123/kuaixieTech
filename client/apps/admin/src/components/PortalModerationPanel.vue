<script setup lang="ts">
import {ref} from 'vue';
import {getBearer,type BackendRecord} from '@kuaixie/sdk';
import {t} from '@kuaixie/i18n';
import {createAdminApi} from '../api/client';
const api=createAdminApi();
const queue=ref<{demands:BackendRecord[];trips:BackendRecord[];posts:BackendRecord[]}>({demands:[],trips:[],posts:[]});
const notice=ref(''),busy=ref(false);
const sections=['demands','trips','posts'] as const;
const titles={demands:'portal.review.demands',trips:'portal.review.trips',posts:'portal.review.posts'};
function summary(row:BackendRecord,kind:'demands'|'trips'|'posts'){return String(kind==='demands'?row.title:kind==='trips'?row.destination:row.caption??'').slice(0,120)}
async function refresh(){busy.value=true;notice.value='';try{queue.value=await api.portal.adminReviews()}catch{notice.value='portal.review.error'}finally{busy.value=false}}
async function decide(kind:'demands'|'trips'|'posts',id:string,approved:boolean){
 if(!window.confirm(t('portal.review.confirm')))return;
 busy.value=true;notice.value='';
 try{
  if(kind==='demands')await api.portal.adminReviewDemand(id,approved);
  else if(kind==='trips')await api.portal.adminReviewTrip(id,approved);
  else await api.portal.adminReviewCommunity(id,approved);
  await refresh();notice.value='portal.review.done';
 }catch{notice.value='portal.review.error'}finally{busy.value=false}
}
</script>
<template>
<section class="review-panel"><header><div><h2>{{ $t('portal.review.title') }}</h2><p>{{ $t('portal.review.subtitle') }}</p></div><button :disabled="busy" @click="refresh">{{ $t('portal.review.refresh') }}</button></header>
 <p v-if="notice" class="review-notice">{{ $t(notice) }}</p>
 <div class="review-groups"><div v-for="kind in sections" :key="kind" class="review-group"><h3>{{ $t(titles[kind]) }} <small>{{queue[kind].length}}</small></h3><p v-if="!queue[kind].length" class="review-empty">{{ $t('portal.review.empty') }}</p><div v-for="item in queue[kind]" :key="String(item.id)" class="review-item"><div class="review-copy">{{ summary(item,kind) }}</div><div class="review-actions"><button :disabled="busy" @click="decide(kind,String(item.id),true)">{{ $t('portal.review.approve') }}</button><button :disabled="busy" class="reject" @click="decide(kind,String(item.id),false)">{{ $t('portal.review.reject') }}</button></div></div></div></div>
</section>
</template>
<style scoped>
.review-panel{background:var(--kx-surface,#fff);border:1px solid #e2e8f0;border-radius:20px;padding:24px;margin:0 0 25px}.review-panel header{display:flex;align-items:center;justify-content:space-between;gap:14px}.review-panel h2{font-size:18px;margin:0}.review-panel header p{font-size:12px;color:#64748b;margin:7px 0}.review-panel button{background:#0e7490;color:#fff;border:0;border-radius:9px;padding:9px 13px;font-size:12px;cursor:pointer}.review-groups{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:13px;margin-top:19px}.review-group{border:1px solid #e2e8f0;border-radius:13px;padding:14px;min-width:0}.review-group h3{font-size:14px;margin:0 0 12px}.review-group h3 small{color:#94a3b8}.review-empty,.review-notice{font-size:12px;color:#64748b;line-height:1.6}.review-item{border-top:1px solid #e2e8f0;padding:12px 0}.review-copy{font-size:12px;line-height:1.55;overflow-wrap:anywhere}.review-actions{display:flex;gap:7px;margin-top:10px}.review-actions button.reject{background:#64748b}.review-panel button:disabled{opacity:.5}@media(max-width:900px){.review-groups{grid-template-columns:1fr}}@media(max-width:420px){.review-panel{padding:14px}.review-panel header{flex-wrap:wrap}}
</style>
