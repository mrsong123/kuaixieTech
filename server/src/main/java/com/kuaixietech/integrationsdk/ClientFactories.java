package com.kuaixietech.integrationsdk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.kuaixietech.integrationsdk.SdkContext.Mode;

/** Product-scoped SDK factories. Construction and validation never invoke provider APIs.
 * No application keys, defaults, ADC, machine credentials or mode fallbacks are read here.
 * SDK-configuration readiness is deliberately separate from business/provider verification. */
public final class ClientFactories {
 public record Descriptor(String category,String provider,String providerKey,Set<String> requiredFields,
   Set<String> secretFields,Set<Mode> supportedModes,String clientKind,boolean consumerWired,boolean businessVerified) {
  public Descriptor {requiredFields=Set.copyOf(requiredFields);secretFields=Set.copyOf(secretFields);supportedModes=Set.copyOf(supportedModes);}
 }
 private record Construction(Object client,AutoCloseable close){}
 public static final class ClientHandle implements AutoCloseable {
  private final SdkContext context;private final Descriptor descriptor;private Object client;private final AutoCloseable closing;private final AtomicBoolean closed=new AtomicBoolean();
  private ClientHandle(SdkContext context,Descriptor descriptor,Construction c){this.context=context;this.descriptor=descriptor;client=c.client();closing=c.close();}
  @JsonIgnore public SdkContext context(){return context;}public Descriptor descriptor(){return descriptor;}
  @JsonIgnore public Object client(){if(closed.get())throw new IllegalStateException("SDK_CLIENT_CLOSED");return client;}
  public <T>T client(Class<T> type){return type.cast(client());}
  public boolean closed(){return closed.get();}
  @Override public void close(){if(closed.compareAndSet(false,true)){try{closing.close();}catch(Exception e){throw new IllegalStateException("SDK_CLIENT_CLOSE_FAILED");}finally{client=null;}}}
  @Override public String toString(){return "SdkClientHandle[provider="+descriptor.providerKey()+", mode="+context.mode()+", revision="+context.configRevision()+", closed="+closed()+"]";}
 }
 private static Descriptor d(String category,String provider,String key,String kind,String required,String secrets,Mode...modes){return new Descriptor(category,provider,key,fields(required),fields(secrets),Set.of(modes),kind,false,false);}
 private static Set<String> fields(String names){return names.isBlank()?Set.of():Set.of(names.split(","));}
 private static final List<Descriptor> DESCRIPTORS=List.of(
  d("WXPAY","weixin","wechat/pay","official-wechatpay-native","merchant_id,merchant_serial,private_key_pem,platform_public_key_pem,platform_public_key_id,api_v3_key","private_key_pem,api_v3_key",Mode.LIVE),
  d("ALIPAY","alipay","alipay/pay","official-alipay-rsa2","app_id,private_key_pem,platform_public_key_pem","private_key_pem",Mode.SANDBOX,Mode.LIVE),
  d("MINIAPP","weixin","wechat/miniprogram","community-wxjava-miniapp","app_id,app_secret","app_secret",Mode.LIVE),
  d("SUBSCRIBE","weixin","wechat/subscribe","community-wxjava-miniapp-subscribe","app_id,app_secret","app_secret",Mode.LIVE),
  d("OAUTH","wecom","wecom/oauth","community-wxjava-wecom","corp_id,corp_secret,agent_id","corp_secret",Mode.LIVE),
  d("OAUTH","weixin","wechat/open","community-wxjava-qrconnect-oauth","app_id,app_secret","app_secret",Mode.LIVE),
  d("SMS","aliyun","aliyun/sms","official-aliyun-sms-api","access_key_id,access_key_secret,region","access_key_secret",Mode.LIVE),
  d("SMS","tencent","tencent/sms","official-tencent-sms-api","secret_id,secret_key,region","secret_key",Mode.LIVE),
  d("TRANSLATE","aliyun","aliyun/translate","official-aliyun-translate-api","access_key_id,access_key_secret,region","access_key_secret",Mode.LIVE),
  d("TRANSLATE","tencent","tencent/translate","official-tencent-translate-api","secret_id,secret_key,region","secret_key",Mode.LIVE),
  d("TRANSLATE","deepl","deepl/translate","official-deepl-http","api_key","api_key",Mode.LIVE),
  d("TRANSLATE","google","google/translate","official-google-v2-http","api_key","api_key",Mode.LIVE),
  d("FACE","aliyun","aliyun/face","official-aliyun-cloudauth-api","access_key_id,access_key_secret,region","access_key_secret",Mode.LIVE),
  d("FACE","tencent","tencent/faceid","official-tencent-faceid-api","secret_id,secret_key,region","secret_key",Mode.LIVE),
  d("PRIVACY_NUMBER","aliyun","aliyun/privacy-number","official-aliyun-number-api","access_key_id,access_key_secret,region","access_key_secret",Mode.LIVE),
  d("PRIVACY_NUMBER","tencent","tencent/privacy-number","official-tencent-number-api","secret_id,secret_key,region","secret_key",Mode.LIVE),
  d("RTC","aliyun","aliyun/rtc","official-aliyun-rtc-management-api","access_key_id,access_key_secret,region","access_key_secret",Mode.LIVE),
  d("RTC","tencent","tencent/trtc","official-tencent-usersig-signer","sdk_app_id,secret_key","secret_key",Mode.LIVE),
  d("RTC","agora","agora/rtc","official-agora-token-signer","app_id,app_certificate","app_certificate",Mode.LIVE),
  d("OSS","aliyun","aliyun/oss","official-aliyun-oss","access_key_id,access_key_secret,endpoint","access_key_secret",Mode.LIVE),
  d("OSS","tencent","tencent/cos","official-tencent-cos","secret_id,secret_key,region","secret_key",Mode.LIVE),
  d("OSS","huawei","huawei/obs","official-huawei-obs","access_key_id,access_key_secret,endpoint","access_key_secret",Mode.LIVE),
  d("OSS","qiniu","qiniu/kodo","official-qiniu-kodo","access_key,secret_key,region","secret_key",Mode.LIVE),
  d("OSS","aws","aws/s3","official-aws-s3-sync","access_key_id,secret_access_key,region","secret_access_key,session_token",Mode.LIVE),
  d("SEARCH","elasticsearch","elastic/search","official-elasticsearch-api-key","endpoint,api_key","api_key",Mode.LIVE)
 );
 private final URI proxyUri;private final Proxy proxy;
 public ClientFactories(){this(System.getenv("HTTPS_PROXY"));}
 /** Explicit transport setting only, useful for offline tests. It is never a source of provider secrets. */
 public ClientFactories(String proxyUrl){if(proxyUrl==null||proxyUrl.isBlank()){proxyUri=null;proxy=null;}else{URI u=URI.create(proxyUrl);if(u.getHost()==null||u.getUserInfo()!=null||!Set.of("http","https").contains(u.getScheme()))throw new IllegalArgumentException("SDK_PROXY_REJECTED");proxyUri=u;proxy=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(u.getHost(),u.getPort()<0?80:u.getPort()));}}
 public List<Descriptor> descriptors(){return DESCRIPTORS;}
 public boolean supports(String category,String provider){return DESCRIPTORS.stream().anyMatch(d->d.category().equals(category)&&d.provider().equals(provider));}
 public Descriptor descriptor(String category,String provider){return DESCRIPTORS.stream().filter(d->d.category().equals(category)&&d.provider().equals(provider)).findFirst().orElseThrow(()->new IllegalArgumentException("SDK_PROVIDER_UNSUPPORTED"));}
 public Descriptor descriptor(String providerKey){return DESCRIPTORS.stream().filter(d->d.providerKey().equals(providerKey)).findFirst().orElseThrow(()->new IllegalArgumentException("SDK_PROVIDER_UNSUPPORTED"));}
 public ClientHandle create(String category,String provider,Mode mode,long revision,Map<String,String> values){return create(new SdkContext(mode,descriptor(category,provider).providerKey(),revision,values));}
 public ClientHandle create(String category,String provider,Mode mode,Map<String,String> values){long revision;try{revision=Long.parseLong(values.get("config_revision"));}catch(Exception e){throw new IllegalArgumentException("SDK_CONFIG_REVISION_REQUIRED");}return create(category,provider,mode,revision,values);}
 public ClientHandle create(SdkContext context){Descriptor d=descriptor(context.providerKey());if(!d.supportedModes().contains(context.mode()))throw new IllegalArgumentException("SDK_MODE_UNSUPPORTED");for(String field:d.requiredFields())value(context,field);try{return new ClientHandle(context,d,construct(context));}catch(Exception|LinkageError e){throw new IllegalStateException("SDK_CLIENT_CONFIGURATION_REJECTED");}}
 public Map<String,Object> validateLocal(SdkContext context){try(var client=create(context)){return Map.of("status","local_configured","providerKey",context.providerKey(),"mode",context.mode().name(),"configRevision",context.configRevision(),"clientKind",client.descriptor().clientKind(),"consumerWired",false,"businessVerified",false);}}
 static String value(SdkContext context,String field){String value=context.values().get(field);if(value==null||value.isBlank())throw new IllegalArgumentException("SDK_REQUIRED_FIELD_MISSING");return value;}
 private String optional(SdkContext context,String field){return context.values().getOrDefault(field,"");}
 private static String https(String endpoint){URI u=URI.create(endpoint);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||u.getFragment()!=null||u.getRawQuery()!=null||u.getPort()!=-1&&u.getPort()!=443)throw new IllegalArgumentException("SDK_HTTPS_ENDPOINT_REQUIRED");return u.toString();}
 private Construction held(Object client){return new Construction(client,()->{});}
 /** Package-private only so offline tests can inspect linkage errors using synthetic credentials. */
 Construction construct(SdkContext c)throws Exception{return switch(c.providerKey()){
  case "wechat/pay"->wechat(c);
  case "alipay/pay"->alipay(c);
  case "wechat/miniprogram","wechat/subscribe"->miniapp(c);
  case "wechat/open"->wxOpen(c);
  case "wecom/oauth"->wecom(c);
  case "aliyun/sms"->held(new com.aliyun.dysmsapi20170525.Client(ali(c,"dysmsapi.aliyuncs.com")));
  case "aliyun/translate"->held(new com.aliyun.alimt20181012.Client(ali(c,"mt.aliyuncs.com")));
  case "aliyun/face"->held(new com.aliyun.cloudauth20190307.Client(ali(c,"cloudauth.aliyuncs.com")));
  case "aliyun/privacy-number"->held(new com.aliyun.dyplsapi20170525.Client(ali(c,"dyplsapi.aliyuncs.com")));
  case "aliyun/rtc"->held(new com.aliyun.rtc20180111.Client(ali(c,"rtc.aliyuncs.com")));
  case "tencent/sms"->held(new com.tencentcloudapi.sms.v20210111.SmsClient(tencentCredential(c),value(c,"region"),tencent(c,"sms.tencentcloudapi.com")));
  case "tencent/translate"->held(new com.tencentcloudapi.tmt.v20180321.TmtClient(tencentCredential(c),value(c,"region"),tencent(c,"tmt.tencentcloudapi.com")));
  case "tencent/faceid"->held(new com.tencentcloudapi.faceid.v20180301.FaceidClient(tencentCredential(c),value(c,"region"),tencent(c,"faceid.tencentcloudapi.com")));
  case "tencent/privacy-number"->held(new com.tencentcloudapi.npp.v20190823.NppClient(tencentCredential(c),value(c,"region"),tencent(c,"npp.tencentcloudapi.com")));
  case "tencent/trtc"->held(new TrtcTokens(Long.parseLong(value(c,"sdk_app_id")),value(c,"secret_key")));
  case "agora/rtc"->held(new AgoraTokens(value(c,"app_id"),value(c,"app_certificate")));
  case "aliyun/oss"->oss(c);
  case "tencent/cos"->cos(c);
  case "huawei/obs"->obs(c);
  case "qiniu/kodo"->qiniu(c);
  case "aws/s3"->s3(c);
  case "deepl/translate"->deepl(c);
  case "google/translate"->google(c);
  case "elastic/search"->elasticsearch(c);
  default->throw new IllegalArgumentException("SDK_PROVIDER_UNSUPPORTED");
 };}
 private com.aliyun.teaopenapi.models.Config ali(SdkContext c,String endpoint){var config=new com.aliyun.teaopenapi.models.Config().setAccessKeyId(value(c,"access_key_id")).setAccessKeySecret(value(c,"access_key_secret")).setRegionId(value(c,"region")).setEndpoint(endpoint).setProtocol("https").setConnectTimeout(8000).setReadTimeout(20000);if(proxyUri!=null)config.setHttpProxy(proxyUri.toString()).setHttpsProxy(proxyUri.toString());return config;}
 private com.tencentcloudapi.common.Credential tencentCredential(SdkContext c){return new com.tencentcloudapi.common.Credential(value(c,"secret_id"),value(c,"secret_key"));}
 private com.tencentcloudapi.common.profile.ClientProfile tencent(SdkContext c,String endpoint){var http=new com.tencentcloudapi.common.profile.HttpProfile();http.setEndpoint(endpoint);http.setProtocol("https");http.setConnTimeout(8);http.setReadTimeout(20);if(proxyUri!=null){http.setProxyHost(proxyUri.getHost());http.setProxyPort(proxyPort());}var profile=new com.tencentcloudapi.common.profile.ClientProfile();profile.setHttpProfile(http);return profile;}
 private int proxyPort(){return proxyUri.getPort()<0?80:proxyUri.getPort();}
 private Construction wechat(SdkContext c){String apiKey=value(c,"api_v3_key");if(apiKey.getBytes(StandardCharsets.UTF_8).length!=32)throw new IllegalArgumentException("SDK_API_V3_KEY_LENGTH");var config=new com.wechat.pay.java.core.RSAPublicKeyConfig.Builder().merchantId(value(c,"merchant_id")).merchantSerialNumber(value(c,"merchant_serial")).privateKey(value(c,"private_key_pem")).publicKey(value(c,"platform_public_key_pem")).publicKeyId(value(c,"platform_public_key_id")).apiV3Key(apiKey).build();var builder=new okhttp3.OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false).connectTimeout(Duration.ofSeconds(8)).readTimeout(Duration.ofSeconds(20)).writeTimeout(Duration.ofSeconds(20));if(proxy!=null)builder.proxy(proxy);var http=builder.build();var sdkHttp=new com.wechat.pay.java.core.http.DefaultHttpClientBuilder().config(config).okHttpClient(http).build();var client=new com.wechat.pay.java.service.payments.nativepay.NativePayService.Builder().config(config).httpClient(sdkHttp).build();return new Construction(client,()->{http.dispatcher().executorService().shutdown();http.connectionPool().evictAll();if(http.cache()!=null)http.cache().close();});}
 private Construction alipay(SdkContext c){String host=c.mode()==Mode.SANDBOX?"https://openapi-sandbox.dl.alipaydev.com/gateway.do":"https://openapi.alipay.com/gateway.do";var builder=com.alipay.api.DefaultAlipayClient.builder(host,value(c,"app_id"),pemBody(value(c,"private_key_pem"))).alipayPublicKey(pemBody(value(c,"platform_public_key_pem"))).format("json").charset("UTF-8").signType("RSA2").connectTimeout(8000).readTimeout(20000);if(proxyUri!=null)builder.proxyHost(proxyUri.getHost()).proxyPort(proxyPort());return held(builder.build());}
 private static String pemBody(String pem){return pem.replaceAll("-----[^-]+-----","").replaceAll("\\s","");}
 private Construction miniapp(SdkContext c){var config=new cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl();config.setAppid(value(c,"app_id"));config.setSecret(value(c,"app_secret"));if(proxyUri!=null){config.setHttpProxyHost(proxyUri.getHost());config.setHttpProxyPort(proxyPort());}var service=new cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl();service.setWxMaConfig(config);return held(service);}
 private Construction wxOpen(SdkContext c){var config=new me.chanjar.weixin.open.api.impl.WxOpenInMemoryConfigStorage();if(proxyUri!=null){config.setHttpProxyHost(proxyUri.getHost());config.setHttpProxyPort(proxyPort());}return held(new me.chanjar.weixin.open.api.impl.WxOpenOAuth2ServiceImpl(value(c,"app_id"),value(c,"app_secret"),config));}
 private Construction wecom(SdkContext c){var config=new me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl();config.setCorpId(value(c,"corp_id"));config.setCorpSecret(value(c,"corp_secret"));config.setAgentId(Integer.valueOf(value(c,"agent_id")));if(proxyUri!=null){config.setHttpProxyHost(proxyUri.getHost());config.setHttpProxyPort(proxyPort());}var service=new me.chanjar.weixin.cp.api.impl.WxCpServiceImpl();service.setWxCpConfigStorage(config);return held(service);}
 private Construction oss(SdkContext c){String endpoint=https(value(c,"endpoint"));var config=new com.aliyun.oss.ClientBuilderConfiguration();config.setProtocol(com.aliyun.oss.common.comm.Protocol.HTTPS);config.setConnectionTimeout(8000);config.setSocketTimeout(20000);if(proxyUri!=null){config.setProxyHost(proxyUri.getHost());config.setProxyPort(proxyPort());}var client=new com.aliyun.oss.OSSClientBuilder().build(endpoint,value(c,"access_key_id"),value(c,"access_key_secret"),config);return new Construction(client,client::shutdown);}
 private Construction cos(SdkContext c){var config=new com.qcloud.cos.ClientConfig(new com.qcloud.cos.region.Region(value(c,"region")));config.setHttpProtocol(com.qcloud.cos.http.HttpProtocol.https);config.setCheckSSLCertificate(true);config.setConnectionTimeout(8000);config.setSocketTimeout(20000);if(proxyUri!=null){config.setHttpProxyIp(proxyUri.getHost());config.setHttpProxyPort(proxyPort());}var client=new com.qcloud.cos.COSClient(new com.qcloud.cos.auth.BasicCOSCredentials(value(c,"secret_id"),value(c,"secret_key")),config);return new Construction(client,client::shutdown);}
 private Construction obs(SdkContext c){var config=new com.obs.services.ObsConfiguration();config.setEndPoint(https(value(c,"endpoint")));config.setConnectionTimeout(8000);config.setSocketTimeout(20000);if(proxyUri!=null)config.setHttpProxy(proxyUri.getHost(),proxyPort(),null,null);var client=new com.obs.services.ObsClient(value(c,"access_key_id"),value(c,"access_key_secret"),config);return new Construction(client,client::close);}
 public record QiniuClients(com.qiniu.util.Auth auth,com.qiniu.storage.UploadManager upload,com.qiniu.storage.BucketManager buckets){@Override public String toString(){return "QiniuClients[configured]";}}
 private Construction qiniu(SdkContext c){var region=switch(value(c,"region")){case "z0"->com.qiniu.storage.Region.region0();case "z1"->com.qiniu.storage.Region.region1();case "z2"->com.qiniu.storage.Region.region2();case "na0"->com.qiniu.storage.Region.regionNa0();case "as0"->com.qiniu.storage.Region.regionAs0();default->throw new IllegalArgumentException("SDK_QINIU_REGION_UNSUPPORTED");};var config=new com.qiniu.storage.Configuration(region);config.useHttpsDomains=true;config.connectTimeout=8;config.readTimeout=20;config.writeTimeout=20;if(proxyUri!=null)config.proxy=new com.qiniu.http.ProxyConfiguration(proxyUri.getHost(),proxyPort());var auth=com.qiniu.util.Auth.create(value(c,"access_key"),value(c,"secret_key"));return held(new QiniuClients(auth,new com.qiniu.storage.UploadManager(config),new com.qiniu.storage.BucketManager(auth,config)));}
 private Construction s3(SdkContext c){var credentials=optional(c,"session_token").isBlank()?software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(value(c,"access_key_id"),value(c,"secret_access_key")):software.amazon.awssdk.auth.credentials.AwsSessionCredentials.create(value(c,"access_key_id"),value(c,"secret_access_key"),value(c,"session_token"));var http=software.amazon.awssdk.http.apache.ApacheHttpClient.builder().connectionTimeout(Duration.ofSeconds(8)).socketTimeout(Duration.ofSeconds(20));if(proxyUri!=null)http.proxyConfiguration(software.amazon.awssdk.http.apache.ProxyConfiguration.builder().endpoint(proxyUri).useSystemPropertyValues(false).useEnvironmentVariableValues(false).build());var client=software.amazon.awssdk.services.s3.S3Client.builder().region(software.amazon.awssdk.regions.Region.of(value(c,"region"))).credentialsProvider(software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(credentials)).httpClientBuilder(http).build();return new Construction(client,client::close);}
 private Construction deepl(SdkContext c){var options=new com.deepl.api.TranslatorOptions().setTimeout(Duration.ofSeconds(20)).setMaxRetries(0).setSendPlatformInfo(false);if(proxy!=null)options.setProxy(proxy);return held(new com.deepl.api.Translator(value(c,"api_key"),options));}
 private Construction google(SdkContext c)throws Exception{var builder=new com.google.api.client.http.javanet.NetHttpTransport.Builder();if(proxy!=null)builder.setProxy(proxy);var transport=builder.build();var client=new com.google.api.services.translate.Translate.Builder(transport,com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),request->{request.setConnectTimeout(8000);request.setReadTimeout(20000);request.setFollowRedirects(false);}).setRootUrl("https://translation.googleapis.com/").setApplicationName("KuaixieTech").setTranslateRequestInitializer(new com.google.api.services.translate.TranslateRequestInitializer(value(c,"api_key"))).build();return new Construction(client,transport::shutdown);}
 private Construction elasticsearch(SdkContext c)throws Exception{URI endpoint=URI.create(https(value(c,"endpoint")));var mapper=new co.elastic.clients.json.jackson.Jackson3JsonpMapper();var builder=co.elastic.clients.transport.rest5_client.low_level.Rest5Client.builder(new org.apache.hc.core5.http.HttpHost("https",endpoint.getHost(),endpoint.getPort()<0?443:endpoint.getPort())).setDefaultHeaders(new org.apache.hc.core5.http.Header[]{new org.apache.hc.core5.http.message.BasicHeader("Authorization","ApiKey "+value(c,"api_key"))}).setHttpClientConfigCallback(http->{http.disableRedirectHandling();http.disableAutomaticRetries();}).setRequestConfigCallback(config->config.setResponseTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(20))).setConnectionConfigCallback(config->{config.setConnectTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(8));config.setSocketTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(20));});if(endpoint.getRawPath()!=null&&!endpoint.getRawPath().isBlank()&&!endpoint.getRawPath().equals("/"))builder.setPathPrefix(endpoint.getRawPath());if(proxyUri!=null)builder.setProxy(new org.apache.hc.core5.http.HttpHost(proxyUri.getScheme(),proxyUri.getHost(),proxyPort()));var http=builder.build();try{var transport=new co.elastic.clients.transport.rest5_client.Rest5ClientTransport(http,mapper);return new Construction(new co.elastic.clients.elasticsearch.ElasticsearchClient(transport),transport::close);}catch(Exception|LinkageError e){http.close();throw e;}}
 public static final class TrtcTokens {
  private final com.tencentyun.TLSSigAPIv2 signer;
  TrtcTokens(long id,String secret){if(id<1||secret.isBlank())throw new IllegalArgumentException("SDK_TRTC_CONFIG_INVALID");signer=new com.tencentyun.TLSSigAPIv2(id,secret);}
  public String sign(String user,long ttlSeconds){if(user==null||user.isBlank()||ttlSeconds<1||ttlSeconds>3600)throw new IllegalArgumentException("SDK_TOKEN_SCOPE_INVALID");return signer.genUserSig(user,ttlSeconds);}
  @Override public String toString(){return "TrtcTokens[configured]";}
 }
 public static final class AgoraTokens {
  private final String appId,certificate;private final io.agora.media.RtcTokenBuilder2 signer=new io.agora.media.RtcTokenBuilder2();
  AgoraTokens(String appId,String certificate){if(!appId.matches("[a-fA-F0-9]{32}")||!certificate.matches("[a-fA-F0-9]{32}"))throw new IllegalArgumentException("SDK_AGORA_CONFIG_INVALID");this.appId=appId;this.certificate=certificate;}
  public String sign(String channel,String account,int ttlSeconds){if(channel==null||channel.isBlank()||account==null||account.isBlank()||ttlSeconds<1||ttlSeconds>3600)throw new IllegalArgumentException("SDK_TOKEN_SCOPE_INVALID");return signer.buildTokenWithUserAccount(appId,certificate,channel,account,io.agora.media.RtcTokenBuilder2.Role.ROLE_PUBLISHER,ttlSeconds,ttlSeconds);}
  @Override public String toString(){return "AgoraTokens[configured]";}
 }
}
