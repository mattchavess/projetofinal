<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import StatusBadge from '../components/StatusBadge.vue'
import BarraCarga from '../components/BarraCarga.vue'
import TorrePainel from '../components/TorrePainel.vue'
import { torreService } from '../services/torreService'
import { alertaService } from '../services/outrosServices'
import { mensagemErro } from '../services/api'
import { toast } from '../utils/toast'
import { tempoDecorrido } from '../utils/format'
import { STATUS_TORRE, MODO } from '../utils/labels'

const torres = ref([])
const alertas = ref([])
const filtro = ref('')
const aberta = ref(null)
let timer = null

const INDICADORES = {
  NORMAL: { rotulo: 'Normal', tom: 'ok' },
  ALERTA: { rotulo: 'Alerta', tom: 'atencao' },
  CRITICO: { rotulo: 'Crítico', tom: 'critico' },
  MANUTENCAO: { rotulo: 'Manutenção', tom: 'info' }
}

async function carregar(silencioso = false) {
  try {
    ;[torres.value, alertas.value] = await Promise.all([torreService.listar(), alertaService.listar({ status: 'ATIVO' })])
  } catch (e) { if (silencioso !== true) toast.erro(mensagemErro(e)) }
}

/** Indicador: manutenção > crítico > alerta > normal. */
function indicador(t) {
  if (t.status === 'EM_MANUTENCAO') return 'MANUTENCAO'
  const ativos = alertas.value.filter((a) => a.torreId === t.id)
  if (t.status === 'INDISPONIVEL' || ativos.some((a) => a.nivel === 'CRITICO')) return 'CRITICO'
  if (t.status === 'ALERTA' || ativos.length) return 'ALERTA'
  return 'NORMAL'
}

const comIndicador = computed(() => torres.value.map((t) => ({ ...t, ind: indicador(t) })))
const contagem = computed(() => {
  const c = { NORMAL: 0, ALERTA: 0, CRITICO: 0, MANUTENCAO: 0 }
  comIndicador.value.forEach((t) => c[t.ind]++)
  return c
})
const ordem = { CRITICO: 0, ALERTA: 1, MANUTENCAO: 2, NORMAL: 3 }
const visiveis = computed(() =>
  comIndicador.value.filter((t) => !filtro.value || t.ind === filtro.value).sort((a, b) => ordem[a.ind] - ordem[b.ind] || a.nome.localeCompare(b.nome))
)

onMounted(() => { carregar(); timer = setInterval(() => { if (!document.hidden) carregar(true) }, 3000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="pagina-cab">
    <div><h1>Monitoramento</h1><p>As torres com problema aparecem primeiro. Atualiza a cada 3 segundos.</p></div>
  </div>

  <div class="resumo">
    <button v-for="(i, k) in INDICADORES" :key="k" class="chip" :class="[i.tom, { ativo: filtro === k }]" @click="filtro = filtro === k ? '' : k">
      <strong>{{ contagem[k] }}</strong> {{ i.rotulo }}
    </button>
  </div>

  <div v-if="!visiveis.length" class="painel vazio"><strong>Nenhuma torre neste indicador</strong>Escolha outro filtro.</div>

  <div class="grade">
    <button v-for="t in visiveis" :key="t.id" class="card" :class="INDICADORES[t.ind].tom" @click="aberta = t.id">
      <div class="card-topo">
        <div><strong>{{ t.nome }}</strong><span class="sub">{{ t.setor }}</span></div>
        <StatusBadge :tom="INDICADORES[t.ind].tom" :texto="INDICADORES[t.ind].rotulo" />
      </div>
      <dl>
        <div><dt>Estado</dt><dd>{{ STATUS_TORRE[t.status] }}</dd></div>
        <div><dt>Bateria</dt><dd><BarraCarga :valor="t.bateria" /></dd></div>
        <div><dt>Combustível</dt><dd><BarraCarga :valor="t.combustivel" :critico="15" :atencao="30" /></dd></div>
        <div><dt>Iluminação</dt><dd>{{ MODO[t.modoIluminacao] }}</dd></div>
        <div><dt>Localização</dt><dd>{{ t.localizacao || '—' }}</dd></div>
        <div><dt>Última comunicação</dt><dd>{{ tempoDecorrido(t.ultimaComunicacao) }}</dd></div>
      </dl>
    </button>
  </div>

  <TorrePainel v-if="aberta" :torre-id="aberta" @fechar="aberta = null" @alterado="carregar" />
</template>

<style scoped>
.resumo { display: flex; flex-wrap: wrap; gap: .6rem; margin-bottom: 1rem; }
.chip { display: inline-flex; align-items: center; gap: .45rem; padding: .45rem .9rem; border: 1px solid var(--line); border-radius: 999px; background: var(--surface); cursor: pointer; }
.chip strong { font-size: 1.1rem; }
.chip.ok strong { color: var(--ok); } .chip.atencao strong { color: var(--atencao); }
.chip.critico strong { color: var(--critico); } .chip.info strong { color: var(--info); }
.chip.ativo { border-color: var(--ink); box-shadow: inset 0 0 0 1px var(--ink); }
.grade { display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: .8rem; }
.card { text-align: left; background: var(--surface); border: 1px solid var(--line); border-top: 5px solid var(--neutro); border-radius: var(--radius); padding: .9rem 1rem; cursor: pointer; }
.card:hover { border-color: #aab9c2; }
.card.ok { border-top-color: var(--ok); } .card.atencao { border-top-color: #d9980f; }
.card.critico { border-top-color: var(--critico); } .card.info { border-top-color: var(--info); }
.card-topo { display: flex; justify-content: space-between; gap: .6rem; margin-bottom: .7rem; }
dl { margin: 0; display: grid; gap: .35rem; }
dl > div { display: grid; grid-template-columns: 7.5rem 1fr; align-items: center; gap: .5rem; }
dt { color: var(--muted); font-size: .8125rem; }
dd { margin: 0; min-width: 0; overflow-wrap: anywhere; }
</style>
