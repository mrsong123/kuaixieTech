import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const client=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const mobile=path.join(client,'apps/mobile');
const args=process.argv.slice(2),modeOption=args.indexOf('--mode'),mode=modeOption<0?'build':args[modeOption+1];
if(!['build','dev'].includes(mode))throw new Error('Use --mode build or --mode dev');
const output=path.join(mobile,'dist',mode,'mp-weixin');
const sync=args.includes('--sync-config');
function fail(message){throw new Error(`WECHAT_OUTPUT_INVALID: ${message}`)}
function readJson(file){if(!fs.existsSync(file))fail(`Missing ${file}. Run pnpm ${mode==='build'?'build':'dev'}:mp-weixin in client/ first. Do not import UniApp src/ as a native mini program.`);try{return JSON.parse(fs.readFileSync(file,'utf8'))}catch(error){fail(`Invalid JSON ${file}: ${error.message}`)}}
function contained(file){const relative=path.relative(output,file);if(relative.startsWith('..'+path.sep)||relative==='..'||path.isAbsolute(relative))fail(`Reference escapes mini-program output: ${file}`);return file}
function fileExists(relative){const file=contained(path.resolve(output,relative));if(!fs.existsSync(file)||!fs.statSync(file).isFile())fail(`Missing generated file: ${file}`);return file}
function page(route){if(typeof route!=='string'||!route)fail('Invalid page route');for(const extension of ['js','json','wxml'])fileExists(route+'.'+extension)}
function files(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(entry=>entry.isDirectory()?files(path.join(dir,entry.name)):[path.join(dir,entry.name)])}

const app=readJson(path.join(output,'app.json'));
fileExists('app.js');
if(!Array.isArray(app.pages)||!app.pages.length)fail('app.json has no pages');
const routes=[...app.pages];
const subpackages=app.subPackages||app.subpackages||[];
for(const group of subpackages){if(typeof group.root!=='string'||!Array.isArray(group.pages))fail('Invalid subpackage');routes.push(...group.pages.map(route=>group.root.replace(/\/$/,'')+'/'+route))}
if(new Set(routes).size!==routes.length)fail('Duplicate registered page');
for(const route of routes)page(route);
for(const tab of app.tabBar?.list||[]){if(!routes.includes(tab.pagePath))fail(`Unregistered tab page ${tab.pagePath}`);for(const key of ['iconPath','selectedIconPath'])if(tab[key])fileExists(tab[key])}
let components=0;
for(const file of files(output).filter(file=>file.endsWith('.json'))){
 const json=readJson(file);
 for(const reference of Object.values(json.usingComponents||{})){
  if(typeof reference!=='string')fail(`Invalid component reference in ${file}`);
  if(reference.startsWith('plugin://')||reference.startsWith('dynamicLib://')||reference.startsWith('wx://'))continue;
  const target=contained(reference.startsWith('/')?path.resolve(output,'.'+reference):path.resolve(path.dirname(file),reference));
  for(const extension of ['js','json','wxml'])if(!fs.existsSync(target+'.'+extension))fail(`Missing component ${reference}.${extension} referenced by ${file}`);
  components++;
 }
}
let staticReferences=0;
for(const file of files(output).filter(file=>/\.(?:js|wxs|wxml|wxss)$/.test(file))){
 const source=fs.readFileSync(file,'utf8');
 const expressions=/\.(?:js|wxs)$/.test(file)?[/\brequire\(\s*['"]([^'"]+)['"]\s*\)/g]:file.endsWith('.wxml')?[/<(?:import|include)\b[^>]*\bsrc=['"]([^'"]+)['"]/g]:[/@import\s+['"]([^'"]+)['"]/g];
 for(const expression of expressions)for(const match of source.matchAll(expression)){
  const reference=match[1];
  if(!reference.startsWith('.')&&!reference.startsWith('/'))continue;
  const target=contained(reference.startsWith('/')?path.resolve(output,'.'+reference):path.resolve(path.dirname(file),reference));
  const candidates=path.extname(target)?[target]:[target,target+'.js',target+'.json'];
  if(!candidates.some(candidate=>fs.existsSync(candidate)&&fs.statSync(candidate).isFile()))fail(`Missing static dependency ${reference} referenced by ${file}`);
  staticReferences++;
 }
}
const rootPath=path.join(mobile,'project.config.json'),root=readJson(rootPath);
const manifest=readJson(path.join(mobile,'src/manifest.json'));
const appid=manifest['mp-weixin']?.appid||'touristappid';
if(appid!=='touristappid'&&!/^wx[0-9a-f]{16}$/.test(appid))fail('Configure a valid mp-weixin.appid in src/manifest.json');
if(sync){
 const generatedPath=path.join(output,'project.config.json'),generated=readJson(generatedPath);
 generated.appid=appid;
 // Direct output imports use app.json beside this project file, not the source project's relative root.
 generated.miniprogramRoot='./';
 fs.writeFileSync(generatedPath,JSON.stringify(generated,null,2)+'\n');
 if(mode==='build'){
  root.appid=appid;
  root.miniprogramRoot='dist/build/mp-weixin/';
  fs.writeFileSync(rootPath,JSON.stringify(root,null,2)+'\n');
 }
}
const importRoot=path.resolve(mobile,root.miniprogramRoot||'.');
if(mode==='build'&&path.resolve(importRoot)!==output)fail('Source project miniprogramRoot must point to dist/build/mp-weixin/');
if(mode==='build')fileExists(path.relative(output,path.join(importRoot,'app.json')));
const generated=readJson(path.join(output,'project.config.json'));
if(generated.appid!==appid||mode==='build'&&root.appid!==appid)fail('Manifest and project AppIDs differ. Rebuild with the output verification hook.');
let total=0,main=0;
for(const file of files(output)){
 const bytes=fs.statSync(file).size,relative=path.relative(output,file).split(path.sep).join('/');total+=bytes;
 if(!subpackages.some(group=>relative.startsWith(group.root.replace(/\/$/,'')+'/')))main+=bytes;
}
console.log(`WECHAT OUTPUT CHECK PASSED: ${routes.length} pages; ${components} local component references; ${staticReferences} static dependencies; ${path.join(output,'app.json')}`);
console.log(`Generated main-package files: ${main} bytes; total: ${total} bytes (local size estimate; DevTools upload report is authoritative).`);
if(main>2*1024*1024)console.warn('Main-package files exceed 2 MiB. This does not cause missing app.json, but release upload size must be checked in WeChat DevTools.');
console.log(`Import ${mode==='build'?mobile:output} in WeChat DevTools after ${mode==='build'?'pnpm build:mp-weixin':'pnpm dev:mp-weixin has completed its first compilation'}.`);
if(appid==='touristappid')console.warn('Tourist AppID is for inspection only; configure your authorized AppID in src/manifest.json and rebuild before real login or release.');
