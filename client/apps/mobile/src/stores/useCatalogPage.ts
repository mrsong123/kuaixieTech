
import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';


import {computed,onMounted,ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';

import {api,showError} from '../api/client';

import {catalogMatch,type PublicProduct,type PublicCategory} from '@kuaixie/sdk';

import {formatAmount,localizedField} from '@kuaixie/i18n';

import {mobileProductName,mobileProductPicture,mobileProductDetails} from '../utils/catalog';

export function useCatalogPage(){

const prefs=preferenceRef;
const products=ref<PublicProduct[]>([]);const categories=ref<PublicCategory[]>([]);
const pending=ref(false);const failed=ref(false);const query=ref('');const category=ref<string|null>(null);
const shown=computed(()=>products.value.filter(p=>catalogMatch(p,query.value,category.value)));
function categoryName(value:PublicCategory){return localizedField(value as unknown as Record<string,unknown>,'name')}
async function reload(){pending.value=true;failed.value=false;const results=await Promise.allSettled([api.catalog.products(100),api.catalog.categories(100)]);if(results[0].status==='fulfilled')products.value=results[0].value;else {products.value=[];failed.value=true}if(results[1].status==='fulfilled')categories.value=results[1].value;else {categories.value=[];failed.value=true}pending.value=false}
function open(p:PublicProduct){uni.navigateTo({url:'/pages/product-detail/index?id='+encodeURIComponent(String(p.id))})}
function back(){uni.navigateBack()}
onLoad((options)=>{const c=options?.category;if(c&&typeof c==='string')category.value=c});
onMounted(reload);

return {prefs,products,categories,pending,failed,query,category,shown,categoryName,reload,open,back,mobilePreferenceStyle,formatAmount,mobileProductName,mobileProductPicture};
}
