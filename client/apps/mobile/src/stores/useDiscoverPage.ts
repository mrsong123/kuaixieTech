
import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';


import {ref} from 'vue';
import {api,showError,navigate} from '../api/client';

import {t} from '@kuaixie/i18n';

export function useDiscoverPage(){

const prefs=preferenceRef;
const current=ref<'life'|'shop'|'route'>('life');const postId=ref('');const content=ref('');const result=ref('');const busy=ref(false);
async function addComment(){busy.value=true;result.value='';try{const data=await api.rpc('batch12/add_post_comment',{postId:postId.value,content:content.value});result.value=String(data);uni.showToast({title:t('mobile.backendResponse'),icon:'none'})}catch(e){showError(e)}finally{busy.value=false}}

return {prefs,current,postId,content,result,busy,addComment,mobilePreferenceStyle,navigate};
}
