import api from './api'

export const setorService = {
  listar: () => api.get('/setores').then((r) => r.data)
}

export const alertaService = {
  listar: (params = {}) => api.get('/alertas', { params }).then((r) => r.data),
  criar: (dados) => api.post('/alertas', dados).then((r) => r.data),
  resolver: (id) => api.patch(`/alertas/${id}/resolver`).then((r) => r.data)
}

export const manutencaoService = {
  listar: (params = {}) => api.get('/manutencoes', { params }).then((r) => r.data),
  criar: (dados) => api.post('/manutencoes', dados).then((r) => r.data),
  atualizar: (id, dados) => api.put(`/manutencoes/${id}`, dados).then((r) => r.data)
}

export const historicoService = {
  listar: (torreId) => api.get('/historico', { params: torreId ? { torreId } : {} }).then((r) => r.data)
}

export const dashboardService = {
  obter: () => api.get('/dashboard').then((r) => r.data)
}

export const usuarioService = {
  listar: () => api.get('/usuarios').then((r) => r.data),
  criar: (dados) => api.post('/usuarios', dados).then((r) => r.data),
  atualizar: (id, dados) => api.put(`/usuarios/${id}`, dados).then((r) => r.data)
}
