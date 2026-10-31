import fs from 'node:fs';
import crypto from 'node:crypto';
const baseline=JSON.parse(fs.readFileSync(new URL('./original-tabbar-assets-v12.json',import.meta.url),'utf8'));
for(const [file,{sha256,original}] of Object.entries(baseline)){
 const actual=crypto.createHash('sha256').update(fs.readFileSync(new URL(`../apps/mobile/src/static/${file}`,import.meta.url))).digest('hex');
 if(actual!==sha256)throw Error(`Original ${original} asset changed: ${file}`);
 console.log(`OK source image ${original} -> ${file}`);
}
console.log('Original tabbar bitmap exact-byte regression passed');
