import {inject,provide,type InjectionKey} from 'vue';
import type {SiteStore} from './useSiteStore';
const KEY:InjectionKey<SiteStore>=Symbol('site-store');
export function provideSiteStore(store:SiteStore){provide(KEY,store)}
export function useSiteContext():SiteStore {const value=inject(KEY);if(!value)throw new Error('SiteStore provider missing');return value}
