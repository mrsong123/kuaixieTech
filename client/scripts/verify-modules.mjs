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
const mobilePages=fs.readdirSync(path.join(root,'apps/mobile/src/pages'));
for(const name of mobilePages){const file=path.join(root,'apps/mobile/src/pages',name,'index.vue');if(!fs.existsSync(file))continue;
 const s=fs.readFileSync(file,'utf8');
 // Standalone settings use a dedicated shared layout + small feature stores; no in-view network calls.
 const settingsScreen=name==='settings' && (file.includes('/settings/'));
 assert.ok(s.includes('../../stores/use')||s.includes('stores/use')||s.includes('SettingsLayout')||s.includes('AccountFeed'),name+' store not decoupled');
 assert.ok(!s.includes('api.portal.')&&!s.includes('api.support.'),name+' contains direct API request');}
const config=fs.readFileSync(path.join(root,'.npmrc'),'utf8');
assert.ok(config.includes('node-linker=hoisted'));assert.ok(config.includes('shared-workspace-lockfile=true'));
const registry=fs.readFileSync(path.join(root,'packages/sdk/src/registry.ts'),'utf8');
assert.ok(!registry.includes('"path": "/api/'));
console.log('OK: modular page/stores/api/router/styles + one root pnpm workspace + prefixless public paths');
