
import {primaryNavigation} from '@kuaixie/sdk';
import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';


import {ref} from 'vue';
import {getBearer,setBearer,clearBearer} from '@kuaixie/sdk';
import {t,setLocale,getLocale} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';

import {setPreferences,resetPreferences,validHex,type ThemeMode,type FontChoice} from '@kuaixie/preferences';

export function useMePage(){
function openServices(){uni.navigateTo({url:'/pages/services/index'})}


const prefs=preferenceRef;
const lang=localeRef;const themeOptions:ThemeMode[]=['system','light','dark'];const fontOptions:FontChoice[]=['system','modern','reading'];
const accentDraft=ref(prefs.value.accent);
function changeTheme(e:{detail:{value:number}}){setPreferences({theme:themeOptions[Number(e.detail.value)]||'system'})}
function changeFont(e:{detail:{value:number}}){setPreferences({font:fontOptions[Number(e.detail.value)]||'system'})}
function changeScale(e:{detail:{value:number}}){setPreferences({scale:Number(e.detail.value)/100})}
function changeCare(e:{detail:{value:boolean}}){setPreferences({care:!!e.detail.value})}
function changeMotion(e:{detail:{value:boolean}}){setPreferences({reduceMotion:!!e.detail.value})}
function applyAccent(){if(validHex(accentDraft.value))setPreferences({accent:accentDraft.value});else uni.showToast({title:t('preferences.accent'),icon:'none'})}
function resetDisplay(){resetPreferences();accentDraft.value=prefs.value.accent}
const localeChoices=['zh-CN','en-US'];
function changeLanguage(e:{detail:{value:number}}){const l=localeChoices[Number(e.detail.value)];if(!l)return;setLocale(l);uni.setStorageSync('kx.locale',l);syncTabLabels()}
function syncTabLabels(){primaryNavigation.map(item=>item.titleKey).forEach((key,index)=>{uni.setTabBarItem({index,text:t(key)})})}
const token=ref(getBearer());const visible=ref(false);
function save(){setBearer(token.value);uni.showToast({title:t('common.tokenSet'),icon:'none'})}
function clear(){clearBearer();token.value='';uni.showToast({title:t('common.tokenClear'),icon:'none'})}


return {openServices,prefs,lang,themeOptions,fontOptions,accentDraft,changeTheme,changeFont,changeScale,changeCare,changeMotion,applyAccent,resetDisplay,localeChoices,changeLanguage,syncTabLabels,token,visible,save,clear,mobilePreferenceStyle};
}
