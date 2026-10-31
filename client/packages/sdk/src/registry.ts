// GENERATED FROM JAVA CONTROLLERS. Do not hand edit.
import type { ApiOperation } from "./types";
export const operations: ApiOperation[] = [
  {
    "id": "api_rpc_admin_grant_points",
    "group": "业务运营",
    "name": "管理端发放积分",
    "method": "POST",
    "path": "/rpc/admin_grant_points",
    "controller": "Batch10RpcController.java",
    "handler": "grantPoints",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "userId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "points",
        "type": "number",
        "javaType": "Integer"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_invite_tier_distribution",
    "group": "业务运营",
    "name": "邀请等级分布统计",
    "method": "POST",
    "path": "/rpc/admin_invite_tier_distribution",
    "controller": "Batch10RpcController.java",
    "handler": "tierDistribution",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "List<Map<String,Object>>"
  },
  {
    "id": "api_rpc_admin_issue_invoice",
    "group": "业务运营",
    "name": "审核并开具发票",
    "method": "POST",
    "path": "/rpc/admin_issue_invoice",
    "controller": "Batch10RpcController.java",
    "handler": "issueInvoice",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "invoiceId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "approve",
        "type": "boolean",
        "javaType": "boolean"
      },
      {
        "name": "invoiceNo",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "fileUrl",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "reason",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_list_violations",
    "group": "业务运营",
    "name": "查询违规记录",
    "method": "POST",
    "path": "/rpc/admin_list_violations",
    "controller": "Batch10RpcController.java",
    "handler": "violations",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "limit",
        "type": "number",
        "javaType": "Integer"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "List<Map<String,Object>>"
  },
  {
    "id": "api_rpc_admin_locale_remove_rpc",
    "group": "业务运营",
    "name": "删除语言地区配置",
    "method": "POST",
    "path": "/rpc/admin_locale_remove_rpc",
    "controller": "Batch10RpcController.java",
    "handler": "localeRemove",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_locale_set_active_rpc",
    "group": "业务运营",
    "name": "启用或停用语言",
    "method": "POST",
    "path": "/rpc/admin_locale_set_active_rpc",
    "controller": "Batch10RpcController.java",
    "handler": "localeToggle",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "active",
        "type": "boolean",
        "javaType": "boolean"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_locale_upsert_rpc",
    "group": "业务运营",
    "name": "新增或更新语言配置",
    "method": "POST",
    "path": "/rpc/admin_locale_upsert_rpc",
    "controller": "Batch10RpcController.java",
    "handler": "localeUpsert",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "nativeName",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "labelZh",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "sort",
        "type": "number",
        "javaType": "Integer"
      },
      {
        "name": "fallback",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_review_enterprise",
    "group": "业务运营",
    "name": "审核企业资质",
    "method": "POST",
    "path": "/rpc/admin_review_enterprise",
    "controller": "Batch10RpcController.java",
    "handler": "enterprise",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "userId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "approve",
        "type": "boolean",
        "javaType": "boolean"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_review_violation",
    "group": "业务运营",
    "name": "处理违规记录",
    "method": "POST",
    "path": "/rpc/admin_review_violation",
    "controller": "Batch10RpcController.java",
    "handler": "reviewViolation",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "id",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_stats",
    "group": "业务运营",
    "name": "运营统计总览",
    "method": "POST",
    "path": "/rpc/admin_stats",
    "controller": "Batch10RpcController.java",
    "handler": "adminStats",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_confirm_meetup",
    "group": "业务运营",
    "name": "确认当面交接",
    "method": "POST",
    "path": "/rpc/confirm_meetup",
    "controller": "Batch10RpcController.java",
    "handler": "confirmMeetup",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "lat",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "lng",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_get_order_evidence",
    "group": "业务运营",
    "name": "查看订单证据",
    "method": "POST",
    "path": "/rpc/get_order_evidence",
    "controller": "Batch10RpcController.java",
    "handler": "orderEvidence",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_issue_meetup_code",
    "group": "业务运营",
    "name": "生成交接验证码",
    "method": "POST",
    "path": "/rpc/issue_meetup_code",
    "controller": "Batch10RpcController.java",
    "handler": "issueMeetup",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_log_privacy_call",
    "group": "业务运营",
    "name": "记录隐私通话",
    "method": "POST",
    "path": "/rpc/log_privacy_call",
    "controller": "Batch10RpcController.java",
    "handler": "callLog",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "result",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_propose_shipping_fee",
    "group": "业务运营",
    "name": "提出运费方案",
    "method": "POST",
    "path": "/rpc/propose_shipping_fee",
    "controller": "Batch10RpcController.java",
    "handler": "propose",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "fee",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_record_arrival_proof",
    "group": "业务运营",
    "name": "记录到达凭证",
    "method": "POST",
    "path": "/rpc/record_arrival_proof",
    "controller": "Batch10RpcController.java",
    "handler": "arrival",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "tripId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "snapshotUrl",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "lat",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "lng",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_request_invoice",
    "group": "业务运营",
    "name": "申请开具发票",
    "method": "POST",
    "path": "/rpc/request_invoice",
    "controller": "Batch10RpcController.java",
    "handler": "requestInvoice",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "titleType",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "titleName",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "taxNo",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "kind",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "email",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "remark",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_respond_shipping_fee",
    "group": "业务运营",
    "name": "响应运费协商",
    "method": "POST",
    "path": "/rpc/respond_shipping_fee",
    "controller": "Batch10RpcController.java",
    "handler": "respond",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "accept",
        "type": "boolean",
        "javaType": "boolean"
      },
      {
        "name": "counter",
        "type": "number",
        "javaType": "BigDecimal"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_accept_order_atomic",
    "group": "交易与PK",
    "name": "原子接单",
    "method": "POST",
    "path": "/rpc/accept_order_atomic",
    "controller": "TradeRpcController.java",
    "handler": "acceptOrder",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "deposit",
        "type": "number",
        "javaType": "BigDecimal"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_award_pk",
    "group": "交易与PK",
    "name": "选定中标报价",
    "method": "POST",
    "path": "/rpc/award_pk",
    "controller": "TradeRpcController.java",
    "handler": "awardPk",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "demandId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "quoteId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_close_pk",
    "group": "交易与PK",
    "name": "结束报价 PK",
    "method": "POST",
    "path": "/rpc/close_pk",
    "controller": "TradeRpcController.java",
    "handler": "closePk",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "demandId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_create_publish_deposit",
    "group": "交易与PK",
    "name": "创建发布保证金",
    "method": "POST",
    "path": "/rpc/create_publish_deposit",
    "controller": "TradeRpcController.java",
    "handler": "createPublish",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "amount",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "source",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_refund_publish_deposit",
    "group": "交易与PK",
    "name": "申请退回发布保证金",
    "method": "POST",
    "path": "/rpc/refund_publish_deposit",
    "controller": "TradeRpcController.java",
    "handler": "refundPublish",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_submit_pk_quote",
    "group": "交易与PK",
    "name": "提交 PK 报价",
    "method": "POST",
    "path": "/rpc/submit_pk_quote",
    "controller": "TradeRpcController.java",
    "handler": "submitQuote",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "demandId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "price",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "promiseDays",
        "type": "number",
        "javaType": "Integer"
      },
      {
        "name": "purchasePlace",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_public_categories",
    "group": "公开目录",
    "name": "国家/商品分类查询",
    "method": "GET",
    "path": "/public/categories",
    "controller": "PublicCatalogController.java",
    "handler": "categories",
    "pathParams": [],
    "queryParams": [
      "limit",
      "countryCode"
    ],
    "bodyFields": [],
    "rawBody": false,
    "access": "public",
    "role": "user",
    "dangerous": false,
    "returns": "List<Map<String,Object>>"
  },
  {
    "id": "api_public_products",
    "group": "公开目录",
    "name": "公开商品目录",
    "method": "GET",
    "path": "/public/products",
    "controller": "PublicCatalogController.java",
    "handler": "products",
    "pathParams": [],
    "queryParams": [
      "limit"
    ],
    "bodyFields": [],
    "rawBody": false,
    "access": "public",
    "role": "user",
    "dangerous": false,
    "returns": "List<Map<String,Object>>"
  },
  {
    "id": "api_public_profiles_masked",
    "group": "公开目录",
    "name": "查询脱敏用户资料",
    "method": "GET",
    "path": "/public/profiles/masked",
    "controller": "PublicCatalogController.java",
    "handler": "maskedProfiles",
    "pathParams": [],
    "queryParams": [
      "ids"
    ],
    "bodyFields": [],
    "rawBody": false,
    "access": "public",
    "role": "user",
    "dangerous": false,
    "returns": "List<Map<String,Object>>"
  },
  {
    "id": "api_public_trips",
    "group": "公开目录",
    "name": "公开行程目录",
    "method": "GET",
    "path": "/public/trips",
    "controller": "PublicCatalogController.java",
    "handler": "trips",
    "pathParams": [],
    "queryParams": [
      "limit",
      "sort"
    ],
    "bodyFields": [],
    "rawBody": false,
    "access": "public",
    "role": "user",
    "dangerous": false,
    "returns": "List<Map<String,Object>>"
  },
  {
    "id": "api_rpc_batch12_accept_assist",
    "group": "协助与互动",
    "name": "接受协助请求",
    "method": "POST",
    "path": "/rpc/batch12/accept_assist",
    "controller": "Batch12RpcController.java",
    "handler": "accept",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "id",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_accept_order_call",
    "group": "协助与互动",
    "name": "接受订单语音通话",
    "method": "POST",
    "path": "/rpc/batch12/accept_order_call",
    "controller": "Batch12RpcController.java",
    "handler": "acceptCall",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "callId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_batch12_add_post_comment",
    "group": "协助与互动",
    "name": "发布动态评论",
    "method": "POST",
    "path": "/rpc/batch12/add_post_comment",
    "controller": "Batch12RpcController.java",
    "handler": "comment",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "postId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "content",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_cancel_assist",
    "group": "协助与互动",
    "name": "取消协助请求",
    "method": "POST",
    "path": "/rpc/batch12/cancel_assist",
    "controller": "Batch12RpcController.java",
    "handler": "cancel",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "id",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_create_assist_request",
    "group": "协助与互动",
    "name": "创建协助请求",
    "method": "POST",
    "path": "/rpc/batch12/create_assist_request",
    "controller": "Batch12RpcController.java",
    "handler": "create",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "rate",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_finish_assist",
    "group": "协助与互动",
    "name": "确认协助完成",
    "method": "POST",
    "path": "/rpc/batch12/finish_assist",
    "controller": "Batch12RpcController.java",
    "handler": "finish",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "id",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "urlsJson",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_get_or_bind_privacy_number",
    "group": "协助与互动",
    "name": "获取或绑定隐私号码",
    "method": "POST",
    "path": "/rpc/batch12/get_or_bind_privacy_number",
    "controller": "Batch12RpcController.java",
    "handler": "privacy",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_batch12_hangup_order_call",
    "group": "协助与互动",
    "name": "挂断订单通话",
    "method": "POST",
    "path": "/rpc/batch12/hangup_order_call",
    "controller": "Batch12RpcController.java",
    "handler": "hangup",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "callId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_batch12_match_join_application",
    "group": "协助与互动",
    "name": "匹配同行申请",
    "method": "POST",
    "path": "/rpc/batch12/match_join_application",
    "controller": "Batch12RpcController.java",
    "handler": "match",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "applicationId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "boolean"
  },
  {
    "id": "api_rpc_batch12_reject_order_call",
    "group": "协助与互动",
    "name": "拒绝订单通话",
    "method": "POST",
    "path": "/rpc/batch12/reject_order_call",
    "controller": "Batch12RpcController.java",
    "handler": "rejectCall",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "callId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "asCallee",
        "type": "boolean",
        "javaType": "boolean"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_request_verification_code",
    "group": "协助与互动",
    "name": "申请身份验证码",
    "method": "POST",
    "path": "/rpc/batch12/request_verification_code",
    "controller": "Batch12RpcController.java",
    "handler": "code",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "target",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "channel",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_screen_chat_content",
    "group": "协助与互动",
    "name": "筛查聊天内容",
    "method": "POST",
    "path": "/rpc/batch12/screen_chat_content",
    "controller": "Batch12RpcController.java",
    "handler": "screen",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "postId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "content",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Batch12ContentModerationService.Screening"
  },
  {
    "id": "api_rpc_batch12_start_order_call",
    "group": "协助与互动",
    "name": "发起订单通话",
    "method": "POST",
    "path": "/rpc/batch12/start_order_call",
    "controller": "Batch12RpcController.java",
    "handler": "startCall",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_batch12_submit_assist_handover_a",
    "group": "协助与互动",
    "name": "提交协助交接凭证",
    "method": "POST",
    "path": "/rpc/batch12/submit_assist_handover_a",
    "controller": "Batch12RpcController.java",
    "handler": "handover",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "id",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "urlsJson",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_batch12_submit_carrier_kyc",
    "group": "协助与互动",
    "name": "申请带货人认证",
    "method": "POST",
    "path": "/rpc/batch12/submit_carrier_kyc",
    "controller": "Batch12RpcController.java",
    "handler": "carrier",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "realName",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "idCardNo",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "passportNo",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "phoneCode",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "inviteCode",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "boolean"
  },
  {
    "id": "api_rpc_batch12_submit_enterprise_verify",
    "group": "协助与互动",
    "name": "申请企业认证",
    "method": "POST",
    "path": "/rpc/batch12/submit_enterprise_verify",
    "controller": "Batch12RpcController.java",
    "handler": "enterprise",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "enterpriseName",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "businessLicenseNo",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "proofUrls",
        "type": "array",
        "javaType": "List<String>"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_review_buyer_kyc",
    "group": "实名认证",
    "name": "审核买家实名认证",
    "method": "POST",
    "path": "/rpc/admin_review_buyer_kyc",
    "controller": "KycRpcController.java",
    "handler": "review",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "userId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "approve",
        "type": "boolean",
        "javaType": "boolean"
      },
      {
        "name": "reason",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_issue_face_challenge",
    "group": "实名认证",
    "name": "下发人脸核验挑战",
    "method": "POST",
    "path": "/rpc/issue_face_challenge",
    "controller": "KycRpcController.java",
    "handler": "challenge",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "scene",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "KycRpcMigrationService.FaceChallenge"
  },
  {
    "id": "api_rpc_record_face_verification",
    "group": "实名认证",
    "name": "记录人脸核验结果",
    "method": "POST",
    "path": "/rpc/record_face_verification",
    "controller": "KycRpcController.java",
    "handler": "record",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "scene",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "snapshotUrl",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "deviceInfo",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "challengeCode",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_submit_buyer_kyc",
    "group": "实名认证",
    "name": "提交买家实名认证",
    "method": "POST",
    "path": "/rpc/submit_buyer_kyc",
    "controller": "KycRpcController.java",
    "handler": "submit",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "realName",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "idCardNo",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_upsert_kyc_doc_with_challenge",
    "group": "实名认证",
    "name": "提交身份资料与挑战码",
    "method": "POST",
    "path": "/rpc/upsert_kyc_doc_with_challenge",
    "controller": "KycRpcController.java",
    "handler": "doc",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "docType",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "storageUrl",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "challengeCode",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_resolve_cancel",
    "group": "订单履约",
    "name": "管理员裁决取消申请",
    "method": "POST",
    "path": "/rpc/admin_resolve_cancel",
    "controller": "Batch11RpcController.java",
    "handler": "resolve",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "approve",
        "type": "boolean",
        "javaType": "boolean"
      },
      {
        "name": "reason",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_confirm_purchase",
    "group": "订单履约",
    "name": "确认采购完成",
    "method": "POST",
    "path": "/rpc/confirm_purchase",
    "controller": "Batch11RpcController.java",
    "handler": "purchase",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "evidence",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "lat",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "lng",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "address",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "meta",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_confirm_ship",
    "group": "订单履约",
    "name": "确认订单发货",
    "method": "POST",
    "path": "/rpc/confirm_ship",
    "controller": "Batch11RpcController.java",
    "handler": "ship",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "carrier",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "tracking",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "labelUrls",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "lat",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "lng",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "address",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_consume_invite_code",
    "group": "订单履约",
    "name": "兑换邀请码",
    "method": "POST",
    "path": "/rpc/consume_invite_code",
    "controller": "Batch11RpcController.java",
    "handler": "consumeInvite",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "boolean"
  },
  {
    "id": "api_rpc_get_peer_profile",
    "group": "订单履约",
    "name": "查看交易对方资料",
    "method": "POST",
    "path": "/rpc/get_peer_profile",
    "controller": "Batch11RpcController.java",
    "handler": "getPeer",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "target",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_open_peer_thread",
    "group": "订单履约",
    "name": "开启交易双方会话",
    "method": "POST",
    "path": "/rpc/open_peer_thread",
    "controller": "Batch11RpcController.java",
    "handler": "openThread",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "Map<String,Object>"
  },
  {
    "id": "api_rpc_record_boarding_proof",
    "group": "订单履约",
    "name": "提交登机凭证",
    "method": "POST",
    "path": "/rpc/record_boarding_proof",
    "controller": "Batch11RpcController.java",
    "handler": "boarding",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "tripId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "snapshotUrl",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "lat",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "lng",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_record_delivery_proof",
    "group": "订单履约",
    "name": "提交交付凭证",
    "method": "POST",
    "path": "/rpc/record_delivery_proof",
    "controller": "Batch11RpcController.java",
    "handler": "delivery",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "faceId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "lat",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "lng",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "city",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_request_cancel_v2",
    "group": "订单履约",
    "name": "申请取消订单",
    "method": "POST",
    "path": "/rpc/request_cancel_v2",
    "controller": "Batch11RpcController.java",
    "handler": "cancel",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "reasonType",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "forceMajeure",
        "type": "boolean",
        "javaType": "boolean"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "proofUrls",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_request_no_boarding_refund",
    "group": "订单履约",
    "name": "未登机退款申请",
    "method": "POST",
    "path": "/rpc/request_no_boarding_refund",
    "controller": "Batch11RpcController.java",
    "handler": "refund",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "orderId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_verify_contact_code",
    "group": "订单履约",
    "name": "验证联系渠道验证码",
    "method": "POST",
    "path": "/rpc/verify_contact_code",
    "controller": "Batch11RpcController.java",
    "handler": "verifyContact",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "target",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "channel",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "boolean"
  },
  {
    "id": "api_rpc_identity_change_password",
    "group": "账户安全",
    "name": "修改当前密码",
    "method": "POST",
    "path": "/rpc/identity/change_password",
    "controller": "IdentitySettingsController.java",
    "handler": "changePassword",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "oldPassword",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "newPassword",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_identity_rebind_contact",
    "group": "账户安全",
    "name": "换绑手机或邮箱",
    "method": "POST",
    "path": "/rpc/identity/rebind_contact",
    "controller": "IdentitySettingsController.java",
    "handler": "rebind",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "channel",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "newTarget",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_identity_reset_own_password_by_code",
    "group": "账户安全",
    "name": "通过验证码重置密码",
    "method": "POST",
    "path": "/rpc/identity/reset_own_password_by_code",
    "controller": "IdentitySettingsController.java",
    "handler": "resetPassword",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "channel",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "target",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "newPassword",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_adjust_balance",
    "group": "运营财务",
    "name": "管理员调整余额",
    "method": "POST",
    "path": "/rpc/admin_adjust_balance",
    "controller": "AdminFinanceRpcController.java",
    "handler": "adjust",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "userId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "delta",
        "type": "number",
        "javaType": "BigDecimal"
      },
      {
        "name": "note",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_admin_commission_rows",
    "group": "运营财务",
    "name": "查询平台佣金流水",
    "method": "POST",
    "path": "/rpc/admin_commission_rows",
    "controller": "AdminFinanceRpcController.java",
    "handler": "commissions",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "limit",
        "type": "number",
        "javaType": "Integer"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "List<Map<String,Object>>"
  },
  {
    "id": "api_rpc_admin_dissolve_pool",
    "group": "运营财务",
    "name": "管理员解散拼单池",
    "method": "POST",
    "path": "/rpc/admin_dissolve_pool",
    "controller": "AdminFinanceRpcController.java",
    "handler": "dissolve",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "tripId",
        "type": "text",
        "javaType": "String"
      },
      {
        "name": "reason",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "admin",
    "dangerous": true,
    "returns": "String"
  },
  {
    "id": "api_rpc_get_invite_code_owner",
    "group": "邀请与团队",
    "name": "查询邀请码归属",
    "method": "POST",
    "path": "/rpc/get_invite_code_owner",
    "controller": "InviteReadRpcController.java",
    "handler": "owner",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "code",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_get_operator_invite_code",
    "group": "邀请与团队",
    "name": "查询运营邀请码",
    "method": "POST",
    "path": "/rpc/get_operator_invite_code",
    "controller": "InviteReadRpcController.java",
    "handler": "operator",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "operatorId",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "String"
  },
  {
    "id": "api_rpc_list_team_members",
    "group": "邀请与团队",
    "name": "查看团队成员",
    "method": "POST",
    "path": "/rpc/list_team_members",
    "controller": "InviteReadRpcController.java",
    "handler": "members",
    "pathParams": [],
    "queryParams": [],
    "bodyFields": [
      {
        "name": "root",
        "type": "text",
        "javaType": "String"
      }
    ],
    "rawBody": false,
    "access": "protected",
    "role": "user",
    "dangerous": false,
    "returns": "List<Map<String,Object>>"
  }
];
