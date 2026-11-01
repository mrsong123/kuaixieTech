<script setup lang="ts">
import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {api} from '../../api/client';
import {uiErrorMessage} from '@kuaixie/i18n';
const url=ref(''),error=ref('');
onLoad(async params=>{try{const payment=await api.payments.status(String(params?.id||''));if(payment.status!=='paid'&&payment.checkout?.kind==='redirect'&&payment.checkout.value.startsWith('https://'))url.value=payment.checkout.value;else error.value='pay.checkoutUnavailable'}catch(e){error.value=uiErrorMessage(e)}});
</script>
<template><view><web-view v-if="url" :src="url"/><view v-else>{{error==='pay.checkoutUnavailable'?$t(error):error||$t('account.loading')}}</view></view></template>
