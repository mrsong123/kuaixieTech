import {ref,computed} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,type BackendRecord} from '@kuaixie/sdk';
import {api} from '../api/client';
import {t} from '@kuaixie/i18n';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';

export function useV15AdminPage(){
const prefs=preferenceRef,stats=ref<Record<string,unknown>|null>(null),records=ref<{demands:BackendRecord[];trips:BackendRecord[];posts:BackendRecord[]}|null>(null),busy=ref(false),error=ref(''),active=ref<'demands'|'trips'|'posts'>('demands'),serverAuthorized=ref(false);
const authorized=computed(()=>serverAuthorized.value);const rows=computed(()=>records.value?.[active.value]||[]);const tabs=['demands','trips','posts'] as const;const labels=['v15.adminDemand','v15.adminTrip','v15.adminPost'];
// Fields are verified against AdminReviewRpcMapper.xml; rendering only bounded numeric metrics, not sensitive arbitrary record blobs.
const fields=['users_total','users_week','carriers_approved','buyers_verified','trips_active','orders_open','orders_done','gmv_total','gmv_week','service_fee_mtd','commission_mtd','todo_carrier_kyc','todo_buyer_kyc','todo_trips','todo_disputes','todo_cancels','todo_pools','todo_demands','todo_support','todo_invoices','todo_violations','products_on','risk_orders'];
const values=computed(()=>fields.filter(key=>stats.value&&stats.value[key]!==undefined&&stats.value[key]!==null).map(key=>({key,label:'v15.adminMetric.'+key,value:String(stats.value?.[key])})));
onShow(()=>{void load()});async function load(){error.value='';records.value=null;stats.value=null;serverAuthorized.value=false;if(!getBearer()){error.value=t('v15.loginRequired');return}busy.value=true;try{
// This RPC exists in the original Java server and invokes DomainAccessGuard.requireAdmin().
const result=await api.rpc('admin_stats',{});if(!result||typeof result!=='object'||Array.isArray(result)||!('users_total' in result))throw new Error(t('v15.loadFail'));
stats.value=result as Record<string,unknown>;serverAuthorized.value=true;
// Optional portal add-on may be absent. Its failure must NOT erase real Spring Boot admin metrics.
try{const r=await api.portal.adminReviews();if(r&&Array.isArray(r.demands)&&Array.isArray(r.trips)&&Array.isArray(r.posts))records.value=r}catch{records.value=null}
}catch(e){error.value=e instanceof Error?e.message:t('v15.adminNotAllowed')}finally{busy.value=false}}
function title(r:BackendRecord){return String(r.title||r.destination||r.caption||r.name||r.id||'—')}
function status(r:BackendRecord){return String(r.status||r.review_status||'—')}
return {prefs,stats,records,busy,error,active,authorized,rows,tabs,labels,values,load,title,status,mobilePreferenceStyle};
}
