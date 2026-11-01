import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-insurance-session-'));
let count=0;function check(value,message){assert.ok(value,message);count++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model,unmounted=false;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});const dispose=()=>{if(!unmounted){unmounted=true;app.unmount()}};try{await test(model,dispose)}finally{dispose()}}
const flush=async()=>{await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0))};
function deferred(){let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {promise,resolve,reject}}
const future=()=>new Date(Date.now()+60000).toISOString();
const quote=(id='quote')=>({id,productId:'product',expiresAt:future(),paymentMode:'insurer_direct',protectionStatus:'not_issued'});
const policy=(id='policy',orderId='order')=>({id,orderId,paymentMode:'insurer_direct',status:'awaiting_provider',protectionStatus:'not_confirmed'});
const subject=()=>({subjectType:'order',subjectId:'order',itemName:'Camera',declaredValue:'100.00'});
function base(){return {account:{order:async id=>({id,item_name:id,goods_amount:'100.00',insurance_quote_id:'quote-'+id}),orders:async()=>[]},insurance:{companies:async()=>[],products:async()=>[],quote:async()=>quote(),ownQuote:async id=>quote(id),forOrder:async()=>({application:null}),apply:async body=>policy('policy',body.orderId),status:async()=>policy(),mine:async()=>[],consents:async()=>[]}}}
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.t=key=>key;exports.uiErrorMessage=e=>e.message;`);
 let source=ts.transpileModule(fs.readFileSync(path.join(root,'packages/ui/src/useInsurance.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])source=source.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 fs.writeFileSync(path.join(tmp,'insurance.cjs'),source);
 const {useInsuranceCentre,useInsuranceSelection,useInsuranceApplication}=require(path.join(tmp,'insurance.cjs'));
 const {setBearer,clearBearer}=require(path.join(tmp,'index.js'));
 {
  setBearer('buyer-A');const old=deferred(),api=base();api.account.orders=()=>old.promise;api.insurance.mine=async()=>[policy()];api.insurance.consents=async()=>[{id:'private-consent'}];
  await mounted(()=>useInsuranceCentre(api),async p=>{setBearer('buyer-B');check(!p.orders.value.length&&!p.applications.value.length&&!p.consents.value.length&&!p.selected.value&&!p.busy.value,'A session transition immediately clears private insurance orders, policies and consent receipts');old.resolve([{id:'order',insurance_quote_id:'quote'}]);await flush();check(!p.orders.value.length&&!p.applications.value.length&&!p.consents.value.length,'Late centre responses cannot restore a previous account\'s insurance data');let calls=0;api.account.orders=async()=>{calls++;return []};await p.load();check(calls===0,'An expired insurance view cannot reload using the new account');});
 }
 {
  setBearer('buyer-A');const old=deferred(),api=base(),selected=[];let calls=0;api.insurance.quote=async()=>{calls++;return old.promise};
  await mounted(()=>useInsuranceSelection(api,subject,v=>selected.push(v)),async p=>{await flush();p.product.value='product';const pending=p.preview();setBearer('buyer-B');old.resolve(quote());await pending;check(!p.quote.value&&!p.product.value&&selected.every(value=>value===null),'A quote response from a previous account never becomes the selected insurer quote');p.product.value='another';await p.preview();check(calls===1,'Changing a stale quote picker cannot issue a quote under a new token');});
 }
 {
  setBearer('buyer-A');const id=vue.ref('old'),old=deferred(),api=base(),selected=[];api.insurance.quote=input=>input.subjectId==='old'?old.promise:Promise.resolve(quote('new-quote'));
  await mounted(()=>useInsuranceSelection(api,()=>({...subject(),subjectId:id.value}),v=>selected.push(v)),async p=>{await flush();p.product.value='product';const first=p.preview();id.value='new';await p.preview();old.resolve(quote('old-quote'));await first;check(p.quote.value?.id==='new-quote'&&selected.filter(Boolean).every(q=>q.id==='new-quote'),'An older subject quote cannot replace the quote for the current order');});
 }
 {
  setBearer('buyer-A');const api=base();api.insurance.quote=async()=>({...quote(),expiresAt:'invalid'});
  await mounted(()=>useInsuranceSelection(api,subject,()=>{}),async p=>{await flush();p.product.value='product';await p.preview();check(!p.quote.value&&p.error.value==='insurance.unavailable','Malformed expiry cannot be presented as an actionable insurer quote');});
 }
 {
  setBearer('buyer-A');const id=vue.ref('old'),old=deferred(),api=base(),lookups=[];api.account.order=async value=>value==='old'?old.promise:{id:value,item_name:'new order',goods_amount:'200.00',insurance_quote_id:'new-quote'};api.insurance.ownQuote=async value=>{lookups.push(value);return quote(value)};api.insurance.forOrder=async value=>{lookups.push(value);return {application:null}};
  await mounted(()=>useInsuranceApplication(api,()=>id.value),async p=>{id.value='new';await flush();old.resolve({id:'old',item_name:'private old order',goods_amount:'100.00',insurance_quote_id:'old-quote'});await flush();check(p.risk.value.itemName==='new order'&&p.quote.value?.id==='new-quote','Changing orders rejects the older private order response');check(!lookups.includes('old')&&!lookups.includes('old-quote'),'An obsolete first response cannot start further private quote or policy requests');});
 }
 {
  setBearer('buyer-A');const api=base(),pending=deferred();let calls=0;api.insurance.apply=async()=>{calls++;return pending.promise};
  await mounted(()=>useInsuranceApplication(api,()=> 'order'),async p=>{await flush();p.termsAccepted.value=true;p.disclosuresAccepted.value=true;const applying=p.apply();setBearer('buyer-B');check(!p.quote.value&&!p.application.value&&!p.risk.value.itemName&&!p.termsAccepted.value&&!p.disclosuresAccepted.value,'Changing accounts clears insurance consent and in-flight application state');pending.resolve(policy());await applying;await p.apply();await p.refresh();check(!p.application.value&&calls===1,'An old application receipt cannot repopulate the view or enable further calls under a new account');});
 }
 {
  setBearer('buyer-A');const api=base(),requests=[];api.insurance.apply=async body=>{requests.push({...body});if(requests.length===1)throw new Error('unknown receipt');return policy('policy',body.orderId)};
  await mounted(()=>useInsuranceApplication(api,()=> 'order'),async p=>{await flush();p.termsAccepted.value=true;p.disclosuresAccepted.value=true;await p.apply();await p.apply();check(requests.length===2&&requests[0].requestId===requests[1].requestId&&JSON.stringify(requests[0])===JSON.stringify(requests[1]),'Retrying an unknown insurer application preserves its original idempotency key and consent body');check(p.application.value?.paymentMode==='insurer_direct','Only the insurer-direct application path is retained');});
 }
 {
  setBearer('buyer-A');const api=base();api.insurance.apply=async()=>policy('wrong','other-order');
  await mounted(()=>useInsuranceApplication(api,()=> 'order'),async p=>{await flush();p.termsAccepted.value=true;p.disclosuresAccepted.value=true;await p.apply();check(!p.application.value&&p.error.value==='insurance.unavailable','An application for another order cannot be shown as the current order\'s insurance');});
 }
 {
  setBearer('buyer-A');const api=base();api.insurance.status=async()=>policy('different-policy','order');
  await mounted(()=>useInsuranceApplication(api,()=> 'order'),async p=>{await flush();p.termsAccepted.value=true;p.disclosuresAccepted.value=true;await p.apply();await p.refresh();check(p.application.value?.id==='policy'&&p.error.value==='insurance.unavailable','Policy status refresh cannot replace an application with another policy identity');});
 }
 {
  setBearer('buyer-A');const api=base(),pending=deferred(),selected=[];api.insurance.quote=()=>pending.promise;await mounted(()=>useInsuranceSelection(api,subject,value=>selected.push(value)),async(p,dispose)=>{await flush();p.product.value='product';const work=p.preview();dispose();pending.resolve(quote());await work;check(!p.quote.value&&selected.every(value=>value===null),'Disposed insurance views discard late quotes without notifying a parent of a usable quote');});
 }
 clearBearer();console.log(`INSURANCE SESSION CHECK PASSED: ${count} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
