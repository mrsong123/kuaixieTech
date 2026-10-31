import {fileURLToPath} from 'node:url';
import {defineConfig} from 'vite';
import uniPlugin from '@dcloudio/vite-plugin-uni';
import {createGatewayProxy} from '../../packages/config/src/proxy';
const uni = typeof uniPlugin === 'function' ? uniPlugin : uniPlugin.default;
export default defineConfig({plugins:[uni()],resolve:{alias:{'@':fileURLToPath(new URL('./src',import.meta.url))}},server:{host:'0.0.0.0',port:5175,strictPort:true,proxy:createGatewayProxy()}});
