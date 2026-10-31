import fs from 'node:fs';
import path from 'node:path';
const root=path.resolve(import.meta.dirname,'..');
const zh=JSON.parse(fs.readFileSync(path.join(root,'packages/i18n/src/zh-CN.json'),'utf8'));
const en=JSON.parse(fs.readFileSync(path.join(root,'packages/i18n/src/en-US.json'),'utf8'));
let errors=[];
const fail=message=>errors.push(message);
const a=Object.keys(zh),b=Object.keys(en);
if(a.length!==b.length) fail('English and Chinese catalog keys have different counts');
for(const key of a){if(!(key in en))fail('Missing en-US key '+key);if(!zh[key]||!en[key])fail('Empty translation '+key);if(/[\u3400-\u9fff]/.test(en[key]))fail('Untranslated zh value in en-US '+key)}
function sources(dir){if(!fs.existsSync(dir))return [];return fs.readdirSync(dir,{withFileTypes:true}).flatMap(entry=>entry.isDirectory()?sources(path.join(dir,entry.name)):[path.join(dir,entry.name)]).filter(x=>x.endsWith('.vue'))}
let pages=0,keysUsed=new Set();
for(const file of [...sources(path.join(root,'apps')),...sources(path.join(root,'packages/ui'))]){
 const rel=path.relative(root,file);const source=fs.readFileSync(file,'utf8');let start=source.indexOf('<template>'),end=source.lastIndexOf('</template>');
 if(start===-1||end<=start)continue;pages++;
 const html=source.slice(start+10,end);
 for(const match of html.matchAll(/\$t\(['"]([\w.]+)['"]/g)){
   keysUsed.add(match[1]);if(!(match[1] in zh))fail(rel+' references missing i18n key '+match[1]);
 }
 // Scan text outside markup and template expressions. Developer comments and source code are excluded.
 let i=0,tag=false,quote='',interp=false;let plain='';
 const check=()=>{const trim=plain.trim();plain='';if(/[\u3400-\u9fff]/.test(trim))fail(rel+' has literal Chinese text: '+trim.slice(0,45))};
 while(i<html.length){
  if(!tag&&html.startsWith('{{',i)){check();interp=true;i+=2;continue}
  if(interp&&html.startsWith('}}',i)){interp=false;i+=2;continue}
  if(interp){i++;continue}
  const c=html[i];
  if(c==='<'){check();tag=true;i++;continue}
  if(tag){if(quote){if(c===quote&&html[i-1]!=='\\')quote=''}else if(c==='"'||c==="'")quote=c;else if(c==='>')tag=false;i++;continue}
  plain+=c;i++;
 }
 check();
 // Screens must never hardcode RMB in product price rendering.
 if(/['"]¥['"]\s*\+\s*Number\(/.test(source))fail(rel+' assumes RMB for backend price');
}
for(const pathName of ['apps/pc/src/main.ts','apps/admin/src/main.ts','apps/mobile/src/main.ts']){
 const content=fs.readFileSync(path.join(root,pathName),'utf8');if(!content.includes('installI18n'))fail(pathName+' lacks shared i18n registration')
}
const sdk=fs.readFileSync(path.join(root,'packages/sdk/src/client.ts'),'utf8');
if(!sdk.includes('Accept-Language'))fail('SDK missing Accept-Language header');
if(!sdk.includes('settings.getLocale'))fail('SDK missing reactive locale source');
console.log(`i18n verified: ${pages} Vue files; ${a.length} Chinese + English keys; ${keysUsed.size} used literal keys`);
if(errors.length){console.error(errors.slice(0,50).join('\n'));console.error(`${errors.length} violations`);process.exitCode=1}
else console.log('I18N STATIC CHECK PASSED');
