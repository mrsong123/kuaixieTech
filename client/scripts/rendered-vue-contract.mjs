import fs from 'node:fs';
import path from 'node:path';
/** Follow only components actually rendered by the page; wrapper extraction must not erase UI checks. */
export function renderedVueSources(root,file,seen=new Set()){
 const full=path.resolve(root,file);if(seen.has(full))return '';seen.add(full);
 const source=fs.readFileSync(full,'utf8'),template=source.match(/<template>([\s\S]*)<\/template>/)?.[1]||'';
 let graph=source;
 for(const match of source.matchAll(/import\s+(\w+)\s+from\s+['"]([^'"]+\.vue)['"]/g)){
  if(!new RegExp('<'+match[1]+'(?:\\s|/|>)').test(template))continue;
  const target=match[2].startsWith('@kuaixie/ui/')?path.join(root,'packages/ui/src',match[2].slice('@kuaixie/ui/'.length)):match[2].startsWith('.')?path.resolve(path.dirname(full),match[2]):null;
  if(target&&fs.existsSync(target))graph+='\n'+renderedVueSources(root,target,seen);
 }
 return graph;
}
