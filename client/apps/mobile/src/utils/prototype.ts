import {DEFAULT_PREFERENCES,cssAccentContrast,mobilePreferenceStyle as baseStyle,type DisplayPreferences} from '@kuaixie/preferences';
/** Original mini-program design uses sky blue until the user selects an accent. */
export function prototypeAccent(accent:string):string{return accent===DEFAULT_PREFERENCES.accent?'#0ea5e9':accent}
export function prototypeContrast(accent:string){return cssAccentContrast(prototypeAccent(accent))}
/** Mobile-specific visual theme: user-saved preference stays authoritative, desktop keeps its own official-site appearance. */
export function mobilePreferenceStyle(value:DisplayPreferences):string{
  return baseStyle({...value,accent:prototypeAccent(value.accent)});
}
