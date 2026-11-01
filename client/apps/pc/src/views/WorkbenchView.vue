<script setup lang="ts">
import {ArrowUpRight,ArrowRight,ArrowLeft,Search,MapPin,Navigation,PackageOpen,ShieldCheck,ChevronRight,Route,Gift,Heart,Compass,Play,ShoppingBag,Clock3,Users,Check,AlertCircle,RefreshCw,Tag,SlidersHorizontal,Globe2,Menu, X, Zap, Fingerprint, Settings2} from 'lucide-vue-next';import {t, formatAmount, formatDate, transportLabel, localizedField, setLocale, getLocale, operationLabel, groupLabel, uiErrorMessage} from '@kuaixie/i18n';
import {tripAvailability} from '@kuaixie/sdk';
import ApiWorkbench from '@kuaixie/ui/ApiWorkbench.vue';
import ProductPublishing from '@kuaixie/ui/ProductPublishing.vue';
import WalletFunding from '@kuaixie/ui/WalletFunding.vue';
import PreferencesPanel from '@kuaixie/ui/PreferencesPanel.vue';
import VoiceDraft from '@kuaixie/ui/VoiceDraft.vue';
import ComparisonPanel from '@kuaixie/ui/ComparisonPanel.vue';
import {useSiteContext} from '../stores/context';
const {locale,showDisplaySettings,changeLocale,api,page,menu,products,trips,categories,busy,loadError,search,activeCategory,tripSort,selectedItem,credentials,showAuth,toast,heroStats,navItems,nav,displayName,img,filtered,money,date,message,refresh,changeTripSort,selectedProduct,selectedTrip,selectedCategoryName,useToken}=useSiteContext();
</script>
<template>
<main  class="container inner-page"><ProductPublishing :client="api"/><WalletFunding :client="api"/><ApiWorkbench :client="api" mode="user" :title="$t('text.0ba06be163d4')"/></main>
</template>
