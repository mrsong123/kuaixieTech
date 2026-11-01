import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/admin/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-contact-health-'));
let assertions=0;function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
const deferred=()=>{let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {promise,resolve,reject}};
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.t=key=>key;exports.uiErrorMessage=e=>e.message;`);
 let code=ts.transpileModule(fs.readFileSync(path.join(root,'apps/admin/src/components/useContactDeliveryHealth.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 const file=path.join(tmp,'health.cjs');fs.writeFileSync(file,code);const {useContactDeliveryHealth,checkedDeliveryHealth}=require(file),{createApiClient,ApiError,setBearer,getBearer}=require(path.join(tmp,'index.js'));
 const diag=(channel,state='ready')=>({channel,state,stage:'configuration',reasonCode:state==='ready'?'CONTACT_PROVIDER_CONFIGURED':'CONTACT_NOT_CHECKED',exceptionType:null,sqlState:null,databaseError:null,missingTable:null,observedAt:'2026-11-01T00:00:00Z'});
 const receipt=()=>({checkedAt:'2026-11-01T00:00:00Z',state:'ready',dispatchConfigured:true,providerDeliveryVerified:false,configuration:{sms:diag('sms'),email:diag('email','idle')},lastRun:{checkedAt:null,state:'not_checked',sms:diag('sms','not_checked'),email:diag('email','not_checked')},queue:[]});
 function fixture(handler){const calls=[];return {calls,api:createApiClient(async request=>{calls.push(request);return handler(request)},{getToken:getBearer})}}
 setBearer('admin-A');
 {
  const f=fixture(()=>receipt());await mounted(()=>useContactDeliveryHealth(f.api),async p=>{await p.refresh();check(p.health.value?.configuration.sms.state==='ready'&&p.health.value.providerDeliveryVerified===false,'Configuration readiness is displayed without claiming delivery verification');check(f.calls.length===1&&f.calls[0].method==='GET'&&f.calls[0].url==='/portal/admin/contact-delivery/health','Refresh performs only the protected read-only health request');check(!p.loading.value&&!p.error.value,'A verified diagnostic response completes the loading state');});
 }
 {
  const response=receipt();response.state='requires_reconciliation';response.queue=[{channel:'phone',status:'blocked',total:'2'}];const f=fixture(()=>response);await mounted(()=>useContactDeliveryHealth(f.api),async p=>{await p.refresh();check(p.blocked.value===2&&p.health.value.state==='requires_reconciliation','Blocked records preserve the reconciliation state and numeric count');check(f.calls.every(row=>row.method==='GET'),'Inspecting blocked attempts never sends or replays a message');});
 }
 {
  const response=receipt();response.state='degraded';response.dispatchConfigured=false;response.configuration.sms={...diag('sms','migration_required'),reasonCode:'CONTACT_SCHEMA_MISSING',sqlState:'42S02',databaseError:1146,missingTable:'t_integration_runtime_state'};
  response.configuration.email=diag('email','paused');const parsed=checkedDeliveryHealth(response);check(parsed.configuration.sms.missingTable==='t_integration_runtime_state'&&parsed.configuration.sms.sqlState==='42S02','Missing schema diagnostics retain actionable safe metadata');
  response.configuration.sms.secret='must-not-survive';check(!JSON.stringify(checkedDeliveryHealth(response)).includes('must-not-survive'),'Undeclared diagnostic fields are discarded instead of retained or dumped');
 }
 {
  const response=receipt();response.dispatchConfigured=false;check(assert.throws(()=>checkedDeliveryHealth(response))===undefined,'Contradictory ready responses are rejected');
  response.dispatchConfigured=true;response.configuration.sms.sqlState='password-value';check(assert.throws(()=>checkedDeliveryHealth(response))===undefined,'Invalid diagnostic metadata is rejected before rendering');
 }
 {
  let fail=false;const f=fixture(()=>{if(fail)throw new ApiError('API_HTTP_503',503);return receipt()});await mounted(()=>useContactDeliveryHealth(f.api),async p=>{await p.refresh();fail=true;await p.refresh();check(!p.health.value&&p.error.value==='API_HTTP_503'&&!p.loading.value,'A failed refresh clears the old ready state and reports an error');});
 }
 {
  const first=deferred(),second=deferred();let n=0;const f=fixture(()=>++n===1?first.promise:second.promise);await mounted(()=>useContactDeliveryHealth(f.api),async p=>{const a=p.refresh(),b=p.refresh();const newest=receipt();newest.state='paused';newest.dispatchConfigured=false;newest.configuration.sms=diag('sms','paused');second.resolve(newest);await b;first.resolve(receipt());await a;check(p.health.value?.state==='paused','A late refresh cannot replace a newer diagnosis with a stale ready result');});
 }
 {
  const reply=deferred(),f=fixture(()=>reply.promise);await mounted(()=>useContactDeliveryHealth(f.api),async p=>{const request=p.refresh();setBearer('admin-B');check(!p.health.value&&!p.loading.value&&p.error.value==='contactHealth.sessionChanged','Changing accounts immediately clears diagnostics and invalidates the old panel');reply.resolve(receipt());await request;await p.refresh();check(!p.health.value&&f.calls.length===1,'Old account responses and refresh actions are rejected after the switch');setBearer('admin-A');await p.refresh();check(f.calls.length===1,'Returning to the original token does not revive an invalidated panel');});
 }
 {
  const reply=deferred(),f=fixture(()=>reply.promise);let model,pending;await mounted(()=>useContactDeliveryHealth(f.api),async p=>{model=p;pending=p.refresh()});reply.resolve(receipt());await pending;check(!model.health.value&&!model.loading.value,'Disposed views cannot receive late configuration results');
 }
 const {parse,compileScript,compileTemplate}=createRequire(path.join(root,'apps/admin/package.json'))('@vue/compiler-sfc');const source=fs.readFileSync(path.join(root,'apps/admin/src/components/ContactDeliveryHealthPanel.vue'),'utf8');const descriptor=parse(source).descriptor;compileScript(descriptor,{id:'contact-health-test'});check(compileTemplate({source:descriptor.template.content,filename:'ContactDeliveryHealthPanel.vue',id:'contact-health-test'}).errors.length===0,'The diagnostic panel script and template compile');
 console.log(`CONTACT DELIVERY HEALTH CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
