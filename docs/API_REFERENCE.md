# 项目 API 接口文档

更新日期：2026-11-01。本文面向 PC/Web、小程序、Android、iOS 和后台管理联调，以当前 Spring Controller、DTO、实际 Service 和 SDK 为准。所有示例账号、UUID、地址、金额和令牌均为合成说明；实际调用替换为对应环境返回值。

全量声明为 **34个 Controller、232条 HTTP 路由**，其中 **84条显式 RPC**；关联 **129个 Java record/enum 字段结构**。路由存在、配置保存、SDK能构造、本地验证通过和真实业务验收是不同状态。当前33项隔离运行回执覆盖登录、权限、地址、发布审核及商品报价托管子链路，没有验证真实支付、保险、短信或全部端设备。

## 文档与全量索引

| 内容 | 文件 |
| --- | --- |
| 全部方法、规范路径、网关路径、参数位置/默认值、响应类型、源码 | [HTTP_INDEX.md](api/HTTP_INDEX.md)、[JSON](api/HTTP_INDEX.json)、[CSV](api/HTTP_INDEX.csv) |
| 请求/响应 DTO 每个字段、Java类型、声明约束 | [JAVA_TYPES.md](api/JAVA_TYPES.md)、[JSON](api/JAVA_TYPES.json) |
| SDK调用方响应字段原文 | [SDK_TYPES.md](api/SDK_TYPES.md)、[JSON](api/SDK_TYPES.json) |
| 84条真实RPC及controller→service定位、失败与未接线边界 | [RPC_INDEX.md](api/RPC_INDEX.md)、[JSON](api/RPC_INDEX.json)、[CSV](api/RPC_INDEX.csv) |
| 认证、错误、分页和RPC详细补充 | [AUTH_RPC_NOTES.md](api/AUTH_RPC_NOTES.md) |
| 常用业务示例分册 | [BUSINESS_EXAMPLES.md](api/BUSINESS_EXAMPLES.md) |
| 支付、保险、配置与供应商边界分册 | [FINANCE_INTEGRATIONS.md](api/FINANCE_INTEGRATIONS.md) |

HTTP索引覆盖全部声明，不只SDK工作台registry中的约71项。本文生成器得到146条SDK调用/条件分支记录，对应144个不同的方法和路径，均找到后端声明；旧路由审计的131项仅统计去重后的纯字面量调用。统计口径不同，并不表示新增了15个API。`'/rpc/'+name` 单列为动态调用，实际允许键以84条显式RPC为准，没有通用任意函数执行或SDK透传接口。

泛型 `Map/Object` 没有被虚构为统一固定字段：常用明确响应列在本文，完整附录同时给出Java声明、已存在的SDK消费类型和源码。SDK类型属于调用方约定，不能替代服务端当前返回、权限和业务状态验证。

## 地址、认证、响应与分页

后端默认根地址为 `http://localhost:8080`，部署时替换为实际HTTPS服务域名。本文全部示例使用规范 `/api/...` 路径，例如 `GET /api/me/profile`。SDK的 `VITE_API_BASE_URL` 填服务根地址，不附 `/api`；SDK使用 `/me/profile` 等业务路径。

服务端网关仅为 `/public`、`/rpc`、`/portal`、`/support`、`/me`、`/auth` 六类路径补 `/api`。**供应商支付回调使用 `/api/payment-callback/{merchant}/{id}`，服务端不提供去掉 `/api` 的回调别名。** 开发代理的额外改写不能当作生产服务端能力。

```http
GET /api/me/profile HTTP/1.1
Authorization: Bearer <登录返回的accessToken>
Accept: application/json
Accept-Language: zh-CN
```

JSON写入增加 `Content-Type: application/json`。上传为带boundary的multipart，字段名 `file`，不要手填缺boundary的Content-Type。私有下载成功为二进制。跨域通过部署配置 `CORS_ORIGINS`，当前不用Cookie会话；微信与原生端还需自己的合法域名/网络与原生SDK配置。

认证为无状态Bearer JWT。默认本地模式需要 `APP_JWT_SECRET` 至少32字节，成功会话有效期28800秒。外部资源服务器模式 `AUTH_JWT_ENABLED=true` 另需HTTPS issuer/JWK与audience；当前 `/auth/login` 仍签本地HS256，不能假定其令牌自动兼容外部JWK。没有可用JWT配置时私有接口关闭。本地管理员bootstrap仅限显式dev/local环境及进程配置，不是生产默认账号。

公开接口为GET `/api/public/**`、图形验证码及指定认证POST；其他接口需有效JWT。匿名放行的支付回调只触发供应商独立查询，仍需服务端验证查单签名、交易、币种与金额。操作者只来自验签后的UUID subject，不接受客户端 `isAdmin`、`X-User-Id` 或body内userId冒充；管理员再查数据库身份，订单/地址/聊天等还校验归属。管理员走 `/me/orders/{id}` 也不能读取他人订单。

**当前没有全局统一 `{code,data,message}` 包装。** DTO/Map/List直接返回对象或数组，void成功可能无body；旧RPC的String通常由字符串转换器返回裸文本，实际按Content-Type解析，不保证总是带JSON引号。`ERR:...`、`false` 或带truthy `err` 的对象即使HTTP200也表示业务拒绝；SDK转为 `API_BUSINESS_REJECTED`。不要只看HTTP200弹成功。

| 场景 | 处理约定 |
| --- | --- |
| 401 / 403 | 清理失效会话并重新登录 / 显示无权限；不当作业务成功 |
| 404 | 对象不存在或当前身份无权查看 |
| 409 | 状态、轮次、价格、配置版本或幂等内容冲突；重新读取后再确认 |
| 429 | 验证码等请求过频 |
| 503 | 服务、供应商、配置或数据库不可用；保留原请求ID，不伪造回执 |
| ProblemDetail | 指定域controller通常返回 `type,title,status,detail`，错误码一般在detail |
| 认证拒绝 | 部分认证接口返回 `{message:业务码}`；login/wechat可能走默认错误处理 |
| 其他错误 | 兼容Spring默认 `timestamp,status,error,path`、安全过滤器空body、RPC错误文本；先判断HTTP状态 |

分页通常为 `limit/offset`，不是统一Page对象，也没有统一total。本人订单/求购/行程/分录/通知默认20/0，limit为1..100，offset为0..1000000；地址/购物车/收藏默认20/0且offset非负。公共列表部分只有limit，具体看全量索引。

后台列表 `POST /api/rpc/admin_console_list` body为 `kind,limit,keyword,offset`，默认limit80并夹到1..200，offset默认0且非负，直接返回数组。支持25个kind，keyword目前仅users消费。违规与佣金RPC只有limit，不能按有offset的完整分页使用。金额按十进制处理，金融DTO多为字符串；SQL响应可能是数字或字符串。输入camelCase与SQL输出snake_case需按各接口保留。

详细安全链、25个kind及错误差异见[认证补充](api/AUTH_RPC_NOTES.md)。接口声明源：[网关](../server/src/main/java/com/kuaixietech/config/ClientApiGatewayFilter.java)、[安全配置](../server/src/main/java/com/kuaixietech/security/ApiSecurityConfig.java)、[SDK](../client/packages/sdk/src/client.ts)。

## 常用业务：完整请求与响应

### 1. 图形验证码、联系方式验证码、注册、登录

#### 图形验证码

`GET /api/auth/captcha`，匿名。响应字段为 `id` 和 PNG data URL `image`：

```json
{
  "id": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1",
  "image": "data:image/png;base64,<PNG内容>"
}
```

前端展示图片，由用户输入4位图形码；答案不由接口明文返回。图形码在当前进程内保存、有效期120秒，消费一次即删除，错误也消耗该挑战；失败后重新申请。

#### 联系方式验证码

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

#### 注册

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

#### 密码或验证码登录

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

`send-code/register/reset-password/recovery-help` 的部分业务拒绝由该 Controller 返回对应HTTP状态及 `{"message":"业务错误码"}`；登录异常并非同一包装，客户端还要处理HTTP状态。依据：[AuthLoginController](../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java)、[AuthLoginService](../server/src/main/java/com/kuaixietech/service/account/impl/AuthLoginService.java)、[AuthRecoveryService](../server/src/main/java/com/kuaixietech/service/account/impl/AuthRecoveryService.java)、[AuthCaptchaService](../server/src/main/java/com/kuaixietech/service/account/impl/AuthCaptchaService.java)。

### 2. 本人订单分页和详情

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

SQL投影字段是 `snake_case`，不要直接按发布/下单DTO的 `camelCase` 读回。分页详情来自 [AccountCentreController](../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java)、[AccountCentreService](../server/src/main/java/com/kuaixietech/service/account/impl/AccountCentreService.java)、[AccountCentreMapper.xml](../server/src/main/resources/mapper/account/AccountCentreMapper.xml)。

### 3. 发布需求、行程和商品

#### 需求发布与审核

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

#### 带货行程

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

依据：[PortalController](../server/src/main/java/com/kuaixietech/controller/PortalController.java)、[PortalDtos](../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java)、[PortalServiceImpl](../server/src/main/java/com/kuaixietech/service/portal/impl/PortalServiceImpl.java)。

#### 商品发布和上架

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

依据：[ProductPublishingController](../server/src/main/java/com/kuaixietech/controller/ProductPublishingController.java)、[ProductPublishingService](../server/src/main/java/com/kuaixietech/service/catalog/support/ProductPublishingService.java)。

### 4. 商品报价、确认下单和幂等

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

依据：[ProductCheckoutController](../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutController.java)、[ProductCheckoutModels](../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutModels.java)、[ProductCheckoutService](../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutService.java)、[IntentPricing](../server/src/main/java/com/kuaixietech/service/trading/support/IntentPricing.java)。

### 5. 地址默认切换

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

依据：[SavedCommerceController](../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java)、[SavedCommerceService](../server/src/main/java/com/kuaixietech/service/account/impl/SavedCommerceService.java)。

### 6. 店铺申请与审核

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

依据：[UploadController](../server/src/main/java/com/kuaixietech/storage/UploadController.java)、[PrivateUploads](../server/src/main/java/com/kuaixietech/storage/PrivateUploads.java)、[ShopController](../server/src/main/java/com/kuaixietech/shops/ShopController.java)、[ShopService](../server/src/main/java/com/kuaixietech/shops/ShopService.java)。

### 7. 交易纠纷和售后状态

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

依据：[TradeCaseController](../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java)、[CaseModels](../server/src/main/java/com/kuaixietech/cases/CaseModels.java)、[TradeCaseService](../server/src/main/java/com/kuaixietech/cases/TradeCaseService.java)。

## 支付、保险、配置与资金边界

### 支付与汇率

来源：[PaymentsController.java](../server/src/main/java/com/kuaixietech/payments/PaymentsController.java)、[PaymentModels.java](../server/src/main/java/com/kuaixietech/payments/PaymentModels.java)、[PaymentService.java](../server/src/main/java/com/kuaixietech/payments/PaymentService.java)。

| 方法与路径 | 请求 | 响应与调用边界 |
| --- | --- | --- |
| GET `/api/public/payments/currencies` | 无 | 数组，每项 `code, minorDigits, name, nameEn`。这是 JDK ISO 币种精度目录，**不是已签约收款币种清单**。 |
| GET `/api/public/payments/methods` | query `platform`，默认 `web`，允许 `web,h5,mp_weixin,app` | 数组：`id,name,method,platform,currencies,status,settlementCurrency,spreadBps`。仅枚举该终端已启用商户；`status` 为 `configured/credentials_missing/provider_unavailable`，币种为商户与供应商实际能力交集。该读取对银行卡能力可能发起供应商查询。 |
| GET `/api/public/payments/rate` | query `base=CNY`、必填 `currency`，均大写 ISO 代码 | `base,currency,rate,source,asOf,validUntil,referenceOnly=true`。参考汇率不能替代锁定报价。 |
| POST `/api/me/payments/quotes` | `QuoteInput` | `Quote`；创建当前用户的限时人民币充值报价。 |
| POST `/api/me/payments` | `ConfirmInput` | `Payment`；创建或取回幂等支付尝试。此处不是直接商品订单支付。 |
| GET `/api/me/payments` | 无分页参数 | 本人最近20条 `Payment`。 |
| GET `/api/me/payments/{id}` | path `id` | 本人 `Payment`；非已支付记录会向原商户查询，不是纯数据库读取。 |
| GET `/api/portal/admin/payments/settings` | 管理员 | `settlementCurrency=CNY,merchants,fxSources,currencies,adapters`。商户记录含 `revision`，无实际私钥。 |
| PUT `/api/portal/admin/payments/merchants` | 管理员，`MerchantInput` | `{ "id": "…" }`。商户业务配置独立于通用耦合器 WXPAY/ALIPAY 目录。 |
| PUT `/api/portal/admin/payments/fx-sources` | 管理员，`FxInput` | `{ "id": "…" }`。 |
| POST `/api/payment-callback/{merchant}/{id}` | 渠道通知入口；路径为商户ID和支付尝试ID | 已独立确认付款：支付宝 `200 success`，其他适配器 `204`；未确认返回 `503 failure`。详细证据规则见下文。 |

请求与响应字段：

| 类型 | 字段与约束 |
| --- | --- |
| `QuoteInput` | 必填 `merchantId,paymentCurrency,amountCny`。`amountCny` 为最多7位整数、最多2位小数的字符串；业务限定 `0 < amount <= 1,000,000.00 CNY`。 |
| `ConfirmInput` | 必填 `quoteId,requestId`；`requestId` 为 UUID，同一次确认/网络重试保留同一个值。 |
| `Quote` | `id,merchantId,merchantName,method,platform,baseCurrency,baseAmount,paymentCurrency,paymentAmount,rate,spreadBps,rateSource,rateAsOf,expiresAt,settlementCurrency`。 |
| `Payment` | `id,status,baseCurrency,baseAmount,paymentCurrency,paymentAmount,merchantName,checkout`。`checkout` 为 `null` 或 `{kind,value}`；`kind` 允许 `redirect,qr_text,wechat_jsapi,wechat_app,alipay_app`，`value` 根据类型为URL、二维码文本或SDK参数JSON字符串。 |
| `MerchantInput` | 可选 `id`；必填 `name,adapter,method,platform,currencies`；`credentialPrefix` 为大写凭据引用前缀，正则 `[A-Z][A-Z0-9_]{2,39}`；`spreadBps=0..2000`，`quoteTtlSeconds=30..900`，`enabled`；可选 `settings`。商户名称最长80，币种最多250种。 |
| 商户 `settings` | 只允许 `notifyUrl,returnUrl,gatewayUrl,checkoutHosts`，值最长1500。**不得放入 api key、私钥或其他秘密字段**；未知键被拒绝。启用前会校验适配器能力和凭据。`notifyUrl` 为回调基础路径，适配器会追加 `/{merchantId}/{paymentId}`。 |
| `FxInput` | 可选 `id`；必填 `name,kind`；`kind` 仅 `open_er_api,rates_json_v1`；`endpoint,credentialPrefix,maxAgeSeconds=60..172800,enabled,transactionEnabled`。`open_er_api` 固定使用服务端内置 CNY 地址，忽略所给endpoint；自建源必须满足URL策略。 |

正确顺序是读取实际支付方式、申请服务端报价、显示 `paymentAmount/paymentCurrency/expiresAt` 给用户确认，然后用报价ID和固定幂等键创建支付。过期报价、商户修订变化或币种能力变化需要重新报价；不能把前端估算金额传为已确认到账。相同用户的 `requestId` 已绑定另一报价时拒绝复用；同报价已有支付时取回已有尝试。外部请求已进入派发中而结果未知时，不创建新的外部交易参考号盲目补发。

钱包统一以人民币记账和结算。服务端按ISO精度换算：`CNY金额 × 汇率 × (1 + spreadBps/10000)`，按目标币种最小单位四舍五入；日元等0小数币种由目录的 `minorDigits` 决定。付款只有在供应商验证收据的支付ID、币种、金额及交易号全部匹配，并落库唯一收据后，才按锁定的CNY基础金额充值钱包。

汇率源必须显式启用；交易报价还要求 `transactionEnabled=true`，查询参考汇率不等于获得商业交易授权。源均失败或过期时拒绝报价，不用固定假汇率补齐。CNY同币种换算为1；非CNY获取新汇率、新支付报价、新支付创建受耦合器LIVE模式门控，SANDBOX不会替真实钱包接收生产付款。

#### 当前支付适配能力与验签

| 适配器ID | 已编码终端/币种 | 证据与限制 |
| --- | --- | --- |
| `wechat_native_v3` | `web`，method `wechat`，CNY | 微信APIv3 Native；[WechatNativeAdapter.java](../server/src/main/java/com/kuaixietech/payments/WechatNativeAdapter.java)。 |
| `wechat_jsapi_v3` | `mp_weixin`，`wechat`，CNY | 需要本人已绑定且来源一致的openid；返回小程序SDK参数；[WechatJsapiAdapter.java](../server/src/main/java/com/kuaixietech/payments/WechatJsapiAdapter.java)。 |
| `wechat_app_v3` | `app`，`wechat`，CNY | 返回App支付SDK参数；客户端原生支付及商户开通须单独验收；[WechatAppAdapter.java](../server/src/main/java/com/kuaixietech/payments/WechatAppAdapter.java)。 |
| `alipay_rsa2` | `web,h5,app`，`alipay`，CNY | RSA2官方支付/查询协议；App需客户端接支付宝SDK；[AlipayPageAdapter.java](../server/src/main/java/com/kuaixietech/payments/AlipayPageAdapter.java)。 |
| `hosted_bank_v1` | `web,h5,app,mp_weixin`，`bank_card/credit_card`；币种由已验证网关能力返回 | 项目自定义HMAC托管桥接协议；**不是银联/所有银行/所有信用卡现成直连**。必须先有实际收单服务实现该桥接并验收合同能力；[HostedBankAdapter.java](../server/src/main/java/com/kuaixietech/payments/HostedBankAdapter.java)。 |

回调入口当前**不解析或直接验签传入的回调body**，只把路径ID作为查询触发信号；任何回调body中的“成功”都不能入账。服务端使用历史商户快照向供应商独立查单，验证响应签名和订单标识/金额/币种后再入账。前端不得伪造回调完成充值。

- 微信查单/下单响应校验 `Wechatpay-Serial/Timestamp/Nonce/Signature`，平台序列号必须匹配，时间偏差不超过300秒，RSA签名覆盖原始响应内容。
- 支付宝查单响应校验平台RSA签名，验证签名时保留响应子对象的原始JSON文本，随后检查业务成功码与金额/交易标识。
- 银行卡桥接调用 `/capabilities,/payments,/query`，使用HMAC签名信封，校验商户、nonce、签发/过期时间及返回币种能力；未签约网关无能力返回时不能收款。
- 已支付尝试的重复通知只返回对应成功响应；实际入账还有收据唯一性和资金事务校验。当前没有独立的“接收、解密并验证微信通知事件”实现，不应在文档中虚构为已有能力。

来源：[PaymentTransactions.java](../server/src/main/java/com/kuaixietech/payments/PaymentTransactions.java)、[PaymentSecrets.java](../server/src/main/java/com/kuaixietech/payments/PaymentSecrets.java)。

### 商品确认与托管

来源：[ProductCheckoutController.java](../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutController.java)、[ProductCheckoutModels.java](../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutModels.java)。

| 方法与路径 | 请求 | 响应 |
| --- | --- | --- |
| GET `/api/public/products/{id}/trips` | path商品ID；query `limit=100,offset=0` | 该商品所有者可承接的行程数组，仍受商品/行程可用性规则限制。 |
| POST `/api/me/product-orders/quote` | `ProductCheckoutModels.Input` | `productId,tripId,storeId,itemName,itemSpec,unitPrice,quantity,goodsAmount,serviceFee,insurancePremium,buyerEscrow,vendorDeposit,currency,pricingVersion,intentHours,productFingerprint`。 |
| POST `/api/me/product-orders` | 同一Input，加确认快照和幂等键 | `{id,status}`；真实钱包托管、库存占用与订单创建在服务端进行。 |

两个商品报价/确认入口均要求已实名买家。Input必填 `productId,tripId`（UUID）、`quantity=1..100,contactName`（最长64）、`contactPhone`（可带+、数字、空格/连字符）、`address`（最长512）、`insuranceChoice=yes/waived`。可选 `expectDate`（日期）、`note`（最长1024）、`insuranceQuoteId`（UUID）。确认时还需提交 `requestId`（UUID）、`expectedUnitPrice`、`expectedBuyerEscrow`、`expectedProductFingerprint`（64位十六进制）；金额快照使用服务端上一笔报价值，不自行重算。

当前新订单 `pricingVersion=escrow_v2_external_insurance`：服务费货值5%，带货人保证金货值20%；平台商品托管不收保险费，`insurancePremium=0.00`。选择保险须使用保险公司自己的报价及直付链路，商品订单确认并不代表已投保、已交保费或保险已生效。旧 `escrow_v1` 的1%、最低5元历史逻辑不能作为新单报价规则。来源：[IntentPricing.java](../server/src/main/java/com/kuaixietech/service/trading/support/IntentPricing.java)。

### 充值原路退款与售后钱包退回

这两个流程用途、资金源与最终凭据不同，调用方需按接口语义展示。

#### 充值原路退款

来源：[PaymentRefundController.java](../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java)、[RefundModels.java](../server/src/main/java/com/kuaixietech/refunds/RefundModels.java)。

| 方法与路径 | 请求与用途 |
| --- | --- |
| GET `/api/me/payment-refunds/eligible` | 本人可申请的充值记录，最多100条；返回 `id,baseAmount,paymentAmount,paymentCurrency,merchantName,paidAt`。 |
| GET `/api/me/payment-refunds` | query `limit=20,offset=0`，本人申请列表。 |
| POST `/api/me/payment-refunds` | `{paymentId,requestId,reason}`；paymentId为32位小写十六进制，requestId为UUID，reason必填最长200。 |
| GET `/api/me/payment-refunds/{id}` | 本人申请详情。 |
| GET `/api/me/payment-refunds/{id}/events` | query `limit=50,offset=0`，本人申请事件。 |
| POST `/api/me/payment-refunds/{id}/cancel` | 无body；按当前申请状态决定可否撤销。 |
| POST `/api/me/payment-refunds/{id}/refresh` | 无body；对派发中/处理中记录向原渠道查验。 |
| GET `/api/portal/admin/payment-refunds` | 管理员，query `limit=20,offset=0`。 |
| GET `/api/portal/admin/payment-refunds/{id}` | 管理员申请详情。 |
| GET `/api/portal/admin/payment-refunds/{id}/events` | 管理员，query `limit=50,offset=0`。 |
| POST `/api/portal/admin/payment-refunds/{id}/review` | `{approve:boolean,reason:string}`；原因必填最长200。审核通过不等于退款到账。 |
| POST `/api/portal/admin/payment-refunds/{id}/dispatch` | 无body；仅已批准记录允许真实派发，受LIVE模式和历史商户凭据约束。 |
| POST `/api/portal/admin/payment-refunds/{id}/refresh` | 无body；查询原渠道退款结果，不盲目再派发。 |

申请/详情响应字段为 `id,paymentId,status,baseCurrency,paymentCurrency,reason,reviewNote,providerRefundId,lastError,createdAt,updatedAt,baseAmount,paymentAmount`。事件响应为 `{id,action,detail,createdAt}` 数组。分页限制 `1<=limit<=100,offset>=0`，返回数组，无内置总数。

仅具有可匹配真实收据的CNY充值可申请，且当前只对微信三个适配器及支付宝开放；实现为**原充值全额退款**，不是任意部分退款、多币种退款或银行卡桥接退款。按原支付商户快照查询/派发；供应商不明结果保留处理中，不因HTTP请求已受理就标记完成。支付宝退款操作回执仍须独立查询原退款请求号，微信/支付宝最终收据均要核对支付ID、交易号、退款ID、币种及金额。来源：[OriginalPaymentRefundGateway.java](../server/src/main/java/com/kuaixietech/payments/OriginalPaymentRefundGateway.java)。

#### 售后托管货款退回平台钱包

来源：[AftersalesEscrowController.java](../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowController.java)、[AftersalesEscrowFunds.java](../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowFunds.java)。

| 方法与路径 | 请求与响应 |
| --- | --- |
| GET `/api/portal/admin/after-sales/{id}/escrow-budget` | 管理员；售后争议ID。返回预算或 `status=blocked,reasonCode`，不是扣款接口。 |
| POST `/api/portal/admin/after-sales/{id}/escrow-refund` | 管理员；`{requestId:UUID,expectedRound:int>=1,note:string}`，note必填最长200。要求争议轮次匹配、判定及资金证据均通过。 |
| GET `/api/me/after-sales/escrow-refunds` | 本人记录，query `limit=20,offset=0`。 |
| GET `/api/me/after-sales/escrow-refunds/{id}/events` | 本人执行单事件，query `limit=50,offset=0`。 |
| GET `/api/portal/admin/after-sales/escrow-refunds/{id}/events` | 管理员执行单事件，同分页。 |

预算通用字段 `caseId,orderId,currency,refundMethod,destination,status`；可执行时还有 `goodsAmount,alreadyRefunded,refundableGoods,requestedAmount,buyerEscrowHeld,serviceFeeHeld,shippingHeld,requiresAccounting,sourceRound,rewardReviewRequired`。已执行/执行结果为 `id,caseId,orderId,requestId,sourceRound,amount,currency,status,refundMethod,destination,requiresAccounting,rewardReviewRequired,createdAt`。

该接口固定 `currency=CNY,refundMethod=escrow_wallet,destination=platform_wallet`。资金退回本人平台钱包，并不发起微信/银行卡原路退款；需核实真实托管余额、商品预约/库存及既有退款上限，不能由后台填写任意金额。幂等键绑定操作者、争议及轮次/备注；已有其他执行或相同key换内容返回冲突。后续完整账务、提成与纠纷全状态仍按业务清单验收。

### 钱包、调账、发票与未开放提现

| 方法与路径 | 请求与响应 |
| --- | --- |
| GET `/api/me/wallet` | 本人钱包 `{balance_amount,currency=CNY,points}`。余额数字来自数据库Map，调用方仍应按十进制处理。 |
| GET `/api/me/wallet/ledger` | query `limit=20,offset=0`；数组字段 `id,kind,amount,balance_after,order_id,note,created_at`。 |
| POST `/api/rpc/admin_adjust_balance` | 管理员，`{userId,delta,note}`；note必填，绝对金额0.01..100000，禁止给本人调账。返回Java String，通常为裸文本 `text/plain`，实际按响应Content-Type处理；值为 `OK:BALANCE:…` 或 `ERR:…`，SDK需兼容ERR。真实目标钱包不存在会抛错，不产生虚假调账成功通知。 |
| POST `/api/rpc/admin_commission_rows` | 管理员，`{limit}`，省略默认100；数组字段 `row_id,inviter_name,invitee_name,order_no,tier_level,rate,base_fee,amount_cny,row_status,revoked_reason,made_at`。 |
| POST `/api/rpc/request_invoice` | 本订单买家，`{orderId,titleType,titleName,taxNo,kind,email,remark}`；titleType为personal/company，kind为electronic_general/vat_special，企业抬头需税号；仅已完成订单可申请。返回字符串 `OK:INVOICE:金额` 或 `ERR:…`，创建的是待处理申请。 |
| POST `/api/rpc/admin_issue_invoice` | 管理员，`{invoiceId,approve,invoiceNo,fileUrl,reason}`；当前approve=true返回 `ERR:INVOICE_ISSUER_UNAVAILABLE`，不能用任意号码和URL标记已开票。驳回需reason，返回 `OK:REJECTED` 或 `ERR:…`。没有真实税票开具/验真/邮件送达闭环。 |

RPC字符串结果即使HTTP为200也可能是 `ERR:…`，客户端不能只按HTTP成功就弹“操作成功”。上述调账是有审计的内部钱包调整，不能作为第三方真实充值收据或银行出款凭证。

提现当前没有实际出款Adapter或已核验出款回执；`WithdrawMigrationServiceImpl` 的新申请及标记已付款保持 `ERR:WITHDRAWAL_PAYOUT_UNAVAILABLE`，不创建新的不可付款请求/冻结。历史记录审批只意味着approved，不意味着打款；拒绝旧记录而缺唯一原冻结证据时要求资金来源对账，不直接返余额。该服务当前未发现controller调用入口，**不得据其方法名编造可用的提现HTTP路由**。

来源：[AccountCentreController.java](../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java)、[AccountCentreMapper.xml](../server/src/main/resources/mapper/account/AccountCentreMapper.xml)、[AdminFinanceRpcController.java](../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java)、[Batch10RpcController.java](../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java)、[InvoiceRpcService.java](../server/src/main/java/com/kuaixietech/service/finance/impl/InvoiceRpcService.java)、[WithdrawMigrationServiceImpl.java](../server/src/main/java/com/kuaixietech/service/finance/impl/WithdrawMigrationServiceImpl.java)。

### 商品保险

来源：[InsuranceController.java](../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java)、[InsuranceModels.java](../server/src/main/java/com/kuaixietech/insurance/InsuranceModels.java)、[InsuranceCatalog.java](../server/src/main/java/com/kuaixietech/insurance/InsuranceCatalog.java)。

**当前生产保险 Adapter 实现为零，真实运行 `settings.adapters=[]`。保险公司名单只是参考，不代表API合作；不能只填写API key就投保。启用未接入产品会拒绝，公共产品列表过滤未接入产品。** 需要与持牌机构落实公开或合作协议后实现报价、申请、可靠查单与保单证据。保费由所选保险公司定价并直接收取，平台不提供担保。

| 方法与路径 | 请求 | 响应 |
| --- | --- | --- |
| GET `/api/public/insurance/companies` | 无 | 参考机构数组，字段 `code,name,status=reference_only`。 |
| GET `/api/public/insurance/products` | query `platform=web`，同支付终端枚举 | 可售产品数组：`id,insurerCode,insurerName,name,kind,termsUrl,exclusionsUrl,claimsUrl,paymentMode=insurer_direct,status=quote_required`；当前通常空数组。 |
| GET `/api/me/insurance/pk-subject` | query `demandId,quoteId` | 已实名买家本人需求中可用竞价标的：`subjectId,subjectType=pk_quote,itemName,declaredValue`。 |
| POST `/api/me/insurance/quotes` | 已实名买家，`Insurance QuoteInput` | `Insurance Quote`。没有可用Adapter时不可完成真实报价。 |
| GET `/api/me/insurance/quotes/{id}` | 本人报价ID | 本人保险报价快照。 |
| POST `/api/me/insurance/applications` | 已实名买家，`ApplyInput` | `Application`；仅此请求创建申请/保险公司收银入口，不表示保单生效。 |
| GET `/api/me/insurance/consents` | 无 | 本人最近100条保险同意记录：`id,consent_type,notice_version,recipient_name,purpose,information_types,rights_method,accepted,accepted_at`。 |
| GET `/api/me/insurance/applications` | 无 | 本人最近50条 `Application`。 |
| GET `/api/me/insurance/orders/{orderId}` | 本人买家订单 | `{application:Application或null}`。 |
| GET `/api/me/insurance/applications/{id}/record` | 本人申请ID | 只读取已存记录，返回 `Application`。 |
| GET `/api/me/insurance/applications/{id}` | 本人申请ID | 返回 `Application`；非reserved申请会向原保险公司查验。 |
| GET `/api/portal/admin/insurance/settings` | 管理员 | `directory,products,adapters,paymentMode=insurer_direct`，产品内含修订号与凭据前缀，密钥不返回。 |
| PUT `/api/portal/admin/insurance/products` | 管理员，`ProductInput` | `{id}`。可保存禁用配置；启用前要求Adapter、凭据和认证终端能力。 |
| GET `/api/portal/admin/insurance/applications` | 管理员 | 最近100条，字段 `id,user_id,order_id,quote_id,insurer_code,status,provider_application_ref,policy_number,coverage_ref,coverage_starts_at,coverage_ends_at,last_checked_at,last_error_code,created_at`。 |

| 类型 | 字段与约束 |
| --- | --- |
| 保险 `QuoteInput` | 必填 `productId,subjectType,subjectId,itemName,declaredValue`；subjectType为 `trip/pk_quote/order`；itemName最长255；declaredValue最多7位整数/2位小数，业务限定 `0<value<=1,000,000`；可选 `platform`，省略默认web。标的需真实可用且与用户和货物匹配。 |
| 保险 `Quote` | `id,productId,insurerName,productName,kind,declaredValue,premium,currency,termsUrl,exclusionsUrl,claimsUrl,termsVersion,expiresAt,paymentMode,protectionStatus`。查询报价返回 `paymentMode=insurer_direct,protectionStatus=not_issued`。 |
| `ApplyInput` | 必填 `orderId,quoteId,requestId`；requestId为36位UUID式字符串；`termsAccepted=true,disclosuresAccepted=true` 必须由客户主动同意。 |
| `Application` | `id,orderId,status,insurerName,productName,premium,currency,checkoutUrl,policyNumber,certificateUrl,claimsUrl,coverageRef,termsVersion,startsAt,endsAt,platform,paymentMode,protectionStatus`。 |
| `ProductInput` | 可选 `id`（1..64字母数字下划线连字符）；必填 `insurerCode,insurerName,name,kind,adapter,providerProductCode,credentialPrefix,termsUrl,exclusionsUrl,claimsUrl,serviceHosts,checkoutHosts`；`enabled`。机构名/产品名最长200，机构/种类/Adapter最长64，渠道产品号最长128；凭据前缀同大写规则。条款/免责/理赔URL各最长1000；域名白名单各1..10项、小写域名且通过服务端URL策略。 |

保险申请幂等关联订单、报价和requestId，保留条款版本、授权用途及告知记录；不能复用报价给其他订单或更改标的金额。保单必须由验证过的保险公司回执证明直接收费、保单号、承保标的/金额、条款版本和承保起止时间等。`checkoutUrl`、已付款的客户端回跳或平台订单状态都不是承保凭证。保险公司退保/理赔当前为其托管链接和Adapter扩展边界；没有已实现的公共“平台赔付”“平台担保”接口或保险公司回调验签controller，勿自行假定其存在。

### 配置中心与密钥

来源：[CouplerController.java](../server/src/main/java/com/kuaixietech/coupler/CouplerController.java)、[CouplerModels.java](../server/src/main/java/com/kuaixietech/coupler/CouplerModels.java)、[CouplerService.java](../server/src/main/java/com/kuaixietech/coupler/CouplerService.java)。以下均管理员权限。

| 方法与路径 | 请求 | 响应与含义 |
| --- | --- | --- |
| GET `/api/portal/admin/integration-center` | 无 | `runtime:{mode,revision},categories,encryptionReady`；category含 `code,name,description,providers`。 |
| PUT `/api/portal/admin/integration-center/runtime` | `{mode:SANDBOX或LIVE,expectedRevision:非负整数}` | `{mode,revision}`，修订不一致409，先刷新再操作。仅切路由模式，不能作为供应商验收。 |
| PUT `/api/portal/admin/integration-center/{category}/{provider}/{mode}` | `Save` | `{revision,configRevision}`。mode严格为 `SANDBOX/LIVE`。 |
| POST `/api/portal/admin/integration-center/{category}/{provider}/{mode}/validate` | 无body | `mode,revision,ready,missingFields,invalidFields,unreadableFields,sdkAvailable,businessVerified=false,checks`。**仅本地参数/SDK构造检查，不发供应商业务请求**。 |
| GET `/api/portal/admin/integrations` | 无 | 旧专业集成 `nativeDeliveryManaged,encryptionReady,products,specialized`；specialized为payments/insurance。 |
| PUT `/api/portal/admin/integrations/{vendor}/{product}` | `{enabled,remark,values,clearFields}` | `{status:saved}`；旧配置模式仍保留专业凭据包和兼容通道。 |
| GET `/api/portal/admin/integrations/sms-deliveries` | 无 | 最近100条投递记录，仅 `id,channel,status,error,created_at,updated_at`，旧非安全错误文本统一脱敏。 |

`Save`字段为 `values`（最多80项，键非空、值非null且最长16000）、`clearFields`（最多80个字段名）、`enabled,selected,expectedRevision`（分类作用域修订号，非configRevision）。`values`只写变更字段；未提交字段保留，删除已有字段必须列入clearFields。同一字段不能同时出现两处，未知键或 `_` 内部键拒绝。`selected=true`且 `consumerWired=false` 的条目拒绝选中，SDK能构造不代表已接业务消费者。

每个provider元数据含 `vendor,product,code,name,capability,docs,sdkAvailable,businessVerified=false,consumerWired,supportedModes,implementedOperations,fields,configurationByMode`。field含 `key,label,secret,required,placeholder,inputType,options`。每模式配置含 `revision,configRevision,enabled,selected,values,configuredFields,unreadableFields,updatedAt`；其中 `values` **仅非secret字段**。前端根据configuredFields显示“已配置”，不要要求回显真实secret或用星号串覆盖密钥。

实际密钥只能经管理员配置写入受保护字段或环境设置，禁止放进URL、浏览器地址、日志、示例代码、商户settings或返回体。服务端需先设置32字节配置加密根密钥。新耦合器 `env:VARIABLE_NAME` 在明确保存时解析真实值、加密为不可变版本，后续环境变量变化不会偷偷重绑该版本；历史未版本化env引用要求重新保存。`unreadableFields` 表示当前根密钥等条件无法解密，不能显示为“供应商已配置成功”。

旧 `/integrations` 请求：`values`最多30项，每值最长16000；`clearFields`最多30，`remark`最长200。其fields元数据包含 `key,secret,required,configured,status`，只有非secret项可能带 `value`。`status=stored/unreadable/missing` 不等于供应商验收。新旧中心并存，**通用WXPAY/ALIPAY表单不能替代支付商户配置，保险名单也不能替代保险Adapter**。

#### SDK工具没有通用远程调用接口

`ClientFactories`、`PublicServiceAdapters`、`WechatMiniProtocol`、RTC签名及显式模式支付协议属于服务端SDK/协议工具。当前没有可供前端指定任意provider、操作名、URL和凭据后透传调用的SDK代理controller。应由经过授权的具体业务服务获取版本化配置后调用，不能为联调编造 `/sdk/invoke`、`/sdk/dispatch` 等路由。

47个目录项不能整体标为已实现：typed客户端构造、本地validate、协议工具、业务消费者、真实供应商验收应分别看待。当前仅部分短信、DirectMail、小程序登录、OSS/COS等有消费接线，其余路线仍须实际业务实现；保险零Adapter、银行卡自定义桥接等缺口见本页对应部分。来源：[ClientFactories.java](../server/src/main/java/com/kuaixietech/integrationsdk/ClientFactories.java)、[PublicServiceAdapters.java](../server/src/main/java/com/kuaixietech/integrationsdk/PublicServiceAdapters.java)、[CouplerAdapterRegistry.java](../server/src/main/java/com/kuaixietech/coupler/CouplerAdapterRegistry.java)。

### 联系方式投递健康检查

GET `/api/portal/admin/contact-delivery/health` 为管理员只读诊断，**不会领取验证码或发信**。响应：

- `checkedAt,configuration,lastRun,dispatchConfigured,providerDeliveryVerified=false,sentMeaning,blockedPolicy,queue,state`；读取队列失败还带 `queueDiagnostic`。
- `configuration`包含sms/email诊断；`lastRun`含 `checkedAt,state,sms,email,nativeOwnership,legacy`。
- 单个诊断字段 `channel,state,stage,reasonCode,exceptionType,sqlState,databaseError,missingTable,observedAt`，不含验证码、收件人、密钥、SQL文本或原始异常消息。
- queue每项为 `channel,status,total`。总状态可能为 `idle,paused,ready,degraded,requires_reconciliation`；缺表可在诊断中得到 `migration_required` 与安全表名。
- `sentMeaning=provider_accepted_not_recipient_delivery`：投递记录sent只能说明供应商受理，不代表用户手机/邮箱已收到。`dispatchConfigured=true`也不是送达验收。
- `blockedPolicy=never_automatically_replay; reconcile_with_provider_before_any_manual_action`：结果不明/blocked记录先与供应商对账，不自动重放。当前没有“点击诊断立即群发/自动补发”接口。

来源：[ContactDeliveryHealthController.java](../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryHealthController.java)、[ContactDeliveryDiagnostic.java](../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryDiagnostic.java)、[ContactCodeDeliveryScheduler.java](../server/src/main/java/com/kuaixietech/scheduler/ContactCodeDeliveryScheduler.java)。

### 联调错误处理

成功结果直接为DTO/数组/Map，或RPC方法返回的String；String通常为裸文本，调用方应按实际Content-Type解析。本域没有统一 `{code,data,message}` 成功包裹。controller局部异常处理使用HTTP状态及ProblemDetail；权限、安全过滤或其他controller的错误形状以主接口文档为准。

| 场景 | 当前对外行为 |
| --- | --- |
| 支付输入错误 | 400 ProblemDetail，`detail=PAYMENT_INPUT_REJECTED`，不暴露底层输入异常文字。 |
| 支付/汇率供应商不可用 | 503 `PAYMENT_OR_FX_UNAVAILABLE`；存储异常503 `PAYMENT_STORAGE_UNAVAILABLE`。 |
| 配置版本冲突 | 409，耦合器 `COUPLER_RUNTIME_CHANGED_REFRESH/COUPLER_SCOPE_CHANGED_REFRESH` 等安全原因；刷新后重新审阅操作，不自动覆盖。 |
| 配置参数/加解密/缺表错误 | 新中心400 `COUPLER_CONFIGURATION_REJECTED` 或503 `COUPLER_CONFIGURATION_UNAVAILABLE`；旧配置对应 `INTEGRATION_CONFIGURATION_REJECTED/UNAVAILABLE`。 |
| 退款或商品确认被拒 | 局部ResponseStatusException保留安全reason；非法参数一般400，服务/存储不可用503。 |
| 售后资金证据不足或旧请求冲突 | 409 ProblemDetail或预算 `status=blocked,reasonCode`；不得把blocked展示为退款成功。 |
| 新外部动作处于SANDBOX | LIVE门控可返回409 `COUPLER_LIVE_MODE_REQUIRED`。历史真实支付/对象查询按原版本查询，不因模式切换被改解释。 |

客户端对创建/派发的超时应先读取原幂等请求状态；明确“未知”不能当作“未执行”重新生成键。401走重新登录，403不隐藏为成功；503显示服务暂不可用并保留原单号，不伪造报价、保单、收款或退款凭证。

## 支付、退款、配置与诊断完整 JSON 示例

### 1. 充值报价与创建支付

先 `GET /api/public/payments/methods?platform=web` 选择实际可用的商户和币种，再由登录用户 `POST /api/me/payments/quotes`：

```json
{"merchantId":"11111111111141118111111111111111","paymentCurrency":"CNY","amountCny":"100.00"}
```

完整Quote响应示例（CNY同币种、spreadBps=0）：

```json
{
  "id":"22222222222242228222222222222222",
  "merchantId":"11111111111141118111111111111111",
  "merchantName":"演示微信商户",
  "method":"wechat",
  "platform":"web",
  "baseCurrency":"CNY",
  "baseAmount":"100.00",
  "paymentCurrency":"CNY",
  "paymentAmount":"100.00",
  "rate":"1",
  "spreadBps":0,
  "rateSource":"identity",
  "rateAsOf":"2030-01-01T00:00:00Z",
  "expiresAt":"2030-01-01T00:05:00Z",
  "settlementCurrency":"CNY"
}
```

用户确认实际返回的金额/币种/有效期后，`POST /api/me/payments`：

```json
{"quoteId":"22222222222242228222222222222222","requestId":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaa10"}
```

完整Payment响应示例：

```json
{
  "id":"33333333333343338333333333333333",
  "status":"pending",
  "baseCurrency":"CNY",
  "baseAmount":"100.00",
  "paymentCurrency":"CNY",
  "paymentAmount":"100.00",
  "merchantName":"演示微信商户",
  "checkout":{"kind":"qr_text","value":"<供应商真实返回的二维码文本>"}
}
```

`checkout`也可能为null或其他已声明类型，不是任意HTML。钱包仍以锁定CNY金额记账；显示ISO币种目录不代表所有币种可收款。`GET /api/me/payments/{id}`可能触发原商户查单。超时重试保留requestId和quoteId，不能换键重复创建。前端回跳、展示二维码和回调body中的成功字样都不能代替服务端收据。

### 2. 原充值全额退款

先读取 `GET /api/me/payment-refunds/eligible`，只有该入口返回且符合真实收据条件的CNY充值才可申请。`POST /api/me/payment-refunds`：

```json
{"paymentId":"33333333333343338333333333333333","requestId":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaa11","reason":"申请退回该笔充值"}
```

申请响应完整结构示例：

```json
{
  "id":"44444444-4444-4444-8444-444444444444",
  "paymentId":"33333333333343338333333333333333",
  "status":"requested",
  "baseCurrency":"CNY",
  "paymentCurrency":"CNY",
  "reason":"申请退回该笔充值",
  "reviewNote":null,
  "providerRefundId":null,
  "lastError":null,
  "createdAt":"2030-01-01T00:10:00Z",
  "updatedAt":"2030-01-01T00:10:00Z",
  "baseAmount":"100.00",
  "paymentAmount":"100.00"
}
```

管理员 `POST /api/portal/admin/payment-refunds/{id}/review`：

```json
{"approve":true,"reason":"已核对原充值及可退余额"}
```

返回同一结构，状态approved仅表示批准。随后管理员调用 `POST /api/portal/admin/payment-refunds/{id}/dispatch`（无body），再经原渠道独立查询确认；处理中/未知不能当作退款完成。当前仅已实现微信/支付宝符合条件的CNY全额原路退款；不是多币种、任意部分金额或所有银行卡退款。售后托管退款另走escrow-refund，退到平台钱包，不等同原渠道退款。

### 3. 配置保存、修订与本地验证

先管理员 `GET /api/portal/admin/integration-center`，读取当前category/provider/mode元数据和作用域revision。下例只设置本地功能开关，不会发送短信；需先配置32字节加密根密钥，示例expectedRevision=0仅适用于实际读到0时。

`PUT /api/portal/admin/integration-center/FEATURES/flags/SANDBOX`：

```json
{"values":{"sms_enabled":"false"},"clearFields":[],"enabled":true,"selected":true,"expectedRevision":0}
```

响应：

```json
{"revision":1,"configRevision":1}
```

`expectedRevision`是分类/模式作用域修订号，不是configRevision。冲突时先刷新并重新审阅，不覆盖其他管理员修改。秘密字段根据catalog的key写入values或用已存在的 `env:VARIABLE_NAME` 引用，在明确保存时加密固定版本；GET不返回secret。缺省字段保留，清空列入clearFields，不用星号串覆盖密钥。

随后 `POST /api/portal/admin/integration-center/FEATURES/flags/SANDBOX/validate`，无body，完整示例：

```json
{
  "mode":"SANDBOX",
  "revision":1,
  "ready":true,
  "missingFields":[],
  "invalidFields":[],
  "unreadableFields":[],
  "sdkAvailable":false,
  "businessVerified":false,
  "checks":[{"code":"LOCAL_ONLY","status":"info","message":"Local configuration/SDK checks only; no provider request or live business verification"}]
}
```

这是本地配置/SDK构造检查，没有发真实供应商请求。`ready=true`不等于短信送达、付款或投保成功。模式切换 `PUT /api/portal/admin/integration-center/runtime` 请求 `{"mode":"SANDBOX","expectedRevision":0}`，成功返回 `{"mode":"SANDBOX","revision":1}`；其修订号来自runtime，不能混用provider作用域修订号。新外呼切到LIVE前仍需有效feature、真实consumer及正式供应商配置。

### 4. 联系方式投递健康检查

管理员 `GET /api/portal/admin/contact-delivery/health`，不发送、不补发验证码。下面是SANDBOX暂停且调度尚未运行的完整结构示意，不是实时回执；null诊断字段可能被省略：

```json
{
  "checkedAt":"2030-01-01T00:00:00Z",
  "configuration":{
    "sms":{"channel":"sms","state":"paused","stage":"configuration","reasonCode":"COUPLER_LIVE_MODE_REQUIRED","exceptionType":"ResponseStatusException","sqlState":null,"databaseError":null,"missingTable":null,"observedAt":"2030-01-01T00:00:00Z"},
    "email":{"channel":"email","state":"paused","stage":"configuration","reasonCode":"COUPLER_LIVE_MODE_REQUIRED","exceptionType":"ResponseStatusException","sqlState":null,"databaseError":null,"missingTable":null,"observedAt":"2030-01-01T00:00:00Z"}
  },
  "lastRun":{
    "checkedAt":null,
    "state":"not_checked",
    "sms":{"channel":"sms","state":"not_checked","stage":"configuration","reasonCode":"CONTACT_NOT_CHECKED","exceptionType":null,"sqlState":null,"databaseError":null,"missingTable":null,"observedAt":"2030-01-01T00:00:00Z"},
    "email":{"channel":"email","state":"not_checked","stage":"configuration","reasonCode":"CONTACT_NOT_CHECKED","exceptionType":null,"sqlState":null,"databaseError":null,"missingTable":null,"observedAt":"2030-01-01T00:00:00Z"},
    "nativeOwnership":{"channel":"native_ownership","state":"not_checked","stage":"configuration","reasonCode":"CONTACT_NOT_CHECKED","exceptionType":null,"sqlState":null,"databaseError":null,"missingTable":null,"observedAt":"2030-01-01T00:00:00Z"},
    "legacy":{"channel":"legacy","state":"not_checked","stage":"configuration","reasonCode":"CONTACT_NOT_CHECKED","exceptionType":null,"sqlState":null,"databaseError":null,"missingTable":null,"observedAt":"2030-01-01T00:00:00Z"}
  },
  "dispatchConfigured":false,
  "providerDeliveryVerified":false,
  "sentMeaning":"provider_accepted_not_recipient_delivery",
  "blockedPolicy":"never_automatically_replay; reconcile_with_provider_before_any_manual_action",
  "queue":[],
  "state":"paused"
}
```

若缺表/连接失败，会返回安全诊断码、SQLState/数字错误码与允许的表名；不返回SQL文本、收件人、验证码、原异常消息或密钥。出现blocked需人工与供应商对账，不自动重放。源字段定义：[ContactDeliveryHealthController](../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryHealthController.java)、[Diagnostic](../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryDiagnostic.java)。

## 声明更新与联调验收

运行以下命令刷新全量controller/DTO/SDK及RPC索引；`--check`只核对生成结果是否与当前源码一致，不连接数据库或调用供应商。

```bash
python3 server/tools/generate_api_reference.py
python3 server/tools/generate_api_reference.py --check
python3 server/tools/generate_rpc_reference.py
python3 server/tools/generate_rpc_reference.py --check
```

全量字段以附录的请求schema及对应源码为准，Java注解不替代service中的条件必填和状态检查。任何未出现于232条声明的SQL函数、service方法、旧云函数或SDK工具均不能据名字猜测HTTP路由。接口变更后同步重生成目录、前端消费类型、按钮错误处理和真实验收用例。

本轮真实运行记录见[33项隔离运行回执](audit/RUNTIME_VALIDATION.md)。真实业务库、供应商和端设备验收继续按[功能清单](audit/FUNCTION_STATUS.json)执行；配置窗口和空数组不可冒充业务闭环。
