<script setup>
import ConfirmDialog from './components/ConfirmDialog.vue'
import ToastHost from './components/ToastHost.vue'
</script>

<template>
  <router-view />
  <ConfirmDialog />
  <ToastHost />
</template>
