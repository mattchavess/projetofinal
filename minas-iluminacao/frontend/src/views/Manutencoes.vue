<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { Plus } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import Modal from '../components/Modal.vue'
import { manutencaoService } from '../services/outrosServices'
import { torreService } from '../services/torreService'
import { mensagemErro } from '../services/api'
import { toast } from '../utils/toast'
import { formatarData, formatarDataHora } from '../utils/format'
import { TIPO_MANUTENCAO, STATUS_MANUTENCAO, TOM_MANUTENCAO } from '../utils/labels'

const lista = ref([])
const torres = ref([])
const filtro = ref('')
const novo = ref(false)
const erro = ref('')
const form = reactive({ torreId: '', tipo: 'PREVENTIVA', descricao: '', responsavel: '', dataPrevista: '', observacoes: '', iniciar: false })
const conclusao = ref(null) // manutenção sendo concluída
const devolver = ref(true)

async function carregar() {
  try { lista.value = await manutencaoService.listar(filtro.value ? { status: filtro.value } : {}) } catch (e) { toast.erro(mensagemErro(e)) }
}

function abrirNovo() {
  Object.assign(form, { torreId: torres.value[0]?.id ?? '', tipo: 'PREVENTIVA', descricao: '', responsavel: '', dataPrevista: '', observacoes: '', iniciar: false })
  erro.value = ''
  novo.value = true
}

async function criar() {
  erro.value = ''
  try {
    await manutencaoService.criar({ ...form, torreId: Number(form.torreId), dataPrevista: form.dataPrevista || null, responsavel: form.responsavel || null, observacoes: form.observacoes || null })
    toast.ok('Manutenção registrada.')
    novo.value = false
    carregar()
    torres.value = await torreService.listar()
  } catch (e) { erro.value = mensagemErro(e) }
}

function corpo(m, extra) {
  return { torreId: m.torreId, tipo: m.tipo, descricao: m.descricao, responsavel: m.responsavel, dataPrevista: m.dataPrevista, observacoes: m.observacoes, ...extra }
}

async function mudar(m, extra, msg) {
  try {
    await manutencaoService.atualizar(m.id, corpo(m, extra))
    toast.ok(msg)
    conclusao.value = null
    carregar()
  } catch (e) { toast.erro(mensagemErro(e)) }
}

const iniciar = (m) => mudar(m, { status: 'EM_ANDAMENTO' }, 'Manutenção iniciada. A torre está em manutenção.')
const cancelar = (m) => mudar(m, { status: 'CANCELADA', devolverTorre: true }, 'Manutenção cancelada.')
const concluir = () => mudar(conclusao.value, { status: 'CONCLUIDA', devolverTorre: devolver.value }, 'Manutenção concluída.')

watch(filtro, carregar)
onMounted(async () => {
  carregar()
  try { torres.value = await torreService.listar() } catch (e) { toast.erro(mensagemErro(e)) }
})
</script>

<template>
  <div class="pagina-cab">
    <div><h1>Manutenções</h1><p>Preventivas e corretivas. Iniciar uma manutenção coloca a torre em manutenção.</p></div>
    <button class="btn btn-primary" @click="abrirNovo"><Plus :size="16" /> Nova manutenção</button>
  </div>

  <section class="painel">
    <div class="barra-filtros">
      <select v-model="filtro" class="input" aria-label="Filtrar por situação">
        <option value="">Todas as situações</option>
        <option v-for="(r, k) in STATUS_MANUTENCAO" :key="k" :value="k">{{ r }}</option>
      </select>
    </div>
    <div class="tabela-wrap">
      <table>
        <thead><tr><th>Torre</th><th>Tipo</th><th>Descrição</th><th>Responsável</th><th>Aberta em</th><th>Prevista</th><th>Concluída</th><th>Situação</th><th></th></tr></thead>
        <tbody>
          <tr v-for="m in lista" :key="m.id">
            <td><strong>{{ m.torreNome }}</strong></td>
            <td>{{ TIPO_MANUTENCAO[m.tipo] }}</td>
            <td>{{ m.descricao }}<span v-if="m.observacoes" class="sub">{{ m.observacoes }}</span></td>
            <td>{{ m.responsavel || '—' }}</td>
            <td>{{ formatarDataHora(m.dataAbertura) }}</td>
            <td>{{ formatarData(m.dataPrevista) }}</td>
            <td>{{ formatarDataHora(m.dataConclusao) }}</td>
            <td><StatusBadge :tom="TOM_MANUTENCAO[m.status]" :texto="STATUS_MANUTENCAO[m.status]" /></td>
            <td class="acoes">
              <template v-if="m.status === 'ABERTA' || m.status === 'EM_ANDAMENTO'">
                <button v-if="m.status === 'ABERTA'" class="btn btn-sm" @click="iniciar(m)">Iniciar</button>
                <button class="btn btn-sm" @click="conclusao = m; devolver = true">Concluir</button>
                <button class="btn btn-sm" @click="cancelar(m)">Cancelar</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!lista.length" class="vazio"><strong>Nenhuma manutenção</strong>Abra uma nova manutenção para uma torre.</div>
    </div>
  </section>

  <Modal v-if="novo" titulo="Nova manutenção" @fechar="novo = false">
    <form id="form-manut" @submit.prevent="criar">
      <div v-if="erro" class="erro-form">{{ erro }}</div>
      <div class="grade-form">
        <div class="campo"><label for="mt-torre">Torre</label>
          <select id="mt-torre" v-model="form.torreId" class="input" required>
            <option v-for="t in torres" :key="t.id" :value="t.id">{{ t.nome }} · {{ t.setor }}</option>
          </select></div>
        <div class="campo"><label for="mt-tipo">Tipo</label>
          <select id="mt-tipo" v-model="form.tipo" class="input"><option v-for="(r, k) in TIPO_MANUTENCAO" :key="k" :value="k">{{ r }}</option></select></div>
        <div class="campo inteira"><label for="mt-desc">Descrição</label><textarea id="mt-desc" v-model="form.descricao" class="input" required maxlength="500"></textarea></div>
        <div class="campo"><label for="mt-resp">Responsável</label><input id="mt-resp" v-model="form.responsavel" class="input" maxlength="100" /></div>
        <div class="campo"><label for="mt-prev">Data prevista</label><input id="mt-prev" v-model="form.dataPrevista" type="date" class="input" /></div>
        <div class="campo inteira"><label for="mt-obs">Observações</label><input id="mt-obs" v-model="form.observacoes" class="input" maxlength="500" /></div>
        <label class="check inteira"><input v-model="form.iniciar" type="checkbox" /> Iniciar agora e colocar a torre em manutenção</label>
      </div>
    </form>
    <template #rodape>
      <button class="btn" type="button" @click="novo = false">Cancelar</button>
      <button class="btn btn-primary" type="submit" form="form-manut">Registrar manutenção</button>
    </template>
  </Modal>

  <Modal v-if="conclusao" titulo="Concluir manutenção" tamanho="estreita" @fechar="conclusao = null">
    <p>Concluir a manutenção de <strong>{{ conclusao.torreNome }}</strong>?</p>
    <label class="check"><input v-model="devolver" type="checkbox" /> Devolver a torre para operação (ligada)</label>
    <template #rodape>
      <button class="btn" @click="conclusao = null">Voltar</button>
      <button class="btn btn-primary" @click="concluir">Concluir manutenção</button>
    </template>
  </Modal>
</template>
