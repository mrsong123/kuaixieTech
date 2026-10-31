import {inject,provide,type InjectionKey} from 'vue';
import type {AdminStore} from './useAdminStore';
const KEY:InjectionKey<AdminStore>=Symbol('admin-store');
export function provideAdminStore(value:AdminStore){provide(KEY,value)}
export function useAdminContext():AdminStore{const value=inject(KEY);if(!value)throw new Error('AdminStore provider missing');return value}
