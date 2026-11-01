import {computed,onMounted,onUnmounted,ref} from 'vue';
import {getBearer,setBearer,clearBearer,subscribeSession,type PublicCategory,type PublicProduct,type PublicTrip,type PublicDemand,type PublicCommunityPost,productImage,catalogMatch} from '@kuaixie/sdk';
import {t,formatAmount,formatDate,localizedField,setLocale,uiErrorMessage} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';
import {createSiteApi} from '../api/client';
import {type Page,routeFromHash} from '../router';

type HallTrip=PublicTrip & {origin_city?:string};

export function useSiteStore(){
 const locale=localeRef,api=createSiteApi();
 const sessionScope=ref(0),token=ref(getBearer()),showDisplaySettings=ref(false);
 const page=ref<Page>(routeFromHash()),menu=ref(false),products=ref<PublicProduct[]>([]),trips=ref<HallTrip[]>([]),categories=ref<PublicCategory[]>([]),demands=ref<PublicDemand[]>([]),posts=ref<PublicCommunityPost[]>([]);
 const busy=ref(false),loadError=ref(''),search=ref(''),searchScope=ref<'all'|'products'|'trips'|'demands'>('all'),activeCategory=ref('__all__'),tripSort=ref<'rank'|'latest'|'score'>('rank');
 const selectedItem=ref<unknown>(null),credentials=ref(''),showAuth=ref(false),toast=ref('');
 let refreshGeneration=0;
 const unsubscribeSession=subscribeSession(()=>{sessionScope.value++;token.value=getBearer();selectedItem.value=null;credentials.value='';showAuth.value=false});
 onUnmounted(unsubscribeSession);
 function changeLocale(event:Event){const selected=(event.target as HTMLSelectElement).value;setLocale(selected);localStorage.setItem('kx.locale',selected)}
 const signedIn=computed(()=>Boolean(token.value));
 const heroStats=computed(()=>{void localeRef.value;return [{n:t('pc.stat.journey'),t:t('pc.stat.journeyDesc')},{n:t('pc.stat.people'),t:t('pc.stat.peopleDesc')},{n:t('pc.stat.price'),t:t('pc.stat.priceDesc')}];});
 const navItems:[Page,string][]=[['home','pc.nav.home'],['products','pc.nav.products'],['trips','pc.nav.trips'],['route','pc.nav.route'],['community','pc.nav.community'],['workspace','pc.nav.workspace'],['business','pc.nav.business']];
 function nav(to:Page){page.value=to;location.hash=to;menu.value=false;selectedItem.value=null;window.scrollTo({top:0,behavior:'smooth'})}
 function displayName(value:PublicProduct|PublicCategory){return localizedField(value as unknown as Record<string,unknown>,'name')}
 function img(item:PublicProduct){return productImage(item,api.files.publicUrl)??''}
 const filtered=computed(()=>products.value.filter(p=>catalogMatch(p,search.value,activeCategory.value==='__all__'?null:activeCategory.value)));
 const match=(values:unknown[])=>{const q=search.value.trim().toLowerCase();return !q||values.some(value=>String(value??'').toLowerCase().includes(q))};
 const listedTrips=computed(()=>trips.value.filter(item=>match([item.origin_city,item.destination])));
 const listedDemands=computed(()=>demands.value.filter(item=>match([item.title,item.description,item.city])));
 function submitSearch(){if(searchScope.value==='trips'){nav('trips');return}if(searchScope.value==='demands'){nav('route');return}if(searchScope.value==='products'||!search.value.trim()||filtered.value.length){nav('products');return}nav(listedTrips.value.length?'trips':'route')}
 function money(v:unknown,currency?:string){return formatAmount(v as number|string|null|undefined,currency)}
 function date(v?:string){return formatDate(v)}
 function message(e:unknown){return uiErrorMessage(e)}
 async function refresh(){const generation=++refreshGeneration;busy.value=true;loadError.value='';const results=await Promise.allSettled([api.catalog.categories(100),api.catalog.products(100),api.catalog.trips(100,tripSort.value),api.portal.demands(100),api.portal.communityFeed(100)]);if(generation!==refreshGeneration)return;if(results[0].status==='fulfilled')categories.value=results[0].value;else categories.value=[];if(results[1].status==='fulfilled')products.value=results[1].value;else products.value=[];if(results[2].status==='fulfilled')trips.value=results[2].value;else trips.value=[];if(results[3].status==='fulfilled')demands.value=results[3].value;else demands.value=[];if(results[4].status==='fulfilled')posts.value=results[4].value;else posts.value=[];if(results.some(x=>x.status==='rejected'))loadError.value=t('common.apiLoadError');busy.value=false}
 function syncHash(){page.value=routeFromHash();selectedItem.value=null}
 onMounted(()=>{window.addEventListener('hashchange',syncHash);void refresh()});onUnmounted(()=>{refreshGeneration++;window.removeEventListener('hashchange',syncHash)});
 async function changeTripSort(){await refresh()}
 function selectedProduct(value:unknown):value is PublicProduct{return !!value&&typeof value==='object'&&'name' in value}
 function selectedTrip(value:unknown):value is PublicTrip{return !!value&&typeof value==='object'&&'destination' in value}
 function selectedDemand(value:unknown):value is PublicDemand{return !!value&&typeof value==='object'&&'title' in value&&!('destination'in value)&&!('name'in value)}
 const selectedCategoryName=computed(()=>{void localeRef.value;const item=selectedItem.value;if(!selectedProduct(item))return '';const c=categories.value.find(c=>String(c.id)===String(item.category_id));return c?displayName(c):t('m2.catalog.categoryUnknown')});
 function useToken(){setBearer(credentials.value);showAuth.value=false;toast.value=t('common.tokenSet');setTimeout(()=>toast.value='',3000)}
 function logout(){clearBearer();nav('home')}
 return {sessionScope,locale,showDisplaySettings,changeLocale,api,page,menu,products,trips,categories,demands,posts,busy,loadError,search,searchScope,submitSearch,activeCategory,tripSort,selectedItem,credentials,showAuth,toast,heroStats,navItems,nav,displayName,img,filtered,listedTrips,listedDemands,money,date,message,refresh,changeTripSort,selectedProduct,selectedTrip,selectedDemand,selectedCategoryName,useToken,token,signedIn,logout};
}
export type SiteStore=ReturnType<typeof useSiteStore>;
