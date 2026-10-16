<script setup>
import { toasts } from '../utils/toast'
</script>

<template>
  <div class="toasts" aria-live="polite">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="t.tipo">{{ t.texto }}</div>
  </div>
</template>
