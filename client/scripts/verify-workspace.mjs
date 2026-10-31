import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';

const root = process.cwd();
const read = (file) => JSON.parse(fs.readFileSync(path.join(root,file),'utf8'));
const packages = ['apps/pc','apps/admin','apps/mobile','packages/sdk','packages/ui','packages/i18n','packages/preferences'];
const allowed = new Set(packages.map((p)=>read(`${p}/package.json`).name));
const errors = [];
const rootPackage = read('package.json');
if(!rootPackage.packageManager?.startsWith('pnpm@10.')) errors.push('根目录 packageManager 未锁定 pnpm 10');
if(!fs.existsSync(path.join(root,'pnpm-workspace.yaml'))) errors.push('缺少 pnpm-workspace.yaml');
for(const folder of packages){
  const pkg = read(`${folder}/package.json`);
  for(const [key, version] of Object.entries({...pkg.dependencies,...pkg.devDependencies,...pkg.peerDependencies})){
    if(key.startsWith('@kuaixie/') && (!allowed.has(key) || !String(version).startsWith('workspace:'))) {
      errors.push(`${folder}: 本地依赖 ${key} 应通过 workspace:* 引用`);
    }
  }
}
const requiredScripts=['dev:pc','dev:admin','dev:mobile','build:pc','build:admin','build:mobile','check'];
for(const script of requiredScripts) if(!rootPackage.scripts?.[script]) errors.push(`缺少启动/构建脚本 ${script}`);
const version=(process.versions.node||'0').split('.').map(Number);
if(version[0]<20 || (version[0]===20 && version[1]<19)) errors.push('请使用 Node.js >=20.19 (推荐 Node.js 22)');
if(errors.length){console.error('工作区校验失败：\n- '+errors.join('\n- '));process.exitCode=1}
else console.log('✔ pnpm 工作区结构与内部依赖静态检查通过（不等于已联网安装）。');
