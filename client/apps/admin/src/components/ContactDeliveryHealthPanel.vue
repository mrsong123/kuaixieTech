<script setup lang="ts">
import {onMounted} from 'vue';
import {t,formatDate} from '@kuaixie/i18n';
import {useAdminContext} from '../stores/context';
import {useContactDeliveryHealth,type DeliveryState} from './useContactDeliveryHealth';
const {api}=useAdminContext();const p=useContactDeliveryHealth(api);
const channels=['sms','email'] as const;
function stateKey(state:string){return 'contactHealth.state.'+state}
function helpKey(state:DeliveryState){return 'contactHealth.help.'+state}
function queueKey(state:string){return ['pending','blocked','sent','failed'].includes(state)?'contactHealth.queue.'+state:'contactHealth.queue.unknown'}
onMounted(()=>{void p.refresh()});
</script>
<template>
<section class="contact-health surface" aria-live="polite">
 <header><div><h2>{{t('contactHealth.title')}}</h2><p>{{t('contactHealth.readOnly')}}</p></div><button class="primary-button" type="button" :disabled="p.loading.value" @click="p.refresh">{{t('contactHealth.refresh')}}</button></header>
 <p v-if="p.loading.value">{{t('account.loading')}}</p><p v-if="p.error.value" class="health-error" role="alert">{{p.error.value}}</p>
 <template v-if="p.health.value">
  <div class="health-summary"><strong>{{t(stateKey(p.health.value.state))}}</strong><span>{{t('contactHealth.checkedAt')}} · {{formatDate(p.health.value.checkedAt)}}</span></div>
  <p>{{t('contactHealth.notDeliveryProof')}}</p>
  <p v-if="!p.health.value.dispatchConfigured" class="health-warning">{{t('contactHealth.notConfigured')}}</p>
  <p v-if="p.blocked.value" class="health-warning" role="alert">{{t('contactHealth.blockedHelp',{count:p.blocked.value})}}</p>
  <div class="health-channels">
   <article v-for="channel in channels" :key="channel">
    <h3>{{t('contactHealth.channel.'+channel)}}</h3>
    <p><b>{{t('contactHealth.configuration')}}</b> · <strong>{{t(stateKey(p.health.value.configuration[channel].state))}}</strong></p>
    <p>{{t(helpKey(p.health.value.configuration[channel].state))}}</p>
    <dl>
     <dt>{{t('contactHealth.reason')}}</dt><dd><code>{{p.health.value.configuration[channel].reasonCode}}</code></dd>
     <template v-if="p.health.value.configuration[channel].missingTable"><dt>{{t('contactHealth.missingTable')}}</dt><dd><code>{{p.health.value.configuration[channel].missingTable}}</code></dd></template>
     <template v-if="p.health.value.configuration[channel].sqlState"><dt>SQLState</dt><dd>{{p.health.value.configuration[channel].sqlState}}</dd></template>
     <template v-if="p.health.value.configuration[channel].databaseError!=null"><dt>{{t('contactHealth.databaseError')}}</dt><dd>{{p.health.value.configuration[channel].databaseError}}</dd></template>
    </dl>
    <div class="last-dispatch"><h4>{{t('contactHealth.lastRun')}}</h4>
     <p v-if="!p.health.value.lastRun.checkedAt">{{t('contactHealth.state.not_checked')}}</p>
     <template v-else><p>{{formatDate(p.health.value.lastRun.checkedAt)}} · {{t(stateKey(p.health.value.lastRun[channel].state))}}</p><p>{{t(helpKey(p.health.value.lastRun[channel].state))}}</p><code>{{p.health.value.lastRun[channel].reasonCode}}</code>
      <p v-if="p.health.value.lastRun[channel].missingTable">{{t('contactHealth.missingTable')}} · <code>{{p.health.value.lastRun[channel].missingTable}}</code></p>
     </template>
    </div>
   </article>
  </div>
  <h3>{{t('contactHealth.queueTitle')}}</h3>
  <div v-if="p.health.value.queueDiagnostic" class="health-error" role="alert"><p>{{t('contactHealth.queueUnavailable')}}</p><p>{{t(helpKey(p.health.value.queueDiagnostic.state))}}</p><code>{{p.health.value.queueDiagnostic.reasonCode}}</code><p v-if="p.health.value.queueDiagnostic.missingTable">{{t('contactHealth.missingTable')}} · <code>{{p.health.value.queueDiagnostic.missingTable}}</code></p></div>
  <table v-else-if="p.health.value.queue.length"><thead><tr><th>{{t('contactHealth.channelLabel')}}</th><th>{{t('contactHealth.queueStatus')}}</th><th>{{t('contactHealth.count')}}</th></tr></thead><tbody><tr v-for="row in p.health.value.queue" :key="row.channel+':'+row.status"><td>{{t('contactHealth.channel.'+(row.channel==='phone'?'sms':'email'))}}</td><td>{{t(queueKey(row.status))}}</td><td>{{row.total}}</td></tr></tbody></table>
  <p v-else>{{t('contactHealth.queueEmpty')}}</p>
 </template>
</section>
</template>
<style scoped>
.contact-health{margin:20px 0;padding:24px;border:1px solid var(--desk-rule,#d7c6a4);background:var(--desk-card,#fffaf0);color:var(--desk-ink,#17140f)}header,.health-summary{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;flex-wrap:wrap}h2,h3,h4{margin:0 0 12px}.contact-health p{font-size:13px;line-height:1.7;overflow-wrap:anywhere}.health-summary{padding:14px 0;border-block:1px solid var(--desk-rule,#d7c6a4);font-size:14px}.health-channels{display:grid;grid-template-columns:1fr 1fr;gap:18px;margin:20px 0}.health-channels article{border:1px solid var(--desk-rule,#d7c6a4);padding:18px;min-width:0}.health-warning{padding:12px;background:#fff2d4;color:#744511}.health-error{padding:12px;border:1px solid #b42318;color:#8d241a}dl{display:grid;grid-template-columns:minmax(80px,1fr) minmax(0,2fr);gap:8px;font-size:12px}dd{margin:0;overflow-wrap:anywhere}.last-dispatch{border-top:1px solid var(--desk-rule,#d7c6a4);padding-top:14px;margin-top:18px}code{overflow-wrap:anywhere;font-size:12px}table{width:100%;border-collapse:collapse;font-size:13px}th,td{border-bottom:1px solid var(--desk-rule,#d7c6a4);text-align:left;padding:10px}@media(max-width:780px){.health-channels{grid-template-columns:1fr}.contact-health{padding:16px}}
</style>
