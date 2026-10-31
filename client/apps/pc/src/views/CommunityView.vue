<script setup lang="ts">
import {ArrowUpRight,ArrowRight,ArrowLeft,Search,MapPin,Navigation,PackageOpen,ShieldCheck,ChevronRight,Route,Gift,Heart,Compass,Play,ShoppingBag,Clock3,Users,Check,AlertCircle,RefreshCw,Tag,SlidersHorizontal,Globe2,Menu, X, Zap, Fingerprint, Settings2} from 'lucide-vue-next';import {t, formatAmount, formatDate, transportLabel, localizedField, setLocale, getLocale, operationLabel, groupLabel, uiErrorMessage} from '@kuaixie/i18n';
import {tripAvailability} from '@kuaixie/sdk';
import ApiWorkbench from '@kuaixie/ui/ApiWorkbench.vue';
import PreferencesPanel from '@kuaixie/ui/PreferencesPanel.vue';
import VoiceDraft from '@kuaixie/ui/VoiceDraft.vue';
import ComparisonPanel from '@kuaixie/ui/ComparisonPanel.vue';
import {useSiteContext} from '../stores/context';
const {locale,showDisplaySettings,changeLocale,api,page,menu,products,trips,categories,busy,loadError,search,activeCategory,tripSort,selectedItem,credentials,showAuth,toast,heroStats,navItems,nav,displayName,img,filtered,money,date,message,refresh,changeTripSort,selectedProduct,selectedTrip,selectedCategoryName,useToken}=useSiteContext();
</script>
<template>
<main  class="container inner-page"><div class="page-intro"><span class="section-kicker">{{ $t('text.b42cee6c73a9') }}</span><h1>{{ $t('text.73ddaa59c277') }}</h1><p>{{ $t('text.c9492b667341') }}</p></div><div class="coming-layout"><div class="coming-art"><div class="coming-sun"/><span>{{ $t('text.08e7508d2dd7') }}<br/><em>{{ $t('text.2d18291d4e48') }}</em></span><div class="coming-caption">{{ $t('text.36b49b12da95') }}</div></div><div class="coming-copy"><span class="badge-planned">{{ $t('text.11516547e371') }}</span><h2>{{ $t('text.9d9ba6565a7c') }}<br/>{{ $t('text.c32bada10dcc') }}</h2><p>{{ $t('text.9cf062aa7473') }} <b>{{ $t('text.938c03c43a62') }}</b>、<b>{{ $t('text.bdb43b140571') }}</b> {{ $t('text.71c06f6e56fa') }}</p><div class="coming-list"><span><Check :size="16"/>{{ $t('text.b54cb1ebcb74') }}</span><span><AlertCircle :size="16"/>{{ $t('text.62001c43971c') }}</span><span><ArrowRight :size="16"/>{{ $t('text.3a53d503af54') }}</span></div><button class="btn-green" @click="nav('workspace')">{{ $t('text.b11a9099ce80') }} <ArrowUpRight :size="17"/></button></div></div></main>
</template>
