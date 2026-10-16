import api from './api'

export const torreService = {
  listar: (params = {}) => api.get('/torres', { params }).then((r) => r.data),
  buscar: (id) => api.get(`/torres/${id}`).then((r) => r.data),
  criar: (dados) => api.post('/torres', dados).then((r) => r.data),
  atualizar: (id, dados) => api.put(`/torres/${id}`, dados).then((r) => r.data),
  excluir: (id) => api.delete(`/torres/${id}`),
  comando: (id, comando) => api.post(`/torres/${id}/comando`, { comando }).then((r) => r.data),
  comandoGrupo: (torreIds, comando) =>
    api.post('/torres/comando-grupo', { torreIds, comando }).then((r) => r.data)
}
