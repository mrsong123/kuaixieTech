<script setup lang="ts">
import {Clock3, Navigation, PackageOpen, RefreshCw} from 'lucide-vue-next';
import {transportLabel} from '@kuaixie/i18n';
import PublishEntryForm from '../components/PublishEntryForm.vue';
import {useSiteContext} from '../stores/context';
const {api,sessionScope,trips,demands,listedTrips,listedDemands,tripSort,changeTripSort,date,money,selectedItem,refresh,busy,showAuth,nav}=useSiteContext();
</script>
<template>
<main class="container inner-page">
  <div class="page-intro">
    <span class="section-kicker">{{ $t('site.kicker') }}</span>
    <h1>{{ $t('portal.publish.trip') }}</h1>
    <p>{{ $t('portal.publish.tripDesc') }}</p>
  </div>
  <PublishEntryForm :key="sessionScope" :client="api" kind="trip" @login="showAuth=true" @published="refresh"/>
  <div class="trips-heading">
    <label class="trip-sort">{{ $t('m2.trip.detail') }}
      <select v-model="tripSort" @change="changeTripSort">
        <option value="rank">{{ $t('m2.trip.sort.rank') }}</option>
        <option value="latest">{{ $t('m2.trip.sort.latest') }}</option>
        <option value="score">{{ $t('m2.trip.sort.score') }}</option>
      </select>
    </label>
    <h3>{{ $t('pc.nav.trips') }} <small>{{ listedTrips.length }}</small></h3>
    <button class="btn-outline" type="button" @click="refresh"><RefreshCw :size="16"/>{{ $t('text.5982c44c18df') }}</button>
  </div>
  <div v-if="!listedTrips.length" class="empty-panel"><PackageOpen/><b>{{ trips.length ? $t('site.searchEmpty') : $t('site.liveEmpty') }}</b></div>
  <div v-else class="trip-list">
    <article v-for="item in listedTrips" :key="item.id" class="trip-card">
      <div class="trip-icon"><Navigation :size="24"/></div>
      <div class="trip-main">
        <small>{{ transportLabel(item.transport_mode) }}</small>
        <h3>{{ item.origin_city || $t('m2.common.notProvided') }} → {{ item.destination || $t('m2.common.notProvided') }}</h3>
        <div class="trip-meta"><span><Clock3 :size="14"/>{{ date(item.depart_date) }}</span></div>
      </div>
      <div class="trip-right"><button type="button" @click="selectedItem=item">{{ $t('m2.catalog.detail') }}</button></div>
    </article>
  </div>
</main>
</template>
<style scoped>
.hall-form select{border:1px solid #c9b896;background:#fffaf0;padding:10px 11px}
</style>
