<script setup lang="ts">
import {ref,onBeforeUnmount} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,subscribeSession} from '@kuaixie/sdk';
import SavedCommerceMobile from '../../components/SavedCommerceMobile.vue';
const scope=ref(0);let session=getBearer();
const unsubscribe=subscribeSession(()=>{session=getBearer();scope.value++});onBeforeUnmount(unsubscribe);
onShow(()=>{if(session!==getBearer()){session=getBearer();scope.value++}});
</script>
<template><SavedCommerceMobile :key="scope" kind="favorites"/></template>
