import type {PublicProduct,PublicTrip} from '@kuaixie/sdk';
import {productImage,tripAvailability} from '@kuaixie/sdk';
import {formatAmount,formatDate,transportLabel,localizedField,t} from '@kuaixie/i18n';
export function mobileProductName(product:PublicProduct){return localizedField(product as unknown as Record<string,unknown>,'name')}
export function mobileProductPicture(product:PublicProduct){return productImage(product)??''}
export function mobileProductDetails(product:PublicProduct,categoryName:string){
  return [
    mobileProductName(product),
    t('m2.catalog.price')+': '+formatAmount(product.price,product.currency),
    t('m2.catalog.category')+': '+categoryName,
    t('m2.catalog.origin')+': '+(product.region||t('m2.catalog.originUnknown')),
    t('m2.catalog.specification')+': '+(product.spec||t('m2.catalog.specUnknown')),
    t('m2.catalog.stock')+': '+String(product.stock??t('m2.catalog.stockUnknown')),
    t('m2.catalog.buyUnavailable')
  ].join('\n');
}
export function mobileTripDetails(trip:PublicTrip){return [
    trip.destination||t('m2.common.notProvided'),
    t('m2.trip.depart')+': '+formatDate(trip.depart_date),
    t('m2.trip.arrival')+': '+formatDate(trip.arrive_date),
    t('m2.trip.return')+': '+formatDate(trip.return_date),
    t('m2.trip.transport')+': '+transportLabel(trip.transport_mode),
    t('m2.trip.remaining')+': '+String(tripAvailability(trip)??t('m2.trip.capacityUnknown')),
    t('m2.trip.fee')+': '+formatAmount(trip.fee_value),
    t('m2.trip.noRouteBooking')
  ].join('\n');}

/** Public-only gallery, never synthesize image URLs. */
export function mobileProductImages(product:PublicProduct):string[]{
 let value:unknown=product.image_urls;
 if(typeof value==='string'&&value.trim().startsWith('[')){try{value=JSON.parse(value)}catch{return []}}
 const urls=Array.isArray(value)?value:[value];
 return urls.filter((url):url is string=>typeof url==='string'&&/^https?:\/\/[^\s/?#]+(?:[/?#]|$)/i.test(url));
}
