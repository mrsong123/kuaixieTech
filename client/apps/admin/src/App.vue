<script setup lang="ts">
import {ADMIN_GROUPS} from './console/tabs';
import PaymentRefunds from '@kuaixie/ui/PaymentRefunds.vue';
import ShopCentre from '@kuaixie/ui/ShopCentre.vue';
import TradeCases from '@kuaixie/ui/TradeCases.vue';
import {useAdminStore} from '@/stores/useAdminStore';
import {provideAdminStore} from '@/stores/context';
import AdminSidebar from '@/components/AdminSidebar.vue';
import AdminTopbar from '@/components/AdminTopbar.vue';
import AdminPageHeader from '@/components/AdminPageHeader.vue';
import AdminFooter from '@/components/AdminFooter.vue';
import SettingsView from '@/views/SettingsView.vue';
import OperationsView from '@/views/OperationsView.vue';
import ConsoleView from '@/views/ConsoleView.vue';
import BusinessCoverageView from '@/views/BusinessCoverageView.vue';
import LoginView from '@/views/LoginView.vue';
const admin=useAdminStore();provideAdminStore(admin);
const {section,token,go}=admin;
</script>
<template>
 <LoginView v-if="!token"/>
 <div v-else class="admin-shell">
   <AdminSidebar />
   <div class="admin-main">
     <AdminTopbar />
     <main class="content">
       <AdminPageHeader />
       <nav v-if="section==='shops'||section==='disputes'||section==='paymentRefunds'" class="workflow-tabs"><button v-for="tab in ADMIN_GROUPS.find(g=>g.key==='trade')?.tabs||[]" :key="tab.key" :class="{active:section===tab.key}" @click="go(tab.key)">{{$t(tab.labelKey)}}</button></nav>
       <SettingsView v-if="section==='settings'" />
       <BusinessCoverageView v-else-if="section==='businessCoverage'"/>
       <OperationsView v-else-if="section==='all'" />
       <PaymentRefunds v-else-if="section==='paymentRefunds'" :client="admin.api" admin/>
       <ShopCentre v-else-if="section==='shops'" :client="admin.api" admin/>
       <TradeCases v-else-if="section==='disputes'" :client="admin.api" admin/>
       <ConsoleView v-else />
     </main>
     <AdminFooter />
   </div>
 </div>
</template>

<style scoped>.workflow-tabs{display:flex;gap:10px;overflow:auto;margin:16px 0}.workflow-tabs button{white-space:nowrap;padding:10px;border:1px solid #cbd5e1;border-radius:8px}.workflow-tabs .active{background:#0e7490;color:white}</style>
