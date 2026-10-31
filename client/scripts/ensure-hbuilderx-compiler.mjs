/**
 * HBuilderX CLI project compatibility bridge for a single-install pnpm workspace.
 * pnpm is installed ONLY at the client/ root. Never copy node_modules or install per app.
 * HBuilderX sometimes looks for apps/mobile/node_modules even though Node resolves
 * the compiler from the workspace root. This makes that path available as a link.
 * Existing real directories are preserved (and missing package links are added).
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const mobile = path.join(root, 'apps', 'mobile');
const rootModules = path.join(root, 'node_modules');
const mobileModules = path.join(mobile, 'node_modules');
const manifestFile = path.join(mobile, 'src', 'manifest.json');
const readJson = (p) => JSON.parse(fs.readFileSync(p, 'utf8'));
const checkOnly = process.argv.includes('--check');
const errors = [];

function ensureLink(link, target) {
  if (fs.existsSync(link)) return;
  if (checkOnly) { errors.push(`缺少编译器依赖链接: ${path.relative(root, link)}`); return; }
  fs.mkdirSync(path.dirname(link), { recursive: true });
  const type = process.platform === 'win32' ? 'junction' : 'dir';
  fs.symlinkSync(target, link, type);
  console.log(`  link ${path.relative(root, link)} -> ${path.relative(root, target)}`);
}

if (!fs.existsSync(manifestFile)) errors.push('缺少 apps/mobile/src/manifest.json');
else if (readJson(manifestFile).vueVersion !== '3') errors.push('manifest.json 必须声明 "vueVersion": "3"');

const mobilePkg = readJson(path.join(mobile, 'package.json'));
for (const dep of ['@dcloudio/uni-app', '@dcloudio/uni-h5', '@dcloudio/uni-mp-weixin', '@dcloudio/uni-app-plus', '@dcloudio/vite-plugin-uni', 'vue', 'vite']) {
  if (!(dep in (mobilePkg.dependencies || {})) && !(dep in (mobilePkg.devDependencies || {}))) errors.push(`mobile package.json 缺少 ${dep}`);
}

if (!fs.existsSync(rootModules)) {
  errors.push('尚未找到根目录 node_modules；请在 client 根目录执行一次 pnpm install');
} else {
  const compiler = path.join(rootModules, '@dcloudio', 'vite-plugin-uni');
  if (!fs.existsSync(compiler)) errors.push('根目录缺少 @dcloudio/vite-plugin-uni：请检查 pnpm install 是否成功及锁文件依赖');
  if (!fs.existsSync(mobileModules)) {
    // Full directory junction/symlink: no duplicated node_modules content.
    ensureLink(mobileModules, rootModules);
  } else if (fs.realpathSync(mobileModules) !== fs.realpathSync(rootModules)) {
    // pnpm may have created app-local workspace links; preserve them.
    // Link only absent namespaces/tools without touching existing entries.
    for (const entry of ['@dcloudio', '@vue', 'vue', 'vite', '.bin']) {
      const target = path.join(rootModules, entry);
      if (fs.existsSync(target)) ensureLink(path.join(mobileModules, entry), target);
    }
    // If @dcloudio already exists as a physical directory, fill its missing modules.
    const scoped = path.join(mobileModules, '@dcloudio');
    const rootScoped = path.join(rootModules, '@dcloudio');
    if (fs.existsSync(scoped) && fs.realpathSync(scoped) !== fs.realpathSync(rootScoped)) {
      for (const entry of fs.readdirSync(rootScoped)) {
        const target = path.join(rootScoped, entry);
        if (fs.statSync(target).isDirectory()) ensureLink(path.join(scoped, entry), target);
      }
    }
  }
  for (const pkg of ['@dcloudio/vite-plugin-uni', '@dcloudio/uni-app', '@dcloudio/uni-h5', '@dcloudio/uni-mp-weixin', '@dcloudio/uni-app-plus', 'vue', 'vite']) {
    if (!fs.existsSync(path.join(mobileModules, ...pkg.split('/')))) errors.push(`HBuilderX 无法从 apps/mobile/node_modules 找到 ${pkg}`);
  }
}
if (errors.length) {
  console.error('HBuilderX 编译环境检查未通过:\n- ' + errors.join('\n- '));
  process.exitCode = 1;
} else {
  console.log('✔ HBuilderX: Vue3 编译器标识与本地 node_modules 可见性均通过；依赖仍只安装一份。');
}
