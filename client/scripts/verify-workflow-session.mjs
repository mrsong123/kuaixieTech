import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath);
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-workflow-session-'));let assertions=0;
const check=(value,message)=>{assert.ok(value,message);assertions++;console.log('OK:',message)};
const deferred=()=>{let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {promise,resolve,reject}};
const flush=async()=>{await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0))};
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let p;const app=renderer.createApp({setup(){p=factory();return()=>null}});app.mount({});try{await test(p,()=>app.unmount())}finally{app.unmount()}}
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),"exports.t=key=>key;exports.uiErrorMessage=e=>e.message;exports.operationLabel=op=>op.name;exports.groupLabel=x=>x;exports.formatAmount=(v,c)=>String(v)+' '+c;");
 fs.writeFileSync(path.join(tmp,'native-api.cjs'),"exports.api={invoke:(...args)=>global.__workflowSessionApi.invoke(...args)};exports.showError=e=>global.__workflowSessionErrors.push(e);");
 fs.writeFileSync(path.join(tmp,'preferences.cjs'),`exports.preferenceRef=require(${JSON.stringify(vuePath)}).ref({});`);
 fs.writeFileSync(path.join(tmp,'prototype.cjs'),"exports.mobilePreferenceStyle=()=>'';");
 function load(source,name){let code=ts.transpileModule(fs.readFileSync(path.join(root,source),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')],['@kuaixie/preferences/vue',path.join(tmp,'preferences.cjs')],['../utils/prototype',path.join(tmp,'prototype.cjs')],['../api/client',path.join(tmp,'native-api.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);const file=path.join(tmp,name+'.cjs');fs.writeFileSync(file,code);return require(file)}
 const {createApiClient,setBearer,clearBearer,operations,ApiError}=require(path.join(tmp,'index.js'));
 const {useBusinessWorkflow}=load('packages/ui/src/useBusinessWorkflow.ts','workflow');
 const {useToolsPage}=load('apps/mobile/src/stores/useToolsPage.ts','tools');
 const quote={pricingVersion:'escrow_v2_external_insurance',currency:'CNY',goodsAmount:'100.00',serviceFee:'5.00',insurancePremium:'0.00',buyerEscrow:'105.00'};
 function client(handler){const calls=[];return {calls,api:createApiClient(async r=>{const copy={...r,data:r.data===undefined?undefined:JSON.parse(JSON.stringify(r.data))};calls.push(copy);return handler(copy)},{getToken:()=>require(path.join(tmp,'index.js')).getBearer()})}}
 function award(p){p.pick(p.flow.operations.findIndex(o=>o.path==='/rpc/award_pk'));p.update('demandId','demand-a');p.update('quoteId','quote-a');p.update('insuranceChoice','waived')}
 function cancel(p){p.update('orderId','order-a');p.update('reasonType','personal')}
 {
  setBearer('a');const f=client(async r=>r.url.startsWith('/me/orders/')?{id:'order-a',vendor_deposit:'20.01'}:'OK');
  await mounted(()=>useBusinessWorkflow(f.api,'order-actions',async()=>true),async p=>{p.update('orderId','order-a');await p.submit();const request=f.calls.find(r=>r.url==='/rpc/accept_order_atomic');check(request?.data.deposit==='20.01'&&request.headers.Authorization==='Bearer a','Normal carrier acceptance preserves the exact server deposit and initiating account')});
 }
 {
  setBearer('a');const wait=deferred();let confirmations=0;const f=client(()=>wait.promise);
  await mounted(()=>useBusinessWorkflow(f.api,'order-actions',async()=>{confirmations++;return true}),async p=>{p.update('orderId','private-order');const work=p.submit();setBearer('b');wait.resolve({vendor_deposit:'20.00'});await work;check(confirmations===0&&f.calls.length===1&&!p.completed&&Object.keys(p.form).length===0,'A late carrier deposit cannot show an old-account confirmation or submit with a new token')});
 }
 {
  setBearer('a');const modal=deferred();const f=client(async()=>({vendor_deposit:'20.00'}));
  await mounted(()=>useBusinessWorkflow(f.api,'order-actions',()=>modal.promise),async p=>{p.update('orderId','order-a');const work=p.submit();await flush();setBearer('b');modal.resolve(true);await work;check(f.calls.every(r=>r.method==='GET')&&!p.completed,'Accepting an old carrier modal after switching accounts cannot mutate an order')});
 }
 {
  setBearer('a');const wait=deferred();let confirmations=0;const f=client(()=>wait.promise);
  await mounted(()=>useBusinessWorkflow(f.api,'pk-quotes',async()=>{confirmations++;return true}),async p=>{award(p);const work=p.submit();setBearer('b');setBearer('a');wait.resolve(quote);await work;check(confirmations===0&&f.calls.length===1&&p.error==='fast.sessionChanged','A→B→A still invalidates an old award quote')});
 }
 {
  setBearer('a');const modal=deferred();const f=client(async()=>quote);
  await mounted(()=>useBusinessWorkflow(f.api,'pk-quotes',()=>modal.promise),async p=>{award(p);const work=p.submit();await flush();p.form.demandId='demand-b';modal.resolve(true);await work;check(!f.calls.some(r=>r.url==='/rpc/award_pk')&&p.error==='fast.contextChanged','Editing the target during an award confirmation cancels the stale financial operation')});
 }
 {
  setBearer('a');const modal=deferred();const f=client(async()=>({shipping_fee:'15.29'}));
  await mounted(()=>useBusinessWorkflow(f.api,'shipping-fee',()=>modal.promise),async p=>{p.pick(p.flow.operations.findIndex(o=>o.path==='/rpc/respond_shipping_fee'));p.update('orderId','order-a');p.update('accept',true);const work=p.submit();await flush();p.form.orderId='order-b';modal.resolve(true);await work;check(f.calls.length===1&&p.error==='fast.contextChanged','Shipping-fee confirmation is bound to the displayed order')});
 }
 {
  setBearer('a');const modal=deferred();let count=0;const f=client(async()=> 'OK');
  await mounted(()=>useBusinessWorkflow(f.api,'cancel-request',()=>{count++;return modal.promise}),async p=>{cancel(p);const first=p.submit();await p.submit();check(count===1&&p.busy,'A workflow keeps its lock while a generic dangerous confirmation is open');modal.resolve(true);await first;check(f.calls.length===1&&f.calls[0].data.orderId==='order-a','A duplicate confirmation cannot cause duplicate cancellation requests')});
 }
 {
  setBearer('a');const modal=deferred();const f=client(async()=> 'OK');
  await mounted(()=>useBusinessWorkflow(f.api,'cancel-request',()=>modal.promise),async p=>{cancel(p);const work=p.submit();setBearer('b');modal.resolve(true);await work;check(f.calls.length===0&&p.receipt.length===0,'Generic dangerous workflow confirmations reject replacement accounts')});
 }
 {
  setBearer('a');const response=deferred();const f=client(()=>response.promise);
  await mounted(()=>useBusinessWorkflow(f.api,'order-actions',async()=>true),async p=>{p.pick(1);p.update('orderId','private-order');const work=p.submit();setBearer('b');response.resolve({privateEvidence:'do not display'});await work;check(!p.completed&&p.receipt.length===0,'Old workflow receipts cannot populate another account')});
 }
 {
  setBearer('a');const response=deferred();const f=client(()=>response.promise);
  await mounted(()=>useBusinessWorkflow(f.api,'pk-quotes',async()=>true),async p=>{award(p);const work=p.loadInsuranceSubject();p.prefill({demandId:'demand-b',quoteId:'quote-b'});response.resolve({subjectId:'old-subject'});await work;check(p.insuranceSubject===null&&p.form.demandId==='demand-b','An insurance subject lookup cannot fill a changed workflow context')});
 }
 {
  setBearer('a');const modal=deferred();const f=client(async()=> 'OK');
  await mounted(()=>useBusinessWorkflow(f.api,'cancel-request',()=>modal.promise),async(p,unmount)=>{cancel(p);const work=p.submit();unmount();modal.resolve(true);await work;check(f.calls.length===0&&Object.keys(p.form).length===0,'Disposing a workflow invalidates its open confirmation and private form')});
 }
 const dangerous=operations.find(op=>op.path==='/rpc/request_cancel_v2');assert.ok(dangerous?.dangerous);
 function nativeFixture(handler=async()=>({result:'accepted'})){
  const f=client(handler),modals=[];global.__workflowSessionApi=f.api;global.__workflowSessionErrors=[];global.uni={showModal:options=>{modals.push(options)}};return {...f,modals};
 }
 function openNative(p){p.open(dangerous);p.payload.orderId='order-a';p.payload.reasonType='personal'}
 {
  setBearer('a');const f=nativeFixture();await mounted(useToolsPage,async p=>{openNative(p);const work=p.invoke();await p.invoke();check(f.modals.length===1&&p.working.value,'Native API tools lock across a pending confirmation');f.modals[0].success({confirm:true});await work;check(f.calls.length===1&&f.calls[0].data.orderId==='order-a'&&f.calls[0].headers.Authorization==='Bearer a','A confirmed native tool preserves its captured target and account')});
 }
 {
  setBearer('a');const f=nativeFixture();await mounted(useToolsPage,async p=>{openNative(p);const work=p.invoke();setBearer('b');f.modals[0].success({confirm:true});await work;check(f.calls.length===0&&!p.result.value&&Object.keys(p.payload).length===0,'A delayed native modal cannot issue requests for a replacement account')});
 }
 {
  setBearer('a');const f=nativeFixture();await mounted(useToolsPage,async p=>{openNative(p);const work=p.invoke();setBearer('b');setBearer('a');f.modals[0].success({confirm:true});await work;check(f.calls.length===0&&p.error.value==='fast.sessionChanged','Returning to the same token does not revive a native confirmation after an intervening account')});
 }
 {
  setBearer('a');const f=nativeFixture();await mounted(useToolsPage,async p=>{openNative(p);const work=p.invoke();p.payload.orderId='order-b';f.modals[0].success({confirm:true});await work;check(f.calls.length===0&&p.error.value==='fast.contextChanged','Native tool target edits invalidate the displayed confirmation')});
 }
 {
  setBearer('a');const f=nativeFixture();await mounted(useToolsPage,async p=>{openNative(p);const first=p.invoke();const other=operations.find(o=>o.role==='user'&&o.id!==dangerous.id);p.open(other);f.modals[0].success({confirm:true});await first;check(f.calls.length===0&&p.chosen.value===other.id,'Opening a different native operation does not reuse an old modal')});
 }
 {
  setBearer('a');const response=deferred(),f=nativeFixture(()=>response.promise);await mounted(useToolsPage,async p=>{openNative(p);const work=p.invoke();f.modals[0].success({confirm:true});await flush();setBearer('b');response.resolve({secret:'private response'});await work;check(!p.result.value&&global.__workflowSessionErrors.length===0,'A late native result cannot expose another account’s response or popup')});
 }
 {
  setBearer('a');const response=deferred(),f=nativeFixture(()=>response.promise);await mounted(useToolsPage,async p=>{openNative(p);const work=p.invoke();f.modals[0].success({confirm:true});await flush();setBearer('b');response.reject(new ApiError('private error',500));await work;check(global.__workflowSessionErrors.length===0&&p.error.value==='fast.sessionChanged','A late native error cannot display an old-account error modal')});
 }
 {
  setBearer('a');const f=nativeFixture();await mounted(useToolsPage,async(p,unmount)=>{openNative(p);const work=p.invoke();unmount();f.modals[0].success({confirm:true});await work;check(f.calls.length===0&&!p.chosen.value,'Disposed native tools never dispatch a pending operation')});
 }
 {
  setBearer('a');const f=nativeFixture();await mounted(useToolsPage,async p=>{const adminOp=operations.find(o=>o.role==='admin');p.open(adminOp);await p.invoke();check(f.calls.length===0&&f.modals[0].showCancel===false,'The client API workbench still refuses administrator-only operations')});
 }
 clearBearer();console.log(`WORKFLOW SESSION CHECK PASSED: ${assertions} assertions`);
}finally{delete global.__workflowSessionApi;delete global.__workflowSessionErrors;delete global.uni;fs.rmSync(tmp,{recursive:true,force:true})}
