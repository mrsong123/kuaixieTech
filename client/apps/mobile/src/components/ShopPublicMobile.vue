<script setup lang="ts">
import {watch} from 'vue';
import {api} from '../api/client';
import {useShop} from '@kuaixie/ui/useShop';
import {formatAmount} from '@kuaixie/i18n';
const props=defineProps<{id:string}>();const p=useShop(api,async()=>false,'h5');watch(()=>props.id,id=>{if(id)void p.preview(id)},{immediate:true});
</script><template><view><view class="op-error" v-if="p.error.value">{{p.error.value}}</view><view v-if="p.publicShop.value" class="op-card"><view class="op-title">{{p.publicShop.value.name}}</view><view>{{p.publicShop.value.description}}</view><view v-for="product in p.publicShop.value.products" :key="product.id" class="op-card" @click="uni.navigateTo({url:'/pages/product-detail/index?id='+encodeURIComponent(product.id)})"><text>{{product.name}}</text><text>{{formatAmount(product.price,'CNY')}}</text></view><button v-if="p.morePublic.value" @click="p.preview(p.publicShop.value.id,true)">{{$t('account.more')}}</button></view></view></template>
