# 可配置支付与多币种付款

已采用：用户按渠道实际能力选择付款币种，钱包、订单、托管及记账结算使用 CNY。不存在外币钱包余额转换。当前实现外部充值→CNY 钱包→订单托管；真实渠道尚未联调，不能据此宣称支付已上线。

## 已实现

- ISO 4217 币种目录不限定为人民币、美元、日元和韩元；用户可选范围取商户配置和渠道实际能力的交集。
- 后端生成有有效期的不可变报价，保存来源、汇率、加价、币种精度和最小单位金额。接口金额使用十进制字符串，后端使用整数和 BigDecimal；JPY 零位、CNY 两位、部分币种三位分别处理。
- 用户确认报价后按请求 UUID 幂等创建支付。报价过期、商户配置改变要求重新报价；不静默变更金额或自动换渠道。
- 创建支付和客户端支付完成提示均不入账。主动查询渠道并验证渠道响应后，匹配商户、支付引用、交易号、币种和金额，在同一事务内保存唯一回执、修改支付状态和记入 CNY 钱包。重复查询/回调不会重复入账。
- 回调仅触发主动查询，不信任回调正文；无法验证时返回失败以便渠道重试。请求发出后遇到网络不确定性保留原支付引用，不重新发起扣款。
- PC、H5、小程序、App 共用报价和状态接口；后台管理商户、币种和汇率来源。商户保存凭据组前缀，API 配置中心可加密录入密钥且不回显，不接收完整卡号和 CVV。

## 渠道适配与边界

| 适配器 | 用途 | 当前限制 |
| --- | --- | --- |
| wechat_native_v3 | PC/H5 微信二维码 | CNY；须验证商户产品及使用场景 |
| wechat_jsapi_v3 | 微信小程序支付 | CNY；从当前用户资料读取 openid，须与 APP_ID 匹配 |
| wechat_app_v3 | App 微信支付 | CNY；须配置原生 SDK 和商户绑定 |
| alipay_rsa2 | 支付宝网页、H5、App | CNY；RSA2；须验证商户产品和 App 配置 |
| hosted_bank_v1 | 银行卡、信用卡托管收银台 | 需选定收单方并实现下述签名桥接协议；不等于直接接通所有银行 |

国内各银行和国际信用卡的实际覆盖、卡组织、地区、3DS、可收款币种及 CNY 清算能力由收单合同决定。小程序 web-view 的业务域名和平台政策、App 支付 SDK 与签名安装包需另外验收。当前未实现外部退款、银行卡出款、渠道账单清算对账；钱包托管释放不是外部退款。

## 迁移与启用

`001-missing-business-tables.proposed.sql` 为原有功能补表，`002-configurable-payments.proposed.sql` 新增 5 张支付表。均未执行。先核对真实业务库，再逐项审阅迁移；不要导入 reference 整库覆盖业务数据。新商户和汇率来源默认禁用，不预置假汇率、假交易或测试商户。

运行环境须配置 DB_URL、DB_USER、DB_PASSWORD、APP_JWT_SECRET。商户 credentialPrefix 例如 WX_MAIN；实际密钥可在 API 配置中心同名凭据组加密保存（包括 PEM），或通过原环境设置和引用文件提供：

- 微信：`<前缀>_MERCHANT_ID`、`_APP_ID`、`_MERCHANT_SERIAL`、`_PRIVATE_KEY_FILE`、`_PLATFORM_SERIAL`、`_PLATFORM_PUBLIC_KEY_FILE`。私钥为 PKCS8，平台公钥支持 PEM 公钥或证书；需维护证书有效期及轮换。
- 支付宝：`<前缀>_APP_ID`、`_PRIVATE_KEY_FILE`、`_PLATFORM_PUBLIC_KEY_FILE`。
- 银行桥接：`<前缀>_MERCHANT_ID`、`_SIGNING_SECRET`（至少 32 字符）。
- 自定义汇率服务可配置 `<前缀>_BEARER_TOKEN`。

商户 settings 支持 notifyUrl、returnUrl、gatewayUrl、checkoutHosts；按适配器填写。notifyUrl 为公开 HTTPS 回调基址，例如 https://api.example.com/api/payment-callback，系统追加商户 ID 和支付引用。支付宝需 returnUrl；银行收银台域名必须在 checkoutHosts 中。

自定义服务主机须加入 PAYMENT_GATEWAY_ALLOWED_HOSTS，且云环境出站网络策略也须放行对应域名。原生接口使用 api.mch.weixin.qq.com、openapi.alipay.com；公开参考汇率服务使用 open.er-api.com。不关闭 TLS 校验，不使用通配出站授权代替具体服务配置。

定时查单配置 `app.payments.reconcile-enabled=true` 后才启用；默认关闭，每批最多 100 笔并记录上次查询时间。此任务是支付状态核实，不是银行清算对账。待核实支付不会被自动当成失败或成功。

## 汇率

支持 open_er_api 参考源或自定义 rates_json_v1 HTTPS 服务。自定义 JSON 包含 base=CNY、rates 币种映射、time_last_update_unix、time_next_update_unix（秒）。配置 maxAgeSeconds 控制最大数据年龄；过期、未来时间、无效或缺少汇率时拒绝报价，无固定汇率兜底。

查询参考汇率和交易报价分开；来源默认不允许交易，需明确开启 transactionEnabled 并核实服务条款和报价风险。加价 spreadBps 在界面展示，目标最小单位 HALF_UP 舍入；CNY→CNY 不加汇兑费用。报价有效期同时受来源有效期和商户 TTL 限制。渠道实际手续费和清算差额仍需另行对账，不能把参考汇率当成收单机构保证的结汇价格。

## 银行桥接协议 hosted_bank_v1

这是给选定收单方编写适配服务的明确协议，现有银行 API 通常不能直接填写为 gatewayUrl。

所有调用 POST JSON `{payload,signature}`。payload 为 UTF-8 JSON 的无填充 base64url，signature 为 HMAC-SHA256(secret, payload 字符串) 的标准 base64。请求包含 merchant、requestedAt（Unix 秒）、nonce；响应签名必须有效，并回显 merchant、nonce，包含 issuedAt、expiresAt（Unix 秒）。响应 issuedAt 最大过去 300 秒、未来 30 秒，expiresAt 不得过期。

| 路径 | 请求业务字段 | 已验签响应业务字段 |
| --- | --- | --- |
| /capabilities | 无 | settlementCurrencies（必须含 CNY）、methods、platforms、currencies 数组 |
| /payments | reference、currency、amountMinor（整数字符串）、method、platform、settlementCurrency=CNY、baseCnyMinor、expiresAt、notifyUrl | reference、checkoutUrl（HTTPS 且在 checkoutHosts） |
| /query | reference | reference、transactionId、currency、amountMinor、status=paid 或未完成状态、settlementCurrency=CNY |

桥接服务必须以 reference 幂等创建真实渠道单，保留原报价并处理渠道认证、3DS 和清算。真实收单方不支持该币种或 CNY 清算时不得伪造 capabilities。

## 验证与待完成条件

当前应用 89 项后端测试通过，覆盖金额精度、汇率时效、报价锁定、签名篡改、重复回执、错误币种与金额拒绝入账；五端资源构建和移动端完整类型检查通过。静态结构核验为原始 82 表加 15 张提议新增表；不证明真实数据库 SQL 和并发行为。

尚无实际业务库连接及支付商户凭据，未执行迁移、真实充值、真实汇率交易或渠道回调联调。还需收单机构接口及合同能力、生产网络域名、原生支付配置和实际清算验证。默认 Maven 历史测试和前端旧视觉检查仍有既有失败，详见 MIGRATION.md。

统一 API 配置及密钥根密钥部署见 [API_CONFIGURATION.md](API_CONFIGURATION.md)。旧环境文件方式继续兼容；已有数据库凭据组停用时不回退到环境旧值。

本轮补齐CNY微信/支付宝原充值全额退款，独立于商品售后；审核、派发与回执查询分离，详见 [ORIGINAL_PAYMENT_REFUNDS.md](ORIGINAL_PAYMENT_REFUNDS.md)。银行卡、跨币种退款、提现和清算对账仍未实现。
