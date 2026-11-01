import fs from 'node:fs';import path from 'node:path';import os from 'node:os';import {execFileSync} from 'node:child_process';
const here=path.resolve(import.meta.dirname,'..');
const source=fs.readFileSync(path.join(here,'packages/sdk/src/registry.ts'),'utf8');
const json=source.split('= ')[1].replace(/;\s*$/,'');
const api=JSON.parse(json);
function check(v,msg){if(!v)throw Error('FAILED: '+msg);console.log('OK:',msg)}
const coreControllers=['PublicCatalogController','TradeRpcController','Batch10RpcController','Batch11RpcController','Batch12RpcController','AdminFinanceRpcController','KycRpcController','IdentitySettingsController','InviteReadRpcController'];
const mappedCount=coreControllers.reduce((total,name)=>total+[...fs.readFileSync(path.join(here,'../server/src/main/java/com/kuaixietech/controller',name+'.java'),'utf8').matchAll(/@(Get|Post|Put|Delete)Mapping\(/g)].length,0);
check(api.length===mappedCount&&new Set(api.map(x=>x.controller)).size===coreControllers.length,'Core route registry matches the current Java controllers');
check(new Set(api.map(x=>x.method+' '+x.path)).size===api.length,'All method + path pairs unique');
check(api.filter(x=>x.access==='public').length===[...fs.readFileSync(path.join(here,'../server/src/main/java/com/kuaixietech/controller/PublicCatalogController.java'),'utf8').matchAll(/@GetMapping\(/g)].length,'Public workbench routes match the catalog controller');
check(api.filter(x=>x.path.startsWith('/route-share')).length===0,'Missing route-share API is not advertised by the SDK');
check(api.every(x=>x.controller.endsWith('Controller.java')),'All operation locations recorded');
const must=['/public/products','/public/categories','/public/trips','/rpc/submit_pk_quote','/rpc/admin_stats','/rpc/batch12/add_post_comment'];
check(must.every(x=>api.some(o=>o.path===x)),'Existing commerce, trade, admin and comment routes present');
const rootContents=['apps/pc/src/App.vue','apps/admin/src/App.vue','apps/mobile/src/pages/index/index.vue','apps/mobile/src/pages/route/index.vue','apps/mobile/src/pages/discover/index.vue','apps/mobile/src/pages/tools/index.vue','apps/mobile/src/pages/me/index.vue'];
check(rootContents.every(x=>fs.existsSync(path.join(here,x))),'All 3 app entry points and 5 mobile pages exist');
for(const f of rootContents){const s=fs.readFileSync(path.join(here,f),'utf8');check(f.endsWith('/App.vue')?(s.includes('./stores/use')||s.includes("@/stores/use")):s.includes('../../stores/use'),'View delegates state and API through store: '+f)}
// API contract source of truth: packages/sdk/src/registry.ts.
// docs/ is optional and must not be required by pnpm install or deployment.
check(api.every(x=>typeof x.method==='string' && typeof x.path==='string' && !x.path.startsWith('/api/') && x.path.startsWith('/')),
  'Every browser route has a method and an absolute path without backend prefix');
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-sdk-'));
try{
 const srcDir=path.join(here,'packages/sdk/src');
 const typescriptCli=path.join(here,'node_modules','typescript','bin','tsc');
 const cliArgs=['--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(srcDir).filter(f=>f.endsWith('.ts')).map(f=>path.join(srcDir,f))];
 // 直接由 Node.js 执行 tsc.js：兼容 Windows，避免 spawn('tsc') 找不到 tsc.cmd。
 if(fs.existsSync(typescriptCli)) execFileSync(process.execPath,[typescriptCli,...cliArgs],{stdio:'pipe'});
 else execFileSync(process.platform==='win32'?'tsc.cmd':'tsc',cliArgs,{stdio:'pipe',shell:process.platform==='win32'});
 check(fs.existsSync(path.join(tmp,'client.js')),'SDK TypeScript compiled to JS without external packages');
 // Temporary compile output is CommonJS, outside package type=module.
 const {createRequire}=await import('node:module');const require=createRequire(import.meta.url);
 const {createApiClient}=require(path.join(tmp,'client.js'));
 const {categoryCountryCodes}=require(path.join(tmp,'catalog.js'));
 check(JSON.stringify(categoryCountryCodes({id:1,name:'catalog',country_codes:'["jp","KR","JP"]'}))==='["JP","KR"]','Country associations decode from MySQL JSON without duplicates');
 check(JSON.stringify(categoryCountryCodes({id:1,name:'catalog',country_codes:['JP','FR']}))==='["JP","FR"]','Multiple country associations are preserved');
 check(categoryCountryCodes({id:1,name:'catalog',country_codes:'invalid JSON'}).length===0,'Invalid country metadata never invents a country');
 check(categoryCountryCodes({id:1,name:'catalog',country_code:'cn'})[0]==='CN','Legacy single-country API responses remain compatible');
 const calls=[];const client=createApiClient(async r=>{calls.push(r);return {demandId:'example'}},{baseUrl:'https://api.example.test',getToken:()=> 'test-token'});
 await client.rpc('submit_pk_quote',{demand_id:'sample',price:20});
 check(calls[0].headers.Authorization==='Bearer test-token','Bearer header injected via SDK transport');
 check(calls[0].url==='https://api.example.test/rpc/submit_pk_quote'&&calls[0].method==='POST','Existing trade RPC path and method correct');
 const publicCalls=[];const pub=createApiClient(async r=>{publicCalls.push(r);return []},{baseUrl:'',getToken:()=>''});await pub.catalog.products(20);
 check(!('Authorization' in publicCalls[0].headers)&&publicCalls[0].url==='/public/products?limit=20','Public API uses no synthetic credentials');
 for(const rejection of ['ERR:INSUFFICIENT_BALANCE',false,{err:'NOT_OWNER'}]){
  const denied=createApiClient(async()=>rejection);
  let failed=false;try{await denied.rpc('award_pk',{})}catch(error){failed=error.message==='API_BUSINESS_REJECTED'}
  check(failed,'RPC business rejection cannot be reported as success: '+JSON.stringify(rejection));
 }
 const quoteCalls=[];const priced=createApiClient(async r=>{quoteCalls.push(r);return {pricingVersion:'escrow_v1',buyerEscrow:'1060.00'}},{getToken:()=> 'test-token'});
 const input={tripId:'trip',itemName:'Camera',quantity:1,unit:'piece',expectUnitPrice:1000,contactName:'Buyer',contactPhone:'13800138000',address:'Address',insuranceChoice:'yes'};
 await priced.originalV15.quoteIntent(input);
 await priced.originalV15.createIntent({...input,requestId:'b6d4beac-e1b8-471f-bc82-04e4a15441b4',expectedBuyerEscrow:'1060.00'});
 check(quoteCalls[0].url==='/me/intents/quote'&&quoteCalls[1].data.expectedBuyerEscrow==='1060.00'&&Boolean(quoteCalls[1].data.requestId),'Order confirmation transmits the quoted amount and retry key');
 const paymentCalls=[];const funding=createApiClient(async r=>{paymentCalls.push(r);return {}},{getToken:()=> 'test-token'});
 await funding.payments.quote({merchantId:'bank-usd',paymentCurrency:'USD',amountCny:'100.01'});
 await funding.payments.create({quoteId:'locked-quote',requestId:'b6d4beac-e1b8-471f-bc82-04e4a15441b4'});
 await funding.payments.status('payment-id');
 check(paymentCalls[0].url==='/me/payments/quotes'&&paymentCalls[0].data.amountCny==='100.01','Payment quote preserves exact decimal string and backend pricing');
 check(paymentCalls[1].url==='/me/payments'&&paymentCalls[1].data.quoteId==='locked-quote'&&Boolean(paymentCalls[1].data.requestId),'Payment creation uses locked quote and idempotency key');
 check(paymentCalls[2].method==='GET'&&paymentCalls[2].url==='/me/payments/payment-id'&&paymentCalls.every(r=>r.headers.Authorization==='Bearer test-token'),'Payment receipt checks remain authenticated and use server status');
 const {backendCapabilities}=require(path.join(tmp,'capabilities.js'));
 check(backendCapabilities.communityFeed&&backendCapabilities.communityPost&&backendCapabilities.productPublishing&&backendCapabilities.walletFunding,'Capabilities include implemented portal, product and payment controllers');
 check(!backendCapabilities.routeShare&&backendCapabilities.personalShops,'Unimplemented route sharing stays disabled; declared shop workflows are available');
 const productCalls=[];const seller=createApiClient(async r=>{productCalls.push(r);return {}},{getToken:()=> 'seller-token'});
 await seller.products.create({name:'Camera',region:'JP',price:'100.01',stock:2,imageUrls:[]});
 await seller.products.state('own-id','off');
 check(productCalls[0].url==='/me/products'&&productCalls[0].data.price==='100.01'&&!('ownerId'in productCalls[0].data),'Product publishing preserves CNY decimal strings and derives ownership on the server');
 check(productCalls[1].url==='/me/products/own-id/status'&&productCalls[1].method==='PUT'&&productCalls[1].data.status==='off','Product status changes use the owner-scoped endpoint');
 const {primaryNavigation,businessServices,flowIds}=require(path.join(tmp,'index.js'));
 check(primaryNavigation.map(x=>x.id).join(',')==='home,community,publish,messages,me','Five primary business areas have one shared catalogue');
 const registeredPages=new Set(JSON.parse(fs.readFileSync(path.join(here,'apps/mobile/src/pages.json'),'utf8')).pages.map(x=>x.path));
 check(businessServices.every(x=>registeredPages.has(x.mobile))&&new Set(businessServices.map(x=>x.id)).size===businessServices.length,'Every shared service has a unique registered mobile page');
 check(flowIds.every(id=>businessServices.some(item=>item.id===id)),'All 21 existing workflows are reachable from the shared service directory');
 const savedCalls=[];const saved=createApiClient(async r=>{savedCalls.push(r);return {}},{getToken:()=> 'owner-token'});
 await saved.saved.setCart('product-id',2);
 await saved.saved.favorite('product','product-id');
 await saved.saved.createAddress({contactName:'Recipient',contactPhone:'+8613800138000',detail:'Street',isDefault:true});
 check(savedCalls[0].method==='PUT'&&savedCalls[0].data.quantity===2&&savedCalls[1].url==='/me/favorites/product/product-id','Cart uses desired quantity and favorites use deterministic owner-scoped URLs');
 check(!('userId'in savedCalls[2].data)&&savedCalls.every(call=>call.headers.Authorization==='Bearer owner-token'),'Address ownership comes from the authenticated server session');
 const {squareTripMatches,squareDemandMatches,productCountryMatches}=require(path.join(tmp,'square.js'));
 check(squareTripMatches({destination:'Tokyo',trip_type:'cross_border',direction:'outbound'},'tokyo','international','outbound')&&!squareTripMatches({destination:'Tokyo',trip_type:'international',direction:'inbound'},'','international','outbound'),'Shared square filters agree on region aliases, search and direction');
 check(squareDemandMatches({title:'Camera',trip_type:'any'},'camera','domestic'),'A demand with unspecified region remains eligible in the shared square');
 check(productCountryMatches({category_id:2},[{id:2,parent_id:1,name:'Child'},{id:1,name:'Parent',country_codes:['JP','KR']}],'KR'),'Both square presentations preserve all ancestor country associations');
 check(!productCountryMatches({category_id:2},[{id:2,parent_id:2,name:'Cycle'}],'JP'),'Cyclic category ancestry does not invent a country or hang search');
 const {flowDefinitions}=require(path.join(tmp,'businessFlows.js'));
 const workflowRoutes=new Set(Object.values(flowDefinitions).flatMap(flow=>flow.operations.map(op=>op.path)));
 check(api.filter(op=>op.role==='user'&&op.access!=='public').every(op=>workflowRoutes.has(op.path)||op.path==='/rpc/quote_pk_award'),'Every core user operation has a shared business workflow or serves the escrow quote helper');
 const {tripReach,squareReaches}=require(path.join(tmp,'square.js'));
 check(squareReaches.length===6&&tripReach('same_city')==='city'&&tripReach('cross_border')==='international'&&tripReach('neighbor')==='neighbor','Desktop and mobile share all six reach filters and their stored aliases');
 const insurerCalls=[];const insured=createApiClient(async r=>{insurerCalls.push(r);return {}},{getToken:()=> 'buyer-token'});
 await insured.insurance.quote({productId:'cargo',subjectType:'trip',subjectId:'trip-id',itemName:'Camera',declaredValue:'1000.00'});
 await insured.insurance.apply({orderId:'order-id',quoteId:'insurance-quote',requestId:'b6d4beac-e1b8-471f-bc82-04e4a15441b4',termsAccepted:true,disclosuresAccepted:true});
 check(insurerCalls[0].url==='/me/insurance/quotes'&&insurerCalls[0].data.declaredValue==='1000.00','Insurance quote preserves declared CNY amount and binds the purchase subject');
 check(insurerCalls[1].data.termsAccepted===true&&insurerCalls[1].data.disclosuresAccepted===true&&!('premium'in insurerCalls[1].data),'Insurer application sends explicit consent and never caller-controlled premiums');
 const caseCalls=[];const cases=createApiClient(async r=>{caseCalls.push(r);return {}},{getToken:()=> 'party-token'});
 await cases.cases.create({orderId:'order',type:'damaged',description:'Damaged goods',expectResolution:'refund_part',refundAmount:'0.29',evidenceUrls:[],requestId:'request'});
 await cases.cases.respond('case/id',{note:'Evidence',evidenceUrls:[],expectedRound:2,requestId:'response'});
 await cases.cases.detail('case/id',true);await cases.cases.events('case/id',20,40,true);
 check(caseCalls[0].data.refundAmount==='0.29'&&!('actorId'in caseCalls[0].data),'Case creation preserves decimal strings and cannot set the authenticated actor');
 check(caseCalls[1].url==='/me/disputes/case%2Fid/respond'&&caseCalls[1].data.expectedRound===2,'Case response encodes IDs and carries the optimistic round');
 check(caseCalls[2].url==='/portal/admin/disputes/case%2Fid'&&caseCalls[3].url==='/portal/admin/disputes/case%2Fid/events?limit=20&offset=40','Admin case details and paginated history use protected routes');
 const integrationCalls=[];const integrations=createApiClient(async r=>{integrationCalls.push(r);return {}},{getToken:()=> 'admin-token'});
 await integrations.integrations.save('credentials','WECHAT_MAIN',{enabled:false,remark:'test',values:{APP_ID:'app'},clearFields:['SIGNING_SECRET']});
 await integrations.integrations.smsDeliveries();
 check(integrationCalls[0].method==='PUT'&&integrationCalls[0].url==='/portal/admin/integrations/credentials/WECHAT_MAIN'&&integrationCalls[0].data.clearFields[0]==='SIGNING_SECRET','Integration configuration uses protected vendor/product routes and explicit clearing');
 check(integrationCalls.every(r=>r.headers.Authorization==='Bearer admin-token')&&integrationCalls[1].url==='/portal/admin/integrations/sms-deliveries','Configuration and redacted delivery history retain authenticated access');
 const shopCalls=[];const shop=createApiClient(async r=>{shopCalls.push(r);return {}},{getToken:()=> 'owner-token'});
 await shop.shops.apply({businessName:'Example',creditCode:'91350211M000100Y46',representative:'Representative',licenseUploadId:'upload',channel:'web',requestId:'request'});await shop.shops.state('shop',{status:'paused',expectedStatus:'active',reason:'pause'});await shop.files.download('document/id');
 check(shopCalls[0].url==='/me/shop/applications'&&!('ownerId'in shopCalls[0].data),'Shop application derives ownership from authentication and references an owned upload');
 check(shopCalls[1].data.expectedStatus==='active'&&shopCalls[2].responseType==='arraybuffer'&&shopCalls[2].headers.Authorization==='Bearer owner-token','Shop transitions preserve expected status; private downloads stay authenticated and binary');
 const form=new FormData();await shop.files.upload(new Blob(['document']),'license.pdf','store_license');const upload=shopCalls.at(-1);check(upload.data instanceof FormData&&!('Content-Type'in upload.headers)&&upload.timeoutMs===45000,'Browser multipart upload preserves its boundary and has an explicit file-transfer timeout');
 const checkoutCalls=[];const checkout=createApiClient(async r=>{checkoutCalls.push(r);return {}},{getToken:()=> 'buyer-token'});
 await checkout.productOrders.trips('product/id',20,40);await checkout.productOrders.create({productId:'product',tripId:'trip',quantity:2,contactName:'Recipient',contactPhone:'+8613800138000',address:'Address',insuranceChoice:'waived',requestId:'request',expectedUnitPrice:'100.01',expectedBuyerEscrow:'210.02',expectedProductFingerprint:'a'.repeat(64)});
 check(checkoutCalls[0].url==='/public/products/product%2Fid/trips?limit=20&offset=40','Product checkout paginates only the backend eligible-trip endpoint');
 check(checkoutCalls[1].data.expectedProductFingerprint==='a'.repeat(64)&&checkoutCalls[1].data.expectedUnitPrice==='100.01'&&!('vendorId'in checkoutCalls[1].data),'Checkout confirms the immutable product fingerprint and server price without choosing a vendor identity');
 const refundsCalls=[];const refunds=createApiClient(async r=>{refundsCalls.push(r);return {}},{getToken:()=> 'admin-token'});await refunds.paymentRefunds.review('refund/id',{approve:true,reason:'Checked'});await refunds.paymentRefunds.dispatch('refund/id');await refunds.paymentRefunds.events('refund/id',20,40,true);
 check(refundsCalls[0].url==='/portal/admin/payment-refunds/refund%2Fid/review'&&refundsCalls[1].url.endsWith('/dispatch')&&refundsCalls[2].url.endsWith('/events?limit=20&offset=40'),'Refund approval, explicit dispatch and protected timeline remain distinct endpoints');
 const {subscribeSession,setBearer,clearBearer}=require(path.join(tmp,'index.js'));let transitions=0;const unsubscribe=subscribeSession(()=>{transitions++});setBearer('observer-token');setBearer('observer-token');clearBearer();unsubscribe();setBearer('after-unsubscribe');check(transitions===2,'Session observers receive only real transitions and can unsubscribe without exposing tokens');clearBearer();
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
console.log('CONTRACT STATIC CHECK PASSED');
