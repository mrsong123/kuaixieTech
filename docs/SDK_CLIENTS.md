# SDK 客户端兼容性与接入边界

本轮提供显式配置快照驱动的 typed SDK 工厂，不将“依赖存在”“客户端能初始化”视为业务对接完成。`ClientFactories.Descriptor` 中 `consumerWired`、`businessVerified` 均为 false；具体业务消费者与真实验收另见 COUPLER_COVERAGE 文档及对应业务模块。

## 已锁定依赖

所有版本为 Maven Central 实际可解析的固定版本；不使用动态 latest、整套云厂商巨型 SDK 或 Spring Boot 3 starter。

| Maven 坐标 | 版本 | 用途与边界 |
|---|---|---|
| com.github.wechatpay-apiv3:wechatpay-java | 0.2.17 | 官方微信支付；本地公钥配置，初始化不自动下载平台证书 |
| com.alipay.sdk:alipay-sdk-java | 4.40.1061.ALL | 官方支付宝 RSA2；显式沙箱/生产网关 |
| com.github.binarywang:weixin-java-miniapp / weixin-java-open / weixin-java-cp | 4.8.6.B | WxJava 社区 SDK，非微信官方 SDK；小程序、订阅、扫码 OAuth、企业微信 |
| com.aliyun:dysmsapi20170525 | 4.6.0 | 短信 API |
| com.aliyun:alimt20181012 | 1.5.2 | 翻译 API |
| com.aliyun:cloudauth20190307 | 3.14.0 | 实人认证 API |
| com.aliyun:dyplsapi20170525 | 3.5.1 | 隐私号码 API |
| com.aliyun:rtc20180111 | 5.3.5 | RTC 管理 API，非媒体传输或通话验收 |
| com.aliyun.oss:aliyun-sdk-oss | 3.18.5 | OSS |
| com.tencentcloudapi:tencentcloud-sdk-java-sms / trtc / faceid / tmt / npp | 3.2.18 / 3.2.29 / 3.2.31 / 3.2.6 / 3.2.6 | 产品级 API SDK；common 统一锁定 3.2.50 |
| com.qcloud:cos_api | 5.6.283 | COS |
| software.amazon.awssdk:s3 / apache-client | 2.57.8 | 同步 S3，显式静态/会话凭据 |
| com.huaweicloud:esdk-obs-java | 3.26.9 | OBS |
| com.huaweicloud.sdk:huaweicloud-sdk-msgsms | 3.1.222 | 短信应用/签名/模板管理 API；不是 appKey/secret WSSE 短信发送，未注册发送工厂 |
| com.qiniu:qiniu-java-sdk | 7.19.0 | Kodo；显式区域 |
| com.deepl.api:deepl-java | 1.17.0 | 翻译；api_key |
| com.google.cloud:google-cloud-translate | 2.100.0 | 仅使用其官方 v2 HTTP 客户端与 api_key，不使用 ADC 或 v3 gRPC |
| co.elastic.clients:elasticsearch-java | 9.5.5 | Rest5 + Jackson3 + ApiKey |
| io.agora:authentication | 2.1.3 | 本地 RTC token 签发 |
| com.github.tencentyun:tls-sig-api-v2 | 2.0 | 本地 TRTC UserSig；sdk_app_id + secret_key 为 UserSig 专用签名凭据 |
| org.bouncycastle:bcprov-jdk18on / bcpkix-jdk18on | 1.84 | 统一加密实现，排除 SDK 引入的旧 jdk15on 重复类 |

## 运行契约与隔离

`SdkContext(mode, providerKey, configRevision, values)` 必须显式提供模式与正数配置版本，复制 values，日志字符串只展示字段名。工厂不读业务密钥环境变量、实例角色或默认凭据链。每个 handle 持有该不可变快照，禁止当前模式变化重解释历史会话。带 Map 的简化 create 重载也要求 config_revision。

只有支付宝工厂显式支持 SANDBOX 和 LIVE；其余当前工厂仅 LIVE。未提供公开独立沙箱的厂商不会把 SANDBOX 偷换成生产网关。工厂本地校验不发送请求，不能验证密钥有效性、商户签约、产品开通或配额。密钥格式的校验深度因厂商 SDK 而异，不能据此接受真实交易。

网络客户端沿用 HTTPS_PROXY（仅运输配置），使用正常 CA/主机校验，不安装信任所有证书的实现。可配置服务端点仅接受 HTTPS。AWS 不读取机器/环境默认凭据；Google 不启动 ADC 探测。支付证书、key、SDK 异常不回显到校验结果。句柄可幂等关闭；独占客户端关闭连接池。部分 SDK 仅暴露进程共享运输器、无实例 close，此类 handle 释放引用，不全局关闭其他业务共用连接池。

TRTC 的 secret_key 是 SDK UserSig 密钥，不能用云 API SecretId/SecretKey 替代；Agora 使用 App ID/App Certificate。两者这里只签发限时凭据，不代表已接通音视频。WxJava 的 OAuth 客户端也不代表小程序、企业微信已通过平台审核。

## 实际验证与未完成项

在 JDK 21、当前 Spring Boot 4 管理依赖下，两个工厂源码独立 javac 编译通过。25/25 描述符用合成凭据完成真实 SDK 对象初始化与关闭；独立进程以 SecurityManager 拒绝所有出站连接，结果 `passed=25 total=25 attempted_network_connections=0`。SecurityManager 只用于该离线验证，未加入应用。补充 JUnit 测试覆盖快照不可变/脱敏、缺失凭据、无沙箱回退、显式版本、token 范围和幂等关闭。

依赖裁剪后的验证 classpath 共 288 jars，约 163.1MB；没有 io.grpc、io.netty 或旧 jdk15on BouncyCastle。Google v3/gRPC 与 S3 Netty 异步运输器被排除；本模块只构造 HTTP/同步客户端。完整业务测试由项目统一 Maven 回归执行，离线初始化不替代真实厂商沙箱验收。

原生 JDK REST 已适合公开 HTTP 协议服务，无需为每家重复引入 SDK。航空公司、12306、银行、保险和私有物流没有可验证公开协议/商务授权时，不虚构 SDK 坐标或业务接口。新增可用能力仍需明确协议、字段映射、业务消费者、回执验签、幂等/退款或撤销、真实凭据及受控验收；仅填写 key 不保证所有业务可用。
