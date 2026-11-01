import {t,getLocale,uiErrorMessage} from '@kuaixie/i18n';
import {createApiClient,ApiError,getBearer,subscribeSession,type Transport,type TransportRequest} from '@kuaixie/sdk';
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
 uni.request({url:javaUrl(req.url),method:req.method,header:req.headers,data:req.data===undefined?undefined:JSON.stringify(req.data),responseType:req.responseType==='arraybuffer'?'arraybuffer':undefined,timeout:req.timeoutMs||15000,
  success:(res)=>{if(res.statusCode>=200&&res.statusCode<300)resolve(res.data as T);else reject(new ApiError(res.statusCode===403?t('common.apiDenied'):`HTTP ${res.statusCode}`,res.statusCode,res.data));},
  fail:(err)=>reject(new ApiError(t('common.apiNetworkError'),0,err))
 });
});
export const api=createApiClient(uniTransport,{baseUrl,getToken:getBearer,getLocale});
export function showError(e:unknown){const detail=uiErrorMessage(e);uni.showModal({title:t('common.apiCallFailure'),content:detail.slice(0,500),showCancel:false})}
export {navigate} from '../router';

// A token can return to the same value after a logout/login. Epochs still invalidate old native callbacks.
let fileSessionEpoch=0;
subscribeSession(()=>{fileSessionEpoch++});
function fileSession(){const token=getBearer(),epoch=fileSessionEpoch;return ()=>getBearer()===token&&fileSessionEpoch===epoch}
function requireFileSession(current:()=>boolean){if(!current())throw new Error(t('upload.sessionChanged'))}
function fileFailure(current:()=>boolean,error:unknown){return current()?error:new Error(t('upload.sessionChanged'))}

export async function uploadPrivateImage(purpose:string,options?:{onSelected?:(path:string)=>void;beforeUpload?:()=>boolean}):Promise<import('@kuaixie/sdk').UploadedDocument>{
 const current=fileSession();
 if(options?.beforeUpload?.()===false)throw new Error(t('productMedia.sessionChanged'));
 const selected=await uni.chooseImage({count:1,sizeType:['compressed'],sourceType:['album','camera']});
 requireFileSession(current);
 const path=selected.tempFilePaths[0];if(!path)throw new Error(t('upload.cancelled'));
 if(options?.beforeUpload?.()===false)throw new Error(t('productMedia.sessionChanged'));
 options?.onSelected?.(path);requireFileSession(current);
 const target=api.files.uploadTarget(purpose);
 return new Promise((resolve,reject)=>{uni.uploadFile({url:javaUrl(target.url),filePath:path,name:'file',header:target.headers,timeout:45000,
  success:response=>{try{requireFileSession(current);if(response.statusCode<200||response.statusCode>=300)throw new ApiError('UPLOAD_SERVICE_UNAVAILABLE',response.statusCode);const data=JSON.parse(response.data);if(typeof data.id!=='string'||!data.id)throw new Error('UPLOAD_SERVICE_UNAVAILABLE');resolve(data)}catch(e){reject(e)}},
  fail:()=>reject(fileFailure(current,new ApiError('API_NETWORK_ERROR',0)))
 })});
}
export async function viewPrivateDocument(id:string,mime='application/pdf'){
 const current=fileSession();
 const path=await downloadPrivateFile(id);requireFileSession(current);
 return new Promise<void>((resolve,reject)=>{
  const done=()=>{try{requireFileSession(current);resolve()}catch(e){reject(e)}};
  const fail=(e:unknown)=>reject(fileFailure(current,e));
  requireFileSession(current);
  if(mime.startsWith('image/'))uni.previewImage({urls:[path],success:done,fail});
  else uni.openDocument({filePath:path,showMenu:false,fileType:'pdf',success:done,fail});
 });
}
export async function downloadPrivateFile(id:string,purpose='store_license'):Promise<string>{
 const current=fileSession(),target=api.files.uploadTarget(purpose);
 const url=target.url.replace(/\/uploads\?.*$/,'/uploads/'+encodeURIComponent(id)+'/content');
 return new Promise((resolve,reject)=>{uni.downloadFile({url:javaUrl(url),header:target.headers,
  success:response=>{try{requireFileSession(current);if(response.statusCode!==200)throw new ApiError('UPLOAD_SERVICE_UNAVAILABLE',response.statusCode);if(!response.tempFilePath)throw new Error(t('upload.unavailable'));resolve(response.tempFilePath)}catch(e){reject(e)}},
  fail:()=>reject(fileFailure(current,new ApiError('API_NETWORK_ERROR',0)))
 })});
}

/** Public images use the same origin/API rewrite as other native requests, without authentication in the URL. */
export function publicMediaUrl(path:string){return javaUrl(api.files.publicUrl(path))}
