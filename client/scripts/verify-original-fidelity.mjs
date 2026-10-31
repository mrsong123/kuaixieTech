import fs from 'node:fs';
const me=fs.readFileSync(new URL('../apps/mobile/src/pages/me/index.vue',import.meta.url),'utf8');
const required=['mine-hero','mine-quick','mine.groupTrade','mine.groupCommunity','mine.groupInvite','mine.groupOther','mine.publishTrip','mine.publishDemand','mine.buyerKyc','mine.carrierKyc','mine.myProducts','mine.favoritesRow','mine.cartRow','mine.addressRow','mine.invoiceRow','mine.assistRow','mine.communityRow','mine.announceRow','mine.inviteRewards','mine.myTeam','mine.sendInvite','mine.joinApply','mine.appearanceDesc','mine.settingsRow','mine.logout'];
for(const x of required)if(!me.includes(x))throw new Error('原版「我的」结构/元素缺失: '+x);
const grp=fs.readFileSync(new URL('../apps/mobile/src/components/mine/MineGroup.vue',import.meta.url),'utf8');
const row=fs.readFileSync(new URL('../apps/mobile/src/components/mine/MineRow.vue',import.meta.url),'utf8');
if(!grp.includes('<slot') || !row.includes('mine-row'))throw new Error('原版分组组件缺失');
console.log('OK: 原版「我的」页面全部已知结构节点在源码中存在（非像素截图验收）');
