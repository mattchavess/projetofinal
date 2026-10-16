import { reactive } from 'vue'

const salvo = JSON.parse(localStorage.getItem('sessao') || 'null')

export const auth = reactive({
  token: salvo?.token || null,
  usuario: salvo?.usuario || null,

  get logado() { return !!this.token },
  get admin() { return this.usuario?.perfil === 'ADMIN' },

  entrar(resposta) {
    this.token = resposta.token
    this.usuario = { nome: resposta.nome, email: resposta.email, perfil: resposta.perfil }
    localStorage.setItem('sessao', JSON.stringify({ token: this.token, usuario: this.usuario }))
  },

  sair() {
    this.token = null
    this.usuario = null
    localStorage.removeItem('sessao')
  }
})
