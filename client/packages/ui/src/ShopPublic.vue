<script setup lang="ts">
import {watch} from 'vue';
import type {ApiClient} from '@kuaixie/sdk';
import {formatAmount,t} from '@kuaixie/i18n';
import {useShop} from './useShop';
const props=defineProps<{client:ApiClient;id:string}>();const p=useShop(props.client,async()=>false,'web');watch(()=>props.id,id=>{void p.preview(id)},{immediate:true});
</script><template><section class="business-card"><p v-if="p.error.value" role="alert">{{p.error.value}}</p><template v-if="p.publicShop.value"><h2>{{p.publicShop.value.name}}</h2><p>{{p.publicShop.value.description}}</p><article v-for="product in p.publicShop.value.products" :key="product.id"><a :href="'#services/product-detail?id='+encodeURIComponent(product.id)">{{product.name}}</a><p>{{formatAmount(product.price,'CNY')}}</p></article><button v-if="p.morePublic.value" @click="p.preview(id,true)">{{t('account.more')}}</button></template></section></template>
