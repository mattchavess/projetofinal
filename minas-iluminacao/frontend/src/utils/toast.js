import { reactive } from 'vue'

export const toasts = reactive([])
let contador = 0

function mostrar(tipo, texto) {
  const id = ++contador
  toasts.push({ id, tipo, texto })
  setTimeout(() => {
    const i = toasts.findIndex((t) => t.id === id)
    if (i >= 0) toasts.splice(i, 1)
  }, 4500)
}

export const toast = {
  ok: (texto) => mostrar('ok', texto),
  erro: (texto) => mostrar('erro', texto)
}
