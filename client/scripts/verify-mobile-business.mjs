import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const vue=createRequire(path.join(root,'apps/pc/package.json'))('vue');
const mobile=path.join(root,'apps/mobile/src');let count=0;
function check(value,note){assert.ok(value,note);count++;console.log('OK:',note)}
let hooks={load:[],show:[]};const cache=new Map(),api={account:{},payments:{},insurance:{}};
const prefs={preferenceRef:vue.ref({})},i18n={t:key=>key,formatAmount:String,formatDate:String,formatBusinessStatus:String,getLocale:()=> 'zh-CN',setLocale(){},uiErrorMessage:e=>e.message};
const nav=[];globalThis.uni={navigateTo:({url})=>nav.push(url),switchTab:({url})=>nav.push(url),showToast(){},setStorageSync(){},setTabBarItem(){}};
function evaluate(source,file){const module={exports:{}};const js=ts.transpileModule(source,{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;new Function('require','module','exports',js)(name=>{
 if(name==='vue')return {...vue,onMounted:()=>{}};
 if(name==='@dcloudio/uni-app')return {onLoad:f=>hooks.load.push(f),onShow:f=>hooks.show.push(f)};
 if(name==='@kuaixie/sdk')return {...session,primaryNavigation:[]};
 if(name==='@kuaixie/i18n')return i18n;
 if(name==='@kuaixie/preferences/vue')return prefs;
 if(name.endsWith('/api/client'))return {api};
 if(name.endsWith('/utils/prototype'))return {mobilePreferenceStyle:()=>''};
 if(name.startsWith('.'))return load(path.resolve(path.dirname(file),name+'.ts'));
 throw new Error('Unexpected import '+name);
 },module,module.exports);return module.exports;}
const session=evaluate(fs.readFileSync(path.join(root,'packages/sdk/src/session.ts'),'utf8'),path.join(root,'packages/sdk/src/session.ts'));
function load(file){if(!cache.has(file))cache.set(file,evaluate(fs.readFileSync(file,'utf8'),file));return cache.get(file)}
function mounted(factory){hooks={load:[],show:[]};const scope=vue.effectScope();const model=scope.run(factory);return {model,hooks,dispose:()=>scope.stop()}}
function page(file){return mounted(()=>evaluate(fs.readFileSync(file,'utf8').match(/<script setup[^>]*>([\s\S]*?)<\/script>/)[1]+'\nexports.snapshot=()=>({url:url.value,error:error.value});',file))}
const flush=async()=>{await vue.nextTick();await new Promise(resolve=>setTimeout(resolve,0))};
const deferred=()=>{let resolve;const promise=new Promise(r=>{resolve=r});return {promise,resolve}};
const {useMineOriginalPage}=load(path.join(mobile,'stores/useMineOriginalPage.ts'));
const {consumePublishEntry}=load(path.join(mobile,'utils/publishEntry.ts'));
const {useOrderSummary}=load(path.join(mobile,'stores/useOrderSummary.ts'));
const {useV15IntentDetailPage}=load(path.join(mobile,'stores/useV15IntentDetailPage.ts'));
const {useWalletSummary}=load(path.join(mobile,'stores/useWalletSummary.ts'));
const {useAccountFeeds}=load(path.join(mobile,'stores/useAccountFeeds.ts'));
const {privateWebTarget}=load(path.join(mobile,'utils/usePrivatePageSession.ts'));
session.setBearer('one');
{
 const p=mounted(useMineOriginalPage);for(const id of ['myDemands','shop','insurance','disputes','refunds','services','inviteTier'])p.model.select(id);
 const routes=new Set(JSON.parse(fs.readFileSync(path.join(mobile,'pages.json'),'utf8')).pages.map(p=>'/'+p.path));
 check(nav.length===7&&nav.every(url=>routes.has(url)),'All synchronized account/business entries resolve to registered native pages');
 p.model.select('publishTrip');check(consumePublishEntry()?.mode==='trip','My page trip publishing selects the trip composer');p.model.select('publishDemand');check(consumePublishEntry()?.mode==='demand','My page demand publishing selects the demand composer');p.dispose();
}
{
 const old=deferred();api.account.profile=()=>old.promise;api.account.wallet=async()=>({balance_amount:'50'});api.account.notifications=async()=>[];
 const p=mounted(useMineOriginalPage),request=p.model.reload();session.setBearer('two');old.resolve({nickname:'old-private-name'});await request;
 check(p.model.profile.value===null&&p.model.wallet.value===null&&p.model.unread.value===null,'A late old-account profile cannot restore identity or wallet data');
 api.account.profile=async()=>({nickname:'new-name'});await p.model.reload();check(p.model.profile.value?.nickname==='new-name','The current account can reload its own profile');session.clearBearer();check(!p.model.signedIn.value&&p.model.profile.value===null,'Logout immediately updates guest state and clears private profile data');p.dispose();
}
{
 session.setBearer('one');const old=deferred();api.account.order=()=>old.promise;const p=mounted(useOrderSummary);p.hooks.load[0]({id:'old-order'});session.setBearer('two');session.setBearer('one');old.resolve({id:'old-order',item_name:'private'});await flush();check(p.model.detail.value===null,'Order details reject a response across an account switch even if the old token returns');
 api.account.order=async()=>({id:'old-order',item_name:'current'});await p.model.load();check(p.model.detail.value?.item_name==='current','The current order detail can be explicitly reloaded');session.clearBearer();check(p.model.detail.value===null,'Logout removes the current order and its private image/insurance children');p.dispose();
}
{
 session.setBearer('one');const old=deferred();api.account.order=()=>old.promise;const p=mounted(useV15IntentDetailPage);p.hooks.load[0]({id:'intent'});session.setBearer('two');old.resolve({id:'intent',item_name:'private'});await flush();check(p.model.order.value===null,'An old intent response cannot refill another account page');p.dispose();
}
{
 session.setBearer('one');const old=deferred();api.account.wallet=()=>old.promise;const p=mounted(useWalletSummary);const request=p.model.load();session.setBearer('two');old.resolve({balance_amount:'999'});await request;check(p.model.wallet.value===null&&p.model.scope.value===1,'Old wallet totals are discarded and the native funding child is rebuilt');p.dispose();
}
{
 session.setBearer('one');const old=deferred();api.payments.status=()=>old.promise;const p=page(path.join(mobile,'pages/payment-checkout/index.vue'));const request=p.hooks.load[0]({id:'payment'});session.setBearer('two');old.resolve({id:'payment',status:'pending',checkout:{kind:'redirect',value:'https://pay.example/private'}});await request;check(!p.model.snapshot().url,'An old payment response cannot open a checkout web-view for the new account');p.dispose();
}
{
 session.setBearer('one');api.payments.status=async()=>({id:'payment',status:'pending',checkout:{kind:'redirect',value:'https://pay.example/private'}});const p=page(path.join(mobile,'pages/payment-checkout/index.vue'));await p.hooks.load[0]({id:'payment'});check(p.model.snapshot().url==='https://pay.example/private','A current matching payment can open its authorized HTTPS checkout');session.clearBearer();check(!p.model.snapshot().url,'Logout immediately removes an already displayed payment web-view');p.dispose();
}
{
 session.setBearer('one');const old=deferred();api.insurance.status=()=>old.promise;const p=page(path.join(mobile,'pages/insurance-document/index.vue'));const request=p.hooks.load[0]({kind:'checkout',applicationId:'policy'});session.setBearer('two');old.resolve({id:'policy',platform:'app',status:'awaiting_provider',checkoutUrl:'https://insurer.example/private'});await request;check(!p.model.snapshot().url,'An old insurance response cannot open another account private document or checkout');p.dispose();
}
{
 session.setBearer('one');const old=deferred();api.account.demands=()=>old.promise;const p=mounted(()=>useAccountFeeds('demands'));const request=p.model.fetchPage();session.setBearer('two');old.resolve([{id:'demand',title:'private'}]);await request;check(p.model.items.value.length===0&&!p.model.loading.value,'The shared native account lists discard old-account demand data immediately');p.dispose();
}
{
 session.setBearer('one');const old=deferred();api.account.orders=role=>role==='all'?old.promise:Promise.resolve([{id:'carrier-order'}]);const p=mounted(()=>useAccountFeeds('orders'));const request=p.model.fetchPage();p.model.chooseRole('carrier');await flush();old.resolve([{id:'old-buyer-order'}]);await request;check(p.model.role.value==='carrier'&&p.model.items.value[0]?.id==='carrier-order','Changing an order-role filter cannot be overwritten by its earlier in-flight list');p.dispose();
}
{
 const source=fs.readFileSync(path.join(mobile,'components/CommunityCommerceBar.vue'),'utf8').match(/<script setup[^>]*>([\s\S]*?)<\/script>/)[1].replace(/import OriginalMdiIcon[^\n]+\n/,'');
 const fixture="const defineProps=()=>({anchor:{type:'product',id:'123e4567-e89b-42d3-a456-426614174000'}});\n"+source+'\nexports.press=press;';
 const component=evaluate(fixture,path.join(mobile,'components/CommunityCommerceBar.vue'));nav.length=0;component.press('product');component.press('demand');check(nav[0].startsWith('/pages/product-detail/index?id=')&&nav[1]==='/pages/index/index','Community commerce links open public product checkout and a valid square instead of private editing or an empty detail');
}
check(privateWebTarget('https://pay.example/path?reference=1')==='https://pay.example/path?reference=1'&&['http://pay.example','https://user:secret@pay.example','https://pay.example\\@evil.example','https://pay.example\n/a'].every(url=>!privateWebTarget(url)),'Native checkout URLs require HTTPS without embedded credentials, slashes or control-character ambiguity');
console.log(`MOBILE BUSINESS PARITY CHECK PASSED: ${count} assertions`);
