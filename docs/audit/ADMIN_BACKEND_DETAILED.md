# 后台与业务服务逐项盘点

盘点基线：`643e42c`。本文件为源代码级审计，不连接业务数据库、不发短信/邮件、不付款/退款、不执行迁移。并行修复可能已处理其中部分发现，修复完成状态须以主报告和对应回归为准，不能将“发现时缺陷”一直视为最新状态。

## 本轮实施复盘（2026-11-01）

本节记录盘点后的具体落盘变化；下面导航表和静态事件索引仍保留发现时快照，不能当成最新代码行号或把已经修复的问题重复计入当前缺陷。主汇总报告记录其余代理的修复与整体验证。

- **通用列表25类**：`AdminConsoleRpcController.ListBody.offset`、`AdminConsoleService.list` 和25条 `AdminConsoleMapper` SQL已接入可选服务端分页；旧Java调用/未传offset默认0，负offset拒绝，limit保留1～200保护。排序尾键使用唯一id/code/virtual_number，避免同时间记录顺序不确定。
- **后台通用界面**：每页80条，向后端请求81条探测下一页，上一页/下一页真实改变SQL offset；并非读取完整列表后切片。用户筛选显式查询并回到首屏；section/账号切换清空列表和页偏移；页、筛选、账号的旧响应被拒绝。概览继续使用独立聚合接口。
- **仍有明确缺口**：违规记录与奖励分成两个专用接口仍最多读取80条明细，没有冒充通用分页，界面明确展示范围。它们的服务端分页/筛选、所有列表的业务详情与全字段出口，以及真实库数据大于80条的浏览器联调，仍是下一执行项。分页不会使任一字段自动成为验收完成。
- **专项验证**：`AdminConsolePagingTest` 6项通过，验证25种kind透传offset、旧参数兼容、用户筛选/limit界限、非法offset和kind拒绝、管理员前置校验、真实MyBatis XML绑定与稳定尾排序。初次沙箱执行因Mockito自附加受限而ERROR，显式加载Mockito javaagent后6项通过；该过程未连接数据库。
- **界面行为验证**：`client/scripts/verify-admin-console-session.mjs` 26项通过，执行真实Console脚本和模板编译，覆盖服务端分页请求/末页/上一页、筛选和页面乱序、账号ABA/卸载后响应拒绝、证据选择乱序、重复业务点击阻断及旧账号业务结果隔离。测试使用受控接口返回值，不能替代真实库、真机或渠道验收。

## 状态标准

- **已编码未真实运行**：可定位页面、handler、API和服务/SQL，不能据此认定系统已跑通。
- **部分实现/半功能**：存在一段流程，但缺业务出口、真实服务、分项核算或正确的运行证据。
- **未实现端到端**：不可达服务/仅目录/占位，不是完整功能。
- 本次没有提升任何数据库字段完整验收状态。113表/1186字段是参考结构与提议结构的计划值；当前业务库的真实结构未由本审计核验。

## 后台导航、按钮链和数据出口

后台导航通过 `useAdminStore.go(section)` 改内存状态，App根据section渲染组件；不是每个section都有浏览器URL路由。下面覆盖所有声明的30个业务tab及额外系统入口。通用table只展示COLUMNS过滤出的少量字段，展开主要展示操作按钮，不是所有被SQL取出的字段都有详情出口。

| 导航/页面 | 按钮与API→服务链 | 主要数据 | 开发状态 | 未闭环/后续 |
| --- | --- | --- | --- | --- |
| 运营看板 | refresh/loadStats → POST /rpc/admin_stats → AdminReviewRpcService.stats/AdminReviewRpcMapper.adminStats | 用户、订单GMV、服务费、佣金、审核计数 | 已编码未真实运行 | 缺todo_products计数；统计只来自业务表，不是渠道清算；没有日期/币种/地区筛选 |
| 带货人认证 | list(carrierKyc)；通过/驳回→/rpc/admin_set_carrier_verify→AdminConsoleService/Mapper | t_profiles carrier_verify_status/reason/verified_at；t_admin_logs | 部分实现 | 列表未展示证件/挑战照详情；人工更新身份，不是外部实名核验 |
| 买家认证 | list(buyerKyc)；通过/驳回→/rpc/admin_review_buyer_kyc→KycRpcMigrationService | t_profiles buyer_verify_status；t_kyc_documents；通知/审计 | 部分实现 | 审批页无原始证件查看/有效性证明；状态条件和真实核验证据需要补 |
| 企业认证 | list(enterpriseKyc)；通过/驳回→/rpc/admin_review_enterprise→AdminReviewRpcService | t_profiles enterprise_name/status；t_kyc_documents | 部分实现 | 仅人工状态更新；无工商/持牌KYC机构核验consumer；列表不展示完整执照凭据 |
| 行程审核 | list(trips)；通过/驳回→/rpc/admin_set_trip_verify→AdminConsoleService | t_trips verify_status/reason/verified_at | 部分实现 | 通用RPC仅改verify_status，不同步visibility；门户新审核REST同步visibility，两个入口需统一 |
| 凭证配方 | list；新增→/rpc/admin_upsert_recipe；启停→/rpc/admin_toggle_recipe | t_proof_recipes trip_type/transport_mode/country_code/items/active | 部分实现 | 后台可CRUD，但取证业务没有读取配方；默认flight与业务air命名还需统一；未验证items语义 |
| 商品治理 | list；待审通过/驳回→/rpc/admin_set_product_status（盘点时无路由）；在架下架→/rpc/admin_force_off_shelf | t_products；owner/status/price/stock | 半功能/缺路由 | 新发布off后本人上架，不自然产生pending；审核按钮调用缺失RPC；缺完整详情/分类/图片审查 |
| 分类管理 | list；新增→/rpc/admin_create_category；停用→/rpc/admin_deactivate_category | t_categories name/parent_id/level/active | 部分实现 | 页面新增固定parentId=null、level=1；不能完整维护三级分类、国家关联、排序/图标/禁运说明 |
| 异常日志 | list；解决→/rpc/admin_resolve_error→ErrorLogMigrationServiceImpl | t_error_logs scope/message/page/status/resolved_at | 部分实现 | 列表偏好kind但SQL是scope；缺完整日志/堆栈/关联请求及告警；resolve只状态落库 |
| 原充值退款 | PaymentRefunds → usePaymentRefunds → eligible/queue/detail/events/review/dispatch/refresh | t_currency_payments；t_payment_refunds/events；t_balance_ledger | 已实现本地逻辑，真实未验收 | CNY微信/支付宝原充值全退；审批与真实派发分离；银行卡/跨币种/自动长期对账待补 |
| 店铺审核与管理 | ShopCentre/admin→useShop：分页队列/执照查看/审核/经营状态；ShopOrders读取归因 | t_store_applications/t_stores/t_store_products；附件与商品订单归因 | 已实现本地逻辑，真实未验收 | 外部工商核验、境外证照和复杂店铺规则缺；本地资料审核不等于第三方核验 |
| 订单与取证 | list(orders)；查看证据→/rpc/get_order_evidence | t_orders；证据接口聚合采购/寄出/交付信息 | 部分实现 | 证据仅JSON文本；无完整事件/资金分项/图片查看操作台；不能从此完成全部履约及财务复核 |
| 取消裁定 | list(cancels)；通过/驳回→/rpc/admin_resolve_cancel→CancellationRpcService | t_cancel_requests/t_orders/t_deposits/t_balance_ledger/奖励 | 部分实现 | 列表reason与SQL reason_detail错位；取消台账已有本地修补，但平台剩余30%无独立资金台账核算 |
| 售后与纠纷 | TradeCases→回应/介入/裁决/申诉/履行；EscrowRefundPanel预算+明确确认+执行 | t_disputes/t_after_sales_cases/t_dispute_events/t_order_escrow_refunds/events | 部分实现 | 证明充分的新商品货款可退钱包；全额后awaiting_accounting无完整费用/保证金核算出口；旧/复杂订单被安全阻断 |
| 人工客服 | SupportAgentPanel：刷新队列/认领/发送→support.adminQueue/claim/agentReply/messages | t_support_conversations/t_support_conversation_messages等 | 部分实现 | 手动刷新，无完整结束/转接/离线提醒/SLA；AI回复、RTC媒体与外部通知另未闭环；需旧响应guard |
| 开票审核 | list；输入发票号；通过/驳回→/rpc/admin_issue_invoice→InvoiceRpcService | t_invoices invoice_no/file_url/status/issued_at；站内通知 | 半功能/通知不实 | 页面fileUrl固定空；服务只改记录便通知已开具且将送邮件，未接税务开票/邮件发送或凭证核验 |
| 违规记录 | /rpc/admin_list_violations；标已审→/rpc/admin_review_violation | t_chat_violations reviewed等 | 部分实现 | 可改复核状态，缺处罚申诉/模型误报/证据链及通知闭环 |
| 隐私号池 | list；释放过期→/rpc/admin_release_privacy→PrivacyReleaseMigrationService | t_privacy_numbers/t_privacy_bindings等 | 部分实现 | 仅数据库池与绑定释放；无实际隐私号机构拨号/录音回执接线 |
| 拼单 | list；强制成团→/rpc/admin_force_form_pool；解散→/rpc/admin_dissolve_pool | t_trips/t_orders/t_deposits/余额/通知 | 部分实现 | 人工状态/退款分支存在；采购后退款、平台费用台账和真实并发未验；新商品直接下单明确不支持pool |
| 求购审核 | list；公开审核→/rpc/admin_set_demand_visibility（盘点时无路由）；关闭→/rpc/admin_close_demand | t_demands/t_quotes；门户另有t_portal_demand_reviews | 半功能/缺路由 | SQL不投影visibility导致按钮隐藏；门户REST有审核，需消除双轨；完整报价/中标列表无正式后台详情 |
| 通知队列 | list(notifyQueue)，无发送/重试/回执按钮 | t_notify_outbox status/channel/reason | 部分实现 | 站内flush会真正插入通知；外部有配置仍只kept/pending，没有consumer即不发送；不可当已送达 |
| 提现 | 只读list(withdrawals)；没有申请/审核/派发正式页面或可达controller | t_withdrawals；未接线WithdrawMigrationService内核 | 未实现端到端 | 内核markPaid原逻辑不验真实支付就称微信零钱到账；属于潜伏危险内核，不是现可达RPC；需先封锁假成功再设计渠道与回执 |
| 社区治理 | PortalModerationPanel新动态/求购/行程待审；旧t_posts列表移除→/rpc/admin_remove_post | 新t_social_posts、旧t_posts两套数据模型 | 部分实现 | 新feed已公开动态缺同等下架/撤销处理，旧移除不能操作新表；评论/媒体投诉版权等未闭环 |
| 奖励与提成 | /rpc/admin_commission_rows、/rpc/admin_invite_tier_distribution | t_reward_ledger/t_profiles commission_mtd；RewardSettlementService | 部分实现 | 只列表/JSON、无筛选分页和人工复核处理台；追回失败/缺账户的null返回需核对；部分货款退款后奖励待核算 |
| 用户与资金调整 | list keyword；封禁/解禁、调余额、改评分、赠积分→admin RPC | t_profiles/t_balance_ledger/t_admin_logs/t_notifications | 部分实现/危险动作待加固 | 调账null原逻辑仍返回OK；UI无操作幂等/双审，分值/积分用Number；缺完整用户详情/认证/注销处理 |
| 资金台账 | 只读list(finance) | t_balance_ledger kind/amount/balance_after/order_id/note | 只读半功能 | 不是双分录总账，不含资金平衡、平台服务费、违约余款、银行账单、手续费/汇差和差错处理 |
| 邀请码 | list；创建→/rpc/admin_create_invite | t_invite_codes code/note/created_by/used_by/used_at | 部分实现 | 页面列used_count但SQL单次used_by，无使用统计/停用/批量导出/邀请滥用审核 |
| 广播与公告 | 表单→/rpc/admin_broadcast；列表映射adminLogs | 发站内t_notifications，列表是t_admin_logs | 部分实现 | 不是公告CMS；页面字段title和映射日志不同；无公告编辑、置顶、停用、定时发布/多语言编辑 |
| 旧通道配置 | 只读脱敏list；启停→/rpc/admin_toggle_channel | t_channel_configs/t_channel_secrets_probe | 兼容半功能 | 该切换仅更新旧配置；与新耦合器和发送consumer不是同一完成度；不能代表有key即有服务 |
| 语种管理 | list；新增/更新/启停/删除→3个locale RPC | t_locales code/native_name/label_zh/sort_order/active/fallback_code | 部分实现 | 存储locale元数据；不等于客户端已有翻译内容或第三种语言完整适配；缺fallback验证/发布同步 |
| 全部API工作台 | ApiWorkbench动态调用registry.operations→SDK.invoke | 71条旧registry操作；声明路由总计231（盘点基线） | 技术工具/覆盖不完整 | 新模块专用页面有接口，但全部API工具只71条，不等于全路由可操作；手动JSON不代正式业务表单 |
| 显示、接口与会话 | PreferencesPanel；token显示/应用/清除；进入集成页 | 本地偏好、SDK内存token、配置展示 | 已编码未真实运行 | 粘贴token不等于权限通过；服务端JWT仍必需；没有业务库迁移/密文根密钥的完整启动向导 |
| API配置中心 | CouplerSettingsPanel+PaymentSettingsPanel+InsuranceSettingsPanel+兼容IntegrationSettingsPanel | t_integration_runtime_state/mode_scopes/mode_configs/config_values、支付商户/FX、保险产品 | 配置窗口已实现/多数服务待实现 | 新keys密文版本、SANDBOX/LIVE隔离；无consumer不能selected；配置保存、本地构造和真业务验收分层，详见集成审计 |
| 业务整合台 | ShopCentre/TradeCases/PaymentRefunds三个子tab | 复用上面店铺、售后、原充值退款数据 | 复用入口/同等限制 | 不是另增业务闭环，不能重复计成功数量；原充值与商品钱包退款不同流程 |
| 业务与字段验收 | GET /portal/admin/business-coverage；搜索/领域过滤/SQL候选展开 | classpath business-coverage.json静态生成计划值113表/1186字段 | 元数据查看已实现/业务全部pending | frontendIntent/adminIntent及文件名只是意图/静态候选，不证明真实数据库有表或字段已被完整业务使用 |

## 具体缺陷与执行优先级

### ADM-001 · P0 · 错误成功

LegacyAdminRpcService.adjustBalance忽略money.addBalance=null后仍通知/返回OK:...0。

账户不存在时没有真实入账，不可显示调账成功；AtomicMoney本身允许负值，不能擅改金额规则。

证据：`server/src/main/java/com/kuaixietech/service/operations/impl/LegacyAdminRpcService.java:41`。

下一步：抛异常回滚并有缺账户测试；由root本轮处理。

### ADM-002 · P0 · 潜伏危险内核

WithdrawMigrationService.markWithdrawalPaid不调用渠道不验凭证却通知微信零钱到账。

rg未发现controller/SDK引用，当前端到端提现未实现，不是已开放RPC。

证据：`server/src/main/java/com/kuaixietech/service/finance/impl/WithdrawMigrationServiceImpl.java:38`。

下一步：先封锁无凭证假成功；再设计真实提款渠道/账单/状态机。

### ADM-003 · P0 · 错误通知/半功能

adminIssueInvoice可空fileUrl并宣称已开票/将送邮箱。

无税务/开票API或邮件投递调用；后台固定传空fileUrl。

证据：`server/src/main/java/com/kuaixietech/service/finance/impl/InvoiceRpcService.java:36`。

下一步：要求可核验原始发票凭证，区分人工登记/实际开票/已发邮件。

### ADM-004 · P0 · 缺后端路由

admin_set_product_status与admin_set_demand_visibility在当前Java中无声明。

SDK.rpc直接POST，因此不是registry缺失而是真404。

证据：`client/apps/admin/src/views/ConsoleView.vue:235`。

下一步：复用已存在审核REST或明确商品审核状态策略；由root本轮处理。

### ADM-005 · P0 · 后台状态竞态

ConsoleView refresh/showEvidence/run无session、generation和mutation busy守卫。

App根按账号重建不足以阻止已等待的回调或重复发非幂等调账；换section也可回填旧行。

证据：`client/apps/admin/src/views/ConsoleView.vue:51`。

下一步：所有读请求绑定section+session generation，资金动作独立busy/确认/幂等；由root处理。

### ADM-006 · P1 · 字段投影错位

可见列和按钮条件与AdminConsoleMapper不匹配。

visibility、origin_city、scope/kind、title_name/title、reason_detail/reason、provider/name、used_by/used_count等。

证据：`server/src/main/resources/mapper/operations/AdminConsoleMapper.xml:18`。

下一步：逐section建立DTO与数据映射测试，不用从任意row动态推列掩盖遗漏。

### ADM-007 · P1 · 固定截断列表

通用列表固定limit80，后端最多200，无offset或游标。

第81条以后大部分数据没有正式后台查看出口；只users支持keyword。

证据：`client/apps/admin/src/views/ConsoleView.vue:81`。

下一步：分页、筛选、稳定排序、详情；验证数据量超过阈值。

### ADM-008 · P1 · 不可实质核验

KYC/行程审核只显示少量资料状态，无证件/凭证完整检视。

carrier RPC改verify但门户REST还改visibility，双轨审核行为不一致。

证据：`server/src/main/java/com/kuaixietech/service/operations/impl/AdminConsoleService.java:77`。

下一步：统一事务、详情证据、安全附件读取与审核前条件。

### ADM-009 · P1 · 停摆业务配置

t_proof_recipes只有后台CRUD，没有被取证业务读取。

配方活跃字段不影响OrderProofMigrationService固定验证。

证据：`server/src/main/resources/mapper/operations/AdminConsoleMapper.xml:39`。

下一步：将模板解析、版本快照和实际取证校验接通或明确标参考。

### ADM-010 · P1 · 平台资金核算缺口

取消违约分支剩余30%只写“待独立台账核验”备注。

不见平台风险池/总账分录；订单/用户余额分支不等于平台资产闭环。

证据：`server/src/main/java/com/kuaixietech/service/order_fulfillment/impl/CancellationRpcService.java:61`。

下一步：确定会计归属并新增可审阅迁移/分录，不擅自向运营账户充值。

### ADM-011 · P1 · 渠道与清算未闭环

finance只有钱包流水；PaymentReconciliation只轮询pending付款且默认关闭。

没有收单账单导入、银行流水、手续费汇差、日终平衡、人工差错记录。

证据：`server/src/main/java/com/kuaixietech/payments/PaymentReconciliation.java:16`。

下一步：分拆订单查询、收单对账、银行清算及差错流程。

### ADM-012 · P1 · 待核算终态无处理出口

全货款售后退款停awaiting_accounting，服务费/运费/保证金标准未落地。

这是保护性暂停，不是售后/财务完成；历史与复杂来源订单被阻断。

证据：`server/src/main/java/com/kuaixietech/aftersalesfunds/AftersalesEscrowFunds.java:32`。

下一步：审定费用分项规则及退货/保证金凭证，再提供财务核算台与账务动作。

### ADM-013 · P1 · 社区数据双轨

旧t_posts移除与新t_social_posts审核分离。

新feed已公开动态缺同等下架治理接口/按钮。

证据：`server/src/main/java/com/kuaixietech/service/portal/impl/PortalServiceImpl.java:74`。

下一步：统一或明确模型边界并补新feed撤销/下架/评论/媒体治理。

### ADM-014 · P1 · 外部通知未投递

NotifyOutbox外部配置ready时仅kept++。

站内有实际转通知；外部没有consumer就一直pending，不会假markSent。

证据：`server/src/main/java/com/kuaixietech/service/communication/impl/NotifyOutboxMigrationService.java:17`。

下一步：实现真实通道投递与回执/死信/人工重试，不凭配置标成功。

### ADM-015 · P1 · 保险未接实际机构

InsuranceCatalog注册List<Adapter>，production无保险Adapter实现。

目录12家是参考；填写产品与密钥仍不能报价/出单；不提供平台担保。

证据：`server/src/main/java/com/kuaixietech/insurance/InsuranceCatalog.java:10`。

下一步：按合作机构公开/合同API实现真实报价/机构直付/出单/退保/理赔与回执。

### ADM-016 · P2 · API工作台覆盖有限

registry71条，declaredBackendRoutes231条。

SDK字面量零缺路由不等于全部按钮/动态RPC已完整绑定。

证据：`client/apps/admin/src/views/OperationsView.vue:2`。

下一步：统一全路由契约元数据，区分技术操作台和正式业务入口。

### ADM-017 · P2 · 后台导航无URL状态

go仅改section内存，router文件只有类型。

F5/链接不能保留当前section及筛选/案件上下文。

证据：`client/apps/admin/src/stores/useAdminStore.ts:71`。

下一步：加入白名单URL路由、查询参数、权限及刷新恢复。

### ADM-018 · P1 · 配置存在不等于运行

通用渠道、耦合器、专用支付/保险多套配置同时存在。

根密钥/业务库/正式商户/外呼模式缺任一条件均不能“填key就开通”。

证据：`client/apps/admin/src/views/IntegrationsView.vue:2`。

下一步：显示依赖和原因、各供应商真实consumer状态，禁止未知通道承接真实订单。

### ADM-019 · P1 · 分销追回条件不足

RewardSettlementService.revokeOrderRewards忽略money.addBalance的null。

账户缺失时可能更新revoked并宣称已追回；需与前台奖励可用余额/负债口径对账。

证据：`server/src/main/java/com/kuaixietech/service/finance/impl/RewardSettlementService.java:85`。

下一步：确保入账/追回分录成功才改状态；历史欠款单独核算，不改既定费率。

## 原始结构与SQL的可执行性

静态列名匹配、INSERT必填列扫描、字段COMMENT完整度均不能证明MySQL严格模式下每条SQL、触发器、锁顺序、外键/字符集及事务回滚正确。所有关键链路还需在隔离验证库或经授权业务库，用真实API请求记录前后余额、存款、库存、订单与通知；不得以返回OK或行数非零替代完整核对。

目前后台不少列表只读取主表部分字段，涉及模板、店铺保单、承保、退货、跨境合规、风险池、地理取证等还需与数据/集成审计交叉确认。业务与字段验收页的frontendIntent/adminIntent是设计意图、sqlCandidates只是文件名候选，不是动态业务使用或权限证明。

## 静态事件与输入绑定全量索引

从后台App可达组件图静态展开了 **25 个SFC、173 处事件定义、100 处v-model输入绑定**。这包括点击、提交、下拉和键盘事件；v-for生成的按钮实例需按具体角色、状态和真实数据展开。并非每个事件都对应网络请求（展开/编辑/切tab可能仅本地状态），也不能据此称每个动态按钮已运行。

耦合器另有 **21 类/47 个provider/JSON静态83个基础字段；按当前工厂补充后约122个输入字段定义（源级推算，未真实展开），SANDBOX与LIVE两份配置**，运行时是否有值、可解密、能选中、consumer已接线和服务已验收均为独立条件。动态字段未在真实业务数据下实例化，不能将静态输入数当实际服务能力数。

### 组件覆盖

- `client/apps/admin/src/App.vue`
- `client/apps/admin/src/components/AdminFooter.vue`
- `client/apps/admin/src/components/AdminPageHeader.vue`
- `client/apps/admin/src/components/AdminSidebar.vue`
- `client/apps/admin/src/components/AdminTopbar.vue`
- `client/apps/admin/src/components/CouplerSettingsPanel.vue`
- `client/apps/admin/src/components/InsuranceSettingsPanel.vue`
- `client/apps/admin/src/components/IntegrationSettingsPanel.vue`
- `client/apps/admin/src/components/PaymentSettingsPanel.vue`
- `client/apps/admin/src/components/PortalModerationPanel.vue`
- `client/apps/admin/src/components/SupportAgentPanel.vue`
- `client/apps/admin/src/views/BusinessCoverageView.vue`
- `client/apps/admin/src/views/BusinessView.vue`
- `client/apps/admin/src/views/ConsoleView.vue`
- `client/apps/admin/src/views/IntegrationsView.vue`
- `client/apps/admin/src/views/LoginView.vue`
- `client/apps/admin/src/views/OperationsView.vue`
- `client/apps/admin/src/views/SettingsView.vue`
- `client/packages/ui/src/ApiWorkbench.vue`
- `client/packages/ui/src/EscrowRefundPanel.vue`
- `client/packages/ui/src/PaymentRefunds.vue`
- `client/packages/ui/src/PreferencesPanel.vue`
- `client/packages/ui/src/ShopCentre.vue`
- `client/packages/ui/src/ShopOrders.vue`
- `client/packages/ui/src/TradeCases.vue`

### 每个静态事件定义

| 序号 | 源码位置 | 事件 | handler/表达式 | 验证状态 |
| --- | --- | --- | --- | --- |
| 1 | `client/apps/admin/src/App.vue:30` | `click` | `go(tab.key)` | 源码可定位；未真实运行 |
| 2 | `client/apps/admin/src/components/AdminSidebar.vue:18` | `click` | `go('overview')` | 源码可定位；未真实运行 |
| 3 | `client/apps/admin/src/components/AdminSidebar.vue:26` | `click` | `go('overview')` | 源码可定位；未真实运行 |
| 4 | `client/apps/admin/src/components/AdminSidebar.vue:28` | `click` | `go(q.tab)` | 源码可定位；未真实运行 |
| 5 | `client/apps/admin/src/components/AdminSidebar.vue:34` | `click` | `go(tb.key)` | 源码可定位；未真实运行 |
| 6 | `client/apps/admin/src/components/AdminSidebar.vue:39` | `click` | `go('all')` | 源码可定位；未真实运行 |
| 7 | `client/apps/admin/src/components/AdminSidebar.vue:40` | `click` | `go('integrations')` | 源码可定位；未真实运行 |
| 8 | `client/apps/admin/src/components/AdminSidebar.vue:41` | `click` | `go('business')` | 源码可定位；未真实运行 |
| 9 | `client/apps/admin/src/components/AdminSidebar.vue:42` | `click` | `go('businessCoverage')` | 源码可定位；未真实运行 |
| 10 | `client/apps/admin/src/components/AdminSidebar.vue:43` | `click` | `go('settings')` | 源码可定位；未真实运行 |
| 11 | `client/apps/admin/src/components/AdminSidebar.vue:51` | `click` | `removeToken` | 源码可定位；未真实运行 |
| 12 | `client/apps/admin/src/components/AdminTopbar.vue:17` | `click` | `sidebarOpen=!sidebarOpen` | 源码可定位；未真实运行 |
| 13 | `client/apps/admin/src/components/AdminTopbar.vue:24` | `change` | `changeLocale` | 源码可定位；未真实运行 |
| 14 | `client/apps/admin/src/components/AdminTopbar.vue:30` | `click` | `go('settings')` | 源码可定位；未真实运行 |
| 15 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `click` | `p.load()` | 源码可定位；未真实运行 |
| 16 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `click` | `p.changeRuntime()` | 源码可定位；未真实运行 |
| 17 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `click` | `p.chooseCategory(category.code)` | 源码可定位；未真实运行 |
| 18 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `click` | `p.chooseMode(mode)` | 源码可定位；未真实运行 |
| 19 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `click` | `p.chooseProvider(provider.code)` | 源码可定位；未真实运行 |
| 20 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `submit.prevent` | `p.save()` | 源码可定位；未真实运行 |
| 21 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `click` | `p.validate()` | 源码可定位；未真实运行 |
| 22 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `click` | `load` | 源码可定位；未真实运行 |
| 23 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `change` | `company` | 源码可定位；未真实运行 |
| 24 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `submit.prevent` | `save` | 源码可定位；未真实运行 |
| 25 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `click` | `reset` | 源码可定位；未真实运行 |
| 26 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `click` | `edit(product)` | 源码可定位；未真实运行 |
| 27 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:15` | `click` | `load` | 源码可定位；未真实运行 |
| 28 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:16` | `click` | `bundle` | 源码可定位；未真实运行 |
| 29 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:16` | `click` | `edit(product)` | 源码可定位；未真实运行 |
| 30 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:17` | `submit.prevent` | `save` | 源码可定位；未真实运行 |
| 31 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:17` | `click` | `selected=null;values={}` | 源码可定位；未真实运行 |
| 32 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:26` | `click` | `load` | 源码可定位；未真实运行 |
| 33 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:30` | `click` | `edit(row)` | 源码可定位；未真实运行 |
| 34 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:31` | `submit.prevent` | `saveMerchant` | 源码可定位；未真实运行 |
| 35 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:33` | `change` | `adapterChanged` | 源码可定位；未真实运行 |
| 36 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:43` | `change` | `toggleCurrency(item.code,$event)` | 源码可定位；未真实运行 |
| 37 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:46` | `click` | `source={...row}` | 源码可定位；未真实运行 |
| 38 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:47` | `submit.prevent` | `saveFx` | 源码可定位；未真实运行 |
| 39 | `client/apps/admin/src/components/PortalModerationPanel.vue:25` | `click` | `refresh` | 源码可定位；未真实运行 |
| 40 | `client/apps/admin/src/components/PortalModerationPanel.vue:27` | `click` | `decide(kind,String(item.id),true)` | 源码可定位；未真实运行 |
| 41 | `client/apps/admin/src/components/PortalModerationPanel.vue:27` | `click` | `decide(kind,String(item.id),false)` | 源码可定位；未真实运行 |
| 42 | `client/apps/admin/src/components/SupportAgentPanel.vue:27` | `click` | `reload` | 源码可定位；未真实运行 |
| 43 | `client/apps/admin/src/components/SupportAgentPanel.vue:30` | `click` | `claim(item.id)` | 源码可定位；未真实运行 |
| 44 | `client/apps/admin/src/components/SupportAgentPanel.vue:31` | `keydown.enter.prevent` | `send` | 源码可定位；未真实运行 |
| 45 | `client/apps/admin/src/components/SupportAgentPanel.vue:31` | `click` | `send` | 源码可定位；未真实运行 |
| 46 | `client/apps/admin/src/views/BusinessCoverageView.vue:12` | `click` | `load` | 源码可定位；未真实运行 |
| 47 | `client/apps/admin/src/views/BusinessView.vue:13` | `click` | `tab=item.key` | 源码可定位；未真实运行 |
| 48 | `client/apps/admin/src/views/ConsoleView.vue:139` | `click` | `loadStats` | 源码可定位；未真实运行 |
| 49 | `client/apps/admin/src/views/ConsoleView.vue:154` | `click` | `refresh` | 源码可定位；未真实运行 |
| 50 | `client/apps/admin/src/views/ConsoleView.vue:155` | `keydown.enter` | `refresh` | 源码可定位；未真实运行 |
| 51 | `client/apps/admin/src/views/ConsoleView.vue:163` | `click` | `run(()=>api.rpc('admin_create_category',{name:catName,parentId:null,level:1}))` | 源码可定位；未真实运行 |
| 52 | `client/apps/admin/src/views/ConsoleView.vue:168` | `click` | `run(()=>api.rpc('admin_create_invite',{code:inviteCode,note:inviteNote}))` | 源码可定位；未真实运行 |
| 53 | `client/apps/admin/src/views/ConsoleView.vue:174` | `click` | `bcScope=s as typeof bcScope` | 源码可定位；未真实运行 |
| 54 | `client/apps/admin/src/views/ConsoleView.vue:176` | `click` | `run(()=>api.rpc('admin_broadcast',{title:bcTitle,body:bcBody,scope:bcScope,userIds:[]}))` | 源码可定位；未真实运行 |
| 55 | `client/apps/admin/src/views/ConsoleView.vue:184` | `click` | `run(()=>api.rpc('admin_locale_upsert_rpc',{code:localeCode,nativeName:localeNative,labelZh:localeZh,sort:Number(localeSort)\|\|100,fallback:localeFallback}))` | 源码可定位；未真实运行 |
| 56 | `client/apps/admin/src/views/ConsoleView.vue:192` | `click` | `run(()=>api.rpc('admin_upsert_recipe',{id:'',tripType:recipeType,transportMode:recipeMode,countryCode:recipeCountry,items:recipeItems,note:recipeNote}))` | 源码可定位；未真实运行 |
| 57 | `client/apps/admin/src/views/ConsoleView.vue:195` | `click` | `run(()=>api.rpc('admin_release_privacy',{}))` | 源码可定位；未真实运行 |
| 58 | `client/apps/admin/src/views/ConsoleView.vue:208` | `click` | `toggle(row)` | 源码可定位；未真实运行 |
| 59 | `client/apps/admin/src/views/ConsoleView.vue:218` | `click` | `run(()=>api.rpc('admin_set_carrier_verify',{userId:row.id,approve:true,note:''}))` | 源码可定位；未真实运行 |
| 60 | `client/apps/admin/src/views/ConsoleView.vue:219` | `click` | `run(()=>api.rpc('admin_set_carrier_verify',{userId:row.id,approve:false,note:requireNote()}))` | 源码可定位；未真实运行 |
| 61 | `client/apps/admin/src/views/ConsoleView.vue:222` | `click` | `run(()=>api.rpc('admin_review_buyer_kyc',{userId:row.id,approve:true,reason:''}))` | 源码可定位；未真实运行 |
| 62 | `client/apps/admin/src/views/ConsoleView.vue:223` | `click` | `run(()=>api.rpc('admin_review_buyer_kyc',{userId:row.id,approve:false,reason:requireNote()}))` | 源码可定位；未真实运行 |
| 63 | `client/apps/admin/src/views/ConsoleView.vue:226` | `click` | `run(()=>api.rpc('admin_review_enterprise',{userId:row.id,approve:true,note:requireNote()}))` | 源码可定位；未真实运行 |
| 64 | `client/apps/admin/src/views/ConsoleView.vue:227` | `click` | `run(()=>api.rpc('admin_review_enterprise',{userId:row.id,approve:false,note:requireNote()}))` | 源码可定位；未真实运行 |
| 65 | `client/apps/admin/src/views/ConsoleView.vue:230` | `click` | `run(()=>api.rpc('admin_set_trip_verify',{tripId:row.id,approve:true,note:''}))` | 源码可定位；未真实运行 |
| 66 | `client/apps/admin/src/views/ConsoleView.vue:231` | `click` | `run(()=>api.rpc('admin_set_trip_verify',{tripId:row.id,approve:false,note:t('admin.console.reject')}))` | 源码可定位；未真实运行 |
| 67 | `client/apps/admin/src/views/ConsoleView.vue:233` | `click` | `run(()=>showEvidence(row.id))` | 源码可定位；未真实运行 |
| 68 | `client/apps/admin/src/views/ConsoleView.vue:235` | `click` | `run(()=>api.rpc('admin_set_product_status',{id:row.id,approve:true,note:''}))` | 源码可定位；未真实运行 |
| 69 | `client/apps/admin/src/views/ConsoleView.vue:236` | `click` | `run(()=>api.rpc('admin_set_product_status',{id:row.id,approve:false,note:requireNote()}))` | 源码可定位；未真实运行 |
| 70 | `client/apps/admin/src/views/ConsoleView.vue:238` | `click` | `run(()=>api.rpc('admin_force_off_shelf',{id:row.id}))` | 源码可定位；未真实运行 |
| 71 | `client/apps/admin/src/views/ConsoleView.vue:239` | `click` | `run(()=>api.rpc('admin_deactivate_category',{id:row.id}))` | 源码可定位；未真实运行 |
| 72 | `client/apps/admin/src/views/ConsoleView.vue:240` | `click` | `run(()=>api.rpc('admin_toggle_recipe',{id:row.id,active:!row.active}))` | 源码可定位；未真实运行 |
| 73 | `client/apps/admin/src/views/ConsoleView.vue:242` | `click` | `run(()=>api.rpc('admin_resolve_cancel',{orderId:row.order_id,approve:true,reason:requireNote()}))` | 源码可定位；未真实运行 |
| 74 | `client/apps/admin/src/views/ConsoleView.vue:243` | `click` | `run(()=>api.rpc('admin_resolve_cancel',{orderId:row.order_id,approve:false,reason:requireNote()}))` | 源码可定位；未真实运行 |
| 75 | `client/apps/admin/src/views/ConsoleView.vue:247` | `click` | `run(()=>api.rpc('admin_issue_invoice',{invoiceId:row.id,approve:true,invoiceNo:invoiceNo[String(row.id)],fileUrl:'',reason:''}))` | 源码可定位；未真实运行 |
| 76 | `client/apps/admin/src/views/ConsoleView.vue:248` | `click` | `run(()=>api.rpc('admin_issue_invoice',{invoiceId:row.id,approve:false,invoiceNo:'',fileUrl:'',reason:t('admin.console.reject')}))` | 源码可定位；未真实运行 |
| 77 | `client/apps/admin/src/views/ConsoleView.vue:250` | `click` | `run(()=>api.rpc('admin_review_violation',{id:row.id}))` | 源码可定位；未真实运行 |
| 78 | `client/apps/admin/src/views/ConsoleView.vue:252` | `click` | `run(()=>api.rpc('admin_force_form_pool',{tripId:row.id}))` | 源码可定位；未真实运行 |
| 79 | `client/apps/admin/src/views/ConsoleView.vue:253` | `click` | `run(()=>api.rpc('admin_dissolve_pool',{tripId:row.id,reason:requireNote()}))` | 源码可定位；未真实运行 |
| 80 | `client/apps/admin/src/views/ConsoleView.vue:256` | `click` | `run(()=>api.rpc('admin_set_demand_visibility',{demandId:row.id,approve:true,note:''}))` | 源码可定位；未真实运行 |
| 81 | `client/apps/admin/src/views/ConsoleView.vue:257` | `click` | `run(()=>api.rpc('admin_set_demand_visibility',{demandId:row.id,approve:false,note:requireNote()}))` | 源码可定位；未真实运行 |
| 82 | `client/apps/admin/src/views/ConsoleView.vue:259` | `click` | `run(()=>api.rpc('admin_close_demand',{demandId:row.id,reason:requireNote()}))` | 源码可定位；未真实运行 |
| 83 | `client/apps/admin/src/views/ConsoleView.vue:260` | `click` | `run(()=>api.rpc('admin_remove_post',{postId:row.id,reason:requireNote()}))` | 源码可定位；未真实运行 |
| 84 | `client/apps/admin/src/views/ConsoleView.vue:261` | `click` | `run(()=>api.rpc('admin_resolve_error',{id:row.id}))` | 源码可定位；未真实运行 |
| 85 | `client/apps/admin/src/views/ConsoleView.vue:262` | `click` | `run(()=>api.rpc('admin_toggle_channel',{id:row.id,enabled:!row.enabled}))` | 源码可定位；未真实运行 |
| 86 | `client/apps/admin/src/views/ConsoleView.vue:264` | `click` | `run(()=>api.rpc('admin_locale_set_active_rpc',{code:row.code,active:!row.active,note:t('admin.console.localeToggle')}))` | 源码可定位；未真实运行 |
| 87 | `client/apps/admin/src/views/ConsoleView.vue:265` | `click` | `run(()=>api.rpc('admin_locale_remove_rpc',{code:row.code,note:t('admin.console.localeRemove')}))` | 源码可定位；未真实运行 |
| 88 | `client/apps/admin/src/views/ConsoleView.vue:268` | `click` | `run(()=>api.rpc('admin_ban_user',{userId:row.id,ban:!row.banned_until,reason:row.banned_until?t('admin.console.unban'):requireNote()}))` | 源码可定位；未真实运行 |
| 89 | `client/apps/admin/src/views/ConsoleView.vue:269` | `click` | `run(()=>api.rpc('admin_adjust_balance',{userId:row.id,delta:requireAmount('admin.console.adjustBalance'),note:requireNote()}))` | 源码可定位；未真实运行 |
| 90 | `client/apps/admin/src/views/ConsoleView.vue:270` | `click` | `run(()=>api.rpc('admin_fix_score',{userId:row.id,delta:requireAmount('admin.console.fixScore'),note:requireNote()}))` | 源码可定位；未真实运行 |
| 91 | `client/apps/admin/src/views/ConsoleView.vue:271` | `click` | `run(()=>api.rpc('admin_grant_points',{userId:row.id,points:requireAmount('admin.console.grantPoints'),note:requireNote()}))` | 源码可定位；未真实运行 |
| 92 | `client/apps/admin/src/views/LoginView.vue:19` | `click` | `loginChannel='phone'` | 源码可定位；未真实运行 |
| 93 | `client/apps/admin/src/views/LoginView.vue:20` | `click` | `loginChannel='email'` | 源码可定位；未真实运行 |
| 94 | `client/apps/admin/src/views/LoginView.vue:25` | `keydown.enter` | `login` | 源码可定位；未真实运行 |
| 95 | `client/apps/admin/src/views/LoginView.vue:27` | `click` | `login` | 源码可定位；未真实运行 |
| 96 | `client/apps/admin/src/views/SettingsView.vue:9` | `click` | `go('integrations')` | 源码可定位；未真实运行 |
| 97 | `client/apps/admin/src/views/SettingsView.vue:18` | `click` | `showToken=!showToken` | 源码可定位；未真实运行 |
| 98 | `client/apps/admin/src/views/SettingsView.vue:21` | `click` | `applyToken` | 源码可定位；未真实运行 |
| 99 | `client/apps/admin/src/views/SettingsView.vue:22` | `click` | `removeToken` | 源码可定位；未真实运行 |
| 100 | `client/packages/ui/src/ApiWorkbench.vue:38` | `change` | `setBearer(token)` | 源码可定位；未真实运行 |
| 101 | `client/packages/ui/src/ApiWorkbench.vue:38` | `click` | `showToken=!showToken` | 源码可定位；未真实运行 |
| 102 | `client/packages/ui/src/ApiWorkbench.vue:38` | `click` | `setBearer(token)` | 源码可定位；未真实运行 |
| 103 | `client/packages/ui/src/ApiWorkbench.vue:40` | `click` | `selectedGroup=g` | 源码可定位；未真实运行 |
| 104 | `client/packages/ui/src/ApiWorkbench.vue:40` | `click` | `selectOp(op)` | 源码可定位；未真实运行 |
| 105 | `client/packages/ui/src/ApiWorkbench.vue:42` | `submit.prevent` | `execute` | 源码可定位；未真实运行 |
| 106 | `client/packages/ui/src/ApiWorkbench.vue:42` | `input` | `form[p]=($event.target as HTMLInputElement).value` | 源码可定位；未真实运行 |
| 107 | `client/packages/ui/src/ApiWorkbench.vue:43` | `input` | `form[p]=($event.target as HTMLInputElement).value` | 源码可定位；未真实运行 |
| 108 | `client/packages/ui/src/ApiWorkbench.vue:45` | `change` | `form[f.name]=($event.target as HTMLSelectElement).value==='true'` | 源码可定位；未真实运行 |
| 109 | `client/packages/ui/src/ApiWorkbench.vue:45` | `input` | `form[f.name]=($event.target as HTMLTextAreaElement).value` | 源码可定位；未真实运行 |
| 110 | `client/packages/ui/src/ApiWorkbench.vue:45` | `input` | `form[f.name]=($event.target as HTMLInputElement).value` | 源码可定位；未真实运行 |
| 111 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.loadBudget()` | 源码可定位；未真实运行 |
| 112 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.execute()` | 源码可定位；未真实运行 |
| 113 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.loadBudget()` | 源码可定位；未真实运行 |
| 114 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.retry()` | 源码可定位；未真实运行 |
| 115 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.loadEvents(p.receipt.value.id)` | 源码可定位；未真实运行 |
| 116 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.loadRecords()` | 源码可定位；未真实运行 |
| 117 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.loadEvents(row.id)` | 源码可定位；未真实运行 |
| 118 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.loadRecords(true)` | 源码可定位；未真实运行 |
| 119 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `click` | `p.loadEvents(p.eventsId.value,true)` | 源码可定位；未真实运行 |
| 120 | `client/packages/ui/src/PaymentRefunds.vue:11` | `click` | `p.loadList();p.loadEligible()` | 源码可定位；未真实运行 |
| 121 | `client/packages/ui/src/PaymentRefunds.vue:12` | `submit.prevent` | `p.apply()` | 源码可定位；未真实运行 |
| 122 | `client/packages/ui/src/PaymentRefunds.vue:13` | `click` | `p.loadDetail(row.id)` | 源码可定位；未真实运行 |
| 123 | `client/packages/ui/src/PaymentRefunds.vue:13` | `click` | `p.loadList(true)` | 源码可定位；未真实运行 |
| 124 | `client/packages/ui/src/PaymentRefunds.vue:14` | `click` | `p.loadDetail(p.entry.value.id)` | 源码可定位；未真实运行 |
| 125 | `client/packages/ui/src/PaymentRefunds.vue:14` | `click` | `p.refresh()` | 源码可定位；未真实运行 |
| 126 | `client/packages/ui/src/PaymentRefunds.vue:14` | `click` | `p.cancel()` | 源码可定位；未真实运行 |
| 127 | `client/packages/ui/src/PaymentRefunds.vue:14` | `click` | `p.review(true)` | 源码可定位；未真实运行 |
| 128 | `client/packages/ui/src/PaymentRefunds.vue:14` | `click` | `p.review(false)` | 源码可定位；未真实运行 |
| 129 | `client/packages/ui/src/PaymentRefunds.vue:14` | `click` | `p.dispatch()` | 源码可定位；未真实运行 |
| 130 | `client/packages/ui/src/PaymentRefunds.vue:14` | `click` | `p.loadEvents(p.entry.value.id,true)` | 源码可定位；未真实运行 |
| 131 | `client/packages/ui/src/PreferencesPanel.vue:17` | `change` | `themeChanged` | 源码可定位；未真实运行 |
| 132 | `client/packages/ui/src/PreferencesPanel.vue:24` | `change` | `fontChanged` | 源码可定位；未真实运行 |
| 133 | `client/packages/ui/src/PreferencesPanel.vue:31` | `input` | `accentChanged` | 源码可定位；未真实运行 |
| 134 | `client/packages/ui/src/PreferencesPanel.vue:34` | `input` | `sizeChanged` | 源码可定位；未真实运行 |
| 135 | `client/packages/ui/src/PreferencesPanel.vue:37` | `change` | `toggleCare` | 源码可定位；未真实运行 |
| 136 | `client/packages/ui/src/PreferencesPanel.vue:38` | `change` | `toggleMotion` | 源码可定位；未真实运行 |
| 137 | `client/packages/ui/src/PreferencesPanel.vue:42` | `click` | `resetPreferences()` | 源码可定位；未真实运行 |
| 138 | `client/packages/ui/src/ShopCentre.vue:12` | `click` | `p.load()` | 源码可定位；未真实运行 |
| 139 | `client/packages/ui/src/ShopCentre.vue:13` | `click` | `download(row.license_upload_id)` | 源码可定位；未真实运行 |
| 140 | `client/packages/ui/src/ShopCentre.vue:13` | `click` | `p.review(row,true)` | 源码可定位；未真实运行 |
| 141 | `client/packages/ui/src/ShopCentre.vue:13` | `click` | `p.review(row,false)` | 源码可定位；未真实运行 |
| 142 | `client/packages/ui/src/ShopCentre.vue:13` | `click` | `selectedOrders={id:row.store_id,name:row.business_name}` | 源码可定位；未真实运行 |
| 143 | `client/packages/ui/src/ShopCentre.vue:13` | `click` | `p.status(row.store_id,row.store_status,'suspended')` | 源码可定位；未真实运行 |
| 144 | `client/packages/ui/src/ShopCentre.vue:13` | `click` | `p.status(row.store_id,row.store_status,'paused')` | 源码可定位；未真实运行 |
| 145 | `client/packages/ui/src/ShopCentre.vue:13` | `click` | `p.load(true)` | 源码可定位；未真实运行 |
| 146 | `client/packages/ui/src/ShopCentre.vue:14` | `submit.prevent` | `p.save()` | 源码可定位；未真实运行 |
| 147 | `client/packages/ui/src/ShopCentre.vue:14` | `click` | `p.status(p.state.value.store.id,p.state.value.store.status,p.state.value.store.status==='active'?'paused':'active')` | 源码可定位；未真实运行 |
| 148 | `client/packages/ui/src/ShopCentre.vue:14` | `click` | `p.preview(p.state.value.store.id)` | 源码可定位；未真实运行 |
| 149 | `client/packages/ui/src/ShopCentre.vue:14` | `click` | `p.link(product)` | 源码可定位；未真实运行 |
| 150 | `client/packages/ui/src/ShopCentre.vue:14` | `click` | `p.loadProducts(true)` | 源码可定位；未真实运行 |
| 151 | `client/packages/ui/src/ShopCentre.vue:14` | `click` | `p.linked()` | 源码可定位；未真实运行 |
| 152 | `client/packages/ui/src/ShopCentre.vue:14` | `click` | `p.link(row.id,true)` | 源码可定位；未真实运行 |
| 153 | `client/packages/ui/src/ShopCentre.vue:15` | `submit.prevent` | `p.apply()` | 源码可定位；未真实运行 |
| 154 | `client/packages/ui/src/ShopCentre.vue:15` | `change` | `upload` | 源码可定位；未真实运行 |
| 155 | `client/packages/ui/src/ShopCentre.vue:16` | `click` | `download(row.license_upload_id)` | 源码可定位；未真实运行 |
| 156 | `client/packages/ui/src/ShopCentre.vue:17` | `click` | `p.preview(p.publicShop.value.id,true)` | 源码可定位；未真实运行 |
| 157 | `client/packages/ui/src/ShopOrders.vue:8` | `click` | `p.load()` | 源码可定位；未真实运行 |
| 158 | `client/packages/ui/src/ShopOrders.vue:8` | `click` | `p.load(true)` | 源码可定位；未真实运行 |
| 159 | `client/packages/ui/src/TradeCases.vue:16` | `click` | `p.loadList()` | 源码可定位；未真实运行 |
| 160 | `client/packages/ui/src/TradeCases.vue:17` | `click` | `p.loadDetail(row.id)` | 源码可定位；未真实运行 |
| 161 | `client/packages/ui/src/TradeCases.vue:17` | `click` | `p.loadList(true)` | 源码可定位；未真实运行 |
| 162 | `client/packages/ui/src/TradeCases.vue:18` | `click` | `p.loadOrders(true)` | 源码可定位；未真实运行 |
| 163 | `client/packages/ui/src/TradeCases.vue:18` | `click` | `p.create()` | 源码可定位；未真实运行 |
| 164 | `client/packages/ui/src/TradeCases.vue:19` | `click` | `p.loadDetail(p.entry.value.id)` | 源码可定位；未真实运行 |
| 165 | `client/packages/ui/src/TradeCases.vue:20` | `click` | `p.act('decide')` | 源码可定位；未真实运行 |
| 166 | `client/packages/ui/src/TradeCases.vue:21` | `click` | `p.act('respond')` | 源码可定位；未真实运行 |
| 167 | `client/packages/ui/src/TradeCases.vue:21` | `click` | `p.act('escalate')` | 源码可定位；未真实运行 |
| 168 | `client/packages/ui/src/TradeCases.vue:21` | `click` | `p.act('appeal')` | 源码可定位；未真实运行 |
| 169 | `client/packages/ui/src/TradeCases.vue:21` | `click` | `p.act('finish')` | 源码可定位；未真实运行 |
| 170 | `client/packages/ui/src/TradeCases.vue:22` | `locked` | `escrowLocked=$event` | 源码可定位；未真实运行 |
| 171 | `client/packages/ui/src/TradeCases.vue:22` | `recorded` | `escrowRecorded` | 源码可定位；未真实运行 |
| 172 | `client/packages/ui/src/TradeCases.vue:22` | `click` | `p.history()` | 源码可定位；未真实运行 |
| 173 | `client/packages/ui/src/TradeCases.vue:22` | `click` | `p.retry()` | 源码可定位；未真实运行 |

### 每个输入绑定

| 序号 | 源码位置 | 输入模型 | 数据状态 |
| --- | --- | --- | --- |
| 1 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.runtimeTarget.value` | 输入可绑定；不证明持久化/业务消费 |
| 2 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.draft.values[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 3 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.draft.values[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 4 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.draft.values[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 5 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.draft.values[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 6 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.draft.clearFields[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 7 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.draft.enabled` | 输入可绑定；不证明持久化/业务消费 |
| 8 | `client/apps/admin/src/components/CouplerSettingsPanel.vue:19` | `p.draft.selected` | 输入可绑定；不证明持久化/业务消费 |
| 9 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.insurerCode` | 输入可绑定；不证明持久化/业务消费 |
| 10 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.insurerName` | 输入可绑定；不证明持久化/业务消费 |
| 11 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.name` | 输入可绑定；不证明持久化/业务消费 |
| 12 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.kind` | 输入可绑定；不证明持久化/业务消费 |
| 13 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.adapter` | 输入可绑定；不证明持久化/业务消费 |
| 14 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.providerProductCode` | 输入可绑定；不证明持久化/业务消费 |
| 15 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.credentialPrefix` | 输入可绑定；不证明持久化/业务消费 |
| 16 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `serviceHosts` | 输入可绑定；不证明持久化/业务消费 |
| 17 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `checkoutHosts` | 输入可绑定；不证明持久化/业务消费 |
| 18 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.termsUrl` | 输入可绑定；不证明持久化/业务消费 |
| 19 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.exclusionsUrl` | 输入可绑定；不证明持久化/业务消费 |
| 20 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.claimsUrl` | 输入可绑定；不证明持久化/业务消费 |
| 21 | `client/apps/admin/src/components/InsuranceSettingsPanel.vue:15` | `draft.enabled` | 输入可绑定；不证明持久化/业务消费 |
| 22 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:16` | `prefix` | 输入可绑定；不证明持久化/业务消费 |
| 23 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:17` | `values[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 24 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:17` | `values[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 25 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:17` | `clear[field.key]` | 输入可绑定；不证明持久化/业务消费 |
| 26 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:17` | `remark` | 输入可绑定；不证明持久化/业务消费 |
| 27 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:17` | `enabled` | 输入可绑定；不证明持久化/业务消费 |
| 28 | `client/apps/admin/src/components/IntegrationSettingsPanel.vue:19` | `search` | 输入可绑定；不证明持久化/业务消费 |
| 29 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:32` | `merchant.name` | 输入可绑定；不证明持久化/业务消费 |
| 30 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:33` | `merchant.adapter` | 输入可绑定；不证明持久化/业务消费 |
| 31 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:34` | `merchant.method` | 输入可绑定；不证明持久化/业务消费 |
| 32 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:35` | `merchant.platform` | 输入可绑定；不证明持久化/业务消费 |
| 33 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:36` | `merchant.credentialPrefix` | 输入可绑定；不证明持久化/业务消费 |
| 34 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:37` | `merchant.spreadBps` | 输入可绑定；不证明持久化/业务消费 |
| 35 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:38` | `merchant.quoteTtlSeconds` | 输入可绑定；不证明持久化/业务消费 |
| 36 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:39` | `merchant.settings.notifyUrl` | 输入可绑定；不证明持久化/业务消费 |
| 37 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:40` | `merchant.settings.returnUrl` | 输入可绑定；不证明持久化/业务消费 |
| 38 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:41` | `merchant.settings.gatewayUrl` | 输入可绑定；不证明持久化/业务消费 |
| 39 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:41` | `merchant.settings.checkoutHosts` | 输入可绑定；不证明持久化/业务消费 |
| 40 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:42` | `currencySearch` | 输入可绑定；不证明持久化/业务消费 |
| 41 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:44` | `merchant.enabled` | 输入可绑定；不证明持久化/业务消费 |
| 42 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:48` | `source.name` | 输入可绑定；不证明持久化/业务消费 |
| 43 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:49` | `source.kind` | 输入可绑定；不证明持久化/业务消费 |
| 44 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:50` | `source.endpoint` | 输入可绑定；不证明持久化/业务消费 |
| 45 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:51` | `source.credentialPrefix` | 输入可绑定；不证明持久化/业务消费 |
| 46 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:52` | `source.maxAgeSeconds` | 输入可绑定；不证明持久化/业务消费 |
| 47 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:53` | `source.enabled` | 输入可绑定；不证明持久化/业务消费 |
| 48 | `client/apps/admin/src/components/PaymentSettingsPanel.vue:53` | `source.transactionEnabled` | 输入可绑定；不证明持久化/业务消费 |
| 49 | `client/apps/admin/src/components/SupportAgentPanel.vue:31` | `draft` | 输入可绑定；不证明持久化/业务消费 |
| 50 | `client/apps/admin/src/views/BusinessCoverageView.vue:12` | `search` | 输入可绑定；不证明持久化/业务消费 |
| 51 | `client/apps/admin/src/views/BusinessCoverageView.vue:12` | `domain` | 输入可绑定；不证明持久化/业务消费 |
| 52 | `client/apps/admin/src/views/ConsoleView.vue:155` | `keyword` | 输入可绑定；不证明持久化/业务消费 |
| 53 | `client/apps/admin/src/views/ConsoleView.vue:162` | `catName` | 输入可绑定；不证明持久化/业务消费 |
| 54 | `client/apps/admin/src/views/ConsoleView.vue:166` | `inviteCode` | 输入可绑定；不证明持久化/业务消费 |
| 55 | `client/apps/admin/src/views/ConsoleView.vue:167` | `inviteNote` | 输入可绑定；不证明持久化/业务消费 |
| 56 | `client/apps/admin/src/views/ConsoleView.vue:171` | `bcTitle` | 输入可绑定；不证明持久化/业务消费 |
| 57 | `client/apps/admin/src/views/ConsoleView.vue:172` | `bcBody` | 输入可绑定；不证明持久化/业务消费 |
| 58 | `client/apps/admin/src/views/ConsoleView.vue:179` | `localeCode` | 输入可绑定；不证明持久化/业务消费 |
| 59 | `client/apps/admin/src/views/ConsoleView.vue:180` | `localeNative` | 输入可绑定；不证明持久化/业务消费 |
| 60 | `client/apps/admin/src/views/ConsoleView.vue:181` | `localeZh` | 输入可绑定；不证明持久化/业务消费 |
| 61 | `client/apps/admin/src/views/ConsoleView.vue:182` | `localeSort` | 输入可绑定；不证明持久化/业务消费 |
| 62 | `client/apps/admin/src/views/ConsoleView.vue:183` | `localeFallback` | 输入可绑定；不证明持久化/业务消费 |
| 63 | `client/apps/admin/src/views/ConsoleView.vue:187` | `recipeType` | 输入可绑定；不证明持久化/业务消费 |
| 64 | `client/apps/admin/src/views/ConsoleView.vue:188` | `recipeMode` | 输入可绑定；不证明持久化/业务消费 |
| 65 | `client/apps/admin/src/views/ConsoleView.vue:189` | `recipeCountry` | 输入可绑定；不证明持久化/业务消费 |
| 66 | `client/apps/admin/src/views/ConsoleView.vue:190` | `recipeItems` | 输入可绑定；不证明持久化/业务消费 |
| 67 | `client/apps/admin/src/views/ConsoleView.vue:191` | `recipeNote` | 输入可绑定；不证明持久化/业务消费 |
| 68 | `client/apps/admin/src/views/ConsoleView.vue:246` | `invoiceNo[String(row.id)]` | 输入可绑定；不证明持久化/业务消费 |
| 69 | `client/apps/admin/src/views/LoginView.vue:23` | `loginContact` | 输入可绑定；不证明持久化/业务消费 |
| 70 | `client/apps/admin/src/views/LoginView.vue:25` | `loginPassword` | 输入可绑定；不证明持久化/业务消费 |
| 71 | `client/apps/admin/src/views/SettingsView.vue:17` | `token` | 输入可绑定；不证明持久化/业务消费 |
| 72 | `client/packages/ui/src/ApiWorkbench.vue:38` | `token` | 输入可绑定；不证明持久化/业务消费 |
| 73 | `client/packages/ui/src/ApiWorkbench.vue:40` | `filter` | 输入可绑定；不证明持久化/业务消费 |
| 74 | `client/packages/ui/src/ApiWorkbench.vue:44` | `raw` | 输入可绑定；不证明持久化/业务消费 |
| 75 | `client/packages/ui/src/ApiWorkbench.vue:45` | `form[f.name]` | 输入可绑定；不证明持久化/业务消费 |
| 76 | `client/packages/ui/src/ApiWorkbench.vue:46` | `confirmFlag` | 输入可绑定；不证明持久化/业务消费 |
| 77 | `client/packages/ui/src/EscrowRefundPanel.vue:12` | `p.note.value` | 输入可绑定；不证明持久化/业务消费 |
| 78 | `client/packages/ui/src/PaymentRefunds.vue:12` | `p.paymentId.value` | 输入可绑定；不证明持久化/业务消费 |
| 79 | `client/packages/ui/src/PaymentRefunds.vue:12` | `p.reason.value` | 输入可绑定；不证明持久化/业务消费 |
| 80 | `client/packages/ui/src/PaymentRefunds.vue:14` | `p.reviewReason.value` | 输入可绑定；不证明持久化/业务消费 |
| 81 | `client/packages/ui/src/ShopCentre.vue:13` | `p.note.value` | 输入可绑定；不证明持久化/业务消费 |
| 82 | `client/packages/ui/src/ShopCentre.vue:14` | `p.edit.name` | 输入可绑定；不证明持久化/业务消费 |
| 83 | `client/packages/ui/src/ShopCentre.vue:14` | `p.edit.description` | 输入可绑定；不证明持久化/业务消费 |
| 84 | `client/packages/ui/src/ShopCentre.vue:14` | `product` | 输入可绑定；不证明持久化/业务消费 |
| 85 | `client/packages/ui/src/ShopCentre.vue:15` | `p.application.businessName` | 输入可绑定；不证明持久化/业务消费 |
| 86 | `client/packages/ui/src/ShopCentre.vue:15` | `p.application.creditCode` | 输入可绑定；不证明持久化/业务消费 |
| 87 | `client/packages/ui/src/ShopCentre.vue:15` | `p.application.representative` | 输入可绑定；不证明持久化/业务消费 |
| 88 | `client/packages/ui/src/ShopCentre.vue:15` | `p.application.invitationCode` | 输入可绑定；不证明持久化/业务消费 |
| 89 | `client/packages/ui/src/ShopCentre.vue:15` | `p.application.licenseUploadId` | 输入可绑定；不证明持久化/业务消费 |
| 90 | `client/packages/ui/src/TradeCases.vue:18` | `p.draft.orderId` | 输入可绑定；不证明持久化/业务消费 |
| 91 | `client/packages/ui/src/TradeCases.vue:18` | `p.draft.type` | 输入可绑定；不证明持久化/业务消费 |
| 92 | `client/packages/ui/src/TradeCases.vue:18` | `p.draft.description` | 输入可绑定；不证明持久化/业务消费 |
| 93 | `client/packages/ui/src/TradeCases.vue:18` | `p.draft.expectResolution` | 输入可绑定；不证明持久化/业务消费 |
| 94 | `client/packages/ui/src/TradeCases.vue:18` | `p.draft.amount` | 输入可绑定；不证明持久化/业务消费 |
| 95 | `client/packages/ui/src/TradeCases.vue:18` | `p.draft.evidence` | 输入可绑定；不证明持久化/业务消费 |
| 96 | `client/packages/ui/src/TradeCases.vue:19` | `p.note.value` | 输入可绑定；不证明持久化/业务消费 |
| 97 | `client/packages/ui/src/TradeCases.vue:19` | `p.evidence.value` | 输入可绑定；不证明持久化/业务消费 |
| 98 | `client/packages/ui/src/TradeCases.vue:20` | `p.outcome.value` | 输入可绑定；不证明持久化/业务消费 |
| 99 | `client/packages/ui/src/TradeCases.vue:20` | `p.amount.value` | 输入可绑定；不证明持久化/业务消费 |
| 100 | `client/packages/ui/src/TradeCases.vue:20` | `p.depositOwner.value` | 输入可绑定；不证明持久化/业务消费 |

## 后续执行顺序

1. **P0运行基线**：修HBuilderX编译依赖发现与调度任务根因可观测；明确库/JWT/密文根密钥/模式前提；启动API并验证公开、认证、管理拒绝和读写错误，不只跑编译。
2. **P0阻断错误成功**：缺失审核路由、后台重复点击/旧响应、调账null、潜伏提现伪到账、发票空凭证错误通知；完成失败事务回滚/状态不假成功测试。
3. **P1首条业务闭环**：认证→发布→审核→商品报价→幂等托管下单→采购/运费/交付→收货结算，并覆盖取消/售后/退款，逐表核对真实数据。
4. **P1管理正式界面**：分页/过滤/详情/证据与全字段出口；补分类/配方真正被业务消费、社区双模型治理、通知发送与回执。
5. **P1资金与第三方**：逐渠道真实充值/退款/提现；独立保险机构API；银行清算/手续费汇差/风险剩余款和全额售后核算。未具备合同/API资料的部分不得伪实现。
6. **全端与上线验收**：各角色每个可达按钮按不同状态实点，保存网络请求、前后数据与回执；微信/App真机、签名包、域名/隐私、备份恢复/告警与回退。

每一步分别记录实施结果、验证环境、实际请求/数据证据、剩余项。不是先将整表改成verified再补测试。
