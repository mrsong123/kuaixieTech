/** Development-only gateway; browser/mini-program API URLs never contain the backend /api prefix. */
export const PUBLIC_API_PREFIX = /^\/(?:public|rpc|portal|support|me|auth)(?:\/|$)/;
export function createGatewayProxy(target = process.env.API_PROXY_TARGET || 'http://localhost:8080') {
  return {
    '^/(public|rpc|portal|support|me|auth)(?:/|$)': {
      target,
      changeOrigin: true,
      rewrite: (path: string) => '/api' + path,
    },
  };
}
