import fs from 'node:fs';import path from 'node:path';import os from 'node:os';import {execFileSync} from 'node:child_process';
const here=path.resolve(import.meta.dirname,'..');
const source=fs.readFileSync(path.join(here,'packages/sdk/src/registry.ts'),'utf8');
const json=source.split('= ')[1].replace(/;\s*$/,'');
const api=JSON.parse(json);
function check(v,msg){if(!v)throw Error('FAILED: '+msg);console.log('OK:',msg)}
check(api.length===69,'69 routes extracted from 9 Java Controller classes');
check(new Set(api.map(x=>x.method+' '+x.path)).size===api.length,'All method + path pairs unique');
check(api.filter(x=>x.access==='public').length===4,'Exactly 4 public endpoints');
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
 const calls=[];const client=createApiClient(async r=>{calls.push(r);return {demandId:'example'}},{baseUrl:'https://api.example.test',getToken:()=> 'test-token'});
 await client.rpc('submit_pk_quote',{demand_id:'sample',price:20});
 check(calls[0].headers.Authorization==='Bearer test-token','Bearer header injected via SDK transport');
 check(calls[0].url==='https://api.example.test/rpc/submit_pk_quote'&&calls[0].method==='POST','Existing trade RPC path and method correct');
 const publicCalls=[];const pub=createApiClient(async r=>{publicCalls.push(r);return []},{baseUrl:'',getToken:()=>''});await pub.catalog.products(20);
 check(!('Authorization' in publicCalls[0].headers)&&publicCalls[0].url==='/public/products?limit=20','Public API uses no synthetic credentials');
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
console.log('CONTRACT STATIC CHECK PASSED');
