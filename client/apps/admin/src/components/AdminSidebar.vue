<script setup lang="ts">
import {LayoutDashboard,ClipboardCheck,ArrowLeftRight,TrendingUp,Settings2,Code2,ShieldCheck,Signal,Command,ChevronRight} from 'lucide-vue-next';
import {useAdminContext} from '../stores/context';
const {section,sidebarOpen,docsCount,go,activeGroup}=useAdminContext();
</script>
<template>
<aside class="sidebar" :class="{opened:sidebarOpen}">
  <button class="admin-brand" @click="go('overview')"><span class="brand-symbol"><Command :size="20"/></span><span><strong>{{ $t('text.db680ac702a7') }}</strong><small>{{ $t('text.6b03ccc830f8') }}</small></span></button>
  <div class="sidebar-menu">
    <p class="side-caption">{{ $t('admin.console.navCaption') }}</p>
    <button :class="{active:activeGroup==='core'}" @click="go('overview')"><LayoutDashboard/><span>{{ $t('admin.group.core') }}</span><ChevronRight class="side-chevron"/></button>
    <button :class="{active:activeGroup==='review'}" @click="go('carrierKyc')"><ClipboardCheck/><span>{{ $t('admin.group.review') }}</span><ChevronRight class="side-chevron"/></button>
    <button :class="{active:activeGroup==='trade'}" @click="go('orders')"><ArrowLeftRight/><span>{{ $t('admin.group.trade') }}</span><ChevronRight class="side-chevron"/></button>
    <button :class="{active:activeGroup==='growth'}" @click="go('users')"><TrendingUp/><span>{{ $t('admin.group.growth') }}</span><ChevronRight class="side-chevron"/></button>
    <p class="side-caption after">{{ $t('text.29d43743c43b') }}</p>
    <button :class="{active:section==='all'}" @click="go('all')"><Code2/><span>{{ $t('text.0d97a269629a') }}</span><span class="side-count">{{ docsCount }}</span></button>
    <button :class="{active:section==='settings'}" @click="go('settings')"><Settings2/><span>{{ $t('text.b935702d5314') }}</span></button>
  </div>
  <div class="sidebar-tip"><div class="tip-icon"><ShieldCheck :size="21"/></div><b>{{ $t('text.d948a44bb8c5') }}</b><p>{{ $t('text.3cafc0afca63') }}</p><span>{{ $t('text.d9dd56ca8ae3') }}</span></div>
  <div class="sidebar-bottom"><span class="avatar">K</span><div><b>{{ $t('text.adfc2d349ee6') }}</b><small>{{ $t('text.c9274041d876') }}</small></div><Signal :size="17"/></div>
</aside>
</template>
