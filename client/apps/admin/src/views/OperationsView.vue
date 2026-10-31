<script setup lang="ts">
import ApiWorkbench from '@kuaixie/ui/ApiWorkbench.vue';
import {useAdminContext} from '../stores/context';
const {api,config,filteredGroupOps}=useAdminContext();
</script>
<template>
<div class="admin-view">
  <div class="operations-overview">
    <div class="overview-left">
      <span class="sub-kicker">{{ $t('text.80eb1d10bb22') }}</span>
      <h2>{{ $t('text.fe53b51702fa') }}</h2>
      <p>{{ $t('text.3b6ef811b85a') }} {{filteredGroupOps.length}} {{ $t('text.8263c5f8b32a') }}</p>
    </div>
  </div>
  <ApiWorkbench :client="api" :groups="config.groups.length?config.groups:undefined" :title="$t('text.05411d04cd49')"/>
</div>
</template>
