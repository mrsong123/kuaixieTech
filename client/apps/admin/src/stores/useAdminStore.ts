import {ref,computed} from 'vue';
import {t,setLocale,uiErrorMessage} from '@kuaixie/i18n';
import {localeRef} from '@kuaixie/i18n/vue';
import {ApiError,getBearer,setBearer,clearBearer,operations} from '@kuaixie/sdk';
import {createAdminApi} from '../api/client';
import {type Section} from '../router';
import {ADMIN_GROUPS,type AdminGroupKey} from '../console/tabs';

export function useAdminStore(){
const locale=localeRef;
function changeLocale(event:Event){const selected=(event.target as HTMLSelectElement).value;setLocale(selected);localStorage.setItem('kx.locale',selected)}
const api=createAdminApi();
const groups=Object.entries(operations.reduce((o,v)=>{o[v.group]=(o[v.group]??0)+1;return o},{} as Record<string,number>)).sort((a,b)=>b[1]-a[1]);
const section=ref<Section>('overview');
const sidebarOpen=ref(false);const token=ref(getBearer());const showToken=ref(false);
const loginChannel=ref<'phone'|'email'>('phone');const loginContact=ref('');const loginPassword=ref('');
const loginBusy=ref(false);const loginError=ref('');
const stats=ref<Record<string,unknown>|null>(null);const statsError=ref('');const busy=ref(false);const search=ref('');
const apiBaseText=import.meta.env.VITE_API_BASE_URL||'/public → Spring Boot gateway';
const activeGroup=computed<AdminGroupKey|null>(()=>ADMIN_GROUPS.find(g=>g.tabs.some(tb=>tb.key===section.value))?.key??null);
const quickLinks=computed<[Section,string,string][]>(()=>{void localeRef.value;return [
  ['carrierKyc',t('admin.group.review'),t('admin.tab.carrierKyc')],
  ['orders',t('admin.group.trade'),t('admin.tab.orders')],
  ['users',t('admin.group.growth'),t('admin.tab.users')],
  ['all',t('admin.nav.all'),t('admin.console.apiHint')],
];});
const nav:[Section,string,string][]=[
  ['overview','admin.group.core','core'],
  ['carrierKyc','admin.group.review','review'],
  ['orders','admin.group.trade','trade'],
  ['users','admin.group.growth','growth'],
  ['all','admin.nav.all','api'],
  ['settings','admin.nav.settings','security'],
];
const groupConfigs:Record<string,{kicker:string;title:string;desc:string;groups:string[]}>= {
 core:{kicker:'admin.group.core',title:'admin.tab.overview',desc:'admin.console.descCore',groups:[]},
 review:{kicker:'admin.group.review',title:'admin.group.review',desc:'admin.console.descReview',groups:[]},
 trade:{kicker:'admin.group.trade',title:'admin.group.trade',desc:'admin.console.descTrade',groups:[]},
 growth:{kicker:'admin.group.growth',title:'admin.group.growth',desc:'admin.console.descGrowth',groups:[]},
 all:{kicker:'text.88111653bdc9',title:'text.2ec4c789a11a',desc:'text.b958e732bf71',groups:[]},
 settings:{kicker:'text.29d43743c43b',title:'text.4f35ab68a79b',desc:'text.84c24453bf7e',groups:[]},
};
const config=computed(()=>{
  const g=activeGroup.value;
  if(g) return groupConfigs[g];
  return groupConfigs[section.value]??groupConfigs.core;
});
const docsCount=operations.length;const protectedCount=operations.filter(o=>o.access==='protected').length;const adminCount=operations.filter(o=>o.role==='admin').length;
const filteredGroupOps=computed(()=>operations.filter(x=>!config.value.groups.length||config.value.groups.includes(x.group)).filter(x=> (x.name+x.path).toLowerCase().includes(search.value.toLowerCase())));
function go(s:Section){section.value=s;sidebarOpen.value=false;search.value='';window.scrollTo({top:0,behavior:'smooth'})}
async function loadStats(){busy.value=true;statsError.value='';try{stats.value=await api.rpc('admin_stats',{}) as Record<string,unknown>}catch(e){statsError.value=uiErrorMessage(e)}finally{busy.value=false}}
function applyToken(){setBearer(token.value);showToken.value=false;stats.value=null;statsError.value='';}
function removeToken(){clearBearer();token.value='';stats.value=null;statsError.value=''}
function authMessage(error:unknown){
  if(error instanceof ApiError && error.detail && typeof error.detail==='object'){
    const body=error.detail as Record<string,unknown>;
    const code=String(body.detail??body.message??'');
    if(code.startsWith('AUTH_')){
      const key='admin.login.'+code;
      const translated=t(key);
      return translated===key?code:translated;
    }
  }
  return uiErrorMessage(error);
}
async function login(){
  loginBusy.value=true;loginError.value='';
  try{
    const result=await api.originalV15.login({channel:loginChannel.value,contact:loginContact.value.trim(),password:loginPassword.value});
    const issued=result.accessToken||result.access_token||result.token||'';
    if(!issued){loginError.value=t('admin.login.noToken');return}
    if(result.isAdmin===false){loginError.value=t('admin.login.notAdmin');return}
    setBearer(issued);token.value=issued;loginPassword.value='';
    await loadStats();
    if(statsError.value){removeToken();loginError.value=t('admin.login.notAdmin')}
  }catch(e){removeToken();loginError.value=authMessage(e)}
  finally{loginBusy.value=false}
}
return {locale,changeLocale,api,groups,section,sidebarOpen,token,showToken,stats,statsError,busy,search,apiBaseText,quickLinks,nav,config,docsCount,protectedCount,adminCount,filteredGroupOps,go,loadStats,applyToken,removeToken,activeGroup,loginChannel,loginContact,loginPassword,loginBusy,loginError,login};
}
export type AdminStore=ReturnType<typeof useAdminStore>;
