import {initPreferences} from '@kuaixie/preferences';
import {setLocale} from '@kuaixie/i18n';
import { installI18n } from '@kuaixie/i18n/vue';
import { createApp } from 'vue';import App from './App.vue';import './styles/app.css';setLocale(localStorage.getItem('kx.locale') || navigator.language);
initPreferences({getItem:key=>localStorage.getItem(key),setItem:(key,value)=>localStorage.setItem(key,value)});installI18n(createApp(App)).mount('#app');
