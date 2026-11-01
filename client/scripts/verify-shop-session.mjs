import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-shop-session-'));
let assertions=0;function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
const deferred=()=>{let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {promise,resolve,reject}};
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.t=key=>key;exports.uiErrorMessage=e=>e.message;`);
 let code=ts.transpileModule(fs.readFileSync(path.join(root,'packages/ui/src/useShop.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 const file=path.join(tmp,'shop.cjs');fs.writeFileSync(file,code);const {useShop}=require(file),{createApiClient,ApiError,setBearer,getBearer,clearBearer}=require(path.join(tmp,'index.js'));
 const state={store:null,applications:[],products:[]};
 const store=id=>({id,name:'Private shop',description:'Private details',status:'active',matching_fee_rate:'5.00',application_id:'application',created_at:'2026-11-01'});
 const row={id:'application',business_name:'Private business',unified_social_credit_code:'91350211M000100Y46',legal_representative:'Private name',license_upload_id:'license',application_channel:'app',status:'pending',created_at:'2026-11-01'};
 function fixture(handler){const calls=[];const api=createApiClient(async request=>{const r={...request,data:request.data===undefined?undefined:JSON.parse(JSON.stringify(request.data))};calls.push(r);const value=handler?.(r);if(value!==undefined)return value;if(r.url==='/me/shop')return state;if(r.url.startsWith('/me/uploads')||r.url.startsWith('/me/products')||r.url.startsWith('/portal/admin/shops/applications'))return [];return {id:'application',status:'pending'}},{getToken:getBearer});return {calls,api};}
 const mutations=f=>f.calls.filter(r=>['POST','PUT','DELETE'].includes(r.method));
 function form(p){Object.assign(p.application,{businessName:'Private business',creditCode:'91350211M000100Y46',representative:'Private name',licenseUploadId:'license-A',invitationCode:'invite-A'})}
 setBearer('shop-A');
 {
  const reply=deferred(),f=fixture(r=>r.url==='/me/shop'?reply.promise:undefined);
  await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{form(p);p.state.value={store:store('shop-A'),applications:[row],products:[]};p.note.value='Private note';const request=p.load();setBearer('shop-B');check(!p.state.value&&!p.uploads.value.length&&!p.application.licenseUploadId&&!p.application.representative&&!p.note.value,'Changing accounts immediately clears shop details, license references, drafts and notes');reply.resolve({store:store('shop-A'),applications:[row],products:[]});await request;check(!p.state.value&&!p.uploads.value.length&&!p.notice.value,'Old owner reads cannot refill the new session or display success');});
 }
 setBearer('shop-A');
 {
  const reply=deferred(),f=fixture(r=>r.url.startsWith('/portal/admin/shops/applications')?reply.promise:undefined);
  await mounted(()=>useShop(f.api,async()=>true,'web',true),async p=>{p.queue.value=[row];const request=p.load();setBearer('shop-B');reply.resolve([row]);await request;check(!p.queue.value.length,'An old administrator queue cannot disclose shop applicants after a session change');});
 }
 for(const operation of ['apply','review','status','link']){
  setBearer('shop-A');const confirm=deferred(),f=fixture();
  await mounted(()=>useShop(f.api,async()=>confirm.promise,'app',operation==='review'),async p=>{form(p);p.note.value='Checked owner evidence';const request=operation==='apply'?p.apply():operation==='review'?p.review({...row},true):operation==='status'?p.status('shop-A','active','paused'):p.link('product-A');setBearer('shop-B');confirm.resolve(true);await request;check(mutations(f).length===0,'An old-account '+operation+' confirmation cannot send a new-account mutation');});
 }
 setBearer('shop-A');
 {
  const confirm=deferred(),f=fixture();await mounted(()=>useShop(f.api,async()=>confirm.promise,'app'),async p=>{form(p);const action=p.apply();p.application.licenseUploadId='different-license';confirm.resolve(true);await action;check(!mutations(f).length&&p.error.value==='shop.changed','A changed application or attachment requires fresh confirmation');});
 }
 setBearer('shop-A');
 {
  const confirm=deferred(),f=fixture();await mounted(()=>useShop(f.api,async()=>confirm.promise,'web',true),async p=>{p.note.value='First note';const action=p.review({...row},true);p.note.value='Second note';confirm.resolve(true);await action;check(!mutations(f).length&&p.error.value==='shop.changed','An admin cannot confirm one review note and silently submit another');});
 }
 setBearer('shop-A');
 {
  const confirm=deferred(),f=fixture();let confirmations=0;await mounted(()=>useShop(f.api,async()=>{confirmations++;return confirm.promise},'app'),async p=>{form(p);const first=p.apply();const duplicate=p.apply();confirm.resolve(true);await Promise.all([first,duplicate]);check(confirmations===1&&mutations(f).length===1,'Concurrent clicks keep one application confirmation and one request');});
 }
 setBearer('shop-A');
 {
  let first=true;const f=fixture(r=>{if(r.url==='/me/shop/applications'){if(first){first=false;throw new ApiError('API_NETWORK_ERROR',0)}return {id:'application',status:'pending'}}});await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{form(p);await p.apply();check(!!p.pending.value,'An unknown application result retains its original request identity');p.application.businessName='Edited';p.application.licenseUploadId='Edited';await p.apply();const requests=mutations(f);check(requests.length===2&&JSON.stringify(requests[0].data)===JSON.stringify(requests[1].data)&&requests[0].data.channel==='app','A retry preserves the initial request, license, platform and idempotency key');});
 }
 setBearer('shop-A');
 {
  const reply=deferred(),started=deferred(),f=fixture(r=>{if(r.method==='POST'){started.resolve();return reply.promise}});await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{form(p);const request=p.apply();await started.promise;setBearer('shop-B');reply.resolve({id:'application',status:'pending'});await request;check(!p.pending.value&&!p.notice.value&&!p.state.value&&f.calls.length===1,'A late mutation receipt cannot show success or trigger reads with the replacement account');});
 }
 setBearer('shop-A');
 {
  const reply=deferred(),f=fixture();await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{const action=p.run(async()=>{await reply.promise;p.application.licenseUploadId='late-private-license'});setBearer('shop-B');reply.resolve();await action;check(!p.application.licenseUploadId&&!p.notice.value&&f.calls.length===0,'An external upload adapter cannot retain a late license reference after session expiry');});
 }
 setBearer('shop-A');
 {
  const first=deferred(),second=deferred();let count=0;const f=fixture(r=>r.url.startsWith('/me/products')?(++count===1?first.promise:second.promise):undefined);await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{const old=p.loadProducts(),fresh=p.loadProducts();second.resolve([{id:'current-product'}]);await fresh;first.resolve([{id:'old-product'}]);await old;check(p.products.value.length===1&&p.products.value[0].id==='current-product','An older product selector response cannot overwrite the latest result');});
 }
 setBearer('shop-A');
 {
  const reply=deferred(),f=fixture(r=>r.url.includes('/products')?reply.promise:undefined);await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{const request=p.loadProducts();setBearer('shop-B');reply.resolve([{id:'old-product'}]);await request;check(!p.products.value.length,'An old owner product selector stays empty after an account switch');});
 }
 setBearer('shop-A');
 {
  const reply=deferred(),f=fixture(r=>r.url.startsWith('/public/shops/')?reply.promise:undefined);await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{const request=p.preview('shop-A');setBearer('shop-B');reply.resolve({...store('shop-A'),products:[]});await request;check(!p.publicShop.value,'A preview response does not revive a disposed account-bound shop model');});
 }
 setBearer('shop-A');
 {
  const f=fixture();await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{setBearer('shop-B');setBearer('shop-A');await p.load();check(!f.calls.length&&!p.isCurrent()&&!p.isCurrentSession(),'Returning to the same token cannot reactivate an expired model');});
 }
 setBearer('shop-A');
 {
  const f=fixture();let model;await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{model=p;form(p);p.state.value={store:store('shop-A'),applications:[row],products:[]}});await model.load();check(!model.isCurrent()&&!model.state.value&&!model.application.businessName&&!f.calls.length,'Disposing a shop screen clears its data and prevents new reads');
 }
 setBearer('shop-A');
 {
  const reply=deferred(),f=fixture(r=>r.url.startsWith('/me/shop/products?')?reply.promise:undefined);await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{p.state.value={store:store('first-shop'),applications:[],products:[]};const request=p.linked();p.state.value={store:store('replacement-shop'),applications:[],products:[]};reply.resolve([{id:'old-linked-product'}]);await request;check(p.state.value.store.id==='replacement-shop'&&!p.state.value.products.length,'Linked-product pagination cannot append to a different shop context');});
 }
 setBearer('shop-A');
 {
  const old=deferred(),f=fixture(r=>r.url.startsWith('/public/shops/old')?old.promise:r.url.startsWith('/public/shops/current')?{...store('current'),products:[]}:undefined);await mounted(()=>useShop(f.api,async()=>true,'app'),async p=>{const request=p.preview('old');await p.preview('current');old.reject(new Error('old preview failure'));await request;check(p.publicShop.value?.id==='current'&&!p.error.value,'A stale preview error cannot clear a newly opened shop');});
 }
 setBearer('shop-A');
 {
  const confirm=deferred(),f=fixture();await mounted(()=>useShop(f.api,async()=>confirm.promise,'app'),async p=>{p.state.value={store:store('shop-A'),applications:[],products:[]};const action=p.status('shop-A','active','paused');p.state.value.store.status='suspended';confirm.resolve(true);await action;check(!mutations(f).length&&p.error.value==='shop.changed','Changing shop state while confirmation is open requires rechecking the action');});
 }
 clearBearer();console.log(`SHOP SESSION MODEL CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
