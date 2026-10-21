<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { Plus, Pencil, Trash2 } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import BarraCarga from '../components/BarraCarga.vue'
import TorreForm from '../components/TorreForm.vue'
import { torreService } from '../services/torreService'
import { setorService } from '../services/outrosServices'
import { mensagemErro } from '../services/api'
import { confirmar } from '../utils/confirm'
import { toast } from '../utils/toast'
import { formatarDataHora } from '../utils/format'
import { STATUS_TORRE, TOM_STATUS, FONTE, MODO, COMANDOS, perguntaComando } from '../utils/labels'

const torres = ref([])
const setores = ref([])
const filtros = reactive({ busca: '', status: '', setorId: '' })
const selecionadas = ref([])
const comandoGrupo = ref('CERRACAO')
const formulario = ref({ aberto: false, torre: null })
const carregando = ref(false)

async function carregar(silencioso = false) {
  if (silencioso !== true) carregando.value = true
  const params = {}
  if (filtros.busca.trim()) params.busca = filtros.busca.trim()
  if (filtros.status) params.status = filtros.status
  if (filtros.setorId) params.setorId = filtros.setorId
  try {
    torres.value = await torreService.listar(params)
    selecionadas.value = selecionadas.value.filter((id) => torres.value.some((t) => t.id === id))
  } catch (e) {
    if (silencioso !== true) toast.erro(mensagemErro(e))
  } finally {
    carregando.value = false
  }
}

// Atualização automática: a cada 3 s, só com a aba visível e sem o formulário aberto
let timer = null
function atualizarSozinho() {
  if (document.hidden || formulario.value.aberto) return
  carregar(true)
}

let espera = null
watch(filtros, () => { clearTimeout(espera); espera = setTimeout(carregar, 250) })

const todasMarcadas = computed(() => torres.value.length > 0 && selecionadas.value.length === torres.value.length)
function alternarTodas(e) { selecionadas.value = e.target.checked ? torres.value.map((t) => t.id) : [] }

function abrirForm(torre = null) { formulario.value = { aberto: true, torre } }
function aoSalvar() { formulario.value.aberto = false; carregar() }

async function excluir(t) {
  const ok = await confirmar({
    titulo: 'Excluir torre',
    mensagem: `Excluir ${t.nome}? Os alertas, manutenções e leituras desta torre também serão removidos. O histórico é mantido.`,
    rotulo: 'Excluir', perigo: true
  })
  if (!ok) return
  try {
    await torreService.excluir(t.id)
    toast.ok(`${t.nome} excluída.`)
    carregar()
  } catch (e) { toast.erro(mensagemErro(e)) }
}

async function aplicarGrupo() {
  const qtd = selecionadas.value.length
  const ok = await confirmar({
    titulo: 'Comando em grupo',
    mensagem: perguntaComando(comandoGrupo.value, { qtd }),
    rotulo: COMANDOS[comandoGrupo.value]
  })
  if (!ok) return
  try {
    const r = await torreService.comandoGrupo(selecionadas.value, comandoGrupo.value)
    toast.ok(`${COMANDOS[comandoGrupo.value]}: ${r.aplicadas} torre(s) alterada(s).`)
    if (r.ignoradas.length) toast.erro('Ignoradas: ' + r.ignoradas.join('; '))
    selecionadas.value = []
    carregar()
  } catch (e) { toast.erro(mensagemErro(e)) }
}

onMounted(async () => {
  try { setores.value = await setorService.listar() } catch (e) { toast.erro(mensagemErro(e)) }
  carregar()
  timer = setInterval(atualizarSozinho, 3000)
})
onBeforeUnmount(() => { clearInterval(timer); clearTimeout(espera) })
</script>

<template>
  <div class="pagina-cab">
    <div><h1>Torres</h1><p>Cadastro e controle da frota de iluminação.</p></div>
    <button class="btn btn-primary" @click="abrirForm()"><Plus :size="16" /> Nova torre</button>
  </div>

  <section class="painel">
    <div class="barra-filtros">
      <input v-model="filtros.busca" class="input busca" type="search" placeholder="Buscar por nome ou código" aria-label="Buscar torre" />
      <select v-model="filtros.status" class="input" aria-label="Filtrar por status">
        <option value="">Todos os status</option>
        <option v-for="(r, k) in STATUS_TORRE" :key="k" :value="k">{{ r }}</option>
      </select>
      <select v-model="filtros.setorId" class="input" aria-label="Filtrar por setor">
        <option value="">Todos os setores</option>
        <option v-for="s in setores" :key="s.id" :value="s.id">{{ s.nome }}</option>
      </select>
    </div>

    <div v-if="selecionadas.length" class="barra-grupo">
      <strong>{{ selecionadas.length }} selecionada(s)</strong>
      <select v-model="comandoGrupo" class="input" aria-label="Comando em grupo">
        <option v-for="(r, k) in COMANDOS" :key="k" :value="k">{{ r }}</option>
      </select>
      <button class="btn btn-primary btn-sm" @click="aplicarGrupo">Aplicar às selecionadas</button>
      <button class="btn btn-sm" @click="selecionadas = []">Limpar seleção</button>
    </div>

    <div class="tabela-wrap">
      <table>
        <thead>
          <tr>
            <th style="width: 36px"><input type="checkbox" :checked="todasMarcadas" aria-label="Selecionar todas" @change="alternarTodas" /></th>
            <th>Torre</th><th>Setor</th><th>Fonte</th><th>Status</th><th>Bateria</th><th>Combustível</th><th>Modo</th><th>Atualizada</th><th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in torres" :key="t.id">
            <td><input v-model="selecionadas" type="checkbox" :value="t.id" :aria-label="`Selecionar ${t.nome}`" /></td>
            <td><strong>{{ t.nome }}</strong><span class="sub">{{ t.codigo }}</span></td>
            <td>{{ t.setor }}</td>
            <td>{{ FONTE[t.fonteEnergia] }}</td>
            <td><StatusBadge :tom="TOM_STATUS[t.status]" :texto="STATUS_TORRE[t.status]" /></td>
            <td><BarraCarga :valor="t.bateria" /></td>
            <td><BarraCarga :valor="t.combustivel" :critico="15" :atencao="30" /></td>
            <td>{{ MODO[t.modoIluminacao] }}</td>
            <td>{{ formatarDataHora(t.ultimaAtualizacao) }}</td>
            <td class="acoes">
              <button class="btn btn-sm btn-icone" :aria-label="`Editar ${t.nome}`" @click="abrirForm(t)"><Pencil :size="15" /></button>
              <button class="btn btn-sm btn-icone" :aria-label="`Excluir ${t.nome}`" @click="excluir(t)"><Trash2 :size="15" /></button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!torres.length && !carregando" class="vazio">
        <strong>Nenhuma torre encontrada</strong>
        Ajuste os filtros ou cadastre uma nova torre.
      </div>
    </div>
  </section>

  <TorreForm v-if="formulario.aberto" :torre="formulario.torre" :setores="setores" @fechar="formulario.aberto = false" @salvo="aoSalvar" />
</template>

<style scoped>
.barra-grupo { display: flex; align-items: center; flex-wrap: wrap; gap: .6rem; padding: .6rem 1rem; background: var(--info-bg); border-bottom: 1px solid var(--line); }
.barra-grupo .input { width: auto; }
</style>
