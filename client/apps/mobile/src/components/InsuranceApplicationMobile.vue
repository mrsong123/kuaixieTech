<script setup lang="ts">
import type {PaymentPlatform} from '@kuaixie/sdk';
let platform:PaymentPlatform='h5';
// #ifdef MP-WEIXIN
platform='mp_weixin';
// #endif
// #ifdef APP-PLUS
platform='app';
// #endif
import {ref} from 'vue';
import InsuranceSelectionMobile from './InsuranceSelectionMobile.vue';
import {useInsuranceApplication,insuranceStatusKey,insurancePlatformKey} from '@kuaixie/ui/useInsurance';
import {uniEventValue} from '../utils/uniEvent';
import {api} from '../api/client';
const props=defineProps<{orderId:string}>();const p=useInsuranceApplication(api,()=>props.orderId);const changeQuote=ref(false);
function consent(kind:'terms'|'disclosure',event:unknown){const value=uniEventValue(event)===true;if(kind==='terms')p.termsAccepted.value=value;else p.disclosuresAccepted.value=value}
function open(kind:string){let query='';if((kind==='terms'||kind==='exclusions')&&p.quote.value)query='quoteId='+encodeURIComponent(p.quote.value.id);else if(p.application.value)query='applicationId='+encodeURIComponent(p.application.value.id);if(query)uni.navigateTo({url:'/pages/insurance-document/index?'+query+'&kind='+kind})}
</script>
<template><view class="op-card"><view class="op-title">{{$t('insurance.application')}}</view><view class="op-muted">{{$t('insurance.boundary')}}</view><view v-if="p.error.value" class="op-error">{{p.error.value}}</view><button :disabled="p.busy.value" @click="p.load">{{$t('common.retry')}}</button><button v-if="!p.application.value" @click="changeQuote=!changeQuote">{{$t('insurance.requote')}}</button><InsuranceSelectionMobile v-if="changeQuote&&!p.application.value" subject-type="order" :subject-id="orderId" :item-name="p.risk.value.itemName" :declared-value="p.risk.value.declaredValue" @selected="p.replaceQuote"/><template v-if="p.quote.value"><view>{{p.quote.value.insurerName}} · {{p.quote.value.productName}}</view><view>{{$t('insurance.directPremium')}} · {{p.quote.value.premium+' '+p.quote.value.currency}}</view><view class="op-row" @click="open('terms')">{{$t('insurance.terms')}} ›</view><view class="op-row" @click="open('exclusions')">{{$t('insurance.exclusions')}} ›</view><view class="op-muted">{{$t('insurance.acceptTerms')}}<switch :checked="p.termsAccepted.value" @change="consent('terms',$event)"/></view><view class="op-muted">{{$t('insurance.acceptDisclosure')}}<switch :checked="p.disclosuresAccepted.value" @change="consent('disclosure',$event)"/></view><button v-if="!p.application.value" :disabled="p.busy.value" @click="p.apply">{{$t('insurance.apply')}}</button></template><template v-if="p.application.value"><view>{{$t(p.application.value.protectionStatus==='provider_confirmed'?'insurance.effective':'insurance.notIssued')}}</view><view>{{$t(insuranceStatusKey(p.application.value.status))}} · {{p.application.value.policyNumber||''}}</view><button v-if="p.application.value.status==='awaiting_provider'&&p.application.value.checkoutUrl&&p.application.value.platform===platform" @click="open('checkout')">{{$t('insurance.payInsurer')}}</button><view v-if="p.application.value.platform!==platform" class="op-muted">{{$t('insurance.platformReturn')}} · {{$t(insurancePlatformKey(p.application.value.platform))}}</view><button :disabled="p.busy.value" @click="p.refresh">{{$t('insurance.verify')}}</button><view v-if="p.application.value.certificateUrl" class="op-row" @click="open('certificate')">{{$t('insurance.certificate')}} ›</view><view class="op-row" @click="open('claims')">{{$t('insurance.claims')}} ›</view><text>{{p.application.value.startsAt}} — {{p.application.value.endsAt}}</text></template></view></template>
