import {onLoad} from '@dcloudio/uni-app';
import {useTradeCases} from '@kuaixie/ui/useTradeCases';
import {api} from '../api/client';
export function useV15DisputeCreatePage(){const p=useTradeCases(api,async(title,content)=>!!(await uni.showModal({title,content})).confirm);onLoad(q=>{p.draft.orderId=String(q?.id||'')});return p}
