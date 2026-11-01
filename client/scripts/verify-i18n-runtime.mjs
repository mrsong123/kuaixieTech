import {mkdtempSync,existsSync,rmSync} from 'node:fs';
import {tmpdir} from 'node:os';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {createRequire} from 'node:module';
const root=path.resolve(import.meta.dirname,'..');
const target=mkdtempSync(path.join(tmpdir(),'kx-i18n-'));
try{
  const tsScript=path.join(root,'node_modules/typescript/bin/tsc');
  const args=['--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',target,
    path.join(root,'packages/i18n/src/core.ts'),path.join(root,'packages/i18n/src/messages.generated.ts')];
  if(existsSync(tsScript))execFileSync(process.execPath,[tsScript,...args],{stdio:'pipe'});
  else execFileSync(process.platform==='win32'?'tsc.cmd':'tsc',args,{stdio:'pipe',shell:process.platform==='win32'});
  const require=createRequire(import.meta.url);
  const intl=require(path.join(target,'core.js'));
  const assert=(v,msg)=>{if(!v)throw Error('FAILED: '+msg);console.log('OK:',msg)};
  intl.setLocale('en-US');
  assert(intl.t('pc.nav.home')==='Home','en-US UI translation');
  assert(intl.t('pc.nav.home')!==intl.t('pc.nav.products'),'distinct navigation terms');
  assert(intl.formatAmount('12.5','USD').includes('$'),'explicit USD formatter');
  assert(intl.formatAmount('12.345','BHD').includes('12.345'),'three-decimal insurance currencies are not rounded to two decimals');
  assert(!intl.formatAmount('1000','JPY').includes('.00'),'zero-decimal currency display preserves its ISO precision');
  assert(!intl.formatAmount('12.5',null).includes('¥'),'price without currency never assumes CNY');
  assert(intl.localizedField({name:'原始名称',translations:{'en-US':{name:'Localized name'}}},'name')==='Localized name','localized API field precedence');
  assert(intl.localizedField({name:'原始名称'},'name')==='原始名称','raw API field preserved without fabricated translation');
  const date=intl.formatDate('2026-10-30');
  assert(date.includes('2026'),'date-only format remains parseable');
  intl.setLocale('zh-CN');
  assert(intl.t('pc.nav.home')==='首页','zh-CN UI translation');
  assert(intl.t('common.locale')==='语言','shared setting key');
  console.log('I18N RUNTIME TEST PASSED');
}finally{rmSync(target,{recursive:true,force:true})}
