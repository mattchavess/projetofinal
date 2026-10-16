<script setup>
defineProps({ rotulo: String, valor: [String, Number], unidade: String, tom: { type: String, default: '' } })
</script>

<template>
  <div class="stat" :class="tom">
    <span class="stat-rot">{{ rotulo }}</span>
    <span class="stat-val">{{ valor }}<small v-if="unidade"> {{ unidade }}</small></span>
  </div>
</template>

<style scoped>
.stat { background: var(--surface); border: 1px solid var(--line); border-left: 4px solid var(--neutro); border-radius: var(--radius); padding: .8rem 1rem; display: flex; flex-direction: column; gap: .2rem; }
.stat.ok { border-left-color: var(--ok); }
.stat.atencao { border-left-color: #d9980f; }
.stat.critico { border-left-color: var(--critico); }
.stat.info { border-left-color: var(--info); }
.stat-rot { font-size: .875rem; color: var(--muted); }
.stat-val { font-size: 1.7rem; font-weight: 600; line-height: 1.1; }
.stat-val small { font-size: .9rem; font-weight: 400; color: var(--muted); }
</style>
