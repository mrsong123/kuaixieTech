import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';

const root = path.resolve(import.meta.dirname, '..');
const getJSON = (p) => JSON.parse(fs.readFileSync(path.join(root,p), 'utf8'));
const packages = new Map();
for (const entry of fs.readdirSync(path.join(root,'packages'), {withFileTypes:true})) {
  if (!entry.isDirectory()) continue;
  const packageFile = path.join(root,'packages',entry.name,'package.json');
  if (!fs.existsSync(packageFile)) continue;
  const pkg = JSON.parse(fs.readFileSync(packageFile,'utf8'));
  packages.set(pkg.name, {dir:path.dirname(packageFile),pkg});
  if (pkg.exports) for (const [key,value] of Object.entries(pkg.exports)) {
    const candidates = typeof value === 'string' ? [value] : [];
    for (const dest of candidates) assert.ok(fs.existsSync(path.resolve(path.dirname(packageFile),dest)),`${pkg.name} export ${key} -> ${dest} missing`);
  }
}
let sharedCount=0, aliasCount=0;
function visit(folder,app){
  for (const entry of fs.readdirSync(folder,{withFileTypes:true})) {
    if (entry.isDirectory()) { if (!['dist','node_modules','.git'].includes(entry.name)) visit(path.join(folder,entry.name),app); continue; }
    if (!/\.(vue|ts|tsx|js|mjs)$/.test(entry.name)) continue;
    const full=path.join(folder,entry.name);
    const content=fs.readFileSync(full,'utf8');
    const pattern=/(?:from\s*|import\s*\(\s*|^\s*import\s*)['"](@(?:kuaixie\/[^'"\s]+|\/[^'"\s]+))['"]/gm;
    let match;
    while ((match=pattern.exec(content))) {
      const spec=match[1];
      if (spec.startsWith('@/')) {
        const dest=path.join(root,'apps',app,'src',spec.slice(2));
        assert.ok([dest,`${dest}.ts`,`${dest}.tsx`,`${dest}.vue`,`${dest}.json`,path.join(dest,'index.ts')].some(fs.existsSync),`${app}: alias ${spec} not found in ${entry.name}`);
        aliasCount++;
      } else {
        const [scope,name,...segments]=spec.split('/');
        const packageName=`${scope}/${name}`;
        const found=packages.get(packageName);
        assert.ok(found,`workspace package ${packageName} not found in ${full}`);
        const subpath=segments.length ? `./${segments.join('/')}` : '.';
        assert.ok(found.pkg.exports && Object.hasOwn(found.pkg.exports,subpath),`Missing ${subpath} specifier in ${packageName} (imported by ${path.relative(root,full)})`);
        sharedCount++;
      }
    }
  }
}
for (const app of ['pc','admin','mobile']) {
  const cfg=fs.readFileSync(path.join(root,'apps',app,'vite.config.ts'),'utf8');
  assert.match(cfg,/alias:\s*\{\s*'@':\s*fileURLToPath/,`${app}: Vite alias @ missing`);
  const ts=getJSON(`apps/${app}/tsconfig.json`);
  assert.deepEqual(ts.compilerOptions.paths['@/*'],['src/*'],`${app}: TS alias @/* missing`);
  visit(path.join(root,'apps',app,'src'),app);
}
console.log(`OK: ${sharedCount} workspace package imports resolve via exports; ${aliasCount} app-local @/ imports resolve; all 3 Vite + TS aliases configured`);
