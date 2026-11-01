import type {PublicTrip,PublicDemand,PublicProduct,PublicCategory} from './types';
import {categoryCountryCodes} from './catalog';
export type SquareRegion='all'|'domestic'|'international';
export function tripRegion(value?:string|null):'domestic'|'international'|'unknown'{
 const key=(value||'').toLowerCase();
 if(['domestic','local','intercity','inland','within_country'].includes(key))return 'domestic';
 if(['international','cross_border','overseas','crossborder'].includes(key))return 'international';
 return 'unknown';
}
export function squareTripMatches(trip:PublicTrip,search:string,region:SquareRegion='all',direction='all'){
 const text=search.trim().toLocaleLowerCase();return (region==='all'||tripRegion(trip.trip_type)===region)&&(direction==='all'||String(trip.direction||'').toLowerCase()===direction)&&(!text||[trip.destination,trip.entry_port,trip.transport_mode,trip.trip_type].some(value=>String(value??'').toLocaleLowerCase().includes(text)));
}
export function squareDemandMatches(demand:PublicDemand,search:string,region:SquareRegion='all'){
 const text=search.trim().toLocaleLowerCase();return (region==='all'||tripRegion(demand.trip_type)==='unknown'||tripRegion(demand.trip_type)===region)&&(!text||[demand.title,demand.city,demand.description].some(value=>String(value??'').toLocaleLowerCase().includes(text)));
}
export function productCountryMatches(product:PublicProduct,categories:PublicCategory[],country:string){
 if(!country)return true;const index=new Map(categories.map(category=>[String(category.id),category]));let key=String(product.category_id??'');const visited=new Set<string>();
 while(key&&!visited.has(key)){visited.add(key);const category=index.get(key);if(!category)return false;if(categoryCountryCodes(category).includes(country.toUpperCase()))return true;key=category.parent_id==null?'':String(category.parent_id)}
 return false;
}
export const squareReaches=['international','domestic','intercity','city','village','neighbor'] as const;
export type SquareReach=typeof squareReaches[number];
export function tripReach(value?:string|null):SquareReach|''{
 const key=(value||'').toLowerCase();
 if(['international','cross_border','overseas','crossborder'].includes(key))return 'international';
 if(['local','same_city','intracity','within_city'].includes(key))return 'city';
 if(['domestic','inland','within_country'].includes(key))return 'domestic';
 return squareReaches.includes(key as SquareReach)?key as SquareReach:'';
}
export function squareReachTitle(reach:SquareReach){return 'plaza.reach.'+reach}
