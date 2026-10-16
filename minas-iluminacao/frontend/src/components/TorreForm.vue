<script setup>
import { reactive, ref, computed } from 'vue'
import Modal from './Modal.vue'
import { torreService } from '../services/torreService'
import { mensagemErro } from '../services/api'
import { toast } from '../utils/toast'
import { STATUS_TORRE, FONTE, MODO } from '../utils/labels'

const props = defineProps({ torre: Object, setores: Array })
const emit = defineEmits(['fechar', 'salvo'])

const editando = computed(() => !!props.torre)
const form = reactive({
  nome: props.torre?.nome ?? '',
  codigo: props.torre?.codigo ?? '',
  localizacao: props.torre?.localizacao ?? '',
  setorId: props.torre?.setorId ?? props.setores?.[0]?.id ?? '',
  fonteEnergia: props.torre?.fonteEnergia ?? 'SOLAR',
  status: props.torre?.status ?? 'DESLIGADA',
  bateria: props.torre?.bateria ?? 100,
  combustivel: props.torre?.combustivel ?? 100,
  modoIluminacao: props.torre?.modoIluminacao ?? 'NORMAL',
  consumoWatts: props.torre?.consumoWatts ?? 400,
  latitude: props.torre?.latitude ?? null,
  longitude: props.torre?.longitude ?? null
})
const erro = ref('')
const salvando = ref(false)

async function salvar() {
  erro.value = ''
  salvando.value = true
  const dados = {
    ...form,
    setorId: Number(form.setorId),
    bateria: Number(form.bateria),
    consumoWatts: Number(form.consumoWatts),
    combustivel: form.fonteEnergia === 'GERADOR' ? Number(form.combustivel) : null,
    latitude: form.latitude === '' ? null : form.latitude,
    longitude: form.longitude === '' ? null : form.longitude
  }
  try {
    if (editando.value) await torreService.atualizar(props.torre.id, dados)
    else await torreService.criar(dados)
    toast.ok(editando.value ? 'Torre atualizada.' : 'Torre cadastrada.')
    emit('salvo')
  } catch (e) {
    erro.value = mensagemErro(e)
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <Modal :titulo="editando ? 'Editar torre' : 'Nova torre'" @fechar="emit('fechar')">
    <form id="form-torre" @submit.prevent="salvar">
      <div v-if="erro" class="erro-form">{{ erro }}</div>
      <div class="grade-form">
        <div class="campo"><label for="t-nome">Nome</label><input id="t-nome" v-model="form.nome" class="input" required maxlength="100" /></div>
        <div class="campo"><label for="t-cod">Código</label><input id="t-cod" v-model="form.codigo" class="input" required maxlength="30" /></div>
        <div class="campo"><label for="t-setor">Setor da mina</label>
          <select id="t-setor" v-model="form.setorId" class="input" required>
            <option v-for="s in setores" :key="s.id" :value="s.id">{{ s.nome }}</option>
          </select>
        </div>
        <div class="campo"><label for="t-loc">Localização</label><input id="t-loc" v-model="form.localizacao" class="input" maxlength="200" /></div>
        <div class="campo"><label for="t-fonte">Fonte de energia</label>
          <select id="t-fonte" v-model="form.fonteEnergia" class="input">
            <option v-for="(r, k) in FONTE" :key="k" :value="k">{{ r }}</option>
          </select>
        </div>
        <div class="campo"><label for="t-status">Status</label>
          <select id="t-status" v-model="form.status" class="input">
            <option v-for="(r, k) in STATUS_TORRE" :key="k" :value="k">{{ r }}</option>
          </select>
        </div>
        <div class="campo"><label for="t-bat">Bateria (%)</label><input id="t-bat" v-model="form.bateria" type="number" min="0" max="100" class="input" required /></div>
        <div class="campo"><label for="t-comb">Combustível (%)</label>
          <input id="t-comb" v-model="form.combustivel" type="number" min="0" max="100" class="input" :disabled="form.fonteEnergia !== 'GERADOR'" />
          <small v-if="form.fonteEnergia !== 'GERADOR'">Só se aplica a torres com gerador.</small>
        </div>
        <div class="campo"><label for="t-modo">Modo de iluminação</label>
          <select id="t-modo" v-model="form.modoIluminacao" class="input">
            <option v-for="(r, k) in MODO" :key="k" :value="k">{{ r }}</option>
          </select>
        </div>
        <div class="campo"><label for="t-w">Potência nominal (W)</label><input id="t-w" v-model="form.consumoWatts" type="number" min="1" class="input" required /></div>
        <div class="campo"><label for="t-lat">Latitude</label><input id="t-lat" v-model="form.latitude" type="number" step="any" class="input" /></div>
        <div class="campo"><label for="t-lng">Longitude</label><input id="t-lng" v-model="form.longitude" type="number" step="any" class="input" /></div>
      </div>
    </form>
    <template #rodape>
      <button class="btn" type="button" @click="emit('fechar')">Cancelar</button>
      <button class="btn btn-primary" type="submit" form="form-torre" :disabled="salvando">Salvar torre</button>
    </template>
  </Modal>
</template>
