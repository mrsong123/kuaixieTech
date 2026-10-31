import type {ApiClient} from '../client';
/** Public catalogue endpoints. All requests are handled by the common transport. */
export const catalogApi = (client:ApiClient) => client.catalog;
