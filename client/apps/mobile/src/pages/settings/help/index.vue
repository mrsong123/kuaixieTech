<script setup lang="ts">
import SettingsLayout from '../../../components/SettingsLayout.vue';
import SettingsRow from '../../../components/SettingsRow.vue';
function message(){uni.switchTab({url:'/pages/messages/index'})}
</script>
<template><SettingsLayout titleKey="settings2.help" subtitleKey="settings2.helpDesc">
 <view class="settings-group"><SettingsRow labelKey="settings2.contactSupport" descKey="settings2.helpNotice" @press="message" /></view>
 <view class="settings-note">{{ $t('settings2.helpNotice') }}</view>
 </SettingsLayout></template>
