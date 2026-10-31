
import {computed,onMounted,ref} from 'vue';

import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle,prototypeAccent} from '../utils/prototype';

import type {PublicCommunityPost} from '@kuaixie/sdk';

import {api,navigate} from '../api/client';

import {formatDate} from '@kuaixie/i18n';

export function useDemandPoolPage(){

const prefs=preferenceRef;
const style=computed(()=>mobilePreferenceStyle(prefs.value)+`--prototype-accent:${prototypeAccent(prefs.value.accent)};`);
const posts=ref<PublicCommunityPost[]>([]),loading=ref(false),error=ref(false);
const category=ref<'all'|'video'|'image'>('all');
const shown=computed(()=>posts.value.filter(item=>category.value==='all'||(category.value==='video'?item.media_kind==='video':item.media_kind!=='video')));
function safeUrl(value?:string):string{
 if(!value||!/^https:\/\/[^\s/?#]+(?:[/?#]|$)/i.test(value))return '';
 return value;
}
async function reload(){loading.value=true;error.value=false;
 try{posts.value=await api.portal.communityFeed(60)}catch{posts.value=[];error.value=true}finally{loading.value=false}}
onMounted(reload);
function publish(){navigate('publish-hub')}
function openDetail(post:PublicCommunityPost){uni.navigateTo({url:'/pages/community-detail/index?id='+encodeURIComponent(String(post.id))})}

return {prefs,style,posts,loading,error,category,shown,safeUrl,reload,publish,openDetail,formatDate};
}
