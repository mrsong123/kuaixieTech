import {ref,computed} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {api} from '../api/client';
import type {PublicProduct,PublicTrip} from '@kuaixie/sdk';
import {mobileProductName} from '../utils/catalog';
import {getLocale,formatDate,formatAmount} from '@kuaixie/i18n';
const normalize=(value:unknown)=>String(value??'').toLocaleLowerCase(getLocale());
export function useUnifiedSearchPage(){
 const products=ref<PublicProduct[]>([]),trips=ref<PublicTrip[]>([]),term=ref('');
 const busy=ref(false),failed=ref(false),kind=ref<'all'|'products'|'trips'>('all');
 const filterKinds=[{value:'all',labelKey:'settings2.search.all'},{value:'products',labelKey:'settings2.search.products'},{value:'trips',labelKey:'settings2.search.trips'}] as const;
 function chooseKind(value:'all'|'products'|'trips'){kind.value=value}
 const productMatches=computed(()=>kind.value==='trips'?[]:products.value.filter(p=>[mobileProductName(p),p.region,p.spec].some(x=>normalize(x).includes(normalize(term.value.trim())))));
 const tripMatches=computed(()=>kind.value==='products'?[]:trips.value.filter(t=>[t.destination,t.entry_port,t.transport_mode].some(x=>normalize(x).includes(normalize(term.value.trim())))));
 async function reload(){busy.value=true;failed.value=false;const res=await Promise.allSettled([api.catalog.products(100),api.catalog.trips(100)]);
  if(res[0].status==='fulfilled')products.value=res[0].value;else {products.value=[];failed.value=true}
  if(res[1].status==='fulfilled')trips.value=res[1].value;else {trips.value=[];failed.value=true}
  busy.value=false;
 }
 function openProduct(p:PublicProduct){uni.navigateTo({url:'/pages/product-detail/index?id='+encodeURIComponent(String(p.id))})}
 function openTrip(trip:PublicTrip){uni.navigateTo({url:'/pages/trip-detail/index?id='+encodeURIComponent(String(trip.id))})}
 onLoad((params)=>{if(typeof params?.q==='string')term.value=params.q;void reload()});
 return {products,trips,term,busy,failed,kind,filterKinds,chooseKind,productMatches,tripMatches,formatDate,formatAmount,mobileProductName,openProduct,openTrip,reload};
}
