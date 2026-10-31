<script setup lang="ts">import SettingsLayout from '../../../components/SettingsLayout.vue';import SettingsRow from '../../../components/SettingsRow.vue';
const items=['order','logistics','commission','system'];
</script>
<template><SettingsLayout titleKey="settings2.notification" subtitleKey="settings2.serverNotification"><view class="settings-group"><SettingsRow v-for="id in items" :key="id" :labelKey="'settings2.notification.'+id" :arrow="false" :disabled="true" /></view><view class="settings-note">{{ $t('settings2.serverRequired') }}</view></SettingsLayout></template>
