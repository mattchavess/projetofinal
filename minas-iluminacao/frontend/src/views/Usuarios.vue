<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Pencil } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import Modal from '../components/Modal.vue'
import { usuarioService } from '../services/outrosServices'
import { mensagemErro } from '../services/api'
import { toast } from '../utils/toast'
import { PERFIL } from '../utils/labels'

const usuarios = ref([])
const modal = ref(false)
const editandoId = ref(null)
const erro = ref('')
const form = reactive({ nome: '', email: '', senha: '', perfil: 'OPERADOR', ativo: true })

async function carregar() {
  try { usuarios.value = await usuarioService.listar() } catch (e) { toast.erro(mensagemErro(e)) }
}

function abrir(u = null) {
  editandoId.value = u?.id ?? null
  Object.assign(form, { nome: u?.nome ?? '', email: u?.email ?? '', senha: '', perfil: u?.perfil ?? 'OPERADOR', ativo: u?.ativo ?? true })
  erro.value = ''
  modal.value = true
}

async function salvar() {
  erro.value = ''
  const dados = { ...form, senha: form.senha || null }
  try {
    if (editandoId.value) await usuarioService.atualizar(editandoId.value, dados)
    else await usuarioService.criar(dados)
    toast.ok('Usuário salvo.')
    modal.value = false
    carregar()
  } catch (e) { erro.value = mensagemErro(e) }
}

onMounted(carregar)
</script>

<template>
  <div class="pagina-cab">
    <div><h1>Usuários</h1><p>Quem acessa o painel. Só administradores veem esta área.</p></div>
    <button class="btn btn-primary" @click="abrir()"><Plus :size="16" /> Novo usuário</button>
  </div>
  <section class="painel">
    <div class="tabela-wrap">
      <table>
        <thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th><th>Situação</th><th></th></tr></thead>
        <tbody>
          <tr v-for="u in usuarios" :key="u.id">
            <td><strong>{{ u.nome }}</strong></td><td>{{ u.email }}</td><td>{{ PERFIL[u.perfil] }}</td>
            <td><StatusBadge :tom="u.ativo ? 'ok' : 'neutro'" :texto="u.ativo ? 'Ativo' : 'Desativado'" /></td>
            <td class="acoes"><button class="btn btn-sm btn-icone" :aria-label="`Editar ${u.nome}`" @click="abrir(u)"><Pencil :size="15" /></button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>

  <Modal v-if="modal" :titulo="editandoId ? 'Editar usuário' : 'Novo usuário'" @fechar="modal = false">
    <form id="form-usuario" @submit.prevent="salvar">
      <div v-if="erro" class="erro-form">{{ erro }}</div>
      <div class="grade-form">
        <div class="campo"><label for="u-nome">Nome</label><input id="u-nome" v-model="form.nome" class="input" required maxlength="100" /></div>
        <div class="campo"><label for="u-email">E-mail</label><input id="u-email" v-model="form.email" type="email" class="input" required /></div>
        <div class="campo"><label for="u-senha">Senha</label>
          <input id="u-senha" v-model="form.senha" type="password" class="input" minlength="6" :required="!editandoId" autocomplete="new-password" />
          <small v-if="editandoId">Deixe em branco para manter a senha atual.</small></div>
        <div class="campo"><label for="u-perfil">Perfil</label>
          <select id="u-perfil" v-model="form.perfil" class="input"><option v-for="(r, k) in PERFIL" :key="k" :value="k">{{ r }}</option></select></div>
        <label class="check inteira"><input v-model="form.ativo" type="checkbox" /> Usuário ativo</label>
      </div>
    </form>
    <template #rodape>
      <button class="btn" type="button" @click="modal = false">Cancelar</button>
      <button class="btn btn-primary" type="submit" form="form-usuario">Salvar usuário</button>
    </template>
  </Modal>
</template>
