/** Intentionally memory-only; neither credentials nor session notifications are persisted. */
let bearer='';
const listeners=new Set<()=>void>();
export function subscribeSession(listener:()=>void){listeners.add(listener);return()=>{listeners.delete(listener)}}
export function setBearer(token:string){const next=token.trim().replace(/^Bearer\s+/i,'');if(next===bearer)return;bearer=next;for(const listener of [...listeners]){try{listener()}catch{/* Observers cannot prevent an authentication transition. */}}}
export function getBearer(){return bearer}
export function clearBearer(){setBearer('')}
