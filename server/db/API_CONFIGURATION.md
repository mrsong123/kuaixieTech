# API 配置中心、短信与私有附件

## 配置和秘密存储

运营后台“设置 → API 与服务配置中心”管理第三方参数、凭据组和内部控制器声明清单；支付/FX 与保险仍有专业配置表单，作为同一配置页的独立区块。内部路由随代码部署，不允许管理员任意修改权限和调用路径。

复用现有 t_integration_vendors/products/product_fields/settings/secrets。只在管理员明确保存时补入已知元数据；不在启动时执行 DDL、不自动导入原始种子、不自动迁移旧明文或未知密文。参考适配器可存草稿但不能启用。已有旧 channel 配置保留兼容，不自动复制到新运行时。

部署环境提供 `INTEGRATION_CONFIG_KEY`，为 Base64 编码的随机 32 字节 AES 密钥。所有配置值按随机 IV 的 AES-256-GCM 保存，附加认证数据绑定 vendor/product/field，跨字段复制或旧密钥无法解密。根密钥不通过网页设置，须随部署安全保存及备份；更换根密钥须先完成受控重加密，不能简单替换后期待旧值可用。

秘密不返回前端，不保存到审计详情；管理员只得到是否保存/能否解密。空输入保留原值，清除必须明确勾选。`env:VARIABLE_NAME` 可引用环境秘密。已存在数据库配置（包括停用配置）优先于原环境回退；停用不静默启用旧密钥。

凭据组名对应 payment/fx credentialPrefix，后缀支持 APP_ID、MERCHANT_ID、MERCHANT_SERIAL、PLATFORM_SERIAL、PRIVATE_KEY_PEM、PLATFORM_PUBLIC_KEY_PEM、SIGNING_SECRET、BEARER_TOKEN、API_KEY。私钥使用 PKCS#8；平台公钥可为 PEM 公钥或有效证书。原环境 `_FILE` 方式继续兼容。商户身份快照仍锁定 APP_ID/MERCHANT_ID，换商户后不能用旧订单自动记账。

自定义 HTTPS 桥接/身份接口还需 `INTEGRATION_API_ALLOWED_HOSTS` 精确主机授权及部署网络放行。TLS 验证和会话代理保持启用，不跟随重定向，不提供任意 URL 代理。原生公开厂商端点由代码约束。密文保存及启用不代表真实联调成功。

## 短信协议

- 阿里云 Dysmsapi 2017-05-25 SendSms：RPC HMAC-SHA1、RFC3986 参数编码，模板采用单个 code 变量。当前为中国大陆短信产品，拒绝假冒支持境外号码。
- 腾讯云 SMS 2021-01-11 SendSms：TC3-HMAC-SHA256，模板参数列表仅含验证码；E.164 号码、SessionContext、请求 ID、返回号码及 SerialNo 校验。境外能力取决于实际账号和模板合同。
- 原生短信仅允许一个供应商启用。每分钟处理至多 20 条 pending；FOR UPDATE SKIP LOCKED 领取并单独提交 blocked 状态后，才调用服务商。超时或无法确认的响应不自动重发，避免重复收费。sent 表示供应商受理，不是终端送达。后台历史不返回验证码或手机号，旧错误脱敏。
- 过期未发送记录转 failed；终态清除队列验证码，过期未决记录也清除验证码而保留未决状态。真实验证表、代码用途、频控和额度仍需完整生产复核。
- 有原生短信配置时不再触发旧的整队列桥接；邮件不会由短信适配器投递。旧桥接仍需显式部署开关。登录界面只显示请求已受理，不伪称已经送达。

核对的公开 SDK 来源：
[Tencent SendSmsRequest](https://github.com/TencentCloud/tencentcloud-sdk-java/blob/master/src/main/java/com/tencentcloudapi/sms/v20210111/models/SendSmsRequest.java)、[Tencent TC3](https://github.com/TencentCloud/tencentcloud-sdk-java/blob/master/src/main/java/com/tencentcloudapi/common/AbstractClient.java)、[Alibaba SendSmsRequest](https://github.com/aliyun/aliyun-openapi-java-sdk/blob/master/aliyun-java-sdk-dysmsapi/src/main/java/com/aliyuncs/dysmsapi/model/v20170525/SendSmsRequest.java)。测试使用独立计算的固定签名样例和模拟回执，没有实际发送。

## 私有附件

原生实现阿里 OSS REST V1 和腾讯 COS XML V5 的私有 PUT/GET 签名，强制 private ACL、Content-MD5 和 ETag 校验。成功回执后才写 t_community_uploads；读取再次校验 SHA-256 和字节数。上传失败或数据库写入失败可能留下无业务引用的对象，尚需回收任务，不会返回虚假附件记录。

`platform/storage-routing` 可选择新上传使用的 aliyun 或 tencent。只配置一个服务时可直接使用；启用两个服务时必须选择路由，避免不明确的写入目标。历史下载按记录的供应商与 bucket 读取；不能把旧对象重解释为新 bucket。保留旧供应商读取配置后，可切换新上传路由。

接口 `/me/uploads` 支持 store_license、insurance_certificate、dispute_evidence，用于私有材料，不是公开商品图/CDN。最多 10MB，接受 JPEG/PNG/PDF 文件标记；不等于病毒扫描或证件真实性核验。后端文件流有大小限制，下载由本人或管理员授权，返回 attachment、no-store、nosniff。前端不把 JWT 放到下载 URL。

PC 用 multipart 上传和认证二进制下载；H5/小程序/App 用原生文件选择、uni.uploadFile、认证下载及文档/图片查看。原生当前选择图片，PC 也可选 PDF。用途和业务授权共享后端，不同终端不冒充拥有不存在的系统能力。

公开签名来源：[OSSRequestSigner](https://github.com/aliyun/aliyun-oss-java-sdk/blob/master/src/main/java/com/aliyun/oss/internal/OSSRequestSigner.java)、[COSSigner](https://github.com/tencentyun/cos-java-sdk-v5/blob/master/src/main/java/com/qcloud/cos/auth/COSSigner.java)。真实 bucket、区域、密钥与访问策略仍待部署联调。

## 本轮补齐邮件与原生投递接管

阿里云 DirectMail SingleSendMail 2015-11-23 已实现公开 RPC HMAC-SHA1 协议。通过 aliyun/directmail 配置发件身份、区域、别名、主题及纯文本模板；占位仅 {code}/{minutes}，剩余分钟按排队后的有效期计算。固定官方区域端点，服务商成功 RequestId 只表示受理，不表示收件箱送达。先提交 email 专属 blocked 领取，再网络调用，未决不自动重发，终态/过期清理验证码；号码/邮箱/验证码均不在管理列表返回。

短信与邮件独立运行，任一通道配置或请求异常不饿死另一通道。存在任意 aliyun/sms、tencent/sms 或 aliyun/directmail 数据库设置行（含草稿/停用，含短信保存时预置的停用行）即认为原生接管；不会回退旧整队列桥接。配置页显式显示 nativeDeliveryManaged，旧桥接只有在未被原生接管、两个原生通道都未就绪、无未知错误且部署开关开启时才可使用。

DirectMail 公开 SDK 参考：[SingleSendMailRequest](https://github.com/aliyun/aliyun-openapi-java-sdk/blob/master/aliyun-java-sdk-dm/src/main/java/com/aliyuncs/dm/model/v20151123/SingleSendMailRequest.java)。实际发件域名授权、签名身份、地域账号能力及终端送达仍待真实联调，本轮未发送邮件。
