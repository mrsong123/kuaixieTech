<script setup lang="ts">
import SettingsLayout from '../../../components/SettingsLayout.vue';
import SettingsRow from '../../../components/SettingsRow.vue';
function password(){uni.navigateTo({url:'/pages/account-security/index'})}
</script>
<template>
 <SettingsLayout titleKey="settings2.account" subtitleKey="settings2.accountDesc">
 <view class="settings-group"><SettingsRow labelKey="settings2.password" descKey="settings2.passwordDesc" @press="password"/><SettingsRow labelKey="settings2.rebind" descKey="settings2.serverProfile" :disabled="true"/></view>
 <view class="settings-note">{{ $t('settings2.accountNotice') }}</view>
 </SettingsLayout>
</template>
