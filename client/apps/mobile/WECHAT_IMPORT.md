# 微信开发者工具导入

本目录是 UniApp 源码工程，`src/pages.json` 和 `src/manifest.json` 由 UniApp 编译为微信需要的 `app.json`。不要在源码根目录手写一个空的 `app.json`，也不要把 `src/` 当作原生微信项目导入。

## 发行资源

在 `client/` 目录执行：

```bash
pnpm build:mp-weixin
```

成功后，脚本会检查生成的 `app.json`、全部注册页面、tab 图标及本地组件引用，并同步工程 AppID。然后用微信开发者工具导入 **`client/apps/mobile`**。此处的 `project.config.json` 已指定 `miniprogramRoot: dist/build/mp-weixin/`，实际入口是 `client/apps/mobile/dist/build/mp-weixin/app.json`。

也可以直接导入 `client/apps/mobile/dist/build/mp-weixin`，生成目录内的工程配置按当前目录读取 `app.json`。`dist/` 不纳入 Git，首次拉取仓库后必须先构建；导入目录前请确认实际存在 `app.json`。

## 开发资源

```bash
pnpm dev:mp-weixin
```

保持命令运行，等待第一次编译完成，再直接导入 **`client/apps/mobile/dist/dev/mp-weixin`**。源码根的工程入口固定指向发行目录，不会自动切到开发目录；请勿在工具中同时导入两个目录并混用旧产物。

开发模式可另开终端检查产物：

```bash
node scripts/verify-wechat-output.mjs --mode dev
```

## AppID 与本地设置

目前 `src/manifest.json` 的 `mp-weixin.appid` 为空，根工程使用一致的 `touristappid`，仅供界面检查。正式运行前在 `src/manifest.json` 填入已授权的小程序 AppID，再重新构建。不要仅修改开发者工具中的 ID；业务登录、服务端配置及对应小程序身份必须一致。

不要复制其他机器的 `project.private.config.json`。本地服务地址、域名检查开关和模拟器设置不应作为团队共享的生产配置。实际发布仍需核对合法域名、隐私授权、主包体积及真机运行；编译通过不代表已发布。

本次仅调整导入与产物校验，不修改小程序或移动端页面、样式、交互设计。
