import {preferenceRef} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../utils/prototype';
import {serviceGroups,serviceTitle,businessServices} from '@kuaixie/sdk';
export function useServicesPage(){
 const prefs=preferenceRef;
 const groups=serviceGroups.map(group=>({id:group.id,title:group.titleKey,items:group.items.map(id=>({id,titleKey:serviceTitle(id)}))}));
 function open(id:string){const item=businessServices.find(item=>item.id===id);if(item)uni.navigateTo({url:'/'+item.mobile})}
 return {prefs,groups,open,mobilePreferenceStyle};
}
