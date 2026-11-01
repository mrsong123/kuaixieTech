import {computed,getCurrentScope,onScopeDispose,ref} from 'vue';
import {ApiError,getBearer,subscribeSession,type ApiClient} from '@kuaixie/sdk';
import {t,uiErrorMessage} from '@kuaixie/i18n';
export type PublishKind='demand'|'trip'|'life'|'product';
export type PublishRegion='domestic'|'international';
const emptyDemand=()=>({title:'',description:'',city:'',expectPrice:''});
const emptyTrip=()=>({originCity:'',destination:'',departDate:'',returnDate:'',capacityQty:'1',transportMode:'road' as 'road'|'rail'|'air'|'other'});
function validDate(value:string){if(!/^\d{4}-\d{2}-\d{2}$/.test(value))return false;const date=new Date(value+'T00:00:00Z');return Number.isFinite(date.getTime())&&date.toISOString().slice(0,10)===value}
function localDay(){const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`}
function receipt(value:{id:string;status:string},kind:PublishKind){if(!value||typeof value.id!=='string'||!value.id.trim()||!(value.status==='pending_review'||kind==='demand'&&value.status==='open'))throw new Error('PUBLISH_RECEIPT_UNCONFIRMED');return value.status==='open'?'portal.publish.success':'portal.publish.review'}
export function usePublishComposer(api:ApiClient){
 const selected=ref<PublishKind>('demand'),region=ref<PublishRegion>('domestic');
 const busy=ref(false),notice=ref(''),error=ref(''),unconfirmed=ref(false),sessionScope=ref(0);
 const demand=ref(emptyDemand()),trip=ref(emptyTrip()),post=ref({caption:''});
 const session=ref(getBearer());let disposed=false,generation=0;
 const signedIn=computed(()=>Boolean(session.value));
 const transportOptions=['road','rail','air','other'] as const;
 function transportTitle(option:string){return t(option==='road'?'portal.publish.transportRoad':option==='rail'?'portal.publish.transportRail':option==='air'?'portal.publish.transportAir':'common.notProvided')}
 function clear(){demand.value=emptyDemand();trip.value=emptyTrip();post.value={caption:''};notice.value='';error.value='';unconfirmed.value=false;busy.value=false;selected.value='demand';region.value='domestic'}
 const unsubscribe=subscribeSession(()=>{const current=getBearer();if(current!==session.value){generation++;sessionScope.value++;session.value=current;clear()}});
 if(getCurrentScope())onScopeDispose(()=>{disposed=true;generation++;unsubscribe();clear()});
 function current(request:number,bearer:string){return !disposed&&request===generation&&bearer===getBearer()}
 function changeMode(value:PublishKind){if(disposed||busy.value||unconfirmed.value||!['demand','trip','life','product'].includes(value))return;selected.value=value;notice.value='';error.value=''}
 function changeRegion(value:PublishRegion){if(!disposed&&!busy.value&&!unconfirmed.value&&['domestic','international'].includes(value))region.value=value}
 function pickDate(key:'departDate'|'returnDate',event:{detail:{value:string}}){if(!disposed&&!busy.value&&!unconfirmed.value&&['departDate','returnDate'].includes(key))trip.value[key]=event.detail.value}
 function transportChange(event:{detail:{value:number}}){const index=Number(event.detail.value);if(!disposed&&!busy.value&&!unconfirmed.value&&Number.isInteger(index)&&index>=0&&index<transportOptions.length)trip.value.transportMode=transportOptions[index]}
 function acknowledgeUnconfirmed(){if(!disposed&&!busy.value){unconfirmed.value=false;notice.value='';error.value=''}}
 async function submit(){
  if(disposed||busy.value||unconfirmed.value)return;
  if(!getBearer()){notice.value='portal.publish.login';return}
  if(selected.value==='product')return; // ProductPublishing owns its independent upload/save flow.
  notice.value='';error.value='';
  const kind=selected.value,reach=region.value;
  const rawDemand={...demand.value},rawTrip={...trip.value},rawPost={...post.value};
  if(kind==='demand'&&(!rawDemand.title.trim()||rawDemand.title.trim().length>200||rawDemand.description.length>4000||rawDemand.city.trim().length>64||rawDemand.expectPrice.trim()&&!/^\d{1,10}(\.\d{1,2})?$/.test(rawDemand.expectPrice.trim()))){notice.value='portal.publish.needFields';return}
  if(kind==='trip'){
   const qty=Number(rawTrip.capacityQty);
   if(!rawTrip.originCity.trim()||rawTrip.originCity.trim().length>64||!rawTrip.destination.trim()||rawTrip.destination.trim().length>128||!Number.isInteger(qty)||qty<1||qty>100||!transportOptions.includes(rawTrip.transportMode)){notice.value='portal.publish.needFields';return}
   if(!validDate(rawTrip.departDate)||!validDate(rawTrip.returnDate)||rawTrip.returnDate<rawTrip.departDate||rawTrip.departDate<localDay()){notice.value='portal.publish.invalidDates';return}
  }
  if(kind==='life'&&(!rawPost.caption.trim()||rawPost.caption.trim().length>4000)){notice.value='portal.publish.needFields';return}
  const request=++generation,bearer=getBearer();busy.value=true;
  try{
   if(kind==='demand'){
    const result=await api.portal.publishDemand({title:rawDemand.title.trim(),description:rawDemand.description.trim(),city:rawDemand.city.trim(),expectPrice:rawDemand.expectPrice.trim()||null,tripType:reach});
    if(!current(request,bearer))return;notice.value=receipt(result,kind);
    if(JSON.stringify(demand.value)===JSON.stringify(rawDemand))demand.value=emptyDemand();
   }else if(kind==='trip'){
    const result=await api.portal.publishTrip({originCity:rawTrip.originCity.trim(),destination:rawTrip.destination.trim(),departDate:rawTrip.departDate,returnDate:rawTrip.returnDate,capacityQty:Number(rawTrip.capacityQty),transportMode:rawTrip.transportMode,tripType:reach});
    if(!current(request,bearer))return;notice.value=receipt(result,kind);
    if(JSON.stringify(trip.value)===JSON.stringify(rawTrip))trip.value=emptyTrip();
   }else{
    const result=await api.portal.postCommunity({caption:rawPost.caption.trim(),mediaKind:'text'});
    if(!current(request,bearer))return;notice.value=receipt(result,kind);
    if(post.value.caption===rawPost.caption)post.value={caption:''};
   }
  }catch(cause){
   if(!current(request,bearer))return;
   const status=cause instanceof ApiError?cause.status:typeof cause==='object'&&cause!==null&&'status'in cause?Number((cause as {status:unknown}).status):0;
   if(![400,401,403,404,409,422].includes(status)){unconfirmed.value=true;notice.value='portal.publish.unconfirmed';return}
   if(status===403)notice.value=kind==='trip'?'portal.publish.tripEligibility':'portal.publish.accountEligibility';
   else if(status===401)notice.value='portal.publish.login';
   else if(status===404)notice.value='portal.publish.unavailable';
   else error.value=uiErrorMessage(cause);
  }finally{if(current(request,bearer))busy.value=false}
 }
 return {selected,region,busy,notice,error,unconfirmed,signedIn,sessionScope,demand,trip,post,transportOptions,transportTitle,changeMode,changeRegion,pickDate,transportChange,acknowledgeUnconfirmed,submit};
}
