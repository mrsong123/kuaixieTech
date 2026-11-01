import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-escrow-refund-'));
let assertions=0;function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
const flush=async()=>{await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0))};
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.t=(key,values)=>key+(values?JSON.stringify(values):'');exports.uiErrorMessage=e=>e.message;`);
 let code=ts.transpileModule(fs.readFileSync(path.join(root,'packages/ui/src/useEscrowRefunds.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 const file=path.join(tmp,'escrow.cjs');fs.writeFileSync(file,code);const {useEscrowRefunds,checkedEscrowReceipt}=require(file),{createApiClient,ApiError,setBearer,clearBearer}=require(path.join(tmp,'index.js'));
 const receipt=(caseId='case',requestId='request',sourceRound=3)=>({id:'execution-'+caseId,caseId,orderId:'order-'+caseId,requestId,sourceRound,amount:'50.00',currency:'CNY',status:'succeeded',refundMethod:'escrow_wallet',destination:'platform_wallet',requiresAccounting:false,rewardReviewRequired:true,createdAt:'2026-11-01T00:00:00Z'});
 const budget=(caseId='case')=>({caseId,orderId:'order-'+caseId,currency:'CNY',refundMethod:'escrow_wallet',destination:'platform_wallet',status:'eligible',goodsAmount:'100.00',alreadyRefunded:'0.00',refundableGoods:'100.00',requestedAmount:'50.00',buyerEscrowHeld:'110.00',serviceFeeHeld:'5.00',shippingHeld:'5.00',sourceRound:3,requiresAccounting:false,rewardReviewRequired:true});
 function fixture(options={}){const calls=[];let posted=null,attempts=0;const api=createApiClient(async r=>{calls.push({...r,data:r.data===undefined?undefined:JSON.parse(JSON.stringify(r.data))});if(r.url.includes('/events?'))return [{id:'event',action:'wallet_refunded',detail:'actual ledger credit',createdAt:'2026-11-01T00:00:00Z'}];if(r.url.startsWith('/me/after-sales/escrow-refunds?'))return options.rows||[];const match=r.url.match(/after-sales\/([^/]+)\/escrow-(budget|refund)$/);assert.ok(match);const id=decodeURIComponent(match[1]);if(match[2]==='budget')return posted||{...budget(id),...(options.changedRound&&attempts?{sourceRound:4}:{})};attempts++;if(options.explicitError)throw new ApiError('explicit rejection',409);const result=receipt(id,r.data.requestId,r.data.expectedRound);if(options.malformed)result.refundMethod='original_payment';if(options.commitThenTimeout&&attempts===1){posted=result;throw new ApiError('API_NETWORK_ERROR',0)}if(options.timeoutFirst&&attempts===1)throw new ApiError('API_NETWORK_ERROR',0);posted=result;return result},{getToken:()=> 'unit-token'});return {api,calls};}
 const adminOptions={admin:true,caseId:()=> 'case',orderId:()=> 'order-case',role:()=> 'operator'};
 {
  const f=fixture();await mounted(()=>useEscrowRefunds(f.api,async()=>true,adminOptions),async p=>{await flush();check(p.eligible.value?.requestedAmount==='50.00'&&p.canExecute.value&&f.calls.every(r=>r.method==='GET'),'Admin execution begins with a read-only server budget and no automatic credit');await p.execute();check(!f.calls.some(r=>r.method==='POST')&&p.error.value==='escrowRefund.noteRequired','An execution note is required before any financial request');});
 }
 {
  const f=fixture();await mounted(()=>useEscrowRefunds(f.api,async()=>false,adminOptions),async p=>{await flush();p.note.value='Checked';await p.execute();check(!f.calls.some(r=>r.method==='POST'),'Declining wallet-refund confirmation never sends a financial request');});
 }
 {
  const f=fixture();await mounted(()=>useEscrowRefunds(f.api,async()=>true,adminOptions),async p=>{await flush();p.note.value='Checked goods evidence';await p.execute();const request=f.calls.find(r=>r.method==='POST');check(request?.url==='/portal/admin/after-sales/case/escrow-refund'&&request.data.expectedRound===3&&!!request.data.requestId&&!('amount'in request.data)&&!('paymentId'in request.data),'Execution carries the case round and idempotency key without caller-controlled amounts or payment sources');check(p.receipt.value?.status==='succeeded'&&!p.pending.value&&p.notice.value==='escrowRefund.walletRecorded','Only a matching wallet execution receipt is displayed as posted');check(f.calls.every(r=>!r.url.includes('/payment-refunds')),'Escrow wallet refunds never invoke the original-payment refund API');});
 }
 {
  const f=fixture({commitThenTimeout:true});await mounted(()=>useEscrowRefunds(f.api,async()=>true,adminOptions),async p=>{await flush();p.note.value='Checked';await p.execute();check(!!p.pending.value&&!p.receipt.value&&!p.retryAllowed.value,'An unknown HTTP result keeps its original execution identity and blocks blind retry');await p.loadBudget();check(p.receipt.value?.status==='succeeded'&&!p.pending.value&&f.calls.filter(r=>r.method==='POST').length===1,'Reading an already committed server execution resolves uncertainty without another credit request');});
 }
 {
  const f=fixture({timeoutFirst:true});await mounted(()=>useEscrowRefunds(f.api,async()=>true,adminOptions),async p=>{await flush();p.note.value='Original note';await p.execute();p.note.value='Edited after unknown result';await p.retry();check(f.calls.filter(r=>r.method==='POST').length===1,'Unknown execution cannot be retried until the current server record was read');await p.loadBudget();check(p.retryAllowed.value,'The same round and budget permit only an original-key retry');await p.retry();const requests=f.calls.filter(r=>r.method==='POST');check(requests.length===2&&JSON.stringify(requests[0].data)===JSON.stringify(requests[1].data),'Retry preserves the original request key, round and note even if local inputs changed');});
 }
 {
  const f=fixture({timeoutFirst:true,changedRound:true});await mounted(()=>useEscrowRefunds(f.api,async()=>true,adminOptions),async p=>{await flush();p.note.value='Checked';await p.execute();await p.loadBudget();await p.retry();check(!!p.pending.value&&!p.retryAllowed.value&&f.calls.filter(r=>r.method==='POST').length===1,'A changed decision round blocks replay against a different case revision');});
 }
 {
  const f=fixture({malformed:true});await mounted(()=>useEscrowRefunds(f.api,async()=>true,adminOptions),async p=>{await flush();p.note.value='Checked';await p.execute();check(!!p.pending.value&&!p.receipt.value,'An original-channel refund response cannot masquerade as a platform-wallet credit');});
 }
 {
  const f=fixture({explicitError:true});await mounted(()=>useEscrowRefunds(f.api,async()=>true,adminOptions),async p=>{await flush();p.note.value='Checked';await p.execute();check(!p.pending.value&&!p.budget.value&&!p.canExecute.value,'An explicit server rejection requires a fresh budget instead of reusing the old execution');});
 }
 {
  const current=vue.ref('case');const f=fixture();let decide;const dialog=new Promise(resolve=>{decide=resolve});await mounted(()=>useEscrowRefunds(f.api,async()=>dialog,{admin:true,caseId:()=>current.value,orderId:()=> 'order-'+current.value}),async p=>{await flush();p.note.value='Checked';const executing=p.execute();current.value='other';await flush();decide(true);await executing;check(!f.calls.some(r=>r.method==='POST'),'Switching cases during confirmation cannot execute the old case budget');});
 }
 {
  const f=fixture({rows:[receipt()]});await mounted(()=>useEscrowRefunds(f.api,async()=>{throw new Error('readonly')},{caseId:()=> 'case',orderId:()=> 'order-case',role:()=> 'buyer',seed:()=>receipt()}),async p=>{await p.loadRecords();await p.loadEvents(p.receipt.value.id);check(p.receipt.value?.refundMethod==='escrow_wallet'&&f.calls.every(r=>r.method==='GET')&&!p.canExecute.value,'Buyer surfaces only read validated wallet receipts and events');});
 }
 {
  const f=fixture();await mounted(()=>useEscrowRefunds(f.api,async()=>false,{caseId:()=> 'case',orderId:()=> 'order-case',role:()=> 'merchant',seed:()=>receipt()}),async p=>{await p.loadRecords();await p.loadEvents('execution-case');check(p.receipt.value?.id==='execution-case'&&!p.canReadEvents.value&&!f.calls.length,'Merchant case projections do not call buyer-only receipt or timeline endpoints');});
 }
 {
  const current=vue.ref('case');let resolveEvent;const api=createApiClient(async()=>new Promise(resolve=>{resolveEvent=resolve}));await mounted(()=>useEscrowRefunds(api,async()=>false,{caseId:()=>current.value,orderId:()=> 'order-'+current.value,role:()=> 'buyer',seed:()=>receipt(current.value)}),async p=>{const loading=p.loadEvents('execution-case');current.value='other';resolveEvent([{id:'old-event',action:'wallet_refunded',detail:'old',createdAt:'2026-11-01T00:00:00Z'}]);await loading;check(!p.events.value.length&&!p.eventsLoading.value&&!p.moreEvents.value,'A late timeline response cannot populate a different case or a newly replaced receipt');});
 }
 {
  setBearer('admin-A');const f=fixture();let decide;const dialog=new Promise(resolve=>{decide=resolve});try{await mounted(()=>useEscrowRefunds(f.api,async()=>dialog,adminOptions),async p=>{await flush();p.note.value='Private note';const executing=p.execute();setBearer('admin-B');decide(true);await executing;check(!f.calls.some(r=>r.method==='POST')&&!p.pending.value&&!p.note.value&&!p.notice.value,'A session transition clears financial inputs and blocks an old admin confirmation');});}finally{clearBearer()}
 }
 {
  const full={...receipt(),requiresAccounting:true};check(checkedEscrowReceipt(full).requiresAccounting&&full.status==='succeeded','A successful wallet credit can still carry an explicit further-accounting requirement');
 }
 console.log(`ESCROW REFUND MODEL CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
