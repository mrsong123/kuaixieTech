import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url);
const ts=require('typescript');
const pcRequire=createRequire(path.join(root,'apps/pc/package.json'));
const vuePath=pcRequire.resolve('vue');const vue=require(vuePath);
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-business-'));
let assertions=0;
function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
try{
 const sdk=path.join(root,'packages/sdk/src');
 execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 // These unit tests isolate business decisions from localization rendering.
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.t=(key,values)=>key+(values?JSON.stringify(values):'');exports.uiErrorMessage=e=>e.message;exports.operationLabel=op=>op.name;exports.formatAmount=(v,c)=>String(v)+' '+c;`);
 function model(name){let code=ts.transpileModule(fs.readFileSync(path.join(root,'packages/ui/src',name+'.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
  for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
  const file=path.join(tmp,name+'.cjs');fs.writeFileSync(file,code);return require(file);
 }
 const {createApiClient,setBearer,clearBearer}=require(path.join(tmp,'index.js'));
 const {useBusinessAuth}=model('useBusinessAuth');
 const authCalls=[];let accepted=0;
 const authApi=createApiClient(async request=>{authCalls.push(request);return request.url==='/auth/captcha'?{id:'captcha',image:'data:image/png;base64,AA=='}:{accessToken:'unit.test.token'};});
 await mounted(()=>useBusinessAuth(authApi,{persistLocale(){},sessionAccepted(){accepted++},openLegal(){},openSupport(){}}),async p=>{
  await p.submit();check(!authCalls.some(r=>r.url==='/auth/login'),'Invalid contact cannot submit login on any platform');
  p.contact.value='13800138000';p.password.value='password123';await p.submit();check(accepted===1,'Shared login accepts only a server-issued session through the platform callback');
  p.mode.value='signup';p.agreed.value=false;p.captchaText.value='ABCD';await p.sendCode();check(!authCalls.some(r=>r.url==='/auth/send-code'),'OTP delivery requires the shared consent check');
  p.agreed.value=true;p.code.value='123456';await p.submit();const body=authCalls.find(r=>r.url==='/auth/register')?.data;check(body?.countryCode==='+86'&&body?.role==='buyer','Registration keeps channel, country and role consistent across presentations');
 });
 const {useBusinessWorkflow}=model('useBusinessWorkflow');setBearer('unit.test.token');
 const quote={pricingVersion:'escrow_v2_external_insurance',currency:'CNY',goodsAmount:'1000.00',serviceFee:'50.00',insurancePremium:'0.00',buyerEscrow:'1050.00'};
 for(const confirmed of [false,true]){
  const calls=[];const api=createApiClient(async request=>{calls.push(request);return request.url==='/rpc/quote_pk_award'?quote:{id:'order'};},{getToken:()=> 'unit.test.token'});
  await mounted(()=>useBusinessWorkflow(api,'pk-quotes',async()=>confirmed),async p=>{
   const index=p.flow.operations.findIndex(op=>op.path==='/rpc/award_pk');p.pick(index);await vue.nextTick();p.update('demandId','demand');p.update('quoteId','quote');p.update('insuranceChoice','waived');await p.submit();await new Promise(resolve=>setTimeout(resolve,0));
   check(calls.some(r=>r.url==='/rpc/quote_pk_award'),'Award always obtains a backend escrow quote before confirmation');
   const award=calls.find(r=>r.url==='/rpc/award_pk');check(confirmed?award?.data.expectedBuyerEscrow==='1050.00':!award,confirmed?'Confirmed award transmits the exact server escrow quote':'Declining an escrow quote never submits the award');
  });
 }
 for(const confirmed of [false,true]){
  const calls=[];const api=createApiClient(async request=>{calls.push(request);return request.url.startsWith('/me/orders/')?{id:'order',vendor_deposit:'200.00'}:'OK:order';},{getToken:()=> 'unit.test.token'});
  await mounted(()=>useBusinessWorkflow(api,'order-actions',async()=>confirmed),async p=>{
   p.update('orderId','order');await p.submit();
   const accepted=calls.find(r=>r.url==='/rpc/accept_order_atomic');check(calls[0]?.url==='/me/orders/order','Carrier acceptance reads the deposit from the server-owned order');
   check(confirmed?accepted?.data.deposit==='200.00':!accepted,confirmed?'Carrier acceptance preserves the exact quoted deposit':'Declining the carrier deposit never accepts an order');
  });
 }
 const {goodsForInsurance,useInsuranceApplication}=model('useInsurance');
 check(goodsForInsurance('0.29',3)==='0.87'&&goodsForInsurance('100.01',2)==='200.02','Insured declared value uses exact CNY minor units');
 check(goodsForInsurance('1.001',1)===''&&goodsForInsurance('1.00',0)==='','Invalid quantity or excess decimal precision cannot form an insurance quote');
 const insuranceCalls=[];const insurerApi=createApiClient(async r=>{insuranceCalls.push(r);if(r.url==='/me/orders/order')return {id:'order',item_name:'Camera',goods_amount:'1000.00',insurance_quote_id:'insurance-quote'};if(r.url==='/me/insurance/quotes/insurance-quote')return {id:'insurance-quote',premium:'8.50',currency:'CNY',paymentMode:'insurer_direct'};return {application:null};});
 await mounted(()=>useInsuranceApplication(insurerApi,()=> 'order'),async p=>{
  await new Promise(resolve=>setTimeout(resolve,0));await p.apply();check(!insuranceCalls.some(r=>r.method==='POST'),'Insurer application cannot submit without explicit terms and disclosure consent');
 });
 const {usePublishComposer}=model('usePublishComposer');const publishCalls=[];
 const publisher=usePublishComposer(createApiClient(async r=>{publishCalls.push(r);return {id:'demand',status:'pending_review'}}));
 await publisher.submit();check(publishCalls.length===0,'Shared publish validation rejects an empty demand');publisher.demand.value.title='Camera';publisher.demand.value.expectPrice='100.01';await publisher.submit();check(publishCalls[0].url==='/portal/demands'&&publisher.notice.value==='portal.publish.review','Both publication presentations preserve pending-review status');
 const {useTradeCases}=model('useTradeCases');const {ApiError}=require(path.join(tmp,'index.js'));
 const caseCalls=[];let failCase=true;const caseApi=createApiClient(async r=>{caseCalls.push(r);if(r.method==='POST'&&r.url==='/me/disputes'){if(failCase)throw new ApiError('API_NETWORK_ERROR',0);return {id:'case',stage:'negotiating'}}if(r.method==='POST')return {id:'case',stage:'platform_review'};if(r.url==='/me/disputes/case')return {id:'case',stage:'negotiating',round:2,myRole:'buyer',events:[]};if(r.url==='/me/orders/order')return {id:'order',my_role:'buyer',status:'purchased',goods_amount:'100.00'};return [];});
 await mounted(()=>useTradeCases(caseApi,async()=>true),async p=>{
  p.draft.orderId='order';await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0));p.draft.description='Damaged goods';p.draft.amount='0.29';await p.create();
  const first=caseCalls.find(r=>r.method==='POST');check(first?.data.refundAmount==='0.29'&&first.data.evidenceUrls.length===0,'Shared case creation preserves exact CNY amount and permits a textual claim');
  check(!!p.pending.value&&!!first.data.requestId,'An ambiguous case submission keeps its original request identity');
  p.draft.amount='99.99';failCase=false;await p.retry();const posts=caseCalls.filter(r=>r.method==='POST');check(posts.length===2&&JSON.stringify(posts[0].data)===JSON.stringify(posts[1].data),'Retrying a case sends the original immutable request rather than edited form values');
  check(!p.pending.value&&p.entry.value?.id==='case','A confirmed case response reloads the authoritative detail');
  p.note.value='Please review';await p.act('escalate');const action=caseCalls.find(r=>r.url==='/me/disputes/case/escalate');check(action.data.expectedRound===2&&action.data.reason==='Please review'&&!('actorId' in action.data),'Case transitions send the server round and derive identity on the backend');
 });
 const malformed=useTradeCases(createApiClient(async()=>({})),async()=>true);malformed.entry.value={id:'case',stage:'negotiating',round:2,events:[]};malformed.note.value='Review';await malformed.act('escalate');check(!!malformed.pending.value,'A malformed success response remains unconfirmed and keeps its retry identity');
 const declinedCalls=[];const declined=useTradeCases(createApiClient(async r=>{declinedCalls.push(r);return {}}),async()=>false);declined.order.value={id:'order',my_role:'buyer',status:'purchased'};declined.draft.orderId='order';declined.draft.description='Damaged';declined.draft.amount='5.00';await declined.create();check(!declinedCalls.some(r=>r.method==='POST'),'Declining confirmation does not open a case');
 const {useShop}=model('useShop');const shopCalls=[];let ambiguousShop=true;const shopApi=createApiClient(async r=>{shopCalls.push(r);if(r.method==='POST'&&r.url==='/me/shop/applications'){if(ambiguousShop)throw new ApiError('API_NETWORK_ERROR',0);return {id:'application',status:'pending'}}if(r.url==='/me/shop')return {store:null,applications:[],products:[]};return []});
 await mounted(()=>useShop(shopApi,async()=>true,'app'),async p=>{Object.assign(p.application,{businessName:'Example',creditCode:'91350211M000100Y46',representative:'Representative',licenseUploadId:'upload'});await p.apply();check(!!p.pending.value,'An ambiguous shop application keeps its original request');p.application.businessName='Edited';ambiguousShop=false;await p.apply();const posts=shopCalls.filter(r=>r.method==='POST');check(posts.length===2&&JSON.stringify(posts[0].data)===JSON.stringify(posts[1].data),'Shop retries cannot silently change the submitted business or attachment');check(posts[0].data.channel==='app'&&!('ownerId'in posts[0].data),'Native shop applications use the shared model and server-side ownership');});
 const {useProductPublishing}=model('useProductPublishing');const editCalls=[];const editApi=createApiClient(async r=>{editCalls.push(r);return r.method==='GET'?[]:{id:'product',status:'off'}});const editor=useProductPublishing(editApi);const owned={id:'product',name:'Camera',region:'JP',price:'100.00',stock:5,status:'off',image_urls:[]};editor.rows.value=[owned];editor.edit(owned);owned.stock=1;editor.draft.value.stock='8';await editor.save();check(editCalls.find(r=>r.method==='PUT')?.data.expectedStock===5,'Product editing confirms the original loaded stock rather than overwriting a concurrent reservation');
 clearBearer();console.log(`BUSINESS MODEL CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
