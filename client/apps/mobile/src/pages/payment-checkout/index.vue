<script setup lang="ts">
import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {api} from '../../api/client';
import {uiErrorMessage,t} from '@kuaixie/i18n';
import {usePrivatePageSession,privateWebTarget} from '../../utils/usePrivatePageSession';
const url=ref(''),error=ref('');
const scope=usePrivatePageSession(()=>{url.value='';error.value=t('mobileBusiness.sessionChanged')});
onLoad(async params=>{const owner=scope.capture(),id=String(params?.id||'');if(!owner.token){error.value='pay.checkoutUnavailable';return}try{const payment=await api.payments.status(id);if(!scope.current(owner))return;const target=payment.checkout?.kind==='redirect'?privateWebTarget(payment.checkout.value):'';if(payment.id===id&&payment.status!=='paid'&&target)url.value=target;else error.value='pay.checkoutUnavailable'}catch(e){if(scope.current(owner))error.value=uiErrorMessage(e)}});
</script>
<template><view><web-view v-if="url" :src="url"/><view v-else>{{error==='pay.checkoutUnavailable'?$t(error):error||$t('account.loading')}}</view></view></template>
