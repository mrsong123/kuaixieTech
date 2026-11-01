# 第三方集成与数据库逐项审计

源快照：`643e42c24d529cae31d4f4a02fff45ac516b1034` 加本轮工作区修复；日期2026-11-01。初始静态核查后，已在一次性 MySQL 8.4.13/113表执行真实 Spring Boot 启动与33项 HTTP/SQL 检查，全部通过，详见 [RUNTIME_VALIDATION.md](RUNTIME_VALIDATION.md) 与脱敏回执。没有连接业务库或验收真实供应商。

## 结论与状态口径

目前不能称系统已全线跑通。现有配置页、SDK构造、签名协议和单元测试均不是供应商业务验收。47个目录项中，只有6个外部消费者接入新配置，另有1个本地flags；25种typed工厂能初始化，不代表25个业务可用。原生支付的专业商户配置独立于通用目录，需单列。

数据库为82原有表+31提议表=113表/1186字段。全部缺完整验收回执；232字段无命名SQL候选，其中21表/140字段连生产表名字面引用都未发现。SELECT *、动态SQL可能覆盖未命名字段，所以不能把232直接叫作死字段；21表仍须检查是否有动态映射，本次未找到对应路径。

状态：**参考占位**=只有目录/参数；**SDK工具**=仅客户端构造；**协议工具**=有HTTP/签名方法但无页面到执行落库消费者；**已编码未运行**=有业务调用链、尚无真实运行回执。所有真实业务验收为pending。

## 发现项与严格执行顺序

| ID | 优先级 | 状态 | 问题及证据 | 下一步 |
| --- | --- | --- | --- | --- |
| INT-001 | P0 | `blocked_runtime_prerequisite` | **旧82表库不能支撑当前31张新增表业务**。配置exists/runtime首先读t_integration_runtime_state及mode_scopes；只导原SQL且008未执行会出现缺表，不是未配key。当前用户堆栈没有根因，不能擅自断言其具体DB错误。 `server/src/main/java/com/kuaixietech/coupler/CouplerService.java:22` | 只读核对真实information_schema及迁移版本；先在一次性数据库验证完整结构，业务库迁移遵从最终部署要求。 |
| INT-002 | P0 | `fixed_disposable_mysql_verified_pending_deployment` | **默认地址触发器同表UPDATE已实证1442**。原结构在 MySQL 8.4.13 确实报错；012提议迁移仅应用一次性审计库后，新增和切换默认地址成功，切换后默认地址数为1。 `server/db/migrations/012-default-address-trigger.proposed.sql` | 业务库仍未执行；按最终部署计划应用012并验收真实默认地址新增/切换事务，保留原始SQL参考件。 |
| INT-003 | P0 | `disposable_width_verified_pending_business_acceptance` | **无责取消状态宽度需011真实落地**。原t_orders.status只有16字符，既有cancelled_no_fault为18字符。一次性113表库已实读width=32，未证明业务库已变更或取消事务已通过。 `server/db/migrations/011-order-status-width.proposed.sql:4` | 部署后读取业务库元数据证明width>=32并验证取消事务，不能只看DDL文件已生成。 |
| INT-004 | P1 | `not_implemented` | **保险12机构只是参考名单，生产适配器为零**。没有production类实现InsuranceModels.Adapter；settings.adapters为空，enabled产品会INSURER_NOT_CONNECTED，消费者products会过滤为空。报价/机构直付/出单/退保/理赔不能仅填key实现。 `server/src/main/java/com/kuaixietech/insurance/InsuranceCatalog.java:10` | 选有正式合作API的持牌机构，实现完整Adapter与验签回执；真实保费由机构收取，平台不担保。 |
| INT-005 | P1 | `partial_custom_bridge_only` | **银行卡/信用卡不是银联或各银行正式收单**。hosted_bank_v1是自定义HMAC信封协议，需另有网关实现/capabilities/payments/query；当前没有银联正式报文/证书/退款/对账consumer。 `server/src/main/java/com/kuaixietech/payments/HostedBankAdapter.java:23` | 确认实际收单机构、产品和合同支持币种，实现供应商正式适配与账单验收，不能把所有ISO货币目录当已能收款。 |
| INT-006 | P1 | `partial_sdk_protocol_only` | **40个非本地外部目录项未接当前业务消费者**。47项含6个外部consumer和1个本地flags；其余40项仅SDK、协议或参考。通用WXPAY/ALIPAY另有专业支付链，但不会读取通用配置直接自动启用。 `server/src/main/java/com/kuaixietech/coupler/CouplerCatalog.java:11` | 按真实优先级逐项接业务按钮、授权、执行、落库、回执和失败恢复；保留consumerWired=false不可选中保护。 |
| INT-007 | P1 | `not_implemented` | **SMTP邮箱发送未接outbox**。Spring mail依赖与application.yml占位SMTP配置没有业务consumer。当前只支持阿里DirectMail验证码协议。 `server/src/main/java/com/kuaixietech/integration/EmailOutboxDispatcher.java:45` | 按已有outbox安全领取规则补SMTP适配、模板、TLS与接受/送达区别；或在UI明确仅DirectMail可用。 |
| INT-008 | P1 | `partial_signaling_only` | **RTC只有订单信令和未接入口的token工具**。start/accept/hangup更新记录和通知；TencentRtcOrderCredentials无controller消费者，各端没有实际媒体join/connected/录音回执。duration不能当供应商真实媒体时长。 `server/src/main/java/com/kuaixietech/service/communication/impl/Batch12CallRpcService.java:12` | 选择一家RTC，把受保护token端点、端侧join/leave、真实连接事件与录音同意及回执闭环。 |
| INT-009 | P1 | `not_implemented` | **微信订阅与外部通知未形成投递闭环**。有公开订阅协议工具而无通知outbox业务consumer；站内通知写入不能当微信/系统推送送达。 `server/src/main/java/com/kuaixietech/integrationsdk/social/WechatMiniProtocol.java:14` | 补同App模板/openid授权记录、outbox原子领取、真实send回执、未知结果及失败状态，不自动重发不明请求。 |
| INT-010 | P1 | `no_production_table_literals` | **21张表140字段未发现生产表名引用**。包含用户偏好/注销申请/撤销token/CBEC/退货/店铺保单/媒体锚点/地理/私信/协带等。1186字段全部缺完整验收，232字段无命名SQL候选；后者不能直接等同死字段。 `server/tools/audit_business_coverage.py:62` | 逐表选择保留并实现真实页面/写入/状态流/权限/读回，或明确设计决策；不要用通用JSON覆盖表当业务出口。 |
| INT-011 | P1 | `partial_finance_unreconciled` | **取消后剩余30%无平台风险池独立分录**。违约分支给对方70%，其余30%仅备注待独立台账核验；没有t_risk_pool生产引用或平台资金总账路径。不能宣称资金全部去向可审计。 `server/src/main/java/com/kuaixietech/service/order_fulfillment/impl/CancellationRpcService.java:61` | 明确平台收入/风险准备金归属，增加守恒分录及日终对账后才允许财务完整验收。 |
| INT-012 | P1 | `partial_identity_scope` | **微信openid未按AppID持久绑定**。当前按全局openid找用户并有全局UNIQUE；没有AppID身份键。仅单固定小程序可约束使用，切换AppID/多小程序/OAuth不能直接混用。 `server/src/main/resources/mapper/account/AuthLoginMapper.xml:35` | 增加按provider+appid+subject的身份映射及已有数据迁移/冲突政策，商户AppID与用户openid来源同步验证。 |
| INT-013 | P1 | `partial_legacy_configuration` | **旧专业密钥env引用仍动态解析**。新coupler保存时固定密文版本；legacyLiveRuntime和专业支付凭据兼容动态env，轮换旧环境变量不能承诺历史记录继续用原材料。 `server/src/main/java/com/kuaixietech/integration/IntegrationConfiguration.java:18` | 制定旧商户与对象存储密钥版本迁移、保留与轮换验收；不允许历史查询悄悄换商户/桶。 |
| INT-014 | P2 | `partial_observability` | **汇率源异常被逐源忽略且无逐源健康记录**。安全上不虚构汇率，但最后仅FRESH_FX_RATE_UNAVAILABLE；还需区分过期、币种缺失、网络/认证、商业交易许可与静默fallback。 `server/src/main/java/com/kuaixietech/payments/FxService.java:32` | 提供源级只读诊断、有效期/来源审计及商户清算汇率差异对账，不把参考价当真实银行成交汇率。 |
| INT-015 | P0 | `deployment_source_ambiguity` | **旧resources/schema.sql与当前权威SQL不一致**。resources/schema.sql含旧无t_前缀表及演示种子，而当前契约是reference/current-schema.sql+11迁移。sql.init=never当前避免误导入，但不能按旧schema启动。 `server/src/main/resources/application.yml:15` | 明确唯一路径、版本、元数据预检和部署日志；保留原件，隔离/标明旧schema不可用于当前部署。 |

执行顺序：先P0编译器/数据库契约/调度诊断与应用真实启动→无外部收费的账号/商品/地址/订单数据流→一种支付+一种退款的真实渠道→财务守恒与对账→保险合作consumer→通知/物流/RTC等扩展。不能并行启用尚未验证的资金或投保入口。

## 47个供应商逐项状态

下表“工厂/协议/消费者”按代码区分，均未做供应商真实验收。数据从静态目录与运行时ClientFactories合并推导，不能只看JSON中sdkAvailable=false，因为CouplerCatalog会在运行时补充。

| 分类/供应商 | 工厂 | 协议工具 | 当前业务消费者 | 当前层级 | 配置以外所需条件/未完成点 |
| --- | --- | --- | --- | --- | --- |
| `FEATURES/flags` 开通开关 | 无 | 无 | 本地策略 | 本地开关，未运行验收 | 每模式分类/操作开关、依赖及只读能力状态；必须实际拦截新业务调用；enabled/selected不能冒充businessVerified |
| `AI/deepseek` DeepSeek | 无 | 有 | 无 | 协议工具，缺业务接线 | chat/completions；模型和额度需开通；接到客服回复/消息落库；目前客服仍调用自定义support-ai-reply桥接 |
| `AI/cursor` Cursor | 无 | 有 | 无 | 协议工具，缺业务接线 | Cloud Agents API；需授权仓库/ref、agent生命周期；不是聊天模型接口；创建/停止agent与客服回复不可混用，需明确业务入口 |
| `AI/chatgpt` ChatGPT | 无 | 有 | 无 | 协议工具，缺业务接线 | OpenAI API项目key和支持的model；ChatGPT订阅不等于API授权；接到客服会话并处理拒绝/限额；不能将客户端初始化或HTTP200当回复已落库 |
| `MINIAPP/weixin` 微信小程序 | 有 | 有 | 已接（未真实验收） | 已编码，未运行/真实验收 | jscode2session；appid/appsecret和对应小程序；AuthLoginService通过IntegrationConfiguration读取新选中配置；AppID与openid绑定、真实账号验收仍需完成 |
| `OAUTH/generic` 通用 OAuth2 | 无 | 无 | 无 | 参考占位，缺实现 | 授权/token/userinfo/issuer、scope、subject映射、state/PKCE/nonce；OAuth2本身不证明身份；补回跳一次性校验、绑定/解绑与本地JWT签发 |
| `OAUTH/wecom` 企业微信 | 有 | 无 | 无 | SDK工具，缺业务接线 | 企业内部/自建/第三方SSO类型、CorpID/AgentID、回调域和授权范围；企业微信不是普通微信openid；补成员状态及租户/本地账号映射 |
| `OAUTH/weibo` 微博登录 | 无 | 无 | 无 | 参考占位，缺实现 | 已审核AppKey/Secret、回调URI、scope与用户info接口版本；补state/PKCE可用方案、idstr映射、绑定/解绑和JWT，不仅拿access token |
| `OAUTH/douyin` 抖音登录 | 无 | 无 | 无 | 参考占位，缺实现 | 确定抖音开放平台网站/移动/小程序类型、回调、权限和版本；不能混用TikTok协议；补unionid/openid作用域和账号绑定 |
| `OAUTH/weixin` 微信开放平台 | 有 | 无 | 无 | SDK工具，缺业务接线 | 网站QRConnect/移动授权产品、开放平台AppID和回调白名单；不能复用jscode2session；不同AppID的openid不得直接当同一人 |
| `RTC/tencent` 腾讯云 TRTC | 有 | 有 | 无 | 协议工具，缺业务接线 | TRTC SDKAppID+SDKSecret不同于云API AK；room/user受限签名；start/accept RPC尚不连接媒体；需五端join、真正connected事件、录音回执 |
| `RTC/aliyun` 阿里云 RTC | 有 | 无 | 无 | SDK工具，缺业务接线 | 应用、频道/用户令牌、产品版本和区域；补order participant→频道、各端媒体join、退出/录制/结果审计 |
| `RTC/agora` 声网 Agora | 有 | 无 | 无 | SDK工具，缺业务接线 | AppID/AppCertificate、token版本、频道/用户权限；端侧RTC、媒体连接/断线回调未接；token可生成不等于通话已通 |
| `SUBSCRIBE/weixin` 微信订阅消息 | 有 | 有 | 无 | 协议工具，缺业务接线 | 稳定access token、订阅模板、同AppID openid和用户授权；通知outbox需要模板映射、授权与真正send结果；不是写t_notifications即已发送 |
| `FACE/tencent` 腾讯云人脸核身 | 有 | 无 | 无 | SDK工具，缺业务接线 | 核身产品/RuleID、企业准入、回跳及结果查询方案；BizToken/init不是passed；补身份资料、前端认证SDK和签名结果核验 |
| `FACE/aliyun` 阿里云实人认证 | 有 | 无 | 无 | SDK工具，缺业务接线 | 具体认证方案、场景ID、资料类别、结果查询/回调；初始化CertifyId不等于认证通过；当前本地取证不得冒充供应商实人核验 |
| `KYC/channel` KYC 通道 | 无 | 无 | 无 | 参考占位，缺实现 | 提供机构名、核验产品、输入证件类型、签名/结果/收费/授权文档；现有HttpIdentityProviderClient仅密码/联系方式管理，不是执照/法人核验 |
| `SMS/aliyun` 阿里云短信 | 有 | 有 | 已接（未真实验收） | 已编码，未运行/真实验收 | 2017-05-25 SendSms、签名/模板和国内短信产品；已接旧dispatcher的新配置路由；无官方sandbox即拒绝SANDBOX |
| `SMS/tencent` 腾讯云短信 | 有 | 有 | 已接（未真实验收） | 已编码，未运行/真实验收 | 2021-01-11 SendSms、SdkAppId、签名/模板、E.164；同上；sent仅供应商受理，不代表手机终端送达 |
| `SMS/huawei` 华为云短信 | 无 | 无 | 无 | 参考占位，缺实现 | 应用key/secret、签名通道、模板、区域endpoint；接到同一outbox幂等claim/回执/未决不重发；不能仅构造MsgSms客户端 |
| `EMAIL/smtp` SMTP | 无 | 无 | 无 | 参考占位，缺实现 | SMTP主机/端口、认证、TLS、From及模板；无通用官方sandbox；缺SMTP与邮件outbox consumer；有JavaMail依赖或DirectMail发送不代表SMTP条目完成 |
| `EMAIL/aliyun` 阿里云 DirectMail | 无 | 有 | 已接（未真实验收） | 已编码，未运行/真实验收 | API产品、授权、参数、回执及运行环境；DirectMail验证码队列已接，SMTP不是该通道；尚缺真实发件身份授权、投递回执、生产额度与对账。 |
| `PRIVACY_NUMBER/aliyun` 阿里云隐私号 | 有 | 无 | 无 | SDK工具，缺业务接线 | 必须确定AXB/AXN方案、号码池/签约号码、绑定/解绑和租约接口；没有号码池不会得到可拨中间号；补订单双方绑定、超时解绑、通话回执 |
| `PRIVACY_NUMBER/tencent` 腾讯云隐私号 | 有 | 无 | 无 | SDK工具，缺业务接线 | 现售产品、供应商/代理商、号码池、绑定/续租/回执；不能把普通RTC或NPP类构造当隐私号分配；需实际合同产品资料 |
| `WXPAY/weixin` 微信支付 | 有 | 无 | 无 | SDK工具，缺业务接线 | APIv3商户号、AppID、商户私钥、平台验签公钥/证书；新quote/create/退款派发已加LIVE gate；旧支付/退款查询保留专业商户LIVE快照 |
| `ALIPAY/alipay` 支付宝电脑网站支付 | 有 | 有 | 无 | 协议工具，缺业务接线 | 生产与官方沙箱分别AppID、私钥、平台公钥和网关；现有钱包只接受既有真实生产凭据；sandbox回执不能入真实钱包/退款台账 |
| `BANK_CARD/unionpay` 银联 / 对公账户 | 无 | 无 | 无 | 参考占位，缺实现 | 收单机构/产品、商户、证书、接口版本、交易/退款/查询/对账与币种合同；现有HMAC桥接不是银联官方协议；对公转账须银行回单不能按钮确认为到账 |
| `TRANSLATE/tencent` 腾讯云翻译 | 有 | 有 | 无 | 协议工具，缺业务接线 | 2018-03-21 TextTranslate、region/projectId、语言代码；新协议需接消息翻译/使用者入口、长度/计费边界，不把返回JSON当消息已更新 |
| `TRANSLATE/aliyun` 阿里云翻译 | 有 | 有 | 无 | 协议工具，缺业务接线 | TranslateGeneral 2018-10-12、区域和语言；接消息/商品展示翻译；禁止覆盖原文且需明确译文语言与来源 |
| `TRANSLATE/deepl` DeepL | 有 | 有 | 无 | 协议工具，缺业务接线 | /v2/translate、free/pro端点与语言变体；api-free是正式免费额度服务，不是sandbox；consumer及输出映射待验 |
| `TRANSLATE/google` Google Translate | 有 | 有 | 无 | 协议工具，缺业务接线 | 当前固定Basic v2；不能把v3 project/location凭据混用；语言/HTML实体处理已有协议设计；实际消息和译文持久化待接 |
| `OSS/aliyun` 阿里云 OSS | 有 | 有 | 已接（未真实验收） | 已编码，未运行/真实验收 | bucket、区域endpoint、AK、私有ACL、MD5/ETag；当前PrivateUploads支持；新耦合器写入路由/历史对象绑定不能由SDK工厂自动替代 |
| `OSS/tencent` 腾讯云 COS | 有 | 有 | 已接（未真实验收） | 已编码，未运行/真实验收 | bucket含APPID、region、SecretId/Key、私有ACL；同上；原对象位置需保留，不能换厂商后用新bucket解释旧storage_key |
| `OSS/huawei` 华为云 OBS | 有 | 无 | 无 | SDK工具，缺业务接线 | OBS区域endpoint、bucket、AK/SK、对象ACL；接上传确认/私有读取/删除/回收及元数据模式；构造OBS client不等于附件可用 |
| `OSS/qiniu` 七牛云 Kodo | 有 | 无 | 无 | SDK工具，缺业务接线 | Kodo区域、bucket、上传策略/有效期、私有下载域名；接相同用途/归属/哈希和私有下载流程，不返回永久公开执照URL |
| `OSS/aws` Amazon S3 | 有 | 无 | 无 | SDK工具，缺业务接线 | 区域/bucket、IAM授权、必要时session token、SigV4；接对象元数据/校验；S3没有通用官方sandbox，测试bucket仍可能收费 |
| `MAP/amap` 高德地图 | 无 | 有 | 无 | 协议工具，缺业务接线 | 服务端rest_key；JS Web key/安全密钥、端侧key需分开；geocode不等于GPS或路线/距离；补具体业务地图入口与GCJ-02/WGS84坐标标签 |
| `MAP/tencent` 腾讯地图 | 无 | 有 | 无 | 协议工具，缺业务接线 | 腾讯位置服务key、白名单/签名设置、坐标标准；同上；服务端地址转换不应作为真实到场取证坐标 |
| `SEARCH/elasticsearch` Elasticsearch | 有 | 无 | 无 | SDK工具，缺业务接线 | 节点/索引别名、认证/TLS、索引字段/分析器、服务端版本；需索引同步/删除/店铺与卖家可见性、结果回源校验；ping/client初始化不是搜索闭环 |
| `LOGISTICS/sf` 顺丰速运 | 无 | 无 | 无 | 参考占位，缺实现 | 客户编码/校验码、服务代码、月结卡、测试/生产域、下单/取消/路由/面单合同；仅tracking字段不能完成寄件；产品及地址/重量/签名/回执字典缺失 |
| `LOGISTICS/debon` 德邦 / 大件 | 无 | 无 | 无 | 参考占位，缺实现 | 明确德邦产品/客户账号、鉴权、区域/价格、大件参数、下单/取消/路由/面单；debon是本附件标识，不等于可猜测的deppon endpoint；需实际授权API文档 |
| `LOGISTICS/jd` 京东物流 | 无 | 无 | 无 | 参考占位，缺实现 | 确定物流平台与产品、业务客户编码、授权token、版本/签名和电子面单；京东电商SDK或AppKey不能替代物流商家授权和下单合同 |
| `LOGISTICS/anjie` 安捷大件 | 无 | 无 | 无 | 参考占位，缺实现 | “安捷大件”对应企业全称、官网、开放平台和签约API；名称无法唯一定位供应商；不能编造URL、签名或运输产品 |
| `AIRLINE/cea` 中国东方航空 | 无 | 无 | 无 | 参考占位，缺实现 | 东航或授权聚合商合同、NDC/IBE profile、航班报价/验价/PNR/出票/退改/结算接口；通用IATA XML和API key不能确定承运人报价/出票语义；未出票不得展示票号 |
| `AIRLINE/csn` 中国南方航空 | 无 | 无 | 无 | 参考占位，缺实现 | 南航授权API产品、代理资质、签名/证书、报价与出票/退改状态字典；先确定直连或聚合商；不同航空系统不能共用猜测接口 |
| `AIRLINE/airchina` 中国国际航空 | 无 | 无 | 无 | 参考占位，缺实现 | 国航授权产品/接入文档、证书/商户、旅客/航段/税费及票务回执；NDC标准不能替代航空公司具体profile、结算和幂等出票规则 |
| `RAIL/12306` 铁路 12306 合作接口 | 无 | 无 | 无 | 参考占位，缺实现 | 12306正式合作方/产品、合法接口文档、鉴权、票务/退改/支付/发票和限额规则；公众网站查询/爬虫/用户cookie不是合作API；不能编造出票或绕过网站验证 |

## 专业支付、汇率与保险另列

| 能力 | 代码证据 | 当前状态/必要验收 |
| --- | --- | --- |
| 微信Native/JSAPI/App充值 | payments/WechatNativeAdapter、WechatJsapiAdapter、WechatAppAdapter；PaymentsController:18-28 | 有生产协议、验签查询、原子钱包入账代码；需商户/AppID/平台证书、回调域名、真实回执及重复/超时核对。小程序touristappid不能做正式登录和支付。 |
| 支付宝page/wap/app | payments/AlipayPageAdapter | 有RSA2签名创建/查询及平台验签代码；需真实应用/商户协议与各端能力验收。 |
| 原充值原路退款 | payments/OriginalPaymentRefundGateway、refunds/ | 仅CNY微信/支付宝原充值全退；有余额预留、审核/派发/查询、事件；银行卡、跨币种、通知及长期对账未闭环。 |
| 多币种报价/换算/查询 | payments/FxService:25-66、CurrencyAmounts、PaymentService | 有ISO货币目录、精度、有效期、来源、商户能力交集；open.er-api与自定义rates_json_v1是参考/交易许可分开配置。大多数货币目录不表示收单机构实际可结算。 |
| 银行卡/信用卡 | HostedBankAdapter:23-66 | 只有hosted_bank_v1自定义契约，缺具体银联/银行/收单方正式适配及合同、对账、退款。 |
| 商品保险机构直付 | InsuranceCatalog:10-27、InsuranceModels.Adapter、InsurerDirectory | 12参考机构、3新表、产品选择/同意/订单绑定/状态机已编码；production Adapter实现为零，真正报价/收费/出单/退保/理赔均未实现，不能靠APIkey自动开通。 |
| 店铺保单核验 | t_store_insurance_policies/t_insurance_coverages | 当前表没有production表名引用；店铺执照申请不等于保险公司核验，不能凭管理员按钮伪造承保。 |

### 配置页按钮的真实含义

- 保存：写密文配置或专业商户/产品参数，不会自动完成服务商签约或开通。
- 本地校验：检查字段、模式、密文、SDK构造/本地签名；不发收费探测，不证明余额/发信/投保有效。
- 设为当前：consumerWired=false应禁止；新LIVE调用还需当前环境与FEATURES开关。SANDBOX不是独立业务库/钱包，缺runtime行默认为SANDBOX。
- 专业支付/保险设置：与通用WXPAY/ALIPAY/参考目录分开；旧LIVE历史记录须保留原商户、桶、密文版本，不得随全局选择漂移。
- 外部接入失败：需渠道级安全错误码、配置缺失/迁移缺失/密钥不可读/真实发送失败的区分；不能只报PARTIALLY_UNAVAILABLE，也不能改成无条件成功。

## 数据库安装与迁移核对

1. 业务库默认名suixinggou，当前spring.sql.init.mode=never。旧server/src/main/resources/schema.sql不可作为当前113表部署文件。
2. 当前完整注释结构版保留原82表+31新表、原索引/外键/触发器，并去掉9条seed INSERT，只供空库。新增迁移包不含旧表CREATE，不能用来初始化空库。
3. CREATE TABLE IF NOT EXISTS不核验已存在定义；必须在实际information_schema对比类型、默认值、索引、字符集/排序规则和generated列，再按版本记录执行。
4. 原integration与community部分表显式utf8mb4_unicode_ci，其余多为默认utf8mb4_0900_ai_ci；001/005/006等新FK的字符类型与旧库排序规则必须实测，不能假设所有用户库都按当前默认创建。
5. 默认地址同表更新触发器需先修再验收；011状态扩宽需证明实际执行；完整脚本MySQL8语法/触发器行为应在一次性库复现，不能用正则名称检查代替。
6. 去掉seed后分类/风险池/集成等基础数据并未自动准备；需要明确哪些由真实运营配置、哪些是技术不变量初始化，不能灌演示成交或伪造商户。
7. 调度器当前会查询新coupler表。旧82表库没迁移即可启动进程却不能执行该任务，P0应提供结构就绪诊断与根因，而不是仅让定时日志安静。

## 113表逐表出口证据台账

下表是源级候选，不是运行验收。`无表名引用`优先列为未接业务；`SQL候选`仍须由前台/后台按钮、权限、写入/读回、状态变更及失败分支给出运行回执。字段全量明细沿用docs/DATABASE_BUSINESS_COVERAGE.csv；本次结构化1186字段证据另在/tmp/integration-audit-findings.json，便于主清单合并。

| 表 | 字段数 | 来源 | 生产引用/SQL候选 | 无命名SQL字段数 | 完整验收 |
| --- | --- | --- | --- | --- | --- |
| `t_profiles` 用户主档 | 60 | 原有 | 46文件引用/45SQL候选 | 12 | pending |
| `t_verification_codes` 验证码与刷脸口令 | 8 | 原有 | 8文件引用/7SQL候选 | 0 | pending |
| `t_kyc_documents` 证件类 KYC 材料 | 8 | 原有 | 4文件引用/3SQL候选 | 0 | pending |
| `t_face_verifications` 刷脸留证记录 | 9 | 原有 | 4文件引用/3SQL候选 | 0 | pending |
| `t_addresses` 买家收货地址簿 | 11 | 原有 | 2文件引用/1SQL候选 | 0 | pending |
| `t_pay_orders` 微信支付充值单 | 12 | 原有 | 2文件引用/1SQL候选 | 4 | pending |
| `t_vc_delivery_outbox` 验证码真实通道下发队列 | 8 | 原有 | 7文件引用/6SQL候选 | 0 | pending |
| `t_categories` 三级品类树 | 14 | 原有 | 7文件引用/6SQL候选 | 3 | pending |
| `t_category_countries` 品类-国家标签关联表（原 country text[]） | 2 | 原有 | 3文件引用/2SQL候选 | 0 | pending |
| `t_products` 带货人在架商品 | 13 | 原有 | 13文件引用/12SQL候选 | 0 | pending |
| `t_favorites` 收藏通用表 | 5 | 原有 | 2文件引用/1SQL候选 | 0 | pending |
| `t_cart_items` 购物车条目 | 5 | 原有 | 2文件引用/1SQL候选 | 0 | pending |
| `t_trips` 带货人发布的行程 | 44 | 原有 | 19文件引用/18SQL候选 | 2 | pending |
| `t_trip_categories` 行程-可带品类关联表（原 category_ids bigint[]） | 2 | 原有 | 3文件引用/2SQL候选 | 0 | pending |
| `t_demands` 买家发布的求购单 | 28 | 原有 | 13文件引用/12SQL候选 | 10 | pending |
| `t_quotes` 带货人对求购单的报价 | 10 | 原有 | 4文件引用/3SQL候选 | 0 | pending |
| `t_publish_deposits` 发布保证金 | 10 | 原有 | 2文件引用/1SQL候选 | 0 | pending |
| `t_orders` 订单主表 | 86 | 原有 | 37文件引用/36SQL候选 | 6 | pending |
| `t_deposits` 订单保证金流水台账 | 11 | 原有 | 12文件引用/11SQL候选 | 0 | pending |
| `t_order_logistics` 订单物流节点轨迹 | 7 | 原有 | 4文件引用/3SQL候选 | 0 | pending |
| `t_cancel_requests` 取消申请与定责 | 17 | 原有 | 4文件引用/3SQL候选 | 4 | pending |
| `t_disputes` 纠纷工单 | 23 | 原有 | 8文件引用/7SQL候选 | 0 | pending |
| `t_reviews` 订单完成后的互评 | 9 | 原有 | 3文件引用/2SQL候选 | 1 | pending |
| `t_balance_ledger` 钱包余额总账 | 8 | 原有 | 7文件引用/6SQL候选 | 0 | pending |
| `t_invoices` 电子发票申请与开具 | 16 | 原有 | 4文件引用/3SQL候选 | 0 | pending |
| `t_coupons` 优惠券/积分兑换券 | 8 | 原有 | 2文件引用/1SQL候选 | 1 | pending |
| `t_invite_codes` 平台邀请码池 | 6 | 原有 | 4文件引用/3SQL候选 | 0 | pending |
| `t_reward_ledger` 分销奖励台账（双币种） | 17 | 原有 | 7文件引用/6SQL候选 | 0 | pending |
| `t_score_logs` 履约分变动日志 | 8 | 原有 | 2文件引用/1SQL候选 | 0 | pending |
| `t_notifications` 站内通知 | 8 | 原有 | 31文件引用/30SQL候选 | 0 | pending |
| `t_notify_outbox` 微信订阅消息待发送队列及发送结果 | 11 | 原有 | 3文件引用/2SQL候选 | 0 | pending |
| `t_messages` 订单参与方会话留言及图片消息 | 8 | 原有 | 8文件引用/7SQL候选 | 0 | pending |
| `t_support_threads` 客服、AI及好友私聊会话线程 | 8 | 原有 | 3文件引用/2SQL候选 | 0 | pending |
| `t_support_tickets` 旧版客服工单及客服处理回复 | 11 | 原有 | 1文件引用/0SQL候选 | 11 | pending |
| `t_support_messages` 客服及私聊会话线程中的消息 | 8 | 原有 | 2文件引用/1SQL候选 | 4 | pending |
| `t_chat_violations` 聊天防引流规则命中及人工复核记录 | 6 | 原有 | 3文件引用/2SQL候选 | 0 | pending |
| `t_call_records` 网络语音通话及录音知情确认记录 | 14 | 原有 | 3文件引用/2SQL候选 | 1 | pending |
| `t_privacy_numbers` 隐私通话虚拟中间号码资源池 | 6 | 原有 | 4文件引用/3SQL候选 | 0 | pending |
| `t_privacy_bindings` 订单双方隐私通话虚拟号码绑定及释放记录 | 11 | 原有 | 4文件引用/3SQL候选 | 1 | pending |
| `t_call_logs` 通过订单隐私虚拟号发起的拨打记录 | 11 | 原有 | 2文件引用/1SQL候选 | 2 | pending |
| `t_admin_logs` 运营后台操作审计留痕 | 7 | 原有 | 16文件引用/15SQL候选 | 0 | pending |
| `t_legal_docs` 法律协议及说明文档的正文和版本 | 7 | 原有 | 2文件引用/1SQL候选 | 0 | pending |
| `t_join_applications` 运营账号加入申请与处理记录 | 8 | 原有 | 3文件引用/2SQL候选 | 1 | pending |
| `t_social_posts` 门户社区动态内容及审核记录 | 10 | 原有 | 2文件引用/2SQL候选 | 0 | pending |
| `t_support_conversations` 用户与AI及人工客服的接待会话 | 6 | 原有 | 2文件引用/2SQL候选 | 0 | pending |
| `t_support_conversation_messages` 客服接待会话的往来消息 | 6 | 原有 | 2文件引用/2SQL候选 | 0 | pending |
| `t_portal_demand_reviews` 门户需求发布内容审核记录 | 6 | 原有 | 2文件引用/2SQL候选 | 0 | pending |
| `t_user_preferences` 用户语言、地区、币种及时区偏好 | 5 | 原有 | 无表名引用（未接消费者候选） | 5 | pending |
| `t_revoked_tokens` 已撤销认证令牌摘要及失效时间 | 3 | 原有 | 无表名引用（未接消费者候选） | 3 | pending |
| `t_account_deletion_requests` 用户账号注销申请及运营处理记录 | 7 | 原有 | 无表名引用（未接消费者候选） | 7 | pending |
| `t_cbec_product_reviews` 跨境电商商品合规资料及审核结论 | 11 | 原有 | 无表名引用（未接消费者候选） | 11 | pending |
| `t_return_cases` 跨境退货案件、申报及货物到达时限记录 | 14 | 原有 | 无表名引用（未接消费者候选） | 14 | pending |
| `t_after_sales_cases` 订单售后申请、证据及退款处理记录 | 13 | 原有 | 5文件引用/5SQL候选 | 1 | pending |
| `t_consent_records` 个人信息处理告知及用户同意选择留痕 | 12 | 原有 | 2文件引用/2SQL候选 | 1 | pending |
| `t_auth_credentials` 用户密码认证凭据摘要 | 3 | 原有 | 1文件引用/1SQL候选 | 0 | pending |
| `t_login_codes` 手机号登录验证码摘要、有效期及校验次数 | 5 | 原有 | 无表名引用（未接消费者候选） | 5 | pending |
| `t_auth_rate_limits` 认证操作按主体及时间窗口限流计数 | 4 | 原有 | 无表名引用（未接消费者候选） | 4 | pending |
| `t_sms_send_events` 短信发送事件的手机号和来源IP摘要记录 | 4 | 原有 | 无表名引用（未接消费者候选） | 4 | pending |
| `t_sms_daily_budget` 短信每日发送量计数 | 2 | 原有 | 无表名引用（未接消费者候选） | 2 | pending |
| `t_store_applications` 商家开店资质申请及运营审核记录 | 14 | 原有 | 1文件引用/1SQL候选 | 1 | pending |
| `t_stores` 店铺主档及撮合费率配置 | 8 | 原有 | 6文件引用/6SQL候选 | 0 | pending |
| `t_store_insurance_policies` 店铺保险保单及外部核验记录 | 15 | 原有 | 无表名引用（未接消费者候选） | 15 | pending |
| `t_insurance_coverages` 订单关联的店铺保单承保记录 | 9 | 原有 | 无表名引用（未接消费者候选） | 9 | pending |
| `t_risk_pool` 历史风险准备金余额记录；不代表平台承保或担保 | 2 | 原有 | 无表名引用（未接消费者候选） | 2 | pending |
| `t_integration_vendors` 第三方集成服务商目录 | 4 | 原有 | 1文件引用/1SQL候选 | 2 | pending |
| `t_integration_products` 第三方集成产品目录 | 6 | 原有 | 1文件引用/1SQL候选 | 2 | pending |
| `t_integration_product_fields` 集成产品配置字段定义 | 6 | 原有 | 1文件引用/1SQL候选 | 1 | pending |
| `t_integration_settings` 集成产品启用状态及管理备注 | 6 | 原有 | 1文件引用/1SQL候选 | 0 | pending |
| `t_integration_secrets` 集成产品配置值密文存储 | 5 | 原有 | 1文件引用/1SQL候选 | 0 | pending |
| `t_posts` 社区帖子内容及互动计数 | 12 | 原有 | 4文件引用/3SQL候选 | 2 | pending |
| `t_post_likes` 社区帖子点赞关系 | 3 | 原有 | 2文件引用/1SQL候选 | 1 | pending |
| `t_post_comments` 社区帖子评论内容 | 5 | 原有 | 2文件引用/1SQL候选 | 0 | pending |
| `t_community_uploads` 社区上传文件元数据及使用关联 | 13 | 原有 | 4文件引用/4SQL候选 | 0 | pending |
| `t_post_media` 社区帖子的媒体及封面关联 | 7 | 原有 | 无表名引用（未接消费者候选） | 7 | pending |
| `t_post_anchors` 社区帖子挂载的关联资源及标题快照 | 5 | 原有 | 无表名引用（未接消费者候选） | 5 | pending |
| `t_post_favorites` 用户收藏社区帖子的关系 | 3 | 原有 | 无表名引用（未接消费者候选） | 3 | pending |
| `t_post_shares` 社区帖子分享行为记录 | 4 | 原有 | 无表名引用（未接消费者候选） | 4 | pending |
| `t_geo_points` 业务资源关联的地理坐标点 | 9 | 原有 | 无表名引用（未接消费者候选） | 9 | pending |
| `t_procurement_venues` 采购场所目录及地理信息 | 10 | 原有 | 无表名引用（未接消费者候选） | 10 | pending |
| `t_direct_threads` 用户间私信会话及来源帖子 | 6 | 原有 | 无表名引用（未接消费者候选） | 6 | pending |
| `t_direct_messages` 私信会话消息内容 | 5 | 原有 | 无表名引用（未接消费者候选） | 5 | pending |
| `t_piggyback_requests` 针对行程发起的捎带请求 | 10 | 原有 | 无表名引用（未接消费者候选） | 10 | pending |
| `t_withdrawals` 用户提现申请及后台审核记录 | 9 | 提议 | 3文件引用/2SQL候选 | 0 | pending |
| `t_announcements` 平台公告及多语言内容 | 10 | 提议 | 4文件引用/3SQL候选 | 2 | pending |
| `t_proof_recipes` 按行程及交通方式配置的凭证材料规则 | 10 | 提议 | 2文件引用/1SQL候选 | 0 | pending |
| `t_channel_configs` 外部渠道供应商配置，保存渠道参数与启用状态 | 9 | 提议 | 4文件引用/3SQL候选 | 0 | pending |
| `t_channel_secrets_probe` 渠道密钥就绪状态的人工探测记录 | 3 | 提议 | 3文件引用/2SQL候选 | 0 | pending |
| `t_error_logs` 客户端及服务异常日志与处理记录 | 10 | 提议 | 3文件引用/2SQL候选 | 2 | pending |
| `t_assist_requests` 订单协带申请、两次交接凭证与分润结算 | 15 | 提议 | 2文件引用/1SQL候选 | 1 | pending |
| `t_enterprise_license_files` 企业认证营业执照附件及排列顺序 | 5 | 提议 | 2文件引用/1SQL候选 | 0 | pending |
| `t_locales` 可用界面语言、显示名称与回退语言配置 | 7 | 提议 | 3文件引用/2SQL候选 | 0 | pending |
| `t_logistics_tracking` 订单物流单号及最近查询的轨迹缓存 | 6 | 提议 | 1文件引用/0SQL候选 | 6 | pending |
| `t_payment_merchants` 多币种支付商户、支付能力与报价参数配置 | 15 | 提议 | 2文件引用/2SQL候选 | 2 | pending |
| `t_fx_sources` 外汇汇率数据源及交易使用开关配置 | 10 | 提议 | 2文件引用/2SQL候选 | 1 | pending |
| `t_payment_quotes` 锁定支付商户配置和汇率的限时支付报价 | 16 | 提议 | 3文件引用/3SQL候选 | 0 | pending |
| `t_currency_payments` 多币种充值支付尝试、渠道收银台信息与核验状态 | 20 | 提议 | 7文件引用/7SQL候选 | 1 | pending |
| `t_currency_payment_receipts` 已核验支付回执，用于防止重复充值入账 | 9 | 提议 | 3文件引用/3SQL候选 | 1 | pending |
| `t_goods_insurance_products` 货物保险产品及保险方接入参数配置 | 7 | 提议 | 2文件引用/2SQL候选 | 1 | pending |
| `t_goods_insurance_quotes` 保险方返回的货物保险限时报价及产品快照 | 16 | 提议 | 3文件引用/3SQL候选 | 0 | pending |
| `t_goods_insurance_applications` 订单保险投保申请及承保方核验结果 | 19 | 提议 | 6文件引用/6SQL候选 | 0 | pending |
| `t_dispute_events` 订单争议处理操作及阶段变更审计事件 | 11 | 提议 | 2文件引用/2SQL候选 | 0 | pending |
| `t_store_products` 店铺与商品的归属关联 | 4 | 提议 | 5文件引用/5SQL候选 | 0 | pending |
| `t_product_order_reservations` 商品下单时的库存占用、计价与归属快照 | 16 | 提议 | 5文件引用/5SQL候选 | 0 | pending |
| `t_payment_refunds` 充值原路退款申请、审核、渠道派发与钱包冻结释放记录 | 22 | 提议 | 3文件引用/3SQL候选 | 0 | pending |
| `t_payment_refund_events` 充值退款审核、派发及渠道结果审计事件 | 6 | 提议 | 2文件引用/2SQL候选 | 0 | pending |
| `t_integration_runtime_state` 外部集成当前运行模式及切换版本 | 5 | 提议 | 1文件引用/1SQL候选 | 0 | pending |
| `t_integration_mode_scopes` 各运行模式下集成类别的供应商选择与版本 | 6 | 提议 | 1文件引用/1SQL候选 | 0 | pending |
| `t_integration_mode_configs` 按运行模式与集成类别隔离的供应商启用配置 | 7 | 提议 | 1文件引用/1SQL候选 | 0 | pending |
| `t_integration_config_values` 按模式、供应商和版本隔离的加密集成配置值 | 7 | 提议 | 1文件引用/1SQL候选 | 1 | pending |
| `t_order_escrow_refunds` 售后裁决退回未花费货款托管资金至买家平台钱包的执行记录 | 21 | 提议 | 2文件引用/2SQL候选 | 0 | pending |
| `t_order_escrow_refund_events` 托管货款退款执行的钱包入账审计事件 | 6 | 提议 | 1文件引用/1SQL候选 | 0 | pending |
| `t_upload_bindings` 上传文件与使用该文件的领域资源之间的引用绑定 | 5 | 提议 | 2文件引用/2SQL候选 | 0 | pending |
| `t_upload_deletions` 上传文件删除请求及对象存储删除确认日志 | 7 | 提议 | 1文件引用/1SQL候选 | 0 | pending |

## 未引用表的功能含义

- t_user_preferences：现前端显示偏好在本地存储，尚无服务端按账号同步该表的闭环。
- t_revoked_tokens、t_account_deletion_requests：不能把客户端清token/隐藏页面当服务端撤销会话或完整注销申请处理。
- t_cbec_product_reviews、t_return_cases：跨境合规和退货海关/入仓链未实现；不能由旧上传包中的后台表单名称推定后端已接。
- t_login_codes/t_auth_rate_limits/t_sms_send_events/t_sms_daily_budget：存在其他验证码/速率限制实现，未发现这组表接线，需合并职责并统一运营统计与实际限额来源。
- t_store_insurance_policies/t_insurance_coverages/t_risk_pool：店铺保单承保/核验和风险资金台账未闭环。
- t_post_media/anchors/favorites/shares、geo/procurement：社区公开图文feed和商品收藏不等于已使用这些社区多媒体/锚点/地理/分享表。
- t_direct_threads/messages、t_piggyback_requests：订单聊天已有其他表实现，不能将其当成私信和协带申请都已接通。

本审计没有删除字段、运行迁移、填充业务库或调用任何供应商，也没有将任何pending改为verified。
