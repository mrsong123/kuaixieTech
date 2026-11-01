# 快协随行购 · 多端部署

**现行代码就在本仓库。** 原版 Taro 小程序里跑在 Meoo 云平台上的业务，已经迁到 `server/`（Spring Boot 4 + Java 21 + MySQL）。  
前端（PC / 运营后台 / UniApp H5 / 微信 / App）只打这套 Java 接口，不再打 `mp-api.meoo.host`、Supabase 或云函数。云函数桥默认全关，未配置就失败，不假装成功。

PC 和运营后台的视觉基准为本次用户提供的 `client.zip`：采用新版纸色、红色印章和业务大厅/运营桌布局；保留现有已接通的业务接口与权限控制。移动端和小程序继续使用当前界面，不用桌面样式覆盖。

视觉约束（移动端）：页面底 `#f8fafc`，强调色 `#0ea5e9`，tab 五栏「广场 / 社区 / 发布 / 消息 / 我的」。PC/后台采用新版桌面设计，主题样式保持端内作用域，不覆盖移动端。

在 `client/` 执行（只需这一层 `pnpm install`）。

## 端与命令

| 端 | 开发 | 产物 |
| --- | --- | --- |
| PC 官网 | `pnpm dev:pc` → :5173 | `pnpm build:pc` → `apps/pc/dist` |
| 运营后台 | `pnpm dev:admin` | `pnpm build:admin` |
| 手机 H5 | `pnpm dev:mobile` → :5175 | `pnpm build:mobile` → `apps/mobile/dist/build/h5` |
| 微信小程序 | `pnpm dev:mp-weixin` | `pnpm build:mp-weixin` → `apps/mobile/dist/build/mp-weixin` |
| App（Android/iOS 基座） | `pnpm dev:app` | `pnpm build:app` 后用 HBuilderX 云打包 |

微信导入与 `app.json` 缺失排查详见 [小程序工程导入说明](apps/mobile/WECHAT_IMPORT.md)。发行构建会检查产物并同步工程入口。可在构建后导入 `apps/mobile`，其 `miniprogramRoot` 指向 `dist/build/mp-weixin/`。

微信：用微信开发者工具打开 `apps/mobile/dist/dev/mp-weixin`（开发）或 `dist/build/mp-weixin`（发行）。`manifest.json` 的 `mp-weixin.appid` 换成正式 AppID。已开 `lazyCodeLoading=requiredComponents`，减轻主包。若发行仍超 2MB，下一刀才是物理分包（会改路由，需同步改现有 v13/v14/v15 校验）。

App：`uni-app-plus` 已进依赖。自定义基座 / 证书 / 应用市场包在 HBuilderX 完成；本仓库只保证能编出 App 资源。

## 后端（同一套 Spring Boot）

目录：`kuaixietech-v1/server`，默认 `http://localhost:8080`。控制器在 `/api/public` 等路径；浏览器开发走 Vite 代理补 `/api`。微信 / App 没有 Vite，服务端 `ClientApiGatewayFilter` 同样把 `/public`、`/portal`、`/rpc`、`/me`、`/auth`、`/support` 接到 `/api…`。

启动：在 `server/` 执行 `mvn spring-boot:run`（Java 21）。微信开发者工具把 request 合法域名配成该主机；本地可设 `VITE_API_BASE_URL=http://127.0.0.1:8080`（不要带 `/api`）。生产必须 HTTPS。

H5/PC/后台直连时 CORS 用 `CORS_ORIGINS`（默认 5173/5174/5175）。未配置 JWT 时仍只有 **GET 公开目录** 可匿名访问，不要为此打开支付云函数。

## 不要做的事

- 不要为了「跨平台」把移动端改成 PC 绿皮肤。
- 不要打开 `PAYMENT_MODE` 或把个人小店塞进企业店铺树。
- 不要把 Taro 工程当运行时再维护一份。
