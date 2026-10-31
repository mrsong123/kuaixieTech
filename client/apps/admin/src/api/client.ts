import {createApiClient,createFetchTransport,getBearer} from '@kuaixie/sdk';
import {getLocale} from '@kuaixie/i18n';
export const createAdminApi = () => createApiClient(createFetchTransport(),{
 baseUrl:import.meta.env.VITE_API_BASE_URL??'',getToken:getBearer,getLocale,
});
