# HBuilderX 报“node_modules缺少编译器模块”

这是 Vue3/Vite 的 **CLI 工作区工程**。编译器来自本项目的依赖，不是把 `src/` 当成 HBuilderX 免 Node 工程运行。HBuilderX 中应导入 `client/apps/mobile`，该目录同时包含 `package.json`、`vite.config.ts` 和 `src/`。

## 先执行一次可复现安装

1. 安装 Node **22.12.0 或更新版本**；本轮实际验证使用 Node 24.19.0。此最低要求来自本仓库锁定的 `@dcloudio/vite-plugin-uni` / `uni-cli-shared` 5.33 依赖元数据，不沿用旧文档的 Node 18/20 要求。
2. 使用 `pnpm 10.34.9`；不要求管理员权限或全局安装。Windows 可按下一节用 `npm.cmd exec --package=pnpm@10.34.9` 启动固定版本。这里只让 npm 启动包管理器，不让 npm 安装本工作区的依赖。
3. 在仓库的 **client 目录**运行：

```bash
pnpm install --frozen-lockfile --prod=false
pnpm doctor:mobile
pnpm build:mp-weixin
```

不要在 `apps/mobile` 单独执行 `npm install`，也不要复制他人 `node_modules`。本仓库使用 `workspace:*` 依赖和 pnpm 单锁文件，普通 npm 安装不能替代工作区安装。

`postinstall` 现在只做关键编译环境准备；完整安装校验保留为 `pnpm verify:legacy-install`，日常校验使用 `pnpm check`。两套检查已按当前真实组件委托、路由和接口实现修正过时的结构断言，保留业务约束与已提交的视觉基准检查。本轮这两个命令均已通过；缺少外部历史原件的比较会单独标记 `NOT RUN`，见下文。

已有依赖、但 HBuilderX 入口链接缺失时：

```bash
pnpm setup:mobile
pnpm doctor:mobile
```

`setup:mobile` 只修依赖入口，不下载包。Windows 使用目录 junction，其他系统使用目录链接；不复制依赖，不删除真实目录或工作区外的链接。真实目录内版本错误、文件损坏或外部链接冲突会明确报错，需要人工核对后重新安装。不要用安装 Vue2/Webpack 编译器的方式处理本项目。

## 按本次 Windows 目录执行

您打开的是 `kuaixieTech-main\kuaixieTech-main\client\apps\mobile`，作为 HBuilderX 工程目录是正确的；**安装工作区依赖时，要回到上两层的 `client`**。重复的 `kuaixieTech-main` 只是解压目录层级，本身不是编译错误原因。

在资源管理器进入您这个 `mobile` 文件夹后，右键打开 PowerShell/终端，再运行下面整段。无需假定盘符，不更改执行策略，不下载并执行远程 PowerShell 脚本，不要求全局安装 pnpm：

```powershell
# 当前应为 ...\kuaixieTech-main\kuaixieTech-main\client\apps\mobile
Set-Location ..\..
if (!(Test-Path .\pnpm-workspace.yaml) -or !(Test-Path .\apps\mobile\package.json)) {
    throw '当前目录不是 client，请停止并核对目录。'
}
Get-Location
if (!(Get-Command node.exe -ErrorAction SilentlyContinue)) { throw '请先安装受支持的 Node.js。' }
node.exe --version
node.exe -p "process.execPath"
node.exe -e "const [a,b]=process.versions.node.split('.').map(Number);if(a<22||(a===22&&b<12)){console.error('DCloud compiler requires Node >=22.12.0');process.exit(1)}"
if ($LASTEXITCODE -ne 0) { throw '先切换到符合要求的 Node，再安装和编译。' }

# npm.cmd 避免 npm.ps1 被 PowerShell 执行策略拦截；固定启动 pnpm 10.34.9。
function Invoke-ProjectPnpm {
    & npm.cmd exec --yes --package=pnpm@10.34.9 -- pnpm @args
    if ($LASTEXITCODE -ne 0) { throw '上一步失败，请先处理错误，不继续编译。' }
}
Invoke-ProjectPnpm --version
Invoke-ProjectPnpm install --frozen-lockfile --prod=false
Invoke-ProjectPnpm setup:mobile
Invoke-ProjectPnpm doctor:mobile
Invoke-ProjectPnpm build:mp-weixin
```

第一条版本输出必须满足当前编译器要求，pnpm版本输出应为 `10.34.9`。`npm exec` 首次可能从配置的 npm 仓库获取这个固定包；保持正常 TLS/校验，不使用 `strict-ssl=false`、`--force` 或 `--ignore-scripts` 来掩盖安装错误。npm对 `.npmrc` 中pnpm专用选项的提示不等于安装通过，以命令退出码和 `MOBILE_COMPILER_READY` 结果为准。

成功后在 HBuilderX 重新打开原来的 `...\client\apps\mobile`。微信开发者工具则在构建成功后导入这个 `mobile` 目录，由 `project.config.json` 指向 `dist/build/mp-weixin/app.json`。这两款工具的源码与产物识别方式不同。

## 配置 HBuilderX 使用同一个 Node

`doctor:mobile` 会输出实际 Node 版本及 `executable` 路径。仅终端检查通过，不能证明 HBuilderX 正在使用相同 Node。

- Windows：HBuilderX → 工具 → 设置 → 运行配置 → Node 路径。
- macOS：HBuilderX → 偏好设置 → 运行配置 → Node 路径。
- 设置为上面输出的系统 Node 可执行文件，保存后重启 HBuilderX，再导入 `client/apps/mobile`。

例如 `node.exe -p "process.execPath"` 返回 `C:\Program Files\nodejs\node.exe`，就把该**完整可执行文件路径**填入 HBuilderX，而不是填文件夹。这里仅为路径形式示例，实际以您机器的输出为准。当前两个直接DCloud编译依赖均声明 `>=22.12.0`，没有 `20.19` 兼容分支；Vite5本身较宽的 `^18.0.0 || >=20.0.0` 要求不能覆盖DCloud的更严格要求。

IDE与CLI编译器的识别能力也必须兼容。若终端完整构建成功而 HBuilderX 仍报相同错误，请记录 HBuilderX 版本、所选 Node 路径及导入目录，不要重复混装不同版本依赖。

官方工程结构及 Node 路径配置参考：[DCloud CLI 文档](https://github.com/dcloudio/unidocs-zh/blob/master/docs/quickstart-cli.md#cli创建项目和hbuilderx可视化界面创建项目的区别clidiff)。该文档的通用 Node 版本说明可能早于本项目具体依赖的 engines，以锁定包要求为准。

## 检查实际验证了什么

新检查会核对13个实际入口及一致版本，包括 `vite-plugin-uni`、`uni-cli-shared`、`uni-i18n`、App/H5/小程序 Vite 编译器、`uni-mp-compiler`、Vue3和项目Vite5。小程序/App插件使用官方 `uni-app.main` 编译入口，不错误地把缺少旧 `main` 当成损坏。每个移动端 dev/build 命令在编译前运行此检查，失败会停止。

本轮已验证固定锁文件的离线完整安装、14项设置回归断言、隔离工作区的缺失/悬空入口修复后真实微信编译，以及真实项目的发行构建、开发模式首次编译和产物校验。开发监听在首次成功后由验证脚本主动停止，日志中的SIGTERM仅表示该次人工停止。**未在用户机器的 Windows/HBuilderX GUI 中执行**，不能把Linux CLI通过写成用户GUI已验收。

历史 Taro 工程、其原始 `app.css` 和最初的62页迁移 CSV 未随当前仓库提供。当前路由、实际组件、20项已提交CSS基准、图标字节和接口约束仍会执行；对上述外部原件的比较明确报告 `HISTORICAL_*_UNAVAILABLE` 与 `NOT RUN`，不代表已验收原始工程。取得原件后，在 `client` 目录执行严格比较；路径按实际位置填写，缺文件或不一致都会失败：

```bash
node scripts/verify-original-css-v11.mjs --require-original-source --source "<原始 app.css 路径>"
node scripts/verify-cross-platform.mjs --require-original-source --source-root "<原始 Taro 工程目录>"
node scripts/verify-v15-migration.mjs --require-original-ledger --ledger "<原始62页迁移 CSV 路径>"
```

微信开发者工具的 `app.json` 入口是另一层配置，参见 [微信工程导入说明](WECHAT_IMPORT.md)。先构建，再导入 `client/apps/mobile` 或其发行产物目录；不要手写空的 `app.json`。
