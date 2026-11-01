<script setup lang="ts">
import {onMounted,ref} from 'vue';
import type {ApiClient,PublicAnnouncement,PublicLegalDocument} from '@kuaixie/sdk';
import {uiErrorMessage} from '@kuaixie/i18n';
const props=defineProps<{client:ApiClient;documentKey?:string}>();const rows=ref<PublicAnnouncement[]>([]),doc=ref<PublicLegalDocument|null>(null),error=ref('');
onMounted(async()=>{try{if(props.documentKey)doc.value=await props.client.prototype.legalDocument(props.documentKey);else rows.value=await props.client.prototype.announcements(100)}catch(e){error.value=uiErrorMessage(e)}});
</script>
<template><section class="business-card"><p v-if="error" role="alert">{{error}}</p><template v-if="doc"><h1>{{doc.title}}</h1><small>{{doc.version}}</small><p>{{doc.body_md}}</p></template><article v-for="row in rows" :key="row.id" class="business-item"><h2>{{row.title}}</h2><p>{{row.body}}</p></article></section></template>
