<script setup lang="ts">
import {useAddressSelection} from '@kuaixie/ui/useSavedCommerce';
import {uniEventValue} from '../utils/uniEvent';
import {api} from '../api/client';
const emit=defineEmits<{selected:[value:{contactName:string;contactPhone:string;address:string}]}>();
const p=useAddressSelection(api,value=>emit('selected',value));
function pick(event:unknown){const row=p.rows.value[Number(uniEventValue(event))];if(row)p.choose(row.id)}
</script>
<template><view><button @click="p.load" :disabled="p.busy.value">{{$t('saved.chooseAddress')}}</button><picker v-if="p.rows.value.length" :range="p.rows.value.map(row=>row.contact_name+' · '+(row.city||'')+' '+row.detail)" @change="pick"><view class="op-row">{{$t('saved.chooseAddress')}} ›</view></picker><view v-if="p.error.value" class="op-error">{{p.error.value}}</view></view></template>
