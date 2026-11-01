<script setup lang="ts">
import {ref} from 'vue';
import {getBearer} from '@kuaixie/sdk';
import {onLoad,onShow} from '@dcloudio/uni-app';
import PaymentRefundsMobile from '../../components/PaymentRefundsMobile.vue';
import {preferenceRef as prefs} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../../utils/prototype';
const id=ref(''),scope=ref(0);let session=getBearer();onShow(()=>{const current=getBearer();if(current!==session){session=current;scope.value++}});onLoad(q=>{id.value=String(q?.id||'')});
</script>
<template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><PaymentRefundsMobile :key="scope" :refund-id="id"/></scroll-view></template>
