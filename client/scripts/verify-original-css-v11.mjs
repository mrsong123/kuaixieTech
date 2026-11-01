import fs from 'node:fs';import path from 'node:path';
const root=process.cwd();
const read=(p)=>fs.readFileSync(path.join(root,p),'utf8');
const app=read('apps/mobile/src/styles/app.css');
const tokens=read('apps/mobile/src/styles/original-prototype.css');
const styleBaseline=JSON.parse(read('scripts/original-style-baseline.json'));
const minify=v=>String(v).replace(/\s+/g,'').toLowerCase();
for(const [key,val] of Object.entries(styleBaseline.values)){
 const found=tokens.match(new RegExp('--'+key+'\\s*:\\s*([^;]+);'));
 if(!found||minify(found[1])!==minify(val))throw Error('Original CSS token drift: '+key);
}
const sourceArg=process.argv.indexOf('--source');
if(sourceArg>=0&&!process.argv[sourceArg+1])throw Error('--source requires an original CSS file path');
const originalSource=sourceArg>=0?path.resolve(process.argv[sourceArg+1]):styleBaseline.source;
const requireOriginal=process.argv.includes('--require-original-source')||sourceArg>=0;
if(originalSource&&fs.existsSync(originalSource)){
 const meooCss=fs.readFileSync(originalSource,'utf8');
 for(const [key,val]of Object.entries(styleBaseline.values)){
  const found=meooCss.match(new RegExp('--'+key+'\\s*:\\s*([^;]+);'));
  if(!found||minify(found[1])!==minify(val))throw Error('Original reference token drift: '+key);
 }
 console.log('HISTORICAL_SOURCE_VERIFIED: '+Object.keys(styleBaseline.values).length+' exact original-reference tokens');
}else if(requireOriginal)throw Error('Required historical CSS source missing: '+originalSource);
else console.log('HISTORICAL_SOURCE_UNAVAILABLE: original Downloads CSS not supplied; external original-source comparison NOT RUN. Use --require-original-source --source <file> for strict historical verification.');
console.log('CURRENT_MOBILE_TOKEN_CHECK_PASSED: '+Object.keys(styleBaseline.values).length+' values match the committed baseline; historical-source status is reported separately.');
const plaza=read('apps/mobile/src/pages/index/index.vue');
const mine=read('apps/mobile/src/pages/me/index.vue');
const store=read('apps/mobile/src/stores/useIndexPage.ts');
const explorer=read('apps/mobile/src/components/plaza/OriginalCategoryExplorer.vue');
const tripCard=read('apps/mobile/src/components/plaza/OriginalTripCard.vue');
const demandCard=read('apps/mobile/src/components/plaza/OriginalDemandCard.vue');
const catalog=read('apps/mobile/src/stores/useCatalogPage.ts');
const locales=read('packages/i18n/src/messages.generated.ts');
function check(ok,msg){if(!ok)throw Error(msg);console.log('OK '+msg)}
check(app.includes('original-prototype.css'),'Original mobile CSS imported');
for(const key of ['--primary:','--border:','--brand-gradient:','--primary-10:'])check(tokens.includes(key),'Prototype token '+key);
for(const cls of ['px-4 pt-6 pb-4','nav-searchbar','nav-search-facade','nav-search-field','nav-search-submit','OriginalCategoryExplorer'])check(plaza.includes(cls),'Plaza original element '+cls);
check(store.includes('api.catalog.categories(100)')&&store.includes('direction.value'),'Original country/category/route filters read real API fields');
check(plaza.includes('OriginalTripCard')&&plaza.includes('OriginalDemandCard')&&tripCard.includes('route-tags')&&demandCard.includes('demand-bottom'),'Original-style trip/demand cards registered');
check(explorer.includes('api.catalog.categories(100)')&&explorer.includes('goCategory'),'Category hierarchy uses real data and route');
check(catalog.includes('options?.category'),'Catalog receives category deep link');
for(const cls of ['mine-hero','mine-quick','MineGroup','mine-logout'])check(mine.includes(cls),'Mine original section '+cls);
for(const lang of ['"zh-CN":','"en-US":'])check(locales.includes(lang),'Language block '+lang);
check(!plaza.includes('v-for="trip in [{'),'No fabricated trip records');
console.log('Original CSS first-pass structural checks passed (not pixel comparison)');
