
import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';


import {onMounted,ref} from 'vue';

import {api} from '../api/client';

import {type PublicTrip,tripAvailability} from '@kuaixie/sdk';

import {formatDate,transportLabel} from '@kuaixie/i18n';

import {mobileTripDetails} from '../utils/catalog';

export function useTripsPage(){

const prefs=preferenceRef;
const trips=ref<PublicTrip[]>([]);const sort=ref<'rank'|'latest'|'score'>('rank');const loading=ref(false);const error=ref(false);
async function load(){loading.value=true;error.value=false;try{trips.value=await api.catalog.trips(100,sort.value)}catch{trips.value=[];error.value=true}finally{loading.value=false}}
function open(t:PublicTrip){uni.navigateTo({url:'/pages/trip-detail/index?id='+encodeURIComponent(String(t.id))})}
function back(){uni.navigateBack()}
function changeSort(e:{detail:{value:number|string}}){const all=['rank','latest','score'] as const;sort.value=all[Number(e.detail.value)]??'rank';load()}
onMounted(load);

return {prefs,trips,sort,loading,error,load,open,back,changeSort,mobilePreferenceStyle,tripAvailability,formatDate,transportLabel};
}
