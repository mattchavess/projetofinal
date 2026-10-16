<script setup>
import { computed } from 'vue'
import { tomNivelCarga } from '../utils/format'

const props = defineProps({
  valor: { type: Number, default: null },
  critico: { type: Number, default: 20 },
  atencao: { type: Number, default: 40 }
})
const tom = computed(() => tomNivelCarga(props.valor, props.critico, props.atencao))
</script>

<template>
  <div v-if="valor == null" class="sub">—</div>
  <div v-else class="carga" role="meter" :aria-valuenow="valor" aria-valuemin="0" aria-valuemax="100">
    <div class="carga-trilho"><div class="carga-valor" :class="tom" :style="{ width: valor + '%' }"></div></div>
    <span class="carga-num">{{ valor }}%</span>
  </div>
</template>
