import { reactive } from 'vue'

export const confirmState = reactive({
  aberto: false, titulo: '', mensagem: '', rotulo: 'Confirmar', perigo: false, resolver: null
})

/** Abre o diálogo e devolve uma Promise<boolean>. */
export function confirmar({ titulo = 'Confirmar ação', mensagem, rotulo = 'Confirmar', perigo = false }) {
  return new Promise((resolver) => {
    Object.assign(confirmState, { aberto: true, titulo, mensagem, rotulo, perigo, resolver })
  })
}

export function responder(valor) {
  confirmState.aberto = false
  confirmState.resolver?.(valor)
}
