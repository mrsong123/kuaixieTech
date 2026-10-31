import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
const root=path.resolve(import.meta.dirname,'..');
const read=p=>fs.readFileSync(path.join(root,p),'utf8');
const locale=JSON.parse(read('packages/i18n/src/zh-CN.json'));
const en=JSON.parse(read('packages/i18n/src/en-US.json'));
for(const key of ['m2.catalog.detail','m2.trip.detail','m2.catalog.buyUnavailable','m2.trip.noRouteBooking','m2.mobile.homeCatalog','m2.mobile.homeTrips']){
 assert.ok(locale[key]&&en[key],key+' is not translated');
}
const pages=JSON.parse(read('apps/mobile/src/pages.json'));
assert.ok(pages.pages.length>=7,'expected at least seven mobile routes');
for(const id of ['catalog','trips','smart'])assert.ok(pages.pages.some(p=>p.path===`pages/${id}/index`));
const desktop=read('apps/pc/src/components/SiteOverlays.vue');
assert.ok(desktop.includes('selectedProduct(selectedItem)'));
assert.ok(desktop.includes('selectedTrip(selectedItem)'));
assert.ok(!desktop.includes('JSON.stringify(selectedItem'));
const home=read('apps/mobile/src/pages/index/index.vue')+read('apps/mobile/src/stores/useIndexPage.ts');
assert.ok(home.includes('openCatalog'));
assert.ok(home.includes('api.catalog.trips(100)')); 
assert.ok(!home.includes('JSON.stringify(o)'));
const {mkdtempSync,existsSync,rmSync}=await import('node:fs');
const {tmpdir}=await import('node:os');
const {execFileSync}=await import('node:child_process');
const {createRequire}=await import('node:module');
const tmp=mkdtempSync(path.join(tmpdir(),'kx-m2-'));
const localTsc=path.join(root,'node_modules/typescript/bin/tsc');
const args=[...(existsSync(localTsc)?[localTsc]:[]),'--target','ES2022','--module','CommonJS','--skipLibCheck','--outDir',tmp,path.join(root,'packages/sdk/src/catalog.ts'),path.join(root,'packages/sdk/src/types.ts')];
execFileSync(existsSync(localTsc)?process.execPath:(process.platform==='win32'?'tsc.cmd':'tsc'),args,{stdio:'pipe',shell:process.platform==='win32'});
const {catalogMatch,productImage,tripAvailability}=createRequire(import.meta.url)(path.join(tmp,'catalog.js'));

const a={id:'8',name:'新疆苹果',category_id:'12',region:'新疆',spec:'500g',image_urls:'["https://cdn.example.com/a.png"]'};
assert.equal(catalogMatch(a,'苹果','12'),true);
assert.equal(catalogMatch(a,'not-found','12'),false);
assert.equal(catalogMatch(a,'苹果','13'),false);
assert.equal(productImage(a),'https://cdn.example.com/a.png');
assert.equal(productImage({...a,image_urls:'javascript:alert(1)'}),undefined);
assert.equal(productImage({...a,image_urls:'[not json'}),undefined);
assert.equal(tripAvailability({id:'t1',remaining_slots:2}),2);
assert.equal(tripAvailability({id:'t1',remaining_slots:-1}),null);
console.log('M2 catalogue/trip page, i18n and shared data tests PASSED');

rmSync(tmp,{recursive:true,force:true});
