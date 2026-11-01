
import {computed,onMounted,ref} from 'vue';

import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle,prototypeAccent,prototypeContrast} from '../utils/prototype';

import {t,formatAmount,formatDate,transportLabel} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';

import type {PublicDemand,PublicTrip,VerifiedPortalRole,PublicCategory,PublicProduct} from '@kuaixie/sdk';

import {getBearer,productImage,catalogMatch,categoryCountryCodes,tripRegion,squareTripMatches,squareDemandMatches,productCountryMatches} from '@kuaixie/sdk';

import {api,navigate} from '../api/client';

import {mobileTripDetails} from '../utils/catalog';

export function useIndexPage(){


const prefs=preferenceRef;
const trips=ref<PublicTrip[]>([]),demands=ref<PublicDemand[]>([]),categories=ref<PublicCategory[]>([]),products=ref<PublicProduct[]>([]);
const role=ref<VerifiedPortalRole|null>(null);
const scopeOptions=['all','domestic','international'] as const;
const active=ref<'trips'|'demands'>('trips');
const region=ref<'all'|'domestic'|'international'>('all');
const direction=ref<'all'|'outbound'|'inbound'>('all');
const country=ref('');
const countryChoices=computed(()=>{const codes=[...new Set(categories.value.flatMap(categoryCountryCodes))].sort();const language=localeRef.value;let names:Intl.DisplayNames|undefined;try{names=new Intl.DisplayNames([language],{type:'region'})}catch{};return [{code:'',name:t('prototype.plaza.countryAll')},...codes.map(code=>({code,name:names?.of(code)||code}))]});
const countryIndex=computed(()=>Math.max(0,countryChoices.value.findIndex(item=>item.code===country.value)));
function countryChanged(e:{detail:{value:string|number}}){const index=Number(e.detail.value);country.value=countryChoices.value[index]?.code||''}
function nearbyHint(){uni.showToast({title:t('prototype.plaza.nearUnavailable'),icon:'none',duration:2500})}
const search=ref(''),loading=ref(false),tripError=ref(false),demandError=ref(false),productError=ref(false);
const accent=computed(()=>prototypeAccent(prefs.value.accent));
const rootStyle=computed(()=>{
 const override=prefs.value.accent&&accent.value!=='#0ea5e9'?`--brand-gradient:linear-gradient(135deg,${accent.value},${accent.value});`:'';
 return mobilePreferenceStyle(prefs.value)+`--prototype-accent:${accent.value};--prototype-accent-text:${prototypeContrast(prefs.value.accent)};`+override;
});
const typeOf=tripRegion;
async function reload(){
 loading.value=true;tripError.value=false;demandError.value=false;productError.value=false;
 const [tripResult,demandResult,categoryResult,productResult]=await Promise.allSettled([api.catalog.trips(100),api.portal.demands(100),api.catalog.categories(100),api.catalog.products(100)]);
 if(productResult.status==='fulfilled')products.value=productResult.value;else{products.value=[];productError.value=true}
 if(categoryResult.status==='fulfilled')categories.value=categoryResult.value;else categories.value=[];
 if(tripResult.status==='fulfilled')trips.value=tripResult.value;else{trips.value=[];tripError.value=true}
 if(demandResult.status==='fulfilled')demands.value=demandResult.value;else{demands.value=[];demandError.value=true}
 if(getBearer()){
   try{role.value=await api.portal.verifiedRole();
     if(role.value?.user_role==='carrier'&&role.value.carrier_verify_status==='approved')active.value='demands';
     else if(role.value?.user_role==='buyer')active.value='trips';
   }catch{role.value=null}
 }else role.value=null;
 loading.value=false;
}
onMounted(reload);
const roleMessageKey=computed(()=>{
 if(role.value?.user_role==='carrier'&&role.value.carrier_verify_status==='approved')return 'portal.square.roleCarrier';
 if(role.value?.user_role==='buyer')return 'portal.square.roleBuyer';
 return role.value?'portal.square.roleOther':'portal.square.rolesUnavailable';
});
const visibleTrips=computed(()=>trips.value.filter(x=>squareTripMatches(x,search.value,region.value,direction.value)));
const visibleDemands=computed(()=>demands.value.filter(x=>squareDemandMatches(x,search.value,region.value)));
// Country associations come from categoryCountryCodes, including ancestor categories.
const matchingProducts=computed(()=>products.value.filter(x=>catalogMatch(x,search.value.trim(),null)&&productCountryMatches(x,categories.value,country.value)).slice(0,10));
const matchingTrips=computed(()=>trips.value.filter(x=>[x.destination,x.entry_port,x.transport_mode,x.trip_type].some(v=>String(v??'').toLocaleLowerCase().includes(search.value.trim().toLocaleLowerCase()))).slice(0,10));
const matchingDemands=computed(()=>demands.value.filter(x=>[x.title,x.city,x.description].some(v=>String(v??'').toLocaleLowerCase().includes(search.value.trim().toLocaleLowerCase()))).slice(0,10));
const searchTotal=computed(()=>matchingProducts.value.length+matchingTrips.value.length+matchingDemands.value.length);
function viewProduct(x:PublicProduct){uni.navigateTo({url:'/pages/product-detail/index?id='+encodeURIComponent(String(x.id))})}
function viewTrip(x:PublicTrip){uni.navigateTo({url:'/pages/trip-detail/index?id='+encodeURIComponent(String(x.id))})}
function viewDemand(x:PublicDemand){uni.navigateTo({url:'/pages/demand-detail/index?id='+encodeURIComponent(String(x.id))})}
function openCatalog(){uni.navigateTo({url:'/pages/catalog/index'})}
function choose(tab:'trips'|'demands'){active.value=tab}

return {prefs,trips,demands,role,scopeOptions,active,region,search,loading,tripError,demandError,accent,rootStyle,typeOf,reload,roleMessageKey,visibleTrips,visibleDemands,viewTrip,viewDemand,viewProduct,openCatalog,choose,formatDate,transportLabel,formatAmount,productImage,matchingProducts,matchingTrips,matchingDemands,searchTotal,productError,country,countryChoices,countryIndex,countryChanged,direction,nearbyHint};
}
