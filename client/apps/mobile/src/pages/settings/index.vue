<script setup lang="ts">
import SettingsLayout from '../../components/SettingsLayout.vue';
import SettingsRow from '../../components/SettingsRow.vue';
import {settingsGroups,openSettings} from '../../features/settings/navigation';
</script>
<template>
 <SettingsLayout titleKey="settings2.title" subtitleKey="settings2.intro">
  <template v-for="group in settingsGroups" :key="group.title"><view class="settings-section-title">{{ $t(group.title) }}</view><view class="settings-group">
   <SettingsRow v-for="item in group.items" :key="item.id" :labelKey="item.label" :descKey="item.desc" @press="openSettings(item.id)" />
  </view></template>
 </SettingsLayout>
</template>
