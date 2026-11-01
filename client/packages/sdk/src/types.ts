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
export interface PublicCategory { id: number | string; parent_id?: number | string | null; name: string; translations?: Record<string,Record<string,string>>; country_code?: string | null; country_codes?: string[] | string | null; icon?: string; region?: string; ref_price_min?: Amount; ref_price_max?: Amount; level?: number; sort_order?: number }
export interface PublicProduct { id: string; store_id?:string|null; category_id?: string; name: string; translations?: Record<string,Record<string,string>>; spec?: string; region?: string; price?: Amount; currency?: string; stock?: number; image_urls?: string | string[] }
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
export interface TransportRequest { method: HttpMethod; url: string; headers: Record<string,string>; data?: unknown; timeoutMs?: number; responseType?:'arraybuffer' }
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
export interface AuthTokenResult { accessToken?:string; access_token?:string; token?:string; userId?:string; expiresIn?:number; isAdmin?:boolean; phoneCountryCode?:string; locale?:'zh-CN'|'en-US' }
export interface OriginalIntentInput {tripId:string;itemName:string;itemSpec?:string;quantity:number;unit:string;categoryId?:string;expectUnitPrice:number;note?:string;contactName:string;contactPhone:string;address:string;expectDate?:string;insuranceChoice:'yes'|'waived';insuranceQuoteId?:string}
export interface IntentQuote {goodsAmount:Amount;serviceFee:Amount;insurancePremium:Amount;buyerEscrow:Amount;vendorDeposit:Amount;currency:'CNY';pricingVersion:'escrow_v1'|'escrow_v2_external_insurance'}
export interface ConfirmedIntentInput extends OriginalIntentInput {requestId:string;expectedBuyerEscrow:Amount}
export interface OriginalDisputeInput {orderId:string;type:'quality'|'wrong_item'|'missing'|'damaged';description:string;expectResolution:'refund_part'|'refund_full'|'resend'|'compensate';refundAmount?:Amount; evidenceUrls:string[];requestId:string}
export interface OriginalDispute {id:string;order_id:string;stage:string;type?:string;description?:string;initiator_id?:string;respondent_id?:string;refund_amount?:number|string;verdict?:string;created_at?:string;[key:string]:unknown}
export interface OriginalInviteTier {tier?:string;direct_count?:number;valid_count?:number;commission_mtd?:Amount;next_tier?:string;points?:number;rules?:Array<{tier:string;minCount:number;directRate?:number;indirectRate?:number}>}

/** Payment amounts are exact decimal strings; wallet settlement is always CNY. */
export type PaymentPlatform='web'|'h5'|'mp_weixin'|'app';
export interface CurrencyInfo {code:string;minorDigits:number;name:string;nameEn:string}
export interface PaymentMethod {id:string;name:string;method:string;platform:PaymentPlatform;currencies:string[];status:string;settlementCurrency:'CNY';spreadBps:number}
export interface ExchangeRate {base:string;currency:string;rate:string;source:string;asOf:string;validUntil:string;referenceOnly:true}
export interface FundingQuote {id:string;merchantId:string;merchantName:string;method:string;platform:PaymentPlatform;baseCurrency:'CNY';baseAmount:string;paymentCurrency:string;paymentAmount:string;rate:string;spreadBps:number;rateSource:string;rateAsOf:string;expiresAt:string;settlementCurrency:'CNY'}
export interface FundingPayment {id:string;status:'creating'|'pending'|'paid';baseCurrency:'CNY';baseAmount:string;paymentCurrency:string;paymentAmount:string;merchantName:string;checkout:{kind:'redirect'|'qr_text'|'wechat_jsapi'|'wechat_app'|'alipay_app';value:string}|null}
export interface MerchantConfig {id?:string;name:string;adapter:string;method:string;platform:PaymentPlatform;currencies:string[];credentialPrefix:string|null;spreadBps:number;quoteTtlSeconds:number;enabled:boolean;revision?:number;settings:Record<string,string>}
export interface FxConfig {id?:string;name:string;kind:'open_er_api'|'rates_json_v1';endpoint:string|null;credentialPrefix:string|null;maxAgeSeconds:number;enabled:boolean;transactionEnabled:boolean}
export interface PaymentSettings {settlementCurrency:'CNY';merchants:MerchantConfig[];fxSources:FxConfig[];currencies:CurrencyInfo[];adapters:{id:string;methods:string[];platforms:PaymentPlatform[];currencies:string[]}[]}

/** Existing product prices and wallet balances are denominated in CNY. */
export interface ProductPublishInput {name:string;spec?:string;region:string;price:string;stock:number;expectedStock?:number;categoryId?:number;imageUrls:string[];note?:string}
export interface OwnedProduct {id:string;name:string;spec?:string;region:string;price:string|number;stock:number;category_id?:number;image_urls:string|string[];note?:string;status:'on'|'off'}

export interface AddressInput {contactName:string;contactPhone:string;province?:string;city?:string;district?:string;detail:string;isDefault:boolean}
export interface SavedAddress {id:string;contact_name:string;contact_phone:string;province?:string;city?:string;district?:string;detail:string;is_default:boolean|number}
export interface SavedCartItem {id:string;product_id:string;qty:number;name:string|null;price:Amount|null;stock:number|null;available:boolean|number}
export interface SavedFavorite {id:string;target_type:'product'|'demand';target_id:string;title:string|null}

export interface InsuranceCompany {code:string;name:string;status:'reference_only'}
export interface InsuranceProduct {id:string;insurerCode:string;insurerName:string;name:string;kind:string;termsUrl:string;exclusionsUrl:string;claimsUrl:string;status:string;paymentMode:'insurer_direct'}
export interface InsuranceQuoteInput {productId:string;subjectType:'trip'|'pk_quote'|'order';subjectId:string;itemName:string;declaredValue:string;platform?:PaymentPlatform}
export interface InsuranceQuote {id:string;productId:string;insurerName:string;productName:string;kind:string;declaredValue:string;premium:string;currency:string;termsUrl:string;exclusionsUrl:string;claimsUrl:string;termsVersion:string;expiresAt:string;paymentMode:'insurer_direct';protectionStatus:'not_issued'}
export interface InsuranceApplication {id:string;orderId:string;status:string;insurerName:string;productName:string;premium:string;currency:string;checkoutUrl:string|null;policyNumber:string|null;certificateUrl:string|null;claimsUrl:string;coverageRef:string|null;termsVersion:string;startsAt:string|null;endsAt:string|null;platform:PaymentPlatform;paymentMode:'insurer_direct';protectionStatus:'provider_confirmed'|'not_confirmed'}
export interface InsuranceProductConfig extends Omit<InsuranceProduct,'status'|'paymentMode'> {adapter:string;providerProductCode:string;credentialPrefix:string;serviceHosts:string[];checkoutHosts:string[];enabled:boolean;revision?:number}
export interface InsuranceSettings {directory:InsuranceCompany[];products:InsuranceProductConfig[];adapters:{id:string;insurerCode:string}[];paymentMode:'insurer_direct'}

export interface TradeCaseEvent {id:string;actor_role:string;action:string;before_stage:string|null;after_stage:string;created_at:string;payload:{input:{note?:string;reason?:string;description?:string;outcome?:string;evidenceUrls?:string[];refundAmount?:Amount}}}
export interface TradeCaseSummary extends OriginalDispute {round:number;expect_resolution:string;item_name:string;order_no:string;refund_status?:string}
export interface TradeCaseDetail extends TradeCaseSummary {round:number;expect_resolution:string;respondent_id?:string;respondent_note?:string;evidence_urls:string[];respondent_evidence_urls:string[];deposit_owner?:string;verdict_reason?:string;review_appeal?:string;review_result?:string;negotiation_deadline?:string;resolved_at?:string;reopened_used:boolean|number;goods_amount:Amount;item_name:string;order_no:string;myRole:'buyer'|'merchant'|'operator';events:TradeCaseEvent[];refund_status?:string}
export interface TradeCaseAction {expectedRound:number;requestId:string}
export interface TradeCaseResponse extends TradeCaseAction {note:string;evidenceUrls:string[]}
export interface TradeCaseReason extends TradeCaseAction {reason:string}
export interface TradeCaseDecision extends TradeCaseReason {outcome:'reject'|'refund_part'|'refund_full'|'resend'|'compensate';refundAmount?:Amount;depositOwner:'return_original'|'buyer'|'vendor'|'none'}

export interface IntegrationField {key:string;secret:boolean;required:boolean;configured:boolean;status:'missing'|'stored'|'unreadable';value?:string}
export interface IntegrationProduct {vendor:string;code:string;category:string;mode:'implemented'|'bridge'|'reference_only';docs:string;enabled:boolean;remark:string;updatedAt:string;fields:IntegrationField[]}
export interface IntegrationSettings {encryptionReady:boolean;nativeDeliveryManaged?:boolean;products:IntegrationProduct[];specialized:string[]}
export interface IntegrationInput {enabled:boolean;remark:string;values:Record<string,string>;clearFields:string[]}

export interface UploadedDocument {id:string;purpose:string;contentType:string;byteSize:number;sha256:string}
export interface ShopApplicationInput {businessName:string;creditCode:string;representative:string;licenseUploadId:string;invitationCode?:string;channel:PaymentPlatform;requestId:string}
export interface ShopApplication {id:string;business_name:string;unified_social_credit_code:string;legal_representative:string;license_upload_id:string;invitation_code?:string;application_channel:string;status:string;review_note?:string;reviewed_at?:string;created_at:string;store_id?:string;store_status?:string;license_content_type?:string}
export interface Shop {id:string;name:string;description:string;status:string;matching_fee_rate:Amount;application_id:string;created_at:string}
export interface ShopState {store:Shop|null;applications:ShopApplication[];products:{id:string;name:string;price:Amount;stock:number;status:string}[]}
export interface ShopStatusInput {status:'active'|'paused'|'suspended';expectedStatus:string;reason:string}
export interface PublicShop {id:string;name:string;description:string;created_at:string;products:PublicProduct[]}

export interface ProductCheckoutTrip {id:string;destination:string;depart_date:string;return_date?:string;remaining_slots:number;capacity_qty:number}
export interface ProductCheckoutInput {productId:string;tripId:string;quantity:number;contactName:string;contactPhone:string;address:string;expectDate?:string;note?:string;insuranceChoice:'yes'|'waived';insuranceQuoteId?:string}
export interface ProductCheckoutQuote {productFingerprint:string;productId:string;tripId:string;storeId:string|null;itemName:string;itemSpec:string|null;unitPrice:string;quantity:number;goodsAmount:string;serviceFee:string;insurancePremium:string;buyerEscrow:string;vendorDeposit:string;currency:'CNY';pricingVersion:'escrow_v2_external_insurance';intentHours:number}
export interface ProductCheckoutConfirm extends ProductCheckoutInput {expectedProductFingerprint:string;requestId:string;expectedUnitPrice:string;expectedBuyerEscrow:string}
export interface PaymentRefund {id:string;paymentId:string;status:string;baseCurrency:string;baseAmount:string;paymentCurrency:string;paymentAmount:string;reason:string;reviewNote?:string|null;providerRefundId?:string|null;lastError?:string|null;createdAt:string;updatedAt:string}
export interface PaymentRefundInput {paymentId:string;requestId:string;reason:string}

export interface EligiblePaymentRefund {id:string;baseAmount:string;paymentAmount:string;paymentCurrency:'CNY';merchantName:string;paidAt:string|null}

export interface PaymentRefundEvent {id:string;action:string;detail:string;createdAt:string}
export interface ShopAttributedOrder {id:string;order_no:string;item_name:string;status:string;product_id:string;quantity:number;goods_amount:Amount;service_fee:Amount;currency:'CNY';stock_state:'held'|'consumed'|'released';created_at:string}

export type CouplerMode='SANDBOX'|'LIVE';
export interface CouplerField {key:string;label:string;secret:boolean;required:boolean;placeholder?:string;inputType?:'text'|'textarea'|'boolean';options?:{value:string;label:string}[]}
export interface CouplerModeConfiguration {revision:number;configRevision?:number;enabled:boolean;selected:boolean;values:Record<string,string>;configuredFields:string[];unreadableFields:string[];updatedAt?:string}
export interface CouplerProvider {vendor:string;product:string;code:string;name:string;capability:string;docs:string;sdkAvailable:boolean;businessVerified:boolean;consumerWired?:boolean;supportedModes:CouplerMode[];implementedOperations:string[];fields:CouplerField[];configurationByMode:Partial<Record<CouplerMode,CouplerModeConfiguration>>}
export interface CouplerCategory {code:string;name:string;description:string;providers:CouplerProvider[]}
export interface CouplerRuntime {mode:CouplerMode;revision:number}
export interface CouplerSettings {runtime:CouplerRuntime;categories:CouplerCategory[];encryptionReady?:boolean}
export interface CouplerValidation {mode:CouplerMode;revision:number;ready:boolean;missingFields:string[];invalidFields:string[];unreadableFields:string[];sdkAvailable:boolean;businessVerified:boolean;checks?:{code:string;status:string;message:string}[]}
export interface CouplerSaveInput {values:Record<string,string>;clearFields:string[];enabled:boolean;selected:boolean;expectedRevision:number}
export interface CouplerRuntimeInput {mode:CouplerMode;expectedRevision:number}
