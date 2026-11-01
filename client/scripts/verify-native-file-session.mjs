import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {execFileSync} from 'node:child_process';

const root=path.resolve(import.meta.dirname,'..'),require=createRequire(import.meta.url),ts=require('typescript');
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'kx-native-file-session-'));
const previousUni=globalThis.uni;
let assertions=0;
function check(value,message){assert.ok(value,message);assertions++;console.log('OK:',message)}
function deferred(){let resolve;const promise=new Promise(done=>{resolve=done});return {promise,resolve}}
const observe=promise=>promise.then(value=>({value}),error=>({error}));
const flush=()=>new Promise(resolve=>setTimeout(resolve,0));

try{
 const sdkSource=path.join(root,'packages/sdk/src'),sdkOut=path.join(tmp,'sdk');
 execFileSync(process.execPath,[path.join(root,'node_modules/typescript/bin/tsc'),'--target','ES2022','--module','CommonJS','--moduleResolution','node','--skipLibCheck','--outDir',sdkOut,...fs.readdirSync(sdkSource).filter(name=>name.endsWith('.ts')).map(name=>path.join(sdkSource,name))],{stdio:'pipe'});
 const sdkFile=path.join(sdkOut,'index.js'),sdk=require(sdkFile);
 const locale=path.join(tmp,'locale.cjs'),router=path.join(tmp,'router.cjs');
 fs.writeFileSync(locale,`exports.t=key=>key;exports.getLocale=()=> 'zh-CN';exports.uiErrorMessage=error=>error.message;`);
 fs.writeFileSync(router,'exports.navigate=()=>{};');
 let source=fs.readFileSync(path.join(root,'apps/mobile/src/api/client.ts'),'utf8').replaceAll('import.meta.env.VITE_API_BASE_URL',JSON.stringify('https://api.example.test'));
 let code=ts.transpileModule(source,{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS}}).outputText;
 for(const [alias,target] of [['@kuaixie/sdk',sdkFile],['@kuaixie/i18n',locale],['../router',router]])code=code.replaceAll(`require("${alias}")`,`require(${JSON.stringify(target)})`);

 let picked=()=>Promise.resolve({tempFilePaths:['/tmp/selected.png']});
 let uploads=[],downloads=[],previews=[],documents=[];
 globalThis.uni={
  request(){throw new Error('The native file tests must not send ordinary API requests')},
  chooseImage:()=>picked(),
  uploadFile:options=>{uploads.push(options)},
  downloadFile:options=>{downloads.push(options)},
  previewImage:options=>{previews.push(options);options.success?.({})},
  openDocument:options=>{documents.push(options);options.success?.({})},
  showModal(){throw new Error('Unexpected modal')},
 };
 const mobileFile=path.join(tmp,'mobile.cjs');fs.writeFileSync(mobileFile,code);
 const mobile=require(mobileFile),id='123e4567-e89b-42d3-a456-426614174000';
 const receipt={id,purpose:'product_image',contentType:'image/png',byteSize:20,sha256:'a'.repeat(64),publicPath:'/public/media/'+id};
 function reset(){sdk.setBearer('account-a');picked=()=>Promise.resolve({tempFilePaths:['/tmp/selected.png']});uploads=[];downloads=[];previews=[];documents=[]}
 function old(result){return result.error?.message==='upload.sessionChanged'}

 reset();
 const uploaded=observe(mobile.uploadPrivateImage('product_image'));
 await flush();
 check(uploads.length===1&&uploads[0].header.Authorization==='Bearer account-a'&&uploads[0].url==='https://api.example.test/api/me/uploads?purpose=product_image','A normal upload uses the current authenticated header and native API rewrite');
 uploads[0].success({statusCode:200,data:JSON.stringify(receipt)});
 check((await uploaded).value.id===id,'An unchanged session receives its confirmed upload receipt');

 reset();
 const choose=deferred();picked=()=>choose.promise;let selectedCalls=0;
 const oldPicker=observe(mobile.uploadPrivateImage('product_image',{onSelected:()=>{selectedCalls++}}));
 sdk.setBearer('account-b');choose.resolve({tempFilePaths:['/tmp/old.png']});
 check(old(await oldPicker)&&uploads.length===0&&selectedCalls===0,'A late native image-picker result cannot upload or attach a previous account image');

 reset();
 const selectedChanged=observe(mobile.uploadPrivateImage('product_image',{onSelected:()=>sdk.setBearer('account-b')}));
 check(old(await selectedChanged)&&uploads.length===0,'Changing accounts in the selection callback prevents the upload from being sent');

 reset();
 const lateUpload=observe(mobile.uploadPrivateImage('product_image'));await flush();
 sdk.setBearer('account-b');uploads[0].success({statusCode:200,data:JSON.stringify(receipt)});
 check(old(await lateUpload),'A late upload acknowledgement is rejected after switching accounts');

 reset();
 const returnedAccountUpload=observe(mobile.uploadPrivateImage('product_image'));await flush();
 sdk.setBearer('account-b');sdk.setBearer('account-a');uploads[0].success({statusCode:200,data:JSON.stringify(receipt)});
 check(old(await returnedAccountUpload),'Returning to the original token still rejects an upload from a previous session epoch');

 reset();
 const downloaded=observe(mobile.downloadPrivateFile(id,'product_image'));
 check(downloads.length===1&&downloads[0].header.Authorization==='Bearer account-a'&&downloads[0].url==='https://api.example.test/api/me/uploads/'+id+'/content','An authenticated private download keeps credentials in headers and uses the file content route');
 downloads[0].success({statusCode:200,tempFilePath:'/tmp/downloaded.png'});
 check((await downloaded).value==='/tmp/downloaded.png','An unchanged session can use its private local file');

 reset();
 const lateDownload=observe(mobile.downloadPrivateFile(id));
 sdk.clearBearer();downloads[0].success({statusCode:200,tempFilePath:'/tmp/old.pdf'});
 check(old(await lateDownload),'Logging out before download completion rejects the old private file path');

 reset();
 const returnedAccountDownload=observe(mobile.downloadPrivateFile(id));
 sdk.setBearer('account-b');sdk.setBearer('account-a');downloads[0].success({statusCode:200,tempFilePath:'/tmp/old.pdf'});
 check(old(await returnedAccountDownload),'Returning to the same account does not revive a download from an old epoch');

 reset();
 const pdf=observe(mobile.viewPrivateDocument(id,'application/pdf'));
 downloads[0].success({statusCode:200,tempFilePath:'/tmp/current.pdf'});
 check(!(await pdf).error&&documents.length===1&&documents[0].filePath==='/tmp/current.pdf'&&previews.length===0,'A current private PDF opens only through the authorized local download');

 reset();
 const image=observe(mobile.viewPrivateDocument(id,'image/png'));
 downloads[0].success({statusCode:200,tempFilePath:'/tmp/current.png'});
 check(!(await image).error&&previews.length===1&&previews[0].urls[0]==='/tmp/current.png'&&documents.length===0,'A current private image previews the downloaded local file');

 for(const mime of ['application/pdf','image/png']){
  reset();const expired=observe(mobile.viewPrivateDocument(id,mime));
  sdk.setBearer('account-b');downloads[0].success({statusCode:200,tempFilePath:'/tmp/old-private-file'});
  check(old(await expired)&&previews.length===0&&documents.length===0,`A stale ${mime} download cannot open a viewer under a different account`);
 }

 sdk.clearBearer();
 const publicUrl=mobile.publicMediaUrl('/public/media/'+id);
 sdk.setBearer('account-b');
 check(publicUrl==='https://api.example.test/api/public/media/'+id&&mobile.publicMediaUrl('/public/media/'+id)===publicUrl&&!/token|Authorization|Bearer/.test(publicUrl),'Public image URLs remain anonymous and independent of private session guards');

 console.log(`NATIVE FILE SESSION CHECK PASSED: ${assertions} assertions`);
}finally{
 globalThis.uni=previousUni;
 fs.rmSync(tmp,{recursive:true,force:true});
}
