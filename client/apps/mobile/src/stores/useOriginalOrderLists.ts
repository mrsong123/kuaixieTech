import {computed,ref,proxyRefs} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,type MyOrder} from '@kuaixie/sdk';
import {t,formatAmount,formatDate,formatBusinessStatus} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
/** Keeps original order pages scoped to the authenticated user's account.
 * This is a client projection, NOT a replacement for a dedicated shipment/open-intent API. */
export function useOriginalOrderList(kind:'shipment'|'open'){
 const prefs=preferenceRef;
 const role=ref<'buyer'|'carrier'>(kind==='open'?'carrier':'buyer');
 const rows=ref<MyOrder[]>([]),busy=ref(false),error=ref(''),hasMore=ref(false),loaded=ref(false);
 const canRead=computed(()=>Boolean(getBearer()));
 const verified=ref<boolean|null>(null),verificationError=ref('');
 let generation=0;
 function visible(order:MyOrder):boolean{
   if(String(order.my_role||'')!==role.value)return false;
   if(kind==='open')return true; // open bids are separate; do not misclassify committed orders.
   return ['purchased','shipped'].includes(String(order.status||'').toLowerCase());
 }
 async function load(reset=true){
   if(!canRead.value){generation++;rows.value=[];hasMore.value=false;error.value=t('account.signinRequired');loaded.value=true;return}
   if(busy.value&&!reset)return;
   const current=++generation;busy.value=true;error.value='';
   try{
     const offset=reset?0:rawOffset.value;
     const batch=await api.account.orders(role.value,30,offset);
     if(current!==generation)return;
     if(!Array.isArray(batch))throw new Error(t('migration.invalidResponse'));
     if(reset)rows.value=[];
     const existing=new Set(rows.value.map(x=>String(x.id)));
     rows.value=[...rows.value,...batch.filter(x=>x && x.id!=null && visible(x) && !existing.has(String(x.id)))];
     rawOffset.value=offset+batch.length;
     hasMore.value=batch.length===30;
     loaded.value=true;
   }catch(e){if(current===generation){error.value=e instanceof Error?e.message:t('common.apiLoadError');if(reset){rows.value=[];hasMore.value=false}loaded.value=true}}
   finally{if(current===generation)busy.value=false}
 }
 const rawOffset=ref(0);
 function switchRole(next:'buyer'|'carrier'){if(role.value===next)return;role.value=next;rawOffset.value=0;void load(true)}
 async function loadVerification(){
   if(kind!=='open'||!getBearer()){verified.value=null;return}
   try{const profile=await api.account.profile();verified.value=profile.carrier_verify_status==='approved';verificationError.value=''}
   catch{verified.value=null;verificationError.value=t('account.backendUnavailable')}
 }
 function detail(o:MyOrder){uni.navigateTo({url:'/pages/order-detail/index?id='+encodeURIComponent(String(o.id))})}
 onShow(()=>{rawOffset.value=0;void load(true);void loadVerification()});
 return proxyRefs({prefs,role,rows,busy,error,hasMore,loaded,canRead,verified,verificationError,load,switchRole,detail,mobilePreferenceStyle,formatAmount,formatDate,formatBusinessStatus});
}
