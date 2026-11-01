<script setup lang="ts">
import {ref,onBeforeUnmount} from 'vue';
import {onShow} from '@dcloudio/uni-app';
import {getBearer,subscribeSession} from '@kuaixie/sdk';
import ShopCentreMobile from '../../components/ShopCentreMobile.vue';
import {preferenceRef as prefs} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../../utils/prototype';
const scope=ref(0);let session=getBearer();function sync(){const current=getBearer();if(current!==session){session=current;scope.value++}}
const unsubscribe=subscribeSession(sync);onBeforeUnmount(unsubscribe);onShow(sync);
</script><template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><ShopCentreMobile :key="scope"/></scroll-view></template>
