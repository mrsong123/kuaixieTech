/** Shared contracts extracted from Java controller signatures. Backend BigDecimal is represented as number|string. */
export type Amount = number | string;
export type ServiceMode = 'FREE' | 'ALONG_ROUTE';
export type BookingStatus = 'AWAITING_ORDER' | 'CANCELLED' | 'EXPIRED' | 'ORDER_LINKED';
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';
export type Access = 'public' | 'protected';
export type ApiFieldType = 'text' | 'number' | 'boolean' | 'array' | 'datetime';
export interface ApiField { name: string; type: ApiFieldType; javaType: string }
export interface ApiOperation {
  id: string; group: string; name: string; method: HttpMethod; path: string;
  controller: string; handler: string; pathParams: string[]; queryParams: string[];
  bodyFields: ApiField[]; rawBody: boolean; access: Access; role: 'admin' | 'user'; dangerous: boolean; returns: string;
}
export type BackendRecord = Record<string, unknown>;
export interface PublicCategory { id: number | string; parent_id?: number | string | null; name: string; translations?: Record<string,Record<string,string>>; country_code?: string | null; icon?: string; region?: string; ref_price_min?: Amount; ref_price_max?: Amount; level?: number; sort_order?: number }
export interface PublicProduct { id: string; category_id?: string; name: string; translations?: Record<string,Record<string,string>>; spec?: string; region?: string; price?: Amount; currency?: string; stock?: number; image_urls?: string | string[] }
export interface PublicTrip { id: string; owner_id?: string; destination?: string; destination_flag?: string; entry_port?: string; transport_mode?: string; depart_date?: string; arrive_date?: string; return_date?: string; capacity_qty?: number; remaining_slots?: number; fee_mode?: string; rank_score?: number; fee_value?: Amount; trip_type?: string; direction?: string }
export interface PricePreviewInput { serviceMode: ServiceMode; extraKm: Amount; extraMinutes: number; weightKg: Amount }
export interface PricePreview { policyVersion: number; currency: string; serviceMode: ServiceMode; baseFee: Amount; detourFee: Amount; waitingFee: Amount; weightFee: Amount; carrierAmount: Amount; platformFee: Amount; totalFee: Amount; distanceSource: string; requiresBuyerConfirmation: boolean; caution: string }
export interface PublishRouteRequest { title: string; description: string; goodsReferencePrice: Amount; pickupRegion: string; dropoffRegion: string; weightKg: Amount; quantity: number; serviceMode: ServiceMode; maxServiceFee: Amount; latestAt: string }
export interface CreateRouteRequest { demandId: string; pickupRegion: string; dropoffRegion: string; weightKg: Amount; quantity: number; serviceMode: ServiceMode; maxServiceFee: Amount; latestAt: string }
export interface RouteStop { sequence: number; regionCode: string; placeLabel: string; eta: string }
export interface RouteMatch { tripId: string; origin: string; destination: string; departDate: string; availableSlots: number; routingEvidence: string }
export interface RouteOffer { id?: string; demandId?: string; tripId?: string; carrierAmount?: Amount; platformFee?: Amount; totalFee?: Amount; status?: string; [key:string]: unknown }
export interface RouteRequest { demandId?: string; status?: string; pickupRegion?: string; dropoffRegion?: string; [key:string]: unknown }
export interface RouteBooking { id?: string; bookingId?: string; status?: BookingStatus | string; expiresAt?: string; [key:string]: unknown }
export interface TransportRequest { method: HttpMethod; url: string; headers: Record<string,string>; data?: unknown; timeoutMs?: number }
export type Transport = <T = unknown>(request: TransportRequest) => Promise<T>;
export class ApiError extends Error {
  constructor(message: string, public readonly status: number, public readonly detail?: unknown){super(message); this.name='ApiError'}
}

/** Additive portal contracts; only usable after installing the optional verified-server add-on. */
export interface PublicDemand { id:string; title:string; description?:string; city?:string; trip_type?:string; expect_price?:Amount; deadline?:string; created_at?:string }
export interface PublicCommunityPost { id:string; caption:string; media_kind:'text'|'image'|'video';media_url?:string;poster_url?:string;author_name?:string;author_avatar?:string;created_at?:string }
export interface VerifiedPortalRole {user_role?:'buyer'|'carrier'|'enterprise'|string;carrier_verify_status?:string}
export interface SupportConversation {id:string;status:'ai'|'waiting'|'human'|'closed';assigned_agent_id?:string|null;created_at?:string;updated_at?:string}
export interface SupportMessage {id:number;sender_type:'user'|'ai'|'agent';content:string;created_at:string}
export interface SupportSendResult {status:string;aiStatus?:'responded'|'unavailable'|'not_requested'}
export interface PortalDemandInput {title:string;description?:string;city?:string;expectPrice?:Amount|null;tripType:'any'|'domestic'|'international'}
export interface PortalTripInput {destination:string;originCity:string;departDate:string;returnDate:string;tripType:'domestic'|'international';transportMode:'road'|'rail'|'air'|'other';capacityQty:number}

export interface SupportQueueItem {id:string;user_id:string;status:string;created_at:string}

/** Authenticated account centre projections: no KYC, phone, bank or private address data. */
export interface MyProfile {id:string; nickname?:string|null; avatar_url?:string|null;city?:string|null;user_role?:string;buyer_verify_status?:string;carrier_verify_status?:string;enterprise_verify_status?:string;performance_score?:number;star_avg?:number|string;deal_count?:number;created_at?:string}
export interface MyOrder {id:string;order_no:string;item_name:string;item_spec?:string;quantity:number;unit?:string;goods_amount:number|string;service_fee:number|string;shipping_fee?:number|string|null;status:string;shipping_fee_status?:string;tracking_no?:string;my_role:'buyer'|'carrier';created_at?:string;updated_at?:string;[key:string]:unknown}
export interface MyDemand {id:string;title:string;item_desc?:string;expect_price?:number|string;status:string;city?:string;visibility?:string;created_at?:string;[key:string]:unknown}
export interface MyTrip {id:string;destination:string;destination_flag?:string;status?:string;verify_status?:string;depart_date?:string;return_date?:string;trip_type?:string;created_at?:string;[key:string]:unknown}
export interface MyWallet {balance_amount:number|string;currency:'CNY';points?:number}
export interface MyLedger {id:string;kind:string;amount:number|string;balance_after?:number|string;order_id?:string;note?:string;created_at?:string}
export interface MyNotification {id:string;kind:string;title:string;body?:string;order_id?:string;read_flag:number|boolean;created_at?:string}

/** Proposed V14 public read contracts: implementation must be added to the Java backend before integration sign-off. */
export interface PublicAnnouncement {id:string;title:string;body:string;kind:'notice'|'activity'|'rule'|string;pinned:boolean;active?:boolean;created_at:string;title_i18n?:Record<string,string>;body_i18n?:Record<string,string>}
export interface PublicLegalDocument {id?:string;doc_key:string;title:string;body_md:string;version:string|number;updated_at?:string;title_i18n?:Record<string,string>;body_md_i18n?:Record<string,string>}
export interface PublicReview {id:string;target_id:string;reviewer_id?:string;reviewer_name?:string;stars:number;content?:string;created_at?:string;hidden?:boolean;reply?:string}

/** Candidate DTOs from the Taro original; absent from the verified Spring Boot baseline. */
export interface AuthTokenResult { accessToken?:string; access_token?:string; token?:string; userId?:string; expiresIn?:number; isAdmin?:boolean }
export interface OriginalIntentInput {tripId:string;itemName:string;itemSpec?:string;quantity:number;unit:string;categoryId?:string;expectUnitPrice:number;note?:string;contactName:string;contactPhone:string;address:string;expectDate?:string;insuranceChoice:'yes'|'waived'}
export interface OriginalDisputeInput {orderId:string;type:'quality'|'wrong_item'|'missing'|'damaged';description:string;expectResolution:'refund_part'|'refund_full'|'resend'|'compensate';refundAmount?:number; evidenceUrls:string[]}
export interface OriginalDispute {id:string;order_id:string;stage:string;type?:string;description?:string;initiator_id?:string;respondent_id?:string;refund_amount?:number|string;verdict?:string;created_at?:string;[key:string]:unknown}
export interface OriginalInviteTier {tier?:string;direct_count?:number;next_tier?:string;points?:number;rules?:Array<{tier:string;minCount:number;directRate?:number;indirectRate?:number}>}
