import fs from 'node:fs';
import path from 'node:path';
const base=process.cwd();
const read=(p)=>fs.readFileSync(path.join(base,p),'utf8');
const targets={
 'apps/mobile/src/components/mine/MineRow.vue':{
  'row px-4 / py-3 / min-h-12':'.mine-row{display:flex;align-items:center;gap:0;padding:24rpx 32rpx;min-height:96rpx',
  'icon w-8 h-8 rounded-lg mr-3':'width:64rpx;height:64rpx;flex:0 0 64rpx;border-radius:12rpx;margin-right:24rpx',
  'original row icon bg is muted':'background:var(--kx-icon-bg,#e9f6fd)',
  'text-sm 14px':'font-size:28rpx;font-weight:400',
  'text-xs 12px':'font-size:24rpx'
 },
 'apps/mobile/src/components/mine/MineGroup.vue':{
  'group title text-xs':'font-size:24rpx;font-weight:400',
  'title ml-1 mb-2':'margin:0 8rpx 16rpx',
  'card rounded-xl':'border-radius:24rpx'
 },
 'apps/mobile/src/pages/me/index.vue':{
  'original bg-background':'background:var(--kx-page-bg,var(--background,#ffffff))',
  'quick icon w-6 h-6':'width:48rpx;height:48rpx',
  'original hero pt-6 px-4 pb-5':'padding:48rpx 32rpx 40rpx',
  'original avatar w-16 h-16':'width:128rpx;height:128rpx'
 },
 'apps/mobile/src/pages/index/index.vue':{
  'country picker source 230rpx':'flex:0 0 230rpx;width:230rpx',
  'search row mt-4 gap-2':'margin-top:32rpx;display:flex;align-items:stretch;gap:16rpx',
  'country control px-3':'padding:0 24rpx',
  'country label text-sm':'font-size:28rpx;font-weight:500',
  'original 90-percent white overlay':'background:rgba(255,255,255,.9)'
 }
};
for (const [file,checks] of Object.entries(targets)) {
 const src=read(file);
 for(const [label,fragment] of Object.entries(checks)){
  if(!src.includes(fragment))throw Error(`${file} : ${label} not aligned`);
  console.log(`OK ${file} :: ${label}`);
 }
}
const pkg=read('package.json');
if(!pkg.includes('test:original-geometry'))throw Error('Original geometry regression not registered');
console.log('Original-source geometry static regression passed (not screenshot parity)');
