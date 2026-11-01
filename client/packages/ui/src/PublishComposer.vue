<script setup lang="ts">
import type {ApiClient} from '@kuaixie/sdk';
import {usePublishComposer} from './usePublishComposer';
import ProductPublishing from './ProductPublishing.vue';
const props=defineProps<{client:ApiClient}>();
const p=usePublishComposer(props.client);
</script>
<template><section class="business-card"><h1>{{$t('portal.publish.title')}}</h1><p>{{$t('portal.publish.subtitle')}}</p>
 <nav class="business-tabs"><button v-for="kind in (['demand','trip','life','product'] as const)" :key="kind" :aria-pressed="p.selected.value===kind" @click="p.changeMode(kind)">{{$t(kind==='product'?'product.title':kind==='demand'?'portal.publish.demand':kind==='trip'?'portal.publish.trip':'portal.publish.life')}}</button></nav>
 <ProductPublishing v-if="p.selected.value==='product'" :key="p.sessionScope.value" :client="client"/>
 <form v-else @submit.prevent="p.submit">
 <label v-if="p.selected.value!=='life'">{{$t('portal.publish.region')}}<select v-model="p.region.value"><option value="domestic">{{$t('portal.square.domestic')}}</option><option value="international">{{$t('portal.square.international')}}</option></select></label>
 <template v-if="p.selected.value==='demand'"><label>{{$t('portal.publish.demandTitle')}}<input v-model="p.demand.value.title" maxlength="200" required/></label><label>{{$t('portal.publish.demandDescription')}}<textarea v-model="p.demand.value.description" maxlength="4000"/></label><label>{{$t('portal.publish.city')}}<input v-model="p.demand.value.city" maxlength="64"/></label><label>{{$t('portal.publish.budget')}}<input v-model="p.demand.value.expectPrice" inputmode="decimal"/></label></template>
 <template v-else-if="p.selected.value==='trip'"><label>{{$t('portal.publish.origin')}}<input v-model="p.trip.value.originCity" maxlength="64" required/></label><label>{{$t('portal.publish.destination')}}<input v-model="p.trip.value.destination" maxlength="128" required/></label><label>{{$t('portal.publish.depart')}}<input v-model="p.trip.value.departDate" type="date" required/></label><label>{{$t('portal.publish.return')}}<input v-model="p.trip.value.returnDate" type="date" required/></label><label>{{$t('portal.publish.transport')}}<select v-model="p.trip.value.transportMode"><option v-for="mode in p.transportOptions" :key="mode" :value="mode">{{p.transportTitle(mode)}}</option></select></label><label>{{$t('portal.publish.capacity')}}<input v-model="p.trip.value.capacityQty" type="number" min="1" max="100"/></label></template>
 <p v-if="p.selected.value==='life'">{{$t('portal.publish.textOnly')}}</p>
 <label v-if="p.selected.value==='life'">{{$t('portal.publish.life')}}<textarea v-model="p.post.value.caption" maxlength="4000" required/></label>
 <button :disabled="p.busy.value||p.unconfirmed.value">{{$t('portal.publish.submit')}}</button><p v-if="p.notice.value" role="status">{{$t(p.notice.value)}}</p><p v-if="p.error.value" role="alert">{{p.error.value}}</p><button v-if="p.unconfirmed.value" type="button" @click="p.acknowledgeUnconfirmed">{{$t('portal.publish.checkedRecords')}}</button><p>{{$t('portal.publish.review')}}</p>
 </form></section></template>
