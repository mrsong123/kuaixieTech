import {businessServices} from '@kuaixie/sdk';
export type Page='home'|'products'|'trips'|'route'|'community'|'workspace'|'publish'|'messages'|'me'|'business'|`services/${string}`;
const pages:Page[]=['home','products','trips','route','community','workspace','publish','messages','me','business'];
const services=new Set<string>([...businessServices.map(item=>item.id),'wallet-ledger','legal-doc','product-detail','shop-detail']);
export function routeFromHash():Page {
 const hash=typeof location!=='undefined'?location.hash.slice(1):'home';
 if(pages.includes(hash as Page))return hash as Page;
 if(hash.startsWith('services/')&&services.has(hash.slice(9).split('?')[0]))return hash as Page;
 return 'home';
}
