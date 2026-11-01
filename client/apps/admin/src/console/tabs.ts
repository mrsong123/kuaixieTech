export type AdminGroupKey='core'|'review'|'trade'|'growth';
export type AdminTabKey=
  |'overview'|'carrierKyc'|'buyerKyc'|'enterpriseKyc'|'trips'|'proofRecipes'|'products'|'categories'|'errorLogs'
  |'paymentRefunds'|'shops'|'orders'|'cancels'|'disputes'|'support'|'invoices'|'violations'|'privacyPool'|'pools'|'demands'|'notifyQueue'|'withdrawals'|'community'
  |'distribution'|'users'|'finance'|'invite'|'broadcast'|'channelConfigs'|'locales';
export type Section=AdminTabKey|'settings'|'all'|'businessCoverage';

export const ADMIN_GROUPS:{key:AdminGroupKey;labelKey:string;tabs:{key:AdminTabKey;labelKey:string;badge?:string}[]}[]=[
  {key:'core',labelKey:'admin.group.core',tabs:[{key:'overview',labelKey:'admin.tab.overview'}]},
  {key:'review',labelKey:'admin.group.review',tabs:[
    {key:'carrierKyc',labelKey:'admin.tab.carrierKyc',badge:'todo_carrier_kyc'},
    {key:'buyerKyc',labelKey:'admin.tab.buyerKyc',badge:'todo_buyer_kyc'},
    {key:'enterpriseKyc',labelKey:'admin.tab.enterpriseKyc'},
    {key:'trips',labelKey:'admin.tab.trips',badge:'todo_trips'},
    {key:'proofRecipes',labelKey:'admin.tab.proofRecipes'},
    {key:'products',labelKey:'admin.tab.products'},
    {key:'categories',labelKey:'admin.tab.categories'},
    {key:'errorLogs',labelKey:'admin.tab.errorLogs'},
  ]},
  {key:'trade',labelKey:'admin.group.trade',tabs:[
    {key:'paymentRefunds',labelKey:'refund.adminTitle'},
    {key:'shops',labelKey:'shop.review'},
    {key:'orders',labelKey:'admin.tab.orders',badge:'risk_orders'},
    {key:'cancels',labelKey:'admin.tab.cancels',badge:'todo_cancels'},
    {key:'disputes',labelKey:'admin.tab.disputes',badge:'todo_disputes'},
    {key:'support',labelKey:'admin.tab.support',badge:'todo_support'},
    {key:'invoices',labelKey:'admin.tab.invoices',badge:'todo_invoices'},
    {key:'violations',labelKey:'admin.tab.violations',badge:'todo_violations'},
    {key:'privacyPool',labelKey:'admin.tab.privacyPool'},
    {key:'pools',labelKey:'admin.tab.pools',badge:'todo_pools'},
    {key:'demands',labelKey:'admin.tab.demands',badge:'todo_demands'},
    {key:'notifyQueue',labelKey:'admin.tab.notifyQueue'},
    {key:'withdrawals',labelKey:'admin.tab.withdrawals'},
    {key:'community',labelKey:'admin.tab.community'},
  ]},
  {key:'growth',labelKey:'admin.group.growth',tabs:[
    {key:'distribution',labelKey:'admin.tab.distribution'},
    {key:'users',labelKey:'admin.tab.users'},
    {key:'finance',labelKey:'admin.tab.finance'},
    {key:'invite',labelKey:'admin.tab.invite'},
    {key:'broadcast',labelKey:'admin.tab.broadcast'},
    {key:'channelConfigs',labelKey:'admin.tab.channelConfigs'},
    {key:'locales',labelKey:'admin.tab.locales'},
  ]},
];
