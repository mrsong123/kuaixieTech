<script setup lang="ts">
import type {ApiClient} from '@kuaixie/sdk';
import {useAddressSelection} from './useSavedCommerce';
const props=defineProps<{client:ApiClient}>();const emit=defineEmits<{selected:[value:{contactName:string;contactPhone:string;address:string}]}>();
const p=useAddressSelection(props.client,value=>emit('selected',value));
</script>
<template><div><button type="button" :disabled="p.busy.value" @click="p.load">{{$t('saved.chooseAddress')}}</button><select v-if="p.rows.value.length" :aria-label="$t('saved.chooseAddress')" @change="p.choose(($event.target as HTMLSelectElement).value)"><option value="">{{$t('saved.chooseAddress')}}</option><option v-for="row in p.rows.value" :key="row.id" :value="row.id">{{row.contact_name}} · {{row.city}} {{row.detail}}</option></select><p v-if="p.error.value" role="alert">{{p.error.value}}</p></div></template>
