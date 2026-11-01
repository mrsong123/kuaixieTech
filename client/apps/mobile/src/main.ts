import {initPreferences} from '@kuaixie/preferences';
import {setLocale} from '@kuaixie/i18n';
import { installI18n } from '@kuaixie/i18n/vue';
import {createSSRApp} from 'vue';import App from './App.vue';export function createApp(){try{initPreferences({getItem:key=>String(uni.getStorageSync(key)||''),setItem:(key,value)=>uni.setStorageSync(key,value)});const l=uni.getStorageSync('kx.locale');if(l)setLocale(String(l))}catch{};const app=createSSRApp(App);app.config.globalProperties.uni=uni;installI18n(app);return {app}}
