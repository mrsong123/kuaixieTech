import {createApiClient,createFetchTransport,getBearer} from '@kuaixie/sdk';
import {getLocale} from '@kuaixie/i18n';
/** Browser-visible URLs omit /api. The development/production gateway adds it upstream. */
export const createSiteApi = () => createApiClient(createFetchTransport(),{
  baseUrl:import.meta.env.VITE_API_BASE_URL??'',getToken:getBearer,getLocale,
});
