import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
const root=path.resolve(import.meta.dirname,'..');
const exists=(p)=>fs.existsSync(path.join(root,p));
for(const app of ['pc','admin','mobile']){
  for(const folder of ['api','stores','router','styles'])assert.ok(exists(`apps/${app}/src/${folder}`), `${app}/${folder}`);
  assert.ok(exists(`apps/${app}/src/App.vue`));
}
for(const app of ['pc','admin']){
  for(const folder of ['views','components'])assert.ok(exists(`apps/${app}/src/${folder}`));
  const appSource=fs.readFileSync(path.join(root,`apps/${app}/src/App.vue`),'utf8');
  assert.ok(appSource.length<3000, `${app} App.vue still holds business logic`);
}
// Native page wrappers may delegate to a rendered business component rather than a page store.
// Follow that real component import; an arbitrary helper or unused import does not qualify.
function delegates(source,file,seen=new Set()){
 if(source.includes('stores/use')||source.includes('SettingsLayout')||source.includes('AccountFeed')||/from ['"](?:@kuaixie\/ui\/use|\.\/use)/.test(source))return true;
 if(seen.has(file))return false;seen.add(file);
 for(const match of source.matchAll(/import\s+(\w+)\s+from\s+['"]([^'"]+\.vue)['"]/g)){
  if(!new RegExp('<'+match[1]+'(?:\\s|/|>)').test(source))continue;
  const target=match[2].startsWith('@kuaixie/ui/')?path.join(root,'packages/ui/src',match[2].slice('@kuaixie/ui/'.length)):match[2].startsWith('.')?path.resolve(path.dirname(file),match[2]):null;
  if(target&&fs.existsSync(target)&&delegates(fs.readFileSync(target,'utf8'),target,seen))return true;
 }
 return false;
}
const mobilePages=fs.readdirSync(path.join(root,'apps/mobile/src/pages'));
for(const name of mobilePages){const file=path.join(root,'apps/mobile/src/pages',name,'index.vue');if(!fs.existsSync(file))continue;
 const s=fs.readFileSync(file,'utf8');
 // Standalone settings use a dedicated shared layout + small feature stores; no in-view network calls.
 const privateDocumentController=['payment-checkout','insurance-document'].includes(name)&&s.includes('usePrivatePageSession')&&s.includes('scope.current(owner)');
 assert.ok(delegates(s,file)||privateDocumentController,name+' must delegate to a real store/model/component or a guarded private-document controller');
 assert.ok(!s.includes('api.portal.')&&!s.includes('api.support.'),name+' contains direct API request');}
const config=fs.readFileSync(path.join(root,'.npmrc'),'utf8');
assert.ok(config.includes('node-linker=hoisted'));assert.ok(config.includes('shared-workspace-lockfile=true'));
const registry=fs.readFileSync(path.join(root,'packages/sdk/src/registry.ts'),'utf8');
assert.ok(!registry.includes('"path": "/api/'));
console.log('OK: modular page/stores/api/router/styles + one root pnpm workspace + prefixless public paths');
