<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import Modal from './Modal.vue'
import StatusBadge from './StatusBadge.vue'
import BarraCarga from './BarraCarga.vue'
import { torreService } from '../services/torreService'
import { alertaService, manutencaoService, historicoService } from '../services/outrosServices'
import { mensagemErro } from '../services/api'
import { confirmar } from '../utils/confirm'
import { toast } from '../utils/toast'
import { formatarDataHora, tempoDecorrido } from '../utils/format'
import { STATUS_TORRE, TOM_STATUS, FONTE, MODO, COMANDOS, perguntaComando } from '../utils/labels'

const props = defineProps({ torreId: Number })
const emit = defineEmits(['fechar', 'alterado'])

const torre = ref(null)
const historico = ref([])
const acao = ref('') // '', 'falha' ou 'manutencao'
const formFalha = reactive({ descricao: '' })
const formManut = reactive({ descricao: '', responsavel: '' })
const ocupado = ref(false)

const emManutencao = computed(() => torre.value?.status === 'EM_MANUTENCAO')

async function carregar() {
  try {
    torre.value = await torreService.buscar(props.torreId)
    historico.value = (await historicoService.listar(props.torreId)).slice(0, 8)
  } catch (e) {
    toast.erro(mensagemErro(e))
    emit('fechar')
  }
}

// Atualização automática do painel aberto (silenciosa: não fecha nem mostra erro se falhar)
let timer = null
async function atualizarSozinho() {
  if (document.hidden || ocupado.value) return
  try {
    const nova = await torreService.buscar(props.torreId)
    const hist = (await historicoService.listar(props.torreId)).slice(0, 8)
    if (!ocupado.value) { torre.value = nova; historico.value = hist }
  } catch (e) { /* tenta de novo no próximo ciclo */ }
}

async function executar(comando) {
  const ok = await confirmar({
    titulo: 'Confirmar comando',
    mensagem: perguntaComando(comando, { nome: torre.value.nome }),
    rotulo: COMANDOS[comando]
  })
  if (!ok) return
  ocupado.value = true
  try {
    torre.value = await torreService.comando(torre.value.id, comando)
    toast.ok(`${COMANDOS[comando]}: comando registrado.`)
    historico.value = (await historicoService.listar(props.torreId)).slice(0, 8)
    emit('alterado')
  } catch (e) {
    toast.erro(mensagemErro(e))
  } finally {
    ocupado.value = false
  }
}

async function registrarFalha() {
  ocupado.value = true
  try {
    await alertaService.criar({
      torreId: torre.value.id,
      tipo: 'FALHA_TECNICA',
      nivel: 'CRITICO',
      descricao: `${formFalha.descricao} — ${torre.value.nome} — ${torre.value.setor}`
    })
    toast.ok('Falha registrada como alerta crítico.')
    formFalha.descricao = ''
    acao.value = ''
    historico.value = (await historicoService.listar(props.torreId)).slice(0, 8)
    emit('alterado')
  } catch (e) {
    toast.erro(mensagemErro(e))
  } finally {
    ocupado.value = false
  }
}

async function abrirManutencao() {
  ocupado.value = true
  try {
    await manutencaoService.criar({
      torreId: torre.value.id,
      tipo: 'CORRETIVA',
      descricao: formManut.descricao,
      responsavel: formManut.responsavel || null,
      iniciar: true
    })
    toast.ok('Manutenção corretiva aberta. A torre está em manutenção.')
    formManut.descricao = ''
    formManut.responsavel = ''
    acao.value = ''
    await carregar()
    emit('alterado')
  } catch (e) {
    toast.erro(mensagemErro(e))
  } finally {
    ocupado.value = false
  }
}

onMounted(() => { carregar(); timer = setInterval(atualizarSozinho, 3000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <Modal v-if="torre" :titulo="`${torre.nome} · ${torre.codigo}`" tamanho="larga" @fechar="emit('fechar')">
    <div class="topo">
      <StatusBadge :tom="TOM_STATUS[torre.status]" :texto="STATUS_TORRE[torre.status]" />
      <span class="sub">{{ torre.setor }} · {{ torre.localizacao || 'sem localização' }}</span>
    </div>

    <div class="medidas">
      <div><span class="rot">Bateria</span><BarraCarga :valor="torre.bateria" /></div>
      <div><span class="rot">Combustível</span><BarraCarga :valor="torre.combustivel" :critico="15" :atencao="30" /></div>
      <div><span class="rot">Fonte de energia</span>{{ FONTE[torre.fonteEnergia] }}</div>
      <div><span class="rot">Modo de iluminação</span>{{ MODO[torre.modoIluminacao] }}</div>
      <div><span class="rot">Última comunicação</span>{{ tempoDecorrido(torre.ultimaComunicacao) }}</div>
      <div><span class="rot">Última atualização</span>{{ formatarDataHora(torre.ultimaAtualizacao) }}</div>
    </div>

    <h2 class="bloco-titulo">Comandos</h2>
    <p v-if="emManutencao" class="sub">Torre em manutenção: conclua a manutenção para voltar a controlá-la.</p>
    <div class="comandos">
      <button v-for="(rotulo, c) in COMANDOS" :key="c" class="btn"
              :disabled="emManutencao || ocupado" @click="executar(c)">{{ rotulo }}</button>
    </div>

    <h2 class="bloco-titulo">Ocorrências</h2>
    <div class="comandos">
      <button class="btn" :class="{ 'btn-primary': acao === 'falha' }" @click="acao = acao === 'falha' ? '' : 'falha'">Registrar falha</button>
      <button class="btn" :class="{ 'btn-primary': acao === 'manutencao' }" :disabled="emManutencao" @click="acao = acao === 'manutencao' ? '' : 'manutencao'">Abrir manutenção corretiva</button>
    </div>

    <form v-if="acao === 'falha'" class="mini-form" @submit.prevent="registrarFalha">
      <div class="campo"><label for="f-desc">O que aconteceu?</label>
        <input id="f-desc" v-model="formFalha.descricao" class="input" required maxlength="150" placeholder="Ex.: Refletor sem resposta" /></div>
      <button class="btn btn-primary" :disabled="ocupado">Registrar falha</button>
    </form>

    <form v-if="acao === 'manutencao'" class="mini-form" @submit.prevent="abrirManutencao">
      <div class="campo"><label for="m-desc">Descrição do reparo</label>
        <input id="m-desc" v-model="formManut.descricao" class="input" required maxlength="500" /></div>
      <div class="campo"><label for="m-resp">Responsável</label>
        <input id="m-resp" v-model="formManut.responsavel" class="input" maxlength="100" /></div>
      <button class="btn btn-primary" :disabled="ocupado">Abrir e colocar torre em manutenção</button>
    </form>

    <h2 class="bloco-titulo">Últimas operações</h2>
    <div v-if="!historico.length" class="sub">Nenhuma operação registrada para esta torre.</div>
    <ul v-else class="lista-hist">
      <li v-for="h in historico" :key="h.id"><span class="sub">{{ formatarDataHora(h.dataHora) }} · {{ h.usuario }}</span>{{ h.operacao }}</li>
    </ul>
  </Modal>
</template>

<style scoped>
.topo { display: flex; align-items: center; gap: .8rem; flex-wrap: wrap; margin-bottom: 1rem; }
.medidas { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1rem; padding-bottom: 1rem; border-bottom: 1px solid var(--line); }
.rot { display: block; font-size: .8125rem; color: var(--muted); margin-bottom: .2rem; }
.bloco-titulo { margin: 1.1rem 0 .6rem; }
.comandos { display: flex; flex-wrap: wrap; gap: .5rem; }
.mini-form { display: grid; gap: .7rem; margin-top: .8rem; padding: .9rem; background: #f4f7f9; border-radius: var(--radius); max-width: 520px; }
.lista-hist { list-style: none; margin: 0; padding: 0; }
.lista-hist li { padding: .45rem 0; border-bottom: 1px solid #e8edf0; }
.lista-hist li:last-child { border: 0; }
@media (max-width: 640px) { .medidas { grid-template-columns: 1fr 1fr; } }
</style>
