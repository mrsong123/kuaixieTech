// Generated against user-verified server.zip baseline. Do not enable absent server routes.
import { operations } from './registry';
const endpointSet = new Set(operations.map(o => o.method + ' ' + o.path));
export const backendCapabilities = {
  publicCategories: endpointSet.has('GET /public/categories'),
  publicProducts: endpointSet.has('GET /public/products'),
  publicTrips: endpointSet.has('GET /public/trips'),
  routeShare: endpointSet.has('POST /route-share/publish'),
  routePricing: endpointSet.has('POST /route-share/pricing/preview'),
  communityFeed: endpointSet.has('GET /community/feed'),
  communityPost: endpointSet.has('POST /community/posts'),
  personalShops: endpointSet.has('POST /shops'),
} as const;
