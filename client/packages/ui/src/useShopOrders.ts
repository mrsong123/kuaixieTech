import {ref,watch} from 'vue';
import type {ApiClient,ShopAttributedOrder} from '@kuaixie/sdk';
import {uiErrorMessage} from '@kuaixie/i18n';
export function shopStockStateKey(state:string){return ({held:'shopOrders.held',consumed:'shopOrders.consumed',released:'shopOrders.released'} as Record<string,string>)[state]||'case.statusUnknown'}
/** Read recorded attribution; never derive merchant earnings or perform financial operations here. */
export function useShopOrders(api:ApiClient,admin=false,storeId:()=>string=()=> ''){
 const rows=ref<ShopAttributedOrder[]>([]),busy=ref(false),more=ref(false),error=ref('');let offset=0,generation=0;
 async function load(append=false){if(append&&busy.value)return;const id=storeId(),request=++generation;if(admin&&!id){rows.value=[];more.value=false;busy.value=false;error.value='';offset=0;return}busy.value=true;error.value='';try{const next=append?offset+20:0;const result=await (admin?api.shops.adminOrders(id,20,next):api.shops.orders(20,next));if(request!==generation||admin&&storeId()!==id)return;rows.value=append?[...new Map([...rows.value,...result].map(row=>[row.id,row])).values()]:result;offset=next;more.value=result.length===20;}catch(e){if(request===generation){error.value=uiErrorMessage(e);if(!append){rows.value=[];more.value=false}}}finally{if(request===generation)busy.value=false}}
 watch(storeId,()=>{generation++;rows.value=[];more.value=false;offset=0;void load()},{immediate:true,flush:'sync'});
 return {rows,busy,more,error,load};
}
