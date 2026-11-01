<script setup lang="ts">
import {ref,onBeforeUnmount} from 'vue';
import {getBearer,subscribeSession} from '@kuaixie/sdk';
import {onLoad,onShow} from '@dcloudio/uni-app';
import ShopPublicMobile from '../../components/ShopPublicMobile.vue';
import {preferenceRef as prefs} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../../utils/prototype';
const id=ref(''),scope=ref(0);let session=getBearer();function sync(){const current=getBearer();if(current!==session){session=current;scope.value++}}
const unsubscribe=subscribeSession(sync);onBeforeUnmount(unsubscribe);onShow(sync);onLoad(q=>{id.value=String(q?.id||'')});
</script><template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><ShopPublicMobile v-if="id" :key="scope" :id="id"/></scroll-view></template>
