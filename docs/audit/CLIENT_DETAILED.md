# 用户客户端逐页面、按钮与数据盘点

审计基线：2026-11-01，Git `643e42c` 后的当前工作区静态源码。此时项目未取得用户真实环境完整运行证据；用户报告HBuilderX编译与验证码调度失败。并行修复中的文件必须在主代理收口时重新扫描并更新状态。

这里不把路由存在、API有方法、模型断言通过或资源构建成功记成业务闭环完成。**本报告没有任何“已在真实环境完整验收”的客户端功能。**

## 状态与证据规则

- **已有代码、未联调验收**：存在真实服务调用和页面逻辑，可继续验收，不代表实际库/渠道/权限全分支已跑通。
- **半功能**：至少一段可编码，但关键链条、正式业务表单或运行回执缺失。
- **明确受限/占位/未实现**：仅提示、永久禁用、装饰、浏览器能力或本地预览，不计功能完成。
- **审计风险**：发现具体代码风险，须先修复并用能复现该风险的测试验证。
- **运行受阻**：工具链/数据库/凭据/真实服务等前提不满足；不得通过假成功或吞异常消除阻塞。

## 覆盖清单与边界

本次登记 **83个小程序/App/H5注册页面、51个PC固定或服务路由、203个组件/模型源文件、603个事件/按钮控制点、2248个插值或输入绑定表达式**。2248包含文案与格式表达式，绝不是2248个数据库字段；603包含input/change/submit等事件，不是603个独立业务按钮。

- [逐控制点清单](CLIENT_BUTTONS.csv)：文件、行、元素、事件、handler、文字、disabled/v-if条件与验收状态。
- [逐输入与展示数据清单](CLIENT_DATA.csv)：每个模板绑定/插值表达式、文件/行及状态。文案与真实业务数据分开标记。
- [页面→组件/模型→API/导航机器清单](CLIENT_SURFACES.json)：每个页面的依赖源、handler与数据条目以及直接API调用证据。
- [人工审查与路由缺口](CLIENT_REVIEW.json)：下面每项人工发现的结构化版本。
- 复扫命令：`node client/scripts/audit-client-surfaces.mjs`。自动生成结果只发现源码关系，人工问题状态须在修复后更新；不能自动提升到已验收。

条件组件的静态import会形成“可能依赖”：例如某个客户端页面可引用共享保险model，并不表示用户看得见管理员操作，也不表示该页实际执行了所有可达函数。CSV中的无直接event按钮也可能通过父form提交；已列为待人工核对，而非一律误判死按钮。真正不可用控制点以下方证据表为准。

## 主要缺口与下一步

| 编号 | 优先级 | 状态 | 功能/触发 | 具体问题与下一步 | 源码证据 |
| --- | --- | --- | --- | --- | --- |
| C01 | P0 | 运行受阻 | HBuilderX缺编译器：用户HBuilderX点击编译即失败 | 历史CLI编译通过不能证明用户GUI会从pnpm工作区正确解析编译器；原bridge只查路径存在，不是完整HBuilderX启动验收；下一步：先修可复现安装/编译器发现并用用户HBuilderX版本验证，不继续宣称小程序已跑通 | `client/scripts/ensure-hbuilderx-compiler.mjs:30` |
| C02 | P0 | 运行受阻 | 验证码投递状态不透明：后台每分钟抛出统一异常 | 前台注册/找回依赖投递；现有通用异常不能区分未配置、迁移缺失、密文问题、通道故障；下一步：根因分类健康状态、独立通道诊断和真实测试验证码；不能伪成功或静音真实失败 | `server/src/main/java/com/kuaixietech/scheduler/ContactCodeDeliveryScheduler.java` |
| C03 | P1 | 半功能 | 21类通用业务流程页：采购/发货/交付/身份/通话等服务页 | 按照RPC字段生成表单，用户手填ID、URL、JSON，结果键值输出；缺订单上下文、实际原生取证、具体阶段产品流程；下一步：按订单状态逐段做专用表单；自动关联订单、上传证据/定位/签收，真实落库并核对资金与状态 | `client/apps/mobile/src/components/WorkflowScreen.vue:25` |
| C04 | P1 | 审计风险 | 通用流程旧确认跨会话：A报价或确认框挂起后切到B | 审计基线无session订阅/await快照守卫，accept_order_atomic/award_pk/respond_shipping_fee确认后直接invoke；下一步：优先加永久初始会话、对象/报价快照、旧响应拒绝和真实Vue竞态测试，再接正式履约界面 | `client/packages/ui/src/useBusinessWorkflow.ts:26` |
| C05 | P1 | 已修复代码+专项通过，真实联调待验 | 地址/购物车/收藏会话：A删除确认/地址列表未返回时切B | 本次已修：地址/购物车/收藏私有model账号绑定、订阅即清、scope dispose与generation；旧确认和旧地址选择不使用新账号。公开商品操作允许登录后新操作，但拒绝旧响应。；下一步：主代理纳入verify-saved-session.mjs的24项断言、原业务回归与跨端构建，再用真实账号验证。 | `client/packages/ui/src/useSavedCommerce.ts:6` |
| C06 | P1 | 占位 | 聊天图片视频发送：选择图片或视频再发送 | chooseMedia仅本地路径预览，send明确拒绝附件；无上传/消息落库/接收方读取链；下一步：实现有归属的聊天附件上传、扫描/限制、消息持久化及读权限；否则隐藏不可用发送入口并明确说明 | `client/apps/mobile/src/stores/useMessagesPage.ts:58` |
| C07 | P2 | 未实现（原生） | 小程序/App语音输入：点击语音 | 仅H5 Web Speech草稿；原生直接unavailable，未接录音上传/ASR服务；下一步：选择已授权语音服务，接录音权限、上传、识别回执和手工确认；禁止本地识别冒充服务器结果 | `client/apps/mobile/src/stores/useMessagesPage.ts:93` |
| C08 | P1 | 半功能 | 网络通话媒体：发起/接听网络通话 | 只有RPC信令字段；客户端无媒体SDK入房、真实connected状态、音视频权限/重连/结束清理；下一步：在受保护订单身份令牌之上接各端媒体SDK和实际入房回执；无媒体能力不得显示已接通 | `client/apps/mobile/src/pages/order-call/index.vue:4` |
| C09 | P1 | 半功能 | 隐私号码业务：申请绑定隐私号 | 通用RPC表单不等于运营商真实AXB绑定/拨号/释放/账单；需对照后端真实消费者能力；下一步：接持牌隐私号机构API、状态/释放/回调与原生拨号；界面显示真实绑定结果与限期 | `client/apps/mobile/src/pages/privacy-call/index.vue:3` |
| C10 | P1 | 半功能/语义不符 | 公开意向池：打开open-orders | 实际读取本人carrier订单列表，并非公开可接意向池；现注释已承认这只是client projection；下一步：补最小公开意向查询与接单资格/隐私投影，或改名本人订单；不要将本人订单冒充公开池 | `client/apps/mobile/src/stores/useOriginalOrderLists.ts:19` |
| C11 | P1 | 半功能 | 物流运单：打开我的运单/跟踪 | 只有订单过滤及tracking_no/shipping_fee；无真实物流轨迹、揽收/签收订阅或异常链；下一步：接物流查询/订阅、回调验签与幂等落库，前台时间线及异常操作 | `client/apps/mobile/src/stores/useOriginalOrderLists.ts:20` |
| C12 | P1 | 半功能 | 求购详情与报价出口：打开旧求购深链 | 只在首100条找ID；页面只有说明无报价/竞价CTA，窗口外有效记录不可达；下一步：增加按ID安全详情API；从详情带demandId进入报价并自动加载上下文和资格 | `client/apps/mobile/src/stores/useDemandDetailPage.ts:11` |
| C13 | P2 | 半功能 | 社区/行程详情分页窗口：打开不在最近窗口的动态/行程 | 社区与trip详情从限量列表查找；合法深链会显示不存在或窗口限制；下一步：分别增加按ID只读详情与分页评论API，正确定义下架/隐藏/403/404 | `client/apps/mobile/src/stores/useOriginalCommunityDetail.ts:12` |
| C14 | P1 | 半功能 | 购物车结账：购物车购买 | 现有仅数量/移除和跳转单品详情，没有批量结账、合单报价、总库存校验；下一步：先明确多卖家/多行程拆单和保险费用规则，再实现后端批量报价/原子预占与逐单回执 | `client/packages/ui/src/SavedCommerce.vue:9` |
| C15 | P1 | 明确受限 | 邀请复制/二维码分享：认证用户点击复制邀请码 | 按钮永久disabled，依赖新鲜服务端人脸验证但链路未接；没有真实分享/二维码生成出口；下一步：对接可信人脸校验及一次性凭证，授权后生成短期分享资源，不绕过验证直接放开 | `client/apps/mobile/src/pages/invite-share/index.vue:5` |
| C16 | P2 | 明确受限 | 设置页换绑：账号安全→换绑 | 设置行永久禁用；通用account-security RPC入口不能替代友好换绑流程；下一步：接旧联系方式验证、新联系方式验证、重新认证与会话失效，写明确成功/失败状态 | `client/apps/mobile/src/pages/settings/account/index.vue:8` |
| C17 | P2 | 占位 | 附近行程/附近商品：点击附近 | 只有toast提示，无定位、半径查询或距离排序；下一步：若开放，增加定位授权/坐标规范和后端距离查询；否则标明确不可用不当已完成功能 | `client/apps/mobile/src/stores/useIndexPage.ts:33` |
| C18 | P2 | 半功能/命名不符 | 智能推荐与路线：打开smart或route | smart仅两个公开商品比较，route仅列表+H5语音草稿，无个性推荐、地图路径或导航；下一步：按实际能力改名或接明确定义的推荐/地图业务；AI/geocode协议存在不等于已接消费者 | `client/apps/mobile/src/stores/useSmartPage.ts:24` |
| C19 | P2 | 占位 | PC应用入口及订单二维码：点击首页继续下单四格 | 四格仅待上传/待配置文本，未读取配置也无实际跳转或二维码；没有下载/发布落地页；下一步：实现受审配置的正式链接、可用平台检测、二维码和真实安装包/小程序路径；未配置保持不可点击 | `client/apps/pc/src/views/HomeView.vue:163` |
| C20 | P2 | 装饰数据 | PC首页大阪→上海行程联：查看首页大卡片 | 地名来自静态字典，是装饰示意，不是正在进行的真实订单或行程；下一步：明确示意或读取真实可公开行程；不能将其计作业务数据已验收 | `client/apps/pc/src/views/HomeView.vue:25` |
| C21 | P1 | 半功能 | 发现页面旧RPC样板：进入discover | 三个分类状态与手填postId评论RPC，非完整发现/社区/商店业务界面；下一步：整合现有真实社区/商品/行程入口，移除重复旧样板和手填技术ID | `client/apps/mobile/src/stores/useDiscoverPage.ts:15` |
| C22 | P1 | 工具入口，非正式业务完成 | 接口工作台与工具页：选择API并执行 | JSON输入输出和所有操作注册表只能提供技术可达性；无业务任务引导，危险确认也无会话守卫；下一步：开发工具与正式客户入口隔离；危险操作加账户/参数快照；每功能另列正式页面验收 | `client/apps/mobile/src/stores/useToolsPage.ts:24` |
| C23 | P1 | 受外部能力阻断 | 商品保险真实投保：选择险种/报价/投保 | 前端框架与机构参考名册有代码，但无已验证保险企业API/产品合同/直接收款回执则不能形成真实保单；下一步：按合作机构逐个报价/投保/出单/退保/理赔接入；保持客户自选、机构定价收费，平台不担保 | `client/packages/ui/src/useInsurance.ts:26` |
| C24 | P1 | 部分支持 | 充值/国际卡/银行卡/多币种：选币种/支付方式并确认 | 前端已报价/状态查询与SDK边界；仅有渠道真实收单能力才可用。银行卡、信用卡、跨境/大部分币种不能由目录或SDK构造证明；下一步：逐渠道商户/币种/回执验收，实际收款、退款和账单对账；不开放无实现渠道 | `client/packages/ui/src/usePaymentWallet.ts:51` |
| C25 | P1 | 部分支持 | 售后退款与财务核算：查看/执行售后退款 | 钱包货款退款仅限可证明的新托管来源；全额后费用/运费/保证金核算仍阻断，复杂/历史/跨币种退款未闭环；下一步：确认费用责任规则和独立核算记录，历史来源对账后逐类型开放，不能把钱包到账当银行原路到账 | `client/packages/ui/src/useEscrowRefunds.ts:10` |
| C26 | P1 | 半功能 | 移动运营台：进入移动admin | 只有受保护统计和三类只读记录；不包含完整后台审核、资金/配置窗口；无session响应守卫；下一步：按角色明确仅只读或补安全审核；优先防跨账号旧记录，后台管理不得无差别下放 | `client/apps/mobile/src/stores/useV15AdminPage.ts:20` |
| C27 | P1 | 部分已修复，余项待执行 | 客服/订单列表/邀请旧私有响应：挂起请求后换账号 | useOriginalOrderLists本次已修即时清理、token/generation/role快照、旧认证响应和旧行导航保护；消息与邀请等旧model仍应按同标准完成逐项复核。；下一步：订单列表24项专项证据纳入root回归；客服、邀请等其余model保留待办，不宣称全私有数据已覆盖。 | `client/apps/mobile/src/stores/useMessagesPage.ts:54` |
| C28 | P1 | 待真实运行验收 | 所有已编码客户业务：真实登录后执行代表性任务 | 此前资源构建和271条模型断言没有证明数据库、JWT、商户/保险/存储、调度与真机连通；下一步：建立可复现数据库/服务启动和健康检查，按测试账号跑单并记录HTTP、DB、资金及真回执，未执行项继续待验 | `client/packages/sdk/src/client.ts:35` |

## 全部移动注册页

所有行的真实运行验收均待完成。每页的完整控制点与数据表达式请按其entry/files字段检索上述JSON与CSV，避免把通用组件复制成多个已完成业务。

| 页面 | 名称 | 代码状态 | 直接入口源码 | 控制点/数据表达式（含条件子组件） | 已识别缺口 |
| --- | --- | --- | --- | --- | --- |
| `pages/index/index` | 行程广场 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/index/index.vue` | 18 / 60 | 未取得真实页面/API/数据库全分支证据 |
| `pages/demand-pool/index` | 社区 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/demand-pool/index.vue` | 15 / 23 | 未取得真实页面/API/数据库全分支证据 |
| `pages/publish-hub/index` | 发布·订单 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/publish-hub/index.vue` | 30 / 87 | 未取得真实页面/API/数据库全分支证据 |
| `pages/messages/index` | 消息 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/messages/index.vue` | 7 / 17 | 文字轮询可编码；消息图/视频仅本地预览且阻断发送；原生语音未实现；AI依赖未接业务消费者 |
| `pages/me/index` | 我的 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/me/index.vue` | 39 / 25 | 未取得真实页面/API/数据库全分支证据 |
| `pages/catalog/index` | 全部分类 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/catalog/index.vue` | 7 / 16 | 未取得真实页面/API/数据库全分支证据 |
| `pages/trips/index` | 顺路线路 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/trips/index.vue` | 5 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/discover/index` | 发现 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/discover/index.vue` | 5 / 30 | 发现页仍为手填postId评论RPC样板，不是完整发现业务页 |
| `pages/route/index` | 线路 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/route/index.vue` | 5 / 23 | H5浏览器语音草稿，非小程序/App语音服务；无实际路线规划/地图导航 |
| `pages/smart/index` | 智能推荐 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/smart/index.vue` | 3 / 32 | 仅公开商品对比/列表；没有AI推荐执行链 |
| `pages/tools/index` | 工具 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/tools/index.vue` | 3 / 46 | API工具表单/JSON输出是开发辅助界面，不能作为业务功能完成证据 |
| `pages/buyer-verify/index` | 实名认证 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/buyer-verify/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/carrier-verify/index` | 带单人认证 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/carrier-verify/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/enterprise-verify/index` | 企业（对公）认证 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/enterprise-verify/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/account-security/index` | 账号与安全 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/account-security/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/kyc-docs/index` | 认证材料 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/kyc-docs/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/pk-quotes/index` | 报价 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/pk-quotes/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/order-actions/index` | 订单操作 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/order-actions/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/purchase-proof/index` | 采购确认可追溯 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/purchase-proof/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/boarding-proof/index` | 登机留证 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/boarding-proof/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/shipping-proof/index` | 寄出留证 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/shipping-proof/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/delivery-proof/index` | 送达留证 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/delivery-proof/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/cancel-request/index` | 申请取消订单 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/cancel-request/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/invoice-request/index` | 申请电子发票 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/invoice-request/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/assist/index` | 协带管理 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/assist/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/invite/index` | 我的邀请 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/invite/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/meetup/index` | 面交 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/meetup/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/shipping-fee/index` | 运费 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/shipping-fee/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/privacy-call/index` | 隐私通话 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/privacy-call/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/order-call/index` | 网络通话 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/order-call/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/community-comment/index` | 评论 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/community-comment/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/deposit/index` | 保证金 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/deposit/index.vue` | 13 / 32 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `pages/product-detail/index` | 产品详情 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/product-detail/index.vue` | 23 / 75 | 未取得真实页面/API/数据库全分支证据 |
| `pages/trip-detail/index` | 行程详情 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/trip-detail/index.vue` | 3 / 19 | 详情从限量列表查找；列表窗口外有效ID不可达，需独立详情接口 |
| `pages/services/index` | 我的服务 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/services/index.vue` | 2 / 5 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/index` | 设置 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/index.vue` | 3 / 7 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/appearance/index` | 外观与主题 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/appearance/index.vue` | 9 / 18 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/general/index` | 通用 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/general/index.vue` | 5 / 9 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/privacy/index` | 隐私与权限 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/privacy/index.vue` | 2 / 8 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/notification/index` | 消息通知 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/notification/index.vue` | 2 / 7 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/storage/index` | 存储空间 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/storage/index.vue` | 3 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/account/index` | 账号与安全 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/settings/account/index.vue` | 3 / 7 | 换绑项永久disabled；账号注销完整流程未在此提供 |
| `pages/settings/profile/index` | 编辑资料 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/profile/index.vue` | 3 / 13 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/about/index` | 关于顺带 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/about/index.vue` | 4 / 11 | 未取得真实页面/API/数据库全分支证据 |
| `pages/settings/help/index` | 帮助与反馈 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/settings/help/index.vue` | 3 / 7 | 未取得真实页面/API/数据库全分支证据 |
| `pages/search/index` | 搜索 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/search/index.vue` | 5 / 21 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-orders/index` | 我的订单 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-orders/index.vue` | 6 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-demands/index` | 我的求购 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-demands/index.vue` | 6 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-trips/index` | 我的行程 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-trips/index.vue` | 6 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-wallet/index` | 我的钱包 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-wallet/index.vue` | 17 / 58 | 未取得真实页面/API/数据库全分支证据 |
| `pages/wallet-ledger/index` | 资金明细 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/wallet-ledger/index.vue` | 6 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-notifications/index` | 消息中心 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-notifications/index.vue` | 6 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/order-detail/index` | 订单详情 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/order-detail/index.vue` | 23 / 72 | 未取得真实页面/API/数据库全分支证据 |
| `pages/invite-team/index` | 团队收益明细 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/invite-team/index.vue` | 2 / 13 | 未取得真实页面/API/数据库全分支证据 |
| `pages/invite-apply/index` | 加入申请 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/invite-apply/index.vue` | 4 / 10 | 未取得真实页面/API/数据库全分支证据 |
| `pages/support-center/index` | 客服中心 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/support-center/index.vue` | 2 / 7 | 未取得真实页面/API/数据库全分支证据 |
| `pages/support-session/index` | 客服会话 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/support-session/index.vue` | 5 / 12 | 文字轮询可编码；消息图/视频仅本地预览且阻断发送；原生语音未实现；AI依赖未接业务消费者 |
| `pages/demand-detail/index` | 求购详情 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/demand-detail/index.vue` | 2 / 17 | 详情从限量列表查找；列表窗口外有效ID不可达，需独立详情接口；仅查看，没有从此页进入报价/成单的CTA |
| `pages/favorites/index` | 我的收藏夹 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/favorites/index.vue` | 14 / 36 | 未取得真实页面/API/数据库全分支证据 |
| `pages/cart/index` | 意向购物车 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/cart/index.vue` | 14 / 36 | 只管理数量/移除并跳单品详情；无批量结账/合单/库存总校验 |
| `pages/address-book/index` | 地址管理 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/address-book/index.vue` | 14 / 36 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-products/index` | 我的可带商品 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-products/index.vue` | 19 / 44 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-shipments/index` | 我的运单 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/my-shipments/index.vue` | 6 / 23 | 只有本人订单tracking_no；无物流轨迹查询/订阅/异常更新 |
| `pages/open-orders/index` | 公开意向池 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/open-orders/index.vue` | 5 / 21 | 实际仅本人carrier订单投影，不是公开意向池 |
| `pages/announcements/index` | 平台公告 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/announcements/index.vue` | 3 / 13 | 未取得真实页面/API/数据库全分支证据 |
| `pages/legal-doc/index` | 条款阅读 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/legal-doc/index.vue` | 2 / 15 | 未取得真实页面/API/数据库全分支证据 |
| `pages/community-detail/index` | 动态详情 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/community-detail/index.vue` | 7 / 19 | 详情从限量列表查找；列表窗口外有效ID不可达，需独立详情接口 |
| `pages/reviews/index` | 历史评价 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/reviews/index.vue` | 2 / 14 | 未取得真实页面/API/数据库全分支证据 |
| `pages/login/index` | 登录 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/login/index.vue` | 20 / 36 | 未取得真实页面/API/数据库全分支证据 |
| `pages/create-intent/index` | 发起代购意向 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/create-intent/index.vue` | 29 / 90 | 未取得真实页面/API/数据库全分支证据 |
| `pages/intent-detail/index` | 意向详情 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/intent-detail/index.vue` | 23 / 69 | 未取得真实页面/API/数据库全分支证据 |
| `pages/dispute-create/index` | 发起争议 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/dispute-create/index.vue` | 21 / 82 | 未取得真实页面/API/数据库全分支证据 |
| `pages/dispute-detail/index` | 争议处理 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/dispute-detail/index.vue` | 21 / 82 | 未取得真实页面/API/数据库全分支证据 |
| `pages/invite-share/index` | 发送邀请 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/invite-share/index.vue` | 4 / 14 | 复制永久禁用，等待新鲜服务端人脸验证；无分享/二维码闭环 |
| `pages/invite-tier/index` | 推荐人等级权益 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/invite-tier/index.vue` | 2 / 25 | 未取得真实页面/API/数据库全分支证据 |
| `pages/admin/index` | 运营台 | 半功能/明确受限；见manualGaps | `client/apps/mobile/src/pages/admin/index.vue` | 4 / 15 | 移动运营台只读指标与三类清单，不能替代后台审核/配置 |
| `pages/payment-checkout/index` | 支付 / Payment | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/payment-checkout/index.vue` | 0 / 1 | 未取得真实页面/API/数据库全分支证据 |
| `pages/insurance-document/index` | 保险机构服务 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/insurance-document/index.vue` | 0 / 1 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-insurance/index` | 投保与保单 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-insurance/index.vue` | 21 / 57 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-disputes/index` | 交易纠纷与售后 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-disputes/index.vue` | 21 / 82 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-shop/index` | 我的店铺 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-shop/index.vue` | 19 / 67 | 未取得真实页面/API/数据库全分支证据 |
| `pages/shop-detail/index` | 店铺 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/shop-detail/index.vue` | 3 / 7 | 未取得真实页面/API/数据库全分支证据 |
| `pages/my-refunds/index` | 充值退款 | 已有代码，真实联调未验收 | `client/apps/mobile/src/pages/my-refunds/index.vue` | 10 / 56 | 未取得真实页面/API/数据库全分支证据 |

## 全部PC接收路由

固定路由来自`apps/pc/src/router/index.ts`，服务目录来自`packages/sdk/src/businessNavigation.ts`。`#services/`任意字符串不合法时会回home，不是每个名字都已有功能。

| 路由 | 页面/组件入口 | 状态 | 控制点/数据表达式 | 缺口 |
| --- | --- | --- | --- | --- |
| `#home` | `client/apps/pc/src/views/HomeView.vue` | 半功能/明确受限；见manualGaps | 9 / 64 | 行程联大阪→上海为装饰示意；应用入口/二维码四格固定待上传/待配置，没有配置加载和点击 |
| `#products` | `client/apps/pc/src/views/ProductsView.vue` | 已有代码，真实联调未验收 | 42 / 177 | 代码有入口，真实联调未验收 |
| `#trips` | `client/apps/pc/src/views/TripsView.vue` | 已有代码，真实联调未验收 | 7 / 51 | 代码有入口，真实联调未验收 |
| `#route` | `client/apps/pc/src/views/RouteView.vue` | 已有代码，真实联调未验收 | 7 / 45 | 代码有入口，真实联调未验收 |
| `#community` | `client/apps/pc/src/views/CommunityView.vue` | 已有代码，真实联调未验收 | 5 / 43 | 代码有入口，真实联调未验收 |
| `#workspace` | `client/apps/pc/src/views/WorkbenchView.vue` | 半功能/明确受限；见manualGaps | 45 / 216 | API工具表单/JSON输出是开发辅助界面，不能作为业务功能完成证据 |
| `#business` | `client/apps/pc/src/views/BusinessView.vue` | 已有代码，真实联调未验收 | 73 / 363 | 代码有入口，真实联调未验收 |
| `#publish` | `client/apps/pc/src/views/BusinessCenterView.vue` | 半功能/明确受限；见manualGaps | 152 / 687 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#messages` | `client/apps/pc/src/components/SupportBusinessPanel.vue` | 半功能/明确受限；见manualGaps | 4 / 10 | 文字轮询可编码；消息图/视频仅本地预览且阻断发送；原生语音未实现；AI依赖未接业务消费者 |
| `#me` | `client/apps/pc/src/components/AccountBusinessPanel.vue` | 已有代码，真实联调未验收 | 27 / 142 | 代码有入口，真实联调未验收 |
| `#services/my-orders` | `client/apps/pc/src/components/AccountBusinessPanel.vue` | 已有代码，真实联调未验收 | 27 / 142 | 代码有入口，真实联调未验收 |
| `#services/my-demands` | `client/apps/pc/src/components/AccountBusinessPanel.vue` | 已有代码，真实联调未验收 | 27 / 142 | 代码有入口，真实联调未验收 |
| `#services/my-trips` | `client/apps/pc/src/components/AccountBusinessPanel.vue` | 已有代码，真实联调未验收 | 27 / 142 | 代码有入口，真实联调未验收 |
| `#services/my-wallet` | `client/apps/pc/src/components/AccountBusinessPanel.vue` | 已有代码，真实联调未验收 | 27 / 142 | 代码有入口，真实联调未验收 |
| `#services/my-refunds` | `client/packages/ui/src/PaymentRefunds.vue` | 已有代码，真实联调未验收 | 13 / 66 | 代码有入口，真实联调未验收 |
| `#services/my-notifications` | `client/apps/pc/src/components/AccountBusinessPanel.vue` | 已有代码，真实联调未验收 | 27 / 142 | 代码有入口，真实联调未验收 |
| `#services/my-products` | `client/packages/ui/src/ProductPublishing.vue` | 已有代码，真实联调未验收 | 16 / 46 | 代码有入口，真实联调未验收 |
| `#services/my-shop` | `client/packages/ui/src/ShopCentre.vue` | 已有代码，真实联调未验收 | 24 / 88 | 代码有入口，真实联调未验收 |
| `#services/my-insurance` | `client/packages/ui/src/InsuranceCentre.vue` | 已有代码，真实联调未验收 | 10 / 68 | 代码有入口，真实联调未验收 |
| `#services/my-disputes` | `client/packages/ui/src/TradeCases.vue` | 已有代码，真实联调未验收 | 24 / 136 | 代码有入口，真实联调未验收 |
| `#services/invite-tier` | `client/packages/ui/src/InviteSummary.vue` | 已有代码，真实联调未验收 | 1 / 14 | 代码有入口，真实联调未验收 |
| `#services/address-book` | `client/packages/ui/src/SavedCommerce.vue` | 已有代码，真实联调未验收 | 10 / 37 | 代码有入口，真实联调未验收 |
| `#services/cart` | `client/packages/ui/src/SavedCommerce.vue` | 半功能/明确受限；见manualGaps | 10 / 37 | 只管理数量/移除并跳单品详情；无批量结账/合单/库存总校验 |
| `#services/favorites` | `client/packages/ui/src/SavedCommerce.vue` | 已有代码，真实联调未验收 | 10 / 37 | 代码有入口，真实联调未验收 |
| `#services/buyer-verify` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/carrier-verify` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/enterprise-verify` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/account-security` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/kyc-docs` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/order-actions` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/purchase-proof` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/boarding-proof` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/shipping-proof` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/delivery-proof` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/cancel-request` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/invoice-request` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/meetup` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/pk-quotes` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/shipping-fee` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/assist` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/deposit` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/invite` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/privacy-call` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/order-call` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/community-comment` | `client/packages/ui/src/BusinessWorkflow.vue` | 半功能/明确受限；见manualGaps | 10 / 35 | 自动字段/手工ID/JSON或URL输入，不是完整取证/通话/认证/履约产品界面；资金确认存在会话风险（审计基线） |
| `#services/support-center` | `client/apps/pc/src/components/SupportBusinessPanel.vue` | 半功能/明确受限；见manualGaps | 4 / 10 | 文字轮询可编码；消息图/视频仅本地预览且阻断发送；原生语音未实现；AI依赖未接业务消费者 |
| `#services/announcements` | `client/apps/pc/src/components/PublicDocumentsPanel.vue` | 已有代码，真实联调未验收 | 0 / 6 | 代码有入口，真实联调未验收 |
| `#services/wallet-ledger` | `client/apps/pc/src/components/AccountBusinessPanel.vue` | 已有代码，真实联调未验收 | 27 / 142 | 代码有入口，真实联调未验收 |
| `#services/legal-doc` | `client/apps/pc/src/components/PublicDocumentsPanel.vue` | 已有代码，真实联调未验收 | 0 / 6 | 代码有入口，真实联调未验收 |
| `#services/product-detail` | `client/apps/pc/src/components/ProductBusinessPanel.vue` | 已有代码，真实联调未验收 | 12 / 88 | 代码有入口，真实联调未验收 |
| `#services/shop-detail` | `client/packages/ui/src/ShopPublic.vue` | 已有代码，真实联调未验收 | 1 / 6 | 代码有入口，真实联调未验收 |

## 数据验证不能遗漏的要求

1. 空数组/空值不是假数据的充分证据：需区分加载中、真实空集、API失败及权限拒绝。指标与金额未取得可信值时应显示未知，不能默认0制造业务事实。
2. 金额/币种/费率必须来自可信服务报价与订单版本，不得由展示目录推断渠道支持或银行到账。商品/行程快照、钱包冻结额和资金流水需逐笔真实核对。
3. 私有字段（联系人、地址、身份证照、保单、客服消息、钱包及订单）须验证本人/参与人权限、账号切换、页栈缓存、慢响应及A→B→A。公开商品的可见性与订单参与人的历史附件授权是不同策略。
4. 地图、机票/火车票、附近、RTC、AI、保险企业、信用卡、银行卡、物流和通知等不能凭SDK/供应商名字完成验收。每个具体操作要有业务consumer、配置、授权、实际请求与可核验回执。
5. 表、字段字典已有113表/1186字段，但本清单只登记界面表达式。表字段的读写、约束、真实数据与后台权限必须在根审计的数据库清单逐字段核对，不能拿模板数量替代字段覆盖。

## 后续执行顺序与验收门槛

1. **P0真正可启动**：修复HBuilderX依赖发现、明确pnpm唯一安装根；做干净环境安装→CLI→HBuilderX首次编译。后台要连实际结构，暴露验证码通道具体根因并可健康idle/明确degraded。
2. **P1危险入口先保护**：先修通用资金/履约表单、SavedCommerce、工具页及旧私有model的会话/确认快照，避免在实际账号联调前引入跨账号请求。
3. **P1做一条完整交易**：用真实角色正式页面跑发布/审核→搜索详情→报价确认→托管→采购证据→邮费→发货/面交→签收→结算，逐按钮核DB状态和资金。将通用手填JSON表单换成业务组件。
4. **P1异常与售后资金**：取消/超时/重复确认/断网/退款未决→恢复→裁决→钱包或原渠道回执→费用和保证金核算，核对奖励抑制与库存，不能只跑成功分支。
5. **P1供应商逐项落地**：每个机构/渠道列操作、授权、key配置和contract回执；先短信/邮件/文件/支付，再保险/物流/通话/通知。未接通的入口保持明示受限，不以更改文案代替实现。
6. **P2产品缺口收口**：公开意向池、独立详情API、分页评论、批量购物车、邀请分享、换绑、附近/地图、智能推荐、媒体消息与下载/二维码等按业务规则做真实链路。
7. **跨端发布验收**：PC/admin/H5/小程序/App完整功能检查，加HBuilderX GUI、微信工具、真机、签名包、网络合法域名、备份恢复和运维告警。每个通过项记录日期、角色、页面动作、HTTP、DB与回执；其余继续标待验。

静态盘点与本次修复是开始执行，不是上述所有剩余功能已经交付。主代理应维护单一待办/证据清单，逐项关闭，不再用测试数量概括“功能齐全”。


## 已执行的P1保存类修复

本轮已按主代理分工执行C05及C27中的本人订单列表部分：四个保存类model、地址/购物车/收藏原生wrapper和原本人订单列表增加账号清理、generation、确认后会话检查与scope销毁保护；公开商品页面仍可在登录后以新操作继续。没有扩大API数据范围、修改金额规则、样式或把本人订单投影冒充公开池。

`verify-saved-session.mjs` 24项实际Vue/SDK竞态断言通过；原有33项业务模型回归通过；移动完整vue-tsc通过。专项日志为`/tmp/saved-session-tests.log`、`/tmp/saved-existing-models.log`、`/tmp/saved-session-mobile-types.log`。这些证明代码级竞态修复，不是实际业务库或真机验收。主代理收口时仍需全端构建与运行联调。
