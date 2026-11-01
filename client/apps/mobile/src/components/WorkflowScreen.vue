<script setup lang="ts">
import InsuranceSelectionMobile from './InsuranceSelectionMobile.vue';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import type {useWorkflowPage} from '../stores/useWorkflowPage';
import {t} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';
import {watch} from 'vue';
import {uniEventValue} from '../utils/uniEvent';
const props=defineProps<{page:ReturnType<typeof useWorkflowPage>}>();
const prefs=preferenceRef;
function showTitle(){try{uni.setNavigationBarTitle({title:t(props.page.flow.titleKey)})}catch{}}
showTitle();watch(localeRef,showTitle);
function changeField(name:string,event:unknown){props.page.update(name,uniEventValue(event))}
function changeInsurance(event:unknown){const value=Number(uniEventValue(event));if(value===0||value===1)props.page.update('insuranceChoice',value===0?'yes':'waived')}
</script>
<template>
 <view class="app-page flow-page" :class="{'kx-care':prefs.care,'kx-dark':prefs.theme==='dark','kx-reduce-motion':prefs.reduceMotion}" :style="mobilePreferenceStyle(prefs)">
  <view class="flow-head"><text class="flow-back" @click="page.back">‹ {{ $t('m2.mobile.back') }}</text><view class="page-heading">{{ $t(page.flow.titleKey) }}</view><view class="flow-subtitle">{{ $t(page.flow.descKey) }}</view></view>
  <scroll-view v-if="page.flow.operations.length>1" scroll-x class="action-scroll"><view class="action-track"><view v-for="(op,i) in page.flow.operations" :key="op.id" class="action-pill" :class="{selected:page.active===i}" @click="page.pick(i)">{{page.label(op)}}</view></view></scroll-view>
  <view class="flow-card" v-if="page.current">
   <view class="flow-action-title">{{page.label(page.current)}}</view>
   <view class="flow-alert" v-if="page.current.dangerous">{{ $t('fast.important') }}</view>
   <view v-if="!page.fields.length" class="flow-note">{{ $t('fast.noFields') }}</view>
   <view v-for="field in page.fields" :key="field.name" class="flow-field">
    <view class="flow-label"><text>{{ page.fieldLabel(field.name) }}</text><text v-if="page.required(field.name)" class="flow-required">*</text></view>
    <picker v-if="field.name==='insuranceChoice'" :range="[$t('v15.insure'),$t('v15.waive')]" @change="changeInsurance"><view class="flow-input">{{page.form[field.name]==='yes'?$t('v15.insure'):page.form[field.name]==='waived'?$t('v15.waive'):$t('v15.insurance')}}</view></picker>
    <switch v-else-if="field.type==='boolean'" :checked="page.form[field.name]===true" color="#0ea5e9" @change="changeField(field.name,$event)" />
    <textarea v-else-if="page.isLong(field.name)" class="flow-input flow-area" :value="String(page.form[field.name]??'')" :maxlength="3000" :placeholder="page.fieldLabel(field.name)" @input="changeField(field.name,$event)" />
    <input v-else class="flow-input" :value="String(page.form[field.name]??'')" :type="field.type==='number'?'digit':'text'" :password="page.isSecret(field.name)" :maxlength="300" :placeholder="page.fieldLabel(field.name)" @input="changeField(field.name,$event)" />
   </view>
   <template v-if="page.current.path==='/rpc/award_pk'&&page.form.insuranceChoice==='yes'"><button :disabled="page.busy" @click="page.loadInsuranceSubject">{{$t('insurance.loadSubject')}}</button><InsuranceSelectionMobile v-if="page.insuranceSubject" subject-type="pk_quote" :subject-id="page.insuranceSubject.subjectId" :item-name="page.insuranceSubject.itemName" :declared-value="page.insuranceSubject.declaredValue" @selected="page.update('insuranceQuoteId',$event?.id)"/></template>
   <view class="flow-note">{{ $t('fast.serverOnly') }}</view>
   <button class="flow-submit" :disabled="page.busy" @click="page.submit">{{page.busy?$t('fast.submitting'):$t('fast.submit')}}</button>
   <view v-if="page.error" class="flow-error" role="alert">{{page.error}}</view>
   <view v-if="page.completed" class="flow-success" role="status"><view class="flow-success-title">{{ $t('fast.success') }}</view><view v-for="pair in page.receipt" :key="pair[0]" class="flow-receipt"><text>{{ $t('fast.resultField') }} {{pair[0]}}</text><text class="flow-result">{{pair[1]}}</text></view></view>
  </view>
 </view>
</template>
<style scoped>
.flow-page{--flow-accent:var(--kx-accent,#0ea5e9);background:#f8fafc;padding:28rpx 28rpx 120rpx}.flow-head{padding-bottom:13rpx}.flow-back{display:block;padding:13rpx 0;font-size:25rpx;color:var(--flow-accent)}.flow-head .page-heading{font-size:39rpx;margin:13rpx 0}.flow-subtitle{font-size:24rpx;line-height:1.7;color:#64748b;overflow-wrap:anywhere}.action-scroll{white-space:nowrap;margin:17rpx 0 20rpx}.action-track{display:flex;gap:12rpx;padding:5rpx 0 13rpx}.action-pill{flex-shrink:0;min-height:65rpx;padding:14rpx 23rpx;border-radius:18rpx;border:1rpx solid #dce4ef;color:#475569;background:white;max-width:375rpx;white-space:normal;font-size:24rpx;line-height:1.45}.action-pill.selected{background:var(--flow-accent);color:white;border-color:var(--flow-accent)}.flow-card{background:white;border:1rpx solid #e2e8f0;border-radius:23rpx;padding:29rpx 24rpx;margin-top:12rpx}.flow-action-title{font-weight:750;font-size:30rpx;line-height:1.5;overflow-wrap:anywhere}.flow-label{display:flex;gap:5rpx;font-size:24rpx;color:#475569;font-weight:600;margin:24rpx 0 11rpx}.flow-required{color:#d44f53}.flow-input{width:100%;height:84rpx;background:#f8fafc;border:1rpx solid #dce4ef;border-radius:13rpx;padding:16rpx 19rpx;font-size:26rpx;color:#0f172a;min-width:0}.flow-area{height:168rpx;line-height:1.6}.flow-note{font-size:22rpx;color:#64748b;margin:20rpx 0;line-height:1.7}.flow-alert{padding:15rpx 19rpx;border-radius:14rpx;background:#fff7ed;color:#9a5c18;font-size:22rpx;margin-top:15rpx}.flow-submit{background:var(--flow-accent);color:#fff;border-radius:15rpx;margin-top:24rpx;font-size:27rpx;padding:12rpx}.flow-submit[disabled]{opacity:.55}.flow-error{color:#a23b34;background:#fff1f0;border-radius:12rpx;margin-top:17rpx;padding:17rpx;font-size:24rpx;overflow-wrap:anywhere}.flow-success{padding:20rpx;background:#f0fdf4;border-radius:14rpx;margin-top:18rpx;color:#166534}.flow-success-title{font-weight:750;font-size:27rpx}.flow-receipt{display:flex;gap:12rpx;justify-content:space-between;flex-wrap:wrap;margin-top:12rpx;font-size:22rpx}.flow-result{word-break:break-all;max-width:75%}.kx-dark .flow-card,.kx-dark .action-pill{background:#202629;color:#f2f4f4;border-color:#46535d}.kx-dark .flow-input{background:#283137;color:#f2f4f4;border-color:#475569}.kx-dark .flow-subtitle,.kx-dark .flow-label{color:#cbd5e1}.kx-dark .action-pill.selected{background:var(--flow-accent)}@media (min-width:768px){.flow-page{max-width:920rpx}.flow-card{padding:35rpx}}
</style>
