<script setup lang="ts">
import {onMounted,ref} from 'vue';
import {uiErrorMessage,formatAmount} from '@kuaixie/i18n';
import type {PublicDemand,PublicCommunityPost} from '@kuaixie/sdk';
import {useSiteContext} from '../stores/context';
const {api,nav}=useSiteContext();const demands=ref<PublicDemand[]>([]),posts=ref<PublicCommunityPost[]>([]),busy=ref(false),error=ref('');
async function load(){if(busy.value)return;busy.value=true;error.value='';const results=await Promise.allSettled([api.portal.demands(100),api.portal.communityFeed(100)]);if(results[0].status==='fulfilled')demands.value=results[0].value;else{demands.value=[];error.value=uiErrorMessage(results[0].reason)}if(results[1].status==='fulfilled')posts.value=results[1].value;else{posts.value=[];error.value=uiErrorMessage(results[1].reason)}busy.value=false}
onMounted(()=>{void load()});
</script>
<template><main class="business-layout"><section class="business-card"><h1>{{$t('portal.tabs.community')}}</h1><nav class="business-tabs"><button :disabled="busy" @click="load">{{$t('common.retry')}}</button><button @click="nav('publish')">{{$t('portal.publish.title')}}</button></nav><p v-if="error" role="alert">{{error}}</p><h2>{{$t('portal.publish.demand')}}</h2><article v-for="row in demands" :key="row.id" class="business-item"><h3>{{row.title}}</h3><p>{{row.description}}</p><p>{{row.city}} · {{formatAmount(row.expect_price,'CNY')}}</p><a :href="'#services/pk-quotes?demandId='+encodeURIComponent(row.id)">{{$t('fast.flow.pk_quotes')}}</a></article><h2>{{$t('portal.publish.life')}}</h2><article v-for="row in posts" :key="row.id" class="business-item"><strong>{{row.author_name}}</strong><p>{{row.caption}}</p><img v-if="row.media_kind==='image'&&row.media_url" :src="row.media_url" alt=""/><video v-if="row.media_kind==='video'&&row.media_url" :src="row.media_url" :poster="row.poster_url" controls/></article></section></main></template>
