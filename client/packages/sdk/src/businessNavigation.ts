/** One business directory; platform adapters choose native navigation, never different business rules. */
export const primaryNavigation=[
 {id:'home',titleKey:'prototype.nav.square',mobile:'pages/index/index',tab:true},
 {id:'community',titleKey:'portal.tabs.community',mobile:'pages/demand-pool/index',tab:true},
 {id:'publish',titleKey:'prototype.nav.publish',mobile:'pages/publish-hub/index',tab:true},
 {id:'messages',titleKey:'prototype.nav.messages',mobile:'pages/messages/index',tab:true},
 {id:'me',titleKey:'mobile.tab.me',mobile:'pages/me/index',tab:true},
] as const;
export const serviceGroups=[
 {id:'account',titleKey:'account.title',items:['my-orders','my-demands','my-trips','my-wallet','my-refunds','my-notifications','my-products','my-shop','my-insurance','my-disputes','invite-tier','address-book','cart','favorites']},
 {id:'identity',titleKey:'fast.group.identity',items:['buyer-verify','carrier-verify','enterprise-verify','account-security','kyc-docs']},
 {id:'orders',titleKey:'fast.group.order',items:['order-actions','purchase-proof','boarding-proof','shipping-proof','delivery-proof','cancel-request','invoice-request','meetup']},
 {id:'commerce',titleKey:'fast.group.trading',items:['pk-quotes','shipping-fee','assist','deposit','invite']},
 {id:'communication',titleKey:'fast.group.communication',items:['privacy-call','order-call','community-comment','support-center','announcements']},
] as const;
const titles:Record<string,string>={'my-refunds':'refund.title','my-shop':'shop.title','my-disputes':'case.title','invite-tier':'v15.inviteTier','address-book':'migration.address','cart':'migration.cart','favorites':'migration.favorites','my-insurance':'insurance.application','my-orders':'account.myOrders','my-demands':'account.myDemands','my-trips':'account.myTrips','my-wallet':'account.wallet','my-notifications':'account.notifications','my-products':'product.title','support-center':'portal.chat.title','announcements':'mine.announcements'};
export function serviceTitle(id:string){return titles[id]??'fast.flow.'+id.replace(/-/g,'_')}
export const businessServices=serviceGroups.flatMap(group=>group.items.map(id=>({id,titleKey:serviceTitle(id),mobile:'pages/'+id+'/index',desktop:'services/'+id,group:group.id})));
