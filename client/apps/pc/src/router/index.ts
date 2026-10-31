export type Page='home'|'products'|'trips'|'route'|'community'|'workspace';
const pages:Page[]=['home','products','trips','route','community','workspace'];
export function routeFromHash():Page {
  const hash=typeof location!=='undefined'?location.hash.slice(1):'home';
  return pages.includes(hash as Page)?hash as Page:'home';
}
