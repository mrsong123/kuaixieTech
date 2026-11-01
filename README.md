# 快协随行购

前端工作区在 `client/`，包括 PC 官网、运营后台、UniApp H5、小程序与 App，共享 SDK 在 `client/packages/sdk/`。Java 21 / Spring Boot 后端在 `server/`。

当前云任务已隔离，直接使用现有检出；除非用户明确要求，不创建额外 Git worktree。

## 商品保险

已确认客户自主投保，由保险机构定价并直接收费；平台不代收或托管保费。机构直付和保单核验流程已实现，真实投保在完成机构协议适配与联调前保持关闭。真实承保须取得核验保单，机构参考名单不代表已接通。实现、配置与待完成条件见 [商品保险说明](server/db/GOODS_INSURANCE.md)。

## 跨端统一与全表闭环

当前执行进度、已接通功能与未完成项见 [跨端业务执行记录](docs/CROSS_PLATFORM_BUSINESS.md)。后台“业务与字段验收”页及 [全字段台账](docs/DATABASE_BUSINESS_COVERAGE.csv) 追踪原始 82 表与提议新增表；台账不代表业务验收通过。

## 全线落地进度

按页面、接口、数据和权限整理的 [验收清单](docs/DELIVERY_CHECKLIST.md) 与自动生成的 [接口缺口报告](client/docs/ROUTE_AUDIT.md) 记录已实现、待实现及尚未真实联调的功能。接口声明覆盖不等于业务验收通过。

## 数据库对齐

以用户提供的 SQL 为基准，原件保存在 `server/db/reference/current-schema.sql`。微信、银行卡及多币种方案见 [可配置支付设计](server/db/CONFIGURABLE_PAYMENTS.md)。迁移方案、已完成修改、未完成业务及验证限制见 [数据库对齐说明](server/db/MIGRATION.md)。不要把结构参考原件整体导入现有业务库；新增迁移尚未执行。

## 开发与验证

使用 Java **JDK** 21（需要 javac）、Maven 3.9+、Node.js 20.19+、pnpm 10.34.9。在云实例中，已校验安装的工具保存在 `/workspace/tooling`：

```sh
export PATH=/workspace/tooling/node_modules/.bin:/workspace/tooling/apache-maven-3.9.11/bin:$PATH
export JAVA_HOME="$(cat /workspace/tooling/jdk21-path)"
```

这些是当前实例保留的工具路径；新机器应先验证文件存在，不能假定运行中的服务会保留。Maven 在此云环境需要使用 `/workspace/tooling/maven-settings.xml` 中的会话代理，以及系统 CA 信任库；不要禁用 TLS 校验。

前端在 `client/` 执行 `pnpm test:contracts`、`pnpm build:all` 和 `pnpm build:app`。依赖仅在该工作区根目录安装，不在各 app 重复安装。`pnpm install --frozen-lockfile` 的历史视觉 postinstall 检查仍有失败，详见数据库对齐说明，不应把依赖下载完成报告为完整安装校验通过。

后端在 `server/` 执行：

```sh
mvn -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts \
  -s /workspace/tooling/maven-settings.xml -Pcurrent-app-tests test
python3 tools/audit_schema.py --include-proposal
```

当前应用 32 项测试已通过。默认 `mvn test` 包含无法编译的历史 `com.kuaixie.market` 测试，完整测试状态尚未通过。上述名称审计不替代 MySQL 集成测试。

在环境设置中配置 `DB_URL`、`DB_USER`、`DB_PASSWORD`、`APP_JWT_SECRET` 后，先按数据库说明执行只读元数据核验；不要在聊天、命令或代码中填写凭据。生产环境缺省关闭管理员自动初始化和 SQL 自动导入。后台启动命令为 `mvn -Pcurrent-app-tests spring-boot:run`，前端开发命令见 [多端部署说明](client/DEPLOY.md)。真实数据库启动与端到端流程尚待连接配置后验证。

商品纠纷已接通跨端申请、回应、平台裁决与售后队列；实际退款和保险赔付不以状态更新冒充完成，详见 [纠纷与售后说明](server/db/TRADE_DISPUTES.md)。

项目定位、与上一轮的逐项对比及剩余实施顺序见 [项目持续复盘](docs/PROJECT_REVIEW.md)。本轮增加统一 API 配置、原生短信、私有附件及店铺流程；[最终部署增量 SQL](server/db/deploy/kuaixieTech_incremental.sql) 已汇总，开发阶段未执行。

最新并行推进已加入[商品直接成单与库存](server/db/PRODUCT_CHECKOUT.md)、[原充值退款](server/db/ORIGINAL_PAYMENT_REFUNDS.md)、店铺订单归因及独立邮件投递。最新验证与未完成项以[持续复盘](docs/PROJECT_REVIEW.md)为准，所有字段仍待完整验收。
