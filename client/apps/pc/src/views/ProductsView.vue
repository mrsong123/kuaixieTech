<script setup lang="ts">
import {ArrowUpRight,ArrowRight,ArrowLeft,Search,MapPin,Navigation,PackageOpen,ShieldCheck,ChevronRight,Route,Gift,Heart,Compass,Play,ShoppingBag,Clock3,Users,Check,AlertCircle,RefreshCw,Tag,SlidersHorizontal,Globe2,Menu, X, Zap, Fingerprint, Settings2} from 'lucide-vue-next';import {t, formatAmount, formatDate, transportLabel, localizedField, setLocale, getLocale, operationLabel, groupLabel, uiErrorMessage} from '@kuaixie/i18n';
import {tripAvailability} from '@kuaixie/sdk';
import ApiWorkbench from '@kuaixie/ui/ApiWorkbench.vue';
import PreferencesPanel from '@kuaixie/ui/PreferencesPanel.vue';
import VoiceDraft from '@kuaixie/ui/VoiceDraft.vue';
import ComparisonPanel from '@kuaixie/ui/ComparisonPanel.vue';
import {useSiteContext} from '../stores/context';
import ProductPublishing from '@kuaixie/ui/ProductPublishing.vue';
const {api,sessionScope,activeCategory,categories,filtered,displayName,img,money,selectedItem,refresh}=useSiteContext();
</script>
<template>
<main  class="container inner-page"><div class="page-intro"><span class="section-kicker">{{ $t('text.279cba87bc56') }}</span><h1>{{ $t('text.3c5bc199e4ed') }}</h1><p>{{ $t('shop.listWaiting') }}</p></div>
<ProductPublishing :key="sessionScope" :client="api"/>
<div class="catalog-toolbar"><span class="catalog-count"><span>{{$t('m2.catalog.count',{count:filtered.length})}}</span></span><button class="btn-outline" @click="refresh"><RefreshCw :size="16"/>{{ $t('text.5ce653f3f5df') }}</button></div><div class="category-chips"><button :class="{selected:activeCategory==='__all__'}" @click="activeCategory='__all__'">{{ $t('text.4b2301945ea3') }}</button><button v-for="c in categories" :key="String(c.id)" :class="{selected:activeCategory===String(c.id)}" @click="activeCategory=String(c.id)">{{displayName(c)}}</button></div><div class="catalog-count"><span>{{$t('m2.catalog.count',{count:filtered.length})}}</span><small>{{$t('m2.catalog.recentLimit')}}</small></div><div v-if="!filtered.length" class="empty-panel"><PackageOpen/><b>{{ $t('text.614f1b739d30') }}</b><p>{{ $t('text.1c4aa6d5a4ab') }}</p></div><div v-else class="product-grid full"><button class="product-card" v-for="(p,i) in filtered" :key="p.id" @click="selectedItem=p"><div class="product-image" :class="'product-tone-'+(i%4)"><img v-if="img(p)" :src="img(p)" :alt="p.name"/><span v-else class="product-placeholder"><ShoppingBag :size="36" stroke-width="1.5"/></span><span class="product-label">{{ $t('m2.common.apiMode') }}</span></div><div class="product-details"><small>{{p.region||$t('text.0425c05606fe')}}</small><strong>{{displayName(p)}}</strong><div class="product-tail"><b>{{money(p.price,p.currency)}}</b><span>{{ $t('text.dfd6b7aab1c8') }} {{p.stock??$t('m2.catalog.stockUnknown')}}</span></div></div></button></div></main>
</template>
