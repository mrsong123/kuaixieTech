<script setup lang="ts">
import {useAdminContext} from '../stores/context';
const {loginChannel,loginContact,loginPassword,loginBusy,loginError,login}=useAdminContext();
</script>
<template>
  <div class="login-wrap">
    <aside class="login-poster">
      <span class="sub-kicker">{{ $t('admin.desk.manifest') }}</span>
      <h1 class="login-mark">{{ $t('text.db680ac702a7') }}</h1>
      <p>{{ $t('admin.desk.route') }}</p>
      <p>{{ $t('admin.login.desc') }}</p>
      <div class="login-stamp">{{ $t('admin.desk.shift') }}<br/>{{ $t('admin.login.title') }}</div>
    </aside>
    <section class="login-card">
      <span class="sub-kicker">{{ $t('admin.login.kicker') }}</span>
      <h2>{{ $t('admin.login.title') }}</h2>
      <p>{{ $t('admin.login.hint') }}</p>
      <div class="chip-row">
        <button type="button" :class="{on:loginChannel==='phone'}" @click="loginChannel='phone'">{{ $t('admin.login.phone') }}</button>
        <button type="button" :class="{on:loginChannel==='email'}" @click="loginChannel='email'">{{ $t('admin.login.email') }}</button>
      </div>
      <label>{{ loginChannel==='phone'?$t('admin.login.phone'):$t('admin.login.email') }}</label>
      <input v-model="loginContact" autocomplete="username" :placeholder="loginChannel==='phone'?$t('admin.login.phonePh'):$t('admin.login.emailPh')"/>
      <label>{{ $t('admin.login.password') }}</label>
      <input v-model="loginPassword" type="password" autocomplete="current-password" :placeholder="$t('admin.login.passwordPh')" @keydown.enter="login"/>
      <p v-if="loginError" class="console-error">{{ loginError }}</p>
      <button class="primary-button" :disabled="loginBusy" @click="login">{{ $t('admin.login.submit') }}</button>
    </section>
  </div>
</template>
