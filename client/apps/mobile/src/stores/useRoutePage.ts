
import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';


import { onMounted, ref } from 'vue';

import { api } from '../api/client';

import {getLocale} from '@kuaixie/i18n';

import type { PublicTrip } from '@kuaixie/sdk';

export function useRoutePage(){

const prefs=preferenceRef;
const trips = ref<PublicTrip[]>([]);
const error = ref('');
const loading = ref(false);
async function reload(){loading.value=true;error.value='';try{trips.value=await api.catalog.trips(30,'latest')}catch(e){error.value=e instanceof Error?e.message:String(e)}finally{loading.value=false}}
onMounted(reload);
interface SpeechInput {lang:string;continuous:boolean;interimResults:boolean;onresult:((e:{results:ArrayLike<{[index:number]:{transcript:string}}>} )=>void)|null;onerror:(()=>void)|null;onend:(()=>void)|null;start():void;stop():void}
interface VoiceWindow {SpeechRecognition?:new()=>SpeechInput;webkitSpeechRecognition?:new()=>SpeechInput}
const draft=ref('');const confirmed=ref(false);const listening=ref(false);const voiceError=ref('');
let recognizer:SpeechInput|undefined;
const supported=typeof window!=='undefined'&&!!((window as unknown as VoiceWindow).SpeechRecognition||(window as unknown as VoiceWindow).webkitSpeechRecognition);
function startVoice(){if(!supported)return;const Ctor=(window as unknown as VoiceWindow).SpeechRecognition||(window as unknown as VoiceWindow).webkitSpeechRecognition;if(!Ctor)return;
 const prefix=draft.value;confirmed.value=false;voiceError.value='';recognizer=new Ctor();recognizer.lang=getLocale();recognizer.continuous=true;recognizer.interimResults=true;
 recognizer.onresult=e=>{const words=[] as string[];for(let i=0;i<e.results.length;i++){const v=e.results[i]?.[0]?.transcript;if(v)words.push(v)}draft.value=[prefix,words.join(' ')].filter(Boolean).join(' ').trim()};recognizer.onerror=()=>{listening.value=false;voiceError.value='voice.error'};recognizer.onend=()=>{listening.value=false};try{recognizer.start();listening.value=true}catch{voiceError.value='voice.error';listening.value=false}}
function stopVoice(){recognizer?.stop();listening.value=false}


return {prefs,trips,error,loading,reload,draft,confirmed,listening,voiceError,recognizer,supported,startVoice,stopVoice,mobilePreferenceStyle};
}
