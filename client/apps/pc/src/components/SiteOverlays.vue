<script setup lang="ts">
import {ArrowUpRight,ArrowRight,ArrowLeft,Search,MapPin,Navigation,PackageOpen,ShieldCheck,ChevronRight,Route,Gift,Heart,Compass,Play,ShoppingBag,Clock3,Users,Check,AlertCircle,RefreshCw,Tag,SlidersHorizontal,Globe2,Menu, X, Zap, Fingerprint, Settings2} from 'lucide-vue-next';import {t, formatAmount, formatDate, transportLabel, localizedField, setLocale, getLocale, operationLabel, groupLabel, uiErrorMessage} from '@kuaixie/i18n';
import {tripAvailability} from '@kuaixie/sdk';
import {ref} from 'vue';
import IntentOrderForm from './IntentOrderForm.vue';
import ProductActions from '@kuaixie/ui/ProductActions.vue';
import BusinessAuth from '@kuaixie/ui/BusinessAuth.vue';
const pendingIntent=ref(false);
import ApiWorkbench from '@kuaixie/ui/ApiWorkbench.vue';
import PreferencesPanel from '@kuaixie/ui/PreferencesPanel.vue';
import VoiceDraft from '@kuaixie/ui/VoiceDraft.vue';
import ComparisonPanel from '@kuaixie/ui/ComparisonPanel.vue';
import {useSiteContext} from '../stores/context';
const {locale,showDisplaySettings,changeLocale,api,page,menu,products,trips,categories,busy,loadError,search,activeCategory,tripSort,selectedItem,credentials,showAuth,toast,heroStats,navItems,nav,displayName,img,filtered,money,date,message,refresh,changeTripSort,selectedProduct,selectedTrip,selectedCategoryName,useToken}=useSiteContext();
</script>
<template>
<div class="site-overlays">
  <div v-if="loadError" class="connection-alert container"><AlertCircle :size="18"/>{{loadError}}<button @click="refresh">{{ $t('text.e2d53a6d3a6a') }}</button></div>
  <footer class="site-footer"><div class="container footer-inner"><div class="brand footer-brand"><span class="brand-mark"><Navigation :size="18"/></span><span>{{ $t('text.db680ac702a7') }}<small>{{ $t('text.b0faece9e105') }}</small></span></div><div><span>{{ $t('text.d90b478486c5') }}</span><small>{{ $t('text.e027b971609d') }}</small></div></div></footer>
  <div v-if="page==='route'" class="container kx-smart-block"><VoiceDraft/><ComparisonPanel :products="products" :trips="trips"/></div>
  <div v-if="page==='products'" class="container kx-smart-block"><ComparisonPanel :products="products" :trips="trips"/></div>
  <div v-if="showAuth" class="modal-backdrop" @click.self="showAuth=false"><div class="modal auth-modal"><button class="modal-close" @click="showAuth=false"><X :size="19"/></button><BusinessAuth :client="api" @accepted="showAuth=false;nav('me')" @legal="showAuth=false;nav(('services/legal-doc?type='+$event) as import('../router').Page)" @support="showAuth=false;nav('messages')"/></div></div>
  <div v-if="selectedItem" class="modal-backdrop" @click.self="!pendingIntent&&(selectedItem=null)"><div class="modal detail-modal"><button class="modal-close" :disabled="pendingIntent" @click="selectedItem=null"><X :size="19"/></button><template v-if="selectedProduct(selectedItem)"><span class="section-kicker">{{ $t('m2.catalog.detail') }}</span><div class="detail-product"><div class="detail-media"><img v-if="img(selectedItem)" :src="img(selectedItem)" :alt="displayName(selectedItem)"/><ShoppingBag v-else :size="45"/></div><div class="detail-content"><h2>{{displayName(selectedItem)}}</h2><strong class="detail-price">{{money(selectedItem.price,selectedItem.currency)}}</strong><dl><div><dt>{{$t('m2.catalog.category')}}</dt><dd>{{selectedCategoryName}}</dd></div><div><dt>{{$t('m2.catalog.origin')}}</dt><dd>{{selectedItem.region||$t('m2.catalog.originUnknown')}}</dd></div><div><dt>{{$t('m2.catalog.specification')}}</dt><dd>{{selectedItem.spec||$t('m2.catalog.specUnknown')}}</dd></div><div><dt>{{$t('m2.catalog.stock')}}</dt><dd>{{selectedItem.stock??$t('m2.catalog.stockUnknown')}}</dd></div></dl><ProductActions :key="selectedItem.id" :client="api" :product-id="selectedItem.id"/><p class="detail-notice"><ShieldCheck :size="18"/>{{$t('m2.catalog.buyUnavailable')}}</p></div></div></template>
  <template v-else-if="selectedTrip(selectedItem)"><span class="section-kicker">{{ $t('m2.trip.detail') }}</span><h2>{{selectedItem.destination||$t('m2.common.notProvided')}}</h2><dl class="detail-list"><div><dt>{{$t('m2.trip.depart')}}</dt><dd>{{date(selectedItem.depart_date)}}</dd></div><div><dt>{{$t('m2.trip.arrival')}}</dt><dd>{{date(selectedItem.arrive_date)}}</dd></div><div><dt>{{$t('m2.trip.return')}}</dt><dd>{{date(selectedItem.return_date)}}</dd></div><div><dt>{{$t('m2.trip.transport')}}</dt><dd>{{transportLabel(selectedItem.transport_mode)}}</dd></div><div><dt>{{$t('m2.trip.remaining')}}</dt><dd>{{tripAvailability(selectedItem)??$t('m2.trip.capacityUnknown')}}</dd></div><div><dt>{{$t('m2.trip.fee')}}</dt><dd>{{money(selectedItem.fee_value)}}</dd></div></dl><IntentOrderForm :key="selectedItem.id" :client="api" :trip-id="selectedItem.id" @pending="pendingIntent=$event"/></template>
  <button class="btn-outline" :disabled="pendingIntent" @click="selectedItem=null">{{ $t('m2.catalog.close') }}</button></div></div>
  <div v-if="toast" class="toast"><Check :size="16"/>{{toast}}</div>
 
</div>
</template>
