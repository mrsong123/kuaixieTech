<script setup lang="ts">
import {useAdminStore} from '@/stores/useAdminStore';
import {provideAdminStore} from '@/stores/context';
import AdminSidebar from '@/components/AdminSidebar.vue';
import AdminTopbar from '@/components/AdminTopbar.vue';
import AdminPageHeader from '@/components/AdminPageHeader.vue';
import AdminFooter from '@/components/AdminFooter.vue';
import SettingsView from '@/views/SettingsView.vue';
import OperationsView from '@/views/OperationsView.vue';
import ConsoleView from '@/views/ConsoleView.vue';
import LoginView from '@/views/LoginView.vue';
const admin=useAdminStore();provideAdminStore(admin);
const {section,token}=admin;
</script>
<template>
 <LoginView v-if="!token"/>
 <div v-else class="admin-shell">
   <AdminSidebar />
   <div class="admin-main">
     <AdminTopbar />
     <main class="content">
       <AdminPageHeader />
       <SettingsView v-if="section==='settings'" />
       <OperationsView v-else-if="section==='all'" />
       <ConsoleView v-else />
     </main>
     <AdminFooter />
   </div>
 </div>
</template>
