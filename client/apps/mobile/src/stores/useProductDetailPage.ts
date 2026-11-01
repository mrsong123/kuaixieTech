import {useProductActions} from '@kuaixie/ui/useSavedCommerce';
import {ref,computed} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {api} from '../api/client';
import {mobilePreferenceStyle} from '../utils/prototype';
import {mobileProductName,mobileProductPicture,mobileProductImages} from '../utils/catalog';
import {formatAmount} from '@kuaixie/i18n';
import type {PublicProduct} from '@kuaixie/sdk';
export function useProductDetailPage(){
 const prefs=preferenceRef;const images=computed(()=>item.value?mobileProductImages(item.value):[]);
 const item=ref<PublicProduct|null>(null);const id=ref('');const loading=ref(false);const unavailable=ref(false);
 const actions=useProductActions(api,()=>id.value);
 async function load(){loading.value=true;unavailable.value=false;try{item.value=await api.catalog.product(id.value);}catch{item.value=null;unavailable.value=true}finally{loading.value=false}}
 onLoad((params)=>{id.value=String(params?.id??'');void load()});
 function preview(url:string){if(images.value.includes(url))uni.previewImage({current:url,urls:images.value})}
 return {actions,prefs,item,images,preview,loading,unavailable,mobilePreferenceStyle,mobileProductName,mobileProductPicture,mobileProductImages,formatAmount,load};
}
