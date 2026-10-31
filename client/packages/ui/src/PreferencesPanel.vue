<script setup lang="ts">
import {preferenceRef} from '@kuaixie/preferences/vue';
import {setPreferences,resetPreferences,validHex,type FontChoice,type ThemeMode} from '@kuaixie/preferences';
const preferences=preferenceRef;
function themeChanged(event:Event){setPreferences({theme:(event.target as HTMLSelectElement).value as ThemeMode})}
function fontChanged(event:Event){setPreferences({font:(event.target as HTMLSelectElement).value as FontChoice})}
function sizeChanged(event:Event){setPreferences({scale:Number((event.target as HTMLInputElement).value)})}
function accentChanged(event:Event){const accent=(event.target as HTMLInputElement).value;if(validHex(accent))setPreferences({accent})}
function toggleCare(event:Event){setPreferences({care:(event.target as HTMLInputElement).checked})}
function toggleMotion(event:Event){setPreferences({reduceMotion:(event.target as HTMLInputElement).checked})}
</script>
<template>
  <section class="kx-preferences" :aria-label="$t('preferences.title')">
    <h2 class="kx-preferences__head">{{ $t('preferences.title') }}</h2>
    <div class="kx-preferences__grid">
      <label class="kx-preferences__field">{{ $t('preferences.theme') }}
        <select :value="preferences.theme" @change="themeChanged">
          <option value="system">{{ $t('preferences.theme.system') }}</option>
          <option value="light">{{ $t('preferences.theme.light') }}</option>
          <option value="dark">{{ $t('preferences.theme.dark') }}</option>
        </select>
      </label>
      <label class="kx-preferences__field">{{ $t('preferences.font') }}
        <select :value="preferences.font" @change="fontChanged">
          <option value="system">{{ $t('preferences.font.system') }}</option>
          <option value="modern">{{ $t('preferences.font.modern') }}</option>
          <option value="reading">{{ $t('preferences.font.reading') }}</option>
        </select>
      </label>
      <label class="kx-preferences__field">{{ $t('preferences.accent') }}
        <input type="color" :value="preferences.accent" @input="accentChanged" :aria-label="$t('preferences.accent')" />
      </label>
      <label class="kx-preferences__field">{{ $t('preferences.size') }}
        <input type="range" min="0.9" max="1.3" step="0.05" :value="preferences.scale" @input="sizeChanged" />
        <output>{{ Math.round(preferences.scale*100) }}%</output>
      </label>
      <label class="kx-preferences__toggle"><input type="checkbox" :checked="preferences.care" @change="toggleCare"/> {{ $t('preferences.care') }}</label>
      <label class="kx-preferences__toggle"><input type="checkbox" :checked="preferences.reduceMotion" @change="toggleMotion"/> {{ $t('preferences.motion') }}</label>
    </div>
    <p class="kx-preferences__hint">{{ $t('preferences.preview') }} · {{ $t('preferences.localeLive') }}</p>
    <p class="kx-preferences__hint">{{ $t('preferences.saved') }}</p>
    <div class="kx-preferences__actions"><button type="button" @click="resetPreferences()">{{ $t('preferences.reset') }}</button></div>
  </section>
</template>
