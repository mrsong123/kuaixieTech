<script setup lang="ts">
import SettingsLayout from '../../../components/SettingsLayout.vue';
import {useAppearanceSettings} from '../../../stores/useAppearanceSettings';
const {prefs,accent,locale,themeIndex,fontIndex,themeLabelKey,fontLabelKey,onTheme,onFont,onScale,onCare,onMotion,onLocale,onAccent,reset}=useAppearanceSettings();
</script>
<template>
 <SettingsLayout titleKey="settings2.appearance" subtitleKey="settings2.appearanceDesc">
  <view class="settings-group">
   <view class="settings-control"><text class="settings-label">{{ $t('preferences.theme') }}</text><picker :range="[$t('preferences.theme.system'),$t('preferences.theme.light'),$t('preferences.theme.dark')]" :value="themeIndex" @change="onTheme"><view class="settings-select">{{ $t(themeLabelKey) }}</view></picker></view>
   <view class="settings-control"><text class="settings-label">{{ $t('preferences.font') }}</text><picker :range="[$t('preferences.font.system'),$t('preferences.font.modern'),$t('preferences.font.reading')]" :value="fontIndex" @change="onFont"><view class="settings-select">{{ $t(fontLabelKey) }}</view></picker></view>
   <view class="settings-control"><text class="settings-label">{{ $t('preferences.size') }} · {{Math.round(prefs.scale*100)}}%</text><slider :value="Math.round(prefs.scale*100)" min="90" max="130" step="5" :activeColor="prefs.accent" @change="onScale" /></view>
   <view class="settings-control"><text class="settings-label">{{ $t('preferences.accent') }}</text><input v-model="accent" class="settings-input" maxlength="7" :placeholder="$t('settings2.hexPlaceholder')"/><button class="settings-action settings-action-secondary" @click="onAccent">{{ $t('settings2.applyColor') }}</button></view>
   <view class="settings-control"><view class="settings-switch"><text>{{ $t('preferences.care') }}</text><switch :checked="prefs.care" :color="prefs.accent" @change="onCare"/></view><view class="settings-switch"><text>{{ $t('preferences.motion') }}</text><switch :checked="prefs.reduceMotion" :color="prefs.accent" @change="onMotion"/></view></view>
  </view>
  <view class="settings-section-title">{{ $t('common.locale') }}</view>
  <view class="settings-group"><view class="settings-control"><picker :range="[$t('common.lang.zh'),$t('common.lang.en')]" :value="locale==='en-US'?1:0" @change="onLocale"><view class="settings-select">{{locale==='en-US'?$t('common.lang.en'):$t('common.lang.zh')}}</view></picker></view></view>
  <button class="settings-action settings-action-secondary" @click="reset">{{ $t('preferences.reset') }}</button>
  <view class="settings-note">{{ $t('preferences.saved') }}</view>
 </SettingsLayout>
</template>
