<script setup lang="ts">
import {Fingerprint, Globe2, Menu, Navigation, Search, Settings2, X} from 'lucide-vue-next';
import PreferencesPanel from '@kuaixie/ui/PreferencesPanel.vue';
import {useSiteContext} from '../stores/context';
const {locale,showDisplaySettings,changeLocale,page,menu,loadError,showAuth,navItems,nav,signedIn,logout,search,searchScope,submitSearch}=useSiteContext();
</script>
<template>
<header class="site-header">
  <div class="container masthead">
    <button class="brand" type="button" @click="nav('home')">
      <span class="brand-mark"><Navigation :size="19" :stroke-width="2.5"/></span>
      <span>{{ $t('text.c048090e129d') }}<span class="brand-green">{{ $t('text.2e300353364d') }}</span><small>{{ $t('text.bdcaefd66669') }}</small></span>
    </button>
    <form class="mast-search" @submit.prevent="submitSearch">
      <label class="mast-scope">
        <span class="sr-only">{{ $t('site.searchScope') }}</span>
        <select v-model="searchScope">
          <option value="all">{{ $t('site.searchAll') }}</option>
          <option value="products">{{ $t('pc.nav.products') }}</option>
          <option value="trips">{{ $t('pc.nav.trips') }}</option>
          <option value="demands">{{ $t('pc.nav.route') }}</option>
        </select>
      </label>
      <input v-model="search" type="search" :placeholder="$t('home.searchPlaceholder')" :aria-label="$t('home.searchPlaceholder')"/>
      <button type="submit" :aria-label="$t('site.searchGo')"><Search :size="18"/></button>
    </form>
    <div class="nav-right">
      <label class="locale-control">
        <Globe2 :size="15"/>
        <select :value="locale" :aria-label="$t('common.locale')" @change="changeLocale">
          <option value="zh-CN">{{ $t('common.lang.zh') }}</option>
          <option value="en-US">{{ $t('common.lang.en') }}</option>
        </select>
      </label>
      <button class="display-settings-trigger" type="button" :aria-label="$t('preferences.open')" @click="showDisplaySettings=!showDisplaySettings">
        <Settings2 :size="17"/><span class="display-settings-label">{{ $t('preferences.open') }}</span>
      </button>
      <span class="live-dot"><i/>{{loadError?$t('m2.common.connectionIssue'):$t('m2.common.apiMode')}}</span>
      <button v-if="signedIn" class="auth-btn" type="button" @click="nav('me')">{{ $t('mobile.tab.me') }}</button>
      <button v-if="signedIn" class="auth-btn" type="button" @click="logout">{{ $t('site.logout') }}</button>
      <button v-else class="auth-btn" type="button" @click="showAuth=true"><Fingerprint :size="17"/> {{ $t('site.loginAction') }}</button>
      <button class="mobile-menu" type="button" @click="menu=!menu"><Menu v-if="!menu"/><X v-else/></button>
    </div>
  </div>
  <div class="site-subnav">
    <div class="container">
      <nav class="nav-links" :class="{open:menu}">
        <button v-for="item in navItems" :key="item[0]" type="button" :class="{active:page===item[0]}" @click="nav(item[0])">{{ $t(item[1]) }}</button>
      </nav>
    </div>
  </div>
</header>
<div v-if="showDisplaySettings" class="container display-settings-block">
  <PreferencesPanel/>
  <button class="display-settings-trigger" type="button" @click="showDisplaySettings=false">{{ $t('preferences.dismiss') }}</button>
</div>
</template>
