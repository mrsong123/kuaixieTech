import {computed} from 'vue';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle,prototypeAccent} from '../utils/prototype';
import {usePublishComposer} from '@kuaixie/ui/usePublishComposer';
import {api} from '../api/client';
export function usePublishHubPage(){
 const prefs=preferenceRef;
 const rootStyle=computed(()=>mobilePreferenceStyle(prefs.value)+`--prototype-accent:${prototypeAccent(prefs.value.accent)};`);
 return {...usePublishComposer(api),prefs,rootStyle};
}
