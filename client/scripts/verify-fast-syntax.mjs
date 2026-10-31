import fs from 'node:fs';
import path from 'node:path';
import ts from 'typescript';
const root=process.cwd();
function files(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(d=>d.isDirectory()?files(path.join(dir,d.name)):[path.join(dir,d.name)]).filter(f=>/\.(vue|ts)$/.test(f));}
const all=files(path.join(root,'apps/mobile/src'));
const fail=[];
for(const f of all){
 const content=fs.readFileSync(f,'utf8');const isVue=f.endsWith('.vue');
 const match=isVue?content.match(/<script(?:\s+[^>]*)?>([\s\S]*?)<\/script>/):null;
 const code=isVue?match?.[1]??'':content;
 if(isVue&&!code.trim()){fail.push(f+': missing script');continue;}
 const result=ts.transpileModule(code,{fileName:f+'.ts',reportDiagnostics:true,compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.ESNext}});
 for(const d of result.diagnostics??[])if(d.category===ts.DiagnosticCategory.Error)fail.push(f+': '+ts.flattenDiagnosticMessageText(d.messageText,' '));
 if(isVue){
  const tmpl=content.match(/<template>([\s\S]*?)<\/template>/);
  if(!tmpl && !f.endsWith('/App.vue'))fail.push(f+': missing template');
 }
}
if(fail.length){console.error(fail.slice(0,30).join('\n'));process.exitCode=1}
else console.log(`MOBILE TYPESCRIPT/VUE SCRIPT SYNTAX CHECK PASSED: ${all.length} source files`);
