import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {getFlow} from '../features/flows/definitions';
export function useServicesPage(){
 const prefs=preferenceRef;
 const groupsRaw=[
  {id:'account',title:'account.title',items:['my-orders','my-demands','my-trips','my-wallet','my-notifications']},
  {id:'identity',title:'fast.group.identity',items:['buyer-verify','carrier-verify','enterprise-verify','account-security','kyc-docs']},
  {id:'orders',title:'fast.group.order',items:['order-actions','purchase-proof','boarding-proof','shipping-proof','delivery-proof','cancel-request','invoice-request','meetup']},
  {id:'commerce',title:'fast.group.trading',items:['pk-quotes','shipping-fee','assist','deposit','invite']},
  {id:'communication',title:'fast.group.communication',items:['privacy-call','order-call','community-comment']},
 ];
 const groups=groupsRaw.map(g=>({...g,items:g.items.map(id=>({id,titleKey:id.startsWith('my-')?'account.'+(id==='my-orders'?'myOrders':id==='my-demands'?'myDemands':id==='my-trips'?'myTrips':id==='my-wallet'?'wallet':'notifications'):'fast.flow.'+id.replace(/-/g,'_')}))}));
 function open(id:string){if(!id.startsWith('my-'))getFlow(id);uni.navigateTo({url:'/pages/'+id+'/index'})}
 return {prefs,groups,open,mobilePreferenceStyle};
}
