<script setup lang="ts">
import type {ApiClient} from '@kuaixie/sdk';
import {useProductActions} from './useSavedCommerce';
const props=defineProps<{client:ApiClient;productId:string}>();const p=useProductActions(props.client,()=>props.productId);
</script>
<template><div><nav class="business-tabs"><button :disabled="p.busy.value" @click="p.perform('cart')">{{$t('saved.addCart')}}</button><button :disabled="p.busy.value" @click="p.perform('favorite')">{{$t('saved.addFavorite')}}</button></nav><p v-if="p.error.value" role="alert">{{p.error.value}}</p><p v-if="p.done.value" role="status">{{$t(p.done.value)}}</p></div></template>
