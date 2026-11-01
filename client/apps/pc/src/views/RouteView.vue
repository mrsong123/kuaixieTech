<script setup lang="ts">
import {MapPin, PackageOpen, RefreshCw} from 'lucide-vue-next';
import {transportLabel} from '@kuaixie/i18n';
import PublishEntryForm from '../components/PublishEntryForm.vue';
import {useSiteContext} from '../stores/context';
const {api,sessionScope,trips,demands,listedTrips,listedDemands,tripSort,changeTripSort,date,money,selectedItem,refresh,busy,showAuth,nav}=useSiteContext();
</script>
<template>
<main class="container inner-page">
  <div class="page-intro">
    <span class="section-kicker">{{ $t('site.kicker') }}</span>
    <h1>{{ $t('site.demands') }}</h1>
    <p>{{ $t('site.demandsHint') }}</p>
  </div>
  <PublishEntryForm :key="sessionScope" :client="api" kind="demand" @login="showAuth=true" @published="refresh"/>
  <div class="trips-heading">
    <h3>{{ $t('site.demands') }} <small>{{ listedDemands.length }}</small></h3>
    <button class="btn-outline" type="button" :disabled="busy" @click="refresh"><RefreshCw :size="16"/>{{ $t('text.5982c44c18df') }}</button>
  </div>
  <div v-if="!listedDemands.length" class="empty-panel">
    <PackageOpen :size="26"/>
    <b>{{ demands.length ? $t('site.searchEmpty') : $t('site.demandsEmpty') }}</b>
  </div>
  <ul v-else class="hall-trips">
    <li v-for="item in listedDemands" :key="item.id">
      <button type="button" @click="selectedItem=item">
        <MapPin :size="16"/>
        <strong>{{ item.title }}</strong>
        <span>{{ item.city || $t('m2.common.notProvided') }}</span>
        <em>{{ money(item.expect_price) }}</em>
      </button>
    </li>
  </ul>
  <div class="action-row">
    <button class="hall-ghost" type="button" @click="nav('trips')">{{ $t('pc.nav.trips') }}</button>
  </div>
</main>
</template>
<style scoped>
.hall-form select{border:1px solid #c9b896;background:#fffaf0;padding:10px 11px}
.hall-form .check{display:flex;align-items:center;gap:8px}
</style>
