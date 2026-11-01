import { messages } from './messages.generated';
export type SupportedLocale = 'zh-CN' | 'en-US';
export const supportedLocales: readonly SupportedLocale[] = ['zh-CN','en-US'] as const;
let locale: SupportedLocale = 'zh-CN';
const listeners = new Set<(lang:SupportedLocale)=>void>();
export function normalizeLocale(value: string | undefined | null): SupportedLocale {
  return /^en(?:[-_]|$)/i.test(value||'') ? 'en-US' : 'zh-CN';
}
export function getLocale(): SupportedLocale { return locale; }
export function setLocale(value: SupportedLocale | string) {
  const next = normalizeLocale(value);
  // Always notify subscribers, even when the requested locale matches.
  // This fixes select controls that previously mutated a shared Vue ref before updating the translator.
  locale = next;
  listeners.forEach(listener=>listener(locale));
}
export function subscribeLocale(listener:(lang:SupportedLocale)=>void):()=>void {
  listeners.add(listener);return ()=>listeners.delete(listener);
}
export function t(key:string, vars:Record<string,string|number>={}):string {
  const result = messages[locale][key] ?? messages['zh-CN'][key];
  if(result===undefined) { if(typeof console!=='undefined') console.warn('[i18n] missing key:',key); return key; }
  return result.replace(/\{(\w+)\}/g,(_,k:string)=>String(vars[k]??'{'+k+'}'));
}
export function formatAmount(value:number|string|null|undefined,currency?:string|null):string {
  if(value==null||value==='')return t('common.priceOnRequest');
  const amount=Number(value);
  if(!Number.isFinite(amount))return t('common.notAvailable');
  const opts: Intl.NumberFormatOptions = currency
    ? {style:'currency',currency:currency.toUpperCase()}
    : {minimumFractionDigits:2,maximumFractionDigits:2};
  try {const formatted=new Intl.NumberFormat(locale,opts).format(amount);
    return currency?formatted:formatted+' ('+t('common.currencyUnknown')+')';
  } catch {return amount.toFixed(2)+' ('+t('common.currencyUnknown')+')';}
}
export function formatDate(value?:string|null):string {
  if(!value)return t('common.timeUnconfirmed');
  // Date-only and offset-less values must remain local; never invent a source time zone.
  const naive=/^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2})(?::(\d{2}))?)?$/.exec(value);
  const parsed=naive
    ? new Date(Number(naive[1]),Number(naive[2])-1,Number(naive[3]),Number(naive[4]??0),Number(naive[5]??0),Number(naive[6]??0))
    : new Date(value);
  if(!Number.isFinite(parsed.getTime())) return String(value);
  const isDateOnly=/^\d{4}-\d{2}-\d{2}$/.test(value.trim());
  const options:Intl.DateTimeFormatOptions=isDateOnly
    ? {year:'numeric',month:'short',day:'numeric'}
    : {year:'numeric',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'};
  try {return new Intl.DateTimeFormat(locale,options).format(parsed)}
  catch {return String(value)}
}
export function localizedField(entity:Record<string,unknown>,field:string):string {
  // Backend must explicitly return localized names or a translations map; never machine-translate product data.
  const translations=entity.translations;
  if(translations && typeof translations==='object') {
    const value=(translations as Record<string,Record<string,unknown>>)[locale]?.[field];
    if(typeof value==='string'&&value.trim())return value;
  }
  const raw=entity[field];return typeof raw==='string'?raw:String(raw??'');
}
export function operationLabel(op:{id:string;name:string}):string { const key='api.operation.'+op.id.replace(/\//g,'.').replace(/-/g,'_'); return messages[getLocale()][key]??op.name; }
export function groupLabel(group:string):string { const key='api.group.'+hashString(group);return messages[getLocale()][key]??group; }
// Stable SHA1 group keys generated at build time; runtime uses this small constant lookup instead of crypto APIs.
const groups:Record<string,string> = {"业务运营": "e7779f8f668e", "交易与PK": "3589aec49def", "公开目录": "20c996471e87", "协助与互动": "29cb58bebd06", "实名认证": "2ad59de38e40", "订单履约": "8f3dc6168168", "账户安全": "59a0b9298ed6", "运营财务": "873f95fc4aab", "邀请与团队": "3bb06e56aaa9"};
function hashString(name:string):string { return groups[name] ?? 'missing'; }

export function uiErrorMessage(error:unknown): string {
  const detail=error instanceof Error ? error.message : String(error);
  const response=error&&typeof error==='object'?(error as {detail?:unknown}).detail:undefined;
  const code=typeof response==='string'?response:response&&typeof response==='object'?String((response as {detail?:unknown}).detail??''):'';
  const orderErrors:Record<string,string>={
    PAYMENT_INPUT_REJECTED:'pay.inputRejected',PAYMENT_OR_FX_UNAVAILABLE:'pay.unavailable',PAYMENT_STORAGE_UNAVAILABLE:'pay.storageUnavailable',
    INSUFFICIENT_BALANCE:'order.insufficientBalance',TRIP_CAPACITY_UNAVAILABLE:'order.capacityUnavailable',
    PRICE_CHANGED_RECONFIRM_QUOTE:'order.priceChanged','ERR:INSURANCE_LOCKED_AFTER_CONFIRMATION':'order.insuranceLocked',
    'ERR:INSUFFICIENT_BALANCE':'order.insufficientBalance','ERR:SHIPPING_FEE_CHANGED_RECONFIRM':'order.priceChanged'
  };
  const commerceErrors:Record<string,string>={PRODUCT_STOCK_UNAVAILABLE:'checkout.stockUnavailable',PRODUCT_STORE_UNAVAILABLE:'checkout.storeUnavailable',PRODUCT_TRIP_UNAVAILABLE:'checkout.tripUnavailable',CATEGORY_NOT_ALLOWED_FOR_TRIP:'checkout.tripUnavailable',PRODUCT_CHANGED_REQUOTE:'checkout.changed',PRODUCT_ORDER_REQUEST_REUSED:'checkout.requestReused',PRODUCT_IDENTITY_HELD:'product.stockChanged',PRODUCT_STOCK_CHANGED_REFRESH:'product.stockChanged',REFUND_INSUFFICIENT_AVAILABLE_BALANCE:'refund.insufficientBalance',REFUND_CHANNEL_NOT_CONFIGURED:'refund.channelMissing',REFUND_ALREADY_REQUESTED:'refund.sourcePending'};
  if(commerceErrors[code])return t(commerceErrors[code]);
  if(code.startsWith('REFUND_'))return t('refund.requestRejected');
  if(code.startsWith('PRODUCT_ORDER_')||code==='PRODUCT_UNAVAILABLE')return t('checkout.serviceUnavailable');
  if(code.startsWith('SHOP_'))return t('shop.error');
  if(code.startsWith('UPLOAD_'))return t('upload.error');
  if(code.startsWith('INTEGRATION_'))return t('integration.error');
  if(code==='DISPUTE_OPEN'||code==='ERR:DISPUTE_OPEN')return t('case.openBlocksSettlement');
  if(code==='DISPUTE_CHANGED_REFRESH')return t('case.changedRefresh');
  if(code.startsWith('DISPUTE_'))return t(code==='DISPUTE_STORAGE_UNAVAILABLE'?'case.storageUnavailable':'case.rejected');
  if(code==='INSURANCE_PREMIUM_RECONCILIATION_REQUIRED')return t('insurance.reconciliationRequired');
  if(code==='ERR:INSURANCE_NOT_EFFECTIVE')return t('insurance.notIssued');
  if(code.startsWith('INSURANCE_')||code.startsWith('ERR:INSURANCE_'))return t(code.includes('SELECTION_REQUIRED')?'insurance.selectionRequired':'insurance.unavailable');
  if(orderErrors[code])return t(orderErrors[code]);
  const map:Record<string,string>={
    API_TOKEN_REJECTED:'common.apiTokenRejected', API_PERMISSION_DENIED:'common.apiPermissionDenied',
    API_TIMEOUT:'common.apiRequestTimeout', API_NETWORK_ERROR:'common.apiNetworkError',
    API_BUSINESS_REJECTED:'order.requestRejected'
  };
  const key=map[detail];return key?t(key):detail;
}

export function transportLabel(value?:string|null):string {
  if(!value)return t('common.transportUnknown');
  const key='transport.'+value.toLowerCase();
  return messages[getLocale()][key]??value;
}

/** Localize known backend state codes without changing or inventing underlying business state. */
export function formatBusinessStatus(code?:string|null):string {
  if(!code)return t('account.notKnown');
  const key='account.state.'+code.toLowerCase();
  return messages[getLocale()][key] ?? code;
}
