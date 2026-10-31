/**
 * Cross-platform deploy contract (visual-preserving).
 * Taro first-edition is the look reference; UniApp + PC/admin is the runtime.
 * This does not claim WeChat 2MB upload success (needs a real build + 微信开发者工具).
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (p) => fs.readFileSync(path.join(root, p), 'utf8');
const json = (p) => JSON.parse(read(p));
const fail = (m) => {
  throw new Error(m);
};

const mobilePkg = json('apps/mobile/package.json');
const rootPkg = json('package.json');
const manifest = json('apps/mobile/src/manifest.json');
const pages = json('apps/mobile/src/pages.json');
const tokens = read('apps/mobile/src/styles/original-prototype.css');
const appCss = read('apps/mobile/src/styles/app.css');
const plaza = read('apps/mobile/src/pages/index/index.vue');

for (const dep of ['@dcloudio/uni-app', '@dcloudio/uni-h5', '@dcloudio/uni-mp-weixin', '@dcloudio/uni-app-plus']) {
  if (!(dep in (mobilePkg.dependencies || {}))) fail(`mobile 缺少 ${dep}`);
}
for (const script of ['dev:h5', 'build:h5', 'dev:mp-weixin', 'build:mp-weixin', 'dev:app', 'build:app']) {
  if (!mobilePkg.scripts?.[script]) fail(`mobile 缺少脚本 ${script}`);
}
for (const script of ['dev:pc', 'dev:admin', 'dev:mobile', 'dev:mp-weixin', 'dev:app', 'build:pc', 'build:admin', 'build:mobile', 'build:mp-weixin', 'build:app']) {
  if (!rootPkg.scripts?.[script]) fail(`workspace 缺少脚本 ${script}`);
}

if (manifest.vueVersion !== '3') fail('manifest vueVersion 必须为 3');
if (!manifest.h5?.router) fail('manifest 缺少 h5');
if (!manifest['mp-weixin']) fail('manifest 缺少 mp-weixin');
if (manifest['mp-weixin'].lazyCodeLoading !== 'requiredComponents') fail('微信端必须开启 lazyCodeLoading');
if (!manifest['app-plus']) fail('manifest 缺少 app-plus（App 基座）');
if (manifest['app-plus'].statusbar?.style !== 'dark') fail('App 状态栏需保持深色文字以贴合白底视觉');

const tabs = pages.tabBar?.list || [];
if (tabs.length !== 5) fail('tabBar 必须保持五栏');
if (pages.tabBar.color.toLowerCase() !== '#999999') fail('tab 未选中色偏离原版 #999999');
if (pages.tabBar.selectedColor.toLowerCase() !== '#0ea5e9') fail('tab 选中色偏离原版 #0ea5e9');
if (pages.globalStyle.backgroundColor.toLowerCase() !== '#f8fafc') fail('页面底色偏离原版 #f8fafc');
if (tabs[1].iconPath !== 'static/tab-community.png' && tabs[1].iconPath !== 'static/tab-demand.png') fail('第二栏缺少原版 tab 图标');
if (!pages.requiredPrivateInfos?.includes('getLocation')) fail('缺少原版定位隐私声明');
if (!manifest['mp-weixin'].permission?.['scope.userLocation']) fail('微信 manifest 缺少定位权限文案');
if (!tokens.includes('#0ea5e9')) fail('原版主色 token 丢失');
if (!appCss.includes('original-prototype.css')) fail('未接入原版视觉 CSS');
if (/\.kx-reduce-motion \*/.test(appCss) || />\*/.test(tokens)) fail('全局样式含微信不支持的 * 选择器');
if (!plaza.includes('#f8fafc') || !plaza.includes('#0ea5e9')) fail('广场页视觉色板偏离原版');

const mobileApi = read('apps/mobile/src/api/client.ts');
if (!mobileApi.includes('javaUrl') || !mobileApi.includes('/api')) fail('微信/App 传输层必须把公开路径接到 Java /api');
if (!mobileApi.includes('127.0.0.1:8080')) fail('微信/App 默认要指向 kuaixietech-v1/server :8080');
const gateway = read('../server/src/main/java/com/kuaixietech/config/ClientApiGatewayFilter.java');
if (!gateway.includes('"/api" + path')) fail('Java 网关必须给无 Vite 的端补 /api');
const yml = read('../server/src/main/resources/application.yml');
if (!yml.includes('5175') || !yml.includes('cors:')) fail('Java CORS 必须覆盖 H5 5175');
if (!yml.includes('jdbc:mysql://')) fail('后台必须是 MySQL，不是云平台库');
if (!/suixinggou:\s*[\s\S]*cloud-functions:/.test(yml)) fail('云函数配置必须挂在 suixinggou.cloud-functions 才能被 Java 读到');
if (!yml.includes('CLOUD_WECHAT_PAY_ENABLED:false')) fail('微信支付云函数必须默认关闭');
function scanNoCloud(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (['node_modules', 'dist', 'unpackage'].includes(entry.name)) continue;
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) { scanNoCloud(full); continue; }
    if (!/\.(ts|vue|js|mjs|json)$/.test(entry.name)) continue;
    const text = fs.readFileSync(full, 'utf8');
    if (/mp-api\.meoo\.host|supabase-wechat-js|createClient\(/.test(text)) fail('前端仍指向云平台: ' + path.relative(root, full));
  }
}
scanNoCloud(path.join(root, 'apps'));
scanNoCloud(path.join(root, 'packages'));
const pcPkg = json('apps/pc/package.json');
const adminPkg = json('apps/admin/package.json');
if (!pcPkg.scripts?.dev || !adminPkg.scripts?.dev) fail('PC/admin 开发脚本缺失');

const meoo = 'C:\\Users\\sim\\Downloads\\meoo_zip_1793317908757';
if (!fs.existsSync(path.join(meoo, 'src', 'app.css'))) fail('找不到视觉源模板 C:\\Users\\sim\\Downloads\\meoo_zip_1793317908757');
const meooPkg = JSON.parse(fs.readFileSync(path.join(meoo, 'package.json'), 'utf8'));
if (!meooPkg.scripts?.['build:weapp'] || !meooPkg.scripts?.['build:web']) fail('meoo 模板应仍是 Taro weapp+h5，对照用，不在那份目录里加 App');
if (meooPkg.scripts?.['dev:app'] || meooPkg.dependencies?.['@dcloudio/uni-app']) fail('不要把 UniApp 写进 Downloads 那份 Taro 作业');
console.log('OK cross-platform: PC + admin + UniApp H5 + 微信 + App-plus 脚本/清单齐；原版 #f8fafc/#0ea5e9 仍在；视觉源=Downloads meoo');
