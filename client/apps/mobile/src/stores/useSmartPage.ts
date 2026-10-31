
import {computed,onMounted,ref} from 'vue';

import {api} from '../api/client';

import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';

import {localizedField,formatAmount,t} from '@kuaixie/i18n';

import type {PublicProduct,PublicTrip} from '@kuaixie/sdk';

export function useSmartPage(){

const prefs=preferenceRef;
const products=ref<PublicProduct[]>([]),trips=ref<PublicTrip[]>([]),failure=ref(''),busy=ref(false);
const a=ref(0),b=ref(1);
const left=computed(()=>products.value[a.value]);
const right=computed(()=>products.value[b.value]);
const names=computed(()=>products.value.map(item=>localizedField(item as unknown as Record<string,unknown>,'name')));
function pickA(e:{detail:{value:number}}){a.value=Number(e.detail.value)}
function pickB(e:{detail:{value:number}}){b.value=Number(e.detail.value)}
async function load(){busy.value=true;failure.value='';const [one,two]=await Promise.allSettled([api.catalog.products(100),api.catalog.trips(30)]);if(one.status==='fulfilled')products.value=one.value;else failure.value=t('common.apiLoadError');if(two.status==='fulfilled')trips.value=two.value;else failure.value=t('common.apiLoadError');busy.value=false}
onMounted(load);
function name(p?:PublicProduct){return p?localizedField(p as unknown as Record<string,unknown>,'name'):t('common.notProvided')}
function price(p?:PublicProduct){return p?formatAmount(p.price,p.currency):t('common.notProvided')}

return {prefs,products,trips,failure,busy,a,b,left,right,names,pickA,pickB,load,name,price,mobilePreferenceStyle};
}
