import fs from 'node:fs';
import path from 'node:path';
const dir=path.join(import.meta.dirname,'../packages/i18n/src');
const zh=JSON.parse(fs.readFileSync(path.join(dir,'zh-CN.json'),'utf8'));
const en=JSON.parse(fs.readFileSync(path.join(dir,'en-US.json'),'utf8'));
const dump=o=>Object.keys(o).map(k=>'    '+JSON.stringify(k)+': '+JSON.stringify(o[k])+',\n').join('').replace(/,\n$/,'\n');
const out='// GENERATED from translation JSON.\nexport const messages: Record<"zh-CN" | "en-US", Record<string,string>> = {\n  "zh-CN": {\n'+dump(zh)+'  },\n  "en-US": {\n'+dump(en)+'  }\n};\n';
fs.writeFileSync(path.join(dir,'messages.generated.ts'), out);
