<script setup>
import { X } from 'lucide-vue-next'

defineProps({ titulo: String, tamanho: { type: String, default: '' } })
const emit = defineEmits(['fechar'])
</script>

<template>
  <div class="modal-fundo" @mousedown.self="emit('fechar')" @keydown.esc="emit('fechar')">
    <div class="modal" :class="tamanho" role="dialog" aria-modal="true" :aria-label="titulo">
      <div class="modal-cab">
        <h2>{{ titulo }}</h2>
        <button class="btn btn-sm btn-icone" aria-label="Fechar" @click="emit('fechar')"><X :size="16" /></button>
      </div>
      <div class="modal-corpo"><slot /></div>
      <div v-if="$slots.rodape" class="modal-rodape"><slot name="rodape" /></div>
    </div>
  </div>
</template>
