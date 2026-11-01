import {computed,ref,proxyRefs} from 'vue';
import {onLoad,onShow} from '@dcloudio/uni-app';
import {api} from '../api/client';
import {getLocale,formatDate,t} from '@kuaixie/i18n';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import type {PublicAnnouncement,PublicLegalDocument} from '@kuaixie/sdk';
export function localized(raw:string,translations?:Record<string,string>):string{
 const loc=getLocale();return translations?.[loc]?.trim()||translations?.['zh-CN']?.trim()||raw||'';
}
export function useAnnouncements(){
 const prefs=preferenceRef,rows=ref<PublicAnnouncement[]>([]),loading=ref(false),error=ref('');
 const expanded=ref('');
 async function load(){loading.value=true;error.value='';try{
 const result=await api.prototype.announcements(30);
 if(!Array.isArray(result))throw new Error(t('migration.invalidResponse'));
 rows.value=result.filter(x=>x&&typeof x.id==='string'&&x.active!==false);
 }catch(e){rows.value=[];error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{loading.value=false}}
 onShow(()=>{void load()});
 return proxyRefs({prefs,rows,loading,error,expanded,load,localized,formatDate,mobilePreferenceStyle});
}
export type DocumentLine={kind:'h1'|'h2'|'list'|'table'|'paragraph'|'blank';text:string;cells?:string[]};
export function parseDocument(md:string):DocumentLine[]{
 return String(md||'').split(/\r?\n/).map(raw=>{
 const v=raw.trim();if(!v)return {kind:'blank',text:''};
 if(/^# /.test(v))return {kind:'h1',text:v.slice(2)};
 if(/^## /.test(v))return {kind:'h2',text:v.slice(3)};
 if(/^- /.test(v))return {kind:'list',text:v.slice(2)};
 if(/^\|.+\|$/.test(v))return {kind:'table',text:'',cells:v.slice(1,-1).split('|').map(x=>x.trim())};
 return {kind:'paragraph',text:v};
 });
}
export function useLegalDocument(){
 const prefs=preferenceRef,doc=ref<PublicLegalDocument|null>(null),loading=ref(false),error=ref(''),type=ref('agreement');
 const lines=computed(()=>parseDocument(localized(doc.value?.body_md||'',doc.value?.body_md_i18n)));
 async function load(){loading.value=true;error.value='';try{
 const result=await api.prototype.legalDocument(type.value);
 if(!result||typeof result!=='object'||typeof result.body_md!=='string')throw new Error(t('migration.invalidResponse'));
 doc.value=result;
 }catch(e){doc.value=null;error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{loading.value=false}}
 onLoad(params=>{type.value=String(params?.type||'agreement');if(!/^(agreement|privacy|collection|cancel_policy)$/.test(type.value))type.value='agreement';void load()});
 return proxyRefs({prefs,doc,loading,error,lines,load,localized,formatDate,mobilePreferenceStyle});
}
