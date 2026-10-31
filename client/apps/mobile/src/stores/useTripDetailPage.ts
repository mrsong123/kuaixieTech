import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {api} from '../api/client';
import {mobilePreferenceStyle} from '../utils/prototype';
import {formatDate,transportLabel} from '@kuaixie/i18n';
import {tripAvailability,type PublicTrip} from '@kuaixie/sdk';
export function useTripDetailPage(){
 const prefs=preferenceRef;const item=ref<PublicTrip|null>(null);const id=ref('');const loading=ref(false);const unavailable=ref(false);
 async function load(){loading.value=true;unavailable.value=false;try{const rows=await api.catalog.trips(100);item.value=rows.find(x=>String(x.id)===id.value)??null}catch{item.value=null;unavailable.value=true}finally{loading.value=false}}
 onLoad((params)=>{id.value=String(params?.id??'');void load()});
 return {prefs,item,loading,unavailable,mobilePreferenceStyle,formatDate,transportLabel,tripAvailability,load};
}
