<script setup lang="ts">
import PreferencesPanel from '@kuaixie/ui/PreferencesPanel.vue';
import {useAdminContext} from '../stores/context';
const {go,token,showToken,apiBaseText,applyToken,removeToken}=useAdminContext();
</script>
<template>
<div class="admin-view">
  <PreferencesPanel/>
  <section class="surface settings-card"><h2>{{$t('admin.nav.integrations')}}</h2><p>{{$t('admin.integrations.desc')}}</p><button class="primary-button" @click="go('integrations')">{{$t('admin.nav.integrations')}}</button></section>
  <div class="settings-grid">
    <section class="surface settings-card">
      <span class="sub-kicker">{{ $t('text.85da46fb9d6b') }}</span>
      <h2>{{ $t('text.073518f755ad') }}</h2>
      <p>{{ $t('text.0a686490612c') }}</p>
      <label>{{ $t('text.94029d4f77b2') }}</label>
      <div class="token-field">
        <input v-model="token" :type="showToken?'text':'password'" autocomplete="off" placeholder="eyJhbGciOi…"/>
        <button @click="showToken=!showToken">{{showToken?$t('text.bb0e7e01aa8d'):$t('text.71b6771bc789')}}</button>
      </div>
      <div class="setting-actions">
        <button class="primary-button" @click="applyToken">{{ $t('text.6815f850c750') }}</button>
        <button class="soft-button" @click="removeToken">{{ $t('text.c0daff09413c') }}</button>
      </div>
    </section>
    <section class="surface settings-card">
      <span class="sub-kicker">{{ $t('text.28332a9ecd4e') }}</span>
      <h2>{{ $t('text.d7b305fce4d6') }}</h2>
      <div class="setting-info"><span>{{ $t('text.8fcb202924ad') }}</span><code>{{apiBaseText}}</code></div>
      <div class="setting-info"><span>{{ $t('text.c4692938ec46') }}</span><code>{{ $t('text.27e87c6f8400') }}</code></div>
      <div class="setting-info"><span>{{ $t('text.1767ee64000c') }}</span><code>{{ $t('text.87f70cf262f8') }}</code></div>
      <p>{{ $t('text.1de44344774c') }}</p>
    </section>
  </div>
</div>
</template>
