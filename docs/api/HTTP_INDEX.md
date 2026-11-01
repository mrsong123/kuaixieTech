# 全量 HTTP 声明与参数

由 `python3 server/tools/generate_api_reference.py` 生成；232个真实控制器路由、129个请求/响应record或enum。JSON含全部字段、注解、默认值与定位。

只枚举已声明入口；不代表全业务或供应商验收。权限提示是路由提示，最终还要经过Security与service guard。泛型Map/Object响应不虚构固定字段；若存在SDK类型，列出其声明并链接[SDK字段附录](SDK_TYPES.md)，仍以服务端返回为准。[Java record字段附录](JAVA_TYPES.md)。

## AuthLoginController

### `GET /api/auth/captcha`

[AuthLoginController.java:36](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L36)；public。

网关兼容路径：`/auth/captcha`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;image:string}`；[client.ts:92](../../client/packages/sdk/src/client.ts#L92)。

### `POST /api/auth/login`

[AuthLoginController.java:28](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L28)；public。

网关兼容路径：`/auth/login`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | LoginBody → [com.kuaixietech.controller.AuthLoginController.LoginBody](JAVA_TYPES.md#com-kuaixietech-controller-authlogincontroller-loginbody): channel:String, contact:String, password:String, code:String, countryCode:String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').AuthTokenResult`；[client.ts:90](../../client/packages/sdk/src/client.ts#L90)。

### `POST /api/auth/recovery-help`

[AuthLoginController.java:50](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L50)；public。

网关兼容路径：`/auth/recovery-help`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | HelpBody → [com.kuaixietech.controller.AuthLoginController.HelpBody](JAVA_TYPES.md#com-kuaixietech-controller-authlogincontroller-helpbody): channel:String, contact:String, countryCode:String, captchaId:String, captchaCode:String | True | — |

返回 Java 类型：`ResponseEntity<Map<String,Object>>`。
SDK调用方声明：`{status:string}`；[client.ts:96](../../client/packages/sdk/src/client.ts#L96)。

### `POST /api/auth/register`

[AuthLoginController.java:42](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L42)；public。

网关兼容路径：`/auth/register`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | RegisterBody → [com.kuaixietech.controller.AuthLoginController.RegisterBody](JAVA_TYPES.md#com-kuaixietech-controller-authlogincontroller-registerbody): channel:String, contact:String, countryCode:String, password:String, code:String, role:String, inviteCode:String, enterpriseName:String, businessLicenseNo:String | True | — |

返回 Java 类型：`ResponseEntity<Map<String,Object>>`。
SDK调用方声明：`import('./types').AuthTokenResult`；[client.ts:93](../../client/packages/sdk/src/client.ts#L93)。

### `POST /api/auth/reset-password`

[AuthLoginController.java:46](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L46)；public。

网关兼容路径：`/auth/reset-password`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | ResetBody → [com.kuaixietech.controller.AuthLoginController.ResetBody](JAVA_TYPES.md#com-kuaixietech-controller-authlogincontroller-resetbody): channel:String, contact:String, countryCode:String, code:String, newPassword:String | True | — |

返回 Java 类型：`ResponseEntity<Map<String,Object>>`。
SDK调用方声明：`import('./types').AuthTokenResult`；[client.ts:95](../../client/packages/sdk/src/client.ts#L95)。

### `POST /api/auth/send-code`

[AuthLoginController.java:38](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L38)；public。

网关兼容路径：`/auth/send-code`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | SendCodeBody → [com.kuaixietech.controller.AuthLoginController.SendCodeBody](JAVA_TYPES.md#com-kuaixietech-controller-authlogincontroller-sendcodebody): channel:String, contact:String, countryCode:String, purpose:String, captchaId:String, captchaCode:String | True | — |

返回 Java 类型：`ResponseEntity<Map<String,Object>>`。
SDK调用方声明：`{status:string;retryAfter?:number}`；[client.ts:94](../../client/packages/sdk/src/client.ts#L94)。

### `POST /api/auth/wechat`

[AuthLoginController.java:32](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L32)；public。

网关兼容路径：`/auth/wechat`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | WechatBody → [com.kuaixietech.controller.AuthLoginController.WechatBody](JAVA_TYPES.md#com-kuaixietech-controller-authlogincontroller-wechatbody): code:String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').AuthTokenResult`；[client.ts:91](../../client/packages/sdk/src/client.ts#L91)。

## SavedCommerceController

### `GET /api/me/addresses`

[SavedCommerceController.java:11](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L11)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/addresses`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').SavedAddress[]`；[client.ts:200](../../client/packages/sdk/src/client.ts#L200)。

### `POST /api/me/addresses`

[SavedCommerceController.java:12](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L12)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/addresses`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | SavedCommerceService.AddressInput → [com.kuaixietech.service.account.impl.SavedCommerceService.AddressInput](JAVA_TYPES.md#com-kuaixietech-service-account-impl-savedcommerceservice-addressinput): contactName:String, contactPhone:String, province:String, city:String, district:String, detail:String, isDefault:boolean | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string}`；[client.ts:201](../../client/packages/sdk/src/client.ts#L201)。

### `DELETE /api/me/addresses/{id}`

[SavedCommerceController.java:14](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/addresses/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:203](../../client/packages/sdk/src/client.ts#L203)。

### `PUT /api/me/addresses/{id}`

[SavedCommerceController.java:13](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/addresses/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | SavedCommerceService.AddressInput → [com.kuaixietech.service.account.impl.SavedCommerceService.AddressInput](JAVA_TYPES.md#com-kuaixietech-service-account-impl-savedcommerceservice-addressinput): contactName:String, contactPhone:String, province:String, city:String, district:String, detail:String, isDefault:boolean | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string}`；[client.ts:202](../../client/packages/sdk/src/client.ts#L202)。

## AftersalesEscrowController

### `GET /api/me/after-sales/escrow-refunds`

[AftersalesEscrowController.java:14](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/after-sales/escrow-refunds`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').EscrowWalletRefund[]`；[client.ts:134](../../client/packages/sdk/src/client.ts#L134)。

### `GET /api/me/after-sales/escrow-refunds/{id}/events`

[AftersalesEscrowController.java:15](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/after-sales/escrow-refunds/{id}/events`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 50 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').EscrowRefundEvent[]`；[client.ts:135](../../client/packages/sdk/src/client.ts#L135)。

## SavedCommerceController

### `GET /api/me/cart`

[SavedCommerceController.java:15](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/cart`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').SavedCartItem[]`；[client.ts:204](../../client/packages/sdk/src/client.ts#L204)。

### `DELETE /api/me/cart/{product}`

[SavedCommerceController.java:17](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L17)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/cart/{product}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | product | String | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:206](../../client/packages/sdk/src/client.ts#L206)。

### `PUT /api/me/cart/{product}`

[SavedCommerceController.java:16](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/cart/{product}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | product | String | True | — |
| body | input | SavedCommerceService.CartInput → [com.kuaixietech.service.account.impl.SavedCommerceService.CartInput](JAVA_TYPES.md#com-kuaixietech-service-account-impl-savedcommerceservice-cartinput): quantity:int | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:205](../../client/packages/sdk/src/client.ts#L205)。

## AccountCentreController

### `GET /api/me/demands`

[AccountCentreController.java:18](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/demands`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').MyDemand[]`；[client.ts:234](../../client/packages/sdk/src/client.ts#L234)。

## TradeCaseController

### `GET /api/me/disputes`

[TradeCaseController.java:13](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').TradeCaseSummary[]`；[client.ts:172](../../client/packages/sdk/src/client.ts#L172)。

### `POST /api/me/disputes`

[TradeCaseController.java:15](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | Create → [com.kuaixietech.cases.CaseModels.Create](JAVA_TYPES.md#com-kuaixietech-cases-casemodels-create): orderId:String, type:String, description:String, expectResolution:String, refundAmount:BigDecimal, evidenceUrls:List< String>, requestId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;stage:string}`；[client.ts:100](../../client/packages/sdk/src/client.ts#L100)。
SDK调用方声明：`{id:string;stage:string}`；[client.ts:175](../../client/packages/sdk/src/client.ts#L175)。

### `GET /api/me/disputes/{id}`

[TradeCaseController.java:14](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').OriginalDispute`；[client.ts:99](../../client/packages/sdk/src/client.ts#L99)。
SDK调用方声明：`import('./types').TradeCaseDetail`；[client.ts:174](../../client/packages/sdk/src/client.ts#L174)。

### `POST /api/me/disputes/{id}/appeal`

[TradeCaseController.java:18](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes/{id}/appeal`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | Appeal → [com.kuaixietech.cases.CaseModels.Appeal](JAVA_TYPES.md#com-kuaixietech-cases-casemodels-appeal): reason:String, expectedRound:int, requestId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;stage:string}`；[client.ts:178](../../client/packages/sdk/src/client.ts#L178)。

### `POST /api/me/disputes/{id}/escalate`

[TradeCaseController.java:17](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L17)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes/{id}/escalate`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | Escalate → [com.kuaixietech.cases.CaseModels.Escalate](JAVA_TYPES.md#com-kuaixietech-cases-casemodels-escalate): reason:String, expectedRound:int, requestId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;stage:string}`；[client.ts:177](../../client/packages/sdk/src/client.ts#L177)。

### `GET /api/me/disputes/{id}/events`

[TradeCaseController.java:20](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L20)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes/{id}/events`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 100 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').TradeCaseEvent[]`；[client.ts:181](../../client/packages/sdk/src/client.ts#L181)。

### `POST /api/me/disputes/{id}/finish`

[TradeCaseController.java:19](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes/{id}/finish`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | Finish → [com.kuaixietech.cases.CaseModels.Finish](JAVA_TYPES.md#com-kuaixietech-cases-casemodels-finish): note:String, expectedRound:int, requestId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;stage:string}`；[client.ts:179](../../client/packages/sdk/src/client.ts#L179)。

### `POST /api/me/disputes/{id}/respond`

[TradeCaseController.java:16](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/disputes/{id}/respond`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | Respond → [com.kuaixietech.cases.CaseModels.Respond](JAVA_TYPES.md#com-kuaixietech-cases-casemodels-respond): note:String, evidenceUrls:List< String>, expectedRound:int, requestId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;stage:string}`；[client.ts:176](../../client/packages/sdk/src/client.ts#L176)。

## SavedCommerceController

### `GET /api/me/favorites`

[SavedCommerceController.java:18](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/favorites`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').SavedFavorite[]`；[client.ts:207](../../client/packages/sdk/src/client.ts#L207)。

### `DELETE /api/me/favorites/{type}/{target}`

[SavedCommerceController.java:20](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L20)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/favorites/{type}/{target}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | type | String | True | — |
| path | target | String | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:209](../../client/packages/sdk/src/client.ts#L209)。

### `PUT /api/me/favorites/{type}/{target}`

[SavedCommerceController.java:19](../../server/src/main/java/com/kuaixietech/controller/SavedCommerceController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/favorites/{type}/{target}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | type | String | True | — |
| path | target | String | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:208](../../client/packages/sdk/src/client.ts#L208)。

## InsuranceController

### `GET /api/me/insurance/applications`

[InsuranceController.java:18](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/applications`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Application>`。
响应字段：[com.kuaixietech.insurance.InsuranceModels.Application](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-application)。
SDK调用方声明：`import('./types').InsuranceApplication[]`；[client.ts:191](../../client/packages/sdk/src/client.ts#L191)。

### `POST /api/me/insurance/applications`

[InsuranceController.java:16](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/applications`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ApplyInput → [com.kuaixietech.insurance.InsuranceModels.ApplyInput](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-applyinput): orderId:String, quoteId:String, requestId:String, termsAccepted:boolean, disclosuresAccepted:boolean | True | — |

返回 Java 类型：`Application`。
响应字段：[com.kuaixietech.insurance.InsuranceModels.Application](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-application)。
SDK调用方声明：`import('./types').InsuranceApplication`；[client.ts:189](../../client/packages/sdk/src/client.ts#L189)。

### `GET /api/me/insurance/applications/{id}`

[InsuranceController.java:21](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L21)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/applications/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Application`。
响应字段：[com.kuaixietech.insurance.InsuranceModels.Application](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-application)。
SDK调用方声明：`import('./types').InsuranceApplication`；[client.ts:192](../../client/packages/sdk/src/client.ts#L192)。

### `GET /api/me/insurance/applications/{id}/record`

[InsuranceController.java:20](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L20)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/applications/{id}/record`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Application`。
响应字段：[com.kuaixietech.insurance.InsuranceModels.Application](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-application)。
SDK调用方声明：`import('./types').InsuranceApplication`；[client.ts:193](../../client/packages/sdk/src/client.ts#L193)。

### `GET /api/me/insurance/consents`

[InsuranceController.java:17](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L17)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/consents`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').BackendRecord[]`；[client.ts:196](../../client/packages/sdk/src/client.ts#L196)。

### `GET /api/me/insurance/orders/{orderId}`

[InsuranceController.java:19](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/orders/{orderId}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | orderId | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`{application:import('./types').InsuranceApplication|null}`；[client.ts:190](../../client/packages/sdk/src/client.ts#L190)。

### `GET /api/me/insurance/pk-subject`

[InsuranceController.java:13](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/pk-subject`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | demandId | String | True | — |
| query | quoteId | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`{subjectId:string;subjectType:'pk_quote';itemName:string;declaredValue:string}`；[client.ts:188](../../client/packages/sdk/src/client.ts#L188)。

### `POST /api/me/insurance/quotes`

[InsuranceController.java:14](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/quotes`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | QuoteInput → [com.kuaixietech.insurance.InsuranceModels.QuoteInput](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-quoteinput): productId:String, subjectType:String, subjectId:String, itemName:String, declaredValue:String, platform:String | True | — |

返回 Java 类型：`Quote`。
响应字段：[com.kuaixietech.insurance.InsuranceModels.Quote](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-quote)。
SDK调用方声明：`import('./types').InsuranceQuote`；[client.ts:186](../../client/packages/sdk/src/client.ts#L186)。

### `GET /api/me/insurance/quotes/{id}`

[InsuranceController.java:15](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/insurance/quotes/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Quote`。
响应字段：[com.kuaixietech.insurance.InsuranceModels.Quote](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-quote)。
SDK调用方声明：`import('./types').InsuranceQuote`；[client.ts:187](../../client/packages/sdk/src/client.ts#L187)。

## IntentOrderController

### `POST /api/me/intents`

[IntentOrderController.java:19](../../server/src/main/java/com/kuaixietech/controller/IntentOrderController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/intents`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | IntentInput → [com.kuaixietech.entity.portal.IntentInput](JAVA_TYPES.md#com-kuaixietech-entity-portal-intentinput): tripId:String, itemName:String, itemSpec:String, quantity:Integer, unit:String, categoryId:String, expectUnitPrice:BigDecimal, note:String, contactName:String, contactPhone:String, address:String, expectDate:LocalDate, insuranceChoice:String, requestId:String, expectedBuyerEscrow:BigDecimal, insuranceQuoteId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:string}`；[client.ts:98](../../client/packages/sdk/src/client.ts#L98)。

### `POST /api/me/intents/quote`

[IntentOrderController.java:18](../../server/src/main/java/com/kuaixietech/controller/IntentOrderController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/intents/quote`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | IntentInput → [com.kuaixietech.entity.portal.IntentInput](JAVA_TYPES.md#com-kuaixietech-entity-portal-intentinput): tripId:String, itemName:String, itemSpec:String, quantity:Integer, unit:String, categoryId:String, expectUnitPrice:BigDecimal, note:String, contactName:String, contactPhone:String, address:String, expectDate:LocalDate, insuranceChoice:String, requestId:String, expectedBuyerEscrow:BigDecimal, insuranceQuoteId:String | True | — |

返回 Java 类型：`IntentPricing.Quote`。
响应字段：[com.kuaixietech.service.trading.support.IntentPricing.Quote](JAVA_TYPES.md#com-kuaixietech-service-trading-support-intentpricing-quote)。
SDK调用方声明：`import('./types').IntentQuote`；[client.ts:97](../../client/packages/sdk/src/client.ts#L97)。

## InviteSummaryController

### `GET /api/me/invite/tier`

[InviteSummaryController.java:14](../../server/src/main/java/com/kuaixietech/controller/InviteSummaryController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/invite/tier`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').OriginalInviteTier`；[client.ts:101](../../client/packages/sdk/src/client.ts#L101)。

## AccountCentreController

### `GET /api/me/notifications`

[AccountCentreController.java:22](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L22)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/notifications`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').MyNotification[]`；[client.ts:238](../../client/packages/sdk/src/client.ts#L238)。

### `POST /api/me/notifications/{id}/read`

[AccountCentreController.java:23](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L23)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/notifications/{id}/read`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{status:string}`；[client.ts:239](../../client/packages/sdk/src/client.ts#L239)。

### `GET /api/me/orders`

[AccountCentreController.java:16](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/orders`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | role | String | False | all |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').MyOrder[]`；[client.ts:232](../../client/packages/sdk/src/client.ts#L232)。

### `GET /api/me/orders/{id}`

[AccountCentreController.java:17](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L17)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/orders/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').MyOrder`；[client.ts:233](../../client/packages/sdk/src/client.ts#L233)。

## PaymentRefundController

### `GET /api/me/payment-refunds`

[PaymentRefundController.java:14](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payment-refunds`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PaymentRefund[]`；[client.ts:123](../../client/packages/sdk/src/client.ts#L123)。

### `POST /api/me/payment-refunds`

[PaymentRefundController.java:15](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payment-refunds`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | Create → [com.kuaixietech.refunds.RefundModels.Create](JAVA_TYPES.md#com-kuaixietech-refunds-refundmodels-create): paymentId:String, requestId:String, reason:String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:124](../../client/packages/sdk/src/client.ts#L124)。

### `GET /api/me/payment-refunds/eligible`

[PaymentRefundController.java:13](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payment-refunds/eligible`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').EligiblePaymentRefund[]`；[client.ts:121](../../client/packages/sdk/src/client.ts#L121)。

### `GET /api/me/payment-refunds/{id}`

[PaymentRefundController.java:16](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payment-refunds/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:125](../../client/packages/sdk/src/client.ts#L125)。

### `POST /api/me/payment-refunds/{id}/cancel`

[PaymentRefundController.java:18](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payment-refunds/{id}/cancel`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:122](../../client/packages/sdk/src/client.ts#L122)。

### `GET /api/me/payment-refunds/{id}/events`

[PaymentRefundController.java:17](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L17)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payment-refunds/{id}/events`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 50 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PaymentRefundEvent[]`；[client.ts:120](../../client/packages/sdk/src/client.ts#L120)。

### `POST /api/me/payment-refunds/{id}/refresh`

[PaymentRefundController.java:19](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payment-refunds/{id}/refresh`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:126](../../client/packages/sdk/src/client.ts#L126)。

## PaymentsController

### `GET /api/me/payments`

[PaymentsController.java:20](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L20)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payments`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Payment>`。
响应字段：[com.kuaixietech.payments.PaymentModels.Payment](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-payment)。
SDK调用方声明：`import('./types').FundingPayment[]`；[client.ts:223](../../client/packages/sdk/src/client.ts#L223)。

### `POST /api/me/payments`

[PaymentsController.java:19](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payments`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ConfirmInput → [com.kuaixietech.payments.PaymentModels.ConfirmInput](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-confirminput): quoteId:String, requestId:String | True | — |

返回 Java 类型：`Payment`。
响应字段：[com.kuaixietech.payments.PaymentModels.Payment](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-payment)。
SDK调用方声明：`import('./types').FundingPayment`；[client.ts:222](../../client/packages/sdk/src/client.ts#L222)。

### `POST /api/me/payments/quotes`

[PaymentsController.java:18](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payments/quotes`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | QuoteInput → [com.kuaixietech.payments.PaymentModels.QuoteInput](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-quoteinput): merchantId:String, paymentCurrency:String, amountCny:String | True | — |

返回 Java 类型：`Quote`。
响应字段：[com.kuaixietech.payments.PaymentModels.Quote](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-quote)。
SDK调用方声明：`import('./types').FundingQuote`；[client.ts:221](../../client/packages/sdk/src/client.ts#L221)。

### `GET /api/me/payments/{id}`

[PaymentsController.java:21](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L21)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/payments/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Payment`。
响应字段：[com.kuaixietech.payments.PaymentModels.Payment](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-payment)。
SDK调用方声明：`import('./types').FundingPayment`；[client.ts:224](../../client/packages/sdk/src/client.ts#L224)。

## ProductCheckoutController

### `POST /api/me/product-orders`

[ProductCheckoutController.java:14](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/product-orders`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ProductCheckoutModels.Input → [com.kuaixietech.commerce.ProductCheckoutModels.Input](JAVA_TYPES.md#com-kuaixietech-commerce-productcheckoutmodels-input): productId:String, tripId:String, quantity:Integer, contactName:String, contactPhone:String, address:String, expectDate:LocalDate, note:String, insuranceChoice:String, insuranceQuoteId:String, requestId:String, expectedUnitPrice:BigDecimal, expectedBuyerEscrow:BigDecimal, expectedProductFingerprint:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:string}`；[client.ts:117](../../client/packages/sdk/src/client.ts#L117)。

### `POST /api/me/product-orders/quote`

[ProductCheckoutController.java:13](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/product-orders/quote`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ProductCheckoutModels.Input → [com.kuaixietech.commerce.ProductCheckoutModels.Input](JAVA_TYPES.md#com-kuaixietech-commerce-productcheckoutmodels-input): productId:String, tripId:String, quantity:Integer, contactName:String, contactPhone:String, address:String, expectDate:LocalDate, note:String, insuranceChoice:String, insuranceQuoteId:String, requestId:String, expectedUnitPrice:BigDecimal, expectedBuyerEscrow:BigDecimal, expectedProductFingerprint:String | True | — |

返回 Java 类型：`ProductCheckoutModels.Quote`。
响应字段：[com.kuaixietech.commerce.ProductCheckoutModels.Quote](JAVA_TYPES.md#com-kuaixietech-commerce-productcheckoutmodels-quote)。
SDK调用方声明：`import('./types').ProductCheckoutQuote`；[client.ts:116](../../client/packages/sdk/src/client.ts#L116)。

## ProductPublishingController

### `GET /api/me/products`

[ProductPublishingController.java:11](../../server/src/main/java/com/kuaixietech/controller/ProductPublishingController.java#L11)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/products`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').OwnedProduct[]`；[client.ts:212](../../client/packages/sdk/src/client.ts#L212)。

### `POST /api/me/products`

[ProductPublishingController.java:12](../../server/src/main/java/com/kuaixietech/controller/ProductPublishingController.java#L12)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/products`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ProductPublishingService.Input → [com.kuaixietech.service.catalog.support.ProductPublishingService.Input](JAVA_TYPES.md#com-kuaixietech-service-catalog-support-productpublishingservice-input): name:String, spec:String, region:String, price:BigDecimal, stock:Integer, categoryId:Long, imageUrls:List< String>, note:String, expectedStock:Integer | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:'off'}`；[client.ts:213](../../client/packages/sdk/src/client.ts#L213)。

### `PUT /api/me/products/{id}`

[ProductPublishingController.java:13](../../server/src/main/java/com/kuaixietech/controller/ProductPublishingController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/products/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | ProductPublishingService.Input → [com.kuaixietech.service.catalog.support.ProductPublishingService.Input](JAVA_TYPES.md#com-kuaixietech-service-catalog-support-productpublishingservice-input): name:String, spec:String, region:String, price:BigDecimal, stock:Integer, categoryId:Long, imageUrls:List< String>, note:String, expectedStock:Integer | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:'off'}`；[client.ts:214](../../client/packages/sdk/src/client.ts#L214)。

### `PUT /api/me/products/{id}/status`

[ProductPublishingController.java:14](../../server/src/main/java/com/kuaixietech/controller/ProductPublishingController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/products/{id}/status`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | ProductPublishingService.State → [com.kuaixietech.service.catalog.support.ProductPublishingService.State](JAVA_TYPES.md#com-kuaixietech-service-catalog-support-productpublishingservice-state): status:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:'on'|'off'}`；[client.ts:215](../../client/packages/sdk/src/client.ts#L215)。

## AccountCentreController

### `GET /api/me/profile`

[AccountCentreController.java:14](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/profile`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').MyProfile`；[client.ts:230](../../client/packages/sdk/src/client.ts#L230)。

### `PUT /api/me/profile`

[AccountCentreController.java:15](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/profile`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ProfileUpdate → [com.kuaixietech.controller.AccountCentreController.ProfileUpdate](JAVA_TYPES.md#com-kuaixietech-controller-accountcentrecontroller-profileupdate): nickname:String, city:String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').MyProfile`；[client.ts:231](../../client/packages/sdk/src/client.ts#L231)。

## ShopController

### `GET /api/me/shop`

[ShopController.java:12](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L12)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').ShopState`；[client.ts:149](../../client/packages/sdk/src/client.ts#L149)。

### `PUT /api/me/shop`

[ShopController.java:14](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ShopService.Edit → [com.kuaixietech.shops.ShopService.Edit](JAVA_TYPES.md#com-kuaixietech-shops-shopservice-edit): name:String, description:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{status:string}`；[client.ts:151](../../client/packages/sdk/src/client.ts#L151)。

### `POST /api/me/shop/applications`

[ShopController.java:13](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop/applications`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ShopService.Application → [com.kuaixietech.shops.ShopService.Application](JAVA_TYPES.md#com-kuaixietech-shops-shopservice-application): businessName:String, creditCode:String, representative:String, licenseUploadId:String, invitationCode:String, channel:String, requestId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:string}`；[client.ts:150](../../client/packages/sdk/src/client.ts#L150)。

## StoreOrderAttributionController

### `GET /api/me/shop/orders`

[StoreOrderAttributionController.java:15](../../server/src/main/java/com/kuaixietech/commerce/StoreOrderAttributionController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop/orders`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').ShopAttributedOrder[]`；[client.ts:147](../../client/packages/sdk/src/client.ts#L147)。

## ShopController

### `GET /api/me/shop/products`

[ShopController.java:16](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop/products`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 100 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').ShopState['products']`；[client.ts:153](../../client/packages/sdk/src/client.ts#L153)。

### `POST /api/me/shop/products`

[ShopController.java:17](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L17)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop/products`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ShopService.Link → [com.kuaixietech.shops.ShopService.Link](JAVA_TYPES.md#com-kuaixietech-shops-shopservice-link): productId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{status:string}`；[client.ts:154](../../client/packages/sdk/src/client.ts#L154)。

### `DELETE /api/me/shop/products/{id}`

[ShopController.java:18](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop/products/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{status:string}`；[client.ts:155](../../client/packages/sdk/src/client.ts#L155)。

### `PUT /api/me/shop/{id}/status`

[ShopController.java:15](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/shop/{id}/status`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | ShopService.State → [com.kuaixietech.shops.ShopService.State](JAVA_TYPES.md#com-kuaixietech-shops-shopservice-state): status:String, expectedStatus:String, reason:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{status:string}`；[client.ts:152](../../client/packages/sdk/src/client.ts#L152)。

## AccountCentreController

### `GET /api/me/trips`

[AccountCentreController.java:19](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/trips`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').MyTrip[]`；[client.ts:235](../../client/packages/sdk/src/client.ts#L235)。

## UploadController

### `GET /api/me/uploads`

[UploadController.java:13](../../server/src/main/java/com/kuaixietech/storage/UploadController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/uploads`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | purpose | String | True | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').BackendRecord[]`；[client.ts:142](../../client/packages/sdk/src/client.ts#L142)。

### `POST /api/me/uploads`

[UploadController.java:12](../../server/src/main/java/com/kuaixietech/storage/UploadController.java#L12)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/uploads`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| multipart | file | MultipartFile | True | — |
| query | purpose | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').UploadedDocument`；[client.ts:141](../../client/packages/sdk/src/client.ts#L141)。

### `GET /api/me/uploads/deletions`

[UploadController.java:15](../../server/src/main/java/com/kuaixietech/storage/UploadController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/uploads/deletions`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').BackendRecord[]`；[client.ts:140](../../client/packages/sdk/src/client.ts#L140)。

### `DELETE /api/me/uploads/{id}`

[UploadController.java:14](../../server/src/main/java/com/kuaixietech/storage/UploadController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/uploads/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:'deleted'|'deleting'}`；[client.ts:139](../../client/packages/sdk/src/client.ts#L139)。

### `GET /api/me/uploads/{id}/content`

[UploadController.java:16](../../server/src/main/java/com/kuaixietech/storage/UploadController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/uploads/{id}/content`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`ResponseEntity<byte[]>`。
SDK调用方声明：`ArrayBuffer`；[client.ts:143](../../client/packages/sdk/src/client.ts#L143)。

## AccountCentreController

### `GET /api/me/wallet`

[AccountCentreController.java:20](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L20)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/wallet`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').MyWallet`；[client.ts:236](../../client/packages/sdk/src/client.ts#L236)。

### `GET /api/me/wallet/ledger`

[AccountCentreController.java:21](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L21)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/me/wallet/ledger`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').MyLedger[]`；[client.ts:237](../../client/packages/sdk/src/client.ts#L237)。

## PaymentsController

### `POST /api/payment-callback/{merchant}/{id}`

[PaymentsController.java:25](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L25)；provider_signature_callback。

服务端无该路径的去 `/api` 网关兼容声明，使用规范路径。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | merchant | String | True | — |
| path | id | String | True | — |

返回 Java 类型：`ResponseEntity<String>`。

## AftersalesEscrowController

### `GET /api/portal/admin/after-sales/escrow-refunds/{id}/events`

[AftersalesEscrowController.java:16](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowController.java#L16)；administrator; service guard applies。

网关兼容路径：`/portal/admin/after-sales/escrow-refunds/{id}/events`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 50 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').EscrowRefundEvent[]`；[client.ts:135](../../client/packages/sdk/src/client.ts#L135)。

### `GET /api/portal/admin/after-sales/{id}/escrow-budget`

[AftersalesEscrowController.java:12](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowController.java#L12)；administrator; service guard applies。

网关兼容路径：`/portal/admin/after-sales/{id}/escrow-budget`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').EscrowRefundBudget`；[client.ts:132](../../client/packages/sdk/src/client.ts#L132)。

### `POST /api/portal/admin/after-sales/{id}/escrow-refund`

[AftersalesEscrowController.java:13](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowController.java#L13)；administrator; service guard applies。

网关兼容路径：`/portal/admin/after-sales/{id}/escrow-refund`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | AftersalesEscrowFunds.Execute → [com.kuaixietech.aftersalesfunds.AftersalesEscrowFunds.Execute](JAVA_TYPES.md#com-kuaixietech-aftersalesfunds-aftersalesescrowfunds-execute): requestId:String, expectedRound:int, note:String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').EscrowWalletRefund`；[client.ts:133](../../client/packages/sdk/src/client.ts#L133)。

## BusinessCoverageController

### `GET /api/portal/admin/business-coverage`

[BusinessCoverageController.java:14](../../server/src/main/java/com/kuaixietech/controller/BusinessCoverageController.java#L14)；administrator; service guard applies。

网关兼容路径：`/portal/admin/business-coverage`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`JsonNode`。

## PortalController

### `POST /api/portal/admin/community/review`

[PortalController.java:20](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L20)；administrator; service guard applies。

网关兼容路径：`/portal/admin/community/review`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PortalDtos.ReviewPost → [com.kuaixietech.entity.portal.PortalDtos.ReviewPost](JAVA_TYPES.md#com-kuaixietech-entity-portal-portaldtos-reviewpost): postId:String, approved:boolean | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:78](../../client/packages/sdk/src/client.ts#L78)。

## ContactDeliveryHealthController

### `GET /api/portal/admin/contact-delivery/health`

[ContactDeliveryHealthController.java:21](../../server/src/main/java/com/kuaixietech/scheduler/ContactDeliveryHealthController.java#L21)；administrator; service guard applies。

网关兼容路径：`/portal/admin/contact-delivery/health`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。

## PortalController

### `POST /api/portal/admin/demands/review`

[PortalController.java:19](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L19)；administrator; service guard applies。

网关兼容路径：`/portal/admin/demands/review`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PortalDtos.ReviewDemand → [com.kuaixietech.entity.portal.PortalDtos.ReviewDemand](JAVA_TYPES.md#com-kuaixietech-entity-portal-portaldtos-reviewdemand): demandId:String, approved:boolean | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:77](../../client/packages/sdk/src/client.ts#L77)。

## TradeCaseController

### `GET /api/portal/admin/disputes`

[TradeCaseController.java:21](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L21)；administrator; service guard applies。

网关兼容路径：`/portal/admin/disputes`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 50 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').TradeCaseSummary[]`；[client.ts:173](../../client/packages/sdk/src/client.ts#L173)。

### `GET /api/portal/admin/disputes/{id}`

[TradeCaseController.java:22](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L22)；administrator; service guard applies。

网关兼容路径：`/portal/admin/disputes/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').TradeCaseDetail`；[client.ts:174](../../client/packages/sdk/src/client.ts#L174)。

### `POST /api/portal/admin/disputes/{id}/decision`

[TradeCaseController.java:23](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L23)；administrator; service guard applies。

网关兼容路径：`/portal/admin/disputes/{id}/decision`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | Decision → [com.kuaixietech.cases.CaseModels.Decision](JAVA_TYPES.md#com-kuaixietech-cases-casemodels-decision): outcome:String, refundAmount:BigDecimal, reason:String, depositOwner:String, expectedRound:int, requestId:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;stage:string}`；[client.ts:180](../../client/packages/sdk/src/client.ts#L180)。

### `GET /api/portal/admin/disputes/{id}/events`

[TradeCaseController.java:24](../../server/src/main/java/com/kuaixietech/cases/TradeCaseController.java#L24)；administrator; service guard applies。

网关兼容路径：`/portal/admin/disputes/{id}/events`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 100 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').TradeCaseEvent[]`；[client.ts:181](../../client/packages/sdk/src/client.ts#L181)。

## InsuranceController

### `GET /api/portal/admin/insurance/applications`

[InsuranceController.java:24](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L24)；administrator; service guard applies。

网关兼容路径：`/portal/admin/insurance/applications`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').BackendRecord[]`；[client.ts:197](../../client/packages/sdk/src/client.ts#L197)。

### `PUT /api/portal/admin/insurance/products`

[InsuranceController.java:23](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L23)；administrator; service guard applies。

网关兼容路径：`/portal/admin/insurance/products`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ProductInput → [com.kuaixietech.insurance.InsuranceModels.ProductInput](JAVA_TYPES.md#com-kuaixietech-insurance-insurancemodels-productinput): id:String, insurerCode:String, insurerName:String, name:String, kind:String, adapter:String, providerProductCode:String, credentialPrefix:String, termsUrl:String, exclusionsUrl:String, claimsUrl:String, serviceHosts:Set< String>, checkoutHosts:Set< String>, enabled:boolean | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string}`；[client.ts:195](../../client/packages/sdk/src/client.ts#L195)。

### `GET /api/portal/admin/insurance/settings`

[InsuranceController.java:22](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L22)；administrator; service guard applies。

网关兼容路径：`/portal/admin/insurance/settings`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').InsuranceSettings`；[client.ts:194](../../client/packages/sdk/src/client.ts#L194)。

## CouplerController

### `GET /api/portal/admin/integration-center`

[CouplerController.java:15](../../server/src/main/java/com/kuaixietech/coupler/CouplerController.java#L15)；administrator; service guard applies。

网关兼容路径：`/portal/admin/integration-center`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').CouplerSettings`；[client.ts:161](../../client/packages/sdk/src/client.ts#L161)。

### `PUT /api/portal/admin/integration-center/runtime`

[CouplerController.java:16](../../server/src/main/java/com/kuaixietech/coupler/CouplerController.java#L16)；administrator; service guard applies。

网关兼容路径：`/portal/admin/integration-center/runtime`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | Switch → [com.kuaixietech.coupler.CouplerModels.Switch](JAVA_TYPES.md#com-kuaixietech-coupler-couplermodels-switch): mode:Mode, expectedRevision:long | True | — |

返回 Java 类型：`Runtime`。
响应字段：[com.kuaixietech.coupler.CouplerModels.Runtime](JAVA_TYPES.md#com-kuaixietech-coupler-couplermodels-runtime)。
SDK调用方声明：`import('./types').CouplerRuntime`；[client.ts:162](../../client/packages/sdk/src/client.ts#L162)。

### `PUT /api/portal/admin/integration-center/{category}/{provider}/{mode}`

[CouplerController.java:17](../../server/src/main/java/com/kuaixietech/coupler/CouplerController.java#L17)；administrator; service guard applies。

网关兼容路径：`/portal/admin/integration-center/{category}/{provider}/{mode}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | category | String | True | — |
| path | provider | String | True | — |
| path | mode | Mode → [com.kuaixietech.coupler.CouplerModels.Mode](JAVA_TYPES.md#com-kuaixietech-coupler-couplermodels-mode) | True | — |
| body | input | Save → [com.kuaixietech.coupler.CouplerModels.Save](JAVA_TYPES.md#com-kuaixietech-coupler-couplermodels-save): values:Map< String, String>, clearFields:List<String>, enabled:boolean, selected:boolean, expectedRevision:long | True | — |

返回 Java 类型：`Map<String,Long>`。
SDK调用方声明：`{revision:number}`；[client.ts:163](../../client/packages/sdk/src/client.ts#L163)。

### `POST /api/portal/admin/integration-center/{category}/{provider}/{mode}/validate`

[CouplerController.java:18](../../server/src/main/java/com/kuaixietech/coupler/CouplerController.java#L18)；administrator; service guard applies。

网关兼容路径：`/portal/admin/integration-center/{category}/{provider}/{mode}/validate`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | category | String | True | — |
| path | provider | String | True | — |
| path | mode | Mode → [com.kuaixietech.coupler.CouplerModels.Mode](JAVA_TYPES.md#com-kuaixietech-coupler-couplermodels-mode) | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').CouplerValidation`；[client.ts:164](../../client/packages/sdk/src/client.ts#L164)。

## IntegrationAdminController

### `GET /api/portal/admin/integrations`

[IntegrationAdminController.java:13](../../server/src/main/java/com/kuaixietech/integration/IntegrationAdminController.java#L13)；administrator; service guard applies。

网关兼容路径：`/portal/admin/integrations`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').IntegrationSettings`；[client.ts:167](../../client/packages/sdk/src/client.ts#L167)。

### `GET /api/portal/admin/integrations/sms-deliveries`

[IntegrationAdminController.java:12](../../server/src/main/java/com/kuaixietech/integration/IntegrationAdminController.java#L12)；administrator; service guard applies。

网关兼容路径：`/portal/admin/integrations/sms-deliveries`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').BackendRecord[]`；[client.ts:169](../../client/packages/sdk/src/client.ts#L169)。

### `PUT /api/portal/admin/integrations/{vendor}/{product}`

[IntegrationAdminController.java:14](../../server/src/main/java/com/kuaixietech/integration/IntegrationAdminController.java#L14)；administrator; service guard applies。

网关兼容路径：`/portal/admin/integrations/{vendor}/{product}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | vendor | String | True | — |
| path | product | String | True | — |
| body | input | IntegrationConfiguration.Input → [com.kuaixietech.integration.IntegrationConfiguration.Input](JAVA_TYPES.md#com-kuaixietech-integration-integrationconfiguration-input): enabled:boolean, remark:String, values:Map< String, String>, clearFields:List<String> | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{status:string}`；[client.ts:168](../../client/packages/sdk/src/client.ts#L168)。

## PaymentRefundController

### `GET /api/portal/admin/payment-refunds`

[PaymentRefundController.java:20](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L20)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payment-refunds`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PaymentRefund[]`；[client.ts:127](../../client/packages/sdk/src/client.ts#L127)。

### `GET /api/portal/admin/payment-refunds/{id}`

[PaymentRefundController.java:21](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L21)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payment-refunds/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:125](../../client/packages/sdk/src/client.ts#L125)。

### `POST /api/portal/admin/payment-refunds/{id}/dispatch`

[PaymentRefundController.java:24](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L24)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payment-refunds/{id}/dispatch`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:129](../../client/packages/sdk/src/client.ts#L129)。

### `GET /api/portal/admin/payment-refunds/{id}/events`

[PaymentRefundController.java:22](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L22)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payment-refunds/{id}/events`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 50 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PaymentRefundEvent[]`；[client.ts:120](../../client/packages/sdk/src/client.ts#L120)。

### `POST /api/portal/admin/payment-refunds/{id}/refresh`

[PaymentRefundController.java:25](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L25)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payment-refunds/{id}/refresh`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:126](../../client/packages/sdk/src/client.ts#L126)。

### `POST /api/portal/admin/payment-refunds/{id}/review`

[PaymentRefundController.java:23](../../server/src/main/java/com/kuaixietech/refunds/PaymentRefundController.java#L23)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payment-refunds/{id}/review`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | Review → [com.kuaixietech.refunds.RefundModels.Review](JAVA_TYPES.md#com-kuaixietech-refunds-refundmodels-review): approve:boolean, reason:String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentRefund`；[client.ts:128](../../client/packages/sdk/src/client.ts#L128)。

## PaymentsController

### `PUT /api/portal/admin/payments/fx-sources`

[PaymentsController.java:24](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L24)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payments/fx-sources`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | FxInput → [com.kuaixietech.payments.PaymentModels.FxInput](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-fxinput): id:String, name:String, kind:String, endpoint:String, credentialPrefix:String, maxAgeSeconds:int, enabled:boolean, transactionEnabled:boolean | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string}`；[client.ts:227](../../client/packages/sdk/src/client.ts#L227)。

### `PUT /api/portal/admin/payments/merchants`

[PaymentsController.java:23](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L23)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payments/merchants`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | MerchantInput → [com.kuaixietech.payments.PaymentModels.MerchantInput](JAVA_TYPES.md#com-kuaixietech-payments-paymentmodels-merchantinput): id:String, name:String, adapter:String, method:String, platform:String, currencies:List<String>, credentialPrefix:String, spreadBps:int, quoteTtlSeconds:int, enabled:boolean, settings:Map<String,String> | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string}`；[client.ts:226](../../client/packages/sdk/src/client.ts#L226)。

### `GET /api/portal/admin/payments/settings`

[PaymentsController.java:22](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L22)；administrator; service guard applies。

网关兼容路径：`/portal/admin/payments/settings`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PaymentSettings`；[client.ts:225](../../client/packages/sdk/src/client.ts#L225)。

## PortalController

### `GET /api/portal/admin/reviews`

[PortalController.java:17](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L17)；administrator; service guard applies。

网关兼容路径：`/portal/admin/reviews`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,List<Map<String,Object>>>`。
SDK调用方声明：`{demands:import('./types').BackendRecord[];trips:import('./types').BackendRecord[];posts:import('./types').BackendRecord[]}`；[client.ts:75](../../client/packages/sdk/src/client.ts#L75)。

## ShopController

### `GET /api/portal/admin/shops/applications`

[ShopController.java:20](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L20)；administrator; service guard applies。

网关兼容路径：`/portal/admin/shops/applications`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').ShopApplication[]`；[client.ts:157](../../client/packages/sdk/src/client.ts#L157)。

### `POST /api/portal/admin/shops/applications/{id}/review`

[ShopController.java:21](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L21)；administrator; service guard applies。

网关兼容路径：`/portal/admin/shops/applications/{id}/review`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | ShopService.Review → [com.kuaixietech.shops.ShopService.Review](JAVA_TYPES.md#com-kuaixietech-shops-shopservice-review): approve:boolean, reason:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:string}`；[client.ts:158](../../client/packages/sdk/src/client.ts#L158)。

## StoreOrderAttributionController

### `GET /api/portal/admin/shops/{id}/orders`

[StoreOrderAttributionController.java:16](../../server/src/main/java/com/kuaixietech/commerce/StoreOrderAttributionController.java#L16)；administrator; service guard applies。

网关兼容路径：`/portal/admin/shops/{id}/orders`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 20 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').ShopAttributedOrder[]`；[client.ts:148](../../client/packages/sdk/src/client.ts#L148)。

## ShopController

### `PUT /api/portal/admin/shops/{id}/status`

[ShopController.java:22](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L22)；administrator; service guard applies。

网关兼容路径：`/portal/admin/shops/{id}/status`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | input | ShopService.State → [com.kuaixietech.shops.ShopService.State](JAVA_TYPES.md#com-kuaixietech-shops-shopservice-state): status:String, expectedStatus:String, reason:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{status:string}`；[client.ts:152](../../client/packages/sdk/src/client.ts#L152)。

## PortalController

### `POST /api/portal/admin/trips/review`

[PortalController.java:18](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L18)；administrator; service guard applies。

网关兼容路径：`/portal/admin/trips/review`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PortalDtos.ReviewTrip → [com.kuaixietech.entity.portal.PortalDtos.ReviewTrip](JAVA_TYPES.md#com-kuaixietech-entity-portal-portaldtos-reviewtrip): tripId:String, approved:boolean | True | — |

返回 Java 类型：`void`。
SDK调用方声明：`void`；[client.ts:76](../../client/packages/sdk/src/client.ts#L76)。

### `POST /api/portal/community/posts`

[PortalController.java:16](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/portal/community/posts`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PortalDtos.SocialPostCreate → [com.kuaixietech.entity.portal.PortalDtos.SocialPostCreate](JAVA_TYPES.md#com-kuaixietech-entity-portal-portaldtos-socialpostcreate): caption:String, mediaKind:String, mediaUrl:String, posterUrl:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:string}`；[client.ts:74](../../client/packages/sdk/src/client.ts#L74)。

### `POST /api/portal/demands`

[PortalController.java:14](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/portal/demands`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PortalDtos.DemandCreate → [com.kuaixietech.entity.portal.PortalDtos.DemandCreate](JAVA_TYPES.md#com-kuaixietech-entity-portal-portaldtos-demandcreate): title:String, description:String, city:String, expectPrice:BigDecimal, tripType:String | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:string}`；[client.ts:72](../../client/packages/sdk/src/client.ts#L72)。

### `GET /api/portal/me`

[PortalController.java:13](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/portal/me`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').VerifiedPortalRole`；[client.ts:71](../../client/packages/sdk/src/client.ts#L71)。

### `POST /api/portal/trips`

[PortalController.java:15](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/portal/trips`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PortalDtos.TripCreate → [com.kuaixietech.entity.portal.PortalDtos.TripCreate](JAVA_TYPES.md#com-kuaixietech-entity-portal-portaldtos-tripcreate): destination:String, originCity:String, departDate:String, returnDate:String, tripType:String, transportMode:String, capacityQty:Integer | True | — |

返回 Java 类型：`Map<String,String>`。
SDK调用方声明：`{id:string;status:string}`；[client.ts:73](../../client/packages/sdk/src/client.ts#L73)。

## PublicInformationController

### `GET /api/public/announcements`

[PublicInformationController.java:10](../../server/src/main/java/com/kuaixietech/controller/PublicInformationController.java#L10)；public。

网关兼容路径：`/public/announcements`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 30 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicAnnouncement[]`；[client.ts:83](../../client/packages/sdk/src/client.ts#L83)。

## PublicCatalogController

### `GET /api/public/categories`

[PublicCatalogController.java:22](../../server/src/main/java/com/kuaixietech/controller/PublicCatalogController.java#L22)；public。

网关兼容路径：`/public/categories`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 100 |
| query | countryCode | String | False | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicCategory[]`；[client.ts:61](../../client/packages/sdk/src/client.ts#L61)。

## PublicCategoryOptionsController

### `GET /api/public/category-options`

[PublicCategoryOptionsController.java:13](../../server/src/main/java/com/kuaixietech/controller/PublicCategoryOptionsController.java#L13)；public。

网关兼容路径：`/public/category-options`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 100 |
| query | offset | int | False | 0 |
| query | search | String | False | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicCategory[]`；[client.ts:60](../../client/packages/sdk/src/client.ts#L60)。

## PortalController

### `GET /api/public/community/feed`

[PortalController.java:12](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L12)；public。

网关兼容路径：`/public/community/feed`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 20 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicCommunityPost[]`；[client.ts:70](../../client/packages/sdk/src/client.ts#L70)。

### `GET /api/public/demands`

[PortalController.java:11](../../server/src/main/java/com/kuaixietech/controller/PortalController.java#L11)；public。

网关兼容路径：`/public/demands`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 30 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicDemand[]`；[client.ts:69](../../client/packages/sdk/src/client.ts#L69)。

## InsuranceController

### `GET /api/public/insurance/companies`

[InsuranceController.java:11](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L11)；public。

网关兼容路径：`/public/insurance/companies`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,String>>`。
SDK调用方声明：`import('./types').InsuranceCompany[]`；[client.ts:184](../../client/packages/sdk/src/client.ts#L184)。

### `GET /api/public/insurance/products`

[InsuranceController.java:12](../../server/src/main/java/com/kuaixietech/insurance/InsuranceController.java#L12)；public。

网关兼容路径：`/public/insurance/products`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | platform | String | False | web |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').InsuranceProduct[]`；[client.ts:185](../../client/packages/sdk/src/client.ts#L185)。

## PublicInformationController

### `GET /api/public/legal-documents/{key}`

[PublicInformationController.java:11](../../server/src/main/java/com/kuaixietech/controller/PublicInformationController.java#L11)；public。

网关兼容路径：`/public/legal-documents/{key}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | key | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PublicLegalDocument`；[client.ts:84](../../client/packages/sdk/src/client.ts#L84)。

## PublicMediaController

### `GET /api/public/media/{id}`

[PublicMediaController.java:9](../../server/src/main/java/com/kuaixietech/storage/PublicMediaController.java#L9)；public。

网关兼容路径：`/public/media/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`ResponseEntity<byte[]>`。

## PaymentsController

### `GET /api/public/payments/currencies`

[PaymentsController.java:13](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L13)；public。

网关兼容路径：`/public/payments/currencies`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').CurrencyInfo[]`；[client.ts:218](../../client/packages/sdk/src/client.ts#L218)。

### `GET /api/public/payments/methods`

[PaymentsController.java:14](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L14)；public。

网关兼容路径：`/public/payments/methods`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | platform | String | False | web |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PaymentMethod[]`；[client.ts:219](../../client/packages/sdk/src/client.ts#L219)。

### `GET /api/public/payments/rate`

[PaymentsController.java:15](../../server/src/main/java/com/kuaixietech/payments/PaymentsController.java#L15)；public。

网关兼容路径：`/public/payments/rate`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | base | String | False | CNY |
| query | currency | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').ExchangeRate`；[client.ts:220](../../client/packages/sdk/src/client.ts#L220)。

## PublicCatalogController

### `GET /api/public/products`

[PublicCatalogController.java:28](../../server/src/main/java/com/kuaixietech/controller/PublicCatalogController.java#L28)；public。

网关兼容路径：`/public/products`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 30 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicProduct[]`；[client.ts:63](../../client/packages/sdk/src/client.ts#L63)。

### `GET /api/public/products/{id}`

[PublicCatalogController.java:30](../../server/src/main/java/com/kuaixietech/controller/PublicCatalogController.java#L30)；public。

网关兼容路径：`/public/products/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PublicProduct`；[client.ts:62](../../client/packages/sdk/src/client.ts#L62)。

## ProductCheckoutController

### `GET /api/public/products/{id}/trips`

[ProductCheckoutController.java:12](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutController.java#L12)；public。

网关兼容路径：`/public/products/{id}/trips`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 100 |
| query | offset | int | False | 0 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').ProductCheckoutTrip[]`；[client.ts:115](../../client/packages/sdk/src/client.ts#L115)。

## PublicCatalogController

### `GET /api/public/profiles/masked`

[PublicCatalogController.java:41](../../server/src/main/java/com/kuaixietech/controller/PublicCatalogController.java#L41)；public。

网关兼容路径：`/public/profiles/masked`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | ids | String | True | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').BackendRecord[]`；[client.ts:65](../../client/packages/sdk/src/client.ts#L65)。

## PublicInformationController

### `GET /api/public/reviews`

[PublicInformationController.java:12](../../server/src/main/java/com/kuaixietech/controller/PublicInformationController.java#L12)；public。

网关兼容路径：`/public/reviews`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | targetId | String | True | — |
| query | limit | int | False | 50 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicReview[]`；[client.ts:85](../../client/packages/sdk/src/client.ts#L85)。

## ShopController

### `GET /api/public/shops/{id}`

[ShopController.java:19](../../server/src/main/java/com/kuaixietech/shops/ShopController.java#L19)；public。

网关兼容路径：`/public/shops/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 100 |
| query | offset | int | False | 0 |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').PublicShop`；[client.ts:156](../../client/packages/sdk/src/client.ts#L156)。

## PublicCatalogController

### `GET /api/public/trips`

[PublicCatalogController.java:36](../../server/src/main/java/com/kuaixietech/controller/PublicCatalogController.java#L36)；public。

网关兼容路径：`/public/trips`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| query | limit | int | False | 30 |
| query | sort | String | False | rank |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').PublicTrip[]`；[client.ts:64](../../client/packages/sdk/src/client.ts#L64)。

## TradeRpcController

### `POST /api/rpc/accept_order_atomic`

[TradeRpcController.java:22](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L22)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/accept_order_atomic`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | AcceptOrderBody → [com.kuaixietech.controller.TradeRpcController.AcceptOrderBody](JAVA_TYPES.md#com-kuaixietech-controller-traderpccontroller-acceptorderbody): orderId:String, deposit:BigDecimal | True | — |

返回 Java 类型：`String`。

## AdminFinanceRpcController

### `POST /api/rpc/admin_adjust_balance`

[AdminFinanceRpcController.java:16](../../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java#L16)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_adjust_balance`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | BalanceBody → [com.kuaixietech.controller.AdminFinanceRpcController.BalanceBody](JAVA_TYPES.md#com-kuaixietech-controller-adminfinancerpccontroller-balancebody): userId:String, delta:BigDecimal, note:String | True | — |

返回 Java 类型：`String`。

## AdminConsoleRpcController

### `POST /api/rpc/admin_ban_user`

[AdminConsoleRpcController.java:43](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L43)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_ban_user`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | BanBody → [com.kuaixietech.controller.AdminConsoleRpcController.BanBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-banbody): userId:String, ban:boolean, reason:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_broadcast`

[AdminConsoleRpcController.java:44](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L44)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_broadcast`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | BroadcastBody → [com.kuaixietech.controller.AdminConsoleRpcController.BroadcastBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-broadcastbody): title:String, body:String, scope:String, userIds:List<String> | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_close_demand`

[AdminConsoleRpcController.java:45](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L45)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_close_demand`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | DemandCloseBody → [com.kuaixietech.controller.AdminConsoleRpcController.DemandCloseBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-demandclosebody): demandId:String, reason:String | True | — |

返回 Java 类型：`String`。

## AdminFinanceRpcController

### `POST /api/rpc/admin_commission_rows`

[AdminFinanceRpcController.java:18](../../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java#L18)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_commission_rows`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | LimitBody → [com.kuaixietech.controller.AdminFinanceRpcController.LimitBody](JAVA_TYPES.md#com-kuaixietech-controller-adminfinancerpccontroller-limitbody): limit:Integer | True | — |

返回 Java 类型：`List<Map<String,Object>>`。

## AdminConsoleRpcController

### `POST /api/rpc/admin_console_list`

[AdminConsoleRpcController.java:39](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L39)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_console_list`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ListBody → [com.kuaixietech.controller.AdminConsoleRpcController.ListBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-listbody): kind:String, limit:Integer, keyword:String, offset:Integer | True | — |

返回 Java 类型：`List<Map<String,Object>>`。

### `POST /api/rpc/admin_create_category`

[AdminConsoleRpcController.java:48](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L48)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_create_category`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | CategoryCreate → [com.kuaixietech.controller.AdminConsoleRpcController.CategoryCreate](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-categorycreate): name:String, parentId:Long, level:Integer | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_create_invite`

[AdminConsoleRpcController.java:50](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L50)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_create_invite`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | InviteBody → [com.kuaixietech.controller.AdminConsoleRpcController.InviteBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-invitebody): code:String, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_deactivate_category`

[AdminConsoleRpcController.java:49](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L49)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_deactivate_category`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | CategoryId → [com.kuaixietech.controller.AdminConsoleRpcController.CategoryId](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-categoryid): id:Long | True | — |

返回 Java 类型：`String`。

## AdminFinanceRpcController

### `POST /api/rpc/admin_dissolve_pool`

[AdminFinanceRpcController.java:17](../../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java#L17)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_dissolve_pool`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PoolBody → [com.kuaixietech.controller.AdminFinanceRpcController.PoolBody](JAVA_TYPES.md#com-kuaixietech-controller-adminfinancerpccontroller-poolbody): tripId:String, reason:String | True | — |

返回 Java 类型：`String`。

## AdminConsoleRpcController

### `POST /api/rpc/admin_fix_score`

[AdminConsoleRpcController.java:46](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L46)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_fix_score`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ScoreBody → [com.kuaixietech.controller.AdminConsoleRpcController.ScoreBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-scorebody): userId:String, delta:Integer, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_force_form_pool`

[AdminConsoleRpcController.java:47](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L47)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_force_form_pool`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PoolFormBody → [com.kuaixietech.controller.AdminConsoleRpcController.PoolFormBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-poolformbody): tripId:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_force_off_shelf`

[AdminConsoleRpcController.java:42](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L42)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_force_off_shelf`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | IdBody → [com.kuaixietech.controller.AdminConsoleRpcController.IdBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-idbody): id:String | True | — |

返回 Java 类型：`String`。

## Batch10RpcController

### `POST /api/rpc/admin_grant_points`

[Batch10RpcController.java:40](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L40)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_grant_points`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | GrantPoints → [com.kuaixietech.controller.Batch10RpcController.GrantPoints](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-grantpoints): userId:String, points:Integer, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_invite_tier_distribution`

[Batch10RpcController.java:38](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L38)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_invite_tier_distribution`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。

### `POST /api/rpc/admin_issue_invoice`

[Batch10RpcController.java:34](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L34)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_issue_invoice`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | InvoiceDecision → [com.kuaixietech.controller.Batch10RpcController.InvoiceDecision](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-invoicedecision): invoiceId:String, approve:boolean, invoiceNo:String, fileUrl:String, reason:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_list_violations`

[Batch10RpcController.java:35](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L35)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_list_violations`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ViolationQuery → [com.kuaixietech.controller.Batch10RpcController.ViolationQuery](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-violationquery): limit:Integer | True | — |

返回 Java 类型：`List<Map<String,Object>>`。

### `POST /api/rpc/admin_locale_remove_rpc`

[Batch10RpcController.java:43](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L43)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_locale_remove_rpc`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | LocaleRemove → [com.kuaixietech.controller.Batch10RpcController.LocaleRemove](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-localeremove): code:String, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_locale_set_active_rpc`

[Batch10RpcController.java:42](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L42)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_locale_set_active_rpc`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | LocaleToggle → [com.kuaixietech.controller.Batch10RpcController.LocaleToggle](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-localetoggle): code:String, active:boolean, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_locale_upsert_rpc`

[Batch10RpcController.java:41](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L41)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_locale_upsert_rpc`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | LocaleUpsert → [com.kuaixietech.controller.Batch10RpcController.LocaleUpsert](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-localeupsert): code:String, nativeName:String, labelZh:String, sort:Integer, fallback:String | True | — |

返回 Java 类型：`String`。

## AdminConsoleRpcController

### `POST /api/rpc/admin_release_privacy`

[AdminConsoleRpcController.java:55](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L55)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_release_privacy`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_remove_post`

[AdminConsoleRpcController.java:54](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L54)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_remove_post`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PostRemove → [com.kuaixietech.controller.AdminConsoleRpcController.PostRemove](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-postremove): postId:String, reason:String | True | — |

返回 Java 类型：`String`。

## Batch11RpcController

### `POST /api/rpc/admin_resolve_cancel`

[Batch11RpcController.java:30](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L30)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_resolve_cancel`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | Decision → [com.kuaixietech.controller.Batch11RpcController.Decision](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-decision): orderId:String, approve:boolean, reason:String | True | — |

返回 Java 类型：`String`。

## AdminConsoleRpcController

### `POST /api/rpc/admin_resolve_error`

[AdminConsoleRpcController.java:56](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L56)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_resolve_error`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ErrorId → [com.kuaixietech.controller.AdminConsoleRpcController.ErrorId](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-errorid): id:Long | True | — |

返回 Java 类型：`String`。

## KycRpcController

### `POST /api/rpc/admin_review_buyer_kyc`

[KycRpcController.java:22](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L22)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_review_buyer_kyc`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | ReviewKyc → [com.kuaixietech.controller.KycRpcController.ReviewKyc](JAVA_TYPES.md#com-kuaixietech-controller-kycrpccontroller-reviewkyc): userId:String, approve:boolean, reason:String | True | — |

返回 Java 类型：`String`。

## Batch10RpcController

### `POST /api/rpc/admin_review_enterprise`

[Batch10RpcController.java:39](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L39)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_review_enterprise`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | EnterpriseDecision → [com.kuaixietech.controller.Batch10RpcController.EnterpriseDecision](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-enterprisedecision): userId:String, approve:boolean, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_review_violation`

[Batch10RpcController.java:36](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L36)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_review_violation`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | RowId → [com.kuaixietech.controller.Batch10RpcController.RowId](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-rowid): id:String | True | — |

返回 Java 类型：`String`。

## AdminConsoleRpcController

### `POST /api/rpc/admin_set_carrier_verify`

[AdminConsoleRpcController.java:40](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L40)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_set_carrier_verify`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | VerifyBody → [com.kuaixietech.controller.AdminConsoleRpcController.VerifyBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-verifybody): userId:String, tripId:String, approve:boolean, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_set_trip_verify`

[AdminConsoleRpcController.java:41](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L41)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_set_trip_verify`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | VerifyBody → [com.kuaixietech.controller.AdminConsoleRpcController.VerifyBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-verifybody): userId:String, tripId:String, approve:boolean, note:String | True | — |

返回 Java 类型：`String`。

## Batch10RpcController

### `POST /api/rpc/admin_stats`

[Batch10RpcController.java:37](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L37)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_stats`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。

## AdminConsoleRpcController

### `POST /api/rpc/admin_toggle_channel`

[AdminConsoleRpcController.java:53](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L53)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_toggle_channel`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ChannelToggle → [com.kuaixietech.controller.AdminConsoleRpcController.ChannelToggle](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-channeltoggle): id:String, enabled:boolean | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_toggle_recipe`

[AdminConsoleRpcController.java:52](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L52)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_toggle_recipe`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | RecipeToggle → [com.kuaixietech.controller.AdminConsoleRpcController.RecipeToggle](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-recipetoggle): id:String, active:boolean | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/admin_upsert_recipe`

[AdminConsoleRpcController.java:51](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L51)；administrator; service guard applies。

网关兼容路径：`/rpc/admin_upsert_recipe`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | RecipeBody → [com.kuaixietech.controller.AdminConsoleRpcController.RecipeBody](JAVA_TYPES.md#com-kuaixietech-controller-adminconsolerpccontroller-recipebody): id:String, tripType:String, transportMode:String, countryCode:String, items:String, note:String | True | — |

返回 Java 类型：`String`。

## TradeRpcController

### `POST /api/rpc/award_pk`

[TradeRpcController.java:31](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L31)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/award_pk`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | AwardBody → [com.kuaixietech.controller.TradeRpcController.AwardBody](JAVA_TYPES.md#com-kuaixietech-controller-traderpccontroller-awardbody): demandId:String, quoteId:String, insuranceChoice:String, expectedBuyerEscrow:BigDecimal, insuranceQuoteId:String | True | — |

返回 Java 类型：`String`。

## Batch12RpcController

### `POST /api/rpc/batch12/accept_assist`

[Batch12RpcController.java:40](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L40)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/accept_assist`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | AssistId → [com.kuaixietech.controller.Batch12RpcController.AssistId](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-assistid): id:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/accept_order_call`

[Batch12RpcController.java:45](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L45)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/accept_order_call`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | CallId → [com.kuaixietech.controller.Batch12RpcController.CallId](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-callid): callId:String | True | — |

返回 Java 类型：`Map<String,Object>`。

### `POST /api/rpc/batch12/add_post_comment`

[Batch12RpcController.java:48](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L48)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/add_post_comment`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | Comment → [com.kuaixietech.controller.Batch12RpcController.Comment](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-comment): postId:String, content:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/cancel_assist`

[Batch12RpcController.java:41](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L41)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/cancel_assist`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | AssistId → [com.kuaixietech.controller.Batch12RpcController.AssistId](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-assistid): id:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/create_assist_request`

[Batch12RpcController.java:39](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L39)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/create_assist_request`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | CreateAssist → [com.kuaixietech.controller.Batch12RpcController.CreateAssist](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-createassist): orderId:String, rate:BigDecimal, note:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/finish_assist`

[Batch12RpcController.java:43](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L43)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/finish_assist`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | Handover → [com.kuaixietech.controller.Batch12RpcController.Handover](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-handover): id:String, urlsJson:String, city:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/get_or_bind_privacy_number`

[Batch12RpcController.java:54](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L54)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/get_or_bind_privacy_number`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | OrderId → [com.kuaixietech.controller.Batch12RpcController.OrderId](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-orderid): orderId:String | True | — |

返回 Java 类型：`Map<String,Object>`。

### `POST /api/rpc/batch12/hangup_order_call`

[Batch12RpcController.java:47](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L47)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/hangup_order_call`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | CallId → [com.kuaixietech.controller.Batch12RpcController.CallId](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-callid): callId:String | True | — |

返回 Java 类型：`Map<String,Object>`。

### `POST /api/rpc/batch12/match_join_application`

[Batch12RpcController.java:52](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L52)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/match_join_application`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | MatchJoin → [com.kuaixietech.controller.Batch12RpcController.MatchJoin](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-matchjoin): applicationId:String | True | — |

返回 Java 类型：`boolean`。

### `POST /api/rpc/batch12/reject_order_call`

[Batch12RpcController.java:46](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L46)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/reject_order_call`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | RejectCall → [com.kuaixietech.controller.Batch12RpcController.RejectCall](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-rejectcall): callId:String, asCallee:boolean | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/request_verification_code`

[Batch12RpcController.java:53](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L53)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/request_verification_code`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | VerifyCode → [com.kuaixietech.controller.Batch12RpcController.VerifyCode](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-verifycode): target:String, channel:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/screen_chat_content`

[Batch12RpcController.java:49](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L49)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/screen_chat_content`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | Comment → [com.kuaixietech.controller.Batch12RpcController.Comment](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-comment): postId:String, content:String | True | — |

返回 Java 类型：`Batch12ContentModerationService.Screening`。
响应字段：[com.kuaixietech.service.community.impl.Batch12ContentModerationService.Screening](JAVA_TYPES.md#com-kuaixietech-service-community-impl-batch12contentmoderationservice-screening)。

### `POST /api/rpc/batch12/start_order_call`

[Batch12RpcController.java:44](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L44)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/start_order_call`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | OrderId → [com.kuaixietech.controller.Batch12RpcController.OrderId](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-orderid): orderId:String | True | — |

返回 Java 类型：`Map<String,Object>`。

### `POST /api/rpc/batch12/submit_assist_handover_a`

[Batch12RpcController.java:42](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L42)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/submit_assist_handover_a`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | Handover → [com.kuaixietech.controller.Batch12RpcController.Handover](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-handover): id:String, urlsJson:String, city:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/batch12/submit_carrier_kyc`

[Batch12RpcController.java:51](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L51)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/submit_carrier_kyc`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | Carrier → [com.kuaixietech.controller.Batch12RpcController.Carrier](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-carrier): realName:String, idCardNo:String, passportNo:String, phoneCode:String, inviteCode:String, note:String | True | — |

返回 Java 类型：`boolean`。

### `POST /api/rpc/batch12/submit_enterprise_verify`

[Batch12RpcController.java:50](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L50)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/batch12/submit_enterprise_verify`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | Enterprise → [com.kuaixietech.controller.Batch12RpcController.Enterprise](JAVA_TYPES.md#com-kuaixietech-controller-batch12rpccontroller-enterprise): enterpriseName:String, businessLicenseNo:String, proofUrls:List<String> | True | — |

返回 Java 类型：`String`。

## TradeRpcController

### `POST /api/rpc/close_pk`

[TradeRpcController.java:28](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L28)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/close_pk`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | DemandBody → [com.kuaixietech.controller.TradeRpcController.DemandBody](JAVA_TYPES.md#com-kuaixietech-controller-traderpccontroller-demandbody): demandId:String | True | — |

返回 Java 类型：`String`。

## Batch10RpcController

### `POST /api/rpc/confirm_meetup`

[Batch10RpcController.java:47](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L47)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/confirm_meetup`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ConfirmMeetup → [com.kuaixietech.controller.Batch10RpcController.ConfirmMeetup](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-confirmmeetup): orderId:String, code:String, lat:BigDecimal, lng:BigDecimal, city:String | True | — |

返回 Java 类型：`String`。

## Batch11RpcController

### `POST /api/rpc/confirm_purchase`

[Batch11RpcController.java:24](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L24)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/confirm_purchase`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | Purchase → [com.kuaixietech.controller.Batch11RpcController.Purchase](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-purchase): orderId:String, evidence:String, lat:BigDecimal, lng:BigDecimal, city:String, address:String, meta:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/confirm_ship`

[Batch11RpcController.java:25](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L25)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/confirm_ship`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | Shipment → [com.kuaixietech.controller.Batch11RpcController.Shipment](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-shipment): orderId:String, carrier:String, tracking:String, labelUrls:String, lat:BigDecimal, lng:BigDecimal, city:String, address:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/consume_invite_code`

[Batch11RpcController.java:34](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L34)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/consume_invite_code`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | InviteCode → [com.kuaixietech.controller.Batch11RpcController.InviteCode](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-invitecode): code:String | True | — |

返回 Java 类型：`boolean`。

## TradeRpcController

### `POST /api/rpc/create_publish_deposit`

[TradeRpcController.java:35](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L35)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/create_publish_deposit`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | PublishBody → [com.kuaixietech.controller.TradeRpcController.PublishBody](JAVA_TYPES.md#com-kuaixietech-controller-traderpccontroller-publishbody): amount:BigDecimal, source:String, orderId:String | True | — |

返回 Java 类型：`String`。

## InviteReadRpcController

### `POST /api/rpc/get_invite_code_owner`

[InviteReadRpcController.java:16](../../server/src/main/java/com/kuaixietech/controller/InviteReadRpcController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/get_invite_code_owner`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | Code → [com.kuaixietech.controller.InviteReadRpcController.Code](JAVA_TYPES.md#com-kuaixietech-controller-invitereadrpccontroller-code): code:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/get_operator_invite_code`

[InviteReadRpcController.java:17](../../server/src/main/java/com/kuaixietech/controller/InviteReadRpcController.java#L17)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/get_operator_invite_code`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | Operator → [com.kuaixietech.controller.InviteReadRpcController.Operator](JAVA_TYPES.md#com-kuaixietech-controller-invitereadrpccontroller-operator): operatorId:String | True | — |

返回 Java 类型：`String`。

## Batch10RpcController

### `POST /api/rpc/get_order_evidence`

[Batch10RpcController.java:44](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L44)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/get_order_evidence`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | EvidenceRead → [com.kuaixietech.controller.Batch10RpcController.EvidenceRead](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-evidenceread): orderId:String | True | — |

返回 Java 类型：`Map<String,Object>`。

## Batch11RpcController

### `POST /api/rpc/get_peer_profile`

[Batch11RpcController.java:32](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L32)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/get_peer_profile`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | PeerId → [com.kuaixietech.controller.Batch11RpcController.PeerId](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-peerid): target:String | True | — |

返回 Java 类型：`Map<String,Object>`。

## IdentitySettingsController

### `POST /api/rpc/identity/change_password`

[IdentitySettingsController.java:18](../../server/src/main/java/com/kuaixietech/controller/IdentitySettingsController.java#L18)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/identity/change_password`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ChangePassword → [com.kuaixietech.controller.IdentitySettingsController.ChangePassword](JAVA_TYPES.md#com-kuaixietech-controller-identitysettingscontroller-changepassword): oldPassword:String, newPassword:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/identity/rebind_contact`

[IdentitySettingsController.java:26](../../server/src/main/java/com/kuaixietech/controller/IdentitySettingsController.java#L26)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/identity/rebind_contact`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | RebindContact → [com.kuaixietech.controller.IdentitySettingsController.RebindContact](JAVA_TYPES.md#com-kuaixietech-controller-identitysettingscontroller-rebindcontact): channel:String, newTarget:String, code:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/identity/reset_own_password_by_code`

[IdentitySettingsController.java:22](../../server/src/main/java/com/kuaixietech/controller/IdentitySettingsController.java#L22)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/identity/reset_own_password_by_code`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | input | ResetOwnPassword → [com.kuaixietech.controller.IdentitySettingsController.ResetOwnPassword](JAVA_TYPES.md#com-kuaixietech-controller-identitysettingscontroller-resetownpassword): channel:String, target:String, code:String, newPassword:String | True | — |

返回 Java 类型：`String`。

## KycRpcController

### `POST /api/rpc/issue_face_challenge`

[KycRpcController.java:25](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L25)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/issue_face_challenge`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | FaceIssue → [com.kuaixietech.controller.KycRpcController.FaceIssue](JAVA_TYPES.md#com-kuaixietech-controller-kycrpccontroller-faceissue): scene:String, orderId:String | True | — |

返回 Java 类型：`KycRpcMigrationService.FaceChallenge`。
响应字段：[com.kuaixietech.service.account.impl.KycRpcMigrationService.FaceChallenge](JAVA_TYPES.md#com-kuaixietech-service-account-impl-kycrpcmigrationservice-facechallenge)。

## Batch10RpcController

### `POST /api/rpc/issue_meetup_code`

[Batch10RpcController.java:46](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L46)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/issue_meetup_code`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | IssueMeetup → [com.kuaixietech.controller.Batch10RpcController.IssueMeetup](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-issuemeetup): orderId:String | True | — |

返回 Java 类型：`String`。

## InviteReadRpcController

### `POST /api/rpc/list_team_members`

[InviteReadRpcController.java:15](../../server/src/main/java/com/kuaixietech/controller/InviteReadRpcController.java#L15)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/list_team_members`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | Root → [com.kuaixietech.controller.InviteReadRpcController.Root](JAVA_TYPES.md#com-kuaixietech-controller-invitereadrpccontroller-root): root:String | True | — |

返回 Java 类型：`List<Map<String,Object>>`。

## Batch10RpcController

### `POST /api/rpc/log_privacy_call`

[Batch10RpcController.java:45](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L45)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/log_privacy_call`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | PrivacyCall → [com.kuaixietech.controller.Batch10RpcController.PrivacyCall](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-privacycall): orderId:String, result:String | True | — |

返回 Java 类型：`Map<String,Object>`。

## Batch11RpcController

### `POST /api/rpc/open_peer_thread`

[Batch11RpcController.java:31](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L31)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/open_peer_thread`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | OrderId → [com.kuaixietech.controller.Batch11RpcController.OrderId](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-orderid): orderId:String | True | — |

返回 Java 类型：`Map<String,Object>`。

## Batch10RpcController

### `POST /api/rpc/propose_shipping_fee`

[Batch10RpcController.java:48](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L48)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/propose_shipping_fee`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ProposeFee → [com.kuaixietech.controller.Batch10RpcController.ProposeFee](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-proposefee): orderId:String, fee:BigDecimal, note:String | True | — |

返回 Java 类型：`String`。

## TradeRpcController

### `POST /api/rpc/quote_pk_award`

[TradeRpcController.java:34](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L34)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/quote_pk_award`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | AwardQuoteBody → [com.kuaixietech.controller.TradeRpcController.AwardQuoteBody](JAVA_TYPES.md#com-kuaixietech-controller-traderpccontroller-awardquotebody): demandId:String, quoteId:String, insuranceChoice:String, insuranceQuoteId:String | True | — |

返回 Java 类型：`Object`。

## Batch10RpcController

### `POST /api/rpc/record_arrival_proof`

[Batch10RpcController.java:50](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L50)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/record_arrival_proof`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | ArrivalProof → [com.kuaixietech.controller.Batch10RpcController.ArrivalProof](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-arrivalproof): tripId:String, snapshotUrl:String, lat:BigDecimal, lng:BigDecimal, city:String | True | — |

返回 Java 类型：`String`。

## Batch11RpcController

### `POST /api/rpc/record_boarding_proof`

[Batch11RpcController.java:26](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L26)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/record_boarding_proof`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | Boarding → [com.kuaixietech.controller.Batch11RpcController.Boarding](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-boarding): tripId:String, snapshotUrl:String, lat:BigDecimal, lng:BigDecimal, city:String | True | — |

返回 Java 类型：`String`。

### `POST /api/rpc/record_delivery_proof`

[Batch11RpcController.java:27](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L27)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/record_delivery_proof`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | Delivery → [com.kuaixietech.controller.Batch11RpcController.Delivery](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-delivery): orderId:String, faceId:String, lat:BigDecimal, lng:BigDecimal, city:String | True | — |

返回 Java 类型：`String`。

## KycRpcController

### `POST /api/rpc/record_face_verification`

[KycRpcController.java:28](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L28)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/record_face_verification`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | FaceRecord → [com.kuaixietech.controller.KycRpcController.FaceRecord](JAVA_TYPES.md#com-kuaixietech-controller-kycrpccontroller-facerecord): scene:String, snapshotUrl:String, deviceInfo:String, orderId:String, challengeCode:String | True | — |

返回 Java 类型：`String`。

## TradeRpcController

### `POST /api/rpc/refund_publish_deposit`

[TradeRpcController.java:38](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L38)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/refund_publish_deposit`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`String`。

## Batch11RpcController

### `POST /api/rpc/request_cancel_v2`

[Batch11RpcController.java:29](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L29)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/request_cancel_v2`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | Cancel → [com.kuaixietech.controller.Batch11RpcController.Cancel](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-cancel): orderId:String, reasonType:String, forceMajeure:boolean, note:String, proofUrls:String | True | — |

返回 Java 类型：`String`。

## Batch10RpcController

### `POST /api/rpc/request_invoice`

[Batch10RpcController.java:33](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L33)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/request_invoice`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | InvoiceRequest → [com.kuaixietech.controller.Batch10RpcController.InvoiceRequest](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-invoicerequest): orderId:String, titleType:String, titleName:String, taxNo:String, kind:String, email:String, remark:String | True | — |

返回 Java 类型：`String`。

## Batch11RpcController

### `POST /api/rpc/request_no_boarding_refund`

[Batch11RpcController.java:28](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L28)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/request_no_boarding_refund`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | OrderId → [com.kuaixietech.controller.Batch11RpcController.OrderId](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-orderid): orderId:String | True | — |

返回 Java 类型：`String`。

## Batch10RpcController

### `POST /api/rpc/respond_shipping_fee`

[Batch10RpcController.java:49](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L49)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/respond_shipping_fee`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | b | RespondFee → [com.kuaixietech.controller.Batch10RpcController.RespondFee](JAVA_TYPES.md#com-kuaixietech-controller-batch10rpccontroller-respondfee): orderId:String, accept:boolean, counter:BigDecimal, expectedFee:BigDecimal | True | — |

返回 Java 类型：`String`。

## KycRpcController

### `POST /api/rpc/submit_buyer_kyc`

[KycRpcController.java:16](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L16)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/submit_buyer_kyc`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | BuyerKyc → [com.kuaixietech.controller.KycRpcController.BuyerKyc](JAVA_TYPES.md#com-kuaixietech-controller-kycrpccontroller-buyerkyc): realName:String, idCardNo:String | True | — |

返回 Java 类型：`String`。

## TradeRpcController

### `POST /api/rpc/submit_pk_quote`

[TradeRpcController.java:25](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L25)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/submit_pk_quote`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | r | QuoteBody → [com.kuaixietech.controller.TradeRpcController.QuoteBody](JAVA_TYPES.md#com-kuaixietech-controller-traderpccontroller-quotebody): demandId:String, price:BigDecimal, promiseDays:Integer, purchasePlace:String, note:String | True | — |

返回 Java 类型：`String`。

## KycRpcController

### `POST /api/rpc/upsert_kyc_doc_with_challenge`

[KycRpcController.java:19](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L19)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/upsert_kyc_doc_with_challenge`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | body | KycDoc → [com.kuaixietech.controller.KycRpcController.KycDoc](JAVA_TYPES.md#com-kuaixietech-controller-kycrpccontroller-kycdoc): docType:String, storageUrl:String, challengeCode:String | True | — |

返回 Java 类型：`String`。

## Batch11RpcController

### `POST /api/rpc/verify_contact_code`

[Batch11RpcController.java:33](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L33)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/rpc/verify_contact_code`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| body | x | VerifyContact → [com.kuaixietech.controller.Batch11RpcController.VerifyContact](JAVA_TYPES.md#com-kuaixietech-controller-batch11rpccontroller-verifycontact): target:String, channel:String, code:String | True | — |

返回 Java 类型：`boolean`。

## SupportChatController

### `GET /api/support/admin/queue`

[SupportChatController.java:15](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L15)；administrator; service guard applies。

网关兼容路径：`/support/admin/queue`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').SupportQueueItem[]`；[client.ts:109](../../client/packages/sdk/src/client.ts#L109)。

### `POST /api/support/admin/{id}/claim`

[SupportChatController.java:17](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L17)；administrator; service guard applies。

网关兼容路径：`/support/admin/{id}/claim`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').SupportConversation`；[client.ts:111](../../client/packages/sdk/src/client.ts#L111)。

### `GET /api/support/admin/{id}/messages`

[SupportChatController.java:16](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L16)；administrator; service guard applies。

网关兼容路径：`/support/admin/{id}/messages`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 60 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').SupportMessage[]`；[client.ts:110](../../client/packages/sdk/src/client.ts#L110)。

### `POST /api/support/admin/{id}/reply`

[SupportChatController.java:18](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L18)；administrator; service guard applies。

网关兼容路径：`/support/admin/{id}/reply`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | b | SupportDtos.AgentReply → [com.kuaixietech.entity.communication.SupportDtos.AgentReply](JAVA_TYPES.md#com-kuaixietech-entity-communication-supportdtos-agentreply): content:String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`{status:string}`；[client.ts:112](../../client/packages/sdk/src/client.ts#L112)。

### `POST /api/support/conversations`

[SupportChatController.java:10](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L10)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/support/conversations`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| — | — | 无参数 | — | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').SupportConversation`；[client.ts:104](../../client/packages/sdk/src/client.ts#L104)。

### `GET /api/support/conversations/{id}`

[SupportChatController.java:11](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L11)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/support/conversations/{id}`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').SupportConversation`；[client.ts:105](../../client/packages/sdk/src/client.ts#L105)。

### `POST /api/support/conversations/{id}/handoff`

[SupportChatController.java:14](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L14)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/support/conversations/{id}/handoff`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').SupportConversation`；[client.ts:108](../../client/packages/sdk/src/client.ts#L108)。

### `GET /api/support/conversations/{id}/messages`

[SupportChatController.java:12](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L12)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/support/conversations/{id}/messages`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| query | limit | int | False | 60 |

返回 Java 类型：`List<Map<String,Object>>`。
SDK调用方声明：`import('./types').SupportMessage[]`；[client.ts:106](../../client/packages/sdk/src/client.ts#L106)。

### `POST /api/support/conversations/{id}/messages`

[SupportChatController.java:13](../../server/src/main/java/com/kuaixietech/controller/SupportChatController.java#L13)；authenticated; service ownership/admin guard applies。

网关兼容路径：`/support/conversations/{id}/messages`。

| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |
| --- | --- | --- | --- | --- |
| path | id | String | True | — |
| body | request | SupportDtos.Send → [com.kuaixietech.entity.communication.SupportDtos.Send](JAVA_TYPES.md#com-kuaixietech-entity-communication-supportdtos-send): content:String | True | — |
| header | Accept-Language | String | False | zh-CN |

返回 Java 类型：`Map<String,Object>`。
SDK调用方声明：`import('./types').SupportSendResult`；[client.ts:107](../../client/packages/sdk/src/client.ts#L107)。

## SDK调用但未找到控制器声明

0条调用（含模板条件分支，按调用点计数），不能作为可用接口。

| 方法 | 路径 | SDK源 |
| --- | --- | --- |

## 动态 SDK 路径

以下是拼接/动态路径，不能当作声明路由；RPC实际键见[RPC索引](RPC_INDEX.json)。

- `POST '/rpc/'+path.replace(/^\//,'')`；[client.ts:241](../../client/packages/sdk/src/client.ts#L241)。
