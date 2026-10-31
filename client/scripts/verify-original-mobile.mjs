import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
const root=path.resolve(import.meta.dirname,'..');
const load=p=>fs.readFileSync(path.join(root,p),'utf8');
const pages=JSON.parse(load('apps/mobile/src/pages.json'));
const tabs=pages.tabBar.list;
const expected=['index','demand-pool','publish-hub','messages','me'];
assert.deepEqual(tabs.map(t=>t.pagePath),expected.map(x=>`pages/${x}/index`));
for(const entry of tabs){
 for(const key of ['iconPath','selectedIconPath']){
  const imagePath=path.join(root,'apps/mobile/src',entry[key]);
  assert.ok(fs.statSync(imagePath).size>100,'Missing original tab icon '+imagePath);
 }
}
for(const name of expected){
 const source=load(`apps/mobile/src/pages/${name}/index.vue`);
 assert.match(source,/<script setup lang="ts">/);
 assert.match(source,/<template>/);
 assert.match(source,/<style scoped>/);
}
const home=load('apps/mobile/src/pages/index/index.vue')+load('apps/mobile/src/stores/useIndexPage.ts');
assert.ok(home.includes('api.portal.demands(100)')); 
assert.ok(home.includes('api.catalog.trips(100)'));
assert.ok(home.includes('api.portal.verifiedRole()')); 
assert.ok(home.includes('portal.square.search')); 
assert.ok(home.includes('mobileTripDetails')); 
const util=load('apps/mobile/src/router/index.ts');
for(const tab of expected)assert.ok(util.includes(tab),'Navigation missing tab: '+tab);
for(const sub of ['discover','route','tools'])assert.ok(util.includes(sub),'Secondary page not reachable: '+sub);
const app=load('apps/mobile/src/App.vue');
assert.ok(app.includes('subscribeLocale'));
assert.ok(app.includes('prototype.nav.square')&&app.includes('portal.tabs.community')); 
const pc=load('apps/pc/src/App.vue')+load('apps/pc/src/stores/useSiteStore.ts');
assert.ok(pc.includes('@kuaixie/sdk'),'PC official site lost real backend catalog');
const dict=JSON.parse(load('packages/i18n/src/zh-CN.json'));
for(const key of ['prototype.nav.square','portal.tabs.community','prototype.nav.publish','prototype.nav.messages','portal.square.search','portal.community.title'])assert.ok(dict[key],key);
console.log('MOBILE V5 NAVIGATION CHECK PASSED: 5 tabs, original assets, role-aware plaza, community and live locale');
