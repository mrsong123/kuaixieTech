export type SettingsPage='appearance'|'general'|'privacy'|'notification'|'storage'|'account'|'profile'|'about'|'help';
export const settingsGroups:ReadonlyArray<{title:string;items:ReadonlyArray<{id:SettingsPage;label:string;desc?:string}>}>= [
 {title:'settings2.section.personal',items:[{id:'profile',label:'settings2.profile',desc:'settings2.serverProfile'},{id:'account',label:'settings2.account'}]},
 {title:'settings2.section.interface',items:[{id:'appearance',label:'settings2.appearance'},{id:'general',label:'settings2.general'},{id:'notification',label:'settings2.notification',desc:'settings2.serverNotification'}]},
 {title:'settings2.section.system',items:[{id:'privacy',label:'settings2.privacy'},{id:'storage',label:'settings2.storage'},{id:'help',label:'settings2.help'},{id:'about',label:'settings2.about'}]},
];
export function openSettings(id:SettingsPage){uni.navigateTo({url:`/pages/settings/${id}/index`})}
