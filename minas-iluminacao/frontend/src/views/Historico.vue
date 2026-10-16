<script setup>
import { ref, watch, onMounted } from 'vue'
import { historicoService } from '../services/outrosServices'
import { torreService } from '../services/torreService'
import { mensagemErro } from '../services/api'
import { toast } from '../utils/toast'
import { formatarDataHora } from '../utils/format'

const registros = ref([])
const torres = ref([])
const torreId = ref('')

async function carregar() {
  try { registros.value = await historicoService.listar(torreId.value) } catch (e) { toast.erro(mensagemErro(e)) }
}

watch(torreId, carregar)
onMounted(async () => {
  carregar()
  try { torres.value = await torreService.listar() } catch (e) { toast.erro(mensagemErro(e)) }
})
</script>

<template>
  <div class="pagina-cab"><div><h1>Histórico</h1><p>Últimas 200 operações, da mais recente para a mais antiga.</p></div></div>
  <section class="painel">
    <div class="barra-filtros">
      <select v-model="torreId" class="input" aria-label="Filtrar por torre">
        <option value="">Todas as torres</option>
        <option v-for="t in torres" :key="t.id" :value="t.id">{{ t.nome }}</option>
      </select>
    </div>
    <div class="tabela-wrap">
      <table>
        <thead><tr><th>Data e hora</th><th>Torre</th><th>Operação</th><th>Responsável</th></tr></thead>
        <tbody>
          <tr v-for="h in registros" :key="h.id">
            <td>{{ formatarDataHora(h.dataHora) }}</td>
            <td>{{ h.torreNome || '—' }}</td>
            <td>{{ h.operacao }}</td>
            <td>{{ h.usuario }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="!registros.length" class="vazio"><strong>Sem registros</strong>As operações feitas no sistema aparecem aqui.</div>
    </div>
  </section>
</template>
