<script setup lang="ts">
import {ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer} from '@kuaixie/sdk';
import ProductPublishingMobile from '../../components/ProductPublishingMobile.vue';
const scope=ref(0);let session=getBearer();onShow(()=>{const current=getBearer();if(current!==session){session=current;scope.value++}});
</script>
<template><view class="original-subpage"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><ProductPublishingMobile :key="scope"/></view></template>
