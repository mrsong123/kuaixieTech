<script setup lang="ts">
import CouplerSettingsPanel from '../components/CouplerSettingsPanel.vue';
import IntegrationSettingsPanel from '../components/IntegrationSettingsPanel.vue';
import PaymentSettingsPanel from '../components/PaymentSettingsPanel.vue';
import InsuranceSettingsPanel from '../components/InsuranceSettingsPanel.vue';
</script>
<template>
  <div class="admin-view integration-desk">
    <CouplerSettingsPanel/>
    <section id="payment-settings"><PaymentSettingsPanel/></section>
    <section id="insurance-settings"><InsuranceSettingsPanel/></section>
    <details class="surface"><summary>{{$t('coupler.legacyTitle')}}</summary><IntegrationSettingsPanel/></details>
  </div>
</template>
