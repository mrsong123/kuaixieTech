import {getBearer,subscribeSession} from '@kuaixie/sdk';
export type PublishMode='demand'|'trip'|'life'|'product';
export type PublishRegion='domestic'|'international';
type Entry={mode:PublishMode;region?:PublishRegion};
// A tab navigation cannot carry query parameters. Keep only a one-shot, session-bound choice in memory.
let queued:(Entry&{session:string})|null=null;
subscribeSession(()=>{queued=null});
export function queuePublishEntry(mode:PublishMode,region?:PublishRegion):boolean{
 if(!['demand','trip','life','product'].includes(mode)||region!==undefined&&!['domestic','international'].includes(region))return false;
 queued={mode,...(region?{region}:{}),session:getBearer()};return true;
}
export function consumePublishEntry():Entry|null{const entry=queued;queued=null;if(!entry||entry.session!==getBearer())return null;return {mode:entry.mode,...(entry.region?{region:entry.region}:{})}}
export function publishLoginPath(mode:PublishMode,region?:PublishRegion):string{
 if(!['demand','trip','life','product'].includes(mode)||region!==undefined&&!['domestic','international'].includes(region))return '/pages/login/index';
 return '/pages/login/index?publishMode='+encodeURIComponent(mode)+(region?'&publishRegion='+encodeURIComponent(region):'');
}
export function readPublishLoginEntry(query:Record<string,unknown>):Entry|null{
 const mode=query.publishMode,region=query.publishRegion;
 if(typeof mode!=='string'||!['demand','trip','life','product'].includes(mode)||region!==undefined&&(typeof region!=='string'||!['domestic','international'].includes(region)))return null;
 return {mode:mode as PublishMode,...(region?{region:region as PublishRegion}:{})};
}
