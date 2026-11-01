<script setup lang="ts">
import {ref,onMounted} from 'vue';
import type {ApiClient,OriginalInviteTier} from '@kuaixie/sdk';
import {t,uiErrorMessage,formatAmount} from '@kuaixie/i18n';
const props=defineProps<{client:ApiClient}>();const summary=ref<OriginalInviteTier|null>(null),error=ref(''),busy=ref(false);
async function load(){busy.value=true;error.value='';try{summary.value=await props.client.originalV15.inviteTier()}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}onMounted(load);
</script><template><section class="business-card"><h2>{{t('v15.inviteTier')}}</h2><p>{{t('case.inviteNotice')}}</p><p v-if="error" role="alert">{{error}}</p><dl v-if="summary"><dt>{{t('v15.tier')}}</dt><dd>{{summary.tier}}</dd><dt>{{t('v15.directCount')}}</dt><dd>{{summary.direct_count}}</dd><dt>{{t('case.validCount')}}</dt><dd>{{summary.valid_count}}</dd><dt>{{t('v15.points')}}</dt><dd>{{summary.points}}</dd><dt>{{t('case.commission')}}</dt><dd>{{formatAmount(summary.commission_mtd??0,'CNY')}}</dd></dl><button :disabled="busy" @click="load">{{t('case.refresh')}}</button></section></template>
