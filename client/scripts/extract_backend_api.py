#!/usr/bin/env python3
"""Re-generate API registry from the supplied Java Controller source tree.
Usage: python scripts/extract_backend_api.py /path/to/backend/src/main/java/com/kuaixietech/controller
"""
import pathlib,re,json,sys,csv
root=pathlib.Path(sys.argv[1]); target=pathlib.Path(__file__).resolve().parents[1]
operations=[]
category_overrides={'PublicCatalogController':'公开目录','RouteShareController':'顺路互助','TradeRpcController':'交易与PK','Batch11RpcController':'订单履约','Batch12RpcController':'协助与互动','Batch10RpcController':'业务运营','AdminFinanceRpcController':'运营财务','KycRpcController':'实名认证','IdentitySettingsController':'账户安全','InviteReadRpcController':'邀请与团队'}
labels={'categories':'国家分类','products':'商品目录','trips':'公开行程','profiles/masked':'公开脱敏档案','pricing/preview':'顺路参考计价','publish':'发布顺路求购','createRequest':'关联旧求购','replaceStops':'设置行程途经点','matches':'匹配顺路行程','offers':'查看顺路报价','offer':'提交顺路报价','accept':'确认报价/预留','booking':'查看行程预留','cancelBooking':'撤销预留','cancel':'取消需求'}
endpoint_labels={
 '/api/rpc/admin_grant_points':'管理端发放积分',
 '/api/rpc/admin_invite_tier_distribution':'邀请等级分布统计',
 '/api/rpc/admin_issue_invoice':'审核并开具发票',
 '/api/rpc/admin_list_violations':'查询违规记录',
 '/api/rpc/admin_locale_remove_rpc':'删除语言地区配置',
 '/api/rpc/admin_locale_set_active_rpc':'启用或停用语言',
 '/api/rpc/admin_locale_upsert_rpc':'新增或更新语言配置',
 '/api/rpc/admin_review_enterprise':'审核企业资质',
 '/api/rpc/admin_review_violation':'处理违规记录',
 '/api/rpc/admin_stats':'运营统计总览',
 '/api/rpc/confirm_meetup':'确认当面交接',
 '/api/rpc/get_order_evidence':'查看订单证据',
 '/api/rpc/issue_meetup_code':'生成交接验证码',
 '/api/rpc/log_privacy_call':'记录隐私通话',
 '/api/rpc/propose_shipping_fee':'提出运费方案',
 '/api/rpc/record_arrival_proof':'记录到达凭证',
 '/api/rpc/request_invoice':'申请开具发票',
 '/api/rpc/respond_shipping_fee':'响应运费协商',
 '/api/rpc/accept_order_atomic':'原子接单',
 '/api/rpc/award_pk':'选定中标报价',
 '/api/rpc/close_pk':'结束报价 PK',
 '/api/rpc/create_publish_deposit':'创建发布保证金',
 '/api/rpc/refund_publish_deposit':'申请退回发布保证金',
 '/api/rpc/submit_pk_quote':'提交 PK 报价',
 '/api/public/categories':'国家/商品分类查询',
 '/api/public/products':'公开商品目录',
 '/api/public/profiles/masked':'查询脱敏用户资料',
 '/api/public/trips':'公开行程目录',
 '/api/rpc/batch12/accept_assist':'接受协助请求',
 '/api/rpc/batch12/accept_order_call':'接受订单语音通话',
 '/api/rpc/batch12/add_post_comment':'发布动态评论',
 '/api/rpc/batch12/cancel_assist':'取消协助请求',
 '/api/rpc/batch12/create_assist_request':'创建协助请求',
 '/api/rpc/batch12/finish_assist':'确认协助完成',
 '/api/rpc/batch12/get_or_bind_privacy_number':'获取或绑定隐私号码',
 '/api/rpc/batch12/hangup_order_call':'挂断订单通话',
 '/api/rpc/batch12/match_join_application':'匹配同行申请',
 '/api/rpc/batch12/reject_order_call':'拒绝订单通话',
 '/api/rpc/batch12/request_verification_code':'申请身份验证码',
 '/api/rpc/batch12/screen_chat_content':'筛查聊天内容',
 '/api/rpc/batch12/start_order_call':'发起订单通话',
 '/api/rpc/batch12/submit_assist_handover_a':'提交协助交接凭证',
 '/api/rpc/batch12/submit_carrier_kyc':'申请带货人认证',
 '/api/rpc/batch12/submit_enterprise_verify':'申请企业认证',
 '/api/rpc/admin_review_buyer_kyc':'审核买家实名认证',
 '/api/rpc/issue_face_challenge':'下发人脸核验挑战',
 '/api/rpc/record_face_verification':'记录人脸核验结果',
 '/api/rpc/submit_buyer_kyc':'提交买家实名认证',
 '/api/rpc/upsert_kyc_doc_with_challenge':'提交身份资料与挑战码',
 '/api/rpc/admin_resolve_cancel':'管理员裁决取消申请',
 '/api/rpc/confirm_purchase':'确认采购完成',
 '/api/rpc/confirm_ship':'确认订单发货',
 '/api/rpc/consume_invite_code':'兑换邀请码',
 '/api/rpc/get_peer_profile':'查看交易对方资料',
 '/api/rpc/open_peer_thread':'开启交易双方会话',
 '/api/rpc/record_boarding_proof':'提交登机凭证',
 '/api/rpc/record_delivery_proof':'提交交付凭证',
 '/api/rpc/request_cancel_v2':'申请取消订单',
 '/api/rpc/request_no_boarding_refund':'未登机退款申请',
 '/api/rpc/verify_contact_code':'验证联系渠道验证码',
 '/api/rpc/identity/change_password':'修改当前密码',
 '/api/rpc/identity/rebind_contact':'换绑手机或邮箱',
 '/api/rpc/identity/reset_own_password_by_code':'通过验证码重置密码',
 '/api/rpc/admin_adjust_balance':'管理员调整余额',
 '/api/rpc/admin_commission_rows':'查询平台佣金流水',
 '/api/rpc/admin_dissolve_pool':'管理员解散拼单池',
 '/api/rpc/get_invite_code_owner':'查询邀请码归属',
 '/api/rpc/get_operator_invite_code':'查询运营邀请码',
 '/api/rpc/list_team_members':'查看团队成员',
 '/api/route-share/bookings/{bookingId}':'查看顺路预留状态',
 '/api/route-share/bookings/{bookingId}/cancel':'撤销顺路预留',
 '/api/route-share/offers/{offerId}/accept':'买家接受顺路报价',
 '/api/route-share/pricing/preview':'顺路服务参考计价',
 '/api/route-share/publish':'发布普通顺路求购',
 '/api/route-share/requests':'关联已存在的非 PK 求购',
 '/api/route-share/requests/{demandId}/cancel':'取消顺路需求',
 '/api/route-share/requests/{demandId}/matches':'匹配可承接的顺路行程',
 '/api/route-share/requests/{demandId}/offers':'顺路报价或查询报价',
 '/api/route-share/trips/{tripId}/stops':'维护行程途经地点'
}
for fp in sorted(root.glob('*Controller.java')):
 text=fp.read_text(encoding='utf-8'); base_match=re.search(r'@RequestMapping\("([^\"]+)"\)',text)
 if not base_match: continue
 base=base_match.group(1)
 records={name:fields for name,fields in re.findall(r'public record\s+(\w+)\((.*?)\)\s*\{',text,re.S)}
 for match in re.finditer(r'@(GetMapping|PostMapping|PutMapping|DeleteMapping)\("([^\"]+)"\)\s*public\s+([\w<>?,.\s]+?)\s+(\w+)\s*\((.*?)\)\s*\{',text,re.S):
  http,path,ret,func,params=match.groups(); full=base+path
  pathvars=re.findall(r'\{(\w+)\}',path)
  bodytype=re.search(r'@RequestBody\s+(?:List<)?([\w.]+)',params)
  body=[]; raw=False
  if bodytype:
   typename=bodytype.group(1).split('.')[-1]
   definition=records.get(typename)
   if not definition:
    # RouteShareService records
    lookup={'Preview':'String serviceMode,BigDecimal extraKm,Integer extraMinutes,BigDecimal weightKg','Publish':'String title,String description,BigDecimal goodsReferencePrice,String pickupRegion,String dropoffRegion,BigDecimal weightKg,Integer quantity,String serviceMode,BigDecimal maxServiceFee,LocalDateTime latestAt','CreateRequest':'String demandId,String pickupRegion,String dropoffRegion,BigDecimal weightKg,Integer quantity,String serviceMode,BigDecimal maxServiceFee,LocalDateTime latestAt','CreateOffer':'String tripId,BigDecimal extraKm,Integer extraMinutes','StopInput':'int sequence,String regionCode,String placeLabel,LocalDateTime eta'}
    definition=lookup.get(typename)
   if 'List<' in params and '@RequestBody' in params: raw=True
   if definition:
    for field in re.split(r',\s*(?![^<]*>)', definition):
     field=field.strip(); fm=re.search(r'([\w.<>?]+)\s+(\w+)\s*$',field)
     if fm:
      typ,key=fm.groups(); body.append({'name':key,'type':'boolean' if typ=='boolean' else 'number' if typ in ('int','Integer','BigDecimal','Long','long') else 'array' if typ.startswith('List<') else 'datetime' if typ=='LocalDateTime' else 'text','javaType':typ})
  query=[]
  for m in re.finditer(r'@RequestParam(?:\([^)]*\))?\s+\w+\s+(\w+)',params): query.append(m.group(1))
  slug=full.replace('/','_').replace('{','').replace('}','').strip('_')
  name=(('提交顺路报价' if http=='PostMapping' else '查询顺路报价') if full=='/api/route-share/requests/{demandId}/offers' else endpoint_labels.get(full)) or labels.get(path.strip('/')) or labels.get(func) or path.strip('/').replace('_',' ').replace('/',' · ')
  isadmin=('/admin' in full or func in ('issueInvoice','reviewViolation','enterprise','grantPoints','localeUpsert','localeToggle','localeRemove','adjust','dissolve','commissions','resolve','review'))
  risk=('admin_' in full or any(s in full for s in ('accept','award','cancel','refund','change_password','rebind','grant_points','dissolve','adjust_balance','confirm_','finish_assist','issue_meetup','consume_invite')))
  operations.append(dict(id=slug,group=category_overrides.get(fp.stem,'其他'),name=name,method=http.upper().replace('MAPPING',''),path=full,controller=fp.name,handler=func,pathParams=pathvars,queryParams=query,bodyFields=body,rawBody=raw,access='public' if full.startswith('/api/public') else 'protected',role='admin' if isadmin else 'user',dangerous=risk,returns=ret.strip()))
operations.sort(key=lambda x:(x['group'],x['path']))
(target/'packages/sdk/src/registry.ts').write_text('// GENERATED FROM JAVA CONTROLLERS. Do not hand edit.\nimport type { ApiOperation } from "./types";\nexport const operations: ApiOperation[] = '+json.dumps(operations,ensure_ascii=False,indent=2)+';\n')
with (target/'docs/API_COVERAGE.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.writer(f);w.writerow(['HTTP','Path','Name','Group','Controller','Handler','Access','Role','Params','Response','Frontend'])
 for o in operations:w.writerow([o['method'],o['path'],o['name'],o['group'],o['controller'],o['handler'],o['access'],o['role'],','.join(x['name'] for x in o['bodyFields']),o['returns'],'PC/API工作台 + 后台/API工作台 + 移动/API工具台'])
print('routes:',len(operations),'groups:',len(set(x['group'] for x in operations)))
