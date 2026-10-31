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
    ? {style:'currency',currency:currency.toUpperCase(),maximumFractionDigits:2}
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
  const map:Record<string,string>={
    API_TOKEN_REJECTED:'common.apiTokenRejected', API_PERMISSION_DENIED:'common.apiPermissionDenied',
    API_TIMEOUT:'common.apiRequestTimeout', API_NETWORK_ERROR:'common.apiNetworkError'
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
