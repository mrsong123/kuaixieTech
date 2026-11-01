<script setup lang="ts">
import {ArrowUpRight, ArrowRight, MapPin, PackageOpen} from 'lucide-vue-next';
import {transportLabel} from '@kuaixie/i18n';
import {tripAvailability} from '@kuaixie/sdk';
import {useSiteContext} from '../stores/context';
const {trips, demands, nav, date, money, selectedItem} = useSiteContext();
</script>
<template>
<main class="hall">
  <section class="hall-hero">
    <div class="container hall-hero-grid">
      <div class="hall-copy">
        <p class="hall-kicker">{{ $t('site.kicker') }}</p>
        <h1>{{ $t('site.hero') }}</h1>
        <p class="hall-lead">{{ $t('site.lead') }}</p>
        <div class="hall-actions">
          <button class="hall-cta" type="button" @click="nav('trips')">{{ $t('site.ctaTrips') }} <ArrowUpRight :size="18"/></button>
          <button class="hall-ghost" type="button" @click="nav('route')">{{ $t('site.ctaDemand') }} <ArrowRight :size="17"/></button>
        </div>
      </div>
      <aside class="hall-pass" aria-hidden="true">
        <span class="hall-pass-stamp">KX</span>
        <p class="hall-pass-label">{{ $t('site.pass') }}</p>
        <div class="hall-pass-route">
          <b>{{ $t('site.from') }}</b>
          <i></i>
          <b>{{ $t('site.to') }}</b>
        </div>
        <ol class="hall-pass-steps">
          <li>{{ $t('site.how1t') }}</li>
          <li>{{ $t('site.how2t') }}</li>
          <li>{{ $t('site.how3t') }}</li>
          <li>{{ $t('site.how4t') }}</li>
        </ol>
      </aside>
    </div>
  </section>

  <section class="container hall-block">
    <header class="hall-head">
      <h2>{{ $t('site.how') }}</h2>
    </header>
    <ol class="hall-how">
      <li>
        <span>01</span>
        <b>{{ $t('site.how1t') }}</b>
        <p>{{ $t('site.how1d') }}</p>
      </li>
      <li>
        <span>02</span>
        <b>{{ $t('site.how2t') }}</b>
        <p>{{ $t('site.how2d') }}</p>
      </li>
      <li>
        <span>03</span>
        <b>{{ $t('site.how3t') }}</b>
        <p>{{ $t('site.how3d') }}</p>
      </li>
      <li>
        <span>04</span>
        <b>{{ $t('site.how4t') }}</b>
        <p>{{ $t('site.how4d') }}</p>
      </li>
    </ol>
  </section>

  <section class="container hall-block">
    <header class="hall-head">
      <div>
        <h2>{{ $t('site.live') }}</h2>
        <p>{{ $t('site.liveHint') }}</p>
      </div>
      <button class="hall-ghost" type="button" @click="nav('trips')">{{ $t('pc.nav.trips') }} <ArrowRight :size="16"/></button>
    </header>
    <div v-if="!trips.length" class="hall-empty">
      <PackageOpen :size="22"/>
      <p>{{ $t('site.liveEmpty') }}</p>
    </div>
    <ul v-else class="hall-trips">
      <li v-for="item in trips.slice(0,4)" :key="item.id">
        <button type="button" @click="selectedItem=item">
          <MapPin :size="16"/>
          <strong>{{ item.origin_city || $t('m2.common.notProvided') }} → {{ item.destination || $t('m2.common.notProvided') }}</strong>
          <span>{{ date(item.depart_date) }}</span>
          <span>{{ transportLabel(item.transport_mode) }}</span>
          <span>{{ tripAvailability(item) ?? $t('m2.trip.capacityUnknown') }}</span>
          <em>{{ money(item.fee_value) }}</em>
        </button>
      </li>
    </ul>
  </section>

  <section class="container hall-block">
    <header class="hall-head">
      <div>
        <h2>{{ $t('site.demands') }}</h2>
        <p>{{ $t('site.demandsHint') }}</p>
      </div>
      <button class="hall-ghost" type="button" @click="nav('route')">{{ $t('pc.nav.route') }} <ArrowRight :size="16"/></button>
    </header>
    <div v-if="!demands.length" class="hall-empty">
      <PackageOpen :size="22"/>
      <p>{{ $t('site.demandsEmpty') }}</p>
    </div>
    <ul v-else class="hall-trips">
      <li v-for="item in demands.slice(0,4)" :key="item.id">
        <button type="button" @click="selectedItem=item">
          <MapPin :size="16"/>
          <strong>{{ item.title }}</strong>
          <span>{{ item.city || $t('m2.common.notProvided') }}</span>
          <em>{{ money(item.expect_price) }}</em>
        </button>
      </li>
    </ul>
  </section>

  <section class="container hall-block">
    <header class="hall-head">
      <h2>{{ $t('site.roles') }}</h2>
    </header>
    <div class="hall-roles">
      <article>
        <h3>{{ $t('site.buyerT') }}</h3>
        <p>{{ $t('site.buyerD') }}</p>
        <button class="hall-ghost" type="button" @click="nav('route')">{{ $t('site.ctaDemand') }}</button>
      </article>
      <article>
        <h3>{{ $t('site.carrierT') }}</h3>
        <p>{{ $t('site.carrierD') }}</p>
        <button class="hall-ghost" type="button" @click="nav('trips')">{{ $t('portal.publish.trip') }}</button>
      </article>
      <article>
        <h3>{{ $t('site.shopT') }}</h3>
        <p>{{ $t('site.shopD') }}</p>
        <button class="hall-ghost" type="button" @click="nav('products')">{{ $t('pc.nav.products') }}</button>
      </article>
    </div>
  </section>

  <section class="container hall-block">
    <header class="hall-head">
      <h2>{{ $t('pc.hall.escrowTitle') }}</h2>
    </header>
    <p class="hall-lead">{{ $t('pc.hall.escrowBody') }}</p>
  </section>

  <section class="container hall-block hall-trust">
    <h2>{{ $t('site.trust') }}</h2>
    <ul>
      <li>{{ $t('site.trust1') }}</li>
      <li>{{ $t('site.trust2') }}</li>
      <li>{{ $t('site.trust3') }}</li>
    </ul>
  </section>

  <section class="container hall-block">
    <header class="hall-head">
      <div>
        <h2>{{ $t('site.entryTitle') }}</h2>
        <p>{{ $t('site.entryHint') }}</p>
      </div>
    </header>
    <div class="entry-grid">
      <article>
        <h3>{{ $t('site.entryOrder') }}</h3>
        <span>{{ $t('community.pendingUpload') }}</span>
      </article>
      <article>
        <h3>{{ $t('site.entryMp') }}</h3>
        <span>{{ $t('site.entryUnset') }}</span>
      </article>
      <article>
        <h3>{{ $t('site.entryAndroid') }}</h3>
        <span>{{ $t('site.entryUnset') }}</span>
      </article>
      <article>
        <h3>{{ $t('site.entryIos') }}</h3>
        <span>{{ $t('site.entryUnset') }}</span>
      </article>
    </div>
  </section>
</main>
</template>
<style scoped>
.hall-roles article{display:grid;gap:12px;align-content:start}
.entry-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:16px}
.entry-grid article{border:1px solid #17140f;background:#f7efd8;padding:14px;min-height:180px;display:grid;place-items:center;gap:8px}
.entry-grid img{width:140px;height:140px}
.entry-grid h3{margin:0;font-size:13px}
@media(max-width:800px){.entry-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}
</style>
