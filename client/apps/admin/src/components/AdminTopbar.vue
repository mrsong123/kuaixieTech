<script setup lang="ts">
import {ChevronRight,Globe2,KeyRound,Menu,Search} from 'lucide-vue-next';
import {useAdminContext} from '../stores/context';
const {locale,changeLocale,section,sidebarOpen,nav,config,go}=useAdminContext();
</script>
<template>
<header class="topbar">
  <div class="topbar-left">
    <button class="sidebar-toggle" @click="sidebarOpen=!sidebarOpen"><Menu/></button>
    <span>{{ $t('text.1bf3defde730') }}</span>
    <ChevronRight :size="15"/>
    <strong>{{ $t(nav.find(x=>x[0]===section)?.[1] || config.title) }}</strong>
  </div>
  <div class="topbar-right">
    <label class="locale-control">
      <Globe2 :size="15"/>
      <select :value="locale" :aria-label="$t('common.locale')" @change="changeLocale">
        <option value="zh-CN">{{ $t('common.lang.zh') }}</option>
        <option value="en-US">{{ $t('common.lang.en') }}</option>
      </select>
    </label>
    <span class="api-online"><span/>{{ $t('text.36b78b53be7c') }}</span>
    <button class="topbar-button" @click="go('all')"><Search :size="17"/>{{ $t('text.a39dbed39da2') }}</button>
    <button class="avatar-button" @click="go('settings')"><KeyRound :size="17"/></button>
  </div>
</header>
</template>
