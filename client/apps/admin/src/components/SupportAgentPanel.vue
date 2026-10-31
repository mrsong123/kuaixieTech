<script setup lang="ts">
import {ref} from 'vue';
import {getBearer,type SupportMessage,type SupportQueueItem,type SupportConversation} from '@kuaixie/sdk';
import {t} from '@kuaixie/i18n';
import {createAdminApi} from '../api/client';
const client=createAdminApi();
const waiting=ref<SupportQueueItem[]>([]),messages=ref<SupportMessage[]>([]),active=ref<SupportConversation|null>(null),draft=ref('');
const busy=ref(false),notice=ref(''),error=ref(false);
async function reload(){notice.value='';error.value=false;
 if(!getBearer()){notice.value='portal.agent.noToken';return}
 busy.value=true;
 try{waiting.value=await client.support.adminQueue();if(active.value)messages.value=await client.support.agentMessages(active.value.id)}
 catch{error.value=true;notice.value='portal.agent.error'}finally{busy.value=false}
}
async function claim(id:string){notice.value='';busy.value=true;
 try{active.value=await client.support.claim(id);messages.value=await client.support.agentMessages(id);await reload();notice.value='portal.agent.claimed'}
 catch{notice.value='portal.agent.error'}finally{busy.value=false}
}
async function send(){if(!draft.value.trim()||!active.value||busy.value)return;
 busy.value=true;notice.value='';
 try{await client.support.agentReply(active.value.id,draft.value.trim());draft.value='';messages.value=await client.support.agentMessages(active.value.id);notice.value='portal.agent.sent'}
 catch{notice.value='portal.agent.error'}finally{busy.value=false}
}
</script>
<template>
<section class="support-panel">
 <div class="support-heading"><div><h2>{{ $t('portal.agent.title') }}</h2><p>{{ $t('portal.agent.subtitle') }}</p></div><button class="quiet-btn" :disabled="busy" @click="reload">{{ $t('portal.agent.refresh') }}</button></div>
 <p v-if="notice" class="notice" :class="{error}">{{ $t(notice) }}</p>
 <div class="support-columns">
  <div class="support-queue"><h3>{{ $t('portal.agent.queue') }}</h3><p v-if="waiting.length===0" class="empty">{{ $t('portal.agent.empty') }}</p><article v-for="item in waiting" :key="item.id" class="support-item"><div><strong>{{item.id.slice(0,8)}}</strong><small>{{ $t('portal.agent.time') }} · {{ item.created_at }}</small></div><button :disabled="busy" @click="claim(item.id)">{{ $t('portal.agent.claim') }}</button></article></div>
  <div class="support-thread"><template v-if="active"><div class="thread-title">{{active.id.slice(0,8)}} · {{active.status}}</div><div class="thread-messages"><p v-for="msg in messages" :key="msg.id" class="thread-bubble" :class="{'thread-agent':msg.sender_type==='agent'}"><small>{{msg.sender_type}}</small>{{msg.content}}</p></div><div class="thread-compose"><input v-model="draft" :placeholder="$t('portal.agent.replyPlaceholder')" @keydown.enter.prevent="send"/><button :disabled="busy||!draft.trim()" @click="send">{{ $t('portal.agent.reply') }}</button></div></template><p v-else class="empty">{{ $t('portal.agent.choose') }}</p></div>
 </div>
</section>
</template>
<style scoped>
.support-panel{margin:0 0 28px;background:var(--kx-surface,#fff);border:1px solid #e2e8f0;border-radius:20px;padding:23px;min-width:0}.support-heading{display:flex;align-items:center;justify-content:space-between;gap:16px}.support-heading h2{font-size:18px;line-height:1.4;margin:0;font-weight:700}.support-heading p{color:#64748b;font-size:12px;margin:7px 0}.quiet-btn,.support-item button,.thread-compose button{border:0;cursor:pointer;border-radius:10px;background:#0e7490;color:white;padding:10px 14px;font-size:12px;white-space:normal;line-height:1.4}.support-columns{margin-top:22px;display:grid;grid-template-columns:minmax(230px,1fr) minmax(0,1.45fr);gap:15px}.support-queue,.support-thread{border:1px solid #e2e8f0;border-radius:14px;padding:15px;min-width:0;min-height:230px}.support-queue h3{font-size:14px;margin:0 0 17px}.support-item{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:12px 0;border-top:1px solid #e2e8f0}.support-item strong{font-size:13px}.support-item small{display:block;font-size:11px;color:#64748b;margin-top:5px;word-break:break-word}.thread-title{font-weight:700;margin-bottom:12px}.thread-messages{max-height:300px;overflow-y:auto}.thread-bubble{background:#f1f5f9;border-radius:12px;padding:11px;font-size:13px;line-height:1.5;white-space:pre-wrap;overflow-wrap:anywhere}.thread-bubble small{display:block;font-size:10px;color:#64748b;margin-bottom:4px}.thread-agent{background:#e0f2fe}.thread-compose{display:flex;gap:8px;margin-top:14px}.thread-compose input{flex:1;min-width:0;border:1px solid #cbd5e1;padding:10px 12px;border-radius:10px;background:white;color:#0f172a;font-size:13px}.notice{font-size:12px;background:#e0f2fe;color:#075985;padding:12px;border-radius:10px;margin-top:12px}.notice.error{background:#fef2f2;color:#9f1239}.empty{color:#64748b;font-size:12px;line-height:1.6}@media(max-width:800px){.support-columns{grid-template-columns:minmax(0,1fr)}}@media(max-width:450px){.support-panel{padding:14px}.support-heading{align-items:flex-start;flex-wrap:wrap}}
</style>
