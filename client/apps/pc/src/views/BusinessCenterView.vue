<script setup lang="ts">
import PaymentRefunds from '@kuaixie/ui/PaymentRefunds.vue';
import ShopPublic from '@kuaixie/ui/ShopPublic.vue';
import ShopCentre from '@kuaixie/ui/ShopCentre.vue';
import InviteSummary from '@kuaixie/ui/InviteSummary.vue';
import TradeCases from '@kuaixie/ui/TradeCases.vue';
import {computed} from 'vue';
import {businessServices,serviceGroups,flowIds} from '@kuaixie/sdk';
import InsuranceCentre from '@kuaixie/ui/InsuranceCentre.vue';
import SavedCommerce from '@kuaixie/ui/SavedCommerce.vue';
import PublishComposer from '@kuaixie/ui/PublishComposer.vue';
import ProductPublishing from '@kuaixie/ui/ProductPublishing.vue';
import BusinessWorkflow from '@kuaixie/ui/BusinessWorkflow.vue';
import ProductBusinessPanel from '../components/ProductBusinessPanel.vue';
import AccountBusinessPanel from '../components/AccountBusinessPanel.vue';
import SupportBusinessPanel from '../components/SupportBusinessPanel.vue';
import PublicDocumentsPanel from '../components/PublicDocumentsPanel.vue';
import {useSiteContext} from '../stores/context';
const {api,page,nav,showAuth}=useSiteContext();
const selected=computed(()=>page.value.startsWith('services/')?page.value.slice(9).split('?')[0]:'');
const context=computed(()=>Object.fromEntries(new URLSearchParams(page.value.split('?')[1]||'')));
function open(id:string){nav(('services/'+id) as import('../router').Page)}
</script>
<template><main class="business-layout">
 <PublishComposer v-if="page==='publish'" :client="api"/>
 <SupportBusinessPanel v-else-if="page==='messages'||selected==='support-center'" :client="api"/>
 <PublicDocumentsPanel v-else-if="selected==='announcements'||selected==='legal-doc'" :key="page" :client="api" :document-key="selected==='legal-doc'?context.type:undefined"/>
 <SavedCommerce v-else-if="['address-book','cart','favorites'].includes(selected)" :key="selected" :client="api" :kind="selected==='address-book'?'addresses':selected==='cart'?'cart':'favorites'"/>
 <ProductBusinessPanel v-else-if="selected==='product-detail'" :key="page" :client="api" :id="context.id||''"/>
 <ProductPublishing v-else-if="selected==='my-products'" :client="api"/>
 <BusinessWorkflow v-else-if="flowIds.includes(selected)" :key="page" :client="api" :flow-id="selected" :context="context"/>
 <InsuranceCentre v-else-if="selected==='my-insurance'" :client="api"/>
 <PaymentRefunds v-else-if="selected==='my-refunds'" :client="api"/>
 <ShopPublic v-else-if="selected==='shop-detail'" :client="api" :id="context.id||''"/>
 <ShopCentre v-else-if="selected==='my-shop'" :client="api"/>
 <InviteSummary v-else-if="selected==='invite-tier'" :client="api"/>
 <TradeCases v-else-if="selected==='my-disputes'" :client="api" :order-id="context.orderId" :case-id="context.id"/>
 <AccountBusinessPanel v-else-if="selected.startsWith('my-')||selected==='wallet-ledger'" :key="selected" :client="api" :kind="selected" @open="open" @signed-out="showAuth=true"/>
 <template v-else><AccountBusinessPanel v-if="page==='me'" :client="api" @open="open" @signed-out="showAuth=true"/><section v-for="group in serviceGroups" :key="group.id" class="business-card"><h2>{{$t(group.titleKey)}}</h2><div class="business-grid"><a v-for="item in businessServices.filter(item=>item.group===group.id)" :key="item.id" :href="'#'+item.desktop">{{$t(item.titleKey)}}</a></div></section></template>
</main></template>
