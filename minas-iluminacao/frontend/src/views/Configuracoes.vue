<script setup>
import { useRouter } from 'vue-router'
import { auth } from '../stores/auth'
import { PERFIL } from '../utils/labels'

const router = useRouter()
function sair() { auth.sair(); router.push('/login') }
</script>

<template>
  <div class="pagina-cab"><div><h1>Configurações</h1></div></div>

  <section class="painel bloco">
    <div class="painel-cab"><h2>Sua conta</h2></div>
    <div class="painel-corpo lista">
      <div><span class="sub">Nome</span>{{ auth.usuario?.nome }}</div>
      <div><span class="sub">E-mail</span>{{ auth.usuario?.email }}</div>
      <div><span class="sub">Perfil</span>{{ PERFIL[auth.usuario?.perfil] }}</div>
      <div><button class="btn" @click="sair">Sair da conta</button></div>
    </div>
  </section>

  <section class="painel bloco">
    <div class="painel-cab"><h2>Limites de alerta</h2></div>
    <div class="painel-corpo">
      <table>
        <thead><tr><th>Medida</th><th>Atenção abaixo de</th><th>Crítico abaixo de</th></tr></thead>
        <tbody>
          <tr><td>Bateria</td><td>40%</td><td>20%</td></tr>
          <tr><td>Combustível</td><td>30%</td><td>15%</td></tr>
        </tbody>
      </table>
      <p class="sub">Definidos no servidor (<code>application.yml</code>, chaves <code>app.limites</code>). Para mudar, ajuste o arquivo e reinicie o backend.</p>
    </div>
  </section>
</template>

<style scoped>
.bloco { max-width: 640px; margin-bottom: 1rem; }
.lista { display: grid; gap: .8rem; }
.lista .sub { display: block; }
</style>
