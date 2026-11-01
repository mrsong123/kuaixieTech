import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-checkout-'));
let assertions=0;function check(condition,message){assert.ok(condition,message);assertions++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
const flush=async()=>{await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0))};
try{
 const sdk=path.join(root,'packages/sdk/src');
 execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.t=(key,values)=>key+(values?JSON.stringify(values):'');exports.uiErrorMessage=e=>e.message;`);
 let code=ts.transpileModule(fs.readFileSync(path.join(root,'packages/ui/src/useProductCheckout.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 const file=path.join(tmp,'checkout.cjs');fs.writeFileSync(file,code);const {useProductCheckout,productDeclaredValue}=require(file),{createApiClient,ApiError,setBearer,clearBearer}=require(path.join(tmp,'index.js'));
 const item={id:'product',name:'Camera',price:'0.29',stock:10};
 const quote={productId:'product',tripId:'trip',storeId:null,productFingerprint:'a'.repeat(64),itemName:'Camera',itemSpec:null,unitPrice:'100.01',quantity:1,goodsAmount:'100.01',serviceFee:'5.00',insurancePremium:'0.00',buyerEscrow:'105.01',vendorDeposit:'20.00',currency:'CNY',pricingVersion:'escrow_v2_external_insurance',intentHours:24};
 function fill(p){Object.assign(p.form,{tripId:'trip',quantity:'1',contactName:'Recipient',contactPhone:'+8613800138000',address:'Street'})}
 function api(transport){return createApiClient(transport,{getToken:()=> 'unit-token'})}
 const trips=[{id:'trip',destination:'Tokyo',depart_date:'2026-11-02',remaining_slots:5,capacity_qty:5}];
 check(productDeclaredValue('0.29',3)==='0.87'&&productDeclaredValue('9999999999.99',100)==='999999999999.00','Insurance declaration multiplies exact minor units without floating-point rounding');
 check(productDeclaredValue('1.001',1)===''&&productDeclaredValue('1.00',101)===''&&productDeclaredValue('1.00',0)==='','Invalid precision or quantity cannot form an insurance declaration');
 {
  const calls=[];const client=api(async r=>{calls.push(r);return r.url.includes('/trips')?trips:quote});
  await mounted(()=>useProductCheckout(client,()=>item,async()=>true),async p=>{await flush();await p.prepare();check(!calls.some(r=>r.method==='POST'),'Incomplete shipping details cannot request or submit checkout');fill(p);await p.prepare();check(p.quote.value?.buyerEscrow==='105.01','Displayed escrow is the backend quote even when catalog prices differ');p.form.address='Changed street';check(p.quote.value===null,'Any shipping input change immediately invalidates the displayed quote');});
 }
 {
  const calls=[];const client=api(async r=>{calls.push(r);return r.url.includes('/trips')?trips:r.url.endsWith('/quote')?quote:{id:'order',status:'requesting'}});
  await mounted(()=>useProductCheckout(client,()=>item,async()=>false),async p=>{await flush();fill(p);await p.prepare();await p.submit();check(!calls.some(r=>r.url==='/me/product-orders'),'Declining confirmation never freezes money or creates an order');});
 }
 {
  const calls=[];let decide;const confirmation=new Promise(resolve=>{decide=resolve});const client=api(async r=>{calls.push(r);return r.url.includes('/trips')?trips:quote});
  await mounted(()=>useProductCheckout(client,()=>item,async()=>confirmation),async p=>{await flush();fill(p);await p.prepare();const submitted=p.submit();p.form.quantity='2';decide(true);await submitted;check(!calls.some(r=>r.url==='/me/product-orders'),'An input change while the confirmation dialog is open cannot submit the old quote');});
 }
 {
  const calls=[];let fail=true;const client=api(async r=>{calls.push(r);if(r.url.includes('/trips'))return trips;if(r.url.endsWith('/quote'))return quote;if(fail)throw new ApiError('API_NETWORK_ERROR',0);return {id:'order',status:'requesting'}});
  await mounted(()=>useProductCheckout(client,()=>item,async()=>true),async p=>{await flush();fill(p);await p.prepare();await p.submit();check(!!p.pending.value&&!!p.pending.value.requestId,'Ambiguous creation retains its idempotency key');p.form.contactName='Changed after failure';fail=false;await p.submit();const creates=calls.filter(r=>r.url==='/me/product-orders');check(creates.length===2&&JSON.stringify(creates[0].data)===JSON.stringify(creates[1].data),'Retry sends the immutable original request, including original recipient and request ID');check(creates[0].data.expectedUnitPrice==='100.01'&&creates[0].data.expectedBuyerEscrow==='105.01'&&creates[0].data.expectedProductFingerprint==='a'.repeat(64),'Checkout sends exact backend unit price and escrow rather than catalog arithmetic');check(p.created.value?.id==='order'&&!p.pending.value,'Only a confirmed nonempty backend order result clears the pending request');});
 }
 {
  const client=api(async r=>r.url.includes('/trips')?trips:r.url.endsWith('/quote')?quote:{});
  await mounted(()=>useProductCheckout(client,()=>item,async()=>true),async p=>{await flush();fill(p);await p.prepare();await p.submit();check(!!p.pending.value&&!p.created.value,'A malformed 200 response stays unconfirmed and cannot show order success');});
 }
 {
  const client=api(async r=>r.url.includes('/trips')?trips:r.url.endsWith('/quote')?{...quote,insurancePremium:'5.00'}:{id:'order',status:'requesting'});
  await mounted(()=>useProductCheckout(client,()=>item,async()=>true),async p=>{await flush();fill(p);await p.prepare();check(!p.quote.value&&!!p.error.value,'Platform insurance premiums in a checkout quote are rejected');});
 }
 {
  const calls=[];const client=api(async r=>{calls.push(r);return r.url.includes('/trips')?trips:quote});
  await mounted(()=>useProductCheckout(client,()=>item,async()=>true),async p=>{await flush();fill(p);p.form.insuranceChoice='yes';await p.prepare();check(!calls.some(r=>r.method==='POST'),'Choosing insurance requires an unexpired insurer quote before order quotation');p.setInsurance({id:'insurance',expiresAt:new Date(Date.now()+60000).toISOString(),paymentMode:'insurer_direct'});p.form.quantity='2';check(!p.insurance.value,'Changing goods quantity invalidates the insurer quote as well as checkout pricing');});
 }
 {
  const selected=vue.ref(item);let oldResolve;const client=api(async r=>r.url.includes('product/trips')?new Promise(resolve=>{oldResolve=resolve}):trips);
  await mounted(()=>useProductCheckout(client,()=>selected.value,async()=>true),async p=>{await vue.nextTick();selected.value={...item,id:'new-product'};await flush();oldResolve(trips);await flush();check(!p.loading.value&&p.trips.value.length===1,'Changing product during trip loading cannot leave loading stuck or overwrite the new selection');});
 }
 {
  let resolveQuote;const client=api(async r=>r.url.includes('/trips')?trips:new Promise(resolve=>{resolveQuote=resolve}));
  await mounted(()=>useProductCheckout(client,()=>item,async()=>true),async p=>{await flush();fill(p);p.form.insuranceChoice='yes';p.setInsurance({id:'insurance',expiresAt:new Date(Date.now()+60000).toISOString(),paymentMode:'insurer_direct'});const loading=p.prepare();p.setInsurance(null);resolveQuote(quote);await loading;check(!p.quote.value,'Invalidating insurance while a quote request is running discards the stale quote response');});
 }
 {
  setBearer('session-A');const calls=[];let decide;const confirmation=new Promise(resolve=>{decide=resolve});const client=api(async r=>{calls.push(r);return r.url.includes('/trips')?trips:quote});
  try{await mounted(()=>useProductCheckout(client,()=>item,async()=>confirmation),async p=>{await flush();fill(p);await p.prepare();const submitted=p.submit();setBearer('session-B');decide(true);await submitted;check(!calls.some(r=>r.url==='/me/product-orders')&&!p.pending.value&&p.form.address==='','A confirmation opened under another session cannot submit or retain its old delivery details');});}finally{clearBearer()}
 }
 {
  setBearer('session-A');const calls=[];const client=api(async r=>{calls.push(r);if(r.url.includes('/trips'))return trips;if(r.url.endsWith('/quote'))return quote;throw new ApiError('API_NETWORK_ERROR',0)});
  try{await mounted(()=>useProductCheckout(client,()=>item,async()=>true),async p=>{await flush();fill(p);await p.prepare();await p.submit();assert.ok(p.pending.value);setBearer('session-B');await p.submit();check(calls.filter(r=>r.url==='/me/product-orders').length===1&&!p.pending.value&&p.form.contactName==='','An unconfirmed request from another account is never retried under the new session');});}finally{clearBearer()}
 }
 console.log(`PRODUCT CHECKOUT MODEL CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
