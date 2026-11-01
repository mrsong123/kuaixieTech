<script setup lang="ts">
import type {PaymentPlatform} from '@kuaixie/sdk';
let platform:PaymentPlatform='h5';
// #ifdef MP-WEIXIN
platform='mp_weixin';
// #endif
// #ifdef APP-PLUS
platform='app';
// #endif
import type {InsuranceQuote} from '@kuaixie/sdk';
import {useInsuranceSelection} from '@kuaixie/ui/useInsurance';
import {uniEventValue} from '../utils/uniEvent';
import {api} from '../api/client';
const props=defineProps<{subjectType:'trip'|'pk_quote'|'order';subjectId:string;itemName:string;declaredValue:string}>();const emit=defineEmits<{selected:[quote:InsuranceQuote|null]}>();
const p=useInsuranceSelection(api,()=>({subjectType:props.subjectType,subjectId:props.subjectId,itemName:props.itemName,declaredValue:props.declaredValue}),value=>emit('selected',value),platform);
function pick(event:unknown){p.product.value=p.products.value[Number(uniEventValue(event))-1]?.id||''}
function document(kind:string){if(p.quote.value)uni.navigateTo({url:'/pages/insurance-document/index?quoteId='+encodeURIComponent(p.quote.value.id)+'&kind='+kind})}
</script>
<template><view class="insurance-box"><view class="op-title">{{$t('insurance.choose')}}</view><text>{{$t('insurance.boundary')}}</text><picker :range="p.companies.value.map(item=>item.name)"><view class="op-row">{{$t('insurance.directory')}} ⌄</view></picker><text v-if="!p.products.value.length">{{$t('insurance.noProducts')}}</text><picker :disabled="p.busy.value" :range="[$t('insurance.choose'),...p.products.value.map(item=>item.insurerName+' · '+item.kind+' · '+item.name)]" @change="pick"><view class="op-row">{{p.products.value.find(item=>item.id===p.product.value)?.name||$t('insurance.choose')}} ⌄</view></picker><button :disabled="p.busy.value||!p.product.value||!declaredValue||!itemName.trim()" @click="p.preview">{{$t('insurance.quote')}}</button><view v-if="p.error.value" class="op-error">{{p.error.value}}</view><template v-if="p.quote.value"><view>{{p.quote.value.insurerName}} · {{p.quote.value.productName}}</view><view>{{$t('insurance.directPremium')}} · {{p.quote.value.premium+' '+p.quote.value.currency}}</view><text>{{$t('insurance.notIssued')}} · {{p.quote.value.expiresAt}}</text><view class="op-row" @click="document('terms')">{{$t('insurance.terms')}} ›</view><view class="op-row" @click="document('exclusions')">{{$t('insurance.exclusions')}} ›</view></template></view></template>
<style scoped>.insurance-box{margin:20rpx 0;padding:22rpx;border:1rpx solid #cbd5e1;border-radius:16rpx}.insurance-box text{display:block;line-height:1.7;font-size:23rpx;color:#64748b}.insurance-box button{margin:16rpx 0;font-size:25rpx;background:var(--kx-accent,#0e7490);color:white}</style>
