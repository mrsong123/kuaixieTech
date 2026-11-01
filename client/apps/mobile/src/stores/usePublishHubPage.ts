import {computed} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {consumePublishEntry,publishLoginPath} from '../utils/publishEntry';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle,prototypeAccent} from '../utils/prototype';
import {usePublishComposer} from '@kuaixie/ui/usePublishComposer';
import {api} from '../api/client';
export function usePublishHubPage(){
 const prefs=preferenceRef;
 const rootStyle=computed(()=>mobilePreferenceStyle(prefs.value)+`--prototype-accent:${prototypeAccent(prefs.value.accent)};`);
 const composer=usePublishComposer(api);
 onShow(()=>{const entry=consumePublishEntry();if(entry){composer.changeMode(entry.mode);if(entry.region)composer.changeRegion(entry.region)}});
 function login(){uni.navigateTo({url:publishLoginPath(composer.selected.value,composer.region.value)})}
 return {...composer,prefs,rootStyle,login};
}
