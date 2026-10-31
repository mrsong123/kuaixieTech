
import {computed,ref,onUnmounted,nextTick} from 'vue';

import {onShow,onHide} from '@dcloudio/uni-app';

import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle,prototypeAccent} from '../utils/prototype';

import {getBearer} from '@kuaixie/sdk';

import type {SupportConversation,SupportMessage} from '@kuaixie/sdk';

import {api} from '../api/client';

import {t,getLocale} from '@kuaixie/i18n';

export function useMessagesPage(){

const prefs=preferenceRef;
const rootStyle=computed(()=>mobilePreferenceStyle(prefs.value)+`--prototype-accent:${prototypeAccent(prefs.value.accent)};`);
const session=ref<SupportConversation|null>(null),messages=ref<SupportMessage[]>([]);
const draft=ref(''),busy=ref(false),loading=ref(false),banner=ref('');
const toolsOpen=ref(false),localAttachment=ref<{kind:'image'|'video';path:string}|null>(null);
const bottomAnchor=ref('chat-bottom');
let refreshInterval:ReturnType<typeof setInterval>|undefined;
const isHuman=computed(()=>session.value?.status==='human');
const isWaiting=computed(()=>session.value?.status==='waiting');
const hasLogin=computed(()=>!!getBearer());
function scrollBottom(){bottomAnchor.value='';nextTick(()=>{bottomAnchor.value='chat-bottom'})}
async function refresh(quiet=false){
 if(!session.value)return;
 try{
  const [status,rows]=await Promise.all([api.support.get(session.value.id),api.support.messages(session.value.id,80)]);
  const before=session.value.status;session.value=status;messages.value=rows;
  if(before==='waiting'&&status.status==='human')banner.value='portal.chat.agentJoined';
  scrollBottom();
 }catch{if(!quiet)banner.value='portal.chat.sessionError'}
}
async function connect(){
 if(!hasLogin.value){banner.value='portal.chat.loginNeeded';return}
 loading.value=true;banner.value='';
 try{session.value=await api.support.open();await refresh()}catch{banner.value='portal.chat.networkUnavailable'}
 finally{loading.value=false}
}
onShow(()=>{
 void connect();
 if(refreshInterval)clearInterval(refreshInterval);
 refreshInterval=setInterval(()=>{if(session.value)void refresh(true)},10000);
});
onHide(()=>{if(refreshInterval){clearInterval(refreshInterval);refreshInterval=undefined}});
onUnmounted(()=>{if(refreshInterval)clearInterval(refreshInterval)});
const isTransfer=(value:string)=>['转人工','人工客服','真人客服','human','human agent','agent','transfer to human'].includes(value.toLowerCase().trim());
async function send(){
 if(busy.value)return;
 const value=draft.value.trim();
 if(!value)return;
 if(localAttachment.value){banner.value='portal.chat.multimediaUnavailable';return}
 if(!hasLogin.value){banner.value='portal.chat.loginNeeded';return}
 busy.value=true;banner.value='';
 try{
  if(!session.value)session.value=await api.support.open();
  if(isTransfer(value)){
   // Use the ordinary message endpoint for the explicit handoff phrase so the
   // user's request appears in the persisted conversation as well as the queue.
   const result=await api.support.send(session.value.id,value);
   draft.value='';banner.value=result.status==='human'?'portal.chat.agentJoined':'portal.chat.queued';
  }else{
   const result=await api.support.send(session.value.id,value);
   draft.value='';
   if(result.aiStatus==='unavailable')banner.value='portal.chat.aiUnavailable';
   else if(result.status==='waiting')banner.value='portal.chat.humanOnly';
  }
  await refresh(true);
 }catch(e){
  const status=typeof e==='object'&&e!==null&&'status'in e?Number((e as {status:unknown}).status):0;
  banner.value=status===404?'portal.chat.transferUnavailable':'portal.chat.networkUnavailable';
 }finally{busy.value=false}
}
function chooseMedia(kind:'image'|'video'){
 toolsOpen.value=false;
 if(kind==='image')uni.chooseImage({count:1,sizeType:['compressed'],sourceType:['album','camera'],success:r=>{const path=r.tempFilePaths?.[0];if(path)localAttachment.value={kind:'image',path};banner.value='portal.chat.attachmentPending'}});
 else uni.chooseVideo({sourceType:['album','camera'],compressed:true,maxDuration:30,success:r=>{if(r.tempFilePath)localAttachment.value={kind:'video',path:r.tempFilePath};banner.value='portal.chat.attachmentPending'}});
}
function removeMedia(){localAttachment.value=null;banner.value=''}
interface RecognizerLike{lang:string;onresult:((event:{results:ArrayLike<{[index:number]:{transcript:string}}>} )=>void)|null;onend:(()=>void)|null;start():void}
interface VoiceEnv{SpeechRecognition?:new()=>RecognizerLike;webkitSpeechRecognition?:new()=>RecognizerLike}
function voiceDraft(){
 toolsOpen.value=false;
 // #ifdef H5
 const scope=window as unknown as VoiceEnv;
 const Ctor=scope.SpeechRecognition||scope.webkitSpeechRecognition;
 if(!Ctor){banner.value='voice.unavailable';return}
 const rec=new Ctor();rec.lang=getLocale();const prefix=draft.value.trim();
 rec.onresult=e=>{let words='';for(let i=0;i<e.results.length;i++){words+=e.results[i]?.[0]?.transcript||''}draft.value=[prefix,words.trim()].filter(Boolean).join(' ')};
 rec.onend=()=>{};
 try{rec.start()}catch{banner.value='portal.chat.noPermission'}
 // #endif
 // #ifndef H5
 banner.value='voice.unavailable';
 // #endif
}

return {prefs,rootStyle,session,messages,draft,busy,loading,banner,toolsOpen,localAttachment,bottomAnchor,refreshInterval,isHuman,isWaiting,hasLogin,scrollBottom,refresh,connect,isTransfer,send,chooseMedia,removeMedia,voiceDraft};
}
