<script setup lang="ts">
import ShopCentreMobile from '../../components/ShopCentreMobile.vue';
import {preferenceRef as prefs} from '@kuaixie/preferences/vue';
import {mobilePreferenceStyle} from '../../utils/prototype';
</script><template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':prefs.theme==='dark','kx-care':prefs.care}" :style="mobilePreferenceStyle(prefs)"><text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><ShopCentreMobile/></scroll-view></template>
