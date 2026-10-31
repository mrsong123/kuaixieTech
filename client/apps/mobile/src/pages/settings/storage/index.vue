<script setup lang="ts">
import SettingsLayout from '../../../components/SettingsLayout.vue';
import {useStorageSettings} from '../../../stores/useStorageSettings';
const {keys,sizeKb,limitKb,clearable,error,reload,clear}=useStorageSettings();
</script>
<template>
 <SettingsLayout titleKey="settings2.storage" subtitleKey="settings2.storageDesc">
  <view class="settings-group"><view class="settings-kv"><text>{{ $t('settings2.storageUsed') }}</text><text>{{sizeKb.toFixed(1)}} KB</text></view><view class="settings-kv"><text>{{ $t('settings2.storageQuota') }}</text><text>{{limitKb.toFixed(0)}} KB</text></view><view class="settings-kv"><text>{{ $t('settings2.storageItems') }}</text><text>{{ keys.length }}</text></view><view class="settings-kv"><text>{{ $t('settings2.storageClearable') }}</text><text>{{ clearable.length }}</text></view></view>
  <view v-if="error" class="settings-note">{{error}}</view>
  <button class="settings-action" :disabled="!clearable.length" @click="clear">{{ $t('settings2.clearCache') }}</button>
  <view class="settings-note">{{ $t('settings2.storageSafety') }}</view>
  <button class="settings-action settings-action-secondary" @click="reload">{{ $t('settings2.refresh') }}</button>
 </SettingsLayout>
</template>
