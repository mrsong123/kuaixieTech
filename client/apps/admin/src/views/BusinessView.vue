<script setup lang="ts">
import {ref} from 'vue';
import ShopCentre from '@kuaixie/ui/ShopCentre.vue';
import TradeCases from '@kuaixie/ui/TradeCases.vue';
import PaymentRefunds from '@kuaixie/ui/PaymentRefunds.vue';
import {useAdminContext} from '../stores/context';
const {api}=useAdminContext();
const tab=ref<'shops'|'disputes'|'paymentRefunds'>('shops');
const tabs=[{key:'shops',label:'admin.tab.shops'},{key:'disputes',label:'admin.tab.disputes'},{key:'paymentRefunds',label:'admin.tab.paymentRefunds'}] as const;
</script>
<template>
  <div class="admin-view business-desk">
    <nav class="business-tabs"><button v-for="item in tabs" :key="item.key" class="soft-button" :class="{active:tab===item.key}" @click="tab=item.key">{{$t(item.label)}}</button></nav>
    <ShopCentre v-if="tab==='shops'" :client="api" admin/>
    <TradeCases v-else-if="tab==='disputes'" :client="api" admin/>
    <PaymentRefunds v-else :client="api" admin/>
  </div>
</template>
