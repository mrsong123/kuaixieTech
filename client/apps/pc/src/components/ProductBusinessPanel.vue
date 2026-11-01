<script setup lang="ts">
import {onMounted,ref} from 'vue';
import type {ApiClient,PublicProduct} from '@kuaixie/sdk';
import {productImage} from '@kuaixie/sdk';
import {formatAmount,uiErrorMessage} from '@kuaixie/i18n';
import ProductCheckout from '@kuaixie/ui/ProductCheckout.vue';
import ProductActions from '@kuaixie/ui/ProductActions.vue';
const props=defineProps<{client:ApiClient;id:string}>();const item=ref<PublicProduct|null>(null),error=ref('');
onMounted(async()=>{try{item.value=await props.client.catalog.product(props.id)}catch(e){error.value=uiErrorMessage(e)}});
</script>
<template><section class="business-card"><p v-if="error" role="alert">{{error}}</p><template v-if="item"><h1>{{item.name}}</h1><img v-if="productImage(item)" :src="productImage(item)||''" :alt="item.name" style="max-width:100%;max-height:400px"/><p>{{item.region}} · {{item.spec}}</p><strong>{{formatAmount(item.price,'CNY')}}</strong><p>{{$t('product.stock')}} · {{item.stock}}</p><a v-if="item.store_id" :href="'#services/shop-detail?id='+encodeURIComponent(item.store_id)">{{$t('shop.preview')}}</a><ProductActions :client="client" :product-id="item.id"/><ProductCheckout :client="client" :product="item"/></template></section></template>
