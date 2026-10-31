import {computed,ref} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {t} from '@kuaixie/i18n';
const CLEARABLE_PREFIXES=['kx.cache.','kx.search.','kx.catalog.'];
/** Whitelist cleanup only. Unknown keys could contain credentials or drafts. */
export function isClearableStorageKey(key:string):boolean{return CLEARABLE_PREFIXES.some(prefix=>key.startsWith(prefix))}
export function useStorageSettings(){
 const keys=ref<string[]>([]),sizeKb=ref(0),limitKb=ref(0),error=ref('');
 const clearable=computed(()=>keys.value.filter(isClearableStorageKey));
 function reload(){try{const info=uni.getStorageInfoSync();keys.value=info.keys??[];sizeKb.value=info.currentSize??0;limitKb.value=info.limitSize??0;error.value=''}catch{error.value=t('settings2.storageUnavailable')}}
 function clear(){uni.showModal({title:t('settings2.clearTitle'),content:t('settings2.clearConfirm',{count:clearable.value.length}),success:({confirm})=>{
  if(!confirm)return;
  try{for(const key of clearable.value){uni.removeStorageSync(key)}reload();uni.showToast({title:t('settings2.clearDone'),icon:'none'})}
  catch{error.value=t('settings2.storageUnavailable')}
 }})}
 onShow(reload);
 return {keys,sizeKb,limitKb,clearable,error,reload,clear};
}
