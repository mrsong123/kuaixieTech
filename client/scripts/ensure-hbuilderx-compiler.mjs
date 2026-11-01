/** Resolve and expose this workspace's pinned Vue3/Vite compiler to HBuilderX. No package downloads. */
import fs from 'node:fs';
import path from 'node:path';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';

export const UNI_COMPILER_PACKAGES=[
 '@dcloudio/vite-plugin-uni','@dcloudio/uni-cli-shared','@dcloudio/uni-i18n',
 '@dcloudio/uni-app','@dcloudio/uni-h5','@dcloudio/uni-mp-weixin','@dcloudio/uni-app-plus',
 '@dcloudio/uni-app-vite','@dcloudio/uni-h5-vite','@dcloudio/uni-mp-vite','@dcloudio/uni-mp-compiler'
];
export function supportedNode(version){const [major,minor]=version.split('.').map(Number);return major>22||major===22&&minor>=12}
const exists=p=>{try{return fs.statSync(p).isDirectory()||fs.statSync(p).isFile()}catch{return false}};
const json=p=>JSON.parse(fs.readFileSync(p,'utf8'));
function lstat(p){try{return fs.lstatSync(p)}catch{return null}}
function internal(parent,value){const rel=path.relative(parent,value);return rel===''||rel!=='..'&&!rel.startsWith('..'+path.sep)&&!path.isAbsolute(rel)}

export function inspectCompiler(root,{repair=false,nodeVersion=process.versions.node}={}){
 const mobile=path.join(root,'apps/mobile'),modules=path.join(mobile,'node_modules'),rootModules=path.join(root,'node_modules');
 const errors=[],actions=[],resolved=[];
 if(!supportedNode(nodeVersion))errors.push(`Node ${nodeVersion} 不兼容当前DCloud编译器；需要 >=22.12.0，建议Node 22 LTS或24 LTS。HBuilderX也须使用同一Node。`);
 const manifest=path.join(mobile,'src/manifest.json'),pkgFile=path.join(mobile,'package.json');
 if(!exists(manifest)||!exists(pkgFile))return {errors:[...errors,'工程结构不完整：请在client安装工作区，并在HBuilderX导入client/apps/mobile（不是src或client）。'],actions,resolved};
 try{if(json(manifest).vueVersion!=='3')errors.push('src/manifest.json必须声明vueVersion为3；不要安装Vue2/Webpack编译器。')}catch{errors.push('src/manifest.json不是有效JSON')}
 const pkg=json(pkgFile),expected=pkg.devDependencies?.['@dcloudio/vite-plugin-uni'];
 if(!/^3\.0\.0-\d+$/.test(expected||''))errors.push('mobile必须精确锁定DCloud Vue3/Vite编译器版本');
 if(!exists(rootModules)||!fs.statSync(rootModules).isDirectory())return {errors:[...errors,'缺少client/node_modules；在client执行pnpm install --frozen-lockfile，不要在mobile执行npm install。'],actions,resolved};
 const modulesStat=lstat(modules);
 if(modulesStat&&!exists(modules))return {errors:[...errors,'apps/mobile/node_modules为失效链接；请移走失效链接后在client重新安装，工具不会删除外部依赖链接。'],actions,resolved};
 if(repair&&modulesStat?.isSymbolicLink()&&fs.realpathSync(modules)===fs.realpathSync(rootModules)){fs.unlinkSync(modules);fs.mkdirSync(modules,{recursive:true});actions.push('apps/mobile/node_modules（替换旧整目录链接为逐包入口，保留根依赖）')}
 else if(modulesStat&&!modulesStat.isSymbolicLink()&&!modulesStat.isDirectory())return {errors:[...errors,'apps/mobile/node_modules不是目录；请保留现文件并人工核对安装路径。'],actions,resolved};
 const appRequire=createRequire(pkgFile),rootRequire=createRequire(path.join(root,'package.json'));
 function resolveManifest(name,reqs=[appRequire,rootRequire]){for(const req of reqs)try{return req.resolve(name+'/package.json')}catch{}return null}
 const compilerFile=resolveManifest('@dcloudio/vite-plugin-uni');
 const compilerRequire=compilerFile?createRequire(compilerFile):rootRequire;
 function expose(name,target){try{exposeEntry(name,target)}catch(error){errors.push(`无法修复${name}入口（${error.code||'FILESYSTEM_ERROR'}）；保留现有文件，请核对路径和目录权限。`)}}
 function exposeEntry(name,target){
  const dest=path.join(modules,...name.split('/')),stat=lstat(dest);
  if(stat&&exists(dest)&&path.resolve(fs.realpathSync(dest))===path.resolve(fs.realpathSync(target)))return;
  if(stat){
   // Repair only links created inside this workspace; never delete a real dependency directory or external link.
   if(!stat.isSymbolicLink()){if(!exists(dest))errors.push(`${name}路径损坏；请重装client依赖`);return}
   const oldTarget=path.resolve(path.dirname(dest),fs.readlinkSync(dest));
   if(!internal(rootModules,oldTarget)){errors.push(`保留外部链接 ${path.relative(root,dest)}；请人工核对后重装client依赖`);return}
   if(!repair){errors.push(`编译器链接失效或指向不一致：${path.relative(root,dest)}；执行pnpm setup:mobile`);return}
   fs.unlinkSync(dest);
  }else if(!repair){errors.push(`HBuilderX源码根缺少物理入口 ${path.relative(root,dest)}；执行pnpm setup:mobile`);return}
  fs.mkdirSync(path.dirname(dest),{recursive:true});
  fs.symlinkSync(target,dest,process.platform==='win32'?'junction':'dir');
  actions.push(path.relative(root,dest));
 }
 for(const name of [...UNI_COMPILER_PACKAGES,'vue','vite']){
  const preferred=name==='vite'?[compilerRequire,appRequire,rootRequire]:[appRequire,rootRequire,compilerRequire];
  const expectedVersion=name==='vite'?pkg.devDependencies?.vite:name.startsWith('@dcloudio/')?expected:null;
  const candidates=[...new Set(preferred.map(req=>resolveManifest(name,[req])).filter(Boolean))];
  const file=candidates.find(file=>!expectedVersion||json(file).version===expectedVersion)||candidates[0];
  if(!file){errors.push(`缺少${name}；在client重新执行pnpm install --frozen-lockfile`);continue}
  const data=json(file);
  if(expectedVersion&&data.version!==expectedVersion){errors.push(`${name}版本${data.version}与锁定${expectedVersion}不一致；不要混装编译器版本`);continue}
  if(name==='vue'&&!String(data.version).startsWith('3.')){errors.push('当前工程需要Vue3');continue}
  const dir=path.dirname(file);
  const main=data['uni-app']?.main||data.main;
  if(main&&!exists(path.resolve(dir,main))){errors.push(`${name}缺少实际编译入口${main}；安装未完成或依赖已损坏`);continue}
  if(name==='@dcloudio/vite-plugin-uni'&&!exists(path.join(dir,'bin/uni.js'))){errors.push('缺少vite-plugin-uni/bin/uni.js');continue}
  expose(name,dir);
  const visible=path.join(modules,...name.split('/'),'package.json');
  if(!exists(visible)){errors.push(`HBuilderX无法直接读取${path.relative(root,visible)}`);continue}
  const visiblePkg=json(visible);
  if(visiblePkg.name!==name||visiblePkg.version!==data.version){errors.push(`HBuilderX物理入口与Node解析结果不一致：${name}`);continue}
  resolved.push({name,version:data.version,entry:path.relative(root,visible)});
 }
 const rootBin=path.join(rootModules,'.bin');
 if(exists(rootBin)){expose('.bin',rootBin);if(!['uni','uni.cmd'].some(file=>exists(path.join(modules,'.bin',file))))errors.push('mobile/node_modules/.bin缺少uni命令；请重新安装工作区依赖')}
 else errors.push('client/node_modules/.bin缺失；请重新安装工作区依赖');
 return {errors:[...new Set(errors)],actions,resolved};
}

if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){
 const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
 const repair=!process.argv.includes('--check');
 const result=inspectCompiler(root,{repair});
 console.log(`Node ${process.versions.node}; executable: ${process.execPath}`);
 for(const action of result.actions)console.log('已修复入口: '+action);
 if(result.errors.length){console.error('MOBILE_COMPILER_NOT_READY\n- '+result.errors.join('\n- '));process.exitCode=1}
 else console.log(`MOBILE_COMPILER_READY: ${result.resolved.length}个真实编译入口可读，DCloud版本一致；本机检查不代表Windows/HBuilderX GUI已验收。`);
}
