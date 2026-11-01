<script setup lang="ts">
import {ref} from 'vue';
import type {PaymentPlatform} from '@kuaixie/sdk';
let platform:PaymentPlatform='h5';
// #ifdef MP-WEIXIN
platform='mp_weixin';
// #endif
// #ifdef APP-PLUS
platform='app';
// #endif
import {onLoad} from '@dcloudio/uni-app';
import {api} from '../../api/client';
import {uiErrorMessage,t} from '@kuaixie/i18n';
import {usePrivatePageSession,privateWebTarget} from '../../utils/usePrivatePageSession';
const url=ref(''),error=ref('');
const scope=usePrivatePageSession(()=>{url.value='';error.value=t('mobileBusiness.sessionChanged')});
onLoad(async q=>{const owner=scope.capture();if(!owner.token){error.value=t('insurance.unavailable');return}try{const kind=String(q?.kind||'');let target:string|null=null;if(q?.quoteId){const quote=await api.insurance.ownQuote(String(q.quoteId));if(!scope.current(owner))return;target=kind==='terms'?quote.termsUrl:kind==='exclusions'?quote.exclusionsUrl:null}else if(q?.applicationId){const app=kind==='checkout'?await api.insurance.status(String(q.applicationId)):await api.insurance.record(String(q.applicationId));if(!scope.current(owner))return;if(kind==='checkout'&&app.platform!==platform)throw new Error(t('insurance.platformReturn'));target=kind==='checkout'&&app.status==='awaiting_provider'?app.checkoutUrl:kind==='certificate'?app.certificateUrl:kind==='claims'?app.claimsUrl:null}if(!scope.current(owner))return;if(!target)throw new Error(t('insurance.unavailable'));const destination=privateWebTarget(target);if(!destination)throw new Error(t('insurance.unavailable'));url.value=destination}catch(e){if(scope.current(owner))error.value=uiErrorMessage(e)}});
</script>
<template><view><view v-if="error" class="op-error">{{error}}</view><web-view v-else-if="url" :src="url"/></view></template>
