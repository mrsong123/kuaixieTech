<script setup lang="ts">
import {useAdminContext} from '../stores/context';
const {config,sectionTitle}=useAdminContext();
</script>
<template>
<div class="admin-header">
  <div class="page-header">
    <div>
      <span class="kicker">{{ $t(config.kicker) }}</span>
      <h1>{{ sectionTitle }}</h1>
      <p>{{ $t(config.desc) }}</p>
    </div>
    <div class="header-badge"><span>{{ $t('admin.desk.shift') }}</span> {{ $t('admin.desk.manifest') }}</div>
  </div>
</div>
</template>
