import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '../stores/auth'

import AdminLayout from '../layouts/AdminLayout.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Torres from '../views/Torres.vue'
import Monitoramento from '../views/Monitoramento.vue'
import Alertas from '../views/Alertas.vue'
import Manutencoes from '../views/Manutencoes.vue'
import Historico from '../views/Historico.vue'
import Usuarios from '../views/Usuarios.vue'
import Configuracoes from '../views/Configuracoes.vue'

const routes = [
  { path: '/login', name: 'login', component: Login },
  {
    path: '/',
    component: AdminLayout,
    meta: { requerLogin: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: Dashboard, meta: { titulo: 'Dashboard' } },
      { path: 'torres', name: 'torres', component: Torres, meta: { titulo: 'Torres' } },
      { path: 'monitoramento', name: 'monitoramento', component: Monitoramento, meta: { titulo: 'Monitoramento' } },
      { path: 'alertas', name: 'alertas', component: Alertas, meta: { titulo: 'Alertas' } },
      { path: 'manutencoes', name: 'manutencoes', component: Manutencoes, meta: { titulo: 'Manutenções' } },
      { path: 'historico', name: 'historico', component: Historico, meta: { titulo: 'Histórico' } },
      { path: 'usuarios', name: 'usuarios', component: Usuarios, meta: { titulo: 'Usuários', requerAdmin: true } },
      { path: 'configuracoes', name: 'configuracoes', component: Configuracoes, meta: { titulo: 'Configurações' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const exigeLogin = to.matched.some((r) => r.meta.requerLogin)
  if (exigeLogin && !auth.logado) return { name: 'login' }
  if (to.name === 'login' && auth.logado) return { name: 'dashboard' }
  if (to.matched.some((r) => r.meta.requerAdmin) && !auth.admin) return { name: 'dashboard' }
})

router.afterEach((to) => {
  document.title = (to.meta.titulo ? to.meta.titulo + ' · ' : '') + 'Iluminação de Minas'
})

export default router
