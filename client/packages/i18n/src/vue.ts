/// <reference path="./vue.d.ts" />
import { ref, type App } from 'vue';
import { getLocale, setLocale, subscribeLocale, t, type SupportedLocale } from './core';
const current=ref<SupportedLocale>(getLocale());
function synchronizeLocale(locale:SupportedLocale){current.value=locale;if(typeof document!=='undefined')document.documentElement.lang=locale;}
subscribeLocale(synchronizeLocale);
synchronizeLocale(getLocale());
export const localeRef=current;
/** Reactive global template translator; all three Vue/UniApp applications share this plugin. */
export function installI18n<T extends App>(app:T):T {
  app.config.globalProperties.$t=(key:string,vars?:Record<string,string|number>)=>{
    void current.value; // dependency ensures reactive updates after language switch
    return t(key,vars);
  };
  return app;
}
export {getLocale,setLocale,t};
