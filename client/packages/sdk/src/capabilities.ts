// Declared routes only; enabled services, credentials and permissions are checked at runtime.
import {declaredBackendRoutes} from './backendRoutes';
const endpointSet = new Set(declaredBackendRoutes.map(o => o.method + ' ' + o.path));
export const backendCapabilities = {
  publicCategories: endpointSet.has('GET /public/categories'),
  publicProducts: endpointSet.has('GET /public/products'),
  publicTrips: endpointSet.has('GET /public/trips'),
  routeShare: endpointSet.has('POST /route-share/publish'),
  routePricing: endpointSet.has('POST /route-share/pricing/preview'),
  communityFeed: endpointSet.has('GET /public/community/feed'),
  communityPost: endpointSet.has('POST /portal/community/posts'),
  productPublishing: endpointSet.has('POST /me/products'),
  walletFunding: endpointSet.has('POST /me/payments'),
  productCheckout:endpointSet.has('POST /me/product-orders'),
  rechargeRefunds:endpointSet.has('POST /me/payment-refunds'),
  personalShops: endpointSet.has('POST /me/shop/applications')&&endpointSet.has('GET /public/shops/{id}'),
} as const;
