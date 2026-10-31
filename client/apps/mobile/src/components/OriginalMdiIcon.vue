<script setup lang="ts">
import {computed} from 'vue';
// The original Taro i-mdi classes require UnoCSS at compile time. Mini programs need images.
// These are rasterized glyphs from Material Design Icons 7.4.47, not bundled font files.
const props=withDefaults(defineProps<{name:string;tone?:'accent'|'white'|'muted'}>(),{tone:'accent'});
const aliases:Record<string,string>={
 'account-camera':'camera-account','account-single':'account','account-travel':'airplane',
 'account-travel-outline':'airplane','airplane-off-outline':'airplane','cosmetic':'palette-swatch-outline',
 'earth-outline':'earth','file-document-search-outline':'file-search-outline',
 'medical':'medical-bag','shield-refund-outline':'shield-outline','stroller':'baby-carriage'
};
const glyph=computed(()=>{const raw=props.name.replace(/^i-mdi-/,'');return aliases[raw]||raw});
const src=computed(()=>`/static/mdi/${props.tone}/${glyph.value}.png`);
</script>
<template><image class="mdi-asset" :src="src" mode="aspectFit"/></template>
<style scoped>.mdi-asset{display:inline-block;width:100%;height:100%;flex-shrink:0}</style>
