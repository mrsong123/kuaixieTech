import type {PublicCategory,PublicProduct,PublicTrip} from './types';
/** Read-only public projections. No frontend-generated catalog items or assumed currency. */
export function productImage(product: PublicProduct):string | undefined {
  let images: unknown=product.image_urls;
  if(typeof images==='string' && images.trim().startsWith('[')) {
    try {images=JSON.parse(images)}catch{return undefined}
  }
  const candidate=Array.isArray(images)?images[0]:images;
  if(typeof candidate!=='string')return undefined;
  // UniApp mini-program JS runtimes may not expose the WHATWG URL constructor.
  return /^https?:\/\/[^\s/?#]+(?:[/?#]|$)/i.test(candidate) ? candidate : undefined;
}
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
