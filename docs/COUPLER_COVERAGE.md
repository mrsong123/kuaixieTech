# 耦合器覆盖与独立接入审查

审查日期：2026-11-01。依据用户附件 `已粘贴的文本.txt`；SHA256：`30ca4666171e6236851c92f4c6481e8ba4b631c1fbd8af175e5aa3763e9f8edb`。原件解析得到 **21 类、46 厂商**，本表逐一对应，不把新目录、已保存key、本地校验或SDK客户端构造作为业务接通证据。

这是代码审查快照，尚未执行真实厂商调用、收费、发信或数据库迁移。既有调用链也不表示真实业务验收。新增版本化配置框架与旧消费者接线已独立编译，34 项配置/密钥隔离/调用路由测试通过，另有 19 项微信/RTC/支付宝环境与签名专项测试通过；typed SDK 的离线构造测试另见 `SDK_CLIENTS.md`（若文件尚未生成，以实际测试记录为准）。

当前目录保留附件的 21 类 46 厂商，另增 EMAIL/aliyun DirectMail，与 SMTP 明确区分。新配置实际接到旧业务消费者的只有 SMS/aliyun、SMS/tencent、OSS/aliyun、OSS/tencent、MINIAPP/weixin、EMAIL/aliyun；FEATURES/flags 为本地开关策略。其他协议/SDK 可以保存参数与本地验证，但 `consumerWired=false` 不允许设为当前运行通道。所有 `businessVerified=false`；未接业务入口的 AI、翻译、地图、RTC、订阅、实人认证等不会因保存 key 自动执行。

迁移 `008-coupler-environments.proposed.sql` 提供 4 张表，由最终部署人工执行，启动不建表、不复制旧明文或 LIVE 密钥。环境/类别/供应商/版本/字段共同参与 AES-GCM AAD；保存新版本保留历史密文。新耦合器的 `env:NAME` 仅在管理员保存时读取并将当时的实际值加密到该版本，环境变量轮换不影响既有版本；缺失或嵌套引用拒绝保存，历史版本若仍包含未固定引用则拒绝执行，需显式重存。旧专业配置 `legacyLiveRuntime` 仍兼容动态环境引用，不能对该旧路径承诺环境变量轮换后的历史凭据不变。类别与环境共用乐观版本控制，选中后禁用会暂停该通道，无旧配置自动回退。SANDBOX 拒绝现有真实新支付、退款派发、短信/邮件、微信登录、保险投保和上传；已有支付/退款查询、回调及历史对象读取继续使用原 LIVE 上下文。此模式只隔离 API 路由，不提供独立测试钱包或数据库。

通用 WXPAY/ALIPAY 参数不替代专业多商户实例配置；现有支付/退款保留原商户快照，新业务入口只增加 LIVE gate。保险继续采用保险企业报价、出单和收费，平台不担保。

## 46 个厂商的具体映射

“SDK”列说明可选依赖或现有协议，不表示所有依赖已安装/版本验证。多数公开HTTP API可以用JDK实现，不需要为凑目录引入或编造一个SDK。

| 类别 / 厂商 | 当前代码层级 | SDK / 协议依赖 | 填key之外的必要资料 | 尚缺的业务绑定或结论 |
| --- | --- | --- | --- | --- |
| AI/deepseek（DeepSeek） | 新增公开协议 | JDK REST；可选OpenAI兼容客户端 | chat/completions；模型和额度需开通 | 接到客服回复/消息落库；目前客服仍调用自定义support-ai-reply桥接 |
| AI/cursor（Cursor） | 新增独立agent协议 | JDK REST；不伪装ChatCompletion SDK | Cloud Agents API；需授权仓库/ref、agent生命周期 | 不是聊天模型接口；创建/停止agent与客服回复不可混用，需明确业务入口 |
| AI/chatgpt（ChatGPT） | 新增公开协议 | JDK REST；可选OpenAI官方Java SDK | OpenAI API项目key和支持的model；ChatGPT订阅不等于API授权 | 接到客服会话并处理拒绝/限额；不能将客户端初始化或HTTP200当回复已落库 |
| MINIAPP/weixin（微信小程序） | 存量登录调用；新耦合器协议在补 | JDK REST；WxJava miniapp为社区SDK | jscode2session；appid/appsecret和对应小程序 | AuthLoginService通过IntegrationConfiguration读取新选中配置；AppID与openid绑定、真实账号验收仍需完成 |
| RTC/tencent（腾讯云 TRTC） | 仅本地信令；UserSig协议在补 | tls-sig-api-v2/厂商SDK；还需各端媒体SDK | TRTC SDKAppID+SDKSecret不同于云API AK；room/user受限签名 | start/accept RPC尚不连接媒体；需五端join、真正connected事件、录音回执 |
| RTC/aliyun（阿里云 RTC） | 未接媒体业务；SDK工厂规划 | 阿里RTC SDK可选；不等于端侧媒体SDK | 应用、频道/用户令牌、产品版本和区域 | 补order participant→频道、各端媒体join、退出/录制/结果审计 |
| RTC/agora（声网 Agora） | 未接媒体业务；SDK工厂规划 | Agora Authentication/RtcTokenBuilder2 | AppID/AppCertificate、token版本、频道/用户权限 | 端侧RTC、媒体连接/断线回调未接；token可生成不等于通话已通 |
| SUBSCRIBE/weixin（微信订阅消息） | 新增公开协议在补 | JDK REST/WxJava miniapp | 稳定access token、订阅模板、同AppID openid和用户授权 | 通知outbox需要模板映射、授权与真正send结果；不是写t_notifications即已发送 |
| FACE/tencent（腾讯云人脸核身） | 未接实人核验；SDK工厂规划 | 腾讯Faceid SDK，不能用普通人脸检测替代 | 核身产品/RuleID、企业准入、回跳及结果查询方案 | BizToken/init不是passed；补身份资料、前端认证SDK和签名结果核验 |
| FACE/aliyun（阿里云实人认证） | 未接实人核验；SDK工厂规划 | 阿里cloudauth SDK | 具体认证方案、场景ID、资料类别、结果查询/回调 | 初始化CertifyId不等于认证通过；当前本地取证不得冒充供应商实人核验 |
| SMS/aliyun（阿里云短信） | 存量验证码队列原生协议 | 无需新增SDK，JDK RPC；可选dysmsapi SDK | 2017-05-25 SendSms、签名/模板和国内短信产品 | 已接旧dispatcher的新配置路由；无官方sandbox即拒绝SANDBOX |
| SMS/tencent（腾讯云短信） | 存量验证码队列原生协议 | 无需新增SDK，JDK TC3；可选腾讯SMS SDK | 2021-01-11 SendSms、SdkAppId、签名/模板、E.164 | 同上；sent仅供应商受理，不代表手机终端送达 |
| SMS/huawei（华为云短信） | 未接队列；SDK工厂规划 | 须确认华为云具体短信产品；JDK REST或对应SDK | 应用key/secret、签名通道、模板、区域endpoint | 接到同一outbox幂等claim/回执/未决不重发；不能仅构造MsgSms客户端 |
| PRIVACY_NUMBER/aliyun（阿里云隐私号） | 仅参考/通用桥接 | dypls号码保护SDK可选 | 必须确定AXB/AXN方案、号码池/签约号码、绑定/解绑和租约接口 | 没有号码池不会得到可拨中间号；补订单双方绑定、超时解绑、通话回执 |
| PRIVACY_NUMBER/tencent（腾讯云隐私号） | 未接号码业务；SDK工厂规划 | 须确认仍可开通的腾讯号码保护产品/SDK | 现售产品、供应商/代理商、号码池、绑定/续租/回执 | 不能把普通RTC或NPP类构造当隐私号分配；需实际合同产品资料 |
| WXPAY/weixin（微信支付） | 存量原生充值/原路退款协议 | JDK APIv3；可选官方wechatpay-java | APIv3商户号、AppID、商户私钥、平台验签公钥/证书 | 新quote/create/退款派发已加LIVE gate；旧支付/退款查询保留专业商户LIVE快照 |
| TRANSLATE/tencent（腾讯云翻译） | 新增公开协议 | JDK TC3；可选tmt SDK | 2018-03-21 TextTranslate、region/projectId、语言代码 | 新协议需接消息翻译/使用者入口、长度/计费边界，不把返回JSON当消息已更新 |
| TRANSLATE/aliyun（阿里云翻译） | 新增公开协议 | JDK RPC；可选alimt SDK | TranslateGeneral 2018-10-12、区域和语言 | 接消息/商品展示翻译；禁止覆盖原文且需明确译文语言与来源 |
| TRANSLATE/deepl（DeepL） | 新增公开协议 | JDK REST；可选deepl-java | /v2/translate、free/pro端点与语言变体 | api-free是正式免费额度服务，不是sandbox；consumer及输出映射待验 |
| TRANSLATE/google（Google Translate） | 新增公开协议 | 当前REST v2 API key；v3需另定服务账号SDK | 当前固定Basic v2；不能把v3 project/location凭据混用 | 语言/HTML实体处理已有协议设计；实际消息和译文持久化待接 |
| OSS/aliyun（阿里云 OSS） | 存量私有附件PUT/GET | JDK REST V1；可选aliyun-sdk-oss | bucket、区域endpoint、AK、私有ACL、MD5/ETag | 当前PrivateUploads支持；新耦合器写入路由/历史对象绑定不能由SDK工厂自动替代 |
| OSS/tencent（腾讯云 COS） | 存量私有附件PUT/GET | JDK COS V5；可选cos_api | bucket含APPID、region、SecretId/Key、私有ACL | 同上；原对象位置需保留，不能换厂商后用新bucket解释旧storage_key |
| OSS/huawei（华为云 OBS） | 未接附件读写；SDK工厂规划 | esdk-obs-java | OBS区域endpoint、bucket、AK/SK、对象ACL | 接上传确认/私有读取/删除/回收及元数据模式；构造OBS client不等于附件可用 |
| OSS/qiniu（七牛云 Kodo） | 未接附件读写；SDK工厂规划 | qiniu-java-sdk | Kodo区域、bucket、上传策略/有效期、私有下载域名 | 接相同用途/归属/哈希和私有下载流程，不返回永久公开执照URL |
| OSS/aws（Amazon S3） | 未接附件读写；SDK工厂规划 | AWS SDK v2 s3；可用签名REST | 区域/bucket、IAM授权、必要时session token、SigV4 | 接对象元数据/校验；S3没有通用官方sandbox，测试bucket仍可能收费 |
| FEATURES/flags（开通开关） | 待全局耦合器闸门 | 无厂商SDK | 每模式分类/操作开关、依赖及只读能力状态 | 必须实际拦截新业务调用；enabled/selected不能冒充businessVerified |
| ALIPAY/alipay（支付宝电脑网站支付） | 存量充值/退款；双模式协议在补 | JDK RSA2；可选alipay-sdk-java | 生产与官方沙箱分别AppID、私钥、平台公钥和网关 | 现有钱包只接受既有真实生产凭据；sandbox回执不能入真实钱包/退款台账 |
| EMAIL/smtp（SMTP） | SMTP未接；DirectMail不是SMTP | 已声明spring-boot-starter-mail/Jakarta Mail | SMTP主机/端口、认证、TLS、From及模板；无通用官方sandbox | 缺SMTP与邮件outbox consumer；有JavaMail依赖或DirectMail发送不代表SMTP条目完成 |
| OAUTH/generic（通用 OAuth2） | 未接OAuth身份生命周期 | Spring Security OAuth2 client可选或JDK HTTP | 授权/token/userinfo/issuer、scope、subject映射、state/PKCE/nonce | OAuth2本身不证明身份；补回跳一次性校验、绑定/解绑与本地JWT签发 |
| OAUTH/wecom（企业微信） | 未接登录；SDK工厂规划 | WxJava cp社区SDK或公开HTTP | 企业内部/自建/第三方SSO类型、CorpID/AgentID、回调域和授权范围 | 企业微信不是普通微信openid；补成员状态及租户/本地账号映射 |
| KYC/channel（KYC 通道） | 厂商/协议未确定 | 不能为generic channel虚构SDK | 提供机构名、核验产品、输入证件类型、签名/结果/收费/授权文档 | 现有HttpIdentityProviderClient仅密码/联系方式管理，不是执照/法人核验 |
| MAP/amap（高德地图） | 旧参考目录；新增geocode协议 | JDK REST；Web/原生地图SDK另计 | 服务端rest_key；JS Web key/安全密钥、端侧key需分开 | geocode不等于GPS或路线/距离；补具体业务地图入口与GCJ-02/WGS84坐标标签 |
| MAP/tencent（腾讯地图） | 新增geocode协议 | JDK REST；端侧SDK另计 | 腾讯位置服务key、白名单/签名设置、坐标标准 | 同上；服务端地址转换不应作为真实到场取证坐标 |
| SEARCH/elasticsearch（Elasticsearch） | 未接商品检索业务；SDK工厂规划 | elasticsearch-java，版本须匹配服务端 | 节点/索引别名、认证/TLS、索引字段/分析器、服务端版本 | 需索引同步/删除/店铺与卖家可见性、结果回源校验；ping/client初始化不是搜索闭环 |
| OAUTH/weibo（微博登录） | 公开协议可做，尚未接登录 | JDK OAuth2 HTTP；不必假造官方Java SDK | 已审核AppKey/Secret、回调URI、scope与用户info接口版本 | 补state/PKCE可用方案、idstr映射、绑定/解绑和JWT，不仅拿access token |
| OAUTH/douyin（抖音登录） | 公开协议可做，尚未接登录 | JDK OAuth2 HTTP；应用类型决定协议 | 确定抖音开放平台网站/移动/小程序类型、回调、权限和版本 | 不能混用TikTok协议；补unionid/openid作用域和账号绑定 |
| OAUTH/weixin（微信开放平台） | 开放平台登录未接；小程序登录另计 | JDK HTTP或WxJava open社区SDK | 网站QRConnect/移动授权产品、开放平台AppID和回调白名单 | 不能复用jscode2session；不同AppID的openid不得直接当同一人 |
| BANK_CARD/unionpay（银联 / 对公账户） | 仅自定义hosted_bank_v1桥接 | 须选银联全渠道/银联商务/具体银行产品SDK | 收单机构/产品、商户、证书、接口版本、交易/退款/查询/对账与币种合同 | 现有HMAC桥接不是银联官方协议；对公转账须银行回单不能按钮确认为到账 |
| LOGISTICS/sf（顺丰速运） | 仅参考/通用物流桥接 | 按顺丰已签开放平台协议，不虚构统一物流SDK | 客户编码/校验码、服务代码、月结卡、测试/生产域、下单/取消/路由/面单合同 | 仅tracking字段不能完成寄件；产品及地址/重量/签名/回执字典缺失 |
| LOGISTICS/debon（德邦 / 大件） | 未接物流；商务协议待确定 | 德邦对应开放平台协议或合作SDK | 明确德邦产品/客户账号、鉴权、区域/价格、大件参数、下单/取消/路由/面单 | debon是本附件标识，不等于可猜测的deppon endpoint；需实际授权API文档 |
| LOGISTICS/jd（京东物流） | 未接物流；商务协议待确定 | 对应京东物流开放平台协议/SDK | 确定物流平台与产品、业务客户编码、授权token、版本/签名和电子面单 | 京东电商SDK或AppKey不能替代物流商家授权和下单合同 |
| LOGISTICS/anjie（安捷大件） | 机构身份和协议均未确定 | 无可确认SDK | “安捷大件”对应企业全称、官网、开放平台和签约API | 名称无法唯一定位供应商；不能编造URL、签名或运输产品 |
| AIRLINE/cea（中国东方航空） | 直连协议/商业授权未提供 | 可有NDC/GDS标准，未形成东航专用consumer | 东航或授权聚合商合同、NDC/IBE profile、航班报价/验价/PNR/出票/退改/结算接口 | 通用IATA XML和API key不能确定承运人报价/出票语义；未出票不得展示票号 |
| AIRLINE/csn（中国南方航空） | 直连协议/商业授权未提供 | 同上，不能冒称通用南航SDK | 南航授权API产品、代理资质、签名/证书、报价与出票/退改状态字典 | 先确定直连或聚合商；不同航空系统不能共用猜测接口 |
| AIRLINE/airchina（中国国际航空） | 直连协议/商业授权未提供 | 同上，不能冒称通用国航SDK | 国航授权产品/接入文档、证书/商户、旅客/航段/税费及票务回执 | NDC标准不能替代航空公司具体profile、结算和幂等出票规则 |
| RAIL/12306（铁路 12306 合作接口） | 合作协议未提供；保持关闭 | 没有可据附件确认的通用合作Java SDK | 12306正式合作方/产品、合法接口文档、鉴权、票务/退改/支付/发票和限额规则 | 公众网站查询/爬虫/用户cookie不是合作API；不能编造出票或绕过网站验证 |

## 附件不能直接作为启动代码复制的原因

1. `IntegrationSchemaBootstrap` 属于 `com.unitimarket`，当前项目是 `com.kuaixietech`；附件引用的 `SysApiCategoryMapper/ProviderMapper`、实体与 `IntegrationConfigService.ensureKeywords()` 未随附件提供。`List.of()`空字段无法证明46厂商已有字段定义、适配器或consumer。
2. 附件 `ApplicationRunner` 会执行 CREATE、ALTER、种子及旧值迁移；用户已要求增量统一留到最终部署，所以这些动作应进部署SQL，不能在应用启动时做。逐项吞ALTER异常会把真实不兼容表误当成功。
3. `field_value → sandbox_value`自动复制没有环境证据：旧值可能是生产key，不能默认为沙箱。应保留旧值待明确归属，不能自动互拷。
4. `enabled/selected`只按provider一套字段保存；`UNIQUE(category_code,code)`仅防厂商重复，不防同类两家同时selected，也不区分模式。默认46家enabled=1不表示这些能力已开通。
5. 附件有Lombok注解而当前基线pom未声明Lombok；仅粘贴文件无法证明Boot4/JDK21可编译。它是目录/迁移样例，不是完整供应商SDK实现。

## 交付闸门：模式、选择、密钥和业务回执

### SANDBOX / LIVE

- 所有新调用必须先捕获不可变绑定：category、provider、mode、revision、参数，再交给协议/SDK；不能在网络调用中途重新读取全局selected和mode。
- 每模式的凭据、enabled、selected和revision独立。解密认证数据应包含provider、field和mode；把LIVE密文复制到SANDBOX应失败。不得从缺失SANDBOX值回退到LIVE、旧field_value或旧环境key。
- SDK客户端缓存至少按provider+mode+revision分离；仅vendor/product的singleton会在切模式或换key后复用旧凭据。
- 微信小程序/订阅、RTC token、短信、AI、翻译、地图、普通对象存储无通用厂商沙箱。SANDBOX应明确拒绝真实调用；测试应用、免费额度或测试bucket仍是LIVE服务，不能当无副作用沙箱。`validate`只能做本地字段/签名检查，不暗中发送收费探测。
- 支付/退款/保险旧记录须固定原商户、协议及 `LEGACY_LIVE` 语义；不能因为管理员切全局SANDBOX，就把真实交易拿去沙箱查询或用沙箱“成功”入真实钱包。
- 特别核验旧入口：SmsProtocols、DirectMail、PrivateUploads、AuthLoginService、现有支付/退款均有固定生产端点。新coupler globalmode若未接这些consumer，界面显示SANDBOX仍可能从旧按钮/cron调用生产接口。旧记录查询与新业务调用须分别处理，不能只改配置页。

### selected 与并发

分类+模式须有一个原子选择位，例如单独路由表 `(category,mode)` 主键和provider引用；或事务内稳定顺序加分类行锁，并有唯一约束兜底。只执行“先全部取消，再selected=1”且不串行化，会被两个管理员并发双选。revision冲突应返回刷新，不采用最后写入覆盖。选中厂商必须属于该分类、在该模式enabled，且实现声明支持该操作/模式；否则拒绝，不能自动换另一家。

### key 不回显与日志

现有 IntegrationCipher 为AES-GCM、API返回配置状态而不返回秘密，这是可复用基础，但AAD目前是vendor/product/field，新模式字段需额外绑定mode。秘密的空白输入表示保留，清除须显式操作；本地校验响应不得返回SDK实例、凭据Map或密文。包含values的Java record默认toString会泄漏key，应覆盖为provider/mode/revision摘要。

既有旧channel summary仍只掩码疑似key字段、保留前三字；新耦合器不能继续此部分回显策略或依赖任意secret_flag降级。未知字段默认秘密，字段分类由服务端协议schema控制。Amap等官方接口在query传key，SDK/HTTP异常只能转安全码，不能将完整URI或request headers写到响应/审计。

### 真实业务结果

- `Batch12CallRpcService.acceptOrderCall()`只改本地信令accepted，目前通知文案却为“网络通话已接通”。在真实媒体join回执之前，应只表述接受邀请/等待连接。duration_sec当前是本地信令时长，不是供应商媒体计费时长或录音证明。
- 企业代码校验、KYC会话初始化或人脸token并不等于核身通过；不能据此把t_face_verifications或企业状态置通过。
- SMTP/DirectMail的accepted不等于收件箱送达；微信access token不等于订阅消息已发送；对象客户端构造不等于文件已保存。
- 银联桥接的HMAC契约不是银联官方协议；银行卡/对公到账必须使用确定收单产品的真实结果，不能把运营按钮或SDK初始化变成银行回单。
- Elasticsearch client/ping不是商品搜索：必须有索引同步、撤销/删除和商品/店铺/账号可见性回源约束。

## Boot 4 / JDK21 依赖核对

当前pom基线为Boot 4.1.2、Java21、mybatis-plus-spring-boot4-starter 3.5.18.1，仍显式使用spring-boot-jackson2兼容 `com.fasterxml.jackson`。SDK新增由独立工厂代理处理，必须锁定实际存在的版本，并在最终pom上检查依赖树及当前应用编译/测试；本审查不凭计划坐标宣布依赖可解析。

- 不混入Boot2/3 starter、`javax.servlet`/旧validation自动配置，保持Jakarta命名空间；第三方纯Java SDK通常不需要Spring starter。
- 厂商SDK常引入Jackson2、Gson、Apache HTTP、OkHttp、Netty、Guava；不要覆盖Boot BOM或把Jackson3 `tools.jackson` 与现有ObjectMapper当同一种API。真实签名响应的原始字节不得被JSON版本重序列化改写。
- WxJava miniapp/open/cp是社区SDK，不是微信官方Java SDK；微信支付wechatpay-java、支付宝SDK与云厂商SDK应分别标注来源。
- Elasticsearch客户端须匹配实际集群版本；AWS S3/Google客户端的认证与HTTP传递依赖较重，客户端能加载也不能证明具体角色/服务权限。
- typed factory只应返回明确类型和受限操作能力；SDK构造测试、协议签名测试、consumer业务测试、真实服务联调必须分开报告。

## 必须取得的商务/产品资料（不能仅靠公开API目录补出）

| 范围 | 需要的确定资料 | 公开资料仍无法决定的原因 |
| --- | --- | --- |
| 银联/银行 | 签约机构和产品、商户/证书、正式/测试网关、币种/卡种/退款与对账协议 | “银联/对公账户”不是一个唯一接口，直接转账也不是在线收单 |
| 号码保护 | AXB/AXN方案、可用号码池、区域、绑定/解绑/计费回执授权 | key不能分配未签约号段，厂商产品生命周期和权限不同 |
| 企业/实人核验 | 机构和方案、场景/RuleID、前端流程、输入资料类别、结果校验合同 | 一个人脸检测或初始化token不能确定“此人/企业已真实核验” |
| 顺丰/德邦/京东 | 平台产品版本、业务客户账号、计费/区域、下单/路由/面单/取消与签名 | 公开产品名不能确定具体客户可用字段、合同产品或业务结果字典 |
| 安捷大件 | 对应企业全称、官方域名、开放平台和合同API | 附件名称不能唯一定位供应商，当前没有可确认协议 |
| 三家航司 | 直连还是授权聚合商、NDC/IBE profile、报价验价/PNR/出票/退改/结算授权 | IATA标准不包含航空公司具体产品和结算规则；不能猜票号或出票结果 |
| 12306 | 正式合作方及产品、合作API文档、鉴权、票务和退改/支付/发票权限 | 公共网站查询不是合作出票API；不能用爬虫/cookie替代 |

AI、翻译、地图、对象存储、标准OAuth/SMTP等有公开协议的能力可继续实现，不应一概声称需要商务文档。仍需选定实际协议版本、字段与应用回调约束，再把公开协议接到已有业务consumer；没有相关controller/服务调用证据就保留“未接业务”。

## 证据入口与后续更新

已有业务实现可核对：`integration/SmsProtocols.java`、`EmailProtocols.java`、`storage/PrivateObjectStorage.java`、`service/account/impl/AuthLoginService.java`、`payments/*Adapter.java`、`payments/OriginalPaymentRefundGateway.java`。新AI/翻译/map协议位于 `integrationsdk/PublicServiceAdapters.java`（LIVE-only、无controller、consumer挂接另验）。通用接口桥接见LegacyCloudFunctionFacade/Bridge；目前明确被客服调用的只有supportAiReply，不能由11个桥接方法名推定11类供应商业务都已接通。

本轮负责方：框架/模式与consumer整合由root；SDK工厂和依赖由product_checkout；AI/翻译/map公开协议由email_delivery；微信/订阅/TRTC/支付宝协议由refunds_finance；配置界面由commerce_frontend；独立后端审查由independent_review。本表由refund_frontend创建并交给independent_review维护最终状态。最终应逐行补上实际消费者、测试文件及真实联调结果，不能把“SDK已加载”改写成“46厂商已可用”。
