import fs from 'node:fs';import os from 'node:os';import path from 'node:path';import assert from 'node:assert/strict';import {createRequire} from 'node:module';import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript'),vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-saved-session-'));
let assertions=0;const check=(value,message)=>{assert.ok(value,message);assertions++;console.log('OK:',message)};
const deferred=()=>{let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {promise,resolve,reject}};
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model,closed=false;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});const dispose=()=>{if(!closed){closed=true;app.unmount()}};try{await test(model,dispose)}finally{dispose()}}
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),"exports.t=key=>key;exports.uiErrorMessage=e=>e.message;exports.formatAmount=String;exports.formatDate=String;exports.formatBusinessStatus=String;");
 const sdkFile=path.join(tmp,'index.js'),{createApiClient,setBearer,getBearer,clearBearer}=require(sdkFile);
 function compile(relative,name,extra={}){let code=ts.transpileModule(fs.readFileSync(path.join(root,relative),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;for(const [alias,target]of Object.entries({'vue':vuePath,'@kuaixie/sdk':sdkFile,'@kuaixie/i18n':path.join(tmp,'locale.cjs'),...extra}))code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);const file=path.join(tmp,name+'.cjs');fs.writeFileSync(file,code);return require(file)}
 const {useSavedCommerce,useProductActions,useAddressSelection}=compile('packages/ui/src/useSavedCommerce.ts','saved');
 const address={id:'address-A',contact_name:'Private A',contact_phone:'13800000000',province:'P',city:'C',district:'D',detail:'Private street',is_default:true};
 const cart={id:'cart-A',product_id:'product-A',name:'Item A',price:'10.00',qty:2,available:true};
 const favorite={id:'favorite-A',target_type:'product',target_id:'product-A',title:'Item A'};
 function fixture(handler){const calls=[];const api=createApiClient(async request=>{const r={...request,data:request.data===undefined?undefined:JSON.parse(JSON.stringify(request.data))};calls.push(r);const next=handler?.(r);if(next!==undefined)return next;if(r.method==='GET')return r.url.includes('/addresses')?[address]:r.url.includes('/cart')?[cart]:[favorite];return {status:'saved'}},{getToken:getBearer});return {api,calls}}
 const writes=f=>f.calls.filter(r=>['POST','PUT','DELETE'].includes(r.method));
 setBearer('saved-A');
 {
  const f=fixture();await mounted(()=>useSavedCommerce(f.api,'addresses',async()=>true),async p=>{await p.load();p.edit(p.addresses.value[0]);check(p.draft.value.contactName==='Private A','Owned address editing uses the loaded private record');setBearer('saved-B');check(p.addresses.value.length===0&&!p.draft.value.contactName&&!p.editing.value,'A session transition immediately clears addresses and edit drafts');await p.save();check(writes(f).length===0,'An invalid old address view cannot save with the replacement account');});setBearer('saved-A');
 }
 for(const kind of ['addresses','cart','favorites']){
  const answer=deferred(),f=fixture();await mounted(()=>useSavedCommerce(f.api,kind,()=>answer.promise),async p=>{const pending=p.remove(kind==='addresses'?'address-A':'product-A','product');setBearer('saved-B');answer.resolve(true);await pending;check(writes(f).length===0,`An old ${kind} removal confirmation cannot dispatch under a new token`);});setBearer('saved-A');
 }
 {
  const response=deferred(),f=fixture(r=>r.method==='GET'?response.promise:undefined);await mounted(()=>useSavedCommerce(f.api,'addresses',async()=>true),async p=>{const pending=p.load();setBearer('saved-B');setBearer('saved-A');response.resolve([address]);await pending;check(p.addresses.value.length===0,'A to B to A rejects the original address response even when the token value returns');});
 }
 {
  const saved=deferred(),f=fixture(r=>r.method==='POST'?saved.promise:undefined);await mounted(()=>useSavedCommerce(f.api,'addresses',async()=>true),async p=>{Object.assign(p.draft.value,{contactName:'First',contactPhone:'13800000000',detail:'Street 1'});const pending=p.save();p.draft.value.contactName='New draft';saved.resolve({status:'saved'});await pending;check(p.draft.value.contactName==='New draft','A successful old save does not erase a draft edited while the request was pending');check(writes(f)[0].data.contactName==='First','An address save submits an immutable input snapshot');});
 }
 {
  const f=fixture();await mounted(()=>useSavedCommerce(f.api,'cart',async()=>true),async p=>{await p.quantity(cart,'3');check(writes(f)[0].data.quantity===3,'A valid cart quantity still reaches the existing API');await p.quantity(cart,'0');check(writes(f).length===1,'Invalid cart quantities never dispatch');});
 }
 {
  const confirmation=deferred(),f=fixture();await mounted(()=>useSavedCommerce(f.api,'cart',()=>confirmation.promise),async p=>{const first=p.remove('product-A');await p.remove('product-A');confirmation.resolve(true);await first;check(writes(f).length===1,'Repeated clicks while confirmation is open cannot duplicate the delete');});
 }
 {
  const response=deferred(),f=fixture(r=>r.method==='GET'?response.promise:undefined);let emitted=null;await mounted(()=>useAddressSelection(f.api,value=>emitted=value),async p=>{const pending=p.load();setBearer('saved-B');response.resolve([address]);await pending;p.choose('address-A');check(p.rows.value.length===0&&emitted===null,'A late address-picker response cannot emit the old account contact details');});setBearer('saved-A');
 }
 {
  const f=fixture();let emitted=null;await mounted(()=>useAddressSelection(f.api,value=>emitted=value),async p=>{await p.load();p.choose('address-A');check(emitted.contactName==='Private A'&&emitted.address==='P C D Private street','Current-account address selection retains the existing callback contract');});
 }
 {
  const response=deferred(),f=fixture(r=>r.method==='PUT'?response.promise:undefined);await mounted(()=>useProductActions(f.api,()=> 'product-A'),async p=>{const pending=p.perform('cart');setBearer('saved-B');response.resolve({status:'saved'});await pending;check(!p.done.value&&!p.busy.value,'An old product action receipt cannot set success after account replacement');});setBearer('saved-A');
 }
 {
  clearBearer();const f=fixture();await mounted(()=>useProductActions(f.api,()=> 'public-product'),async p=>{setBearer('saved-A');await p.perform('favorite');check(writes(f)[0].headers.Authorization==='Bearer saved-A'&&p.done.value==='saved.added','An anonymous public product view can still act after login using a fresh operation');});
 }
 {
  const response=deferred(),f=fixture(r=>r.method==='GET'?response.promise:undefined);await mounted(()=>useSavedCommerce(f.api,'addresses',async()=>true),async(p,dispose)=>{const pending=p.load();dispose();response.resolve([address]);await pending;check(p.addresses.value.length===0,'Disposed address views reject late reads');});
 }
 // Use the actual original-order list model with only platform and network boundaries stubbed.
 fs.writeFileSync(path.join(tmp,'uni.cjs'),'exports.onShow=callback=>{globalThis.__orderShow=callback};');
 fs.writeFileSync(path.join(tmp,'prefs.cjs'),'exports.preferenceRef={value:{}};');
 fs.writeFileSync(path.join(tmp,'prototype.cjs'),'exports.mobilePreferenceStyle=()=>"";');
 fs.writeFileSync(path.join(tmp,'api.cjs'),'exports.api=globalThis.__orderApi;');
 const orderExtra={'@dcloudio/uni-app':path.join(tmp,'uni.cjs'),'@kuaixie/preferences/vue':path.join(tmp,'prefs.cjs'),'../utils/prototype':path.join(tmp,'prototype.cjs'),'../api/client':path.join(tmp,'api.cjs')};
 function orderModel(api){globalThis.__orderApi=api;delete require.cache[path.join(tmp,'api.cjs')];return compile('apps/mobile/src/stores/useOriginalOrderLists.ts','orders-'+Math.random(),orderExtra).useOriginalOrderList}
 const order={id:'order-A',order_no:'A',item_name:'Private order',status:'shipped',my_role:'buyer',goods_amount:'10.00',service_fee:'0.50',quantity:1};
 {
  const response=deferred(),f=fixture(r=>r.url.includes('/me/orders')?response.promise:{}),use=orderModel(f.api);await mounted(()=>use('shipment'),async p=>{const pending=p.load(true);setBearer('saved-B');response.resolve([order]);await pending;check(p.rows.length===0&&!p.busy,'A late private order list cannot populate the replacement account');check(p.canRead,'Order readability reacts to the new authenticated session');clearBearer();check(!p.canRead&&p.rows.length===0,'Logging out immediately disables and clears a cached order list');});setBearer('saved-A');
 }
 {
  const one=deferred(),two=deferred(),f=fixture(r=>r.url.includes('role=buyer')?one.promise:r.url.includes('role=carrier')?two.promise:{}),use=orderModel(f.api);await mounted(()=>use('shipment'),async p=>{const first=p.load(true);p.switchRole('carrier');two.resolve([{...order,id:'vendor-order',my_role:'carrier'}]);await new Promise(resolve=>setTimeout(resolve,0));one.resolve([order]);await first;check(p.rows.length===1&&p.rows[0].id==='vendor-order','A slower buyer-role response cannot overwrite the selected carrier list');});
 }
 {
  const response=deferred(),f=fixture(r=>r.url==='/me/profile'?response.promise:[]),use=orderModel(f.api);await mounted(()=>use('open'),async p=>{globalThis.__orderShow();setBearer('saved-B');setBearer('saved-A');response.resolve({carrier_verify_status:'approved'});await new Promise(resolve=>setTimeout(resolve,0));check(p.verified===null,'Old carrier verification cannot return after an A to B to A transition');check(f.calls.filter(r=>r.url.includes('/orders')).every(r=>r.url.startsWith('/me/')),'The alleged open pool remains a protected own-order projection, never an all-user query');});
 }
 globalThis.uni={navigateTo(){throw new Error('Old order must not navigate')}};
 {
  const f=fixture(r=>r.url.includes('/orders')?[order]:{}),use=orderModel(f.api);await mounted(()=>use('shipment'),async p=>{await p.load(true);setBearer('saved-B');assert.doesNotThrow(()=>p.detail(order));check(p.rows.length===0,'A click callback from an old order row cannot navigate after its account is cleared');});
 }
 console.log(`SAVED AND ORDER SESSION CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
