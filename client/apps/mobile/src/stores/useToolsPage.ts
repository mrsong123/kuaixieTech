
import {preferenceRef} from '@kuaixie/preferences/vue';

import {mobilePreferenceStyle} from '../utils/prototype';


import {ref,reactive,computed,watch} from 'vue';

import {t,operationLabel,groupLabel,uiErrorMessage} from '@kuaixie/i18n';

import {operations,type ApiOperation,type ApiField,ApiError} from '@kuaixie/sdk';

import {api,showError} from '../api/client';

export function useToolsPage(){

const prefs=preferenceRef;
const search=ref('');const group=ref('__all__');const chosen=ref('');const working=ref(false);const payload=reactive<Record<string,string>>({});const raw=ref('[]');const result=ref('');const error=ref('');
const categories=['__all__',...new Set(operations.map(o=>o.group))];
const filtered=computed(()=>operations.filter(o=>(group.value==='__all__'||o.group===group.value)&&(o.name+o.path).toLowerCase().includes(search.value.toLowerCase())));
const selected=computed(()=>operations.find(o=>o.id===chosen.value));
function open(op:ApiOperation){chosen.value=op.id;result.value='';error.value='';Object.keys(payload).forEach(k=>delete payload[k]);for(const f of op.bodyFields)payload[f.name]=f.type==='boolean'?'false':'';for(const k of [...op.pathParams,...op.queryParams])payload[k]='';raw.value='[]'}
function convert(f:ApiField){const v=payload[f.name]??'';if(f.type==='number')return v.trim()?Number(v):null;if(f.type==='boolean')return v==='true';if(f.type==='array')return JSON.parse(v||'[]');return v}
async function invoke(){const op=selected.value;if(!op||working.value)return;if(op.role==='admin'){uni.showModal({title:t('common.adminOnlyTitle'),content:t('common.adminOnlyDescription'),showCancel:false});return;}const doCall=()=>{working.value=true;error.value='';result.value='';const pathParams=Object.fromEntries(op.pathParams.map(x=>[x,payload[x]]));const query=Object.fromEntries(op.queryParams.filter(x=>payload[x]).map(x=>[x,payload[x]]));let body:unknown={};try{body=op.rawBody?JSON.parse(raw.value):Object.fromEntries(op.bodyFields.map(f=>[f.name,convert(f)]))}catch(e){error.value=t('common.invalidJson')+String(e);working.value=false;return}
  api.invoke(op,{pathParams,query,body}).then(data=>{result.value=typeof data==='string'?data:JSON.stringify(data,null,2)}).catch(e=>{error.value=uiErrorMessage(e);showError(e)}).finally(()=>working.value=false)}
 if(op.dangerous){uni.showModal({title:t('common.confirmDanger'),content:t('common.dangerDescription'),success:r=>{if(r.confirm)doCall()}})}else doCall()}

return {prefs,search,group,chosen,working,payload,raw,result,error,categories,filtered,selected,open,convert,invoke,mobilePreferenceStyle,operationLabel,groupLabel,operations,api};
}
