/** Intentionally memory-only; do not persist bearer credentials to localStorage or URLs. */
let bearer = '';
export function setBearer(token:string){ bearer=token.trim().replace(/^Bearer\s+/i,'') }
export function getBearer(){return bearer}
export function clearBearer(){bearer=''}
