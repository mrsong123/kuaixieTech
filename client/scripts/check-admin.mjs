/** Check the local Admin Vite entry without requiring any additional npm package. */
const host = process.env.ADMIN_CHECK_URL || 'http://127.0.0.1:5174/';
const fail = (message) => { console.error(`后台检查失败：${message}`); process.exitCode = 1; };
try {
  const response = await fetch(host, {signal: AbortSignal.timeout(4000), headers: {accept: 'text/html'}});
  const body = await response.text();
  if (!response.ok) fail(`${host} 返回 HTTP ${response.status}；请先执行 pnpm dev:admin（或 pnpm dev）并确认 Vite 日志。`);
  else if (!body.includes('<div id="app"></div>') || !body.includes('/src/main.ts'))
    fail(`${host} 已响应，但不像本项目的后台首页。请检查 5174 是否被其他服务占用。`);
  else console.log(`后台入口正常：${host} (HTTP ${response.status})。如果页面内容空白，请查看浏览器控制台。`);
} catch (error) {
  fail(`无法连接 ${host}。请在 client 根目录运行 pnpm dev:admin（只启动后台）或 pnpm dev（同时启动 PC 与后台）。原始错误：${error?.message || error}`);
}
