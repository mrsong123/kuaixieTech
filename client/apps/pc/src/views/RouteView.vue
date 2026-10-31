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
<main  class="container inner-page route-page">
    <div class="page-intro"><span class="section-kicker">{{ $t('text.39378b53bdba') }}</span><h1>{{ $t('text.ad2c728dd59f') }}</h1><p>{{ $t('text.1ea9a98e0758') }}</p></div>
    <section class="panel" style="max-width:850px;margin:35px auto;padding:36px">
      <div class="panel-heading"><span class="step-marker">✓</span><div><h2>{{ $t('text.f97b88721f60') }}</h2><p>{{ $t('text.6d14c2777925') }}</p></div></div>
      <div class="action-row"><button class="btn-green" @click="nav('trips')">{{ $t('text.0bdaaf9d5d39') }}</button><button class="btn-outline" @click="nav('products')">{{ $t('text.cecba109ff9a') }}</button></div>
      <div class="info-strip" style="margin-top:30px"><ShieldCheck :size="20"/>{{ $t('text.8150d2ba9fc2') }}</div>
    </section>
  </main>
</template>
