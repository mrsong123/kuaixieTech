<script setup lang="ts">
import {onMounted} from 'vue';
import {t,formatDate,formatAmount} from '@kuaixie/i18n';
import type {EscrowWalletRefund} from '@kuaixie/sdk';
import {useEscrowRefunds} from '@kuaixie/ui/useEscrowRefunds';
import {api} from '../api/client';

const props=defineProps<{caseId:string;orderId?:string;role?:string;record?:EscrowWalletRefund|null}>();
// This surface only reads receipts. Execution is available exclusively in the admin application.
const p=useEscrowRefunds(api,async()=>false,{caseId:()=>props.caseId,orderId:()=>props.orderId||'',role:()=>props.role||'buyer',seed:()=>props.record||null});
onMounted(()=>{if((props.role||'buyer')==='buyer')void p.loadRecords()});
</script>

<template>
  <view class="escrow-refund">
    <view class="op-card-title">{{t('escrowRefund.title')}}</view>
    <view class="op-muted">{{t('escrowRefund.destinationNotice')}}</view>
    <view v-if="p.error.value" class="op-error">{{p.error.value}}</view>
    <view v-if="p.notice.value" class="op-muted">{{p.notice.value}}</view>
    <view v-if="p.receipt.value" class="op-card">
      <view>{{t('escrowRefund.receipt')}} · {{formatAmount(p.receipt.value.amount,'CNY')}}</view>
      <text selectable>{{p.receipt.value.id}}</text>
      <view>{{formatDate(p.receipt.value.createdAt)}}</view>
      <view v-if="p.receipt.value.requiresAccounting">{{t('escrowRefund.fullAccounting')}}</view>
      <view v-if="p.receipt.value.rewardReviewRequired">{{t('escrowRefund.rewardReview')}}</view>
      <button v-if="p.canReadEvents.value" :disabled="p.eventsLoading.value" @click="p.loadEvents(p.receipt.value.id)">{{t('escrowRefund.events')}}</button>
    </view>
    <view v-if="(role||'buyer')==='buyer'">
      <view class="op-card-title">{{t('escrowRefund.history')}}</view>
      <button :disabled="p.recordsLoading.value" @click="p.loadRecords()">{{t('escrowRefund.refresh')}}</button>
      <view v-if="p.recordsLoading.value" class="op-muted">{{t('account.loading')}}</view>
      <view v-for="row in p.records.value" :key="row.id" class="op-card">
        <text selectable>{{row.orderId}}</text>
        <view>{{formatAmount(row.amount,'CNY')}} · {{formatDate(row.createdAt)}}</view>
        <view v-if="row.requiresAccounting">{{t('escrowRefund.fullAccounting')}}</view>
        <view v-if="row.rewardReviewRequired">{{t('escrowRefund.rewardReview')}}</view>
        <button :disabled="p.eventsLoading.value" @click="p.loadEvents(row.id)">{{t('escrowRefund.events')}}</button>
      </view>
      <view v-if="!p.records.value.length&&!p.recordsLoading.value" class="op-muted">{{t('escrowRefund.noRecords')}}</view>
      <button v-if="p.more.value" :disabled="p.recordsLoading.value" @click="p.loadRecords(true)">{{t('escrowRefund.more')}}</button>
    </view>
    <view v-if="p.canReadEvents.value&&p.eventsId.value" class="op-card">
      <view class="op-card-title">{{t('escrowRefund.events')}}</view>
      <text selectable>{{p.eventsId.value}}</text>
      <view v-if="p.eventsLoading.value" class="op-muted">{{t('account.loading')}}</view>
      <view v-for="event in p.events.value" :key="event.id" class="escrow-event">
        <view>{{formatDate(event.createdAt)}}</view>
        <view>{{event.detail}}</view>
      </view>
      <view v-if="!p.events.value.length&&!p.eventsLoading.value" class="op-muted">{{t('escrowRefund.noEvents')}}</view>
      <button v-if="p.moreEvents.value" :disabled="p.eventsLoading.value" @click="p.loadEvents(p.eventsId.value,true)">{{t('escrowRefund.more')}}</button>
    </view>
  </view>
</template>

<style scoped>
.escrow-refund{margin-top:16px;padding-top:12px;border-top:1px solid #e5e7eb}.escrow-refund text{display:block;overflow-wrap:anywhere}.escrow-refund button{margin:10px 0}.escrow-event{padding:10px 0;border-bottom:1px solid #e5e7eb}
</style>
