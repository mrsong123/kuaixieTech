# Java 请求与响应字段

从controller参数与返回值追踪的record/enum，保留字段类型及声明约束。`@Valid`仅在对应入口声明时触发Bean Validation；service还可能校验条件必填/权限/状态。未标@NotNull不等于业务允许空值。金额精度、UUID和状态以附列注解及业务章节为准。

<a id="com-kuaixietech-aftersalesfunds-aftersalesescrowfunds-execute"></a>
## com.kuaixietech.aftersalesfunds.AftersalesEscrowFunds.Execute

[AftersalesEscrowFunds.java:17](../../server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowFunds.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| requestId | String | @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| expectedRound | int | @Min(1) |
| note | String | @NotBlank @Size(max=200) |

<a id="com-kuaixietech-cases-casemodels-appeal"></a>
## com.kuaixietech.cases.CaseModels.Appeal

[CaseModels.java:16](../../server/src/main/java/com/kuaixietech/cases/CaseModels.java#L16)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| reason | String | @NotBlank @Size(max=4000) |
| expectedRound | int | @Min(1) |
| requestId | String | @NotBlank |

<a id="com-kuaixietech-cases-casemodels-create"></a>
## com.kuaixietech.cases.CaseModels.Create

[CaseModels.java:7](../../server/src/main/java/com/kuaixietech/cases/CaseModels.java#L7)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | @NotBlank |
| type | String | @NotBlank @Pattern(regexp="quality\|wrong_item\|missing\|damaged") |
| description | String | @NotBlank @Size(max=4000) |
| expectResolution | String | @NotBlank @Pattern(regexp="refund_part\|refund_full\|resend\|compensate") |
| refundAmount | BigDecimal | @DecimalMin("0.01") @Digits(integer=10,fraction=2) |
| evidenceUrls | List< String> | @NotNull @Size(max=4) @NotBlank @Size(max=1000) |
| requestId | String | @NotBlank |

<a id="com-kuaixietech-cases-casemodels-decision"></a>
## com.kuaixietech.cases.CaseModels.Decision

[CaseModels.java:13](../../server/src/main/java/com/kuaixietech/cases/CaseModels.java#L13)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| outcome | String | @NotBlank @Pattern(regexp="reject\|refund_part\|refund_full\|resend\|compensate") |
| refundAmount | BigDecimal | @DecimalMin("0.01") @Digits(integer=10,fraction=2) |
| reason | String | @NotBlank @Size(max=1000) |
| depositOwner | String | @NotBlank @Pattern(regexp="return_original\|buyer\|vendor\|none") |
| expectedRound | int | @Min(1) |
| requestId | String | @NotBlank |

<a id="com-kuaixietech-cases-casemodels-escalate"></a>
## com.kuaixietech.cases.CaseModels.Escalate

[CaseModels.java:12](../../server/src/main/java/com/kuaixietech/cases/CaseModels.java#L12)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| reason | String | @NotBlank @Size(max=1000) |
| expectedRound | int | @Min(1) |
| requestId | String | @NotBlank |

<a id="com-kuaixietech-cases-casemodels-finish"></a>
## com.kuaixietech.cases.CaseModels.Finish

[CaseModels.java:17](../../server/src/main/java/com/kuaixietech/cases/CaseModels.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| note | String | @NotBlank @Size(max=1000) |
| expectedRound | int | @Min(1) |
| requestId | String | @NotBlank |

<a id="com-kuaixietech-cases-casemodels-respond"></a>
## com.kuaixietech.cases.CaseModels.Respond

[CaseModels.java:11](../../server/src/main/java/com/kuaixietech/cases/CaseModels.java#L11)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| note | String | @NotBlank @Size(max=1024) |
| evidenceUrls | List< String> | @NotNull @Size(max=4) @NotBlank @Size(max=1000) |
| expectedRound | int | @Min(1) |
| requestId | String | @NotBlank |

<a id="com-kuaixietech-commerce-productcheckoutmodels-input"></a>
## com.kuaixietech.commerce.ProductCheckoutModels.Input

[ProductCheckoutModels.java:7](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutModels.java#L7)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| productId | String | @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| tripId | String | @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| quantity | Integer | @NotNull @Min(1) @Max(100) |
| contactName | String | @NotBlank @Size(max=64) |
| contactPhone | String | @NotBlank @Pattern(regexp="\\+?[0-9 -]{6,24}") |
| address | String | @NotBlank @Size(max=512) |
| expectDate | LocalDate | — |
| note | String | @Size(max=1024) |
| insuranceChoice | String | @NotBlank @Pattern(regexp="yes\|waived") |
| insuranceQuoteId | String | @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| requestId | String | @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| expectedUnitPrice | BigDecimal | @DecimalMin("0.01") @Digits(integer=10,fraction=2) |
| expectedBuyerEscrow | BigDecimal | @DecimalMin("0.01") @Digits(integer=10,fraction=2) |
| expectedProductFingerprint | String | @Pattern(regexp="[a-f0-9]{64}") |

<a id="com-kuaixietech-commerce-productcheckoutmodels-quote"></a>
## com.kuaixietech.commerce.ProductCheckoutModels.Quote

[ProductCheckoutModels.java:19](../../server/src/main/java/com/kuaixietech/commerce/ProductCheckoutModels.java#L19)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| productId | String | — |
| tripId | String | — |
| storeId | String | — |
| itemName | String | — |
| itemSpec | String | — |
| unitPrice | String | — |
| quantity | int | — |
| goodsAmount | String | — |
| serviceFee | String | — |
| insurancePremium | String | — |
| buyerEscrow | String | — |
| vendorDeposit | String | — |
| currency | String | — |
| pricingVersion | String | — |
| intentHours | int | — |
| productFingerprint | String | — |

<a id="com-kuaixietech-controller-accountcentrecontroller-profileupdate"></a>
## com.kuaixietech.controller.AccountCentreController.ProfileUpdate

[AccountCentreController.java:13](../../server/src/main/java/com/kuaixietech/controller/AccountCentreController.java#L13)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| nickname | String | — |
| city | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-banbody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.BanBody

[AdminConsoleRpcController.java:25](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L25)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| userId | String | — |
| ban | boolean | — |
| reason | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-broadcastbody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.BroadcastBody

[AdminConsoleRpcController.java:26](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L26)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| title | String | — |
| body | String | — |
| scope | String | — |
| userIds | List<String> | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-categorycreate"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.CategoryCreate

[AdminConsoleRpcController.java:30](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L30)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| name | String | — |
| parentId | Long | — |
| level | Integer | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-categoryid"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.CategoryId

[AdminConsoleRpcController.java:31](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L31)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | Long | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-channeltoggle"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.ChannelToggle

[AdminConsoleRpcController.java:35](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L35)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| enabled | boolean | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-demandclosebody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.DemandCloseBody

[AdminConsoleRpcController.java:27](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L27)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| demandId | String | — |
| reason | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-errorid"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.ErrorId

[AdminConsoleRpcController.java:37](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L37)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | Long | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-idbody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.IdBody

[AdminConsoleRpcController.java:24](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L24)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-invitebody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.InviteBody

[AdminConsoleRpcController.java:32](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L32)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| code | String | — |
| note | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-listbody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.ListBody

[AdminConsoleRpcController.java:20](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L20)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| kind | String | — |
| limit | Integer | — |
| keyword | String | — |
| offset | Integer | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-poolformbody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.PoolFormBody

[AdminConsoleRpcController.java:29](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L29)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| tripId | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-postremove"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.PostRemove

[AdminConsoleRpcController.java:36](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L36)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| postId | String | — |
| reason | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-recipebody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.RecipeBody

[AdminConsoleRpcController.java:33](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L33)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| tripType | String | — |
| transportMode | String | — |
| countryCode | String | — |
| items | String | — |
| note | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-recipetoggle"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.RecipeToggle

[AdminConsoleRpcController.java:34](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L34)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| active | boolean | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-scorebody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.ScoreBody

[AdminConsoleRpcController.java:28](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L28)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| userId | String | — |
| delta | Integer | — |
| note | String | — |

<a id="com-kuaixietech-controller-adminconsolerpccontroller-verifybody"></a>
## com.kuaixietech.controller.AdminConsoleRpcController.VerifyBody

[AdminConsoleRpcController.java:23](../../server/src/main/java/com/kuaixietech/controller/AdminConsoleRpcController.java#L23)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| userId | String | — |
| tripId | String | — |
| approve | boolean | — |
| note | String | — |

<a id="com-kuaixietech-controller-adminfinancerpccontroller-balancebody"></a>
## com.kuaixietech.controller.AdminFinanceRpcController.BalanceBody

[AdminFinanceRpcController.java:13](../../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java#L13)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| userId | String | — |
| delta | BigDecimal | — |
| note | String | — |

<a id="com-kuaixietech-controller-adminfinancerpccontroller-limitbody"></a>
## com.kuaixietech.controller.AdminFinanceRpcController.LimitBody

[AdminFinanceRpcController.java:15](../../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java#L15)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| limit | Integer | — |

<a id="com-kuaixietech-controller-adminfinancerpccontroller-poolbody"></a>
## com.kuaixietech.controller.AdminFinanceRpcController.PoolBody

[AdminFinanceRpcController.java:14](../../server/src/main/java/com/kuaixietech/controller/AdminFinanceRpcController.java#L14)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| tripId | String | — |
| reason | String | — |

<a id="com-kuaixietech-controller-authlogincontroller-helpbody"></a>
## com.kuaixietech.controller.AuthLoginController.HelpBody

[AuthLoginController.java:26](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L26)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| channel | String | — |
| contact | String | — |
| countryCode | String | — |
| captchaId | String | — |
| captchaCode | String | — |

<a id="com-kuaixietech-controller-authlogincontroller-loginbody"></a>
## com.kuaixietech.controller.AuthLoginController.LoginBody

[AuthLoginController.java:21](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L21)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| channel | String | — |
| contact | String | — |
| password | String | — |
| code | String | — |
| countryCode | String | — |

<a id="com-kuaixietech-controller-authlogincontroller-registerbody"></a>
## com.kuaixietech.controller.AuthLoginController.RegisterBody

[AuthLoginController.java:25](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L25)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| channel | String | — |
| contact | String | — |
| countryCode | String | — |
| password | String | — |
| code | String | — |
| role | String | — |
| inviteCode | String | — |
| enterpriseName | String | — |
| businessLicenseNo | String | — |

<a id="com-kuaixietech-controller-authlogincontroller-resetbody"></a>
## com.kuaixietech.controller.AuthLoginController.ResetBody

[AuthLoginController.java:24](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L24)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| channel | String | — |
| contact | String | — |
| countryCode | String | — |
| code | String | — |
| newPassword | String | — |

<a id="com-kuaixietech-controller-authlogincontroller-sendcodebody"></a>
## com.kuaixietech.controller.AuthLoginController.SendCodeBody

[AuthLoginController.java:23](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L23)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| channel | String | — |
| contact | String | — |
| countryCode | String | — |
| purpose | String | — |
| captchaId | String | — |
| captchaCode | String | — |

<a id="com-kuaixietech-controller-authlogincontroller-wechatbody"></a>
## com.kuaixietech.controller.AuthLoginController.WechatBody

[AuthLoginController.java:22](../../server/src/main/java/com/kuaixietech/controller/AuthLoginController.java#L22)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| code | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-arrivalproof"></a>
## com.kuaixietech.controller.Batch10RpcController.ArrivalProof

[Batch10RpcController.java:32](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L32)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| tripId | String | — |
| snapshotUrl | String | — |
| lat | BigDecimal | — |
| lng | BigDecimal | — |
| city | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-confirmmeetup"></a>
## com.kuaixietech.controller.Batch10RpcController.ConfirmMeetup

[Batch10RpcController.java:29](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L29)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| code | String | — |
| lat | BigDecimal | — |
| lng | BigDecimal | — |
| city | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-enterprisedecision"></a>
## com.kuaixietech.controller.Batch10RpcController.EnterpriseDecision

[Batch10RpcController.java:21](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L21)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| userId | String | — |
| approve | boolean | — |
| note | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-evidenceread"></a>
## com.kuaixietech.controller.Batch10RpcController.EvidenceRead

[Batch10RpcController.java:27](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L27)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-grantpoints"></a>
## com.kuaixietech.controller.Batch10RpcController.GrantPoints

[Batch10RpcController.java:22](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L22)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| userId | String | — |
| points | Integer | — |
| note | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-invoicedecision"></a>
## com.kuaixietech.controller.Batch10RpcController.InvoiceDecision

[Batch10RpcController.java:18](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L18)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| invoiceId | String | — |
| approve | boolean | — |
| invoiceNo | String | — |
| fileUrl | String | — |
| reason | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-invoicerequest"></a>
## com.kuaixietech.controller.Batch10RpcController.InvoiceRequest

[Batch10RpcController.java:17](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| titleType | String | — |
| titleName | String | — |
| taxNo | String | — |
| kind | String | — |
| email | String | — |
| remark | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-issuemeetup"></a>
## com.kuaixietech.controller.Batch10RpcController.IssueMeetup

[Batch10RpcController.java:28](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L28)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-localeremove"></a>
## com.kuaixietech.controller.Batch10RpcController.LocaleRemove

[Batch10RpcController.java:25](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L25)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| code | String | — |
| note | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-localetoggle"></a>
## com.kuaixietech.controller.Batch10RpcController.LocaleToggle

[Batch10RpcController.java:24](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L24)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| code | String | — |
| active | boolean | — |
| note | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-localeupsert"></a>
## com.kuaixietech.controller.Batch10RpcController.LocaleUpsert

[Batch10RpcController.java:23](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L23)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| code | String | — |
| nativeName | String | — |
| labelZh | String | — |
| sort | Integer | — |
| fallback | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-privacycall"></a>
## com.kuaixietech.controller.Batch10RpcController.PrivacyCall

[Batch10RpcController.java:26](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L26)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| result | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-proposefee"></a>
## com.kuaixietech.controller.Batch10RpcController.ProposeFee

[Batch10RpcController.java:30](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L30)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| fee | BigDecimal | — |
| note | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-respondfee"></a>
## com.kuaixietech.controller.Batch10RpcController.RespondFee

[Batch10RpcController.java:31](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L31)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| accept | boolean | — |
| counter | BigDecimal | — |
| expectedFee | BigDecimal | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-rowid"></a>
## com.kuaixietech.controller.Batch10RpcController.RowId

[Batch10RpcController.java:20](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L20)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |

<a id="com-kuaixietech-controller-batch10rpccontroller-violationquery"></a>
## com.kuaixietech.controller.Batch10RpcController.ViolationQuery

[Batch10RpcController.java:19](../../server/src/main/java/com/kuaixietech/controller/Batch10RpcController.java#L19)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| limit | Integer | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-boarding"></a>
## com.kuaixietech.controller.Batch11RpcController.Boarding

[Batch11RpcController.java:16](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L16)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| tripId | String | — |
| snapshotUrl | String | — |
| lat | BigDecimal | — |
| lng | BigDecimal | — |
| city | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-cancel"></a>
## com.kuaixietech.controller.Batch11RpcController.Cancel

[Batch11RpcController.java:19](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L19)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| reasonType | String | — |
| forceMajeure | boolean | — |
| note | String | — |
| proofUrls | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-decision"></a>
## com.kuaixietech.controller.Batch11RpcController.Decision

[Batch11RpcController.java:20](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L20)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| approve | boolean | — |
| reason | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-delivery"></a>
## com.kuaixietech.controller.Batch11RpcController.Delivery

[Batch11RpcController.java:17](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| faceId | String | — |
| lat | BigDecimal | — |
| lng | BigDecimal | — |
| city | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-invitecode"></a>
## com.kuaixietech.controller.Batch11RpcController.InviteCode

[Batch11RpcController.java:23](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L23)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| code | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-orderid"></a>
## com.kuaixietech.controller.Batch11RpcController.OrderId

[Batch11RpcController.java:18](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L18)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-peerid"></a>
## com.kuaixietech.controller.Batch11RpcController.PeerId

[Batch11RpcController.java:21](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L21)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| target | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-purchase"></a>
## com.kuaixietech.controller.Batch11RpcController.Purchase

[Batch11RpcController.java:14](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L14)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| evidence | String | — |
| lat | BigDecimal | — |
| lng | BigDecimal | — |
| city | String | — |
| address | String | — |
| meta | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-shipment"></a>
## com.kuaixietech.controller.Batch11RpcController.Shipment

[Batch11RpcController.java:15](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L15)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| carrier | String | — |
| tracking | String | — |
| labelUrls | String | — |
| lat | BigDecimal | — |
| lng | BigDecimal | — |
| city | String | — |
| address | String | — |

<a id="com-kuaixietech-controller-batch11rpccontroller-verifycontact"></a>
## com.kuaixietech.controller.Batch11RpcController.VerifyContact

[Batch11RpcController.java:22](../../server/src/main/java/com/kuaixietech/controller/Batch11RpcController.java#L22)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| target | String | — |
| channel | String | — |
| code | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-assistid"></a>
## com.kuaixietech.controller.Batch12RpcController.AssistId

[Batch12RpcController.java:29](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L29)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-callid"></a>
## com.kuaixietech.controller.Batch12RpcController.CallId

[Batch12RpcController.java:32](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L32)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| callId | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-carrier"></a>
## com.kuaixietech.controller.Batch12RpcController.Carrier

[Batch12RpcController.java:36](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L36)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| realName | String | — |
| idCardNo | String | — |
| passportNo | String | — |
| phoneCode | String | — |
| inviteCode | String | — |
| note | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-comment"></a>
## com.kuaixietech.controller.Batch12RpcController.Comment

[Batch12RpcController.java:34](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L34)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| postId | String | — |
| content | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-createassist"></a>
## com.kuaixietech.controller.Batch12RpcController.CreateAssist

[Batch12RpcController.java:28](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L28)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| rate | BigDecimal | — |
| note | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-enterprise"></a>
## com.kuaixietech.controller.Batch12RpcController.Enterprise

[Batch12RpcController.java:35](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L35)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| enterpriseName | String | — |
| businessLicenseNo | String | — |
| proofUrls | List<String> | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-handover"></a>
## com.kuaixietech.controller.Batch12RpcController.Handover

[Batch12RpcController.java:30](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L30)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| urlsJson | String | — |
| city | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-matchjoin"></a>
## com.kuaixietech.controller.Batch12RpcController.MatchJoin

[Batch12RpcController.java:37](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L37)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| applicationId | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-orderid"></a>
## com.kuaixietech.controller.Batch12RpcController.OrderId

[Batch12RpcController.java:31](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L31)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-rejectcall"></a>
## com.kuaixietech.controller.Batch12RpcController.RejectCall

[Batch12RpcController.java:33](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L33)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| callId | String | — |
| asCallee | boolean | — |

<a id="com-kuaixietech-controller-batch12rpccontroller-verifycode"></a>
## com.kuaixietech.controller.Batch12RpcController.VerifyCode

[Batch12RpcController.java:38](../../server/src/main/java/com/kuaixietech/controller/Batch12RpcController.java#L38)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| target | String | — |
| channel | String | — |

<a id="com-kuaixietech-controller-identitysettingscontroller-changepassword"></a>
## com.kuaixietech.controller.IdentitySettingsController.ChangePassword

[IdentitySettingsController.java:15](../../server/src/main/java/com/kuaixietech/controller/IdentitySettingsController.java#L15)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| oldPassword | String | — |
| newPassword | String | — |

<a id="com-kuaixietech-controller-identitysettingscontroller-rebindcontact"></a>
## com.kuaixietech.controller.IdentitySettingsController.RebindContact

[IdentitySettingsController.java:17](../../server/src/main/java/com/kuaixietech/controller/IdentitySettingsController.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| channel | String | — |
| newTarget | String | — |
| code | String | — |

<a id="com-kuaixietech-controller-identitysettingscontroller-resetownpassword"></a>
## com.kuaixietech.controller.IdentitySettingsController.ResetOwnPassword

[IdentitySettingsController.java:16](../../server/src/main/java/com/kuaixietech/controller/IdentitySettingsController.java#L16)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| channel | String | — |
| target | String | — |
| code | String | — |
| newPassword | String | — |

<a id="com-kuaixietech-controller-invitereadrpccontroller-code"></a>
## com.kuaixietech.controller.InviteReadRpcController.Code

[InviteReadRpcController.java:13](../../server/src/main/java/com/kuaixietech/controller/InviteReadRpcController.java#L13)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| code | String | — |

<a id="com-kuaixietech-controller-invitereadrpccontroller-operator"></a>
## com.kuaixietech.controller.InviteReadRpcController.Operator

[InviteReadRpcController.java:14](../../server/src/main/java/com/kuaixietech/controller/InviteReadRpcController.java#L14)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| operatorId | String | — |

<a id="com-kuaixietech-controller-invitereadrpccontroller-root"></a>
## com.kuaixietech.controller.InviteReadRpcController.Root

[InviteReadRpcController.java:12](../../server/src/main/java/com/kuaixietech/controller/InviteReadRpcController.java#L12)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| root | String | — |

<a id="com-kuaixietech-controller-kycrpccontroller-buyerkyc"></a>
## com.kuaixietech.controller.KycRpcController.BuyerKyc

[KycRpcController.java:11](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L11)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| realName | String | — |
| idCardNo | String | — |

<a id="com-kuaixietech-controller-kycrpccontroller-faceissue"></a>
## com.kuaixietech.controller.KycRpcController.FaceIssue

[KycRpcController.java:14](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L14)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| scene | String | — |
| orderId | String | — |

<a id="com-kuaixietech-controller-kycrpccontroller-facerecord"></a>
## com.kuaixietech.controller.KycRpcController.FaceRecord

[KycRpcController.java:15](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L15)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| scene | String | — |
| snapshotUrl | String | — |
| deviceInfo | String | — |
| orderId | String | — |
| challengeCode | String | — |

<a id="com-kuaixietech-controller-kycrpccontroller-kycdoc"></a>
## com.kuaixietech.controller.KycRpcController.KycDoc

[KycRpcController.java:12](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L12)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| docType | String | — |
| storageUrl | String | — |
| challengeCode | String | — |

<a id="com-kuaixietech-controller-kycrpccontroller-reviewkyc"></a>
## com.kuaixietech.controller.KycRpcController.ReviewKyc

[KycRpcController.java:13](../../server/src/main/java/com/kuaixietech/controller/KycRpcController.java#L13)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| userId | String | — |
| approve | boolean | — |
| reason | String | — |

<a id="com-kuaixietech-controller-traderpccontroller-acceptorderbody"></a>
## com.kuaixietech.controller.TradeRpcController.AcceptOrderBody

[TradeRpcController.java:16](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L16)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | — |
| deposit | BigDecimal | — |

<a id="com-kuaixietech-controller-traderpccontroller-awardbody"></a>
## com.kuaixietech.controller.TradeRpcController.AwardBody

[TradeRpcController.java:18](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L18)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| demandId | String | — |
| quoteId | String | — |
| insuranceChoice | String | — |
| expectedBuyerEscrow | BigDecimal | — |
| insuranceQuoteId | String | — |

<a id="com-kuaixietech-controller-traderpccontroller-awardquotebody"></a>
## com.kuaixietech.controller.TradeRpcController.AwardQuoteBody

[TradeRpcController.java:19](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L19)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| demandId | String | — |
| quoteId | String | — |
| insuranceChoice | String | — |
| insuranceQuoteId | String | — |

<a id="com-kuaixietech-controller-traderpccontroller-demandbody"></a>
## com.kuaixietech.controller.TradeRpcController.DemandBody

[TradeRpcController.java:21](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L21)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| demandId | String | — |

<a id="com-kuaixietech-controller-traderpccontroller-publishbody"></a>
## com.kuaixietech.controller.TradeRpcController.PublishBody

[TradeRpcController.java:20](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L20)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| amount | BigDecimal | — |
| source | String | — |
| orderId | String | — |

<a id="com-kuaixietech-controller-traderpccontroller-quotebody"></a>
## com.kuaixietech.controller.TradeRpcController.QuoteBody

[TradeRpcController.java:17](../../server/src/main/java/com/kuaixietech/controller/TradeRpcController.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| demandId | String | — |
| price | BigDecimal | — |
| promiseDays | Integer | — |
| purchasePlace | String | — |
| note | String | — |

<a id="com-kuaixietech-coupler-couplermodels-mode"></a>
## com.kuaixietech.coupler.CouplerModels.Mode

[CouplerModels.java:7](../../server/src/main/java/com/kuaixietech/coupler/CouplerModels.java#L7)；enum。

值：`SANDBOX`, `LIVE`。

<a id="com-kuaixietech-coupler-couplermodels-runtime"></a>
## com.kuaixietech.coupler.CouplerModels.Runtime

[CouplerModels.java:8](../../server/src/main/java/com/kuaixietech/coupler/CouplerModels.java#L8)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| mode | Mode | — |
| revision | long | — |

<a id="com-kuaixietech-coupler-couplermodels-save"></a>
## com.kuaixietech.coupler.CouplerModels.Save

[CouplerModels.java:10](../../server/src/main/java/com/kuaixietech/coupler/CouplerModels.java#L10)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| values | Map< String, String> | @NotNull @Size(max=80) @NotBlank @NotNull @Size(max=16000) |
| clearFields | List<String> | @NotNull @Size(max=80) |
| enabled | boolean | — |
| selected | boolean | — |
| expectedRevision | long | @Min(0) |

<a id="com-kuaixietech-coupler-couplermodels-switch"></a>
## com.kuaixietech.coupler.CouplerModels.Switch

[CouplerModels.java:9](../../server/src/main/java/com/kuaixietech/coupler/CouplerModels.java#L9)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| mode | Mode | @NotNull |
| expectedRevision | long | @Min(0) |

<a id="com-kuaixietech-entity-communication-supportdtos-agentreply"></a>
## com.kuaixietech.entity.communication.SupportDtos.AgentReply

[SupportDtos.java:6](../../server/src/main/java/com/kuaixietech/entity/communication/SupportDtos.java#L6)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| content | String | @NotBlank @Size(max=2000) |

<a id="com-kuaixietech-entity-communication-supportdtos-send"></a>
## com.kuaixietech.entity.communication.SupportDtos.Send

[SupportDtos.java:5](../../server/src/main/java/com/kuaixietech/entity/communication/SupportDtos.java#L5)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| content | String | @NotBlank @Size(max=2000) |

<a id="com-kuaixietech-entity-portal-intentinput"></a>
## com.kuaixietech.entity.portal.IntentInput

[IntentInput.java:8](../../server/src/main/java/com/kuaixietech/entity/portal/IntentInput.java#L8)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| tripId | String | @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| itemName | String | @NotBlank @Size(max=120) |
| itemSpec | String | @Size(max=160) |
| quantity | Integer | @NotNull @Min(1) @Max(100) |
| unit | String | @NotBlank @Pattern(regexp="piece\|box\|bottle\|kg") |
| categoryId | String | @Pattern(regexp="[0-9]{1,18}") |
| expectUnitPrice | BigDecimal | @NotNull @DecimalMin("0.01") @DecimalMax("1000000") @Digits(integer=7,fraction=2) |
| note | String | @Size(max=1024) |
| contactName | String | @NotBlank @Size(max=64) |
| contactPhone | String | @NotBlank @Pattern(regexp="\\+?[0-9 -]{6,24}") |
| address | String | @NotBlank @Size(max=512) |
| expectDate | LocalDate | — |
| insuranceChoice | String | @NotBlank @Pattern(regexp="yes\|waived") |
| requestId | String | @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| expectedBuyerEscrow | BigDecimal | @DecimalMin("0.01") @Digits(integer=8,fraction=2) |
| insuranceQuoteId | String | @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |

<a id="com-kuaixietech-entity-portal-portaldtos-demandcreate"></a>
## com.kuaixietech.entity.portal.PortalDtos.DemandCreate

[PortalDtos.java:9](../../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java#L9)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| title | String | @NotBlank @Size(max=200) |
| description | String | @Size(max=4000) |
| city | String | @Size(max=64) |
| expectPrice | BigDecimal | @DecimalMin("0.0") |
| tripType | String | @Pattern(regexp="any\|domestic\|international") |

<a id="com-kuaixietech-entity-portal-portaldtos-reviewdemand"></a>
## com.kuaixietech.entity.portal.PortalDtos.ReviewDemand

[PortalDtos.java:19](../../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java#L19)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| demandId | String | @NotBlank |
| approved | boolean | — |

<a id="com-kuaixietech-entity-portal-portaldtos-reviewpost"></a>
## com.kuaixietech.entity.portal.PortalDtos.ReviewPost

[PortalDtos.java:18](../../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java#L18)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| postId | String | @NotBlank |
| approved | boolean | — |

<a id="com-kuaixietech-entity-portal-portaldtos-reviewtrip"></a>
## com.kuaixietech.entity.portal.PortalDtos.ReviewTrip

[PortalDtos.java:20](../../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java#L20)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| tripId | String | @NotBlank |
| approved | boolean | — |

<a id="com-kuaixietech-entity-portal-portaldtos-socialpostcreate"></a>
## com.kuaixietech.entity.portal.PortalDtos.SocialPostCreate

[PortalDtos.java:16](../../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java#L16)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| caption | String | @NotBlank @Size(max=4000) |
| mediaKind | String | @Pattern(regexp="text\|image\|video") |
| mediaUrl | String | @Size(max=800) |
| posterUrl | String | @Size(max=800) |

<a id="com-kuaixietech-entity-portal-portaldtos-tripcreate"></a>
## com.kuaixietech.entity.portal.PortalDtos.TripCreate

[PortalDtos.java:11](../../server/src/main/java/com/kuaixietech/entity/portal/PortalDtos.java#L11)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| destination | String | @NotBlank @Size(max=128) |
| originCity | String | @NotBlank @Size(max=64) |
| departDate | String | @NotBlank |
| returnDate | String | @NotBlank |
| tripType | String | @Pattern(regexp="domestic\|international") |
| transportMode | String | @Pattern(regexp="road\|rail\|air\|other") |
| capacityQty | Integer | @Min(1) @Max(100) |

<a id="com-kuaixietech-insurance-insurancemodels-application"></a>
## com.kuaixietech.insurance.InsuranceModels.Application

[InsuranceModels.java:33](../../server/src/main/java/com/kuaixietech/insurance/InsuranceModels.java#L33)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| orderId | String | — |
| status | String | — |
| insurerName | String | — |
| productName | String | — |
| premium | String | — |
| currency | String | — |
| checkoutUrl | String | — |
| policyNumber | String | — |
| certificateUrl | String | — |
| claimsUrl | String | — |
| coverageRef | String | — |
| termsVersion | String | — |
| startsAt | Instant | — |
| endsAt | Instant | — |
| platform | String | — |
| paymentMode | String | — |
| protectionStatus | String | — |

<a id="com-kuaixietech-insurance-insurancemodels-applyinput"></a>
## com.kuaixietech.insurance.InsuranceModels.ApplyInput

[InsuranceModels.java:23](../../server/src/main/java/com/kuaixietech/insurance/InsuranceModels.java#L23)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| orderId | String | @NotBlank |
| quoteId | String | @NotBlank |
| requestId | String | @NotBlank @Pattern(regexp="[a-fA-F0-9-]{36}") |
| termsAccepted | boolean | @AssertTrue |
| disclosuresAccepted | boolean | @AssertTrue |

<a id="com-kuaixietech-insurance-insurancemodels-productinput"></a>
## com.kuaixietech.insurance.InsuranceModels.ProductInput

[InsuranceModels.java:11](../../server/src/main/java/com/kuaixietech/insurance/InsuranceModels.java#L11)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | @Pattern(regexp="[a-zA-Z0-9_-]{1,64}") |
| insurerCode | String | @NotBlank @Size(max=64) |
| insurerName | String | @NotBlank @Size(max=200) |
| name | String | @NotBlank @Size(max=200) |
| kind | String | @NotBlank @Size(max=64) |
| adapter | String | @NotBlank @Size(max=64) |
| providerProductCode | String | @NotBlank @Size(max=128) |
| credentialPrefix | String | @NotBlank @Pattern(regexp="[A-Z][A-Z0-9_]{2,39}") |
| termsUrl | String | @NotBlank @Size(max=1000) |
| exclusionsUrl | String | @NotBlank @Size(max=1000) |
| claimsUrl | String | @NotBlank @Size(max=1000) |
| serviceHosts | Set< String> | @NotNull @Size(min=1,max=10) @Pattern(regexp="[a-z0-9.-]{1,253}") |
| checkoutHosts | Set< String> | @NotNull @Size(min=1,max=10) @Pattern(regexp="[a-z0-9.-]{1,253}") |
| enabled | boolean | — |

<a id="com-kuaixietech-insurance-insurancemodels-quote"></a>
## com.kuaixietech.insurance.InsuranceModels.Quote

[InsuranceModels.java:20](../../server/src/main/java/com/kuaixietech/insurance/InsuranceModels.java#L20)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| productId | String | — |
| insurerName | String | — |
| productName | String | — |
| kind | String | — |
| declaredValue | String | — |
| premium | String | — |
| currency | String | — |
| termsUrl | String | — |
| exclusionsUrl | String | — |
| claimsUrl | String | — |
| termsVersion | String | — |
| expiresAt | Instant | — |
| paymentMode | String | — |
| protectionStatus | String | — |

<a id="com-kuaixietech-insurance-insurancemodels-quoteinput"></a>
## com.kuaixietech.insurance.InsuranceModels.QuoteInput

[InsuranceModels.java:18](../../server/src/main/java/com/kuaixietech/insurance/InsuranceModels.java#L18)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| productId | String | @NotBlank |
| subjectType | String | @NotBlank @Pattern(regexp="trip\|pk_quote\|order") |
| subjectId | String | @NotBlank |
| itemName | String | @NotBlank @Size(max=255) |
| declaredValue | String | @NotBlank @Pattern(regexp="[0-9]{1,7}(\\.[0-9]{1,2})?") |
| platform | String | @Pattern(regexp="web\|h5\|app\|mp_weixin") |

<a id="com-kuaixietech-integration-integrationconfiguration-input"></a>
## com.kuaixietech.integration.IntegrationConfiguration.Input

[IntegrationConfiguration.java:15](../../server/src/main/java/com/kuaixietech/integration/IntegrationConfiguration.java#L15)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| enabled | boolean | — |
| remark | String | @Size(max=200) |
| values | Map< String, String> | @NotNull @Size(max=30) @NotBlank @NotNull @Size(max=16000) |
| clearFields | List<String> | @NotNull @Size(max=30) |

<a id="com-kuaixietech-payments-paymentmodels-checkout"></a>
## com.kuaixietech.payments.PaymentModels.Checkout

[PaymentModels.java:28](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java#L28)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| kind | String | — |
| value | String | — |

<a id="com-kuaixietech-payments-paymentmodels-confirminput"></a>
## com.kuaixietech.payments.PaymentModels.ConfirmInput

[PaymentModels.java:24](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java#L24)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| quoteId | String | @NotBlank |
| requestId | String | @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |

<a id="com-kuaixietech-payments-paymentmodels-fxinput"></a>
## com.kuaixietech.payments.PaymentModels.FxInput

[PaymentModels.java:17](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| name | String | @NotBlank @Size(max=80) |
| kind | String | @NotBlank |
| endpoint | String | — |
| credentialPrefix | String | @Pattern(regexp="[A-Z][A-Z0-9_]{2,39}") |
| maxAgeSeconds | int | @Min(60) @Max(172800) |
| enabled | boolean | — |
| transactionEnabled | boolean | — |

<a id="com-kuaixietech-payments-paymentmodels-merchantinput"></a>
## com.kuaixietech.payments.PaymentModels.MerchantInput

[PaymentModels.java:10](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java#L10)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| name | String | @NotBlank @Size(max=80) |
| adapter | String | @NotBlank |
| method | String | @NotBlank |
| platform | String | @NotBlank |
| currencies | List<String> | @NotEmpty |
| credentialPrefix | String | @Pattern(regexp="[A-Z][A-Z0-9_]{2,39}") |
| spreadBps | int | @Min(0) @Max(2000) |
| quoteTtlSeconds | int | @Min(30) @Max(900) |
| enabled | boolean | — |
| settings | Map<String,String> | — |

<a id="com-kuaixietech-payments-paymentmodels-payment"></a>
## com.kuaixietech.payments.PaymentModels.Payment

[PaymentModels.java:29](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java#L29)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| status | String | — |
| baseCurrency | String | — |
| baseAmount | String | — |
| paymentCurrency | String | — |
| paymentAmount | String | — |
| merchantName | String | — |
| checkout | Checkout | — |

<a id="com-kuaixietech-payments-paymentmodels-quote"></a>
## com.kuaixietech.payments.PaymentModels.Quote

[PaymentModels.java:25](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java#L25)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| id | String | — |
| merchantId | String | — |
| merchantName | String | — |
| method | String | — |
| platform | String | — |
| baseCurrency | String | — |
| baseAmount | String | — |
| paymentCurrency | String | — |
| paymentAmount | String | — |
| rate | String | — |
| spreadBps | int | — |
| rateSource | String | — |
| rateAsOf | Instant | — |
| expiresAt | Instant | — |
| settlementCurrency | String | — |

<a id="com-kuaixietech-payments-paymentmodels-quoteinput"></a>
## com.kuaixietech.payments.PaymentModels.QuoteInput

[PaymentModels.java:22](../../server/src/main/java/com/kuaixietech/payments/PaymentModels.java#L22)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| merchantId | String | @NotBlank |
| paymentCurrency | String | @NotBlank |
| amountCny | String | @NotBlank @Pattern(regexp="[0-9]{1,7}(\\.[0-9]{1,2})?") |

<a id="com-kuaixietech-refunds-refundmodels-create"></a>
## com.kuaixietech.refunds.RefundModels.Create

[RefundModels.java:6](../../server/src/main/java/com/kuaixietech/refunds/RefundModels.java#L6)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| paymentId | String | @NotBlank @Pattern(regexp="[a-f0-9]{32}") |
| requestId | String | @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") |
| reason | String | @NotBlank @Size(max=200) |

<a id="com-kuaixietech-refunds-refundmodels-review"></a>
## com.kuaixietech.refunds.RefundModels.Review

[RefundModels.java:7](../../server/src/main/java/com/kuaixietech/refunds/RefundModels.java#L7)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| approve | boolean | — |
| reason | String | @NotBlank @Size(max=200) |

<a id="com-kuaixietech-service-account-impl-kycrpcmigrationservice-facechallenge"></a>
## com.kuaixietech.service.account.impl.KycRpcMigrationService.FaceChallenge

[KycRpcMigrationService.java:56](../../server/src/main/java/com/kuaixietech/service/account/impl/KycRpcMigrationService.java#L56)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| challengeId | String | — |
| code | String | — |

<a id="com-kuaixietech-service-account-impl-savedcommerceservice-addressinput"></a>
## com.kuaixietech.service.account.impl.SavedCommerceService.AddressInput

[SavedCommerceService.java:13](../../server/src/main/java/com/kuaixietech/service/account/impl/SavedCommerceService.java#L13)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| contactName | String | @NotBlank @Size(max=64) |
| contactPhone | String | @NotBlank @Pattern(regexp="[+0-9() -]{5,32}") |
| province | String | @Size(max=64) |
| city | String | @Size(max=64) |
| district | String | @Size(max=64) |
| detail | String | @NotBlank @Size(max=512) |
| isDefault | boolean | — |

<a id="com-kuaixietech-service-account-impl-savedcommerceservice-cartinput"></a>
## com.kuaixietech.service.account.impl.SavedCommerceService.CartInput

[SavedCommerceService.java:14](../../server/src/main/java/com/kuaixietech/service/account/impl/SavedCommerceService.java#L14)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| quantity | int | @Min(1) @Max(10000) |

<a id="com-kuaixietech-service-catalog-support-productpublishingservice-input"></a>
## com.kuaixietech.service.catalog.support.ProductPublishingService.Input

[ProductPublishingService.java:20](../../server/src/main/java/com/kuaixietech/service/catalog/support/ProductPublishingService.java#L20)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| name | String | @NotBlank @Size(max=255) |
| spec | String | @Size(max=255) |
| region | String | @NotBlank @Size(max=64) |
| price | BigDecimal | @NotNull @DecimalMin("0.01") @DecimalMax("9999999999.99") @Digits(integer=10,fraction=2) |
| stock | Integer | @NotNull @Min(0) @Max(1000000) |
| categoryId | Long | @Positive |
| imageUrls | List< String> | @NotNull @Size(max=9) @NotBlank @Size(max=800) |
| note | String | @Size(max=1024) |
| expectedStock | Integer | @Min(0) @Max(1000000) |

<a id="com-kuaixietech-service-catalog-support-productpublishingservice-state"></a>
## com.kuaixietech.service.catalog.support.ProductPublishingService.State

[ProductPublishingService.java:26](../../server/src/main/java/com/kuaixietech/service/catalog/support/ProductPublishingService.java#L26)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| status | String | @NotBlank @Pattern(regexp="on\|off") |

<a id="com-kuaixietech-service-community-impl-batch12contentmoderationservice-screening"></a>
## com.kuaixietech.service.community.impl.Batch12ContentModerationService.Screening

[Batch12ContentModerationService.java:10](../../server/src/main/java/com/kuaixietech/service/community/impl/Batch12ContentModerationService.java#L10)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| decision | String | — |
| codes | List<String> | — |

<a id="com-kuaixietech-service-trading-support-intentpricing-quote"></a>
## com.kuaixietech.service.trading.support.IntentPricing.Quote

[IntentPricing.java:18](../../server/src/main/java/com/kuaixietech/service/trading/support/IntentPricing.java#L18)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| goodsAmount | BigDecimal | — |
| serviceFee | BigDecimal | — |
| insurancePremium | BigDecimal | — |
| buyerEscrow | BigDecimal | — |
| vendorDeposit | BigDecimal | — |
| currency | String | — |
| pricingVersion | String | — |

<a id="com-kuaixietech-shops-shopservice-application"></a>
## com.kuaixietech.shops.ShopService.Application

[ShopService.java:15](../../server/src/main/java/com/kuaixietech/shops/ShopService.java#L15)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| businessName | String | @NotBlank @Size(max=200) |
| creditCode | String | @NotBlank @Pattern(regexp="[0-9A-HJ-NPQRTUWXY]{18}") |
| representative | String | @NotBlank @Size(max=100) |
| licenseUploadId | String | @NotBlank |
| invitationCode | String | @Size(max=16) |
| channel | String | @NotBlank @Pattern(regexp="web\|h5\|app\|mp_weixin") |
| requestId | String | @NotBlank |

<a id="com-kuaixietech-shops-shopservice-edit"></a>
## com.kuaixietech.shops.ShopService.Edit

[ShopService.java:17](../../server/src/main/java/com/kuaixietech/shops/ShopService.java#L17)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| name | String | @NotBlank @Size(max=200) |
| description | String | @Size(max=1000) |

<a id="com-kuaixietech-shops-shopservice-link"></a>
## com.kuaixietech.shops.ShopService.Link

[ShopService.java:19](../../server/src/main/java/com/kuaixietech/shops/ShopService.java#L19)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| productId | String | @NotBlank |

<a id="com-kuaixietech-shops-shopservice-review"></a>
## com.kuaixietech.shops.ShopService.Review

[ShopService.java:16](../../server/src/main/java/com/kuaixietech/shops/ShopService.java#L16)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| approve | boolean | — |
| reason | String | @NotBlank @Size(max=1000) |

<a id="com-kuaixietech-shops-shopservice-state"></a>
## com.kuaixietech.shops.ShopService.State

[ShopService.java:18](../../server/src/main/java/com/kuaixietech/shops/ShopService.java#L18)；record。

| 字段 | 类型 | 声明注解 |
| --- | --- | --- |
| status | String | @NotBlank @Pattern(regexp="active\|paused\|suspended") |
| expectedStatus | String | @NotBlank |
| reason | String | @NotBlank @Size(max=1000) |
