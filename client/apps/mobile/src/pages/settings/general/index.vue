<script setup lang="ts">
import SettingsLayout from '../../../components/SettingsLayout.vue';
import SettingsRow from '../../../components/SettingsRow.vue';
import {useAppearanceSettings} from '../../../stores/useAppearanceSettings';
const {prefs,onCare,onMotion}=useAppearanceSettings();
function appearance(){uni.navigateTo({url:'/pages/settings/appearance/index'})}
</script>
<template>
 <SettingsLayout titleKey="settings2.general" subtitleKey="settings2.generalDesc">
  <view class="settings-group"><SettingsRow labelKey="settings2.appearance" descKey="settings2.appearanceDesc" @press="appearance" />
  <view class="settings-control"><view class="settings-switch"><text>{{ $t('preferences.care') }}</text><switch :checked="prefs.care" :color="prefs.accent" @change="onCare"/></view></view>
  <view class="settings-control"><view class="settings-switch"><text>{{ $t('preferences.motion') }}</text><switch :checked="prefs.reduceMotion" :color="prefs.accent" @change="onMotion"/></view></view></view>
  <view class="settings-note">{{ $t('settings2.localOnly') }}</view>
 </SettingsLayout>
</template>
