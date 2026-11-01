# 本轮验证回执汇总

日期：2026-11-01。源码基线为 `643e42c` 加本轮提交的修复。下面结果只证明各自覆盖范围，不提升113表1186字段的完整验收状态。

| 范围 | 实际结果 | 可复现入口与限制 |
| --- | --- | --- |
| 当前Spring应用 | 417项，0失败/错误/跳过 | `mvn -f server/pom.xml -Pcurrent-app-tests test`；JDK21，本环境使用Mockito显式javaagent。默认历史包测试不包含在此结果 |
| 前端业务模型 | 17组359项断言通过 | 从client运行下表17个脚本；离线模型/真实Vue模板断言，不代表真机或真实供应商 |
| 编译器准备 | 14项断言及隔离CLI真实编译通过；13个实际编译入口检查通过 | `pnpm test:compiler-setup`、`pnpm doctor:mobile`；用户Windows/HBuilderX GUI仍待验 |
| 五端资源 | PC、admin、H5、mp-weixin、App全部构建通过 | `pnpm build:pc`、`build:admin`、`build:mobile`、`build:mp-weixin`、`build:app`；App资源不是签名安装包 |
| 微信产物 | 83页、87处组件引用、339静态依赖通过 | 构建脚本自动检查app.json与文件存在性；touristappid不能用于正式登录支付 |
| 当前前端全链检查 | `pnpm check`与`pnpm verify:legacy-install`均通过 | 缺失的外部历史CSS/62页台账独立显示NOT RUN，未冒称外部原始资料已比对；CSS/Taro/台账三种严格模式已确认缺原件会失败 |
| Python审计工具 | 17项通过 | `python3 -m unittest discover -s server/tools -p 'test_*.py'` |
| SQL/结构静态审计 | 113表1186字段备注齐全；150条字面量INSERT未发现必填列遗漏；12份迁移汇总一致 | `schema_comments.py --check`、`audit_schema.py`、`audit_required_inserts.py`、`build_deployment_sql.py --check`；不证明所有SQL语义已验 |
| 真实服务与MySQL | 33/33项HTTP/SQL检查通过 | [实跑说明](RUNTIME_VALIDATION.md)、[脱敏收据](RUNTIME_RECEIPT.json)；仅隔离官方MySQL8.4.13，无业务库/第三方渠道 |

## 17组前端模型断言

从 `client` 工作区使用Node>=22.12.0、pnpm10.34.9执行。

| pnpm脚本 | 断言数 |
| --- | ---: |
| test:business-models | 33 |
| test:product-checkout | 19 |
| test:payment-refunds | 23 |
| test:shop-orders | 10 |
| test:coupler-settings | 23 |
| test:product-media | 22 |
| test:escrow-refunds | 20 |
| test:publish-composer | 26 |
| test:shop-session | 22 |
| test:insurance-session | 16 |
| test:native-file-session | 15 |
| test:wallet-session | 23 |
| test:mobile-business | 19 |
| test:saved-session | 24 |
| test:workflow-session | 22 |
| test:admin-console-session | 26 |
| test:contact-delivery-health | 16 |
| 合计 | 359 |

实际执行过程日志留在本轮临时目录，不将包含运行上下文的原始服务日志加入仓库。真实服务逐项结果另以脱敏JSON保存。Windows安装和HBuilderX Node配置见[专用步骤](../../client/apps/mobile/HBUILDERX_SETUP.md)。

未验收：用户业务数据库、全部业务分支、并发/故障注入、真实短信邮件送达、支付与退款、银行/信用卡、保险企业、物流/媒体/实名、各端真机及备份恢复。
