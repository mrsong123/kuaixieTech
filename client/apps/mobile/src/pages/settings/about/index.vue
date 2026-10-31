<script setup lang="ts">import SettingsLayout from '../../../components/SettingsLayout.vue';
import OriginalMdiIcon from '../../../components/OriginalMdiIcon.vue';
function legal(type:'agreement'|'privacy'|'collection'){uni.navigateTo({url:'/pages/legal-doc/index?type='+type})}
</script>
<template><SettingsLayout titleKey="settings2.about" subtitleKey="settings2.aboutDesc">
 <view class="settings-group"><view class="settings-kv"><text>{{ $t('settings2.platform') }}</text><text>{{ $t('settings2.platformName') }}</text></view><view class="settings-kv"><text>{{ $t('settings2.client') }}</text><text>{{ $t('settings2.mobileTechnology') }}</text></view></view>
 <view class="settings-group v14-legal-links">
 <view class="settings-row" @click="legal('agreement')"><view class="v14-legal-icon"><OriginalMdiIcon name="file-document-outline" /></view><text class="settings-row-label">{{$t('v14.agreement')}}</text><text class="settings-row-arrow">›</text></view>
 <view class="settings-row" @click="legal('privacy')"><view class="v14-legal-icon"><OriginalMdiIcon name="shield-lock-outline" /></view><text class="settings-row-label">{{$t('v14.privacy')}}</text><text class="settings-row-arrow">›</text></view>
 <view class="settings-row" @click="legal('collection')"><view class="v14-legal-icon"><OriginalMdiIcon name="format-list-bulleted-type" /></view><text class="settings-row-label">{{$t('v14.collection')}}</text><text class="settings-row-arrow">›</text></view>
 </view>
 <view class="settings-note">{{ $t('settings2.legalNotice') }}</view>
 </SettingsLayout></template>
