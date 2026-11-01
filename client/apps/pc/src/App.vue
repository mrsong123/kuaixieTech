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
import BusinessView from '@/views/BusinessView.vue';
import WorkbenchView from '@/views/WorkbenchView.vue';
import BusinessCenterView from '@/views/BusinessCenterView.vue';
const site=useSiteStore();
provideSiteStore(site);
const {page,sessionScope}=site;
</script>
<template>
  <div class="app-shell">
    <SiteHeader />
    <HomeView v-if="page==='home'" />
    <ProductsView v-else-if="page==='products'" />
    <TripsView v-else-if="page==='trips'" :key="sessionScope"/>
    <RouteView v-else-if="page==='route'" :key="sessionScope"/>
    <CommunityView v-else-if="page==='community'" :key="sessionScope"/>
    <BusinessView v-else-if="page==='business'" :key="sessionScope"/>
    <WorkbenchView v-else-if="page==='workspace'" :key="sessionScope"/>
    <BusinessCenterView v-else :key="sessionScope"/>
    <SiteOverlays />
  </div>
</template>
