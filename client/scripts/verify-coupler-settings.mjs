import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vuePath=createRequire(path.join(root,'apps/pc/package.json')).resolve('vue'),vue=require(vuePath),tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-coupler-'));
let assertions=0;function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
const renderer=vue.createRenderer({createElement:()=>({}),createText:()=>({}),createComment:()=>({}),setText(){},setElementText(){},parentNode:()=>null,nextSibling:()=>null,insert(){},remove(){},patchProp(){}});
async function mounted(factory,test){let model;const app=renderer.createApp({setup(){model=factory();return()=>null}});app.mount({});try{await test(model)}finally{app.unmount()}}
try{
 const sdk=path.join(root,'packages/sdk/src');execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',tmp,...fs.readdirSync(sdk).filter(x=>x.endsWith('.ts')).map(x=>path.join(sdk,x))],{stdio:'pipe'});
 fs.writeFileSync(path.join(tmp,'locale.cjs'),`exports.t=(key,values)=>key+(values?JSON.stringify(values):'');exports.uiErrorMessage=e=>e.message;`);
 let code=ts.transpileModule(fs.readFileSync(path.join(root,'packages/ui/src/useCouplerSettings.ts'),'utf8'),{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['vue',vuePath],['@kuaixie/sdk',path.join(tmp,'index.js')],['@kuaixie/i18n',path.join(tmp,'locale.cjs')]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);
 const file=path.join(tmp,'coupler.cjs');fs.writeFileSync(file,code);const {useCouplerSettings}=require(file),{createApiClient,ApiError,setBearer,clearBearer}=require(path.join(tmp,'index.js'));
 function configuration(mode){return {revision:mode==='SANDBOX'?1:7,enabled:false,selected:false,values:{region:mode==='SANDBOX'?'sandbox-region':'live-region'},configuredFields:['api_key','region'],unreadableFields:[],updatedAt:'2026-11-01T00:00:00Z'}}
 function provider(code){return {vendor:code,product:'sms',code,name:code,capability:'business_protocol',docs:'https://example.invalid/docs',sdkAvailable:true,businessVerified:false,consumerWired:true,supportedModes:['SANDBOX','LIVE'],implementedOperations:['send_verification_code'],fields:[{key:'api_key',label:'API key',secret:true,required:true},{key:'region',label:'Region',secret:false,required:true}],configurationByMode:{SANDBOX:configuration('SANDBOX'),LIVE:configuration('LIVE')}}}
 function fixture(options={}){const data={runtime:{mode:'SANDBOX',revision:3},encryptionReady:true,categories:[{code:'SMS',name:'SMS',description:'Text messages',providers:[provider('aliyun'),provider('tencent')]},{code:'EMAIL',name:'Email',description:'Email',providers:[provider('smtp')]}]},calls=[];
  const api=createApiClient(async r=>{calls.push({...r,data:structuredClone(r.data)});if(r.method==='GET')return structuredClone(data);if(r.url.endsWith('/runtime')){if(options.runtimeError)throw new ApiError('runtime error',options.runtimeError);data.runtime={mode:r.data.mode,revision:data.runtime.revision+1};return {...data.runtime}}
   const match=r.url.match(/integration-center\/([^/]+)\/([^/]+)\/(SANDBOX|LIVE)(\/validate)?$/);assert.ok(match);const [,categoryCode,providerCode,mode,validation]=match,category=data.categories.find(c=>c.code===decodeURIComponent(categoryCode)),p=category.providers.find(p=>p.code===decodeURIComponent(providerCode)),current=p.configurationByMode[mode];
   if(validation)return {mode:options.wrongValidationMode?'LIVE':mode,revision:current.revision,ready:true,missingFields:[],invalidFields:[],unreadableFields:[],sdkAvailable:p.sdkAvailable,businessVerified:false,checks:[{code:'LOCAL_ONLY',status:'info',message:'Local only'}]};
   if(options.saveError!==undefined)throw new ApiError('save error',options.saveError);if(options.malformedReceipt)return {};
   assert.equal(r.data.expectedRevision,current.revision);const revision=current.revision+1;for(const candidate of category.providers){candidate.configurationByMode[mode].revision=revision;if(r.data.selected)candidate.configurationByMode[mode].selected=candidate.code===providerCode;}
   current.enabled=r.data.enabled;current.selected=r.data.selected;for(const [key,value] of Object.entries(r.data.values)){if(!current.configuredFields.includes(key))current.configuredFields.push(key);if(!p.fields.find(f=>f.key===key).secret)current.values[key]=value;}
   for(const key of r.data.clearFields){current.configuredFields=current.configuredFields.filter(name=>name!==key);delete current.values[key];}
   return {revision};
  },{getToken:()=> 'unit-admin'});return {api,data,calls};
 }
 {
  const f=fixture();f.data.categories[0].providers[0].configurationByMode.SANDBOX.values.api_key='never-prefill-a-returned-secret';
  await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();check(p.draft.values.api_key===''&&p.draft.values.region==='sandbox-region','Secret fields are never prefilled from a catalog response');await p.chooseMode('LIVE');check(p.configuration.value.revision===7&&p.draft.values.region==='live-region'&&p.draft.values.api_key==='','LIVE editor reads its independent values and revision');check(p.settings.value.runtime.mode==='SANDBOX'&&!f.calls.some(r=>r.method==='PUT'),'Changing editor mode does not change runtime mode');});
 }
 {
  const f=fixture();await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.draft.values.region='new-sandbox';p.draft.selected=true;await p.save();const saved=f.calls.find(r=>r.method==='PUT');check(saved.url==='/portal/admin/integration-center/SMS/aliyun/SANDBOX'&&saved.data.expectedRevision===1,'Provider writes are scoped by category, provider, mode and expected revision');check(!('api_key'in saved.data.values)&&saved.data.clearFields.length===0,'Blank secret input preserves saved credentials instead of erasing them');check(p.configuration.value.revision===2&&p.configuration.value.selected&&p.notice.value==='coupler.saved','A provider save is confirmed only after its revision and selection are reloaded');check(f.data.categories[0].providers[0].configurationByMode.LIVE.values.region==='live-region','Saving SANDBOX does not copy parameters into LIVE');});
 }
 {
  const f=fixture();await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.draft.clearFields.api_key=true;await p.save();const saved=f.calls.find(r=>r.method==='PUT');check(saved.data.clearFields[0]==='api_key'&&!('api_key'in saved.data.values)&&p.draftMissing.value.includes('api_key'),'Secret deletion requires an explicit clear field and removes its configured marker');});
 }
 {
  const f=fixture();await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();await p.validate();check(p.validation.value.ready&&!p.validation.value.businessVerified,'Local readiness never fabricates completed business acceptance');check(f.calls.every(r=>r.method==='GET'||r.url.endsWith('/validate'))&&f.calls.at(-1).method==='POST','Validation only calls the local readiness endpoint, not a provider operation');p.draft.values.region='edited';await p.validate();check(f.calls.filter(r=>r.url.endsWith('/validate')).length===1&&p.error.value==='coupler.saveBeforeValidate','Unsaved drafts cannot be presented as validation of stored configuration');});
 }
 {
  const f=fixture();await mounted(()=>useCouplerSettings(f.api,async()=>false),async p=>{await p.load();p.draft.values.api_key='typed-only';await p.chooseMode('LIVE');check(p.mode.value==='SANDBOX'&&p.draft.values.api_key==='typed-only','Declining discard retains the original environment and secret draft');});
 }
 {
  const f=fixture();await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.draft.values.api_key='sandbox-only';await p.chooseMode('LIVE');check(p.mode.value==='LIVE'&&p.draft.values.api_key===''&&p.draft.values.region==='live-region','Accepting environment switch discards the old secret and never carries it into LIVE');});
 }
 {
  const f=fixture({saveError:409});await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.draft.values.api_key='typed-secret';await p.save();await p.save();check(p.needsRefresh.value&&p.draft.values.api_key==='typed-secret'&&f.calls.filter(r=>r.method==='PUT').length===1,'Revision conflicts preserve the draft but block blind resubmission');await p.chooseMode('LIVE');await p.save();check(p.needsRefresh.value&&f.calls.filter(r=>r.method==='PUT').length===1,'Switching editors cannot bypass the required server refresh after a conflict');await p.load();check(!p.needsRefresh.value&&p.draft.values.api_key==='','Fresh server configuration clears stale state without returning saved secrets');});
 }
 {
  const f=fixture({saveError:0});await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.draft.values.region='new';await p.save();await p.save();check(p.needsRefresh.value&&!p.notice.value&&f.calls.filter(r=>r.method==='PUT').length===1,'An ambiguous save is not reported as success or automatically repeated');});
 }
 {
  const f=fixture();await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.runtimeTarget.value='LIVE';await p.changeRuntime();const write=f.calls.find(r=>r.method==='PUT');check(write.url==='/portal/admin/integration-center/runtime'&&write.data.expectedRevision===3&&p.settings.value.runtime.mode==='LIVE','Runtime mode switch uses its separate optimistic revision');check(p.mode.value==='SANDBOX'&&p.draft.values.region==='sandbox-region','Runtime switching does not silently change the editor or its environment values');});
 }
 {
  const f=fixture();await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.draft.values.region='draft';p.runtimeTarget.value='LIVE';await p.changeRuntime();check(!f.calls.some(r=>r.method==='PUT')&&p.error.value==='coupler.saveBeforeRuntime','Unsaved provider edits must be resolved before a global runtime switch');});
 }
 {
  const f=fixture({malformedReceipt:true});await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();p.draft.values.region='new';await p.save();check(!p.notice.value&&p.needsRefresh.value,'A malformed successful save response does not prove configuration was saved');});
 }
 {
  const f=fixture({wrongValidationMode:true});await mounted(()=>useCouplerSettings(f.api,async()=>true),async p=>{await p.load();await p.validate();check(!p.validation.value&&!!p.error.value,'A validation report for another mode cannot appear as current readiness');});
 }
 {
  const f=fixture();setBearer('admin-session-A');let decide;const dialog=new Promise(resolve=>{decide=resolve});try{await mounted(()=>useCouplerSettings(f.api,async()=>dialog),async p=>{await p.load();p.draft.values.api_key='typed-secret';const saving=p.save();setBearer('admin-session-B');decide(true);await saving;check(!f.calls.some(r=>r.method==='PUT')&&!p.settings.value&&Object.keys(p.draft.values).length===0,'A stale admin confirmation cannot save secrets under another session');});}finally{clearBearer()}
 }
 console.log(`COUPLER SETTINGS MODEL CHECK PASSED: ${assertions} assertions`);
}finally{fs.rmSync(tmp,{recursive:true,force:true})}
