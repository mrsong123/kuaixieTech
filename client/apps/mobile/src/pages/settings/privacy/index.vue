<script setup lang="ts">
import SettingsLayout from '../../../components/SettingsLayout.vue';
import {ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {t} from '@kuaixie/i18n';
const permissionScopes=[{key:'scope.userLocation',label:'settings2.permission.location'},{key:'scope.camera',label:'settings2.permission.camera'},{key:'scope.record',label:'settings2.permission.microphone'},{key:'scope.writePhotosAlbum',label:'settings2.permission.album'}];
const status=ref<Record<string,boolean|undefined>>({});const available=ref(false);
function reload(){try{uni.getSetting({success(res){available.value=true;const auth=res.authSetting??{};const next:Record<string,boolean|undefined>={};for(const item of permissionScopes)next[item.key]=auth[item.key];status.value=next},fail(){available.value=false}})}catch{available.value=false}}
function openSystem(){try{uni.openSetting({success:reload,fail(){uni.showToast({title:t('settings2.permissionUnsupported'),icon:'none'})}})}catch{uni.showToast({title:t('settings2.permissionUnsupported'),icon:'none'})}}
onShow(reload);
</script>
<template>
 <SettingsLayout titleKey="settings2.privacy" subtitleKey="settings2.privacyDesc">
  <view v-if="available" class="settings-group"><view v-for="p in permissionScopes" :key="p.key" class="settings-kv"><text>{{ $t(p.label) }}</text><text>{{status[p.key]===true?$t('settings2.allowed'):status[p.key]===false?$t('settings2.denied'):$t('settings2.unset')}}</text></view></view>
  <view v-else class="settings-note">{{ $t('settings2.permissionUnsupported') }}</view>
  <button v-if="available" class="settings-action" @click="openSystem">{{ $t('settings2.systemPermission') }}</button>
  <view class="settings-note">{{ $t('settings2.privacyNotice') }}</view>
 </SettingsLayout>
</template>
