import {ref} from 'vue';
import {getBearer,type ApiClient} from '@kuaixie/sdk';
import {t} from '@kuaixie/i18n';
export function usePublishComposer(api:ApiClient){
const selected=ref<'demand'|'trip'|'life'|'product'>('demand');
const region=ref<'domestic'|'international'>('domestic');
const busy=ref(false),notice=ref('');
const demand=ref({title:'',description:'',city:'',expectPrice:''});
const trip=ref({originCity:'',destination:'',departDate:'',returnDate:'',capacityQty:'1',transportMode:'road' as 'road'|'rail'|'air'|'other'});
const post=ref({caption:''});
const transportOptions=['road','rail','air','other'] as const;
function transportTitle(option:string){return t(option==='road'?'portal.publish.transportRoad':option==='rail'?'portal.publish.transportRail':option==='air'?'portal.publish.transportAir':'common.notProvided')}
function changeMode(value:'demand'|'trip'|'life'|'product'){selected.value=value;notice.value=''}
function changeRegion(value:'domestic'|'international'){region.value=value}
function pickDate(key:'departDate'|'returnDate',e:{detail:{value:string}}){trip.value[key]=e.detail.value}
function transportChange(e:{detail:{value:number}}){trip.value.transportMode=transportOptions[Number(e.detail.value)]||'road'}
async function submit(){
 if(!getBearer()){notice.value='portal.publish.login';return}
 notice.value='';
 try{
  busy.value=true;
  if(selected.value==='demand'){
    if(!demand.value.title.trim()){notice.value='portal.publish.needFields';return}
    const raw=demand.value.expectPrice.trim();const amount=raw?Number(raw):null;
    if(amount!==null&&(!Number.isFinite(amount)||amount<0)){notice.value='portal.publish.needFields';return}
    const res=await api.portal.publishDemand({title:demand.value.title.trim(),description:demand.value.description.trim(),city:demand.value.city.trim(),expectPrice:amount,tripType:region.value});
    notice.value=res.status==='open'?'portal.publish.success':'portal.publish.review';
    demand.value={title:'',description:'',city:'',expectPrice:''};
  } else if(selected.value==='trip'){
    const data=trip.value;
    if(!data.originCity.trim()||!data.destination.trim()||!data.departDate||!data.returnDate){notice.value='portal.publish.needFields';return}
    if(data.returnDate<data.departDate){notice.value='portal.publish.dateError';return}
    const qty=Number(data.capacityQty);
    if(!Number.isInteger(qty)||qty<1||qty>100){notice.value='portal.publish.needFields';return}
    const res=await api.portal.publishTrip({originCity:data.originCity.trim(),destination:data.destination.trim(),departDate:data.departDate,returnDate:data.returnDate,capacityQty:qty,transportMode:data.transportMode,tripType:region.value});
    notice.value=res.status==='pending_review'?'portal.publish.success':'portal.publish.review';
  } else {
    if(!post.value.caption.trim()){notice.value='portal.publish.needFields';return}
    await api.portal.postCommunity({caption:post.value.caption.trim(),mediaKind:'text'});
    notice.value='portal.publish.success';post.value.caption='';
  }
 }catch(e){
  const status=typeof e==='object'&&e!==null&&'status'in e?Number((e as {status:unknown}).status):0;
  notice.value=status===404?'portal.publish.unavailable':status===403?'portal.publish.tripEligibility':'portal.publish.failed';
 }finally{busy.value=false}
}

return {selected,region,busy,notice,demand,trip,post,transportOptions,transportTitle,changeMode,changeRegion,pickDate,transportChange,submit};
}
