<script setup lang="ts">
import {Layers} from 'lucide-vue-next';
import {useAdminContext} from '../stores/context';
const {section,config,filteredGroupOps}=useAdminContext();
</script>
<template>
<div class="admin-header">
  <div class="page-header">
    <div>
      <span class="kicker">{{ $t(config.kicker) }}</span>
      <h1>{{ $t(config.title) }}</h1>
      <p>{{ $t(config.desc) }}</p>
    </div>
    <div v-if="section==='overview'" class="header-graphic"><div class="header-symbol"><Layers :size="40"/></div><div class="header-glow"/></div>
    <div v-else class="header-badge"><span>{{filteredGroupOps.length}}</span> {{ $t('text.ac3e877d2f0d') }}</div>
  </div>
</div>
</template>
