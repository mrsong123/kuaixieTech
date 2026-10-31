import {fileURLToPath} from 'node:url';
import {defineConfig} from 'vite';
import uniPlugin from '@dcloudio/vite-plugin-uni';
import {createGatewayProxy} from '../../packages/config/src/proxy';
const uni = typeof uniPlugin === 'function' ? uniPlugin : uniPlugin.default;
export default defineConfig({
  plugins:[uni()],
  resolve:{alias:[
    {find:'@',replacement:fileURLToPath(new URL('./src',import.meta.url))},
    {find:'@kuaixie/i18n/vue',replacement:fileURLToPath(new URL('../../packages/i18n/src/vue.ts',import.meta.url))},
    {find:'@kuaixie/i18n',replacement:fileURLToPath(new URL('../../packages/i18n/src/index.ts',import.meta.url))},
  ]},
  optimizeDeps:{exclude:['@kuaixie/i18n']},
  server:{host:'0.0.0.0',port:5175,strictPort:true,proxy:createGatewayProxy()},
});
