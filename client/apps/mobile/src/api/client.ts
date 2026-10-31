import {t,getLocale,uiErrorMessage} from '@kuaixie/i18n';
import {createApiClient,ApiError,getBearer,type Transport,type TransportRequest} from '@kuaixie/sdk';
/** H5: empty base uses Vite rewrite to kuaixietech-v1/server :8080. WeChat/App have no proxy. */
let baseUrl=import.meta.env.VITE_API_BASE_URL||'';
// #ifdef MP-WEIXIN || APP-PLUS
if(!baseUrl)baseUrl='http://127.0.0.1:8080';
// #endif
function javaUrl(url:string){
  if(!/^https?:\/\//i.test(url))return url;
  return url.replace(/^(https?:\/\/[^/]+)(?!\/api(?:\/|$))(\/)/,'$1/api$2');
}
const uniTransport:Transport=<T>(req:TransportRequest)=>new Promise<T>((resolve,reject)=>{
 uni.request({url:javaUrl(req.url),method:req.method,header:req.headers,data:req.data,timeout:req.timeoutMs||15000,
  success:(res)=>{if(res.statusCode>=200&&res.statusCode<300)resolve(res.data as T);else reject(new ApiError(res.statusCode===403?t('common.apiDenied'):`HTTP ${res.statusCode}`,res.statusCode,res.data));},
  fail:(err)=>reject(new ApiError(t('common.apiNetworkError'),0,err))
 });
});
export const api=createApiClient(uniTransport,{baseUrl,getToken:getBearer,getLocale});
export function showError(e:unknown){const detail=uiErrorMessage(e);uni.showModal({title:t('common.apiCallFailure'),content:detail.slice(0,500),showCancel:false})}
export {navigate} from '../router';
