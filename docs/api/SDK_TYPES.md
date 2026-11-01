# SDK调用方类型字段

以下为前端TypeScript声明的逐项原文，便于对照泛型Map响应。它们不是额外后端保证；`BackendRecord`/索引签名仍是开放字段，真实SQL选择列、业务状态及当前响应优先。

## Amount

[types.ts:2](../../client/packages/sdk/src/types.ts#L2)

```typescript
export type Amount = number | string;
```

## ServiceMode

[types.ts:3](../../client/packages/sdk/src/types.ts#L3)

```typescript
export type ServiceMode = 'FREE' | 'ALONG_ROUTE';
```

## BookingStatus

[types.ts:4](../../client/packages/sdk/src/types.ts#L4)

```typescript
export type BookingStatus = 'AWAITING_ORDER' | 'CANCELLED' | 'EXPIRED' | 'ORDER_LINKED';
```

## HttpMethod

[types.ts:5](../../client/packages/sdk/src/types.ts#L5)

```typescript
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';
```

## Access

[types.ts:6](../../client/packages/sdk/src/types.ts#L6)

```typescript
export type Access = 'public' | 'protected';
```

## ApiFieldType

[types.ts:7](../../client/packages/sdk/src/types.ts#L7)

```typescript
export type ApiFieldType = 'text' | 'number' | 'boolean' | 'array' | 'datetime';
```

## ApiField

[types.ts:8](../../client/packages/sdk/src/types.ts#L8)

```typescript
export interface ApiField { name: string; type: ApiFieldType; javaType: string }
```

## ApiOperation

[types.ts:9](../../client/packages/sdk/src/types.ts#L9)

```typescript
export interface ApiOperation {
  id: string; group: string; name: string; method: HttpMethod; path: string;
  controller: string; handler: string; pathParams: string[]; queryParams: string[];
  bodyFields: ApiField[]; rawBody: boolean; access: Access; role: 'admin' | 'user'; dangerous: boolean; returns: string;
}
```

## BackendRecord

[types.ts:14](../../client/packages/sdk/src/types.ts#L14)

```typescript
export type BackendRecord = Record<string, unknown>;
```

## PublicCategory

[types.ts:15](../../client/packages/sdk/src/types.ts#L15)

```typescript
export interface PublicCategory { id: number | string; parent_id?: number | string | null; name: string; translations?: Record<string,Record<string,string>>; country_code?: string | null; country_codes?: string[] | string | null; icon?: string; region?: string; ref_price_min?: Amount; ref_price_max?: Amount; level?: number; sort_order?: number }
```

## PublicProduct

[types.ts:16](../../client/packages/sdk/src/types.ts#L16)

```typescript
export interface PublicProduct { id: string; store_id?:string|null; category_id?: string; name: string; translations?: Record<string,Record<string,string>>; spec?: string; region?: string; price?: Amount; currency?: string; stock?: number; image_urls?: string | string[] }
```

## PublicTrip

[types.ts:17](../../client/packages/sdk/src/types.ts#L17)

```typescript
export interface PublicTrip { id: string; owner_id?: string; destination?: string; destination_flag?: string; entry_port?: string; transport_mode?: string; depart_date?: string; arrive_date?: string; return_date?: string; capacity_qty?: number; remaining_slots?: number; fee_mode?: string; rank_score?: number; fee_value?: Amount; trip_type?: string; direction?: string }
```

## PricePreviewInput

[types.ts:18](../../client/packages/sdk/src/types.ts#L18)

```typescript
export interface PricePreviewInput { serviceMode: ServiceMode; extraKm: Amount; extraMinutes: number; weightKg: Amount }
```

## PricePreview

[types.ts:19](../../client/packages/sdk/src/types.ts#L19)

```typescript
export interface PricePreview { policyVersion: number; currency: string; serviceMode: ServiceMode; baseFee: Amount; detourFee: Amount; waitingFee: Amount; weightFee: Amount; carrierAmount: Amount; platformFee: Amount; totalFee: Amount; distanceSource: string; requiresBuyerConfirmation: boolean; caution: string }
```

## PublishRouteRequest

[types.ts:20](../../client/packages/sdk/src/types.ts#L20)

```typescript
export interface PublishRouteRequest { title: string; description: string; goodsReferencePrice: Amount; pickupRegion: string; dropoffRegion: string; weightKg: Amount; quantity: number; serviceMode: ServiceMode; maxServiceFee: Amount; latestAt: string }
```

## CreateRouteRequest

[types.ts:21](../../client/packages/sdk/src/types.ts#L21)

```typescript
export interface CreateRouteRequest { demandId: string; pickupRegion: string; dropoffRegion: string; weightKg: Amount; quantity: number; serviceMode: ServiceMode; maxServiceFee: Amount; latestAt: string }
```

## RouteStop

[types.ts:22](../../client/packages/sdk/src/types.ts#L22)

```typescript
export interface RouteStop { sequence: number; regionCode: string; placeLabel: string; eta: string }
```

## RouteMatch

[types.ts:23](../../client/packages/sdk/src/types.ts#L23)

```typescript
export interface RouteMatch { tripId: string; origin: string; destination: string; departDate: string; availableSlots: number; routingEvidence: string }
```

## RouteOffer

[types.ts:24](../../client/packages/sdk/src/types.ts#L24)

```typescript
export interface RouteOffer { id?: string; demandId?: string; tripId?: string; carrierAmount?: Amount; platformFee?: Amount; totalFee?: Amount; status?: string; [key:string]: unknown }
```

## RouteRequest

[types.ts:25](../../client/packages/sdk/src/types.ts#L25)

```typescript
export interface RouteRequest { demandId?: string; status?: string; pickupRegion?: string; dropoffRegion?: string; [key:string]: unknown }
```

## RouteBooking

[types.ts:26](../../client/packages/sdk/src/types.ts#L26)

```typescript
export interface RouteBooking { id?: string; bookingId?: string; status?: BookingStatus | string; expiresAt?: string; [key:string]: unknown }
```

## TransportRequest

[types.ts:27](../../client/packages/sdk/src/types.ts#L27)

```typescript
export interface TransportRequest { method: HttpMethod; url: string; headers: Record<string,string>; data?: unknown; timeoutMs?: number; responseType?:'arraybuffer' }
```

## Transport

[types.ts:28](../../client/packages/sdk/src/types.ts#L28)

```typescript
export type Transport = <T = unknown>(request: TransportRequest) => Promise<T>;
```

## PublicDemand

[types.ts:34](../../client/packages/sdk/src/types.ts#L34)

```typescript
export interface PublicDemand { id:string; title:string; description?:string; city?:string; trip_type?:string; expect_price?:Amount; deadline?:string; created_at?:string }
```

## PublicCommunityPost

[types.ts:35](../../client/packages/sdk/src/types.ts#L35)

```typescript
export interface PublicCommunityPost { id:string; caption:string; media_kind:'text'|'image'|'video';media_url?:string;poster_url?:string;author_name?:string;author_avatar?:string;created_at?:string }
```

## VerifiedPortalRole

[types.ts:36](../../client/packages/sdk/src/types.ts#L36)

```typescript
export interface VerifiedPortalRole {user_role?:'buyer'|'carrier'|'enterprise'|string;carrier_verify_status?:string}
```

## SupportConversation

[types.ts:37](../../client/packages/sdk/src/types.ts#L37)

```typescript
export interface SupportConversation {id:string;status:'ai'|'waiting'|'human'|'closed';assigned_agent_id?:string|null;created_at?:string;updated_at?:string}
```

## SupportMessage

[types.ts:38](../../client/packages/sdk/src/types.ts#L38)

```typescript
export interface SupportMessage {id:number;sender_type:'user'|'ai'|'agent';content:string;created_at:string}
```

## SupportSendResult

[types.ts:39](../../client/packages/sdk/src/types.ts#L39)

```typescript
export interface SupportSendResult {status:string;aiStatus?:'responded'|'unavailable'|'not_requested'}
```

## PortalDemandInput

[types.ts:40](../../client/packages/sdk/src/types.ts#L40)

```typescript
export interface PortalDemandInput {title:string;description?:string;city?:string;expectPrice?:Amount|null;tripType:'any'|'domestic'|'international'}
```

## PortalTripInput

[types.ts:41](../../client/packages/sdk/src/types.ts#L41)

```typescript
export interface PortalTripInput {destination:string;originCity:string;departDate:string;returnDate:string;tripType:'domestic'|'international';transportMode:'road'|'rail'|'air'|'other';capacityQty:number}
```

## SupportQueueItem

[types.ts:43](../../client/packages/sdk/src/types.ts#L43)

```typescript
export interface SupportQueueItem {id:string;user_id:string;status:string;created_at:string}
```

## MyProfile

[types.ts:46](../../client/packages/sdk/src/types.ts#L46)

```typescript
export interface MyProfile {id:string; nickname?:string|null; avatar_url?:string|null;city?:string|null;user_role?:string;buyer_verify_status?:string;carrier_verify_status?:string;enterprise_verify_status?:string;performance_score?:number;star_avg?:number|string;deal_count?:number;created_at?:string}
```

## MyOrder

[types.ts:47](../../client/packages/sdk/src/types.ts#L47)

```typescript
export interface MyOrder {id:string;order_no:string;item_name:string;item_spec?:string;quantity:number;unit?:string;goods_amount:number|string;service_fee:number|string;shipping_fee?:number|string|null;status:string;shipping_fee_status?:string;tracking_no?:string;my_role:'buyer'|'carrier';created_at?:string;updated_at?:string;[key:string]:unknown}
```

## MyDemand

[types.ts:48](../../client/packages/sdk/src/types.ts#L48)

```typescript
export interface MyDemand {id:string;title:string;item_desc?:string;expect_price?:number|string;status:string;city?:string;visibility?:string;created_at?:string;[key:string]:unknown}
```

## MyTrip

[types.ts:49](../../client/packages/sdk/src/types.ts#L49)

```typescript
export interface MyTrip {id:string;destination:string;destination_flag?:string;status?:string;verify_status?:string;depart_date?:string;return_date?:string;trip_type?:string;created_at?:string;[key:string]:unknown}
```

## MyWallet

[types.ts:50](../../client/packages/sdk/src/types.ts#L50)

```typescript
export interface MyWallet {balance_amount:number|string;currency:'CNY';points?:number}
```

## MyLedger

[types.ts:51](../../client/packages/sdk/src/types.ts#L51)

```typescript
export interface MyLedger {id:string;kind:string;amount:number|string;balance_after?:number|string;order_id?:string;note?:string;created_at?:string}
```

## MyNotification

[types.ts:52](../../client/packages/sdk/src/types.ts#L52)

```typescript
export interface MyNotification {id:string;kind:string;title:string;body?:string;order_id?:string;read_flag:number|boolean;created_at?:string}
```

## PublicAnnouncement

[types.ts:55](../../client/packages/sdk/src/types.ts#L55)

```typescript
export interface PublicAnnouncement {id:string;title:string;body:string;kind:'notice'|'activity'|'rule'|string;pinned:boolean;active?:boolean;created_at:string;title_i18n?:Record<string,string>;body_i18n?:Record<string,string>}
```

## PublicLegalDocument

[types.ts:56](../../client/packages/sdk/src/types.ts#L56)

```typescript
export interface PublicLegalDocument {id?:string;doc_key:string;title:string;body_md:string;version:string|number;updated_at?:string;title_i18n?:Record<string,string>;body_md_i18n?:Record<string,string>}
```

## PublicReview

[types.ts:57](../../client/packages/sdk/src/types.ts#L57)

```typescript
export interface PublicReview {id:string;target_id:string;reviewer_id?:string;reviewer_name?:string;stars:number;content?:string;created_at?:string;hidden?:boolean;reply?:string}
```

## AuthTokenResult

[types.ts:60](../../client/packages/sdk/src/types.ts#L60)

```typescript
export interface AuthTokenResult { accessToken?:string; access_token?:string; token?:string; userId?:string; expiresIn?:number; isAdmin?:boolean; phoneCountryCode?:string; locale?:'zh-CN'|'en-US' }
```

## OriginalIntentInput

[types.ts:61](../../client/packages/sdk/src/types.ts#L61)

```typescript
export interface OriginalIntentInput {tripId:string;itemName:string;itemSpec?:string;quantity:number;unit:string;categoryId?:string;expectUnitPrice:number;note?:string;contactName:string;contactPhone:string;address:string;expectDate?:string;insuranceChoice:'yes'|'waived';insuranceQuoteId?:string}
```

## IntentQuote

[types.ts:62](../../client/packages/sdk/src/types.ts#L62)

```typescript
export interface IntentQuote {goodsAmount:Amount;serviceFee:Amount;insurancePremium:Amount;buyerEscrow:Amount;vendorDeposit:Amount;currency:'CNY';pricingVersion:'escrow_v1'|'escrow_v2_external_insurance'}
```

## ConfirmedIntentInput

[types.ts:63](../../client/packages/sdk/src/types.ts#L63)

```typescript
export interface ConfirmedIntentInput extends OriginalIntentInput {requestId:string;expectedBuyerEscrow:Amount}
```

## OriginalDisputeInput

[types.ts:64](../../client/packages/sdk/src/types.ts#L64)

```typescript
export interface OriginalDisputeInput {orderId:string;type:'quality'|'wrong_item'|'missing'|'damaged';description:string;expectResolution:'refund_part'|'refund_full'|'resend'|'compensate';refundAmount?:Amount; evidenceUrls:string[];requestId:string}
```

## OriginalDispute

[types.ts:65](../../client/packages/sdk/src/types.ts#L65)

```typescript
export interface OriginalDispute {id:string;order_id:string;stage:string;type?:string;description?:string;initiator_id?:string;respondent_id?:string;refund_amount?:number|string;verdict?:string;created_at?:string;[key:string]:unknown}
```

## OriginalInviteTier

[types.ts:66](../../client/packages/sdk/src/types.ts#L66)

```typescript
export interface OriginalInviteTier {tier?:string;direct_count?:number;valid_count?:number;commission_mtd?:Amount;next_tier?:string;points?:number;rules?:Array<{tier:string;minCount:number;directRate?:number;indirectRate?:number}>}
```

## PaymentPlatform

[types.ts:69](../../client/packages/sdk/src/types.ts#L69)

```typescript
export type PaymentPlatform='web'|'h5'|'mp_weixin'|'app';
```

## CurrencyInfo

[types.ts:70](../../client/packages/sdk/src/types.ts#L70)

```typescript
export interface CurrencyInfo {code:string;minorDigits:number;name:string;nameEn:string}
```

## PaymentMethod

[types.ts:71](../../client/packages/sdk/src/types.ts#L71)

```typescript
export interface PaymentMethod {id:string;name:string;method:string;platform:PaymentPlatform;currencies:string[];status:string;settlementCurrency:'CNY';spreadBps:number}
```

## ExchangeRate

[types.ts:72](../../client/packages/sdk/src/types.ts#L72)

```typescript
export interface ExchangeRate {base:string;currency:string;rate:string;source:string;asOf:string;validUntil:string;referenceOnly:true}
```

## FundingQuote

[types.ts:73](../../client/packages/sdk/src/types.ts#L73)

```typescript
export interface FundingQuote {id:string;merchantId:string;merchantName:string;method:string;platform:PaymentPlatform;baseCurrency:'CNY';baseAmount:string;paymentCurrency:string;paymentAmount:string;rate:string;spreadBps:number;rateSource:string;rateAsOf:string;expiresAt:string;settlementCurrency:'CNY'}
```

## FundingPayment

[types.ts:74](../../client/packages/sdk/src/types.ts#L74)

```typescript
export interface FundingPayment {id:string;status:'creating'|'pending'|'paid';baseCurrency:'CNY';baseAmount:string;paymentCurrency:string;paymentAmount:string;merchantName:string;checkout:{kind:'redirect'|'qr_text'|'wechat_jsapi'|'wechat_app'|'alipay_app';value:string}|null}
```

## MerchantConfig

[types.ts:75](../../client/packages/sdk/src/types.ts#L75)

```typescript
export interface MerchantConfig {id?:string;name:string;adapter:string;method:string;platform:PaymentPlatform;currencies:string[];credentialPrefix:string|null;spreadBps:number;quoteTtlSeconds:number;enabled:boolean;revision?:number;settings:Record<string,string>}
```

## FxConfig

[types.ts:76](../../client/packages/sdk/src/types.ts#L76)

```typescript
export interface FxConfig {id?:string;name:string;kind:'open_er_api'|'rates_json_v1';endpoint:string|null;credentialPrefix:string|null;maxAgeSeconds:number;enabled:boolean;transactionEnabled:boolean}
```

## PaymentSettings

[types.ts:77](../../client/packages/sdk/src/types.ts#L77)

```typescript
export interface PaymentSettings {settlementCurrency:'CNY';merchants:MerchantConfig[];fxSources:FxConfig[];currencies:CurrencyInfo[];adapters:{id:string;methods:string[];platforms:PaymentPlatform[];currencies:string[]}[]}
```

## ProductPublishInput

[types.ts:80](../../client/packages/sdk/src/types.ts#L80)

```typescript
export interface ProductPublishInput {name:string;spec?:string;region:string;price:string;stock:number;expectedStock?:number;categoryId?:string|number;imageUrls:string[];note?:string}
```

## OwnedProduct

[types.ts:81](../../client/packages/sdk/src/types.ts#L81)

```typescript
export interface OwnedProduct {id:string;name:string;spec?:string;region:string;price:string|number;stock:number;category_id?:string|number;image_urls:string|string[];note?:string;status:'on'|'off'}
```

## AddressInput

[types.ts:83](../../client/packages/sdk/src/types.ts#L83)

```typescript
export interface AddressInput {contactName:string;contactPhone:string;province?:string;city?:string;district?:string;detail:string;isDefault:boolean}
```

## SavedAddress

[types.ts:84](../../client/packages/sdk/src/types.ts#L84)

```typescript
export interface SavedAddress {id:string;contact_name:string;contact_phone:string;province?:string;city?:string;district?:string;detail:string;is_default:boolean|number}
```

## SavedCartItem

[types.ts:85](../../client/packages/sdk/src/types.ts#L85)

```typescript
export interface SavedCartItem {id:string;product_id:string;qty:number;name:string|null;price:Amount|null;stock:number|null;available:boolean|number}
```

## SavedFavorite

[types.ts:86](../../client/packages/sdk/src/types.ts#L86)

```typescript
export interface SavedFavorite {id:string;target_type:'product'|'demand';target_id:string;title:string|null}
```

## InsuranceCompany

[types.ts:88](../../client/packages/sdk/src/types.ts#L88)

```typescript
export interface InsuranceCompany {code:string;name:string;status:'reference_only'}
```

## InsuranceProduct

[types.ts:89](../../client/packages/sdk/src/types.ts#L89)

```typescript
export interface InsuranceProduct {id:string;insurerCode:string;insurerName:string;name:string;kind:string;termsUrl:string;exclusionsUrl:string;claimsUrl:string;status:string;paymentMode:'insurer_direct'}
```

## InsuranceQuoteInput

[types.ts:90](../../client/packages/sdk/src/types.ts#L90)

```typescript
export interface InsuranceQuoteInput {productId:string;subjectType:'trip'|'pk_quote'|'order';subjectId:string;itemName:string;declaredValue:string;platform?:PaymentPlatform}
```

## InsuranceQuote

[types.ts:91](../../client/packages/sdk/src/types.ts#L91)

```typescript
export interface InsuranceQuote {id:string;productId:string;insurerName:string;productName:string;kind:string;declaredValue:string;premium:string;currency:string;termsUrl:string;exclusionsUrl:string;claimsUrl:string;termsVersion:string;expiresAt:string;paymentMode:'insurer_direct';protectionStatus:'not_issued'}
```

## InsuranceApplication

[types.ts:92](../../client/packages/sdk/src/types.ts#L92)

```typescript
export interface InsuranceApplication {id:string;orderId:string;status:string;insurerName:string;productName:string;premium:string;currency:string;checkoutUrl:string|null;policyNumber:string|null;certificateUrl:string|null;claimsUrl:string;coverageRef:string|null;termsVersion:string;startsAt:string|null;endsAt:string|null;platform:PaymentPlatform;paymentMode:'insurer_direct';protectionStatus:'provider_confirmed'|'not_confirmed'}
```

## InsuranceProductConfig

[types.ts:93](../../client/packages/sdk/src/types.ts#L93)

```typescript
export interface InsuranceProductConfig extends Omit<InsuranceProduct,'status'|'paymentMode'> {adapter:string;providerProductCode:string;credentialPrefix:string;serviceHosts:string[];checkoutHosts:string[];enabled:boolean;revision?:number}
```

## InsuranceSettings

[types.ts:94](../../client/packages/sdk/src/types.ts#L94)

```typescript
export interface InsuranceSettings {directory:InsuranceCompany[];products:InsuranceProductConfig[];adapters:{id:string;insurerCode:string}[];paymentMode:'insurer_direct'}
```

## TradeCaseEvent

[types.ts:96](../../client/packages/sdk/src/types.ts#L96)

```typescript
export interface TradeCaseEvent {id:string;actor_role:string;action:string;before_stage:string|null;after_stage:string;created_at:string;payload:{input:{note?:string;reason?:string;description?:string;outcome?:string;evidenceUrls?:string[];refundAmount?:Amount}}}
```

## TradeCaseSummary

[types.ts:97](../../client/packages/sdk/src/types.ts#L97)

```typescript
export interface TradeCaseSummary extends OriginalDispute {round:number;expect_resolution:string;item_name:string;order_no:string;refund_status?:string}
```

## TradeCaseDetail

[types.ts:98](../../client/packages/sdk/src/types.ts#L98)

```typescript
export interface TradeCaseDetail extends TradeCaseSummary {refund_method?:'escrow_wallet';escrowRefund?:EscrowWalletRefund;round:number;expect_resolution:string;respondent_id?:string;respondent_note?:string;evidence_urls:string[];respondent_evidence_urls:string[];deposit_owner?:string;verdict_reason?:string;review_appeal?:string;review_result?:string;negotiation_deadline?:string;resolved_at?:string;reopened_used:boolean|number;goods_amount:Amount;item_name:string;order_no:string;myRole:'buyer'|'merchant'|'operator';events:TradeCaseEvent[];refund_status?:string}
```

## TradeCaseAction

[types.ts:99](../../client/packages/sdk/src/types.ts#L99)

```typescript
export interface TradeCaseAction {expectedRound:number;requestId:string}
```

## TradeCaseResponse

[types.ts:100](../../client/packages/sdk/src/types.ts#L100)

```typescript
export interface TradeCaseResponse extends TradeCaseAction {note:string;evidenceUrls:string[]}
```

## TradeCaseReason

[types.ts:101](../../client/packages/sdk/src/types.ts#L101)

```typescript
export interface TradeCaseReason extends TradeCaseAction {reason:string}
```

## TradeCaseDecision

[types.ts:102](../../client/packages/sdk/src/types.ts#L102)

```typescript
export interface TradeCaseDecision extends TradeCaseReason {outcome:'reject'|'refund_part'|'refund_full'|'resend'|'compensate';refundAmount?:Amount;depositOwner:'return_original'|'buyer'|'vendor'|'none'}
```

## IntegrationField

[types.ts:104](../../client/packages/sdk/src/types.ts#L104)

```typescript
export interface IntegrationField {key:string;secret:boolean;required:boolean;configured:boolean;status:'missing'|'stored'|'unreadable';value?:string}
```

## IntegrationProduct

[types.ts:105](../../client/packages/sdk/src/types.ts#L105)

```typescript
export interface IntegrationProduct {vendor:string;code:string;category:string;mode:'implemented'|'bridge'|'reference_only';docs:string;enabled:boolean;remark:string;updatedAt:string;fields:IntegrationField[]}
```

## IntegrationSettings

[types.ts:106](../../client/packages/sdk/src/types.ts#L106)

```typescript
export interface IntegrationSettings {encryptionReady:boolean;nativeDeliveryManaged?:boolean;products:IntegrationProduct[];specialized:string[]}
```

## IntegrationInput

[types.ts:107](../../client/packages/sdk/src/types.ts#L107)

```typescript
export interface IntegrationInput {enabled:boolean;remark:string;values:Record<string,string>;clearFields:string[]}
```

## UploadedDocument

[types.ts:109](../../client/packages/sdk/src/types.ts#L109)

```typescript
export interface UploadedDocument {publicPath?:string;id:string;purpose:string;contentType:string;byteSize:number;sha256:string}
```

## ShopApplicationInput

[types.ts:110](../../client/packages/sdk/src/types.ts#L110)

```typescript
export interface ShopApplicationInput {businessName:string;creditCode:string;representative:string;licenseUploadId:string;invitationCode?:string;channel:PaymentPlatform;requestId:string}
```

## ShopApplication

[types.ts:111](../../client/packages/sdk/src/types.ts#L111)

```typescript
export interface ShopApplication {id:string;business_name:string;unified_social_credit_code:string;legal_representative:string;license_upload_id:string;invitation_code?:string;application_channel:string;status:string;review_note?:string;reviewed_at?:string;created_at:string;store_id?:string;store_status?:string;license_content_type?:string}
```

## Shop

[types.ts:112](../../client/packages/sdk/src/types.ts#L112)

```typescript
export interface Shop {id:string;name:string;description:string;status:string;matching_fee_rate:Amount;application_id:string;created_at:string}
```

## ShopState

[types.ts:113](../../client/packages/sdk/src/types.ts#L113)

```typescript
export interface ShopState {store:Shop|null;applications:ShopApplication[];products:{id:string;name:string;price:Amount;stock:number;status:string}[]}
```

## ShopStatusInput

[types.ts:114](../../client/packages/sdk/src/types.ts#L114)

```typescript
export interface ShopStatusInput {status:'active'|'paused'|'suspended';expectedStatus:string;reason:string}
```

## PublicShop

[types.ts:115](../../client/packages/sdk/src/types.ts#L115)

```typescript
export interface PublicShop {id:string;name:string;description:string;created_at:string;products:PublicProduct[]}
```

## ProductCheckoutTrip

[types.ts:117](../../client/packages/sdk/src/types.ts#L117)

```typescript
export interface ProductCheckoutTrip {id:string;destination:string;depart_date:string;return_date?:string;remaining_slots:number;capacity_qty:number}
```

## ProductCheckoutInput

[types.ts:118](../../client/packages/sdk/src/types.ts#L118)

```typescript
export interface ProductCheckoutInput {productId:string;tripId:string;quantity:number;contactName:string;contactPhone:string;address:string;expectDate?:string;note?:string;insuranceChoice:'yes'|'waived';insuranceQuoteId?:string}
```

## ProductCheckoutQuote

[types.ts:119](../../client/packages/sdk/src/types.ts#L119)

```typescript
export interface ProductCheckoutQuote {productFingerprint:string;productId:string;tripId:string;storeId:string|null;itemName:string;itemSpec:string|null;unitPrice:string;quantity:number;goodsAmount:string;serviceFee:string;insurancePremium:string;buyerEscrow:string;vendorDeposit:string;currency:'CNY';pricingVersion:'escrow_v2_external_insurance';intentHours:number}
```

## ProductCheckoutConfirm

[types.ts:120](../../client/packages/sdk/src/types.ts#L120)

```typescript
export interface ProductCheckoutConfirm extends ProductCheckoutInput {expectedProductFingerprint:string;requestId:string;expectedUnitPrice:string;expectedBuyerEscrow:string}
```

## PaymentRefund

[types.ts:121](../../client/packages/sdk/src/types.ts#L121)

```typescript
export interface PaymentRefund {id:string;paymentId:string;status:string;baseCurrency:string;baseAmount:string;paymentCurrency:string;paymentAmount:string;reason:string;reviewNote?:string|null;providerRefundId?:string|null;lastError?:string|null;createdAt:string;updatedAt:string}
```

## PaymentRefundInput

[types.ts:122](../../client/packages/sdk/src/types.ts#L122)

```typescript
export interface PaymentRefundInput {paymentId:string;requestId:string;reason:string}
```

## EligiblePaymentRefund

[types.ts:124](../../client/packages/sdk/src/types.ts#L124)

```typescript
export interface EligiblePaymentRefund {id:string;baseAmount:string;paymentAmount:string;paymentCurrency:'CNY';merchantName:string;paidAt:string|null}
```

## PaymentRefundEvent

[types.ts:126](../../client/packages/sdk/src/types.ts#L126)

```typescript
export interface PaymentRefundEvent {id:string;action:string;detail:string;createdAt:string}
```

## ShopAttributedOrder

[types.ts:127](../../client/packages/sdk/src/types.ts#L127)

```typescript
export interface ShopAttributedOrder {id:string;order_no:string;item_name:string;status:string;product_id:string;quantity:number;goods_amount:Amount;service_fee:Amount;currency:'CNY';stock_state:'held'|'consumed'|'released';created_at:string}
```

## CouplerMode

[types.ts:129](../../client/packages/sdk/src/types.ts#L129)

```typescript
export type CouplerMode='SANDBOX'|'LIVE';
```

## CouplerField

[types.ts:130](../../client/packages/sdk/src/types.ts#L130)

```typescript
export interface CouplerField {key:string;label:string;secret:boolean;required:boolean;placeholder?:string;inputType?:'text'|'textarea'|'boolean';options?:{value:string;label:string}[]}
```

## CouplerModeConfiguration

[types.ts:131](../../client/packages/sdk/src/types.ts#L131)

```typescript
export interface CouplerModeConfiguration {revision:number;configRevision?:number;enabled:boolean;selected:boolean;values:Record<string,string>;configuredFields:string[];unreadableFields:string[];updatedAt?:string}
```

## CouplerProvider

[types.ts:132](../../client/packages/sdk/src/types.ts#L132)

```typescript
export interface CouplerProvider {vendor:string;product:string;code:string;name:string;capability:string;docs:string;sdkAvailable:boolean;businessVerified:boolean;consumerWired?:boolean;supportedModes:CouplerMode[];implementedOperations:string[];fields:CouplerField[];configurationByMode:Partial<Record<CouplerMode,CouplerModeConfiguration>>}
```

## CouplerCategory

[types.ts:133](../../client/packages/sdk/src/types.ts#L133)

```typescript
export interface CouplerCategory {code:string;name:string;description:string;providers:CouplerProvider[]}
```

## CouplerRuntime

[types.ts:134](../../client/packages/sdk/src/types.ts#L134)

```typescript
export interface CouplerRuntime {mode:CouplerMode;revision:number}
```

## CouplerSettings

[types.ts:135](../../client/packages/sdk/src/types.ts#L135)

```typescript
export interface CouplerSettings {runtime:CouplerRuntime;categories:CouplerCategory[];encryptionReady?:boolean}
```

## CouplerValidation

[types.ts:136](../../client/packages/sdk/src/types.ts#L136)

```typescript
export interface CouplerValidation {mode:CouplerMode;revision:number;ready:boolean;missingFields:string[];invalidFields:string[];unreadableFields:string[];sdkAvailable:boolean;businessVerified:boolean;checks?:{code:string;status:string;message:string}[]}
```

## CouplerSaveInput

[types.ts:137](../../client/packages/sdk/src/types.ts#L137)

```typescript
export interface CouplerSaveInput {values:Record<string,string>;clearFields:string[];enabled:boolean;selected:boolean;expectedRevision:number}
```

## CouplerRuntimeInput

[types.ts:138](../../client/packages/sdk/src/types.ts#L138)

```typescript
export interface CouplerRuntimeInput {mode:CouplerMode;expectedRevision:number}
```

## EscrowWalletRefund

[types.ts:140](../../client/packages/sdk/src/types.ts#L140)

```typescript
export interface EscrowWalletRefund {id:string;caseId:string;orderId:string;requestId:string;sourceRound:number;amount:string;currency:'CNY';status:'succeeded';refundMethod:'escrow_wallet';destination:'platform_wallet';requiresAccounting:boolean;rewardReviewRequired:true;createdAt:string}
```

## EscrowRefundBudget

[types.ts:141](../../client/packages/sdk/src/types.ts#L141)

```typescript
export type EscrowRefundBudget = EscrowWalletRefund | {caseId:string;orderId:string;currency:'CNY';refundMethod:'escrow_wallet';destination:'platform_wallet';status:'blocked';reasonCode:string} | {caseId:string;orderId:string;currency:'CNY';refundMethod:'escrow_wallet';destination:'platform_wallet';status:'eligible';goodsAmount:string;alreadyRefunded:string;refundableGoods:string;requestedAmount:string;buyerEscrowHeld:string;serviceFeeHeld:string;shippingHeld:string;sourceRound:number;requiresAccounting:boolean;rewardReviewRequired:true};
```

## EscrowRefundEvent

[types.ts:142](../../client/packages/sdk/src/types.ts#L142)

```typescript
export interface EscrowRefundEvent {id:string;action:string;detail:string;createdAt:string}
```
