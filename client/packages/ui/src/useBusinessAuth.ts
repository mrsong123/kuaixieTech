import {computed,onUnmounted,ref} from 'vue';
import {ApiError,setBearer,type ApiClient} from '@kuaixie/sdk';
import {setLocale,t} from '@kuaixie/i18n';

const knownErrors:Record<string,string>={
  CAPTCHA_FAILED:'v15.captchaFailed',
  CODE_TOO_SOON:'v15.codeWait',
  AUTH_BAD_PASSWORD:'v15.passwordRule',
  AUTH_FAILED:'v15.authFailed',
  AUTH_CONTACT_TAKEN:'v15.contactTaken',
  AUTH_BAD_LICENSE:'v15.licenseInvalid',
  AUTH_BAD_ENTERPRISE:'v15.enterpriseRequired',
  SUPPORT_UNAVAILABLE:'v15.supportLink',
};

export interface AuthPlatform {persistLocale:(value:string)=>void;sessionAccepted:()=>void;openLegal:(type:string)=>void;openSupport:()=>void;wechatCode?:()=>Promise<string>}
export function useBusinessAuth(api:ApiClient,platform:AuthPlatform){
const mode=ref<'signin'|'signup'|'recover'>('signin');
const channel=ref<'phone'|'email'>('phone');
const contact=ref('');
const countryCode=ref('+86');
const password=ref('');
const code=ref('');
const inviteCode=ref('');
const role=ref<'buyer'|'carrier'|'enterprise'>('buyer');
const enterpriseName=ref('');
const businessLicenseNo=ref('');
const withCode=ref(false);
const agreed=ref(false);
const busy=ref(false);
const error=ref('');
const notice=ref('');
const passwordFailed=ref(false);
const captchaId=ref('');
const captchaImage=ref('');
const captchaText=ref('');
const codeWait=ref(0);
const countryCodes=['+86','+1','+44','+61','+81','+49'];
const contactValue=computed(()=>contact.value.trim());
const valid=computed(()=>channel.value==='email'?/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contact.value.trim()):countryCode.value==='+86'?/^1[3-9]\d{9}$/.test(contact.value.trim()):/^\d{5,15}$/.test(contact.value.trim()));
const needCode=computed(()=>mode.value==='signup'||mode.value==='recover'||withCode.value);
const needPassword=computed(()=>mode.value==='signup'||mode.value==='recover'||!withCode.value);
let waitTimer:ReturnType<typeof setInterval>|0=0;
function applyPhoneLocale(country:string){const value=country==='+86'?'zh-CN':'en-US';setLocale(value);platform.persistLocale(value)}
function chooseCountry(next:string){if(!countryCodes.includes(next))return;countryCode.value=next;applyPhoneLocale(next)}
function codeOf(e:unknown){if(!(e instanceof ApiError)||!e.detail||typeof e.detail!=='object')return '';const message=(e.detail as {message?:unknown}).message;return typeof message==='string'?message:''}
function explain(e:unknown){const reason=codeOf(e);if(reason==='AUTH_BAD_CONTACT')return t(channel.value==='phone'?'v15.phoneInvalid':'v15.emailInvalid');if(reason&&knownErrors[reason])return t(knownErrors[reason]);if(e instanceof ApiError&&e.status===401)return t('v15.authFailed');return e instanceof Error?e.message:t('v15.authUnavailable')}
function startWait(seconds:number){codeWait.value=seconds;if(waitTimer)clearInterval(waitTimer);waitTimer=setInterval(()=>{codeWait.value-=1;if(codeWait.value<=0){codeWait.value=0;if(waitTimer)clearInterval(waitTimer);waitTimer=0}},1000)}
onUnmounted(()=>{if(waitTimer)clearInterval(waitTimer)});

function changeMode(){mode.value=mode.value==='signin'?'signup':'signin';error.value='';notice.value='';passwordFailed.value=false}
function openRecover(){mode.value='recover';error.value='';notice.value='';code.value='';password.value='';passwordFailed.value=false;void refreshCaptcha()}
function agreement(type:string){platform.openLegal(type)}
function guard(){error.value='';if(!valid.value){error.value=t(channel.value==='phone'?'v15.phoneInvalid':'v15.emailInvalid');return false}return true}
function phoneBody(){return channel.value==='phone'?{countryCode:countryCode.value}:{}}
async function refreshCaptcha(report=false){try{const issued=await api.originalV15.captcha();captchaId.value=issued.id||'';captchaImage.value=issued.image||'';captchaText.value=''}catch(e){captchaImage.value='';if(report&&needCode.value)error.value=explain(e)}}
function acceptSession(response:{accessToken?:string;access_token?:string;token?:string;locale?:string}|undefined,phone:boolean){
 const token=response?.accessToken||response?.access_token||response?.token;
 if(typeof token!=='string'||!/^[-_a-zA-Z0-9]+\.[-_a-zA-Z0-9]+\.[-_a-zA-Z0-9]+$/.test(token))throw new Error(t('v15.emptyToken'));
 if(phone&&(response?.locale==='zh-CN'||response?.locale==='en-US'))applyPhoneLocale(response.locale==='zh-CN'?'+86':'+1');
 setBearer(token);platform.sessionAccepted();
}
async function sendCode(){
 if(busy.value||codeWait.value>0||!guard())return;
 if(mode.value!=='recover'&&!agreed.value){error.value=t('v15.mustAgree');return}
 if(!captchaId.value||captchaText.value.trim().length!==4){error.value=t('v15.captchaFailed');return}
 busy.value=true;error.value='';
 const purpose=mode.value==='signup'?'signup':mode.value==='recover'?'reset':'signin';
 try{
  await api.originalV15.sendCode({channel:channel.value,contact:contactValue.value,purpose,captchaId:captchaId.value,captchaCode:captchaText.value.trim(),...phoneBody()});
  notice.value=t('v15.codeSent');
  startWait(60);
 }catch(e){
  if(codeOf(e)==='CODE_TOO_SOON')startWait(60);
  error.value=codeOf(e)?explain(e):e instanceof ApiError&&e.status===401?t('v15.authUnavailable'):explain(e);
 }finally{busy.value=false;void refreshCaptcha()}
}
function passwordReady(){
 const value=password.value.trim();
 if(!value){error.value=t('v15.invalidPassword');return false}
 if(value.length<8||value.length>20){error.value=t('v15.passwordRule');return false}
 return true;
}
async function submit(){
 if(busy.value||!guard())return;
 if(needPassword.value&&!passwordReady())return;
 if(needCode.value&&!code.value.trim()){error.value=t('v15.invalidCode');return}
 if((mode.value==='signup'||(needCode.value&&mode.value!=='recover'))&&!agreed.value){error.value=t('v15.mustAgree');return}
 if(mode.value==='signup'&&role.value==='enterprise'){
  if(enterpriseName.value.trim().length<2){error.value=t('v15.enterpriseRequired');return}
  if(!/^[0-9A-HJ-NPQRTUWXY]{18}$/.test(businessLicenseNo.value.trim().toUpperCase())){error.value=t('v15.licenseInvalid');return}
 }
 busy.value=true;error.value='';
 const phone=channel.value==='phone';
 try{
  if(mode.value==='recover'){
   acceptSession(await api.originalV15.resetPassword({channel:channel.value,contact:contactValue.value,code:code.value.trim(),newPassword:password.value.trim(),...phoneBody()}),phone);
  }else if(mode.value==='signin'){
   acceptSession(await api.originalV15.login({channel:channel.value,contact:contactValue.value,...phoneBody(),...(needCode.value?{code:code.value.trim()}:{password:password.value})}),phone);
  }else{
   acceptSession(await api.originalV15.signup({channel:channel.value,contact:contactValue.value,password:password.value.trim(),code:code.value.trim(),role:role.value,...phoneBody(),...(inviteCode.value?{inviteCode:inviteCode.value.trim()}:{}),...(role.value==='enterprise'?{enterpriseName:enterpriseName.value.trim(),businessLicenseNo:businessLicenseNo.value.trim().toUpperCase()}:{})}),phone);
  }
  passwordFailed.value=false;
 }catch(e){
  error.value=explain(e);
  if(mode.value==='signin'&&!needCode.value)passwordFailed.value=true;
 }finally{busy.value=false}
}
async function askSupport(){
 if(valid.value&&captchaId.value&&captchaText.value.trim().length===4){
  try{await api.originalV15.recoveryHelp({channel:channel.value,contact:contactValue.value,captchaId:captchaId.value,captchaCode:captchaText.value.trim(),...phoneBody()});notice.value=t('v15.supportSent')}catch(e){error.value=explain(e)}
  void refreshCaptcha();
 }
 platform.openSupport();
}
async function wechatLogin(){if(busy.value)return;if(!agreed.value){error.value=t('v15.mustAgree');return}busy.value=true;error.value='';try{
 if(!platform.wechatCode)throw new Error(t('v15.wechatUnavailable'));
 const code=await platform.wechatCode();
 if(!code)throw new Error(t('v15.wechatFailed'));
 acceptSession(await api.originalV15.wechat({code}),false);
}catch(e){error.value=e instanceof ApiError&&e.status===503?t('v15.wechatUnavailable'):e instanceof Error?e.message:t('v15.wechatFailed')}finally{busy.value=false}}
void refreshCaptcha();
return {mode,channel,contact,countryCode,password,code,inviteCode,role,enterpriseName,businessLicenseNo,withCode,agreed,busy,error,notice,passwordFailed,captchaImage,captchaText,codeWait,countryCodes,needCode,needPassword,sendCode,submit,wechatLogin,agreement,changeMode,openRecover,askSupport,refreshCaptcha,chooseCountry};
}
