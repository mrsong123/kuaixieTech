# 认证、响应与RPC联调约定

本文件补充[独立API主文](../API_REFERENCE.md)，以当前Java和TypeScript实现为准。完整RPC枚举见[RPC_INDEX.md](RPC_INDEX.md)、[RPC_INDEX.json](RPC_INDEX.json)和[RPC_INDEX.csv](RPC_INDEX.csv)：共 **9个controller、84个显式POST路由**，每条均附实际controller及service源码位置。存在路由、DTO或service调用不等于业务已完成，更不等于真实供应商验收。

## 地址、网关与请求头

服务默认HTTP端口为8080，生产以实际HTTPS反向代理域名为准。Java规范路径包含 `/api`，例如 `/api/me/profile`。SDK业务路径使用 `/me/profile`；`VITE_API_BASE_URL` 应填写站点根地址，不附加 `/api`。

服务端 [ClientApiGatewayFilter](../../server/src/main/java/com/kuaixietech/config/ClientApiGatewayFilter.java) 仅为六个前缀 `public`、`rpc`、`portal`、`support`、`me`、`auth` 自动补 `/api`；已有 `/api/` 的请求不重复改写。开发Vite代理将这六类以及 `payment-callback` 加前缀。移动端 `javaUrl()` 对绝对地址也会加 `/api`。

**供应商回调固定使用 `/api/payment-callback/...` 规范路径。** 服务端别名过滤器没有包含 `payment-callback`；不能因开发Vite代理支持无前缀回调，就把它当作生产服务端路由。

普通JSON请求头：

```http
Accept: application/json
Content-Type: application/json
Accept-Language: zh-CN
Authorization: Bearer <当前会话accessToken>
```

匿名接口不要求Authorization；GET通常不发送Content-Type。文件上传使用multipart，客户端不要手工填缺少boundary的Content-Type；私有下载响应为二进制。浏览器跨域需部署时配置 `CORS_ORIGINS`；当前只允许GET/POST/PUT/DELETE/OPTIONS，允许Authorization/Content-Type/Accept/Accept-Language，不使用cookie会话，`allowCredentials=false`。微信与原生App还须分别满足平台合法域名/网络设置。

源码：[SDK client](../../client/packages/sdk/src/client.ts)、[Vite代理](../../client/packages/config/src/proxy.ts)、[移动端transport](../../client/apps/mobile/src/api/client.ts)、[CORS](../../server/src/main/java/com/kuaixietech/config/CrossPlatformCorsConfig.java)。

## 登录与权限

当前安全链为stateless Bearer JWT。账号身份只取验签后UUID格式的 `sub`，不接受 `X-User-Id`、URL/body中的userId或客户端isAdmin作为当前操作者。目标 `userId` 可用于管理员操作对象，但操作者仍来自安全上下文。

| 方式 | 实际配置与限制 |
| --- | --- |
| 本地模式 | 默认 `AUTH_JWT_ENABLED=false`；`APP_JWT_SECRET` 的UTF-8字节数至少32。`/auth/login`、注册、重置密码和微信登录成功后签发HS256令牌，期限28800秒。无有效secret时登录返回不可用，私有接口关闭。 |
| 外部资源服务器模式 | `AUTH_JWT_ENABLED=true`，需有效HTTPS `AUTH_JWT_ISSUER`、`AUTH_JWT_JWK_SET_URI` 和 `AUTH_JWT_AUDIENCE`。校验签名、issuer、audience、时间与UUID subject，还要求本地有活跃profile。 |
| 两种模式衔接 | 当前 `/auth/login` 等仍调用 `LocalHmacJwtService` 签本地HS256；启用远端JWK不会把这些接口自动改为外部IdP登录。不能假设本地登录结果会被远端decoder接受；实际IdP登录/令牌来源须单独联调。 |
| 本地管理员bootstrap | 仅 `APP_ENVIRONMENT=local/dev` 且配置 `ADMIN_BOOTSTRAP_PASSWORD` 时启用；是开发初始化能力，不是公开生产默认账号或固定密码。文档不提供或记录该密码。 |

`POST /api/auth/login` 请求成员为 `channel, contact, password, code, countryCode`。`channel` 仅 `phone/email`；password非空时优先使用，否则使用code。六位纯数字按登录验证码处理；其他secret按8至20字符密码规则校验。手机号输入国家内号码，`countryCode` 如 `+86`，空值默认 `+86`；邮箱去空白并转小写。

成功对象成员为 `accessToken, token, userId, isAdmin, expiresIn`；`token` 与accessToken相同，电话登录另有 `phoneCountryCode, locale`。没有当前HTTP接口提供refresh token或服务端统一logout；前端应在401后移除会话并重新登录，不自行延长expiresIn。

匿名允许范围以 [ApiSecurityConfig](../../server/src/main/java/com/kuaixietech/security/ApiSecurityConfig.java) 为准：

- OPTIONS；GET `/api/public/**` 与 `/api/auth/captcha`。
- POST `/api/auth/login`、`wechat`、`send-code`、`register`、`reset-password`、`recovery-help`。
- POST `/api/payment-callback/**` 在安全链匿名放行，当前只作为触发原商户独立查单的信号；查单响应须验签并核对订单/金额/币种。没有传入通知body直接验签实现，详见金融分册，不能以匿名放行替代资金证据。
- 其余均需有效JWT。没有任何已配置的JWT模式时，其余直接denyAll。

验签后，私有接口还检查profile未删除，且不在有效封禁期。管理员权限查询数据库 `t_profiles.is_admin`；认证角色查询buyer/carrier/enterprise的approved状态；订单/地址/聊天等继续执行对象归属校验。管理员访问 `/me/orders/{id}` 也只能读本人订单；需要管理视图时用相应管理员路由。

源码：[TrustedActor](../../server/src/main/java/com/kuaixietech/security/TrustedActor.java)、[ActiveProfileFilter](../../server/src/main/java/com/kuaixietech/security/ActiveProfileFilter.java)、[DomainAccessGuard](../../server/src/main/java/com/kuaixietech/security/DomainAccessGuard.java)、[本地JWT](../../server/src/main/java/com/kuaixietech/security/LocalHmacJwtService.java)、[AuthLoginController](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java)、[AuthLoginService](../../server/src/main/java/com/kuaixietech/service/account/impl/AuthLoginService.java)。

注册/找回验证码应先调用captcha，再带 `captchaId/captchaCode` 申请验证码。`send-code` 返回 `status=OK,retryAfter=60` 只代表应用接受入队，并对部分不存在账号保持相同结果；没有把验证码返回客户端，不代表邮件或短信已送达。`/auth/send-code` 当前还受LIVE模式gate约束，SANDBOX不能用真实验证码外发冒充测试。注册和重置分别消费purpose为signup/reset的OTP，不可互换。

## 成功与失败响应

目前没有全局统一的 `{code,data,message}` 封装。联调必须按每个接口真实声明解析：

| 实现形式 | 返回内容与处理 |
| --- | --- |
| DTO、Map、List | 直接对象或数组，属性名可能为DTO的camelCase，也可能为显式SQL投影的snake_case；不要全局改名。 |
| `String` RPC | 方法直接返回 `OK`、`OK:<id/amount>` 或 `ERR:<原因>` 原文；不是固定 `{success:...}` 对象。实际Content-Type及字符串编码应按HTTP回执处理，不能仅依Java类型假设所有RPC都为JSON对象。 |
| `boolean` RPC | 直接true/false；false表示业务未完成，不可显示成功。 |
| Map RPC失败 | 可能为 `{"err":"NOT_PARTY"}`，HTTP仍为200。成功字段与失败err分支不同。 |
| void/ResponseEntity | 可能为空body；状态码以对应controller为准，不能对所有成功强制JSON.parse。 |
| 文件 | 成功为二进制；失败为HTTP错误，不能把错误JSON当下载文件。 |

[SDK request()](../../client/packages/sdk/src/client.ts) 将RPC在HTTP200下返回的 `ERR:` 前缀String、`false`、或truthy `err` 对象统一转为 `API_BUSINESS_REJECTED`。fetch transport将401变为 `API_TOKEN_REJECTED`，403为 `API_PERMISSION_DENIED`，其余HTTP错误为 `API_HTTP_<status>`；网络失败和超时分别是 `API_NETWORK_ERROR/API_TIMEOUT`。移动uni transport的HTTP错误code文字与fetch分支不同，调用者应同时检查 `ApiError.status`，不要只比较展示文本。

错误响应也未全部统一：

- ProductCheckout、支付、保险、退款、店铺、纠纷、配置中心、上传等指定handler返回ProblemDetail，通常有 `type,title,status,detail`，框架可补instance。业务错误码一般在detail。
- AuthLoginController对send-code/register/reset-password/recovery-help包裹的ResponseStatusException返回 `{"message":"AUTH_..."}`；login/wechat不经过同一run()包装。
- 其余ResponseStatusException或未处理异常可由Spring默认错误处理返回 `timestamp,status,error,path` 等字段。某些RPC的IllegalArgumentException/IllegalStateException没有通用转换，不能承诺全部输入错误都是400或都有detail。
- 401/403可能由安全过滤器提前产生，没有controller业务响应。客户端应先按HTTP状态处理权限失败，再兼容detail/message/err与文本。

常见状态：401未登录/令牌无效；403权限或账户不可用；404无权视为不存在的对象；409业务状态/报价冲突；429验证码请求过频；503缺少可用供应商/配置/存储。并非每个RPC都使用这些状态：大量旧兼容RPC仍以HTTP200返回业务失败。

示例ProblemDetail与RPC失败（代表格式，不是特定订单的真实回执）：

```json
{"type":"about:blank","title":"Service Unavailable","status":503,"detail":"PAYMENT_OR_FX_UNAVAILABLE"}
```

```json
{"err":"NOT_PARTY"}
```

源码：[PaymentErrors](../../server/src/main/java/com/kuaixietech/payments/PaymentErrors.java)、[InsuranceErrors](../../server/src/main/java/com/kuaixietech/insurance/InsuranceErrors.java)、[InsuranceTradeErrors](../../server/src/main/java/com/kuaixietech/insurance/InsuranceTradeErrors.java)、[CaseErrors](../../server/src/main/java/com/kuaixietech/cases/CaseErrors.java)。完整局部handler须结合主HTTP目录定位controller。

## 分页及列表

没有统一Page对象、页号或total字段。当前大部分列表直接返回数组；有limit不一定有offset，空数组也可能来自权限过滤。金额、UUID、分类ID按相应DTO与SQL投影类型保留，不能因都是Map就转换为一套固定schema。

| 入口 | 当前行为 |
| --- | --- |
| 本人订单/求购/行程/钱包分录/通知 | limit默认20、offset默认0；limit为1..100，offset为0..1,000,000。订单role为all/buyer/carrier。 |
| 地址/购物车/收藏 | limit默认20、offset默认0；limit为1..100、offset非负。 |
| 公共商品/行程/分类 | 单次limit为1..100，默认值看各路由；这些入口未统一提供offset。 |
| 公共分类选项 | limit默认100、offset默认0，search可选且最长100；limit为1..100、offset非负。 |
| `POST /rpc/admin_console_list` | body `kind,limit,keyword,offset`；limit缺省80并夹在1..200；offset缺省0，负数拒绝；返回数组，不带total。keyword当前仅users分支消费。 |
| 违规/佣金RPC | `admin_list_violations`、`admin_commission_rows`只有limit，无offset，不可当已支持全量翻页。 |

admin_console_list的25个合法kind：`carrierKyc,buyerKyc,enterpriseKyc,trips,proofRecipes,products,categories,errorLogs,orders,cancels,disputes,invoices,privacyPool,pools,demands,notifyQueue,withdrawals,community,users,finance,invite,broadcast,channelConfigs,locales,adminLogs`。

示例（Authorization为当前真实管理员令牌）：

```http
POST /api/rpc/admin_console_list
Content-Type: application/json

{"kind":"orders","limit":80,"offset":0}
```

源码：[AdminConsoleService](../../server/src/main/java/com/kuaixietech/service/operations/impl/AdminConsoleService.java)、[AccountCentreService](../../server/src/main/java/com/kuaixietech/service/account/impl/AccountCentreService.java)、[SavedCommerceService](../../server/src/main/java/com/kuaixietech/service/account/impl/SavedCommerceService.java)。

## RPC调用规则、明确响应字段与未完成边界

`api.rpc('key', body)` 调POST `/rpc/key`。batch12必须使用 `batch12/<key>`；身份设置使用 `identity/<key>`。没有任意函数名分发或缺失函数自动fallback；不存在的key不得照旧SQL函数名调用。多数record成员没有@NotNull/@Valid，不能只因是record成员就认定全部必填；业务条件以对应service校验为准，primitive boolean需客户端显式传入以避免缺省false。

以下对象成功字段经明确service返回值核对；失败仍可返回err对象。泛型Map的嵌套数据未在此编造固定字段。

| RPC | 成功对象字段 |
| --- | --- |
| `quote_pk_award` | `goodsAmount,serviceFee,insurancePremium,buyerEscrow,vendorDeposit,currency,pricingVersion` |
| `issue_face_challenge` | `challengeId,code`；这是留证挑战口令，不是登录OTP或供应商核验结果 |
| `get_order_evidence` | `order,trip,faces`；管理员读取完整留证链 |
| `open_peer_thread` | `thread_id,peer_id,peer_nickname,reused` |
| `get_peer_profile` | `id,nickname,avatar_url,city,performance_score,star_avg,star_count,deal_count,carrier_verify_status,buyer_verify_status,carrier_tier,invite_tier,user_role,created_at,phone_masked` |
| `log_privacy_call` | `ok,log_id` |
| `batch12/start_order_call` | `call_id,room_id,peer` |
| `batch12/accept_order_call` | `ok,room_id` |
| `batch12/hangup_order_call` | `ok,duration_sec` |
| `batch12/get_or_bind_privacy_number` | `binding_id,virtual_number,expire_at,reused,mode`，mode为local_pool |
| `batch12/screen_chat_content` | `decision,codes`，decision为pass/block |

必须保留的业务边界：

- `identity/*` 三条RPC依赖IdentityProviderClient，与当前本地登录/BCrypt不是同一实现链。内部 `resetContactPasswordAfterVerifiedCode` 没有HTTP路由，禁止对外伪造入口。
- `submit_*_kyc`、资料上传、face challenge、record face verification是应用留证/人工审核流程，不是保险、银行或持牌实名服务商验证结论。
- `batch12/request_verification_code` 返回OK只是入outbox；联系方式health显示的供应商受理也不等于手机/邮箱终端送达。
- RTC只有应用信令；隐私号只有本地号码池，没有真实媒体或运营商绑定回执。客户端回报connected的日志不能被当成供应商通话证明。
- 新报价使用人民币记账，服务费5%、带货人保证金20%。新外部保险版本的 `insurancePremium=0`，平台不收保费；选择保险要真实机构报价与直接收费。当前无生产保险Adapter，不能仅填key完成投保。
- `admin_adjust_balance`、发布保证金冻结/解冻、协带分润是内部钱包操作，不是外部充值、银行打款或原渠道退款。资金动作必须同时检查业务结果与台账回执。
- `admin_issue_invoice` 的approve=true当前明确返回 `ERR:INVOICE_ISSUER_UNAVAILABLE`；支持申请/驳回不能标为税务开票完成。
- 后台取证模板可CRUD，但当前采购取证没有模板consumer；旧渠道开关、47项通用目录、SDK工厂不代表已接齐47项业务。

RPC_INDEX中的字面失败标记只采集controller及直接service方法体，未展开所有helper、SQL约束、外部调用异常，因此不是穷尽错误码契约。它也不把旧cloud transport、数据库函数或未暴露service方法算作已实现HTTP接口。

复核命令（仅读源码，重建本组RPC索引）：

```bash
python3 server/tools/generate_rpc_reference.py
```
