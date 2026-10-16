<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import StatCard from '../components/StatCard.vue'
import StatusBadge from '../components/StatusBadge.vue'
import ChartCanvas from '../components/ChartCanvas.vue'
import { dashboardService, alertaService } from '../services/outrosServices'
import { mensagemErro } from '../services/api'
import { toast } from '../utils/toast'
import { numero, tempoDecorrido } from '../utils/format'
import { STATUS_TORRE, NIVEL_ALERTA, TOM_NIVEL } from '../utils/labels'

const d = ref(null)
const alertas = ref([])
let timer = null

async function carregar() {
  try {
    d.value = await dashboardService.obter()
    alertas.value = await alertaService.listar({ status: 'ATIVO' })
  } catch (e) {
    toast.erro(mensagemErro(e))
  }
}

const CORES_STATUS = { LIGADA: '#2b8a57', DESLIGADA: '#8795a0', EM_MANUTENCAO: '#3466b0', ALERTA: '#d9980f', INDISPONIVEL: '#c0392e' }
const criticos = computed(() => alertas.value.filter((a) => a.nivel === 'CRITICO'))
const prioridade = computed(() => [...alertas.value].sort((a, b) => (a.nivel === 'CRITICO' ? -1 : 1) - (b.nivel === 'CRITICO' ? -1 : 1)).slice(0, 6))

const dadosStatus = computed(() => ({
  labels: Object.keys(d.value.torresPorStatus).map((k) => STATUS_TORRE[k]),
  datasets: [{ data: Object.values(d.value.torresPorStatus), backgroundColor: Object.keys(d.value.torresPorStatus).map((k) => CORES_STATUS[k]), borderWidth: 0 }]
}))
const dadosConsumo = computed(() => ({
  labels: Object.keys(d.value.consumoPorSetorKw),
  datasets: [{ label: 'Consumo estimado (kW)', data: Object.values(d.value.consumoPorSetorKw), backgroundColor: '#1b5e7b', borderRadius: 3 }]
}))
const dadosAlertas = computed(() => ({
  labels: Object.keys(d.value.alertasPorNivel).map((k) => NIVEL_ALERTA[k]),
  datasets: [{ label: 'Alertas ativos', data: Object.values(d.value.alertasPorNivel), backgroundColor: ['#3466b0', '#d9980f', '#c0392e'], borderRadius: 3 }]
}))
const dadosDisp = computed(() => {
  const s = d.value.torresPorStatus
  const indisp = s.INDISPONIVEL
  const manut = s.EM_MANUTENCAO
  return {
    labels: ['Disponíveis', 'Em manutenção', 'Indisponíveis'],
    datasets: [{ data: [d.value.totalTorres - indisp - manut, manut, indisp], backgroundColor: ['#2b8a57', '#3466b0', '#c0392e'], borderWidth: 0 }]
  }
})
const semLegenda = { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }
const rosca = { plugins: { legend: { position: 'right' } }, cutout: '62%' }

onMounted(() => { carregar(); timer = setInterval(carregar, 15000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="pagina-cab">
    <div>
      <h1>Dashboard</h1>
      <p v-if="d">{{ d.totalTorres }} torres cadastradas · atualiza a cada 15 segundos</p>
    </div>
  </div>

  <template v-if="d">
    <section class="painel atencao-agora" :class="{ limpo: !alertas.length }">
      <div class="painel-cab">
        <h2 v-if="alertas.length">Requer atenção: {{ criticos.length }} crítico(s) e {{ alertas.length - criticos.length }} outro(s) alerta(s) ativo(s)</h2>
        <h2 v-else>Nenhum alerta ativo. A operação está normal.</h2>
        <router-link v-if="alertas.length" to="/alertas" class="btn btn-sm">Ver alertas</router-link>
      </div>
      <ul v-if="alertas.length" class="lista-alertas">
        <li v-for="a in prioridade" :key="a.id">
          <StatusBadge :tom="TOM_NIVEL[a.nivel]" :texto="NIVEL_ALERTA[a.nivel]" />
          <span class="txt">{{ a.descricao }}</span>
          <span class="sub">{{ tempoDecorrido(a.dataHora) }}</span>
        </li>
      </ul>
    </section>

    <div class="grade-cards">
      <StatCard rotulo="Torres cadastradas" :valor="d.totalTorres" />
      <StatCard rotulo="Ligadas" :valor="d.ligadas" tom="ok" />
      <StatCard rotulo="Desligadas" :valor="d.desligadas" />
      <StatCard rotulo="Em manutenção" :valor="d.emManutencao" tom="info" />
      <StatCard rotulo="Com alerta ativo" :valor="d.comAlerta" :tom="d.comAlerta ? 'atencao' : ''" />
      <StatCard rotulo="Bateria crítica" :valor="d.bateriaCritica" :tom="d.bateriaCritica ? 'critico' : ''" />
      <StatCard rotulo="Consumo estimado agora" :valor="numero(d.consumoEstimadoKw, 2)" unidade="kW" />
      <StatCard rotulo="Tempo total de atividade" :valor="numero(d.horasAtividadeTotal, 0)" unidade="h" />
    </div>

    <div class="grade-graficos">
      <section class="painel"><div class="painel-cab"><h2>Consumo estimado por setor</h2></div><div class="painel-corpo"><ChartCanvas type="bar" :data="dadosConsumo" :options="semLegenda" /></div></section>
      <section class="painel"><div class="painel-cab"><h2>Torres por status</h2></div><div class="painel-corpo"><ChartCanvas type="doughnut" :data="dadosStatus" :options="rosca" /></div></section>
      <section class="painel"><div class="painel-cab"><h2>Alertas ativos por nível</h2></div><div class="painel-corpo"><ChartCanvas type="bar" :data="dadosAlertas" :options="semLegenda" /></div></section>
      <section class="painel"><div class="painel-cab"><h2>Disponibilidade: {{ numero(d.disponibilidadePercentual, 0) }}%</h2></div><div class="painel-corpo"><ChartCanvas type="doughnut" :data="dadosDisp" :options="rosca" /></div></section>
    </div>
  </template>
</template>

<style scoped>
.atencao-agora { border-left: 4px solid var(--critico); margin-bottom: 1rem; }
.atencao-agora.limpo { border-left-color: var(--ok); }
.lista-alertas { list-style: none; margin: 0; padding: 0; }
.lista-alertas li { display: flex; align-items: center; gap: .8rem; padding: .55rem 1rem; border-bottom: 1px solid #e8edf0; flex-wrap: wrap; }
.lista-alertas li:last-child { border: 0; }
.txt { flex: 1; min-width: 200px; }
.grade-cards { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: .8rem; margin-bottom: 1rem; }
.grade-graficos { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .8rem; }
@media (max-width: 1100px) { .grade-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 760px) { .grade-graficos { grid-template-columns: 1fr; } }
@media (max-width: 420px) { .grade-cards { grid-template-columns: 1fr; } }
</style>
