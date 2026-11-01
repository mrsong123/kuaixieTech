import {ref,proxyRefs} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {api} from '../api/client';
import type {PublicCommunityPost} from '@kuaixie/sdk';
import {formatDate,t} from '@kuaixie/i18n';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
/** No fake likes/comments; detail is from the same real public feed used in plaza. */
export function useOriginalCommunityDetail(){
 const prefs=preferenceRef,id=ref(''),post=ref<PublicCommunityPost|null>(null),loading=ref(false),error=ref('');
 async function load(){loading.value=true;error.value='';try{
 const items=await api.portal.communityFeed(100);
 if(!Array.isArray(items))throw new Error(t('migration.invalidResponse'));
 post.value=items.find(x=>String(x.id)===id.value)||null;
 if(!post.value&&items.length>=100)error.value=t('v14.communityWindow');
 }catch(e){post.value=null;error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{loading.value=false}}
 function safeUrl(value?:string){return value&&/^https:\/\/[^\s/?#]+(?:[/?#]|$)/i.test(value)?value:''}
 onLoad(params=>{id.value=String(params?.id||'');if(!id.value){error.value=t('v14.badId');return}void load()});
 return proxyRefs({prefs,id,post,loading,error,load,safeUrl,formatDate,mobilePreferenceStyle});
}
