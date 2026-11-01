<script setup lang="ts">
import {ref} from 'vue';
import {onLoad,onShow} from '@dcloudio/uni-app';
import {getBearer} from '@kuaixie/sdk';
import TradeCasesMobile from '../../components/TradeCasesMobile.vue';
import {preferenceRef as prefs} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../../utils/prototype';
const scope=ref(0);let session=getBearer();onShow(()=>{const current=getBearer();if(current!==session){session=current;scope.value++}});
const id=ref('');onLoad(q=>{id.value=String(q?.id||q?.orderId||'')});
</script>
<template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><TradeCasesMobile :key="scope" :case-id="id"/></scroll-view></template>
