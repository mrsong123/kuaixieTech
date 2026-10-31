import {ref,computed,onMounted} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,type MyOrder,type MyDemand,type MyTrip,type MyLedger,type MyNotification} from '@kuaixie/sdk';
import {t,formatDate,formatAmount,formatBusinessStatus} from '@kuaixie/i18n';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
export type AccountFeedKind='orders'|'demands'|'trips'|'ledger'|'notifications';
export type AccountFeedItem=MyOrder|MyDemand|MyTrip|MyLedger|MyNotification;
export function useAccountFeeds(kind:AccountFeedKind){
 const prefs=preferenceRef;
 const items=ref<AccountFeedItem[]>([]);
 const loading=ref(false), error=ref(''),more=ref(false),role=ref<'all'|'buyer'|'carrier'>('all');
 const canRead=computed(()=>Boolean(getBearer()));
 const titleKey={orders:'account.myOrders',demands:'account.myDemands',trips:'account.myTrips',ledger:'account.ledger',notifications:'account.notifications'}[kind];
 const roleOptions:['all','buyer','carrier']=['all','buyer','carrier'];
 const roleKeys:Record<'all'|'buyer'|'carrier',string>={all:'account.myRoleAll',buyer:'account.myRoleBuyer',carrier:'account.myRoleCarrier'};
 function roleLabel(role:'all'|'buyer'|'carrier'){return roleKeys[role]}
 let requestId=0;
 async function fetchPage(reset=true){
   if(loading.value)return;
   if(!getBearer()){items.value=[];error.value='account.signinRequired';return}
   const request=++requestId;
   loading.value=true;error.value='';
   try{
    const offset=reset?0:items.value.length;
    const data=await (kind==='orders'?api.account.orders(role.value,20,offset):kind==='demands'?api.account.demands(20,offset):kind==='trips'?api.account.trips(20,offset):kind==='ledger'?api.account.ledger(20,offset):api.account.notifications(20,offset));
    if(request!==requestId)return;
    items.value=reset?data:[...items.value,...data];more.value=data.length===20;
   }catch(e){if(request===requestId){error.value='account.backendUnavailable';if(reset)items.value=[]}}
   finally{if(request===requestId)loading.value=false}
 }
 function chooseRole(next:'all'|'buyer'|'carrier'){if(role.value===next)return;role.value=next;void fetchPage()}
 function select(item:AccountFeedItem){
   if(kind==='orders')uni.navigateTo({url:'/pages/order-detail/index?id='+encodeURIComponent(String(item.id))});
 }
 async function read(item:AccountFeedItem){if(kind!=='notifications')return;
   try{await api.account.markNotificationRead(String(item.id));await fetchPage()}
   catch{uni.showToast({title:t('account.backendUnavailable'),icon:'none'})}
 }
 function headline(item:AccountFeedItem){if(kind==='orders')return String((item as MyOrder).item_name||'');if(kind==='demands')return String((item as MyDemand).title||'');if(kind==='trips')return String((item as MyTrip).destination||'');if(kind==='ledger')return String((item as MyLedger).kind||'');return String((item as MyNotification).title||'')}
 function state(item:AccountFeedItem){const code=(item as MyOrder).status||(item as MyTrip).verify_status||(item as MyNotification).kind||'';return formatBusinessStatus(String(code))}
 function summary(item:AccountFeedItem){if(kind==='orders')return formatAmount((item as MyOrder).goods_amount,'CNY');if(kind==='demands')return formatAmount((item as MyDemand).expect_price,'CNY');if(kind==='ledger')return formatAmount((item as MyLedger).amount,'CNY');if(kind==='trips')return String((item as MyTrip).depart_date||t('common.notProvided'));return (item as MyNotification).body||''}
 function when(item:AccountFeedItem){return formatDate(item.created_at)}
 onMounted(()=>{void fetchPage()});
 onShow(()=>{if(items.value.length>0)void fetchPage()});
 return {prefs,items,loading,error,more,role,roleOptions,roleLabel,canRead,titleKey,fetchPage,chooseRole,select,read,headline,state,summary,when,mobilePreferenceStyle,t};
}
