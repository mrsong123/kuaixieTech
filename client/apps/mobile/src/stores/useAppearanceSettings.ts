import {computed, ref} from 'vue';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {setPreferences,resetPreferences,validHex,type ThemeMode,type FontChoice} from '@kuaixie/preferences';
import {setLocale,getLocale,t} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';
import {uniEventValue} from '../utils/uniEvent';
export function useAppearanceSettings(){
 const prefs=preferenceRef;
 const themes:ThemeMode[]=['system','light','dark'];const fonts:FontChoice[]=['system','modern','reading'];
 const accent=ref(prefs.value.accent);const locale=localeRef;
 const themeIndex=computed(()=>Math.max(0,themes.indexOf(prefs.value.theme)));
 const themeLabelKey=computed(()=>`preferences.theme.${prefs.value.theme}`);
 const fontLabelKey=computed(()=>`preferences.font.${prefs.value.font}`);
 const fontIndex=computed(()=>Math.max(0,fonts.indexOf(prefs.value.font)));
 function onTheme(e:{detail:{value:number|string}}){setPreferences({theme:themes[Number(e.detail.value)]??'system'})}
 function onFont(e:{detail:{value:number|string}}){setPreferences({font:fonts[Number(e.detail.value)]??'system'})}
 function onScale(e:{detail:{value:number}}){setPreferences({scale:Number(e.detail.value)/100})}
 function onCare(e:unknown){const value=uniEventValue(e);if(typeof value==='boolean')setPreferences({care:value})}
 function onMotion(e:unknown){const value=uniEventValue(e);if(typeof value==='boolean')setPreferences({reduceMotion:value})}
 function onLocale(e:{detail:{value:number|string}}){const next=Number(e.detail.value)===1?'en-US':'zh-CN';setLocale(next);uni.setStorageSync('kx.locale',next);(['prototype.nav.square','portal.tabs.community','prototype.nav.publish','prototype.nav.messages','mobile.tab.me']as const).forEach((key,index)=>uni.setTabBarItem({index,text:t(key)}))}
 function onAccent(){if(!validHex(accent.value)){uni.showToast({title:t('settings2.invalidColor'),icon:'none'});return}setPreferences({accent:accent.value})}
 function reset(){resetPreferences();accent.value=prefs.value.accent}
 return {prefs,themes,fonts,accent,locale,themeIndex,fontIndex,themeLabelKey,fontLabelKey,onTheme,onFont,onScale,onCare,onMotion,onLocale,onAccent,reset};
}
