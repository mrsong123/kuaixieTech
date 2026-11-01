import {getCurrentScope,onScopeDispose,ref} from 'vue';
import {getBearer,subscribeSession} from '@kuaixie/sdk';

/** A read response belongs to the account and page lifetime which started it. */
export function usePrivatePageSession(clear:()=>void){
 const revision=ref(0);let alive=true;
 const unsubscribe=subscribeSession(()=>{revision.value++;clear()});
 if(getCurrentScope())onScopeDispose(()=>{alive=false;revision.value++;unsubscribe();clear()});
 function capture(){return {revision:revision.value,token:getBearer()}}
 function current(value:ReturnType<typeof capture>){return alive&&value.revision===revision.value&&value.token===getBearer()}
 return {revision,capture,current};
}

/** Native runtimes need not provide the browser URL constructor. */
export function privateWebTarget(value:unknown){return typeof value==='string'&&/^https:\/\/[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?(?::443)?(?:[/?#]|$)/i.test(value)&&!/[\\\u0000-\u0020]/.test(value)?value:''}
