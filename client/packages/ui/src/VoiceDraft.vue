<script setup lang="ts">
import {computed,onUnmounted,ref} from 'vue';
import {getLocale} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';
interface RecognitionResult {isFinal:boolean;[index:number]:{transcript:string}}
interface VoiceRecognition {
  lang:string;continuous:boolean;interimResults:boolean;
  onresult:((e:{results:ArrayLike<RecognitionResult>})=>void)|null;
  onerror:((e:unknown)=>void)|null;onend:(()=>void)|null;
  start():void;stop():void;
}
interface RecognitionWindow {SpeechRecognition?:new()=>VoiceRecognition;webkitSpeechRecognition?:new()=>VoiceRecognition}
const text=ref('');const listening=ref(false);const confirmed=ref(false);const error=ref('');const spokenBase=ref('');
let recognition:VoiceRecognition|undefined;
const available=typeof window!=='undefined'&&!!((window as unknown as RecognitionWindow).SpeechRecognition||(window as unknown as RecognitionWindow).webkitSpeechRecognition);
const status=computed(()=>{void localeRef.value;return listening.value?'voice.listening':available?'voice.permission':'voice.unavailable'});
function start(){if(!available)return;
 const ctor=(window as unknown as RecognitionWindow).SpeechRecognition||(window as unknown as RecognitionWindow).webkitSpeechRecognition;
 if(!ctor)return;
 error.value='';confirmed.value=false;spokenBase.value=text.value.trim();recognition=new ctor();recognition.lang=getLocale();recognition.continuous=true;recognition.interimResults=true;
 recognition.onresult=e=>{const segments:Array<string>=[];for(let i=0;i<e.results.length;i++){const segment=e.results[i];if(segment?.[0]?.transcript)segments.push(segment[0].transcript)}text.value=[spokenBase.value,segments.join(' ')].filter(Boolean).join(' ').trim()};
 recognition.onerror=()=>{error.value='voice.error';listening.value=false};recognition.onend=()=>{listening.value=false};
 try{recognition.start();listening.value=true}catch{error.value='voice.error';listening.value=false}
}
function stop(){recognition?.stop();listening.value=false}
onUnmounted(stop);
</script>
<template>
<section class="kx-preferences kx-voice" :aria-label="$t('voice.title')">
  <h2 class="kx-preferences__head">{{ $t('voice.title') }}</h2>
  <p class="kx-preferences__hint">{{ $t(status) }}</p>
  <div class="kx-voice__toolbar">
    <button type="button" :disabled="!available||listening" @click="start">{{ $t('voice.start') }}</button>
    <button type="button" :disabled="!listening" @click="stop">{{ $t('voice.stop') }}</button>
  </div>
  <label class="kx-preferences__field">{{ $t('voice.draft') }}
    <textarea v-model="text" rows="4" :placeholder="$t('voice.placeholder')" @input="confirmed=false" />
  </label>
  <p v-if="error" role="alert" class="kx-preferences__hint">{{ $t(error) }}</p>
  <p class="kx-preferences__hint">{{ $t('voice.review') }}</p>
  <label class="kx-preferences__toggle"><input type="checkbox" v-model="confirmed" :disabled="!text.trim()" />{{ $t('voice.confirm') }}</label>
  <p class="kx-preferences__hint">{{ confirmed?$t('voice.ready'):$t('voice.noSubmit') }}</p>
</section>
</template>
<style scoped>
.kx-voice__toolbar{display:flex;gap:10px;flex-wrap:wrap;margin:12px 0}
.kx-voice__toolbar button{border:1px solid var(--kx-border);border-radius:9px;background:var(--kx-accent);color:var(--kx-accent-text);padding:8px 14px;min-height:42px}
.kx-voice__toolbar button:disabled{opacity:.45;cursor:not-allowed}
.kx-voice textarea {display:block;width:100%;box-sizing:border-box;max-width:100%;border:1px solid var(--kx-border);border-radius:10px;padding:10px;font:inherit;color:var(--kx-ink);background:var(--kx-surface)}
</style>
