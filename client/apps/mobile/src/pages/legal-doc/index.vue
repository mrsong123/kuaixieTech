<script setup lang="ts">
import {useLegalDocument} from '../../stores/useOriginalDocuments';
const s=useLegalDocument();
</script><template><scroll-view scroll-y class="original-subpage" :class="{'kx-dark':s.prefs.theme==='dark','kx-care':s.prefs.care}" :style="s.mobilePreferenceStyle(s.prefs)">
<text class="op-back" @click="uni.navigateBack()">‹ {{$t('m2.mobile.back')}}</text><view class="op-title">{{s.doc?s.localized(s.doc.title,s.doc.title_i18n):$t('v14.legalTitle')}}</view>
<view v-if="s.loading" class="op-empty">{{$t('account.loading')}}</view>
<view v-else-if="s.error" class="op-error">{{$t('v14.missingPublicEndpoint')}} · {{s.error}}<button class="op-action op-secondary" @click="s.load()">{{$t('common.retry')}}</button></view>
<view v-if="s.doc" class="op-card">
 <view class="v14-between"><text class="op-muted">{{$t('v14.version')}} {{s.doc.version}}</text><text class="op-muted">{{s.formatDate(s.doc.updated_at)}}</text></view>
 <template v-for="(line,i) in s.lines" :key="i">
  <view v-if="line.kind==='blank'" class="v14-spacer"></view>
  <text v-else-if="line.kind==='h1'" class="v14-md-h1">{{line.text}}</text>
  <text v-else-if="line.kind==='h2'" class="v14-md-h2">{{line.text}}</text>
  <text v-else-if="line.kind==='list'" class="v14-md-p">• {{line.text}}</text>
  <view v-else-if="line.kind==='table'" class="v14-md-table"><text v-for="(c,j) in line.cells" :key="j" class="v14-md-cell">{{c}}</text></view>
  <text v-else class="v14-md-p">{{line.text}}</text>
 </template></view><view class="op-muted v14-footer">{{$t('v14.legalFoot')}}</view>
</scroll-view></template>