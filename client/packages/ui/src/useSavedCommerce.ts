import {getCurrentScope,onScopeDispose,ref,watch} from 'vue';
import {getBearer,subscribeSession,type ApiClient,type AddressInput,type SavedAddress,type SavedCartItem,type SavedFavorite} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';
export type SavedKind='addresses'|'cart'|'favorites';
/** Private collection views never adopt another account without reconstruction. */
function privateSession(clear:()=>void,error:{value:string}){
 const initial=getBearer();let invalid=false,alive=true;
 const isCurrent=()=>alive&&!invalid&&getBearer()===initial;
 function requireCurrent(){if(isCurrent())return true;if(alive){invalid=true;clear();error.value=t('case.sessionChanged')}return false}
 const unsubscribe=subscribeSession(()=>{if(getBearer()!==initial){invalid=true;clear();if(alive)error.value=t('case.sessionChanged')}});
 if(getCurrentScope())onScopeDispose(()=>{alive=false;invalid=true;unsubscribe();clear()});
 return {isCurrent,requireCurrent};
}
export function useSavedCommerce(api:ApiClient,kind:SavedKind,confirm:()=>Promise<boolean>){
 const addresses=ref<SavedAddress[]>([]),cart=ref<SavedCartItem[]>([]),favorites=ref<SavedFavorite[]>([]),busy=ref(false),error=ref(''),more=ref(false),editing=ref('');
 const empty=():AddressInput=>({contactName:'',contactPhone:'',province:'',city:'',district:'',detail:'',isDefault:false});
 const draft=ref(empty());let offset=0,readGeneration=0,mutationGeneration=0;
 function blankDraft(){draft.value=empty();editing.value=''}
 function clear(){readGeneration++;mutationGeneration++;addresses.value=[];cart.value=[];favorites.value=[];busy.value=false;error.value='';more.value=false;offset=0;blankDraft()}
 const session=privateSession(clear,error);
 async function load(append=false){
  if(!session.requireCurrent()||busy.value)return;const request=++readGeneration,next=append?offset+20:0;busy.value=true;error.value='';
  try{
   const rows=kind==='addresses'?await api.saved.addresses(20,next):kind==='cart'?await api.saved.cart(20,next):await api.saved.favorites(20,next);
   if(!session.requireCurrent()||request!==readGeneration)return;
   if(kind==='addresses')addresses.value=append?[...addresses.value,...rows as SavedAddress[]]:rows as SavedAddress[];
   else if(kind==='cart')cart.value=append?[...cart.value,...rows as SavedCartItem[]]:rows as SavedCartItem[];
   else favorites.value=append?[...favorites.value,...rows as SavedFavorite[]]:rows as SavedFavorite[];
   more.value=rows.length===20;offset=next;
  }catch(e){if(session.requireCurrent()&&request===readGeneration)error.value=uiErrorMessage(e)}
  finally{if(session.isCurrent()&&request===readGeneration)busy.value=false}
 }
 function reset(){if(session.requireCurrent())blankDraft()}
 function edit(row:SavedAddress){if(!session.requireCurrent()||busy.value)return;editing.value=row.id;draft.value={contactName:row.contact_name,contactPhone:row.contact_phone,province:row.province||'',city:row.city||'',district:row.district||'',detail:row.detail,isDefault:Boolean(row.is_default)}}
 async function perform(task:()=>Promise<unknown>,after?:()=>void){
  const request=++mutationGeneration;
  try{if(!session.requireCurrent())return;await task();if(!session.requireCurrent()||request!==mutationGeneration)return;after?.();busy.value=false;await load()}
  catch(e){if(session.requireCurrent()&&request===mutationGeneration)error.value=uiErrorMessage(e)}
  finally{if(session.isCurrent()&&request===mutationGeneration)busy.value=false}
 }
 async function save(){
  if(!session.requireCurrent()||busy.value)return;const id=editing.value,input={...draft.value},snapshot=JSON.stringify(input);busy.value=true;error.value='';
  await perform(()=>id?api.saved.updateAddress(id,input):api.saved.createAddress(input),()=>{if(editing.value===id&&JSON.stringify(draft.value)===snapshot)blankDraft()});
 }
 async function remove(id:string,type?:'product'|'demand'){
  if(!session.requireCurrent()||busy.value)return;busy.value=true;error.value='';
  try{if(!await confirm()||!session.requireCurrent())return;await perform(()=>kind==='addresses'?api.saved.deleteAddress(id):kind==='cart'?api.saved.deleteCart(id):api.saved.deleteFavorite(type!,id))}
  catch(e){if(session.requireCurrent())error.value=uiErrorMessage(e)}finally{if(session.isCurrent())busy.value=false}
 }
 async function quantity(row:SavedCartItem,value:string){
  if(!session.requireCurrent()||busy.value)return;const count=Number(value),id=row.product_id;
  if(!Number.isInteger(count)||count<1||count>10000)return;busy.value=true;error.value='';await perform(()=>api.saved.setCart(id,count));
 }
 return {addresses,cart,favorites,busy,error,more,draft,editing,load,save,reset,edit,remove,quantity,isCurrentSession:session.isCurrent};
}
/** Public product views may survive login. Each action still belongs to its starting account and product. */
export function useProductActions(api:ApiClient,id:()=>string){
 const busy=ref(false),error=ref(''),done=ref('');let alive=true,generation=0;
 function clear(){generation++;busy.value=false;error.value='';done.value=''}
 const unsubscribe=subscribeSession(clear);watch(id,clear,{flush:'sync'});
 if(getCurrentScope())onScopeDispose(()=>{alive=false;unsubscribe();clear()});
 async function perform(action:'cart'|'favorite'){
  if(!alive||busy.value)return;const request=++generation,token=getBearer(),product=id();busy.value=true;error.value='';done.value='';
  const current=()=>alive&&request===generation&&token===getBearer()&&product===id();
  try{if(action==='cart')await api.saved.setCart(product,1);else await api.saved.favorite('product',product);if(current())done.value='saved.added'}catch(e){if(current())error.value=uiErrorMessage(e)}finally{if(current())busy.value=false}
 }
 return {busy,error,done,perform};
}
export function useAddressSelection(api:ApiClient,selected:(value:{contactName:string;contactPhone:string;address:string})=>void){
 const rows=ref<SavedAddress[]>([]),busy=ref(false),error=ref('');let generation=0;
 function clear(){generation++;rows.value=[];busy.value=false;error.value=''}
 const session=privateSession(clear,error);
 async function load(){if(!session.requireCurrent()||busy.value)return;const request=++generation;busy.value=true;error.value='';try{const result=await api.saved.addresses(100,0);if(session.requireCurrent()&&request===generation)rows.value=result}catch(e){if(session.requireCurrent()&&request===generation)error.value=uiErrorMessage(e)}finally{if(session.isCurrent()&&request===generation)busy.value=false}}
 function choose(id:string){if(!session.requireCurrent())return;const row=rows.value.find(row=>row.id===id);if(!row)return;selected({contactName:row.contact_name,contactPhone:row.contact_phone,address:[row.province,row.city,row.district,row.detail].filter(Boolean).join(' ')})}
 return {rows,busy,error,load,choose,isCurrentSession:session.isCurrent};
}
