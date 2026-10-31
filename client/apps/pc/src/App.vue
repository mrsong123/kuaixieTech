<script setup lang="ts">
import {useSiteStore} from '@/stores/useSiteStore';
import {provideSiteStore} from '@/stores/context';
import SiteHeader from '@/components/SiteHeader.vue';
import SiteOverlays from '@/components/SiteOverlays.vue';
import HomeView from '@/views/HomeView.vue';
import ProductsView from '@/views/ProductsView.vue';
import TripsView from '@/views/TripsView.vue';
import RouteView from '@/views/RouteView.vue';
import CommunityView from '@/views/CommunityView.vue';
import WorkbenchView from '@/views/WorkbenchView.vue';
const site=useSiteStore();
provideSiteStore(site);
const {page}=site;
</script>
<template>
  <div class="app-shell">
    <SiteHeader />
    <HomeView v-if="page==='home'" />
    <ProductsView v-else-if="page==='products'" />
    <TripsView v-else-if="page==='trips'" />
    <RouteView v-else-if="page==='route'" />
    <CommunityView v-else-if="page==='community'" />
    <WorkbenchView v-else-if="page==='workspace'" />
    <SiteOverlays />
  </div>
</template>
