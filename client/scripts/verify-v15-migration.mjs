import {renderedVueSources} from './rendered-vue-contract.mjs';
import {historicalPageLedger} from './historical-page-ledger.mjs';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import {execFileSync} from 'node:child_process';
import {createRequire} from 'node:module';
const root=path.resolve(import.meta.dirname,'..');
const read=name=>fs.readFileSync(path.join(root,name),'utf8');
const pages=JSON.parse(read('apps/mobile/src/pages.json')).pages;
const migrated=['login','create-intent','intent-detail','dispute-create','dispute-detail','invite-share','invite-tier','admin'];
for(const name of migrated){
  const route=`pages/${name}/index`;
  assert.equal(pages.filter(p=>p.path===route).length,1,`Route missing or duplicate ${route}`);
  const file=`apps/mobile/src/${route}.vue`;
  assert.ok(fs.existsSync(path.join(root,file)),'Missing '+file);
  const page=read(file);
  for(const marker of ['$t(','original-subpage','mobilePreferenceStyle'])assert.ok(page.includes(marker),`${name}: original style/i18n marker missing ${marker}`);
  assert.ok(!/mockData|demoData|seedRecords|Math\.random\(/i.test(page),`${name}: fixture data disguised as backend`);
}
const historical=historicalPageLedger(root);
for (const name of migrated) {
 const file=`apps/mobile/src/pages/${name}/index.vue`,page=read(file);
 if(['dispute-create','dispute-detail'].includes(name)){
  const graph=renderedVueSources(root,file);
  assert.ok(page.includes('TradeCasesMobile')&&graph.includes('useTradeCases(api'),`${name}: shared case model must be rendered`);
  const model=read('packages/ui/src/useTradeCases.ts');
  for(const call of ['api.cases.create','api.cases.detail','api.cases.respond','api.cases.appeal'])assert.ok(model.includes(call),'shared case operation missing: '+call);
  assert.ok(model.includes('subscribeSession')&&model.includes('sameSession()'),'shared case account guards missing');
 }else{
  const store=`apps/mobile/src/stores/useV15${name.split('-').map(part=>part[0].toUpperCase()+part.slice(1)).join('')}Page.ts`;
  assert.ok(fs.existsSync(path.join(root,store)),`${name}: composable absent`);
  assert.ok(page.includes('useV15'),`${name}: composable not wired`);
 }
}
if(historical)for(const name of migrated)assert.ok(historical.entries.some(row=>row.module===name&&row.target===`apps/mobile/src/pages/${name}/index.vue`),`Ledger not updated for ${name}`);
const zh=JSON.parse(read('packages/i18n/src/zh-CN.json')),en=JSON.parse(read('packages/i18n/src/en-US.json'));
assert.deepEqual(Object.keys(zh).sort(),Object.keys(en).sort());
for(const key of ['v15.login','v15.disputeUnavailable','v15.needFace','v15.intentPostUnavailable'])assert.ok(zh[key]&&en[key],key);
const me=read('apps/mobile/src/stores/useMineOriginalPage.ts');
for(const target of ["login:['login']","inviteShare:['invite-share']","admin:['admin']"])assert.ok(me.includes(target),`Mine navigation missing: ${target}`);
const link=read('apps/mobile/src/pages/trip-detail/index.vue');assert.ok(link.includes('/pages/create-intent/index?tripId='),'Trip->intent entry absent');
assert.ok(read('apps/mobile/src/pages/order-detail/index.vue').includes('/pages/dispute-create/index?id='),'Order->dispute entry absent');
const login=read('apps/mobile/src/stores/useV15LoginPage.ts');
assert.ok(read('apps/mobile/src/pages/login/index.vue').includes('needCode'),'Login code field must be rendered');
const authModel=read('packages/ui/src/useBusinessAuth.ts');
assert.ok(login.includes("from '@kuaixie/ui/useBusinessAuth'")&&login.includes('useBusinessAuth(api'),'native login must use the real shared authentication model');
const authReturn=authModel.slice(authModel.lastIndexOf('return {'));
for(const field of ['countryCodes','needCode','sendCode'])assert.ok(new RegExp('\\b'+field+'\\b').test(authReturn),'Shared login must expose code-login state: '+field);
assert.ok(authModel.includes("if(typeof token!=='string'||!/^"),'Only set session on JWT-shaped token');
assert.ok(authModel.indexOf('setBearer(token)')>authModel.indexOf('if(typeof token'), 'No premature login success');
assert.ok(read('apps/mobile/src/pages/invite-share/index.vue').includes('disabled>'),'Face check gate must remain fail-closed');
assert.ok(read('apps/mobile/src/stores/useV15InviteSharePage.ts').includes('operatorId:profile.value?.id'),'Server invite code needs verified operator id');
const config=read('packages/config/src/proxy.ts'),prod=read('deploy/nginx-gateway.example.conf');
for(const prefix of ['me','auth']){assert.ok(config.includes('support|me|auth'), 'H5 dev gateway missing '+prefix);assert.ok(prod.includes('support|me|auth'),'production gateway missing '+prefix)}
const cli=path.join(root,'node_modules/typescript/bin/tsc');
const temp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-v15-sdk-'));
try {
 const src=path.join(root,'packages/sdk/src');
 const args=['--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',temp,...fs.readdirSync(src).filter(x=>x.endsWith('.ts')).map(x=>path.join(src,x))];
 if(fs.existsSync(cli))execFileSync(process.execPath,[cli,...args],{stdio:'pipe'});else execFileSync('tsc',args,{stdio:'pipe'});
 const sdk=createRequire(import.meta.url)(path.join(temp,'client.js'));
 const calls=[];const c=sdk.createApiClient(async r=>{calls.push(r);return {id:'sample'}},{baseUrl:'https://api.example.test',getToken:()=> 'verified-test-token'});
 await c.originalV15.login({channel:'email',contact:'test@example.test',password:'redacted'});
 await c.originalV15.createIntent({tripId:'test',itemName:'X',quantity:1,unit:'piece',expectUnitPrice:4,contactName:'X',contactPhone:'1234567',address:'X',insuranceChoice:'yes'});
 await c.originalV15.dispute('some-id');
 assert.equal(calls[0].url,'https://api.example.test/auth/login');assert.equal(calls[0].method,'POST');
 assert.equal(calls[1].url,'https://api.example.test/me/intents');assert.equal(calls[1].headers.Authorization,'Bearer verified-test-token');
 assert.equal(calls[2].url,'https://api.example.test/me/disputes/some-id');
 assert.equal(calls[2].method,'GET');
} finally {fs.rmSync(temp,{recursive:true,force:true})}
console.log('V15 PAGE + SDK CONTRACT REGRESSION PASSED: 8 routes, external historical-ledger status reported separately, no mock success, shared locales, login/token guard, proxy and authenticated API requests.');
