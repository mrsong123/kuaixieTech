<script setup lang="ts">
import {onMounted,onUnmounted,ref} from 'vue';
import type {ApiClient,SupportConversation,SupportMessage} from '@kuaixie/sdk';
import {uiErrorMessage} from '@kuaixie/i18n';
const props=defineProps<{client:ApiClient}>();const session=ref<SupportConversation|null>(null),rows=ref<SupportMessage[]>([]),draft=ref(''),busy=ref(false),error=ref('');let timer:ReturnType<typeof setInterval>|undefined;let alive=true;
async function refresh(){if(!session.value)return;try{const [status,messages]=await Promise.all([props.client.support.get(session.value.id),props.client.support.messages(session.value.id,80)]);if(alive){session.value=status;rows.value=messages}}catch(e){if(alive)error.value=uiErrorMessage(e)}}
async function connect(){busy.value=true;error.value='';try{const value=await props.client.support.open();if(alive){session.value=value;await refresh()}}catch(e){if(alive)error.value=uiErrorMessage(e)}finally{busy.value=false}}
async function send(){if(busy.value||!session.value||!draft.value.trim())return;busy.value=true;error.value='';try{await props.client.support.send(session.value.id,draft.value.trim());draft.value='';await refresh()}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
async function handoff(){if(!session.value||busy.value)return;busy.value=true;try{session.value=await props.client.support.handoff(session.value.id);await refresh()}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
onMounted(()=>{void connect();timer=setInterval(()=>{void refresh()},10000)});onUnmounted(()=>{alive=false;if(timer)clearInterval(timer)});
</script>
<template><section class="business-card"><h1>{{$t('portal.chat.title')}}</h1><p v-if="error" role="alert">{{error}}</p><button :disabled="busy" @click="connect">{{$t('common.retry')}}</button><button :disabled="busy||!session||session.status==='closed'" @click="handoff">{{$t('portal.chat.human')}}</button><p v-if="session">{{session.status}}</p><article v-for="row in rows" :key="row.id" class="business-item"><small>{{row.sender_type}}</small><p>{{row.content}}</p></article><form @submit.prevent="send"><label>{{$t('portal.chat.placeholder')}}<textarea v-model="draft" maxlength="2000" required/></label><button :disabled="busy||!session||session.status==='closed'">{{$t('portal.chat.send')}}</button></form></section></template>
