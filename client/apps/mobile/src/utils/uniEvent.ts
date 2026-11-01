/** UniApp emits detail.value; DOM event declarations alone do not describe this payload. */
export function uniEventValue(event:unknown):unknown {
  if(event&&typeof event==='object'&&'detail' in event){
    const detail=event.detail;
    if(detail&&typeof detail==='object'&&'value' in detail)return detail.value;
  }
  return undefined;
}
