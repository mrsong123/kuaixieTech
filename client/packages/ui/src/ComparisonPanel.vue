<script setup lang="ts">
import {computed,ref} from 'vue';
import {formatAmount,localizedField} from '@kuaixie/i18n';
import type {PublicProduct,PublicTrip} from '@kuaixie/sdk';
const props=defineProps<{products:PublicProduct[];trips:PublicTrip[]}>();
const a=ref('');const b=ref('');
const first=computed(()=>props.products.find(p=>String(p.id)===a.value));
const second=computed(()=>props.products.find(p=>String(p.id)===b.value));
function label(p:PublicProduct){return localizedField(p as unknown as Record<string,unknown>,'name')}
function price(p:PublicProduct){return formatAmount(p.price,p.currency)}
</script>
<template>
  <section class="kx-preferences kx-ai" :aria-label="$t('ai.title')">
    <h2 class="kx-preferences__head">{{ $t('ai.title') }}</h2>
    <p class="kx-preferences__hint">{{ $t('ai.rulesOnly') }}</p>
    <div class="kx-ai__columns">
      <div><h3>{{ $t('ai.buyer') }}</h3><p>{{ $t('ai.browseProducts') }}：{{ products.length }}</p><p>{{ $t('ai.browseTrips') }}：{{ trips.length }}</p></div>
      <div><h3>{{ $t('ai.carrier') }}</h3><p>{{ $t('ai.noDemands') }}</p><p>{{ $t('ai.needRoute') }}</p></div>
    </div>
    <h3>{{ $t('ai.compare') }}</h3><p class="kx-preferences__hint">{{ $t('ai.compareHint') }}</p>
    <div class="kx-ai__columns">
      <label class="kx-preferences__field">{{ $t('ai.selectA') }}<select v-model="a"><option value="">{{ $t('common.notProvided') }}</option><option v-for="p in products" :key="String(p.id)" :value="String(p.id)">{{ label(p) }}</option></select></label>
      <label class="kx-preferences__field">{{ $t('ai.selectB') }}<select v-model="b"><option value="">{{ $t('common.notProvided') }}</option><option v-for="p in products" :key="String(p.id)" :value="String(p.id)">{{ label(p) }}</option></select></label>
    </div>
    <div v-if="first&&second" class="kx-ai__table-wrap"><table class="kx-ai__table"><thead><tr><th>{{ $t('ai.field') }}</th><th>{{label(first)}}</th><th>{{label(second)}}</th></tr></thead><tbody>
      <tr><th>{{ $t('ai.price') }}</th><td>{{price(first)}}</td><td>{{price(second)}}</td></tr>
      <tr><th>{{ $t('ai.region') }}</th><td>{{first.region||$t('common.notProvided')}}</td><td>{{second.region||$t('common.notProvided')}}</td></tr>
      <tr><th>{{ $t('ai.spec') }}</th><td>{{first.spec||$t('common.notProvided')}}</td><td>{{second.spec||$t('common.notProvided')}}</td></tr>
      <tr><th>{{ $t('ai.stock') }}</th><td>{{first.stock??$t('common.notProvided')}}</td><td>{{second.stock??$t('common.notProvided')}}</td></tr>
    </tbody></table></div>
    <p v-else class="kx-preferences__hint">{{ $t('ai.empty') }}</p>
    <p class="kx-preferences__hint">{{ $t('ai.noAI') }}</p>
  </section>
</template>
<style scoped>
.kx-ai__columns{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:15px;margin:16px 0}
.kx-ai__columns>div{border:1px solid var(--kx-border);border-radius:10px;padding:12px;min-width:0}
.kx-ai__columns h3{font-size:1em;overflow-wrap:anywhere}
.kx-ai__columns p{color:var(--kx-muted);line-height:1.6}
.kx-ai__table-wrap{overflow:auto;max-width:100%}
.kx-ai__table{width:100%;border-collapse:collapse;min-width:380px;margin-top:14px;text-align:left}
.kx-ai__table th,.kx-ai__table td{padding:10px;border-bottom:1px solid var(--kx-border);font-weight:400;overflow-wrap:anywhere}
@media(max-width:650px){.kx-ai__columns{grid-template-columns:1fr}}
</style>
