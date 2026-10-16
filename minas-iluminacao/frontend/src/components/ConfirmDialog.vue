<script setup>
import { confirmState, responder } from '../utils/confirm'
</script>

<template>
  <div v-if="confirmState.aberto" class="modal-fundo" style="z-index: 90" @keydown.esc="responder(false)">
    <div class="modal estreita" role="alertdialog" aria-modal="true">
      <div class="modal-cab"><h2>{{ confirmState.titulo }}</h2></div>
      <div class="modal-corpo">{{ confirmState.mensagem }}</div>
      <div class="modal-rodape">
        <button class="btn" @click="responder(false)">Cancelar</button>
        <button class="btn" :class="confirmState.perigo ? 'btn-perigo' : 'btn-primary'" @click="responder(true)">
          {{ confirmState.rotulo }}
        </button>
      </div>
    </div>
  </div>
</template>
