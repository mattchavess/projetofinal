<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LayoutDashboard, Radio, Activity, Bell, Wrench, History, Users, Settings, LogOut, Menu, Lightbulb } from 'lucide-vue-next'
import { auth } from '../stores/auth'
import { alertaService } from '../services/outrosServices'

const route = useRoute()
const router = useRouter()
const menuAberto = ref(false)
const alertasAtivos = ref(0)
let timer = null

const itens = [
  { to: '/dashboard', rotulo: 'Dashboard', icone: LayoutDashboard },
  { to: '/torres', rotulo: 'Torres', icone: Lightbulb },
  { to: '/monitoramento', rotulo: 'Monitoramento', icone: Activity },
  { to: '/alertas', rotulo: 'Alertas', icone: Bell, contador: true },
  { to: '/manutencoes', rotulo: 'Manutenções', icone: Wrench },
  { to: '/historico', rotulo: 'Histórico', icone: History },
  { to: '/usuarios', rotulo: 'Usuários', icone: Users, admin: true },
  { to: '/configuracoes', rotulo: 'Configurações', icone: Settings }
]

async function contarAlertas() {
  try { alertasAtivos.value = (await alertaService.listar({ status: 'ATIVO' })).length } catch { /* tratado no interceptor */ }
}

function sair() {
  auth.sair()
  router.push('/login')
}

watch(() => route.fullPath, () => { menuAberto.value = false; contarAlertas() })
onMounted(() => { contarAlertas(); timer = setInterval(contarAlertas, 20000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="casca">
    <header class="topo-movel">
      <button class="btn btn-icone" aria-label="Abrir menu" @click="menuAberto = true"><Menu :size="18" /></button>
      <strong>{{ route.meta.titulo }}</strong>
    </header>

    <div v-if="menuAberto" class="cortina" @click="menuAberto = false"></div>
    <aside class="lateral" :class="{ aberta: menuAberto }">
      <div class="marca">
        <span class="marca-icone"><Lightbulb :size="18" /></span>
        <span>Iluminação<br />de Minas</span>
      </div>
      <nav>
        <template v-for="i in itens" :key="i.to">
          <router-link v-if="!i.admin || auth.admin" :to="i.to" class="nav-item">
            <component :is="i.icone" :size="18" />
            <span>{{ i.rotulo }}</span>
            <span v-if="i.contador && alertasAtivos" class="contador">{{ alertasAtivos }}</span>
          </router-link>
        </template>
      </nav>
      <div class="usuario">
        <div class="usuario-nome">{{ auth.usuario?.nome }}</div>
        <button class="sair" @click="sair"><LogOut :size="16" /> Sair</button>
      </div>
    </aside>

    <main class="conteudo">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.casca { display: grid; grid-template-columns: 232px minmax(0, 1fr); min-height: 100vh; }
.lateral { background: var(--side); color: var(--side-ink); display: flex; flex-direction: column; position: sticky; top: 0; height: 100vh; }
.marca { display: flex; align-items: center; gap: .7rem; padding: 1.1rem 1rem; color: #fff; font-weight: 600; line-height: 1.15; border-bottom: 1px solid #234a5b; }
.marca-icone { width: 34px; height: 34px; border-radius: 8px; background: #e3a72f; color: #14303d; display: grid; place-items: center; }
nav { flex: 1; padding: .7rem .5rem; display: flex; flex-direction: column; gap: 2px; overflow-y: auto; }
.nav-item { display: flex; align-items: center; gap: .7rem; padding: .6rem .75rem; border-radius: var(--radius); color: var(--side-ink); text-decoration: none; }
.nav-item:hover { background: #1b3f50; }
.nav-item.router-link-active { background: var(--side-active); color: #fff; font-weight: 500; }
.contador { margin-left: auto; background: var(--critico); color: #fff; font-size: .75rem; font-weight: 600; min-width: 22px; text-align: center; padding: 0 .4rem; border-radius: 999px; }
.usuario { padding: .9rem 1rem; border-top: 1px solid #234a5b; }
.usuario-nome { color: #fff; margin-bottom: .4rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sair { background: none; border: 0; color: var(--side-ink); cursor: pointer; display: inline-flex; align-items: center; gap: .4rem; padding: 0; }
.sair:hover { color: #fff; }
.conteudo { padding: 1.4rem 1.6rem 2.5rem; min-width: 0; }
.topo-movel, .cortina { display: none; }

@media (max-width: 900px) {
  .casca { display: block; }
  .topo-movel { display: flex; align-items: center; gap: .8rem; padding: .6rem .9rem; background: var(--surface); border-bottom: 1px solid var(--line); position: sticky; top: 0; z-index: 30; }
  .lateral { position: fixed; z-index: 45; left: 0; top: 0; width: 250px; transform: translateX(-100%); transition: transform .18s; }
  .lateral.aberta { transform: none; }
  .cortina { display: block; position: fixed; inset: 0; background: rgba(15, 28, 36, .5); z-index: 40; }
  .conteudo { padding: 1rem .9rem 2rem; }
}
</style>
