<script setup lang="ts">
import OriginalMdiIcon from '../OriginalMdiIcon.vue';
import {computed,onMounted,ref} from 'vue';
import {api} from '../../api/client';
import {getLocale,localizedField,t} from '@kuaixie/i18n';
import type {PublicCategory} from '@kuaixie/sdk';
import {localeRef} from '@kuaixie/i18n/vue';
const props=defineProps<{country:string;query?:string;compact?:boolean;focus?:string[]}>();
const all=ref<PublicCategory[]>([]);
const loading=ref(false),failed=ref(false);
const first=ref(''),second=ref('');
// The locale ref triggers translation recalculation immediately; never fabricate translated business names.
const language=localeRef;
const label=(item:PublicCategory)=>{void language.value;return localizedField(item as unknown as Record<string,unknown>,'name')};
const visible=computed(()=>{
 const byId=new Map(all.value.map(x=>[String(x.id),x]));
 const chain=(row:PublicCategory)=>{const nodes=[row];let parent=row.parent_id;while(parent!=null&&byId.has(String(parent))){const next=byId.get(String(parent));if(!next)break;nodes.push(next);parent=next.parent_id}return nodes};
 let rows=all.value.filter(row=>!props.country||chain(row).some(node=>String(node.country_code||'').toUpperCase()===props.country.toUpperCase()));
 const q=(props.query||'').trim().toLocaleLowerCase();
 const focus=new Set((props.focus||[]).filter(Boolean));
 if(!q&&!focus.size)return rows;
 const scoped=new Map(rows.map(x=>[String(x.id),x]));
 const keep=new Set<string>();
 for(const row of rows){
  if(!(q&&label(row).toLocaleLowerCase().includes(q))&&!focus.has(String(row.id)))continue;
  chain(row).forEach(node=>{if(scoped.has(String(node.id)))keep.add(String(node.id))});
 }
 return rows.filter(x=>keep.has(String(x.id)));
});
const roots=computed(()=>visible.value.filter(x=>x.parent_id==null||!visible.value.some(y=>String(y.id)===String(x.parent_id))));
const children=(parent:string)=>visible.value.filter(x=>String(x.parent_id??'')===parent);
const chosenRoot=computed(()=>roots.value.find(x=>String(x.id)===first.value));
const chosenSecond=computed(()=>children(first.value).find(x=>String(x.id)===second.value));
const lower=computed(()=>chosenSecond.value?children(String(chosenSecond.value.id)):[]);
async function reload(){loading.value=true;failed.value=false;try{all.value=await api.catalog.categories(100)}catch{all.value=[];failed.value=true}finally{loading.value=false}}
function selectRoot(id:string){first.value=first.value===id?'':id;second.value=''}
function selectChild(id:string){second.value=second.value===id?'':id}
function goCategory(item:PublicCategory){uni.navigateTo({url:'/pages/catalog/index?category='+encodeURIComponent(String(item.id))})}
onMounted(reload);
</script>
<template>
  <view class="original-category-explorer">
    <view v-if="!compact" class="original-section-heading"><text>{{ $t('home.byCategoryTrips') }}</text><text class="section-side" @click="reload">{{ $t('portal.community.refresh') }}</text></view>
    <scroll-view scroll-x class="original-category-scroll"><view class="category-chips">
      <view class="category-chip" :class="{chosen:!first}" @click="selectRoot('')">{{ $t('common.all') }}</view>
      <view v-for="root in roots" :key="String(root.id)" class="category-chip" :class="{chosen:first===String(root.id)}" @click="selectRoot(String(root.id))">{{label(root)}}</view>
    </view></scroll-view>
    <view v-if="failed" class="category-notice" @click="reload">{{ $t('common.apiLoadError') }} · {{ $t('common.retry') }}</view>
    <view v-else-if="loading" class="category-notice">{{ $t('portal.square.loading') }}</view>
    <view v-else-if="!roots.length && !compact" class="category-notice">{{ $t('prototype.plaza.categoryEmpty') }}</view>
    <template v-if="chosenRoot && children(String(chosenRoot.id)).length">
      <text class="category-hint">{{ $t('home.subdivisionHint') }}</text>
      <view class="category-grid subcategory-grid">
        <view v-for="child in children(String(chosenRoot.id))" :key="String(child.id)" class="subcategory-card" :class="{chosen:second===String(child.id)}" @click="selectChild(String(child.id))">
          <view class="subcategory-label">{{label(child)}}</view>
          <text class="subcategory-detail">{{children(String(child.id)).slice(0,3).map(label).join(' · ')}}</text>
        </view>
      </view>
      <view v-if="lower.length" class="category-chips leaf-chips">
        <view v-for="leaf in lower" :key="String(leaf.id)" class="category-chip" @click="goCategory(leaf)">{{label(leaf)}}</view>
      </view>
    </template>
    <view v-if="!compact" class="original-section-heading hot-heading"><text>{{ $t('home.hotCategories') }}</text><text class="section-side">{{roots.length}}</text></view>
    <view v-if="!compact" class="category-grid hot-grid">
      <view v-for="root in roots.slice(0,8)" :key="String(root.id)" class="hot-card" @click="goCategory(root)">
        <view class="hot-name"><view class="hot-symbol"><OriginalMdiIcon name="shape-outline"/></view><text class="hot-label">{{label(root)}}</text><text class="hot-arrow">›</text></view>
        <text class="hot-desc">{{children(String(root.id)).slice(0,3).map(label).join(' · ')||$t('prototype.plaza.categoryBrowse')}}</text>
      </view>
    </view>
  </view>
</template>
<style scoped>
/* Direct migration of original index.tsx Tailwind values: px-4/p-3/gap-2/rounded-xl/text-xs. */
.original-category-explorer{color:var(--foreground,#0f172a)}
.original-section-heading{display:flex;align-items:center;justify-content:space-between;gap:16rpx;margin:30rpx 0 18rpx;font-size:29rpx;font-weight:700;line-height:1.4}.section-side{font-size:22rpx;font-weight:400;color:var(--muted-foreground,#64748b)}
.original-category-scroll{width:100%;white-space:nowrap;min-height:66rpx}.category-chips{display:flex;gap:14rpx;align-items:center;padding:4rpx 0 12rpx;white-space:nowrap}.category-chip{padding:12rpx 23rpx;flex-shrink:0;border:1rpx solid var(--border,#e2e8f0);background:var(--card,#fff);border-radius:99rpx;font-size:23rpx;color:var(--muted-foreground,#64748b);max-width:310rpx;white-space:normal;overflow-wrap:anywhere}.category-chip.chosen{background:var(--primary,#0ea5e9);color:#fff;border-color:var(--primary,#0ea5e9)}
.category-grid{display:flex;flex-wrap:wrap;gap:14rpx;align-items:stretch}.category-hint{display:block;color:var(--muted-foreground,#64748b);font-size:22rpx;margin:13rpx 0}.subcategory-card{width:calc(33.333% - 10rpx);min-width:0;padding:16rpx;border:1rpx solid var(--border,#e2e8f0);border-radius:13rpx;background:var(--card,#fff)}.subcategory-card.chosen{border-color:var(--primary,#0ea5e9);background:var(--primary-08,#f0f9ff)}.subcategory-label{font-size:23rpx;font-weight:600;overflow-wrap:anywhere;line-height:1.4}.subcategory-detail{font-size:20rpx;line-height:1.45;margin-top:7rpx;color:var(--muted-foreground,#64748b);overflow-wrap:anywhere}.leaf-chips{margin-top:15rpx;flex-wrap:wrap;white-space:normal}
.hot-heading{margin-top:14rpx}.hot-grid{flex-wrap:nowrap;overflow-x:auto;gap:10rpx}.hot-card{width:auto;flex:0 0 auto;max-width:280rpx;border:1rpx solid var(--border,#e2e8f0);background:var(--card,#fff);border-radius:17rpx;padding:12rpx 16rpx}.hot-desc{display:none}.original-section-heading{margin:14rpx 0 8rpx}.hot-name{display:flex;align-items:center;gap:10rpx;font-size:25rpx;font-weight:600;line-height:1.35}.hot-symbol{color:var(--primary,#0ea5e9);font-size:28rpx}.hot-symbol{width:28rpx;height:28rpx;flex:0 0 28rpx}.hot-label{flex:1;min-width:0;overflow-wrap:anywhere}.hot-arrow{font-size:29rpx;color:var(--muted-foreground,#64748b)}.hot-desc{display:block;margin-top:9rpx;font-size:20rpx;color:var(--muted-foreground,#64748b);line-height:1.45;overflow-wrap:anywhere}.category-notice{font-size:22rpx;line-height:1.6;color:var(--muted-foreground,#64748b);padding:18rpx}
.kx-dark .hot-card,.kx-dark .subcategory-card,.kx-dark .category-chip{background:#202629;border-color:#475569;color:#f2f4f4}
@media(max-width:355px){.subcategory-card{width:calc(50% - 8rpx)}}
</style>
