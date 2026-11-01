<script setup lang="ts">
import {computed} from 'vue';
import {LayoutDashboard,Settings2,Code2,Cloud,Store,ClipboardCheck} from 'lucide-vue-next';
import {t} from '@kuaixie/i18n';
import {ADMIN_GROUPS,TODO_QUEUES} from '../console/tabs';
import {useAdminContext} from '../stores/context';
const {section,sidebarOpen,docsCount,go,removeToken,stats}=useAdminContext();
const queued=new Set(TODO_QUEUES.map(q=>q.tab));
const restGroups=computed(()=>ADMIN_GROUPS.map(g=>({
  ...g,
  tabs:g.tabs.filter(tb=>tb.key!=='overview'&&!queued.has(tb.key))
})).filter(g=>g.tabs.length));
function n(key:string){const value=stats.value?.[key];if(value==null)return null;const number=Number(value);return Number.isFinite(number)?number:null}
function metric(key:string){return t(key==='todo_products'?'admin.metric.pendingProducts':'v15.adminMetric.'+key)}
</script>
<template>
<aside class="sidebar" :class="{opened:sidebarOpen}">
  <button class="admin-brand" @click="go('overview')">
    <span class="brand-symbol">KX</span>
    <span>
      <strong>{{ $t('text.db680ac702a7') }}</strong>
      <small>{{ $t('admin.desk.manifest') }}</small>
    </span>
  </button>
  <nav class="sidebar-menu">
    <button :class="{active:section==='overview'}" @click="go('overview')"><LayoutDashboard/><span>{{ $t('admin.tab.overview') }}</span></button>
    <p class="side-caption">{{ $t('admin.console.todo') }}</p>
    <button v-for="q in TODO_QUEUES" :key="q.tab" class="queue-item" :class="{active:section===q.tab,'has-work':(n(q.stat)??0)>0}" @click="go(q.tab)">
      <span>{{ metric(q.stat) }}</span>
      <b>{{ n(q.stat)??'—' }}</b>
    </button>
    <template v-for="g in restGroups" :key="g.key">
      <p class="side-caption after">{{ $t(g.labelKey) }}</p>
      <button v-for="tb in g.tabs" :key="tb.key" :class="{active:section===tb.key}" @click="go(tb.key)">
        <span>{{ $t(tb.labelKey) }}</span>
      </button>
    </template>
    <p class="side-caption after">{{ $t('text.29d43743c43b') }}</p>
    <button :class="{active:section==='all'}" @click="go('all')"><Code2/><span>{{ $t('text.0d97a269629a') }}</span><span class="side-count">{{ docsCount }}</span></button>
    <button :class="{active:section==='integrations'}" @click="go('integrations')"><Cloud/><span>{{ $t('admin.nav.integrations') }}</span></button>
    <button :class="{active:section==='business'}" @click="go('business')"><Store/><span>{{ $t('admin.nav.business') }}</span></button>
    <button :class="{active:section==='businessCoverage'}" @click="go('businessCoverage')"><ClipboardCheck/><span>{{ $t('coverage.title') }}</span></button>
    <button :class="{active:section==='settings'}" @click="go('settings')"><Settings2/><span>{{ $t('text.b935702d5314') }}</span></button>
  </nav>
  <div class="sidebar-bottom">
    <span class="avatar">K</span>
    <div>
      <b>{{ $t('admin.desk.shift') }}</b>
      <small>{{ $t('admin.desk.session') }}</small>
    </div>
    <button class="logout-link" type="button" @click="removeToken">{{ $t('admin.desk.logout') }}</button>
  </div>
</aside>
</template>
