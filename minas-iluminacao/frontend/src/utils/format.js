const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
const soData = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' })

export function formatarDataHora(valor) {
  return valor ? dataHora.format(new Date(valor)) : '—'
}

export function formatarData(valor) {
  if (!valor) return '—'
  // datas sem horário (yyyy-mm-dd) são lidas como locais para não voltar um dia
  const d = /^\d{4}-\d{2}-\d{2}$/.test(valor) ? new Date(valor + 'T00:00:00') : new Date(valor)
  return soData.format(d)
}

/** "há 5 min", "há 2 h", "há 3 dias". */
export function tempoDecorrido(valor) {
  if (!valor) return 'sem comunicação'
  const min = Math.max(0, Math.round((Date.now() - new Date(valor).getTime()) / 60000))
  if (min < 1) return 'agora'
  if (min < 60) return `há ${min} min`
  if (min < 1440) return `há ${Math.round(min / 60)} h`
  return `há ${Math.round(min / 1440)} dias`
}

export function numero(valor, casas = 1) {
  return Number(valor ?? 0).toLocaleString('pt-BR', { maximumFractionDigits: casas })
}

/** Nível de gravidade usado nos indicadores visuais de nível de carga. */
export function tomNivelCarga(valor, critico, atencao) {
  if (valor == null) return 'neutro'
  if (valor < critico) return 'critico'
  if (valor < atencao) return 'atencao'
  return 'ok'
}
