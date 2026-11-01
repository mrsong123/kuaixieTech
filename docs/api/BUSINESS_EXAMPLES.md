# 常用业务接口联调示例

依据2026-11-01当前 Spring Controller、DTO、Service 和 SQL 核对。下列邮箱、密码、UUID、地址和金额均为合成示例；UUID必须替换为对应环境实际返回的记录。示例响应用于说明代码契约，不能当作真实供应商、业务库或全部端设备的验收回执。

本文统一使用服务端规范路径 `/api/...`。需登录的请求增加 `Authorization: Bearer <登录返回的 accessToken>`，JSON请求增加 `Content-Type: application/json`。没有统一的 `{code,data,message}` 外层包装；下文对象/数组为响应体本身。

## 1. 图形验证码、联系方式验证码、注册、登录

### 图形验证码

`GET /api/auth/captcha`，匿名。响应字段为 `id` 和 PNG data URL `image`：

```json
{
  "id": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1",
  "image": "data:image/png;base64,<PNG内容>"
}
```

前端展示图片，由用户输入4位图形码；答案不由接口明文返回。图形码在当前进程内保存、有效期120秒，消费一次即删除，错误也消耗该挑战；失败后重新申请。

### 联系方式验证码

`POST /api/auth/send-code`，匿名：

```json
{
  "channel": "email",
  "contact": "buyer@example.invalid",
  "purpose": "signup",
  "captchaId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1",
  "captchaCode": "ABCD"
}
```

成功示例：

```json
{"status":"OK","retryAfter":60}
```

`channel` 为 `phone|email`，`purpose` 为 `signin|signup|reset`。电话使用国家区号 `countryCode`（如 `+86`）和不含区号的号码 `contact`；缺省国家区号为 `+86`。邮箱会去首尾空格并转小写。

该接口先执行外呼 LIVE 模式门禁，再校验/消费图形码、保存验证码并写投递队列。**OK表示请求受理/入队，不表示短信或邮件已送达**。重复请求可能返回429；不存在账号的登录/重置申请亦可能返回相同OK，不能用返回值判断账号是否存在。需要已配置且实际可用的短信/邮件投递通道，不能在SANDBOX模式依赖此接口完成真实送达。

### 注册

`POST /api/auth/register`，匿名：

```json
{
  "channel": "email",
  "contact": "buyer@example.invalid",
  "password": "DemoPassw0rd!17",
  "code": "123456",
  "role": "buyer"
}
```

`code` 必须是该联系方式实际收到、用途为 `signup` 且尚未使用的验证码，示例数字不能直接使用。`role` 为 `buyer|carrier|enterprise`；企业角色还需 `enterpriseName`（2～80字符）及 `businessLicenseNo`（18位格式）。`inviteCode` 为可选字段。密码当前规则为去首尾空格后8～20字符。

成功响应字段与登录一致。企业注册资料通过格式校验不等于已完成供应商实名或店铺审核；带货人注册不等于已通过带货人认证。

### 密码或验证码登录

`POST /api/auth/login`，匿名：

```json
{
  "channel": "email",
  "contact": "buyer@example.invalid",
  "password": "DemoPassw0rd!17"
}
```

验证码登录可不传 `password`，改传 `code`（实际收到的 `signin` 验证码）。当 `password` 非空时优先采用 `password`；最终选定的口令若为6位数字，按验证码处理，不按密码处理。

邮件登录成功的完整字段示例：

```json
{
  "accessToken": "<JWT>",
  "token": "<同一个JWT，兼容旧客户端>",
  "userId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2",
  "isAdmin": false,
  "expiresIn": 28800
}
```

电话登录还返回 `phoneCountryCode` 和 `locale`（`+86`为`zh-CN`，其他区号为`en-US`）。`isAdmin`由服务端资料决定，不接受客户端声明。`LOCAL_JWT_SECRET_MISSING` 时不能签发会话；本地 bootstrap 管理员仅限明确配置的dev/local环境，不是生产注册途径。

`send-code/register/reset-password/recovery-help` 的部分业务拒绝由该 Controller 返回对应HTTP状态及 `{"message":"业务错误码"}`；登录异常并非同一包装，客户端还要处理HTTP状态。依据：[AuthLoginController](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java)、[AuthLoginService](../../server/src/main/java/com/kuaixietech/service/account/impl/AuthLoginService.java)、[AuthRecoveryService](../../server/src/main/java/com/kuaixietech/service/account/impl/AuthRecoveryService.java)、[AuthCaptchaService](../../server/src/main/java/com/kuaixietech/service/account/impl/AuthCaptchaService.java)。

## 2. 本人订单分页和详情

`GET /api/me/orders?role=buyer&limit=20&offset=0`，需有效登录。`role=all|buyer|carrier`，缺省 `all`；`limit=1..100`，缺省20；`offset=0..1000000`。返回裸数组，空页是 `[]`，没有 `total`、`page` 或 `records` 包装。

单条示意（空值列可能返回null，也可能被当前序列化配置省略）：

```json
[
  {
    "id": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3",
    "order_no": "DDexample",
    "item_name": "演示商品",
    "item_spec": "标准规格",
    "quantity": 1,
    "unit": "piece",
    "goods_amount": 100.00,
    "service_fee": 5.00,
    "shipping_fee": null,
    "status": "requesting",
    "insurance_quote_id": null,
    "insurance_choice": "waived",
    "pricing_version": "escrow_v2_external_insurance",
    "shipping_fee_status": "none",
    "tracking_no": null,
    "insured": "waived",
    "insurance_premium": 0.00,
    "buyer_deposit": 105.00,
    "vendor_deposit": 20.00,
    "buyer_deposit_status": "frozen",
    "vendor_deposit_status": "unpaid",
    "expect_arrive_date": null,
    "promised_arrive_date": null,
    "created_at": "2026-11-01T15:38:00",
    "updated_at": "2026-11-01T15:38:00",
    "my_role": "buyer"
  }
]
```

`GET /api/me/orders/{id}`返回同一投影的对象，并增加 `ref_image_urls`；字段可能为JSON字符串，前端须按实际序列化处理。接口身份来自令牌，不接受 `userId` 切换本人。无关账户即使是管理员，走此本人详情接口也不能看到他人订单，应返回404；后台业务使用对应后台接口。

SQL投影字段是 `snake_case`，不要直接按发布/下单DTO的 `camelCase` 读回。分页详情来自 [AccountCentreController](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java)、[AccountCentreService](../../server/src/main/java/com/kuaixietech/service/account/impl/AccountCentreService.java)、[AccountCentreMapper.xml](../../server/src/main/resources/mapper/account/AccountCentreMapper.xml)。

## 3. 发布需求、行程和商品

### 需求发布与审核

`POST /api/portal/demands`，需有效活跃账户：

```json
{
  "title": "采购演示商品",
  "description": "合成联调需求说明",
  "city": "杭州",
  "expectPrice": "100.00",
  "tripType": "domestic"
}
```

成功响应：

```json
{"id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa4","status":"pending_review"}
```

`title`必填且不超过200字符，`description`不超过4000字符，`city`不超过64字符，`expectPrice>=0`，`tripType=any|domestic|international`，缺省any。响应 `pending_review` 来自审核流程；底层需求为 `status=open`、`visibility=hidden`，并有待审记录，不等于已经公开。

管理员 `POST /api/portal/admin/demands/review`：

```json
{"demandId":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa4","approved":true}
```

成功时响应体为空；审批通过后需求才公开。重复审核不在待审状态会冲突，客户端刷新状态，不应自行强制覆盖。

### 带货行程

`POST /api/portal/trips`，需已通过带货人认证：

```json
{
  "originCity": "杭州",
  "destination": "上海",
  "departDate": "2026-12-10",
  "returnDate": "2026-12-12",
  "tripType": "domestic",
  "transportMode": "road",
  "capacityQty": 3
}
```

日期需替换为不早于服务器当前日期的 `yyyy-MM-dd`；返程不能早于出发。`tripType=domestic|international`，缺省domestic；`transportMode=road|rail|air|other`，缺省other；容量1～100，缺省1。

成功返回 `{"id":"<行程UUID>","status":"pending_review"}`。管理员 `POST /api/portal/admin/trips/review`，请求 `{"tripId":"<行程UUID>","approved":true}`，成功体为空；审核通过前不作为已公开行程。

依据：[PortalController](../../server/src/main/java/com/kuaixietech/controller/PortalController.java)、[PortalDtos](../../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java)、[PortalServiceImpl](../../server/src/main/java/com/kuaixietech/service/portal/impl/PortalServiceImpl.java)。

### 商品发布和上架

`POST /api/me/products`，需已通过带货人认证：

```json
{
  "name": "演示商品",
  "spec": "标准规格",
  "region": "杭州",
  "price": "100.00",
  "stock": 3,
  "imageUrls": [],
  "note": "合成联调商品"
}
```

成功返回 `{"id":"<商品UUID>","status":"off"}`。`categoryId` 可选，提供时必须为可用分类；最多9张图。图片需为本系统上传返回的 `/public/media/{uuid}`，或后台允许域名的HTTPS地址。空数组允许，不代表文件上传已验证。价格0.01～9999999999.99，最多2位小数；库存0～1000000。

`PUT /api/me/products/{id}/status` 请求：

```json
{"status":"on"}
```

返回 `{"id":"<商品UUID>","status":"on"}`，`status=on|off`。上架时需正库存及已通过的带货人；活跃拥有者仍能下架。更新商品 `PUT /api/me/products/{id}` 使用发布同一DTO，成功后重新变为off；存在库存占用/既有预订时，库存变更可能需 `expectedStock`，商品身份变更可能返回409，需重新读取。

依据：[ProductPublishingController](../../server/src/main/java/com/kuaixietech/controller/ProductPublishingController.java)、[ProductPublishingService](../../server/src/main/java/com/kuaixietech/service/catalog/support/ProductPublishingService.java)。

## 4. 商品报价、确认下单和幂等

先匿名 `GET /api/public/products/{id}/trips?limit=100&offset=0` 选择该商品卖家的有效、公开且审核通过的行程。买家需已通过买家认证，不能购买自己的商品，库存、店铺、卖家及行程必须满足后端即时条件。

`POST /api/me/product-orders/quote`：

```json
{
  "productId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa5",
  "tripId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa6",
  "quantity": 1,
  "contactName": "演示买家",
  "contactPhone": "13800009999",
  "address": "合成联调地址",
  "insuranceChoice": "waived"
}
```

完整响应结构示例：

```json
{
  "productId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa5",
  "tripId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa6",
  "storeId": null,
  "itemName": "演示商品",
  "itemSpec": "标准规格",
  "unitPrice": "100.00",
  "quantity": 1,
  "goodsAmount": "100.00",
  "serviceFee": "5.00",
  "insurancePremium": "0.00",
  "buyerEscrow": "105.00",
  "vendorDeposit": "20.00",
  "currency": "CNY",
  "pricingVersion": "escrow_v2_external_insurance",
  "intentHours": 24,
  "productFingerprint": "1111111111111111111111111111111111111111111111111111111111111111"
}
```

`productFingerprint` 为商品当前快照的64位十六进制哈希，上面仅占位；必须原样使用实际quote返回值。`intentHours`为后端配置值，24只是缺省。当前新商品链按后端规则托管货值+5%服务费，带货人保证金为货值20%。**保险保费由保险机构独立定价和收费；本新链 `insurancePremium=0.00`，不会按旧估算比例托管保费。** 选 `insuranceChoice=yes` 时还需可用且匹配该主体/金额/商品的实际 `insuranceQuoteId`，当前无已连接适配器时不能假装投保成功。

用户核对报价后，`POST /api/me/product-orders`：

```json
{
  "productId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa5",
  "tripId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa6",
  "quantity": 1,
  "contactName": "演示买家",
  "contactPhone": "13800009999",
  "address": "合成联调地址",
  "insuranceChoice": "waived",
  "requestId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa7",
  "expectedUnitPrice": "100.00",
  "expectedBuyerEscrow": "105.00",
  "expectedProductFingerprint": "1111111111111111111111111111111111111111111111111111111111111111"
}
```

响应：

```json
{"id":"<订单UUID>","status":"requesting"}
```

`requestId`为客户端为该次用户确认生成且持久到请求结束的UUID；超时重试保留同一UUID和同一完整内容。相同用户同键同内容返回原订单，不再扣款/占库存；同键改内容返回 `PRODUCT_ORDER_REQUEST_REUSED`。报价变更/商品快照改变返回409，如 `PRICE_CHANGED_RECONFIRM_QUOTE`、`PRODUCT_CHANGED_REQUOTE`，应重新报价并让用户确认后使用新UUID。缺确认字段返回400；余额不足返回409。网络异常不能直接假定失败或发起新交易。

创建事务会扣买家余额并记录托管、占用商品库存；该响应只表示 `requesting`，不是已履约或已结算。隔离环境的真实33项烟测覆盖上述放弃保险的单品下单及同内容重试，未覆盖所有并发、退款和履约分支。

依据：[ProductCheckoutController](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutController.java)、[ProductCheckoutModels](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutModels.java)、[ProductCheckoutService](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutService.java)、[IntentPricing](../../server/src/main/java/com/kuaixietech/service/trading/support/IntentPricing.java)。

## 5. 地址默认切换

`POST /api/me/addresses`，需活跃登录账户：

```json
{
  "contactName": "演示买家",
  "contactPhone": "13800009999",
  "province": "浙江省",
  "city": "杭州市",
  "district": "演示区",
  "detail": "合成联调地址A",
  "isDefault": true
}
```

响应 `{"id":"<地址UUID>"}`。再创建另一个 `isDefault=true` 的地址B，后端会把该用户旧默认置false；`GET /api/me/addresses?limit=20&offset=0`返回裸数组：

```json
[
  {"id":"<B的UUID>","contact_name":"演示买家","contact_phone":"13800009999","province":"浙江省","city":"杭州市","district":"演示区","detail":"合成联调地址B","is_default":true,"created_at":"<时间>","updated_at":"<时间>"},
  {"id":"<A的UUID>","contact_name":"演示买家","contact_phone":"13800009999","province":"浙江省","city":"杭州市","district":"演示区","detail":"合成联调地址A","is_default":false,"created_at":"<时间>","updated_at":"<时间>"}
]
```

`is_default`在不同驱动序列化下可为布尔或0/1。更新使用 `PUT /api/me/addresses/{id}` 同一DTO；删除本人地址使用 `DELETE /api/me/addresses/{id}`，成功体为空。列表/修改/删除均限本人，无 `userId` 参数。默认切换依赖完整结构及012地址触发器修复；旧原始触发器可能触发MySQL1442。

依据：[SavedCommerceController](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java)、[SavedCommerceService](../../server/src/main/java/com/kuaixietech/service/account/impl/SavedCommerceService.java)。

## 6. 店铺申请与审核

先上传营业执照：`POST /api/me/uploads?purpose=store_license`，`multipart/form-data`，文件字段名 `file`。需真实已配置私有对象存储与LIVE门禁；不能用任意文件URL代替。上传响应：

```json
{"id":"<上传UUID>","purpose":"store_license","contentType":"application/pdf","byteSize":12345,"sha256":"<64位哈希>"}
```

上传可接受的实际文件类型由服务端字节检测决定；`application/pdf`为示例，不是声明任意MIME均可用。上传后 `POST /api/me/shop/applications`：

```json
{
  "businessName": "演示企业",
  "creditCode": "<合法18位统一社会信用代码>",
  "representative": "演示法人",
  "licenseUploadId": "<上一步实际返回的上传UUID>",
  "channel": "web",
  "requestId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa8"
}
```

响应 `{"id":"<店铺申请UUID>","status":"pending"}`。`creditCode`需通过18位字符和校验位检查；上传需属于本人、purpose正确、有内容哈希和正字节数、尚未使用；可选 `invitationCode` 必须对应有效其他用户。重复同键同内容可读回原申请，换内容冲突。已有店铺或待审申请时不能重复申请。

管理员 `GET /api/portal/admin/shops/applications?limit=20&offset=0` 读队列，再 `POST /api/portal/admin/shops/applications/{id}/review`：

```json
{"approve":true,"reason":"已核对提交的经营资料"}
```

响应 `{"id":"<店铺申请UUID>","status":"approved"}`，后台建立active店铺。审核为平台审核动作，不等于外部工商/人脸/保险机构API已完成。`GET /api/me/shop`返回 `{"store":对象或null,"applications":[],"products":[]}`。

依据：[UploadController](../../server/src/main/java/com/kuaixietech/storage/UploadController.java)、[PrivateUploads](../../server/src/main/java/com/kuaixietech/storage/PrivateUploads.java)、[ShopController](../../server/src/main/java/com/kuaixietech/shops/ShopController.java)、[ShopService](../../server/src/main/java/com/kuaixietech/shops/ShopService.java)。

## 7. 交易纠纷和售后状态

`POST /api/me/disputes`，需订单买家本人；订单需为 `purchased|shipped|delivered|completed`，有不同于买家的卖家，且无未结纠纷。刚下单的 `requesting` 订单不能直接用于该示例。

```json
{
  "orderId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3",
  "type": "damaged",
  "description": "合成说明：收到商品有破损",
  "expectResolution": "refund_part",
  "refundAmount": "20.00",
  "evidenceUrls": [],
  "requestId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa9"
}
```

成功响应：

```json
{"id":"<纠纷UUID>","stage":"negotiating"}
```

`type=quality|wrong_item|missing|damaged`；`expectResolution=refund_part|refund_full|resend|compensate`。创建证据最多4条，条目需为已配置媒体域名的HTTPS URL；空数组当前允许。私有上传文档的 `/me/uploads/{id}/content` 不能直接假定为可用 `evidenceUrls` 值。退款金额及可选性按处理方案和订单货值再次校验。

`GET /api/me/disputes/{id}`返回纠纷详情，包括 `id,order_id,initiator_id,respondent_id,type,expect_resolution,description,evidence_urls,respondent_evidence_urls,respondent_note,stage,round,verdict,refund_amount,deposit_owner,verdict_reason,reopened_used,review_appeal,review_result,negotiation_deadline,resolved_at,created_at,item_name,order_no,goods_amount,events,myRole`；非管理员不返回 `reviewed_by`。有关联售后时增加 `refund_status,after_sales_status`；有钱包退款凭证时增加 `refund_method,escrowRefund`。

补充材料 `POST /api/me/disputes/{id}/respond`：

```json
{"note":"合成补充说明","evidenceUrls":[],"expectedRound":1,"requestId":"bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1"}
```

申请平台介入 `POST /api/me/disputes/{id}/escalate`：

```json
{"reason":"双方未达成一致","expectedRound":2,"requestId":"bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb2"}
```

返回 `{"id":"<纠纷UUID>","stage":"platform_review"}`。上述轮次假设依次完成创建、补充材料和升级；每次动作需先读取最新 `round` 再传 `expectedRound`，不能照抄示例轮次。`requestId`必须UUID，用于同动作内容重试；复用不同内容会冲突。

管理员在 `platform_review|appeal` 阶段调用 `POST /api/portal/admin/disputes/{id}/decision`：

```json
{"outcome":"refund_part","refundAmount":"20.00","reason":"记录平台处理决定","depositOwner":"return_original","expectedRound":3,"requestId":"bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb3"}
```

响应 `{"id":"<纠纷UUID>","stage":"awaiting_refund"}`。`outcome=reject|refund_part|refund_full|resend|compensate`；`depositOwner=return_original|buyer|vendor|none`。**裁决记录不等于钱已退回**：退款需另走实际资金执行/凭证链路，前端读取 `refund_status`，不能把 `awaiting_refund` 展示为退款成功。补发/结案/一次申诉还有各自阶段限制。

依据：[TradeCaseController](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java)、[CaseModels](../../server/src/main/java/com/kuaixietech/cases/CaseModels.java)、[TradeCaseService](../../server/src/main/java/com/kuaixietech/cases/TradeCaseService.java)。
