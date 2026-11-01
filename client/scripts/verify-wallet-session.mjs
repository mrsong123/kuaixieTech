import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-wallet-session-'));
let count=0;const check=(value,message)=>{assert.ok(value,message);count++;console.log('OK:',message)};
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
const flush=async()=>{await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0))};
const deferred=()=>{let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {resolve,reject,promise}};
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 const sdkPath=path.join(tmp,'index.js'),localePath=path.join(tmp,'locale.cjs'),localeVuePath=path.join(tmp,'locale-vue.cjs');
 fs.writeFileSync(localePath,`exports.t=(key,values)=>key+(values?JSON.stringify(values):'');exports.uiErrorMessage=e=>e.message;`);
 fs.writeFileSync(localeVuePath,`exports.localeRef=require(${JSON.stringify(vuePath)}).ref('zh-CN')`);
 const rewrite=source=>{let code=ts.transpileModule(source,{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',sdkPath],['@kuaixie/i18n',localePath],['@kuaixie/i18n/vue',localeVuePath]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);return code};
 const modelPath=path.join(tmp,'wallet.cjs');fs.writeFileSync(modelPath,rewrite(fs.readFileSync(path.join(root,'packages/ui/src/usePaymentWallet.ts'),'utf8')));
 const {usePaymentWallet}=require(modelPath),{createApiClient,setBearer,getBearer,ApiError}=require(sdkPath);
 const A='123e4567-e89b-42d3-a456-426614174000',B='223e4567-e89b-42d3-a456-426614174000',Q='323e4567-e89b-42d3-a456-426614174000',P='a'.repeat(32);
 const quote=()=>({id:Q,merchantId:'merchant',merchantName:'Merchant',method:'wxpay',platform:'web',baseCurrency:'CNY',baseAmount:'10.00',paymentCurrency:'USD',paymentAmount:'1.40',rate:'0.14',spreadBps:0,rateSource:'provider',rateAsOf:'2026-01-01T00:00:00Z',expiresAt:'2099-01-01T00:00:00Z',settlementCurrency:'CNY'});
 const payment=(status='pending',id=P)=>({id,status,baseCurrency:'CNY',baseAmount:'10.00',paymentCurrency:'USD',paymentAmount:'1.40',merchantName:'Merchant',checkout:null});
 function fixture(options={}){const calls=[];const api=createApiClient(async request=>{calls.push({...request,data:request.data===undefined?undefined:JSON.parse(JSON.stringify(request.data))});if(request.url==='/me/profile')return options.profile?options.profile():{id:getBearer()==='B'?B:A};if(request.url==='/public/payments/currencies')return [{code:'USD',name:'美元',nameEn:'US Dollar',minorDigits:2}];if(request.url.startsWith('/public/payments/methods'))return [{id:'merchant',name:'Merchant',status:'configured',currencies:['USD']}];if(request.url==='/me/payments')return request.method==='POST'?(options.create?options.create(request.data):payment()):(options.history?options.history():[]);if(request.url==='/me/payments/quotes')return options.quote?options.quote(request.data):quote();if(request.url.startsWith('/me/payments/'))return options.status?options.status(request.url.split('/').at(-1)):payment();if(request.url.startsWith('/public/payments/rate'))return options.rate?options.rate():{base:'CNY',currency:'USD',rate:'0.14',asOf:'2026-01-01T00:00:00Z'};throw new Error(request.url)},{getToken:getBearer});return {api,calls}}
 const prepare=async p=>{await flush();p.merchantId.value='merchant';await vue.nextTick();p.currency.value='USD';p.amountCny.value='10.00';await vue.nextTick();await p.preview()};
 const storage=map=>({get:key=>map.get(key)||null,set:(key,value)=>map.set(key,value),remove:key=>map.delete(key)});
 setBearer('A');{
  const f=fixture();await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await prepare(p);check(p.quote.value?.id===Q&&f.calls.some(c=>c.url==='/me/profile'),'Quote creation waits for the authenticated server profile and retains exact decimal strings');});
 }
 setBearer('A');{
  let attempts=0;const bodies=[],map=new Map([['kx.payment.pending.web',JSON.stringify({paymentId:P})]]);const f=fixture({create:body=>{bodies.push(body);if(++attempts===1)throw new ApiError('timeout',0);return payment()}});
  await mounted(()=>usePaymentWallet(f.api,'web',undefined,storage(map)),async p=>{await prepare(p);await p.confirm(Q);check(!!p.pending.value&&!p.payment.value,'An ambiguous create retains its original idempotency request');await p.confirm(Q);check(bodies.length===2&&JSON.stringify(bodies[0])===JSON.stringify(bodies[1]),'An uncertain creation retries the original quote and nonce without a second payment intent');const saved=JSON.parse(map.get('kx.payment.pending.v2.web.'+A));check(saved.ownerId===A&&saved.platform==='web'&&saved.paymentId===P&&!JSON.stringify(saved).includes('Bearer'),'Recovery is stored by verified user and platform, with no token or checkout credentials');check(!map.has('kx.payment.pending.web')&&!f.calls.some(c=>c.url==='/me/payments/'+P),'Legacy platform-only recovery is deleted and is never queried');});
  setBearer('B');const g=fixture();await mounted(()=>usePaymentWallet(g.api,'web',undefined,storage(map)),async p=>{await flush();check(!p.pending.value&&!p.payment.value&&!g.calls.some(c=>c.url==='/me/payments/'+P),'A different account never restores or queries the prior account payment');});
 }
 setBearer('A');{
  const d=deferred();const f=fixture({quote:()=>d.promise});await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await flush();p.merchantId.value='merchant';await vue.nextTick();p.currency.value='USD';p.amountCny.value='10.00';await vue.nextTick();const action=p.preview();setBearer('B');d.resolve(quote());await action;check(!p.quote.value&&!p.amountCny.value&&!p.history.value.length&&p.error.value==='pay.sessionChanged','A session transition clears funding data and rejects a late quote');await p.confirm();check(!f.calls.some(c=>c.url==='/me/payments'&&c.method==='POST'),'A quote from the old page cannot be confirmed with the new bearer');});
 }
 setBearer('A');{
  const d=deferred();let paid=0;const f=fixture({create:()=>d.promise});await mounted(()=>usePaymentWallet(f.api,'web',()=>paid++),async p=>{await prepare(p);const action=p.confirm(Q);setBearer('B');d.resolve(payment('paid'));await action;check(paid===0&&!p.payment.value&&!p.pending.value,'A late successful receipt from a prior account cannot notify or populate the new session');setBearer('A');await p.load();check(!p.payment.value&&p.error.value==='pay.sessionChanged','Returning to the same token cannot revive an invalidated old model');});
 }
 setBearer('A');{
  const d=deferred();const f=fixture({history:()=>d.promise});await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await flush();setBearer('B');d.resolve([payment('paid')]);await flush();check(p.history.value.length===0,'A late payment-history response never reveals the old account history');});
 }
 setBearer('A');{
  let paid=0;const f=fixture({create:()=>({...payment('paid'),paymentAmount:'999.99'})});await mounted(()=>usePaymentWallet(f.api,'web',()=>paid++),async p=>{await prepare(p);await p.confirm(Q);check(paid===0&&!p.payment.value&&!!p.pending.value,'A mismatched paid response cannot clear the pending request or claim a wallet credit');});
 }
 setBearer('A');{
  const d=deferred();const f=fixture({create:()=>d.promise});await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await prepare(p);const one=p.confirm(Q);await p.confirm(Q);check(f.calls.filter(c=>c.url==='/me/payments'&&c.method==='POST').length===1,'Concurrent confirmation sends at most one creation request');d.resolve(payment());await one;});
 }
 setBearer('A');{
  const map=new Map();let first=true;const f=fixture({create:()=>{if(first){first=false;throw new ApiError('timeout',0)}return payment('paid')}});let original;
  await mounted(()=>usePaymentWallet(f.api,'web',undefined,storage(map)),async p=>{await prepare(p);await p.confirm(Q);original=JSON.parse(JSON.stringify(p.pending.value));});
  await mounted(()=>usePaymentWallet(f.api,'web',undefined,storage(map)),async p=>{await flush();check(p.pending.value?.requestId===original.requestId&&p.quote.value?.id===Q,'Same-account recovery restores the original nonce only after profile verification');await p.confirm(Q);check(p.payment.value?.status==='paid'&&!map.has('kx.payment.pending.v2.web.'+A),'A server-confirmed recovered payment clears only that account recovery record');});
 }
 setBearer('A');{
  const map=new Map([['kx.payment.pending.v2.web.'+A,JSON.stringify({ownerId:B,platform:'web',paymentId:P})]]);const f=fixture();await mounted(()=>usePaymentWallet(f.api,'web',undefined,storage(map)),async p=>{await flush();check(!p.payment.value&&!f.calls.some(c=>c.url==='/me/payments/'+P)&&!map.size,'A recovery payload with an owner mismatch is rejected without querying payment status');});
 }
 setBearer('A');{
  const d=deferred();let paid=0;const f=fixture({status:()=>d.promise,history:()=>[payment()]});await mounted(()=>usePaymentWallet(f.api,'web',()=>paid++),async p=>{await flush();p.resume(p.history.value[0]);const action=p.refresh();setBearer('B');d.resolve(payment('paid'));await action;check(paid===0&&!p.payment.value,'A late provider status response cannot mark the new session as paid');});
 }
 setBearer('A');{
  const d=deferred();const f=fixture({rate:()=>d.promise});await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await flush();p.referenceTarget.value='USD';const action=p.rate();setBearer('B');d.resolve({base:'CNY',currency:'USD',rate:'0.14',asOf:'2026-01-01'});await action;check(!p.reference.value&&!p.referenceTarget.value,'Exchange-rate inputs and responses also clear on session invalidation');});
 }
 setBearer('A');{
  const f=fixture();await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await prepare(p);await p.confirm('423e4567-e89b-42d3-a456-426614174000');check(!f.calls.some(c=>c.url==='/me/payments'&&c.method==='POST')&&p.error.value==='pay.changed','Confirming an obsolete quote identity cannot create a different payment');});
 }
 setBearer('A');{
  const originalSet=globalThis.setInterval,originalClear=globalThis.clearInterval;let tick,cleared=0;
  globalThis.setInterval=callback=>{tick=callback;return 42};globalThis.clearInterval=id=>{if(id===42)cleared++};
  try{const f=fixture({history:()=>[payment()]});await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await flush();p.resume(p.history.value[0]);setBearer('B');tick();await flush();check(cleared===1&&!f.calls.some(c=>c.url==='/me/payments/'+P),'Session invalidation stops polling and even a queued old tick cannot query the new account');});}finally{globalThis.setInterval=originalSet;globalThis.clearInterval=originalClear}
 }
 // Execute the native script with a deferred modal and the actual shared model.
 function native(p,uni){const source=fs.readFileSync(path.join(root,'apps/mobile/src/components/WalletFundingMobile.vue'),'utf8').match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1];let code=rewrite(source);const modelStub=path.join(tmp,'native-model.cjs'),apiStub=path.join(tmp,'native-api.cjs'),eventStub=path.join(tmp,'native-event.cjs');fs.writeFileSync(modelStub,'exports.usePaymentWallet=()=>globalThis.__walletTestModel;exports.paymentStateKey=x=>x');fs.writeFileSync(apiStub,'exports.api={}');fs.writeFileSync(eventStub,'exports.uniEventValue=e=>e.detail.value');for(const [alias,target] of [['@kuaixie/ui/usePaymentWallet',modelStub],['../api/client',apiStub],['../utils/uniEvent',eventStub]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);globalThis.__walletTestModel=p;return new Function('require','uni','defineEmits','exports',code+';return {confirm,openCheckout};')(require,uni,()=>()=>{},{});}
 setBearer('A');{
  const modal=deferred();const f=fixture();await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await prepare(p);const n=native(p,{showModal:()=>modal.promise});const action=n.confirm();setBearer('B');modal.resolve({confirm:true});await action;check(!f.calls.some(c=>c.url==='/me/payments'&&c.method==='POST'),'A native confirmation dialog cannot submit an old quote after an account switch');});
 }
 setBearer('A');{
  const modal=deferred();const f=fixture();await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await prepare(p);const n=native(p,{showModal:()=>modal.promise});const action=n.confirm();p.quote.value={...quote(),id:'423e4567-e89b-42d3-a456-426614174000'};modal.resolve({confirm:true});await action;check(!f.calls.some(c=>c.url==='/me/payments'&&c.method==='POST'),'A native confirmation preserves the exact quote shown in its dialog');});
 }
 setBearer('A');{
  const sdkDone=deferred();const f=fixture({history:()=>[{...payment(),checkout:{kind:'wechat_app',value:'{}'}}]});await mounted(()=>usePaymentWallet(f.api,'web'),async p=>{await flush();p.resume(p.history.value[0]);const n=native(p,{requestPayment:()=>sdkDone.promise});const action=n.openCheckout();setBearer('B');sdkDone.resolve({});await action;check(!f.calls.some(c=>c.url==='/me/payments/'+P),'An old native SDK callback cannot query or notify payment success under another account');});
 }
 console.log(`WALLET SESSION CHECK PASSED: ${count} assertions`);
}finally{delete globalThis.__walletTestModel;fs.rmSync(tmp,{recursive:true,force:true})}
