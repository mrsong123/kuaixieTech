import type {PublicCategory,PublicProduct,PublicTrip} from './types';
/** Read-only public projections. No frontend-generated catalog items or assumed currency. */
export type PublicMediaResolver=(path:string)=>string;
const canonicalMedia=/^\/public\/media\/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/;
function imageReference(candidate:unknown,resolve?:PublicMediaResolver):string|undefined {
  if(typeof candidate!=='string')return undefined;
  if(canonicalMedia.test(candidate)){try{return resolve?resolve(candidate):candidate}catch{return undefined}}
  // Mini-program runtimes may not expose WHATWG URL. Preserve validated legacy public URLs.
  return /^https?:\/\/[^\s/?#]+(?:[/?#]|$)/i.test(candidate)?candidate:undefined;
}
function imageValues(product:PublicProduct):unknown[]{
 let values:unknown=product.image_urls;
 if(typeof values==='string'&&values.trim().startsWith('[')){try{values=JSON.parse(values)}catch{return []}}
 return Array.isArray(values)?values:[values];
}
/** Resolve canonical media through the caller's public gateway; never append bearer credentials. */
export function productImage(product:PublicProduct,resolve?:PublicMediaResolver):string|undefined{return imageReference(imageValues(product)[0],resolve)}
export function productImages(product:PublicProduct,resolve?:PublicMediaResolver):string[]{return imageValues(product).flatMap(value=>{const image=imageReference(value,resolve);return image?[image]:[]})}
export function catalogMatch(product:PublicProduct, query:string, categoryId:string | null):boolean {
  if(categoryId!==null && String(product.category_id??'')!==categoryId)return false;
  const needle=query.trim().toLocaleLowerCase();
  if(!needle)return true;
  const names=Object.values(product.translations??{}).flatMap(fields=>[fields.name,fields.spec,fields.region]);
  return [product.name,product.spec,product.region,...names].some(v=>String(v??'').toLocaleLowerCase().includes(needle));
}
export function categoryLabel(category:PublicCategory):string {return String(category.name??'')}
/** MySQL JSON projections can arrive as strings. Keep every real country association. */
export function categoryCountryCodes(category:PublicCategory):string[] {
  let values:unknown=category.country_codes;
  if(typeof values==='string') {try{values=JSON.parse(values)}catch{values=[]}}
  if(!Array.isArray(values))values=[];
  return [...new Set([...(values as unknown[]),category.country_code]
    .filter((code):code is string=>typeof code==='string'&&/^[a-z]{2}$/i.test(code))
    .map(code=>code.toUpperCase()))];
}
export function tripAvailability(trip:PublicTrip):number | null {
  const count=trip.remaining_slots;
  return typeof count==='number'&&Number.isFinite(count)&&count>=0?count:null;
}
export function stablePublicId(record:{id:string|number}):string {return String(record.id)}
