/** User-controlled visual preferences. Never used as business data or sent as a transaction. */
export type ThemeMode='system'|'light'|'dark';
export type FontChoice='system'|'modern'|'reading';
export interface DisplayPreferences {
  theme:ThemeMode;
  accent:string; // verified 6-digit hex, not a palette imposed by platform
  font:FontChoice;
  scale:number; // 0.90–1.30; care mode enforces an accessible minimum
  care:boolean;
  reduceMotion:boolean;
}
export const preferenceStorageKey='kx.display.v1';
export const DEFAULT_PREFERENCES:Readonly<DisplayPreferences>=Object.freeze({theme:'system',accent:'#42575f',font:'system',scale:1,care:false,reduceMotion:false});
type StorageAdapter={getItem(key:string):string|null;setItem(key:string,value:string):void};
let prefs:DisplayPreferences={...DEFAULT_PREFERENCES};
let storage:StorageAdapter|undefined;
const listeners=new Set<(value:DisplayPreferences)=>void>();
export const validHex=(value:unknown):value is string=>typeof value==='string'&&/^#[0-9a-fA-F]{6}$/.test(value);
const themes:ThemeMode[]=['system','light','dark'];
const fonts:FontChoice[]=['system','modern','reading'];
export function normalizePreferences(value:unknown):DisplayPreferences {
  const raw=(value&&typeof value==='object'?value:{}) as Partial<DisplayPreferences>;
  return {
    theme:themes.includes(raw.theme as ThemeMode)?raw.theme!:DEFAULT_PREFERENCES.theme,
    accent:validHex(raw.accent)?raw.accent.toLowerCase():DEFAULT_PREFERENCES.accent,
    font:fonts.includes(raw.font as FontChoice)?raw.font!:DEFAULT_PREFERENCES.font,
    scale:typeof raw.scale==='number'&&Number.isFinite(raw.scale)?Math.min(1.3,Math.max(.9,Math.round(raw.scale*20)/20)):1,
    care:raw.care===true,
    reduceMotion:raw.reduceMotion===true,
  };
}
function emit(){const value={...prefs};listeners.forEach(listener=>listener(value));applyDocumentPreferences(value)}
export function initPreferences(adapter?:StorageAdapter):DisplayPreferences {
  storage=adapter;
  try {prefs=normalizePreferences(JSON.parse(storage?.getItem(preferenceStorageKey)||'null'))}catch{prefs={...DEFAULT_PREFERENCES}}
  emit();return getPreferences();
}
export function getPreferences():DisplayPreferences {return {...prefs}}
export function setPreferences(next:Partial<DisplayPreferences>):DisplayPreferences {
  prefs=normalizePreferences({...prefs,...next});
  try{storage?.setItem(preferenceStorageKey,JSON.stringify(prefs))}catch{/* Private mode can deny persistence. */}
  emit();return getPreferences();
}
export function resetPreferences(){return setPreferences({...DEFAULT_PREFERENCES})}
export function subscribePreferences(fn:(value:DisplayPreferences)=>void):()=>void {listeners.add(fn);return ()=>listeners.delete(fn)}
export function effectiveScale(value:DisplayPreferences=prefs):number {return value.care?Math.max(1.15,value.scale):value.scale}
export function cssAccentContrast(hex:string):'#ffffff'|'#111827' {
  const rgb=[1,3,5].map(i=>parseInt(hex.slice(i,i+2),16)/255);
  const lum=rgb.map(n=>n<=.04045?n/12.92:((n+.055)/1.055)**2.4);
  return .2126*lum[0]+.7152*lum[1]+.0722*lum[2]>.179?'#111827':'#ffffff';
}
/** Guard DOM access: same module is imported by UniApp mini programs and H5. */
export function applyDocumentPreferences(value:DisplayPreferences=prefs):void {
  if(typeof document==='undefined')return;
  const root=document.documentElement;
  root.dataset.theme=value.theme;
  root.dataset.font=value.font;
  root.dataset.care=String(value.care);
  root.dataset.reduceMotion=String(value.reduceMotion);
  root.style.setProperty('--kx-accent',value.accent);
  root.style.setProperty('--kx-accent-text',cssAccentContrast(value.accent));
  root.style.setProperty('--kx-user-scale',String(effectiveScale(value)));
}
export function mobilePreferenceStyle(value:DisplayPreferences=prefs):string {
  const effective=effectiveScale(value);
  const font=value.font==='reading'?"'Noto Sans SC','PingFang SC','Microsoft YaHei UI',sans-serif":value.font==='modern'?"Inter,'Noto Sans SC','PingFang SC',sans-serif":"system-ui,'PingFang SC','Microsoft YaHei UI',sans-serif";
  return `--kx-accent:${value.accent};--kx-accent-text:${cssAccentContrast(value.accent)};--kx-user-scale:${effective};font-family:${font};font-size:${Math.round(14*effective)}px;`;
}
