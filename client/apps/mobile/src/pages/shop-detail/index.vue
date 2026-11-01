<script setup lang="ts">
import {preferenceRef as prefs} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../../utils/prototype';
import {onLoad} from '@dcloudio/uni-app';
import {api} from '../../api/client';
import {useShop} from '@kuaixie/ui/useShop';
import {formatAmount} from '@kuaixie/i18n';
const p=useShop(api,async()=>false,'h5');onLoad(q=>{void p.preview(String(q?.id||''))});
</script><template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-error" v-if="p.error.value">{{p.error.value}}</view><view v-if="p.publicShop.value" class="op-card"><view class="op-title">{{p.publicShop.value.name}}</view><view>{{p.publicShop.value.description}}</view><view v-for="product in p.publicShop.value.products" :key="product.id" class="op-card" @click="uni.navigateTo({url:'/pages/product-detail/index?id='+encodeURIComponent(product.id)})"><text>{{product.name}}</text><text>{{formatAmount(product.price,'CNY')}}</text></view><button v-if="p.morePublic.value" @click="p.preview(p.publicShop.value.id,true)">{{$t('account.more')}}</button></view></scroll-view></template>
