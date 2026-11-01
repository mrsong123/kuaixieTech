import {onLoad} from '@dcloudio/uni-app';
import {useBusinessWorkflow} from '@kuaixie/ui/useBusinessWorkflow';
import {api} from '../api/client';
export function useWorkflowPage(id:string){
 const page=useBusinessWorkflow(api,id,async(title,content)=>Boolean((await uni.showModal({title,content})).confirm));
 onLoad(params=>page.prefill(params??{}));
 return Object.assign(page,{back:()=>uni.navigateBack()});
}
