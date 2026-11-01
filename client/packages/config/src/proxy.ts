/** Development-only gateway; browser/mini-program API URLs never contain the backend /api prefix. */
export const PUBLIC_API_PREFIX = /^\/(?:public|rpc|portal|support|me|auth|payment-callback)(?:\/|$)/;
export function createGatewayProxy(target = process.env.API_PROXY_TARGET || 'http://localhost:8080') {
  return {
    '^/(public|rpc|portal|support|me|auth|payment-callback)(?:/|$)': {
      target,
      changeOrigin: true,
      rewrite: (path: string) => '/api' + path,
    },
  };
}
