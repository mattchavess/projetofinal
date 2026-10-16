<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lightbulb } from 'lucide-vue-next'
import { authService } from '../services/authService'
import { mensagemErro } from '../services/api'
import { auth } from '../stores/auth'

const router = useRouter()
const email = ref('')
const senha = ref('')
const erro = ref('')
const carregando = ref(false)

async function entrar() {
  erro.value = ''
  carregando.value = true
  try {
    auth.entrar(await authService.login(email.value, senha.value))
    router.push('/dashboard')
  } catch (e) {
    erro.value = mensagemErro(e)
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="tela">
    <section class="lado-marca">
      <span class="icone"><Lightbulb :size="26" /></span>
      <h1>Iluminação de Minas</h1>
      <p>Controle das torres móveis, consumo, alertas e manutenções em um só lugar.</p>
    </section>
    <section class="lado-form">
      <form class="form" @submit.prevent="entrar">
        <h2>Entrar no painel</h2>
        <div v-if="erro" class="erro-form" role="alert">{{ erro }}</div>
        <div class="campo">
          <label for="email">E-mail</label>
          <input id="email" v-model="email" class="input" type="email" autocomplete="username" required autofocus />
        </div>
        <div class="campo">
          <label for="senha">Senha</label>
          <input id="senha" v-model="senha" class="input" type="password" autocomplete="current-password" required />
        </div>
        <button class="btn btn-primary" :disabled="carregando">{{ carregando ? 'Entrando…' : 'Entrar' }}</button>
      </form>
    </section>
  </div>
</template>

<style scoped>
.tela { min-height: 100vh; display: grid; grid-template-columns: 1fr 1fr; }
.lado-marca { background: var(--side); color: #fff; display: flex; flex-direction: column; justify-content: center; gap: 1rem; padding: 3rem; }
.lado-marca h1 { font-size: 2rem; }
.lado-marca p { color: var(--side-ink); max-width: 30ch; margin: 0; font-size: 1.05rem; }
.icone { width: 48px; height: 48px; border-radius: 10px; background: #e3a72f; color: #14303d; display: grid; place-items: center; }
.lado-form { display: grid; place-items: center; padding: 2rem 1.2rem; }
.form { width: 100%; max-width: 360px; display: grid; gap: 1rem; }
@media (max-width: 760px) {
  .tela { grid-template-columns: 1fr; }
  .lado-marca { padding: 1.6rem 1.2rem; }
  .lado-marca h1 { font-size: 1.4rem; }
}
</style>
