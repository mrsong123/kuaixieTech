import {computed,ref,proxyRefs,getCurrentScope,onScopeDispose} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,subscribeSession,type MyOrder} from '@kuaixie/sdk';
import {t,formatAmount,formatDate,formatBusinessStatus} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
/** Only the authenticated user's orders are queried. This is not a public open-intent or carrier-tracking API. */
export function useOriginalOrderList(kind:'shipment'|'open'){
 const prefs=preferenceRef,session=ref(getBearer());let alive=true,generation=0,verificationGeneration=0;
 const role=ref<'buyer'|'carrier'>(kind==='open'?'carrier':'buyer');
 const rows=ref<MyOrder[]>([]),busy=ref(false),error=ref(''),hasMore=ref(false),loaded=ref(false),rawOffset=ref(0);
 const canRead=computed(()=>Boolean(session.value));
 const verified=ref<boolean|null>(null),verificationError=ref('');
 function clear(){generation++;verificationGeneration++;rows.value=[];busy.value=false;error.value='';hasMore.value=false;loaded.value=false;rawOffset.value=0;verified.value=null;verificationError.value=''}
 const unsubscribe=subscribeSession(()=>{session.value=getBearer();clear()});
 if(getCurrentScope())onScopeDispose(()=>{alive=false;unsubscribe();clear()});
 function visible(order:MyOrder,requestedRole:'buyer'|'carrier'):boolean{
  if(String(order.my_role||'')!==requestedRole)return false;
  if(kind==='open')return true; // 本人订单投影; do not expose all users' orders as a purported public pool.
  return ['purchased','shipped'].includes(String(order.status||'').toLowerCase());
 }
 async function load(reset=true){
  if(!alive)return;if(!canRead.value){clear();error.value=t('account.signinRequired');loaded.value=true;return}
  if(busy.value&&!reset)return;
  const request=++generation,token=getBearer(),requestedRole=role.value,offset=reset?0:rawOffset.value;busy.value=true;error.value='';if(reset){rows.value=[];hasMore.value=false;rawOffset.value=0}
  const current=()=>alive&&request===generation&&token===getBearer()&&requestedRole===role.value;
  try{
   const batch=await api.account.orders(requestedRole,30,offset);
   if(!current())return;if(!Array.isArray(batch))throw new Error(t('migration.invalidResponse'));
   const existing=new Set(rows.value.map(x=>String(x.id)));
   rows.value=[...rows.value,...batch.filter(x=>x&&x.id!=null&&visible(x,requestedRole)&&!existing.has(String(x.id)))];
   rawOffset.value=offset+batch.length;hasMore.value=batch.length===30;loaded.value=true;
  }catch(e){if(current()){error.value=e instanceof Error?e.message:t('common.apiLoadError');if(reset){rows.value=[];hasMore.value=false}loaded.value=true}}
  finally{if(current())busy.value=false}
 }
 function switchRole(next:'buyer'|'carrier'){if(!alive||!['buyer','carrier'].includes(next)||role.value===next)return;role.value=next;generation++;rows.value=[];rawOffset.value=0;hasMore.value=false;loaded.value=false;void load(true)}
 async function loadVerification(){
  if(!alive||kind!=='open')return;const request=++verificationGeneration,token=getBearer();verified.value=null;verificationError.value='';if(!token)return;
  const current=()=>alive&&request===verificationGeneration&&token===getBearer();
  try{const profile=await api.account.profile();if(current())verified.value=profile.carrier_verify_status==='approved'}catch{if(current())verificationError.value=t('account.backendUnavailable')}
 }
 function detail(order:MyOrder){if(!alive||!canRead.value||!rows.value.some(row=>String(row.id)===String(order.id)))return;uni.navigateTo({url:'/pages/order-detail/index?id='+encodeURIComponent(String(order.id))})}
 onShow(()=>{rawOffset.value=0;void load(true);void loadVerification()});
 return proxyRefs({prefs,role,rows,busy,error,hasMore,loaded,canRead,verified,verificationError,load,switchRole,detail,mobilePreferenceStyle,formatAmount,formatDate,formatBusinessStatus});
}
