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
      const response = await fetch(req.url,{method:req.method,headers:req.headers,body:req.method==='GET'?undefined:JSON.stringify(req.data??{}),signal:controller.signal});
      const contentType=response.headers.get('content-type')??'';
      const data: unknown = contentType.includes('json') ? await response.json() : await response.text();
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
  async function request<T>(method:HttpMethod,path:string,options:{body?:unknown;query?:Record<string,unknown>;pathParams?:Record<string,string|number>}={}){
    let actual=normalizePublicPath(path);
    for(const [k,v] of Object.entries(options.pathParams??{}))actual=actual.replace('{'+k+'}',encodeURIComponent(String(v)));
    if(/\{[^}]+\}/.test(actual))throw new Error('API_MISSING_PATH_PARAMS: '+actual);
    const token=settings.getToken?.()?.trim();
    const headers:Record<string,string>={'Accept':'application/json','Accept-Language':settings.getLocale?.()??'zh-CN'};
    if(method!=='GET')headers['Content-Type']='application/json';
    if(token)headers['Authorization']='Bearer '+token;
    return transport<T>({method,url:join(baseUrl,actual+queryString(options.query)),headers,data:options.body,timeoutMs:settings.timeoutMs});
  }
  async function invoke<T=unknown>(op:ApiOperation|string,values:{pathParams?:Record<string,string|number>;query?:Record<string,unknown>;body?:unknown}={}){
    const item=typeof op==='string'?operations.find(x=>x.id===op):op;
    if(!item)throw new Error('API_NOT_FOUND: '+op);
    return request<T>(item.method,item.path,{pathParams:values.pathParams,query:values.query,body:values.body});
  }
  return {
    request, invoke,
    catalog: {
      categories:(limit=100,countryCode?:string)=>request<import('./types').PublicCategory[]>('GET','/public/categories',{query:{limit,countryCode}}),
      products:(limit=30)=>request<import('./types').PublicProduct[]>('GET','/public/products',{query:{limit}}),
      trips:(limit=30,sort:'rank'|'latest'|'score'='rank')=>request<import('./types').PublicTrip[]>('GET','/public/trips',{query:{limit,sort}}),
      maskedProfiles:(ids:string[])=>request<import('./types').BackendRecord[]>('GET','/public/profiles/masked',{query:{ids:ids.join(',')}}),
    },
    portal: {
      // Add-on routes are explicitly optional: the verified base server intentionally has no such routes.
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
      // V14 proposed migration API contract. NOT present in the verified server baseline.
      // A failed HTTP request must remain visible; no static announcements or legal terms.
      announcements:(limit=30)=>request<import('./types').PublicAnnouncement[]>('GET','/public/announcements',{query:{limit}}),
      legalDocument:(key:string)=>request<import('./types').PublicLegalDocument>('GET','/public/legal-documents/{key}',{pathParams:{key}}),
      reviews:(targetId:string,limit=50)=>request<import('./types').PublicReview[]>('GET','/public/reviews',{query:{targetId,limit}}),
    },
    originalV15: {
      // V15 migration candidates, NOT confirmed Java backend routes. Do not report success until HTTP succeeds.
      // Auth requests MUST be rate-limited, identity-verification and consent-checked by the server.
      login:(body:{channel:'phone'|'email';contact:string;password?:string;code?:string})=>request<import('./types').AuthTokenResult>('POST','/auth/login',{body}),
      signup:(body:{channel:'phone'|'email';contact:string;password:string;code:string;role:'buyer'|'carrier';inviteCode?:string})=>request<import('./types').AuthTokenResult>('POST','/auth/register',{body}),
      sendCode:(body:{channel:'phone'|'email';contact:string;purpose:'signin'|'signup'})=>request<{status:string}>('POST','/auth/send-code',{body}),
      createIntent:(body:import('./types').OriginalIntentInput)=>request<{id:string;status:string}>('POST','/me/intents',{body}),
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
