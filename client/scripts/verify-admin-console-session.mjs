import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';

const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const adminRequire=createRequire(path.join(root,'apps/admin/package.json')),vuePath=adminRequire.resolve('vue'),vue=require(vuePath);
const {parse,compileScript,compileTemplate}=adminRequire('@vue/compiler-sfc');
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-admin-console-'));
const transpile=source=>ts.transpileModule(source,{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
let assertions=0;
function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
const deferred=()=>{let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {promise,resolve,reject}};
const tick=async()=>{await vue.nextTick();await Promise.resolve();await Promise.resolve();await vue.nextTick()};
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
try{
 const sdk=path.join(tmp,'session.cjs');fs.writeFileSync(sdk,transpile(fs.readFileSync(path.join(root,'packages/sdk/src/session.ts'),'utf8')));
 const session=require(sdk);session.setBearer('admin-A');
 const contextFile=path.join(tmp,'context.cjs');fs.writeFileSync(contextFile,'exports.useAdminContext=()=>exports.current;');
 const context=require(contextFile);
 const locale=path.join(tmp,'locale.cjs');fs.writeFileSync(locale,'exports.t=key=>key;exports.uiErrorMessage=e=>e.message;');
 const tabs=path.join(tmp,'tabs.cjs');fs.writeFileSync(tabs,transpile(fs.readFileSync(path.join(root,'apps/admin/src/console/tabs.ts'),'utf8')));
 const stub=path.join(tmp,'panel.cjs');fs.writeFileSync(stub,'exports.default={};');
 const source=fs.readFileSync(path.join(root,'apps/admin/src/views/ConsoleView.vue'),'utf8'),descriptor=parse(source).descriptor;
 const compiled=compileScript(descriptor,{id:'admin-console-test'});let code=transpile(compiled.content);
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',sdk],['@kuaixie/i18n',locale],['../console/tabs',tabs],['../stores/context',contextFile],['../components/SupportAgentPanel.vue',stub],['../components/PortalModerationPanel.vue',stub]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 const componentFile=path.join(tmp,'console.cjs');fs.writeFileSync(componentFile,code);const Console=require(componentFile).default;
 function fixture(handler,{section='orders'}={}){
  const calls=[],scope=vue.ref(1),state={section:vue.ref(section),sessionScope:scope,stats:vue.ref({}),statsError:vue.ref(''),busy:vue.ref(false),statsCalls:0,loadStats:async()=>{state.statsCalls++},api:{rpc:async(name,body)=>{calls.push({name,body:structuredClone(body)});return handler(name,body,calls.length)}}};
  const unsubscribe=session.subscribeSession(()=>{scope.value++});context.current=state;let model;
  const app=renderer.createApp({setup(){model=Console.setup({}, {expose(){}});return()=>null}});app.mount({});
  return {calls,state,model,dispose(){app.unmount();unsubscribe()}};
 }
 async function using(handler,body,options){const f=fixture(handler,options);try{await tick();await body(f)}finally{f.dispose()}}
 function page(body,total=165){return Array.from({length:Math.max(0,Math.min(body.limit,total-body.offset))},(_,i)=>({id:String(body.offset+i),order_no:'ORDER-'+(body.offset+i)}))}
 await using((_name,body)=>page(body),async({model:m,calls})=>{
  check(calls.length===1&&calls[0].body.limit===81&&calls[0].body.offset===0,'First page requests only 80 rows plus one server-side continuation sentinel');
  check(m.rows.value.length===80&&m.hasNextPage.value&&m.pageNumber.value===1,'The sentinel enables Next without exposing an extra row');
  await m.changePage(1);check(calls[1].body.offset===80&&m.rows.value[0].id==='80'&&m.pageNumber.value===2,'Next requests the second page from the server');
  await m.changePage(1);check(calls[2].body.offset===160&&m.rows.value.length===5&&!m.hasNextPage.value,'The final partial page disables Next');
  await m.changePage(1);check(calls.length===3,'Disabled Next never requests an empty extra page');
  await m.changePage(-1);await m.changePage(-1);check(calls.at(-1).body.offset===0&&m.rows.value[0].id==='0','Previous requests the earlier page from the server');
  await m.changePage(-1);check(calls.length===5,'Previous cannot request a negative offset');
 });
 {
  const late=deferred();await using((_name,body)=>body.offset===80?late.promise:page(body),async({model:m,state,calls})=>{
   const old=m.changePage(1);state.section.value='users';await tick();check(calls.at(-1).body.kind==='users'&&calls.at(-1).body.offset===0,'Changing sections resets the server offset');
   late.resolve([{id:'old-orders'}]);await old;check(m.rows.value[0].id==='0'&&m.pageOffset.value===0,'A late previous-section page cannot overwrite the new section');
  });
 }
 {
  let oldFilter;await using((_name,body)=>body.keyword==='first'?(oldFilter=deferred()).promise:page(body),async({model:m,calls})=>{
   await m.changePage(1);m.keyword.value='first';check(m.rows.value.length===0&&m.pageOffset.value===0&&m.filterPending.value,'Editing a user filter immediately clears the old page and resets the offset');
   const count=calls.length;await tick();check(calls.length===count,'Typing does not submit a request before the explicit Search action');
   const old=m.refresh();m.keyword.value='second';await m.refresh();check(calls.at(-1).body.keyword==='second'&&calls.at(-1).body.offset===0,'Submitted filters are forwarded with a fresh first-page request');
   oldFilter.resolve([{id:'old-filter'}]);await old;check(m.rows.value[0].id==='0'&&!m.filterPending.value,'A late previous-filter response cannot refill the table');
  },{section:'users'});
 }
 await using((_name,body)=>{if(body.offset===80)throw new Error('page failed');return page(body)},async({model:m})=>{
  await m.changePage(1);check(m.rows.value.length===0&&m.loadError.value==='page failed'&&!m.hasNextPage.value&&!m.loading.value,'A failed page clears old records and continuation state');
  await m.changePage(-1);check(m.rows.value[0].id==='0'&&!m.loadError.value,'Previous can recover from a failed page');
 });
 {
  const late=deferred();await using((_name,body)=>body.offset===80?late.promise:page(body),async({model:m})=>{
   const old=m.changePage(1);m.evidenceText.value='private';session.setBearer('admin-B');check(!m.rows.value.length&&m.pageOffset.value===0&&!m.evidenceText.value,'Changing accounts immediately clears page and private evidence');
   session.setBearer('admin-A');late.resolve([{id:'old-account'}]);await old;check(!m.rows.value.length&&m.pageOffset.value===0,'An A to B to A token transition cannot revive a stale page');
  });
 }
 {
  const late=deferred();await using((name,body)=>name==='get_order_evidence'&&body.orderId==='first'?late.promise:name==='get_order_evidence'?{id:'new-evidence'}:page(body),async({model:m})=>{
   const old=m.showEvidence('first');await m.showEvidence('second');late.resolve({id:'old-evidence'});await old;check(m.evidenceText.value.includes('new-evidence')&&!m.evidenceText.value.includes('old-evidence'),'Evidence responses are bound to the latest selection');
  });
 }
 {
  const late=deferred();await using((_name,body)=>page(body),async({model:m,state,calls})=>{
   let writes=0;const first=m.run(()=>{writes++;return late.promise}),second=m.run(async()=>{writes++;return 'OK'});await second;check(writes===1&&m.mutationBusy.value,'A second click cannot execute a duplicate mutation');
   session.setBearer('admin-B');const before=calls.length;late.resolve('OK');await first;check(!m.notice.value&&calls.length===before&&state.statsCalls===0&&!m.mutationBusy.value,'An old-account mutation cannot announce success or refresh another account');session.setBearer('admin-A');
  });
 }
 await using(()=>{throw new Error('must not load generic list')},async({model:m,state,calls})=>{check(!m.paged.value&&calls.length===0&&state.statsCalls===1,'Overview keeps aggregate loading and does not call the paged list')},{section:'overview'});
 for(const section of ['violations','distribution'])await using(name=>name==='admin_invite_tier_distribution'?[]:[],async({model:m,calls})=>{check(!m.paged.value&&calls.every(x=>x.name!=='admin_console_list'),'The '+section+' specialized view does not pretend to support generic pagination')},{section});
 {
  const late=deferred(),f=fixture(()=>late.promise),m=f.model;f.dispose();late.resolve([{id:'disposed'}]);await tick();check(!m.rows.value.length&&!m.loading.value,'Disposed views reject pending page responses');
 }
 check(compileTemplate({source:descriptor.template.content,filename:'ConsoleView.vue',id:'admin-console-test',compilerOptions:{bindingMetadata:compiled.bindings}}).errors.length===0,'The actual Console template compiles with pagination controls');
 check(descriptor.template.content.includes(':disabled="mutationBusy"')&&descriptor.template.content.includes('admin.pagination.specializedLimit'),'Mutation fieldset protection and the specialized-view limit notice remain visible');
 console.log(`ADMIN CONSOLE SESSION/PAGING CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
