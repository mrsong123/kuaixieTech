import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {getBearer,type SupportMessage,type SupportConversation} from '@kuaixie/sdk';
import {api} from '../api/client';
import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {t} from '@kuaixie/i18n';
export function useSupportSessionPage(){
const prefs=preferenceRef,id=ref(''),conversation=ref<SupportConversation|null>(null),messages=ref<SupportMessage[]>([]),draft=ref(''),busy=ref(false),loading=ref(false),error=ref('');
onLoad((params)=>{id.value=String(params?.id||'');void load()});
async function load(){if(!getBearer()){error.value=t('account.signinRequired');return}if(!id.value){error.value=t('migration.invalidConversation');return}loading.value=true;error.value='';try{const [session,rows]=await Promise.all([api.support.get(id.value),api.support.messages(id.value,60)]);conversation.value=session;messages.value=rows}catch(e){error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{loading.value=false}}
async function send(){const content=draft.value.trim();if(!content||busy.value)return;busy.value=true;error.value='';try{await api.support.send(id.value,content);draft.value='';await load()}catch(e){error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{busy.value=false}}
async function handoff(){if(busy.value||!id.value)return;busy.value=true;error.value='';try{conversation.value=await api.support.handoff(id.value);await load()}catch(e){error.value=e instanceof Error?e.message:t('common.apiLoadError')}finally{busy.value=false}}
return {prefs,id,conversation,messages,draft,busy,loading,error,load,send,handoff,mobilePreferenceStyle};
}
