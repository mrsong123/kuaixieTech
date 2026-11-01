# 支付、退款、配置与健康诊断 JSON 示例

以下全部为契约示例，未调用供应商。替换实际返回的商户ID、报价ID、支付ID、修订号、时间和交易数据；只有已开通的真实商户、有效凭据、可用供应商、LIVE门禁和真实资金证据齐备时，相应资金调用才可能成功。JSON中的null可能按当前序列化设置被省略。

## 1. 充值报价与创建支付

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

## 2. 原充值全额退款

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

## 3. 配置保存、修订与本地验证

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

## 4. 联系方式投递健康检查

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

若缺表/连接失败，会返回安全诊断码、SQLState/数字错误码与允许的表名；不返回SQL文本、收件人、验证码、原异常消息或密钥。出现blocked需人工与供应商对账，不自动重放。源字段定义：[ContactDeliveryHealthController](../../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryHealthController.java)、[Diagnostic](../../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryDiagnostic.java)。
