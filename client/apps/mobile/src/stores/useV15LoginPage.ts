import {computed,ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {setBearer,getBearer} from '@kuaixie/sdk';
import {t} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15LoginPage(){
const prefs=preferenceRef, mode=ref<'signin'|'signup'>('signin'),channel=ref<'phone'|'email'>('phone'),contact=ref(''),countryCode=ref('+86'),password=ref(''),code=ref(''),inviteCode=ref(''),returnTrip=ref(''),role=ref<'buyer'|'carrier'>('buyer'),withCode=ref(false),agreed=ref(false),busy=ref(false),error=ref(''),notice=ref('');
const countryCodes=['+86','+1','+44','+61','+81','+49'];
const contactValue=computed(()=>channel.value==='phone'&&countryCode.value!=='+86'?countryCode.value.slice(1)+contact.value.trim():contact.value.trim());
const valid=computed(()=>channel.value==='email'?/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contact.value.trim()):countryCode.value==='+86'?/^1[3-9]\d{9}$/.test(contact.value.trim()):/^\d{5,15}$/.test(contact.value.trim()));
const needCode=computed(()=>mode.value==='signup'||withCode.value);
onLoad((q)=>{inviteCode.value=String(q?.code||'').replace(/[^a-zA-Z0-9_-]/g,'').slice(0,40);const candidate=String(q?.returnPath||'');const match=candidate.match(/^\/pages\/create-intent\/index\?tripId=([a-zA-Z0-9_-]{1,80})$/);returnTrip.value=match?match[1]:'';});
function changeMode(){mode.value=mode.value==='signin'?'signup':'signin';error.value='';notice.value=''}
function agreement(type:string){uni.navigateTo({url:'/pages/legal-doc/index?type='+type})}
function guard(){error.value='';if(!valid.value){error.value=t(channel.value==='phone'?'v15.phoneInvalid':'v15.emailInvalid');return false}return true}
async function sendCode(){if(busy.value||!guard())return;if(!agreed.value){error.value=t('v15.mustAgree');return}busy.value=true;try{await api.originalV15.sendCode({channel:channel.value,contact:contactValue.value,purpose:mode.value});notice.value=t('v15.sendCode')}catch(e){error.value=e instanceof Error?e.message:t('v15.authUnavailable')}finally{busy.value=false}}
async function submit(){if(busy.value||!guard())return;if(!password.value.trim()&&(mode.value==='signup'||!needCode.value)){error.value=t('v15.invalidPassword');return}if(needCode.value&&!code.value.trim()){error.value=t('v15.invalidCode');return}if((mode.value==='signup'||needCode.value)&&!agreed.value){error.value=t('v15.mustAgree');return}busy.value=true;error.value='';try{
 const response=mode.value==='signin'?await api.originalV15.login({channel:channel.value,contact:contactValue.value,...(needCode.value?{code:code.value.trim()}:{password:password.value})}):await api.originalV15.signup({channel:channel.value,contact:contactValue.value,password:password.value,code:code.value.trim(),role:role.value,...(inviteCode.value?{inviteCode:inviteCode.value.trim()}: {})});
 const token=response?.accessToken||response?.access_token||response?.token;
 if(typeof token!=='string'||!/^[-_a-zA-Z0-9]+\.[-_a-zA-Z0-9]+\.[-_a-zA-Z0-9]+$/.test(token))throw new Error(t('v15.emptyToken'));
 setBearer(token);uni.showToast({title:t('v15.loggedIn'),icon:'success'});if(returnTrip.value)uni.redirectTo({url:'/pages/create-intent/index?tripId='+encodeURIComponent(returnTrip.value)});else uni.switchTab({url:'/pages/me/index'});
}catch(e){error.value=e instanceof Error?e.message:t('v15.authUnavailable')}finally{busy.value=false}}
return {prefs,mode,channel,contact,countryCode,password,code,inviteCode,role,withCode,agreed,busy,error,notice,countryCodes,needCode,sendCode,submit,agreement,changeMode,mobilePreferenceStyle};
}
