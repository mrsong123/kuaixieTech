import type { ComponentCustomProperties } from '@vue/runtime-core';
declare module '@vue/runtime-core' {
  interface ComponentCustomProperties { $t: (key:string, vars?:Record<string,string|number>)=>string }
}
export {};
