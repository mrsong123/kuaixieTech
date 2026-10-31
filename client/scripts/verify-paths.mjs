import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
const root=path.resolve(import.meta.dirname,'..');let count=0;
function scan(directory){for(const entry of fs.readdirSync(directory,{withFileTypes:true})){
 const filename=path.join(directory,entry.name);
 if(entry.isDirectory()){if(!['node_modules','dist','.git'].includes(entry.name))scan(filename);continue;}
 if(!/\.(vue|ts|css)$/.test(filename))continue;
 let data=fs.readFileSync(filename,'utf8');
 const rex=/(?:from\s*|@import\s*)(['"])(\.{1,2}\/[^'"\n]+)\1/g;let match;
 while((match=rex.exec(data))){const declared=match[2];const absolute=path.resolve(path.dirname(filename),declared);
 const variations=[absolute,...['.ts','.js','.vue','.json','.css'].map(x=>absolute+x),path.join(absolute,'index.ts')];
 assert.ok(variations.some(x=>fs.existsSync(x)),`missing import ${declared} from ${path.relative(root,filename)}`);count++;
 }
}}
for(const entry of ['apps','packages'])scan(path.join(root,entry));
console.log(`OK: ${count} local imports resolved`);
