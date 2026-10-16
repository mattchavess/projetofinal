import axios from 'axios'
import { auth } from '../stores/auth'

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api' })

api.interceptors.request.use((config) => {
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const login = erro.config?.url?.includes('/auth/login')
    if (erro.response?.status === 401 && !login) {
      auth.sair()
      window.location.href = '/login'
    }
    return Promise.reject(erro)
  }
)

/** Texto de erro pronto para exibir ao operador. */
export function mensagemErro(erro) {
  return erro.response?.data?.mensagem || 'Não foi possível conectar ao servidor.'
}

export default api
