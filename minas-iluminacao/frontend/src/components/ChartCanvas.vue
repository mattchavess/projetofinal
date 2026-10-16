<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)
Chart.defaults.font.family = "'IBM Plex Sans', system-ui, sans-serif"
Chart.defaults.color = '#576873'

const props = defineProps({ type: String, data: Object, options: { type: Object, default: () => ({}) } })
const canvas = ref(null)
let grafico = null

function montar() {
  grafico?.destroy()
  grafico = new Chart(canvas.value, {
    type: props.type,
    data: JSON.parse(JSON.stringify(props.data)),
    options: { responsive: true, maintainAspectRatio: false, ...props.options }
  })
}

onMounted(montar)
watch(() => props.data, montar, { deep: true })
onBeforeUnmount(() => grafico?.destroy())
</script>

<template>
  <div class="grafico"><canvas ref="canvas"></canvas></div>
</template>

<style scoped>
.grafico { position: relative; height: 240px; }
</style>
