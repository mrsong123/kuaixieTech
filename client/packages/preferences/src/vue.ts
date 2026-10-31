import {ref,readonly} from 'vue';
import {getPreferences,subscribePreferences,initPreferences,setPreferences,type DisplayPreferences} from './core';
const state=ref<DisplayPreferences>(getPreferences());
subscribePreferences(value=>{state.value=value});
export const preferenceRef=readonly(state);
export {initPreferences,setPreferences};
