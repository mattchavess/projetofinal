<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import StatusBadge from '../components/StatusBadge.vue'
import { alertaService } from '../services/outrosServices'
import { mensagemErro } from '../services/api'
import { toast } from '../utils/toast'
import { formatarDataHora } from '../utils/format'
import { NIVEL_ALERTA, TOM_NIVEL, TIPO_ALERTA } from '../utils/labels'

const alertas = ref([])
const filtros = reactive({ status: 'ATIVO', nivel: '' })

async function carregar() {
  const params = {}
  if (filtros.status) params.status = filtros.status
  if (filtros.nivel) params.nivel = filtros.nivel
  try { alertas.value = await alertaService.listar(params) } catch (e) { toast.erro(mensagemErro(e)) }
}

async function resolver(a) {
  try {
    await alertaService.resolver(a.id)
    toast.ok('Alerta marcado como resolvido.')
    carregar()
  } catch (e) { toast.erro(mensagemErro(e)) }
}

watch(filtros, carregar)
onMounted(carregar)
</script>

<template>
  <div class="pagina-cab"><div><h1>Alertas</h1><p>Registro de bateria, combustível, falhas e comunicação.</p></div></div>
  <section class="painel">
    <div class="barra-filtros">
      <select v-model="filtros.status" class="input" aria-label="Filtrar por situação">
        <option value="ATIVO">Ativos</option>
        <option value="RESOLVIDO">Resolvidos</option>
        <option value="">Todos</option>
      </select>
      <select v-model="filtros.nivel" class="input" aria-label="Filtrar por nível">
        <option value="">Todos os níveis</option>
        <option v-for="(r, k) in NIVEL_ALERTA" :key="k" :value="k">{{ r }}</option>
      </select>
    </div>
    <div class="tabela-wrap">
      <table>
        <thead><tr><th>Nível</th><th>Alerta</th><th>Tipo</th><th>Data e hora</th><th>Situação</th><th></th></tr></thead>
        <tbody>
          <tr v-for="a in alertas" :key="a.id">
            <td><StatusBadge :tom="TOM_NIVEL[a.nivel]" :texto="NIVEL_ALERTA[a.nivel]" /></td>
            <td>{{ a.descricao }}</td>
            <td>{{ TIPO_ALERTA[a.tipo] }}</td>
            <td>{{ formatarDataHora(a.dataHora) }}</td>
            <td>{{ a.status === 'ATIVO' ? 'Ativo' : 'Resolvido em ' + formatarDataHora(a.dataResolucao) }}</td>
            <td class="acoes"><button v-if="a.status === 'ATIVO'" class="btn btn-sm" @click="resolver(a)">Marcar como resolvido</button></td>
          </tr>
        </tbody>
      </table>
      <div v-if="!alertas.length" class="vazio"><strong>Nenhum alerta</strong>Nada a mostrar com estes filtros.</div>
    </div>
  </section>
</template>
