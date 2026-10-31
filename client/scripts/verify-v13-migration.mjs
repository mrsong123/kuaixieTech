import fs from 'node:fs';
import path from 'node:path';
const root=process.cwd();
const file=p=>path.join(root,p);
const read=p=>fs.readFileSync(file(p),'utf8');
const assert=(condition,msg)=>{if(!condition)throw Error(`V13 regression: ${msg}`)};
const pages=JSON.parse(read('apps/mobile/src/pages.json')).pages;
const newPages=['invite-team','invite-apply','support-center','support-session','demand-detail','favorites','cart','address-book','my-products'];
for(const name of newPages){
  const route=`pages/${name}/index`;
  assert(pages.some(x=>x.path===route),`missing ${route} in pages.json`);
  assert(fs.existsSync(file(`apps/mobile/src/${route}.vue`)),`missing ${route}.vue`);
  console.log(`OK independent original route: ${name}`);
}
const icons=read('apps/mobile/src/pages/me/index.vue');
const home=read('apps/mobile/src/pages/index/index.vue');
const store=read('apps/mobile/src/stores/useIndexPage.ts');
const iconComponent=read('apps/mobile/src/components/OriginalMdiIcon.vue');
assert(icons.includes('OriginalMdiIcon'), 'original Mine icon assets not rendered');
assert(home.includes('matchingProducts')&&home.includes('matchingDemands')&&home.includes('matchingTrips'),'original search result groups absent');
assert(store.includes('catalogMatch')&&store.includes('matchesCountry')&&store.includes('cat.country_code'),'real catalog or real country constraint absent');
assert(store.includes('api.catalog.products(100)'),'real public catalog API not requested');
assert(!store.includes('mockProducts')&&!store.includes('fakeData'),'fake product data');
assert(iconComponent.includes('/static/mdi/'),'mini-program safe icon image missing');
assert(fs.existsSync(file('apps/mobile/src/static/mdi/white/account.png')),'profile white glyph absent');
for(const glyph of ['magnify','earth','close-circle','package-variant'])assert(fs.existsSync(file(`apps/mobile/src/static/mdi/muted/${glyph}.png`)),`missing muted ${glyph}`);
let totalBytes=0;
for(const tone of ['accent','white','muted'])for(const name of fs.readdirSync(file(`apps/mobile/src/static/mdi/${tone}`))){
  assert(name.endsWith('.png'),'font or unsupported binary found in icon assets');
  totalBytes+=fs.statSync(file(`apps/mobile/src/static/mdi/${tone}/${name}`)).size;
}
assert(totalBytes<650*1024,`icon resources too large for mini-program main package: ${totalBytes}`);
const aliases={'account-camera':'camera-account','account-single':'account','account-travel':'airplane','account-travel-outline':'airplane','airplane-off-outline':'airplane','cosmetic':'palette-swatch-outline','earth-outline':'earth','file-document-search-outline':'file-search-outline','medical':'medical-bag','shield-refund-outline':'shield-outline','stroller':'baby-carriage'};
function scanVue(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(d=>d.isDirectory()?scanVue(path.join(dir,d.name)):(d.name.endsWith('.vue')?[path.join(dir,d.name)]:[]));}
for(const vue of scanVue(file('apps/mobile/src'))){
  const code=fs.readFileSync(vue,'utf8');
  const names=[...code.matchAll(/\bicon="i-mdi-([\w-]+)"/g)].map(x=>x[1]);
  names.push(...[...code.matchAll(/<OriginalMdiIcon\s+name="([\w-]+)"/g)].map(x=>x[1]));
  for(const name of names){
    const normalized=aliases[name]||name;
    assert(fs.existsSync(file(`apps/mobile/src/static/mdi/accent/${normalized}.png`)),`${name} referenced but missing icon in ${vue}`);
  }
}
const rowCount=read('MIGRATION_62_PAGE_STATUS.csv').trim().split(/\r?\n/).length-1;
assert(rowCount===62,`original page inventory altered (${rowCount})`);
assert(read('packages/i18n/src/zh-CN.json').includes('"migration.address"'),'translation keys absent');
for(const name of newPages){const code=read(`apps/mobile/src/pages/${name}/index.vue`);assert(code.includes('$t('),'hardcoded fallback user copy');}
console.log(`V13 incremental migration verification passed: ${newPages.length} registered routes; ${rowCount} original pages audited; MDI assets ${Math.round(totalBytes/1024)} KiB; real catalog country guard present`);
