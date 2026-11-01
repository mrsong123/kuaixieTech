<script setup lang="ts">
import {onMounted,onUnmounted,ref} from 'vue';
import {Globe2,KeyRound,Menu} from 'lucide-vue-next';
import {useAdminContext} from '../stores/context';
const {locale,changeLocale,sidebarOpen,sectionTitle,go}=useAdminContext();
const now=ref(new Date());
let tick=0;
onMounted(()=>{tick=window.setInterval(()=>{now.value=new Date()},1000)});
onUnmounted(()=>window.clearInterval(tick));
function clock(){
  return now.value.toLocaleString(locale.value==='en-US'?'en-GB':'zh-CN',{hour12:false,month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',second:'2-digit'});
}
</script>
<template>
<header class="topbar">
  <div class="topbar-left">
    <button class="sidebar-toggle" @click="sidebarOpen=!sidebarOpen"><Menu/></button>
    <strong>{{ sectionTitle }}</strong>
    <span class="desk-clock">{{ clock() }}</span>
  </div>
  <div class="topbar-right">
    <label class="locale-control">
      <Globe2 :size="15"/>
      <select :value="locale" :aria-label="$t('common.locale')" @change="changeLocale">
        <option value="zh-CN">{{ $t('common.lang.zh') }}</option>
        <option value="en-US">{{ $t('common.lang.en') }}</option>
      </select>
    </label>
    <span class="api-online"><span/>{{ $t('admin.desk.session') }}</span>
    <button class="topbar-button" @click="go('settings')"><KeyRound :size="16"/>{{ $t('admin.nav.settings') }}</button>
  </div>
</header>
</template>
