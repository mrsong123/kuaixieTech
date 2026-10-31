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
<header class="site-header"><div class="container nav-inner"><button class="brand" @click="nav('home')"><span class="brand-mark"><Navigation :size="19" :stroke-width="2.5"/></span><span>{{ $t('text.c048090e129d') }}<span class="brand-green">{{ $t('text.2e300353364d') }}</span><small>{{ $t('text.bdcaefd66669') }}</small></span></button>
   <nav class="nav-links" :class="{open:menu}"><button v-for="item in navItems" :key="item[0]" :class="{active:page===item[0]}" @click="nav(item[0])">{{ $t(item[1]) }}</button></nav>
   <div class="nav-right"><label class="locale-control"><Globe2 :size="15"/><select :value="locale" :aria-label="$t('common.locale')" @change="changeLocale"><option value="zh-CN">{{ $t('common.lang.zh') }}</option><option value="en-US">{{ $t('common.lang.en') }}</option></select></label><button class="display-settings-trigger" :aria-label="$t('preferences.open')" @click="showDisplaySettings=!showDisplaySettings"><Settings2 :size="17"/><span class="display-settings-label">{{ $t('preferences.open') }}</span></button><span class="live-dot"><i/>{{loadError?$t('m2.common.connectionIssue'):$t('m2.common.apiMode')}}</span><button class="auth-btn" @click="showAuth=true"><Fingerprint :size="17"/> {{ $t('text.f313cfdd2764') }}</button><button class="mobile-menu" @click="menu=!menu"><Menu v-if="!menu"/><X v-else/></button></div></div></header>
  <div v-if="showDisplaySettings" class="container display-settings-block"><PreferencesPanel/><button class="display-settings-trigger" @click="showDisplaySettings=false">{{ $t('preferences.dismiss') }}</button></div>
  
</template>
