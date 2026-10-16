<script setup>
defineProps({ tom: { type: String, default: 'neutro' }, texto: String })
</script>

<template>
  <span class="selo" :class="tom">{{ texto }}</span>
</template>
