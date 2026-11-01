import {ref} from 'vue';
import type {ApiClient,AddressInput,SavedAddress,SavedCartItem,SavedFavorite} from '@kuaixie/sdk';
import {uiErrorMessage} from '@kuaixie/i18n';
export type SavedKind='addresses'|'cart'|'favorites';
export function useSavedCommerce(api:ApiClient,kind:SavedKind,confirm:()=>Promise<boolean>){
 const addresses=ref<SavedAddress[]>([]),cart=ref<SavedCartItem[]>([]),favorites=ref<SavedFavorite[]>([]),busy=ref(false),error=ref(''),more=ref(false),editing=ref('');
 const empty=():AddressInput=>({contactName:'',contactPhone:'',province:'',city:'',district:'',detail:'',isDefault:false});
 const draft=ref(empty());let offset=0;
 async function load(append=false){if(busy.value)return;busy.value=true;error.value='';try{const next=append?offset+20:0;if(kind==='addresses'){const rows=await api.saved.addresses(20,next);addresses.value=append?[...addresses.value,...rows]:rows;more.value=rows.length===20}else if(kind==='cart'){const rows=await api.saved.cart(20,next);cart.value=append?[...cart.value,...rows]:rows;more.value=rows.length===20}else{const rows=await api.saved.favorites(20,next);favorites.value=append?[...favorites.value,...rows]:rows;more.value=rows.length===20}offset=next}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 function reset(){draft.value=empty();editing.value=''}
 function edit(row:SavedAddress){editing.value=row.id;draft.value={contactName:row.contact_name,contactPhone:row.contact_phone,province:row.province||'',city:row.city||'',district:row.district||'',detail:row.detail,isDefault:Boolean(row.is_default)}}
 async function mutate(task:()=>Promise<unknown>){if(busy.value)return;busy.value=true;error.value='';try{await task();busy.value=false;await load()}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 async function save(){await mutate(async()=>{if(editing.value)await api.saved.updateAddress(editing.value,draft.value);else await api.saved.createAddress(draft.value);reset()})}
 async function remove(id:string,type?:'product'|'demand'){if(busy.value||!await confirm())return;await mutate(()=>kind==='addresses'?api.saved.deleteAddress(id):kind==='cart'?api.saved.deleteCart(id):api.saved.deleteFavorite(type!,id))}
 async function quantity(row:SavedCartItem,value:string){const count=Number(value);if(!Number.isInteger(count)||count<1||count>10000)return;await mutate(()=>api.saved.setCart(row.product_id,count))}
 return {addresses,cart,favorites,busy,error,more,draft,editing,load,save,reset,edit,remove,quantity};
}
export function useProductActions(api:ApiClient,id:()=>string){
 const busy=ref(false),error=ref(''),done=ref('');
 async function perform(action:'cart'|'favorite'){if(busy.value)return;busy.value=true;error.value='';done.value='';try{if(action==='cart')await api.saved.setCart(id(),1);else await api.saved.favorite('product',id());done.value='saved.added'}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 return {busy,error,done,perform};
}
export function useAddressSelection(api:ApiClient,selected:(value:{contactName:string;contactPhone:string;address:string})=>void){
 const rows=ref<SavedAddress[]>([]),busy=ref(false),error=ref('');
 async function load(){if(busy.value)return;busy.value=true;error.value='';try{rows.value=await api.saved.addresses(100,0)}catch(e){error.value=uiErrorMessage(e)}finally{busy.value=false}}
 function choose(id:string){const row=rows.value.find(row=>row.id===id);if(!row)return;selected({contactName:row.contact_name,contactPhone:row.contact_phone,address:[row.province,row.city,row.district,row.detail].filter(Boolean).join(' ')});}
 return {rows,busy,error,load,choose};
}
