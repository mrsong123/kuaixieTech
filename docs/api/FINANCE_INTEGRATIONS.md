# 支付、保险与配置接口联调说明

本页依据当前 Java controller、DTO 与服务代码编写，日期2026-11-01。路径使用服务器原始 `/api` 前缀，部署域名由调用方环境配置。`/api/me/**` 需要有效用户 Bearer 令牌并接受业务归属校验，`/api/portal/admin/**` 需要管理员身份。金额以十进制字符串传输，客户端不要用二进制浮点计算扣款金额。完整路由和 DTO 自动目录见 [API_REFERENCE.md](../API_REFERENCE.md)。

本地隔离运行已验证商品报价、钱包托管确认和幂等；没有真实商户、银行、汇率商业服务或保险公司验收。本页的“已有接口”表示代码入口存在，不能推导填入任意 key 后业务即可使用。

## 支付与汇率

来源：[PaymentsController.java](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java)、[PaymentModels.java](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java)、[PaymentService.java](../../server/src/main/java/com/kuaixietech/payments/PaymentService.java)。

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

### 当前支付适配能力与验签

| 适配器ID | 已编码终端/币种 | 证据与限制 |
| --- | --- | --- |
| `wechat_native_v3` | `web`，method `wechat`，CNY | 微信APIv3 Native；[WechatNativeAdapter.java](../../server/src/main/java/com/kuaixietech/payments/WechatNativeAdapter.java)。 |
| `wechat_jsapi_v3` | `mp_weixin`，`wechat`，CNY | 需要本人已绑定且来源一致的openid；返回小程序SDK参数；[WechatJsapiAdapter.java](../../server/src/main/java/com/kuaixietech/payments/WechatJsapiAdapter.java)。 |
| `wechat_app_v3` | `app`，`wechat`，CNY | 返回App支付SDK参数；客户端原生支付及商户开通须单独验收；[WechatAppAdapter.java](../../server/src/main/java/com/kuaixietech/payments/WechatAppAdapter.java)。 |
| `alipay_rsa2` | `web,h5,app`，`alipay`，CNY | RSA2官方支付/查询协议；App需客户端接支付宝SDK；[AlipayPageAdapter.java](../../server/src/main/java/com/kuaixietech/payments/AlipayPageAdapter.java)。 |
| `hosted_bank_v1` | `web,h5,app,mp_weixin`，`bank_card/credit_card`；币种由已验证网关能力返回 | 项目自定义HMAC托管桥接协议；**不是银联/所有银行/所有信用卡现成直连**。必须先有实际收单服务实现该桥接并验收合同能力；[HostedBankAdapter.java](../../server/src/main/java/com/kuaixietech/payments/HostedBankAdapter.java)。 |

回调入口当前**不解析或直接验签传入的回调body**，只把路径ID作为查询触发信号；任何回调body中的“成功”都不能入账。服务端使用历史商户快照向供应商独立查单，验证响应签名和订单标识/金额/币种后再入账。前端不得伪造回调完成充值。

- 微信查单/下单响应校验 `Wechatpay-Serial/Timestamp/Nonce/Signature`，平台序列号必须匹配，时间偏差不超过300秒，RSA签名覆盖原始响应内容。
- 支付宝查单响应校验平台RSA签名，验证签名时保留响应子对象的原始JSON文本，随后检查业务成功码与金额/交易标识。
- 银行卡桥接调用 `/capabilities,/payments,/query`，使用HMAC签名信封，校验商户、nonce、签发/过期时间及返回币种能力；未签约网关无能力返回时不能收款。
- 已支付尝试的重复通知只返回对应成功响应；实际入账还有收据唯一性和资金事务校验。当前没有独立的“接收、解密并验证微信通知事件”实现，不应在文档中虚构为已有能力。

来源：[PaymentTransactions.java](../../server/src/main/java/com/kuaixietech/payments/PaymentTransactions.java)、[PaymentSecrets.java](../../server/src/main/java/com/kuaixietech/payments/PaymentSecrets.java)。

## 商品确认与托管

来源：[ProductCheckoutController.java](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutController.java)、[ProductCheckoutModels.java](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutModels.java)。

| 方法与路径 | 请求 | 响应 |
| --- | --- | --- |
| GET `/api/public/products/{id}/trips` | path商品ID；query `limit=100,offset=0` | 该商品所有者可承接的行程数组，仍受商品/行程可用性规则限制。 |
| POST `/api/me/product-orders/quote` | `ProductCheckoutModels.Input` | `productId,tripId,storeId,itemName,itemSpec,unitPrice,quantity,goodsAmount,serviceFee,insurancePremium,buyerEscrow,vendorDeposit,currency,pricingVersion,intentHours,productFingerprint`。 |
| POST `/api/me/product-orders` | 同一Input，加确认快照和幂等键 | `{id,status}`；真实钱包托管、库存占用与订单创建在服务端进行。 |

两个商品报价/确认入口均要求已实名买家。Input必填 `productId,tripId`（UUID）、`quantity=1..100,contactName`（最长64）、`contactPhone`（可带+、数字、空格/连字符）、`address`（最长512）、`insuranceChoice=yes/waived`。可选 `expectDate`（日期）、`note`（最长1024）、`insuranceQuoteId`（UUID）。确认时还需提交 `requestId`（UUID）、`expectedUnitPrice`、`expectedBuyerEscrow`、`expectedProductFingerprint`（64位十六进制）；金额快照使用服务端上一笔报价值，不自行重算。

当前新订单 `pricingVersion=escrow_v2_external_insurance`：服务费货值5%，带货人保证金货值20%；平台商品托管不收保险费，`insurancePremium=0.00`。选择保险须使用保险公司自己的报价及直付链路，商品订单确认并不代表已投保、已交保费或保险已生效。旧 `escrow_v1` 的1%、最低5元历史逻辑不能作为新单报价规则。来源：[IntentPricing.java](../../server/src/main/java/com/kuaixietech/service/trading/support/IntentPricing.java)。

## 充值原路退款与售后钱包退回

这两个流程用途、资金源与最终凭据不同，调用方需按接口语义展示。

### 充值原路退款

来源：[PaymentRefundController.java](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java)、[RefundModels.java](../../server/src/main/java/com/kuaixietech/refunds/RefundModels.java)。

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

仅具有可匹配真实收据的CNY充值可申请，且当前只对微信三个适配器及支付宝开放；实现为**原充值全额退款**，不是任意部分退款、多币种退款或银行卡桥接退款。按原支付商户快照查询/派发；供应商不明结果保留处理中，不因HTTP请求已受理就标记完成。支付宝退款操作回执仍须独立查询原退款请求号，微信/支付宝最终收据均要核对支付ID、交易号、退款ID、币种及金额。来源：[OriginalPaymentRefundGateway.java](../../server/src/main/java/com/kuaixietech/payments/OriginalPaymentRefundGateway.java)。

### 售后托管货款退回平台钱包

来源：[AftersalesEscrowController.java](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowController.java)、[AftersalesEscrowFunds.java](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowFunds.java)。

| 方法与路径 | 请求与响应 |
| --- | --- |
| GET `/api/portal/admin/after-sales/{id}/escrow-budget` | 管理员；售后争议ID。返回预算或 `status=blocked,reasonCode`，不是扣款接口。 |
| POST `/api/portal/admin/after-sales/{id}/escrow-refund` | 管理员；`{requestId:UUID,expectedRound:int>=1,note:string}`，note必填最长200。要求争议轮次匹配、判定及资金证据均通过。 |
| GET `/api/me/after-sales/escrow-refunds` | 本人记录，query `limit=20,offset=0`。 |
| GET `/api/me/after-sales/escrow-refunds/{id}/events` | 本人执行单事件，query `limit=50,offset=0`。 |
| GET `/api/portal/admin/after-sales/escrow-refunds/{id}/events` | 管理员执行单事件，同分页。 |

预算通用字段 `caseId,orderId,currency,refundMethod,destination,status`；可执行时还有 `goodsAmount,alreadyRefunded,refundableGoods,requestedAmount,buyerEscrowHeld,serviceFeeHeld,shippingHeld,requiresAccounting,sourceRound,rewardReviewRequired`。已执行/执行结果为 `id,caseId,orderId,requestId,sourceRound,amount,currency,status,refundMethod,destination,requiresAccounting,rewardReviewRequired,createdAt`。

该接口固定 `currency=CNY,refundMethod=escrow_wallet,destination=platform_wallet`。资金退回本人平台钱包，并不发起微信/银行卡原路退款；需核实真实托管余额、商品预约/库存及既有退款上限，不能由后台填写任意金额。幂等键绑定操作者、争议及轮次/备注；已有其他执行或相同key换内容返回冲突。后续完整账务、提成与纠纷全状态仍按业务清单验收。

## 钱包、调账、发票与未开放提现

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

来源：[AccountCentreController.java](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java)、[AccountCentreMapper.xml](../../server/src/main/resources/mapper/account/AccountCentreMapper.xml)、[AdminFinanceRpcController.java](../../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java)、[Batch10RpcController.java](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java)、[InvoiceRpcService.java](../../server/src/main/java/com/kuaixietech/service/finance/impl/InvoiceRpcService.java)、[WithdrawMigrationServiceImpl.java](../../server/src/main/java/com/kuaixietech/service/finance/impl/WithdrawMigrationServiceImpl.java)。

## 商品保险

来源：[InsuranceController.java](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java)、[InsuranceModels.java](../../server/src/main/java/com/kuaixietech/insurance/InsuranceModels.java)、[InsuranceCatalog.java](../../server/src/main/java/com/kuaixietech/insurance/InsuranceCatalog.java)。

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

## 配置中心与密钥

来源：[CouplerController.java](../../server/src/main/java/com/kuaixietech/coupler/CouplerController.java)、[CouplerModels.java](../../server/src/main/java/com/kuaixietech/coupler/CouplerModels.java)、[CouplerService.java](../../server/src/main/java/com/kuaixietech/coupler/CouplerService.java)。以下均管理员权限。

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

### SDK工具没有通用远程调用接口

`ClientFactories`、`PublicServiceAdapters`、`WechatMiniProtocol`、RTC签名及显式模式支付协议属于服务端SDK/协议工具。当前没有可供前端指定任意provider、操作名、URL和凭据后透传调用的SDK代理controller。应由经过授权的具体业务服务获取版本化配置后调用，不能为联调编造 `/sdk/invoke`、`/sdk/dispatch` 等路由。

47个目录项不能整体标为已实现：typed客户端构造、本地validate、协议工具、业务消费者、真实供应商验收应分别看待。当前仅部分短信、DirectMail、小程序登录、OSS/COS等有消费接线，其余路线仍须实际业务实现；保险零Adapter、银行卡自定义桥接等缺口见本页对应部分。来源：[ClientFactories.java](../../server/src/main/java/com/kuaixietech/integrationsdk/ClientFactories.java)、[PublicServiceAdapters.java](../../server/src/main/java/com/kuaixietech/integrationsdk/PublicServiceAdapters.java)、[CouplerAdapterRegistry.java](../../server/src/main/java/com/kuaixietech/coupler/CouplerAdapterRegistry.java)。

## 联系方式投递健康检查

GET `/api/portal/admin/contact-delivery/health` 为管理员只读诊断，**不会领取验证码或发信**。响应：

- `checkedAt,configuration,lastRun,dispatchConfigured,providerDeliveryVerified=false,sentMeaning,blockedPolicy,queue,state`；读取队列失败还带 `queueDiagnostic`。
- `configuration`包含sms/email诊断；`lastRun`含 `checkedAt,state,sms,email,nativeOwnership,legacy`。
- 单个诊断字段 `channel,state,stage,reasonCode,exceptionType,sqlState,databaseError,missingTable,observedAt`，不含验证码、收件人、密钥、SQL文本或原始异常消息。
- queue每项为 `channel,status,total`。总状态可能为 `idle,paused,ready,degraded,requires_reconciliation`；缺表可在诊断中得到 `migration_required` 与安全表名。
- `sentMeaning=provider_accepted_not_recipient_delivery`：投递记录sent只能说明供应商受理，不代表用户手机/邮箱已收到。`dispatchConfigured=true`也不是送达验收。
- `blockedPolicy=never_automatically_replay; reconcile_with_provider_before_any_manual_action`：结果不明/blocked记录先与供应商对账，不自动重放。当前没有“点击诊断立即群发/自动补发”接口。

来源：[ContactDeliveryHealthController.java](../../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryHealthController.java)、[ContactDeliveryDiagnostic.java](../../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryDiagnostic.java)、[ContactCodeDeliveryScheduler.java](../../server/src/main/java/com/kuaixietech/scheduler/ContactCodeDeliveryScheduler.java)。

## 联调错误处理

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
