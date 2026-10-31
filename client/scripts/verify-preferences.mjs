import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import {mkdtempSync,rmSync,existsSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {execFileSync} from 'node:child_process';
import {createRequire} from 'node:module';
const projectRoot=path.resolve(import.meta.dirname,'..');
const tmp=mkdtempSync(path.join(tmpdir(),'kx-pref-'));
const tscPath=path.join(projectRoot,'node_modules/typescript/bin/tsc');
const tsc=existsSync(tscPath)?process.execPath:(process.platform==='win32'?'tsc.cmd':'tsc');
const args=[...(existsSync(tscPath)?[tscPath]:[]),'--target','ES2022','--module','CommonJS','--skipLibCheck','--outDir',tmp,path.join(projectRoot,'packages/preferences/src/core.ts')];
try{execFileSync(tsc,args,{stdio:'pipe',shell:process.platform==='win32'})}
catch(e){rmSync(tmp,{recursive:true,force:true});throw e}
const require=createRequire(import.meta.url);
const {normalizePreferences,DEFAULT_PREFERENCES,cssAccentContrast,initPreferences,subscribePreferences,getPreferences,setPreferences,resetPreferences,effectiveScale,validHex}=require(path.join(tmp,'core.js'));

const fixed={};
const adapter={getItem:(k)=>fixed[k]??null,setItem:(k,v)=>{fixed[k]=v}};
assert.deepEqual(normalizePreferences(null),DEFAULT_PREFERENCES);
assert.equal(normalizePreferences({accent:'red',scale:99,theme:'invalid'}).accent,DEFAULT_PREFERENCES.accent);
assert.equal(normalizePreferences({scale:99}).scale,1.3);
assert.equal(normalizePreferences({scale:.1}).scale,.9);
assert.equal(cssAccentContrast('#000000'),'#ffffff');
assert.equal(cssAccentContrast('#ffffff'),'#111827');
assert.equal(validHex('#123ABC'),true);assert.equal(validHex('#12oops'),false);
initPreferences(adapter);
let counter=0;const unsubscribe=subscribePreferences(()=>{counter++});
setPreferences({care:true,scale:.9,accent:'#123456',font:'reading'});
assert.equal(effectiveScale(),1.15);assert.equal(getPreferences().accent,'#123456');
assert.equal(getPreferences().font,'reading');
assert.equal(JSON.parse(fixed['kx.display.v1']).care,true);
assert.equal(counter,1);unsubscribe();resetPreferences();assert.equal(getPreferences().theme,'system');
const root=path.resolve(import.meta.dirname,'..');
for(const app of ['pc','admin']){
 const source=fs.readFileSync(path.join(root,`apps/${app}/src/components/${app==='pc'?'SiteHeader':'AdminTopbar'}.vue`),'utf8')+fs.readFileSync(path.join(root,`apps/${app}/src/stores/${app==='pc'?'useSiteStore':'useAdminStore'}.ts`),'utf8')+(app==='admin'?fs.readFileSync(path.join(root,'apps/admin/src/views/SettingsView.vue'),'utf8'):'');
 assert.ok(source.includes('<PreferencesPanel'), `${app} needs shared preferences`);
 assert.ok(source.includes('changeLocale(event:Event)'), `${app} must not mutate localeRef through v-model`);
 assert.ok(!source.includes('<select v-model="locale"'), `${app} locale selector must be single-direction`);
}
const me=fs.readFileSync(path.join(root,'apps/mobile/src/pages/me/index.vue'),'utf8')+fs.readFileSync(path.join(root,'apps/mobile/src/stores/useMePage.ts'),'utf8');
assert.ok(me.includes('changeTheme')&&me.includes('changeFont')&&me.includes('changeCare'));
const mobile=fs.readFileSync(path.join(root,'apps/mobile/src/App.vue'),'utf8');
assert.ok(mobile.includes('subscribeLocale'), 'mobile tab bar must subscribe to language changes');
const missing=['/route-share/','/api/ai/recommend','/api/community/video'];
const client=fs.readFileSync(path.join(root,'packages/sdk/src/client.ts'),'utf8');
for(const path of missing)assert.ok(!client.includes(path),'SDK advertises missing backend route '+path);
console.log('DISPLAY PREFERENCES, LIVE I18N AND CAPABILITY BOUNDARY TESTS PASSED');

rmSync(tmp,{recursive:true,force:true});
