import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
const root=path.resolve(import.meta.dirname,'..');
const read=(name)=>fs.readFileSync(path.join(root,name),'utf8');
const routes=JSON.parse(read('apps/mobile/src/pages.json')).pages.map(p=>p.path);
for(const name of ['my-orders','my-demands','my-trips','my-wallet','wallet-ledger','my-notifications','order-detail']){
 const id='pages/'+name+'/index';
 assert.ok(routes.includes(id),'missing UniApp route '+id);
 assert.ok(read('apps/mobile/src/'+id+'.vue').includes('<template>'),'missing page template '+id);
}
const sdk=read('packages/sdk/src/client.ts');
for(const endpoint of ['/me/profile','/me/orders','/me/orders/{id}','/me/demands','/me/trips','/me/wallet','/me/wallet/ledger','/me/notifications','/me/notifications/{id}/read'])assert.ok(sdk.includes("'"+endpoint+"'"),endpoint+' missing from SDK');
assert.ok(sdk.includes("'PUT','/me/profile'"),'profile must use PUT; patch not portable on mini programmes');
assert.ok(!sdk.includes("'/api/me"),'public API must remain prefix-free');
const zh=JSON.parse(read('packages/i18n/src/zh-CN.json'));
const en=JSON.parse(read('packages/i18n/src/en-US.json'));
for(const key of Object.keys(zh))assert.ok(Object.hasOwn(en,key),'missing en key '+key);
assert.equal(Object.keys(zh).length,Object.keys(en).length);
assert.ok(read('apps/mobile/src/stores/useServicesPage.ts').includes("'my-orders'"),'orders missing from personal menu');
assert.ok(read('apps/mobile/src/stores/useAccountFeeds.ts').includes('api.account.orders'),'orders must use real account API');
assert.ok(read('apps/mobile/src/stores/useWalletSummary.ts').includes('api.account.wallet'),'wallet must use real balance API');
assert.ok(read('apps/mobile/src/stores/useAccountProfile.ts').includes('api.account.updateProfile'),'profile save must use server API');
console.log(`V9 ACCOUNT CONTRACT CHECK PASSED: 10 account endpoints, 7 new pages, ${Object.keys(zh).length} bilingual keys`);
