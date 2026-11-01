import fs from 'node:fs';
import path from 'node:path';

function csv(text){
 const rows=[];let row=[],field='',quoted=false;
 for(let i=0;i<text.length;i++){
  const char=text[i];
  if(quoted){if(char==='"'&&text[i+1]==='"'){field+='"';i++}else if(char==='"')quoted=false;else field+=char;}
  else if(char==='"')quoted=true;
  else if(char===','){row.push(field);field=''}
  else if(char==='\n'||char==='\r'){if(char==='\r'&&text[i+1]==='\n')i++;row.push(field);if(row.some(v=>v.trim()))rows.push(row);row=[];field=''}
  else field+=char;
 }
 if(quoted)throw new Error('Historical ledger contains an unclosed CSV quote');
 row.push(field);if(row.some(v=>v.trim()))rows.push(row);return rows;
}
/** Optional external historical evidence is separate from current route regression checks. */
export function historicalPageLedger(root,args=process.argv.slice(2)){
 const index=args.indexOf('--ledger');
 if(index>=0&&!args[index+1])throw new Error('--ledger requires the original CSV file path');
 const file=index>=0?path.resolve(args[index+1]):path.join(root,'MIGRATION_62_PAGE_STATUS.csv');
 const required=index>=0||args.includes('--require-original-ledger');
 if(!fs.existsSync(file)){
  if(required)throw new Error('Required original 62-page migration ledger missing: '+file);
  console.log('HISTORICAL_LEDGER_UNAVAILABLE: original 62-page CSV was not supplied; external source-to-target mapping NOT RUN. Use --require-original-ledger --ledger <csv> for strict comparison.');
  return null;
 }
 const rows=csv(fs.readFileSync(file,'utf8').replace(/^\uFEFF/,''));
 if(rows.length!==63)throw new Error('Original historical ledger must retain exactly 62 data rows');
 const routes=new Set(JSON.parse(fs.readFileSync(path.join(root,'apps/mobile/src/pages.json'),'utf8')).pages.map(p=>p.path));
 const entries=rows.slice(1).map(columns=>{
  const module=columns[0]?.trim(),target=columns[2]?.trim().replaceAll('\\','/');
  if(!module||!target||!target.startsWith('apps/mobile/src/pages/')||!target.endsWith('.vue'))throw new Error('Invalid historical module/target row');
  const actual=path.resolve(root,target),relative=path.relative(root,actual);
  if(relative==='..'||relative.startsWith('..'+path.sep)||path.isAbsolute(relative))throw new Error('Historical target escapes client root');
  if(!fs.existsSync(actual)||!fs.statSync(actual).isFile())throw new Error('Historical target file missing: '+module+' -> '+target);
  const route=target.replace(/^apps\/mobile\/src\//,'').replace(/\.vue$/,'');
  if(!routes.has(route))throw new Error('Historical target route not registered: '+module+' -> '+route);
  return {module,target,route};
 });
 console.log('HISTORICAL_LEDGER_VERIFIED: 62 supplied source rows resolve to actual registered Vue pages');
 return {entries};
}
