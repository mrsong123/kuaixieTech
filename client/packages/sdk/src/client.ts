import { ApiError, type ApiOperation, type HttpMethod, type Transport, type TransportRequest } from './types';
import { operations } from './registry';
export interface ClientSettings { baseUrl?: string; getToken?: () => string | null | undefined; timeoutMs?: number; getLocale?: () => string }
/** Public request URLs never include /api. Existing Java /api routing is an internal gateway concern. */
export function normalizePublicPath(path: string): string {
  if (/^https?:\/\//i.test(path)) throw new Error('API_ABSOLUTE_PATH_FORBIDDEN');
  const p = '/' + path.replace(/^\/+/, '');
  return p.replace(/^\/api(?=\/|$)/, '') || '/';
}
function join(base: string, path: string): string {
  const url = (base || '').replace(/\/+$/, '').replace(/\/api(?=\/|$)/g, '');
  return url + normalizePublicPath(path);
}
function queryString(values: Record<string,unknown> = {}): string {
  const q = Object.entries(values).filter(([,v])=>v!==undefined && v!==null && String(v)!=='')
      .map(([k,v])=>encodeURIComponent(k)+'='+encodeURIComponent(String(v))).join('&');
  return q ? '?'+q : '';
}
export function createFetchTransport():Transport {
  return async <T>(req: TransportRequest):Promise<T> => {
    const controller = new AbortController(); const timer=setTimeout(()=>controller.abort(),req.timeoutMs??15000);
    try {
      const response = await fetch(req.url,{method:req.method,headers:req.headers,body:req.method==='GET'?undefined:(typeof FormData!=='undefined'&&req.data instanceof FormData?req.data:JSON.stringify(req.data??{})),signal:controller.signal});
      const contentType=response.headers.get('content-type')??'';
      const data: unknown = req.responseType==='arraybuffer'&&response.ok?await response.arrayBuffer():contentType.includes('json') ? await response.json() : await response.text();
      if(!response.ok) throw new ApiError(response.status===401?'API_TOKEN_REJECTED':response.status===403?'API_PERMISSION_DENIED':`API_HTTP_${response.status}`,response.status,data);
      return data as T;
    } catch(e) {
      if(e instanceof ApiError)throw e;
      if(e instanceof Error && e.name==='AbortError')throw new ApiError('API_TIMEOUT',0,e);
      throw new ApiError('API_NETWORK_ERROR',0,e);
    } finally {clearTimeout(timer)}
  };
}
export function createApiClient(transport:Transport, settings:ClientSettings={}){
  const baseUrl=settings.baseUrl??'';
  async function request<T>(method:HttpMethod,path:string,options:{body?:unknown;query?:Record<string,unknown>;pathParams?:Record<string,string|number>;responseType?:'arraybuffer';timeoutMs?:number}={}){
    let actual=normalizePublicPath(path);
    for(const [k,v] of Object.entries(options.pathParams??{}))actual=actual.replace('{'+k+'}',encodeURIComponent(String(v)));
    if(/\{[^}]+\}/.test(actual))throw new Error('API_MISSING_PATH_PARAMS: '+actual);
    const token=settings.getToken?.()?.trim();
    const headers:Record<string,string>={'Accept':'application/json','Accept-Language':settings.getLocale?.()??'zh-CN'};
    if(method!=='GET'&&!(typeof FormData!=='undefined'&&options.body instanceof FormData))headers['Content-Type']='application/json';
    if(token)headers['Authorization']='Bearer '+token;
    const result=await transport<T>({method,url:join(baseUrl,actual+queryString(options.query)),headers,data:options.body,timeoutMs:options.timeoutMs??settings.timeoutMs,responseType:options.responseType});
    if(actual.startsWith('/rpc/')&&(
      typeof result==='string'&&result.startsWith('ERR:')||result===false||
      result!==null&&typeof result==='object'&&'err' in result&&Boolean((result as {err?:unknown}).err)
    ))throw new ApiError('API_BUSINESS_REJECTED',200,result);
    return result;
  }
  async function invoke<T=unknown>(op:ApiOperation|string,values:{pathParams?:Record<string,string|number>;query?:Record<string,unknown>;body?:unknown}={}){
    const item=typeof op==='string'?operations.find(x=>x.id===op):op;
    if(!item)throw new Error('API_NOT_FOUND: '+op);
    return request<T>(item.method,item.path,{pathParams:values.pathParams,query:values.query,body:values.body});
  }
  return {
    request, invoke,
    catalog: {
      categoryOptions:(limit=100,offset=0,search?:string)=>request<import('./types').PublicCategory[]>('GET','/public/category-options',{query:{limit,offset,search}}),
      categories:(limit=100,countryCode?:string)=>request<import('./types').PublicCategory[]>('GET','/public/categories',{query:{limit,countryCode}}),
      product:(id:string)=>request<import('./types').PublicProduct>('GET','/public/products/{id}',{pathParams:{id}}),
      products:(limit=30)=>request<import('./types').PublicProduct[]>('GET','/public/products',{query:{limit}}),
      trips:(limit=30,sort:'rank'|'latest'|'score'='rank')=>request<import('./types').PublicTrip[]>('GET','/public/trips',{query:{limit,sort}}),
      maskedProfiles:(ids:string[])=>request<import('./types').BackendRecord[]>('GET','/public/profiles/masked',{query:{ids:ids.join(',')}}),
    },
    portal: {
      // Portal routes are implemented by the current Java server; database and identity checks still apply.
      demands:(limit=30)=>request<import('./types').PublicDemand[]>('GET','/public/demands',{query:{limit}}),
      communityFeed:(limit=30)=>request<import('./types').PublicCommunityPost[]>('GET','/public/community/feed',{query:{limit}}),
      verifiedRole:()=>request<import('./types').VerifiedPortalRole>('GET','/portal/me'),
      publishDemand:(body:import('./types').PortalDemandInput)=>request<{id:string;status:string}>('POST','/portal/demands',{body}),
      publishTrip:(body:import('./types').PortalTripInput)=>request<{id:string;status:string}>('POST','/portal/trips',{body}),
      postCommunity:(body:{caption:string;mediaKind:'text'|'image'|'video';mediaUrl?:string;posterUrl?:string})=>request<{id:string;status:string}>('POST','/portal/community/posts',{body}),
      adminReviews:()=>request<{demands:import('./types').BackendRecord[];trips:import('./types').BackendRecord[];posts:import('./types').BackendRecord[]}>('GET','/portal/admin/reviews'),
      adminReviewTrip:(tripId:string,approved:boolean)=>request<void>('POST','/portal/admin/trips/review',{body:{tripId,approved}}),
      adminReviewDemand:(demandId:string,approved:boolean)=>request<void>('POST','/portal/admin/demands/review',{body:{demandId,approved}}),
      adminReviewCommunity:(postId:string,approved:boolean)=>request<void>('POST','/portal/admin/community/review',{body:{postId,approved}}),
    },
    prototype: {
      // Public information routes implemented against the current schema and proposed announcements table.
      // A failed HTTP request must remain visible; no static announcements or legal terms.
      announcements:(limit=30)=>request<import('./types').PublicAnnouncement[]>('GET','/public/announcements',{query:{limit}}),
      legalDocument:(key:string)=>request<import('./types').PublicLegalDocument>('GET','/public/legal-documents/{key}',{pathParams:{key}}),
      reviews:(targetId:string,limit=50)=>request<import('./types').PublicReview[]>('GET','/public/reviews',{query:{targetId,limit}}),
    },
    originalV15: {
      // Auth, intent, dispute and stored invite-tier routes are implemented; live database checks still apply.
      // Auth requests MUST be rate-limited, identity-verification and consent-checked by the server.
      login:(body:{channel:'phone'|'email';contact:string;password?:string;code?:string;countryCode?:string})=>request<import('./types').AuthTokenResult>('POST','/auth/login',{body}),
      wechat:(body:{code:string})=>request<import('./types').AuthTokenResult>('POST','/auth/wechat',{body}),
      captcha:()=>request<{id:string;image:string}>('GET','/auth/captcha'),
      signup:(body:{channel:'phone'|'email';contact:string;password:string;code:string;role:'buyer'|'carrier'|'enterprise';inviteCode?:string;countryCode?:string;enterpriseName?:string;businessLicenseNo?:string})=>request<import('./types').AuthTokenResult>('POST','/auth/register',{body}),
      sendCode:(body:{channel:'phone'|'email';contact:string;purpose:'signin'|'signup'|'reset';countryCode?:string;captchaId:string;captchaCode:string})=>request<{status:string;retryAfter?:number}>('POST','/auth/send-code',{body}),
      resetPassword:(body:{channel:'phone'|'email';contact:string;code:string;newPassword:string;countryCode?:string})=>request<import('./types').AuthTokenResult>('POST','/auth/reset-password',{body}),
      recoveryHelp:(body:{channel:'phone'|'email';contact:string;countryCode?:string;captchaId:string;captchaCode:string})=>request<{status:string}>('POST','/auth/recovery-help',{body}),
      quoteIntent:(body:import('./types').OriginalIntentInput)=>request<import('./types').IntentQuote>('POST','/me/intents/quote',{body}),
      createIntent:(body:import('./types').ConfirmedIntentInput)=>request<{id:string;status:string}>('POST','/me/intents',{body}),
      dispute:(id:string)=>request<import('./types').OriginalDispute>('GET','/me/disputes/{id}',{pathParams:{id}}),
      createDispute:(body:import('./types').OriginalDisputeInput)=>request<{id:string;stage:string}>('POST','/me/disputes',{body}),
      inviteTier:()=>request<import('./types').OriginalInviteTier>('GET','/me/invite/tier'),
    },
    support: {
      open:()=>request<import('./types').SupportConversation>('POST','/support/conversations'),
      get:(id:string)=>request<import('./types').SupportConversation>('GET','/support/conversations/{id}',{pathParams:{id}}),
      messages:(id:string,limit=60)=>request<import('./types').SupportMessage[]>('GET','/support/conversations/{id}/messages',{pathParams:{id},query:{limit}}),
      send:(id:string,content:string)=>request<import('./types').SupportSendResult>('POST','/support/conversations/{id}/messages',{pathParams:{id},body:{content}}),
      handoff:(id:string)=>request<import('./types').SupportConversation>('POST','/support/conversations/{id}/handoff',{pathParams:{id}}),
      adminQueue:()=>request<import('./types').SupportQueueItem[]>('GET','/support/admin/queue'),
      agentMessages:(id:string,limit=60)=>request<import('./types').SupportMessage[]>('GET','/support/admin/{id}/messages',{pathParams:{id},query:{limit}}),
      claim:(id:string)=>request<import('./types').SupportConversation>('POST','/support/admin/{id}/claim',{pathParams:{id}}),
      agentReply:(id:string,content:string)=>request<{status:string}>('POST','/support/admin/{id}/reply',{pathParams:{id},body:{content}}),
    },
    productOrders: {
      trips:(id:string,limit=100,offset=0)=>request<import('./types').ProductCheckoutTrip[]>('GET','/public/products/{id}/trips',{pathParams:{id},query:{limit,offset}}),
      quote:(body:import('./types').ProductCheckoutInput)=>request<import('./types').ProductCheckoutQuote>('POST','/me/product-orders/quote',{body}),
      create:(body:import('./types').ProductCheckoutConfirm)=>request<{id:string;status:string}>('POST','/me/product-orders',{body}),
    },
    paymentRefunds: {
      events:(id:string,limit=100,offset=0,admin=false)=>request<import('./types').PaymentRefundEvent[]>('GET',admin?'/portal/admin/payment-refunds/{id}/events':'/me/payment-refunds/{id}/events',{pathParams:{id},query:{limit,offset}}),
      eligible:()=>request<import('./types').EligiblePaymentRefund[]>('GET','/me/payment-refunds/eligible'),
      cancel:(id:string)=>request<import('./types').PaymentRefund>('POST','/me/payment-refunds/{id}/cancel',{pathParams:{id}}),
      mine:(limit=20,offset=0)=>request<import('./types').PaymentRefund[]>('GET','/me/payment-refunds',{query:{limit,offset}}),
      create:(body:import('./types').PaymentRefundInput)=>request<import('./types').PaymentRefund>('POST','/me/payment-refunds',{body}),
      detail:(id:string,admin=false)=>request<import('./types').PaymentRefund>('GET',admin?'/portal/admin/payment-refunds/{id}':'/me/payment-refunds/{id}',{pathParams:{id}}),
      refresh:(id:string,admin=false)=>request<import('./types').PaymentRefund>('POST',admin?'/portal/admin/payment-refunds/{id}/refresh':'/me/payment-refunds/{id}/refresh',{pathParams:{id}}),
      queue:(limit=20,offset=0)=>request<import('./types').PaymentRefund[]>('GET','/portal/admin/payment-refunds',{query:{limit,offset}}),
      review:(id:string,body:{approve:boolean;reason:string})=>request<import('./types').PaymentRefund>('POST','/portal/admin/payment-refunds/{id}/review',{pathParams:{id},body}),
      dispatch:(id:string)=>request<import('./types').PaymentRefund>('POST','/portal/admin/payment-refunds/{id}/dispatch',{pathParams:{id}}),
    },
    aftersalesEscrow: {
      budget:(id:string)=>request<import('./types').EscrowRefundBudget>('GET','/portal/admin/after-sales/{id}/escrow-budget',{pathParams:{id}}),
      execute:(id:string,body:{requestId:string;expectedRound:number;note:string})=>request<import('./types').EscrowWalletRefund>('POST','/portal/admin/after-sales/{id}/escrow-refund',{pathParams:{id},body}),
      mine:(limit=20,offset=0)=>request<import('./types').EscrowWalletRefund[]>('GET','/me/after-sales/escrow-refunds',{query:{limit,offset}}),
      events:(id:string,limit=50,offset=0,admin=false)=>request<import('./types').EscrowRefundEvent[]>('GET',admin?'/portal/admin/after-sales/escrow-refunds/{id}/events':'/me/after-sales/escrow-refunds/{id}/events',{pathParams:{id},query:{limit,offset}}),
    },
    files: {
      publicUrl:(path:string)=>{if(!/^\/public\/media\/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/.test(path))throw new Error('PUBLIC_IMAGE_PATH_INVALID');return join(baseUrl,path)},
      remove:(id:string)=>request<{id:string;status:'deleted'|'deleting'}>('DELETE','/me/uploads/{id}',{pathParams:{id}}),
      deletions:()=>request<import('./types').BackendRecord[]>('GET','/me/uploads/deletions'),
      upload:(file:Blob,name:string,purpose:string)=>{const body=new FormData();body.append('file',file,name);return request<import('./types').UploadedDocument>('POST','/me/uploads',{body,query:{purpose},timeoutMs:45000})},
      mine:(purpose:string)=>request<import('./types').BackendRecord[]>('GET','/me/uploads',{query:{purpose}}),
      download:(id:string)=>request<ArrayBuffer>('GET','/me/uploads/{id}/content',{pathParams:{id},responseType:'arraybuffer',timeoutMs:45000}),
      uploadTarget:(purpose:string)=>({url:join(baseUrl,'/me/uploads'+queryString({purpose})),headers:{Authorization:'Bearer '+(settings.getToken?.()?.trim()||''),'Accept-Language':settings.getLocale?.()??'zh-CN'}}),
    },
    shops: {
      orders:(limit=20,offset=0)=>request<import('./types').ShopAttributedOrder[]>('GET','/me/shop/orders',{query:{limit,offset}}),
      adminOrders:(id:string,limit=20,offset=0)=>request<import('./types').ShopAttributedOrder[]>('GET','/portal/admin/shops/{id}/orders',{pathParams:{id},query:{limit,offset}}),
      mine:()=>request<import('./types').ShopState>('GET','/me/shop'),
      apply:(body:import('./types').ShopApplicationInput)=>request<{id:string;status:string}>('POST','/me/shop/applications',{body}),
      edit:(body:{name:string;description:string})=>request<{status:string}>('PUT','/me/shop',{body}),
      state:(id:string,body:import('./types').ShopStatusInput,admin=false)=>request<{status:string}>('PUT',admin?'/portal/admin/shops/{id}/status':'/me/shop/{id}/status',{pathParams:{id},body}),
      linked:(limit=100,offset=0)=>request<import('./types').ShopState['products']>('GET','/me/shop/products',{query:{limit,offset}}),
      link:(productId:string)=>request<{status:string}>('POST','/me/shop/products',{body:{productId}}),
      unlink:(id:string)=>request<{status:string}>('DELETE','/me/shop/products/{id}',{pathParams:{id}}),
      public:(id:string,limit=100,offset=0)=>request<import('./types').PublicShop>('GET','/public/shops/{id}',{pathParams:{id},query:{limit,offset}}),
      queue:(limit=20,offset=0)=>request<import('./types').ShopApplication[]>('GET','/portal/admin/shops/applications',{query:{limit,offset}}),
      review:(id:string,approve:boolean,reason:string)=>request<{id:string;status:string}>('POST','/portal/admin/shops/applications/{id}/review',{pathParams:{id},body:{approve,reason}}),
    },
    integrationCenter: {
      catalog:()=>request<import('./types').CouplerSettings>('GET','/portal/admin/integration-center'),
      runtime:(body:import('./types').CouplerRuntimeInput)=>request<import('./types').CouplerRuntime>('PUT','/portal/admin/integration-center/runtime',{body}),
      save:(category:string,provider:string,mode:import('./types').CouplerMode,body:import('./types').CouplerSaveInput)=>request<{revision:number}>('PUT','/portal/admin/integration-center/{category}/{provider}/{mode}',{pathParams:{category,provider,mode},body}),
      validate:(category:string,provider:string,mode:import('./types').CouplerMode)=>request<import('./types').CouplerValidation>('POST','/portal/admin/integration-center/{category}/{provider}/{mode}/validate',{pathParams:{category,provider,mode}}),
    },
    integrations: {
      settings:()=>request<import('./types').IntegrationSettings>('GET','/portal/admin/integrations'),
      save:(vendor:string,product:string,body:import('./types').IntegrationInput)=>request<{status:string}>('PUT','/portal/admin/integrations/{vendor}/{product}',{pathParams:{vendor,product},body}),
      smsDeliveries:()=>request<import('./types').BackendRecord[]>('GET','/portal/admin/integrations/sms-deliveries'),
    },
    cases: {
      mine:(limit=20,offset=0)=>request<import('./types').TradeCaseSummary[]>('GET','/me/disputes',{query:{limit,offset}}),
      queue:(limit=20,offset=0)=>request<import('./types').TradeCaseSummary[]>('GET','/portal/admin/disputes',{query:{limit,offset}}),
      detail:(id:string,admin=false)=>request<import('./types').TradeCaseDetail>('GET',admin?'/portal/admin/disputes/{id}':'/me/disputes/{id}',{pathParams:{id}}),
      create:(body:import('./types').OriginalDisputeInput)=>request<{id:string;stage:string}>('POST','/me/disputes',{body}),
      respond:(id:string,body:import('./types').TradeCaseResponse)=>request<{id:string;stage:string}>('POST','/me/disputes/{id}/respond',{pathParams:{id},body}),
      escalate:(id:string,body:import('./types').TradeCaseReason)=>request<{id:string;stage:string}>('POST','/me/disputes/{id}/escalate',{pathParams:{id},body}),
      appeal:(id:string,body:import('./types').TradeCaseReason)=>request<{id:string;stage:string}>('POST','/me/disputes/{id}/appeal',{pathParams:{id},body}),
      finish:(id:string,body:import('./types').TradeCaseAction&{note:string})=>request<{id:string;stage:string}>('POST','/me/disputes/{id}/finish',{pathParams:{id},body}),
      decide:(id:string,body:import('./types').TradeCaseDecision)=>request<{id:string;stage:string}>('POST','/portal/admin/disputes/{id}/decision',{pathParams:{id},body}),
      events:(id:string,limit=100,offset=0,admin=false)=>request<import('./types').TradeCaseEvent[]>('GET',admin?'/portal/admin/disputes/{id}/events':'/me/disputes/{id}/events',{pathParams:{id},query:{limit,offset}}),
    },
    insurance: {
      companies:()=>request<import('./types').InsuranceCompany[]>('GET','/public/insurance/companies'),
      products:(platform:import('./types').PaymentPlatform='web')=>request<import('./types').InsuranceProduct[]>('GET','/public/insurance/products',{query:{platform}}),
      quote:(body:import('./types').InsuranceQuoteInput)=>request<import('./types').InsuranceQuote>('POST','/me/insurance/quotes',{body}),
      ownQuote:(id:string)=>request<import('./types').InsuranceQuote>('GET','/me/insurance/quotes/{id}',{pathParams:{id}}),
      pkSubject:(demandId:string,quoteId:string)=>request<{subjectId:string;subjectType:'pk_quote';itemName:string;declaredValue:string}>('GET','/me/insurance/pk-subject',{query:{demandId,quoteId}}),
      apply:(body:{orderId:string;quoteId:string;requestId:string;termsAccepted:boolean;disclosuresAccepted:boolean})=>request<import('./types').InsuranceApplication>('POST','/me/insurance/applications',{body}),
      forOrder:(orderId:string)=>request<{application:import('./types').InsuranceApplication|null}>('GET','/me/insurance/orders/{orderId}',{pathParams:{orderId}}),
      mine:()=>request<import('./types').InsuranceApplication[]>('GET','/me/insurance/applications'),
      status:(id:string)=>request<import('./types').InsuranceApplication>('GET','/me/insurance/applications/{id}',{pathParams:{id}}),
      record:(id:string)=>request<import('./types').InsuranceApplication>('GET','/me/insurance/applications/{id}/record',{pathParams:{id}}),
      settings:()=>request<import('./types').InsuranceSettings>('GET','/portal/admin/insurance/settings'),
      saveProduct:(body:import('./types').InsuranceProductConfig)=>request<{id:string}>('PUT','/portal/admin/insurance/products',{body}),
      consents:()=>request<import('./types').BackendRecord[]>('GET','/me/insurance/consents'),
      adminApplications:()=>request<import('./types').BackendRecord[]>('GET','/portal/admin/insurance/applications'),
    },
    saved: {
      addresses:(limit=20,offset=0)=>request<import('./types').SavedAddress[]>('GET','/me/addresses',{query:{limit,offset}}),
      createAddress:(body:import('./types').AddressInput)=>request<{id:string}>('POST','/me/addresses',{body}),
      updateAddress:(id:string,body:import('./types').AddressInput)=>request<{id:string}>('PUT','/me/addresses/{id}',{pathParams:{id},body}),
      deleteAddress:(id:string)=>request<void>('DELETE','/me/addresses/{id}',{pathParams:{id}}),
      cart:(limit=20,offset=0)=>request<import('./types').SavedCartItem[]>('GET','/me/cart',{query:{limit,offset}}),
      setCart:(product:string,quantity:number)=>request<void>('PUT','/me/cart/{product}',{pathParams:{product},body:{quantity}}),
      deleteCart:(product:string)=>request<void>('DELETE','/me/cart/{product}',{pathParams:{product}}),
      favorites:(limit=20,offset=0)=>request<import('./types').SavedFavorite[]>('GET','/me/favorites',{query:{limit,offset}}),
      favorite:(type:'product'|'demand',target:string)=>request<void>('PUT','/me/favorites/{type}/{target}',{pathParams:{type,target}}),
      deleteFavorite:(type:'product'|'demand',target:string)=>request<void>('DELETE','/me/favorites/{type}/{target}',{pathParams:{type,target}}),
    },
    products: {
      mine:(limit=20,offset=0)=>request<import('./types').OwnedProduct[]>('GET','/me/products',{query:{limit,offset}}),
      create:(body:import('./types').ProductPublishInput)=>request<{id:string;status:'off'}>('POST','/me/products',{body}),
      update:(id:string,body:import('./types').ProductPublishInput)=>request<{id:string;status:'off'}>('PUT','/me/products/{id}',{pathParams:{id},body}),
      state:(id:string,status:'on'|'off')=>request<{id:string;status:'on'|'off'}>('PUT','/me/products/{id}/status',{pathParams:{id},body:{status}}),
    },
    payments: {
      currencies:()=>request<import('./types').CurrencyInfo[]>('GET','/public/payments/currencies'),
      methods:(platform:import('./types').PaymentPlatform)=>request<import('./types').PaymentMethod[]>('GET','/public/payments/methods',{query:{platform}}),
      rate:(base:string,currency:string)=>request<import('./types').ExchangeRate>('GET','/public/payments/rate',{query:{base,currency}}),
      quote:(body:{merchantId:string;paymentCurrency:string;amountCny:string})=>request<import('./types').FundingQuote>('POST','/me/payments/quotes',{body}),
      create:(body:{quoteId:string;requestId:string})=>request<import('./types').FundingPayment>('POST','/me/payments',{body}),
      history:()=>request<import('./types').FundingPayment[]>('GET','/me/payments'),
      status:(id:string)=>request<import('./types').FundingPayment>('GET','/me/payments/{id}',{pathParams:{id}}),
      settings:()=>request<import('./types').PaymentSettings>('GET','/portal/admin/payments/settings'),
      saveMerchant:(body:import('./types').MerchantConfig)=>request<{id:string}>('PUT','/portal/admin/payments/merchants',{body}),
      saveFx:(body:import('./types').FxConfig)=>request<{id:string}>('PUT','/portal/admin/payments/fx-sources',{body}),
    },
    account: {
      profile:()=>request<import('./types').MyProfile>('GET','/me/profile'),
      updateProfile:(body:{nickname?:string;city?:string})=>request<import('./types').MyProfile>('PUT','/me/profile',{body}),
      orders:(role:'all'|'buyer'|'carrier'='all',limit=20,offset=0)=>request<import('./types').MyOrder[]>('GET','/me/orders',{query:{role,limit,offset}}),
      order:(id:string)=>request<import('./types').MyOrder>('GET','/me/orders/{id}',{pathParams:{id}}),
      demands:(limit=20,offset=0)=>request<import('./types').MyDemand[]>('GET','/me/demands',{query:{limit,offset}}),
      trips:(limit=20,offset=0)=>request<import('./types').MyTrip[]>('GET','/me/trips',{query:{limit,offset}}),
      wallet:()=>request<import('./types').MyWallet>('GET','/me/wallet'),
      ledger:(limit=20,offset=0)=>request<import('./types').MyLedger[]>('GET','/me/wallet/ledger',{query:{limit,offset}}),
      notifications:(limit=20,offset=0)=>request<import('./types').MyNotification[]>('GET','/me/notifications',{query:{limit,offset}}),
      markNotificationRead:(id:string)=>request<{status:string}>('POST','/me/notifications/{id}/read',{pathParams:{id}}),
    },
    rpc:(path:string,body:unknown={})=>request<unknown>('POST','/rpc/'+path.replace(/^\//,''),{body}),
  };
}
export type ApiClient = ReturnType<typeof createApiClient>;
