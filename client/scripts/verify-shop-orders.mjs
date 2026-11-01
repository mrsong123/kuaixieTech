import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-shop-orders-'));
let assertions=0;function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
const flush=async()=>{await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0))};
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.uiErrorMessage=e=>e.message;`);
 let code=ts.transpileModule(fs.readFileSync(path.join(root,'packages/ui/src/useShopOrders.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 const file=path.join(tmp,'orders.cjs');fs.writeFileSync(file,code);const {useShopOrders,shopStockStateKey}=require(file),{createApiClient,ApiError}=require(path.join(tmp,'index.js'));
 function row(id){return {id,order_no:id,item_name:'Product',status:'requesting',product_id:'product',quantity:1,goods_amount:'100.01',service_fee:'5.00',currency:'CNY',stock_state:'held',created_at:'2026-11-01T12:00:00Z'}}
 const page=Array.from({length:20},(_,i)=>row('order-'+i));
 {
  const calls=[];const api=createApiClient(async r=>{calls.push(r);return r.url.endsWith('offset=0')?page:[row('order-20')]},{getToken:()=> 'owner-unit'});
  await mounted(()=>useShopOrders(api),async p=>{await flush();check(p.rows.value.length===20&&p.more.value,'Owner attribution reads a bounded first page');await p.load(true);check(calls[1].url==='/me/shop/orders?limit=20&offset=20'&&p.rows.value.length===21&&!p.more.value,'Owner pagination uses backend offsets and stops on a short page');check(calls.every(r=>r.method==='GET'&&r.headers.Authorization==='Bearer owner-unit'),'Order attribution never performs writes and retains authenticated access');});
 }
 {
  const calls=[];const selected=vue.ref('');const api=createApiClient(async r=>{calls.push(r);return [row('admin-order')]},{getToken:()=> 'admin-unit'});
  await mounted(()=>useShopOrders(api,true,()=>selected.value),async p=>{await flush();check(!calls.length&&!p.rows.value.length,'Admin attribution waits for an explicit selected shop');selected.value='shop/id';await flush();check(calls[0].url==='/portal/admin/shops/shop%2Fid/orders?limit=20&offset=0','Admin attribution uses the protected and encoded selected-shop route');});
 }
 {
  const selected=vue.ref('first');let resolveFirst;const api=createApiClient(async r=>r.url.includes('/first/')?new Promise(resolve=>{resolveFirst=resolve}):[row('second-order')]);
  await mounted(()=>useShopOrders(api,true,()=>selected.value),async p=>{await vue.nextTick();selected.value='second';await flush();resolveFirst([row('first-private-order')]);await flush();check(p.rows.value.length===1&&p.rows.value[0].id==='second-order'&&!p.busy.value,'A delayed old-shop response cannot overwrite the new shop or leave loading stuck');});
 }
 {
  const selected=vue.ref('first');let resolveFirst;const api=createApiClient(async()=>new Promise(resolve=>{resolveFirst=resolve}));
  await mounted(()=>useShopOrders(api,true,()=>selected.value),async p=>{await vue.nextTick();selected.value='';resolveFirst([row('first-private-order')]);await flush();check(!p.rows.value.length&&!p.more.value&&!p.busy.value,'Clearing the admin shop selection drops its pending response and rows');});
 }
 {
  const calls=[];let fail=true;const api=createApiClient(async r=>{calls.push(r);if(r.url.endsWith('offset=0'))return page;if(fail)throw new ApiError('API_NETWORK_ERROR',0);return [row('order-20')]});
  await mounted(()=>useShopOrders(api),async p=>{await flush();await p.load(true);check(p.rows.value.length===20&&p.more.value&&!!p.error.value,'A failed next page preserves the already loaded attribution records');fail=false;await p.load(true);check(calls[1].url===calls[2].url&&p.rows.value.length===21,'Retrying a failed page does not skip an offset');});
 }
 check(shopStockStateKey('held')==='shopOrders.held'&&shopStockStateKey('consumed')==='shopOrders.consumed'&&shopStockStateKey('released')==='shopOrders.released'&&shopStockStateKey('invented')==='case.statusUnknown','Inventory states are displayed explicitly without inventing restocking or settlement');
 console.log(`SHOP ORDER ATTRIBUTION MODEL CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
