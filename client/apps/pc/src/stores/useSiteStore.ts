import {computed,onMounted,onUnmounted,ref} from 'vue';import {createApiClient,createFetchTransport, getBearer,setBearer,type PublicCategory,type PublicProduct,type PublicTrip, productImage, catalogMatch, tripAvailability} from '@kuaixie/sdk';import {t, formatAmount, formatDate, transportLabel, localizedField, setLocale, getLocale, operationLabel, groupLabel, uiErrorMessage} from '@kuaixie/i18n';import {localeRef} from '@kuaixie/i18n/vue';import {createSiteApi} from '../api/client';
import {type Page,routeFromHash} from '../router';

export function useSiteStore(){










const locale=localeRef;
const showDisplaySettings=ref(false);
function changeLocale(event:Event){const selected=(event.target as HTMLSelectElement).value;setLocale(selected);localStorage.setItem('kx.locale',selected)}
const api = createSiteApi();


const page=ref<Page>(routeFromHash()); const menu=ref(false); const products=ref<PublicProduct[]>([]);const trips=ref<PublicTrip[]>([]);const categories=ref<PublicCategory[]>([]);const busy=ref(false);const loadError=ref('');const search=ref('');const activeCategory=ref('__all__');const tripSort=ref<'rank'|'latest'|'score'>('rank');const selectedItem=ref<unknown>(null);const credentials=ref('');const showAuth=ref(false);const toast=ref('');
const heroStats=computed(()=>{void localeRef.value;return [{n:t('pc.stat.journey'),t:t('pc.stat.journeyDesc')},{n:t('pc.stat.people'),t:t('pc.stat.peopleDesc')},{n:t('pc.stat.price'),t:t('pc.stat.priceDesc')}];});
const navItems:[Page,string][]=[['home','pc.nav.home'],['products','pc.nav.products'],['trips','pc.nav.trips'],['route','pc.nav.route'],['community','pc.nav.community'],['workspace','pc.nav.workspace']];
function nav(to:Page){page.value=to;location.hash=to;menu.value=false;selectedItem.value=null;window.scrollTo({top:0,behavior:'smooth'})}
function displayName(value:PublicProduct|PublicCategory){return localizedField(value as unknown as Record<string,unknown>,'name')}
function img(item:PublicProduct){return productImage(item)??''}
const filtered=computed(()=>products.value.filter(p=>catalogMatch(p,search.value,activeCategory.value==='__all__'?null:activeCategory.value)));
function money(v:unknown,currency?:string){return formatAmount(v as number|string|null|undefined,currency)}
function date(v?:string){return formatDate(v)}
function message(e:unknown){return uiErrorMessage(e)}
async function refresh(){busy.value=true;loadError.value='';const results=await Promise.allSettled([api.catalog.categories(100),api.catalog.products(100),api.catalog.trips(100,tripSort.value)]);if(results[0].status==='fulfilled')categories.value=results[0].value; if(results[1].status==='fulfilled')products.value=results[1].value;if(results[2].status==='fulfilled')trips.value=results[2].value; const failures=results.filter(x=>x.status==='rejected');if(failures.length)loadError.value=t('common.apiLoadError');busy.value=false}
onMounted(()=>{window.addEventListener('hashchange',syncHash);void refresh()});onUnmounted(()=>window.removeEventListener('hashchange',syncHash));function syncHash(){page.value=routeFromHash()}
async function changeTripSort(){busy.value=true;loadError.value='';try{trips.value=await api.catalog.trips(100,tripSort.value)}catch{loadError.value=t('common.apiLoadError')}finally{busy.value=false}}
function selectedProduct(value:unknown):value is PublicProduct{return !!value&&typeof value==='object'&&'name' in value}
function selectedTrip(value:unknown):value is PublicTrip{return !!value&&typeof value==='object'&&'destination' in value}
const selectedCategoryName=computed(()=>{void localeRef.value;const item=selectedItem.value;if(!selectedProduct(item))return '';const c=categories.value.find(c=>String(c.id)===String(item.category_id));return c?displayName(c):t('m2.catalog.categoryUnknown')});
function useToken(){setBearer(credentials.value);showAuth.value=false;toast.value=t('common.tokenSet');setTimeout(()=>toast.value='',3000)}

return {locale,showDisplaySettings,changeLocale,api,page,menu,products,trips,categories,busy,loadError,search,activeCategory,tripSort,selectedItem,credentials,showAuth,toast,heroStats,navItems,nav,displayName,img,filtered,money,date,message,refresh,changeTripSort,selectedProduct,selectedTrip,selectedCategoryName,useToken};
}
export type SiteStore=ReturnType<typeof useSiteStore>;
