<script setup lang="ts">
import {useWorkflowPage} from '../../stores/useWorkflowPage';
import WorkflowScreen from '../../components/WorkflowScreen.vue';
const page=useWorkflowPage('shipping-fee');
</script>
<template><WorkflowScreen :page="page" /></template>
