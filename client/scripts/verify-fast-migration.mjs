import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
const root=process.cwd();
const read=p=>fs.readFileSync(path.join(root,p),'utf8');
const pages=JSON.parse(read('apps/mobile/src/pages.json'));
const source=read('apps/mobile/src/features/flows/definitions.ts');
const content=source.match(/const definitions:Record<string,\{title:string;description:string;paths:string\[\]\}>=([\s\S]*?);\nexport const flowDefinitions/);
assert.ok(content,'missing flow contract');
const definitions=JSON.parse(content[1]);
const routes=new Set(pages.pages.map(p=>p.path));
const sdk=read('packages/sdk/src/registry.ts');
let unique=new Set();
for(const [name, config] of Object.entries(definitions)){
 const dir=`apps/mobile/src/pages/${name}/index.vue`;
 assert.ok(fs.existsSync(path.join(root,dir)),`flow page missing: ${name}`);
 assert.ok(routes.has(`pages/${name}/index`),`route missing: ${name}`);
 assert.ok(read(dir).includes('useWorkflowPage'),`page not modular: ${name}`);
 for(const operation of config.paths){
  assert.ok(!operation.startsWith('/api/'),`frontend prefix leaked: ${operation}`);
  assert.ok(sdk.includes(`"path": "${operation}"`),`operation missing in verified backend: ${operation}`);
  unique.add(operation);
 }
}
for(const page of ['services','product-detail','trip-detail'])assert.ok(routes.has(`pages/${page}/index`),page);
for(const page of ['catalog','trips','index'])assert.ok(read(`apps/mobile/src/stores/use${page==='catalog'?'Catalog':page==='trips'?'Trips':'Index'}Page.ts`).includes('navigateTo'),'detail navigation missing');
assert.equal(Object.keys(definitions).length,21);
assert.equal(unique.size,48);
const messages=JSON.parse(read('packages/i18n/src/zh-CN.json'));
for(const name of Object.keys(definitions))assert.ok(messages['fast.flow.'+name.replace(/-/g,'_')]);
console.log(`FAST MIGRATION CHECK PASSED: ${Object.keys(definitions).length} workflow pages, ${unique.size} backend operations, 2 catalog details, services index, no synthetic API prefix`);
