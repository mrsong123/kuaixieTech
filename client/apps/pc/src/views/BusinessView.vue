<script setup lang="ts">
import {computed,ref} from 'vue';
import {t} from '@kuaixie/i18n';
import ShopCentre from '@kuaixie/ui/ShopCentre.vue';
import InsuranceCentre from '@kuaixie/ui/InsuranceCentre.vue';
import TradeCases from '@kuaixie/ui/TradeCases.vue';
import PaymentRefunds from '@kuaixie/ui/PaymentRefunds.vue';
import {useSiteContext} from '../stores/context';
const {api,sessionScope,nav}=useSiteContext();
const tab=ref<'apply'|'insurance'|'aftersales'|'refunds'>('apply');
const tabs=computed(()=>[{key:'apply' as const,label:t('pc.business.tab.apply')},{key:'insurance' as const,label:t('pc.business.tab.insurance')},{key:'aftersales' as const,label:t('pc.business.tab.aftersales')},{key:'refunds' as const,label:t('refund.title')}]);
</script>
<template><main class="container inner-page"><div class="page-intro"><span class="section-kicker">{{$t('site.kicker')}}</span><h1>{{$t('pc.business.title')}}</h1><p>{{$t('pc.business.subtitle')}}</p></div><nav class="business-tabs" :aria-label="$t('pc.business.title')"><button v-for="item in tabs" :key="item.key" :aria-pressed="tab===item.key" :class="{active:tab===item.key}" @click="tab=item.key">{{item.label}}</button><button @click="nav('me')">{{$t('mobile.tab.me')}}</button></nav><div class="business-panel"><ShopCentre v-if="tab==='apply'" :key="sessionScope" :client="api"/><InsuranceCentre v-else-if="tab==='insurance'" :key="sessionScope" :client="api"/><TradeCases v-else-if="tab==='aftersales'" :key="sessionScope" :client="api"/><PaymentRefunds v-else :key="sessionScope" :client="api"/></div></main></template>
