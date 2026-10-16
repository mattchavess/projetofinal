// Textos exibidos na interface para cada valor de enum da API.

export const STATUS_TORRE = {
  LIGADA: 'Ligada',
  DESLIGADA: 'Desligada',
  EM_MANUTENCAO: 'Em manutenção',
  ALERTA: 'Alerta',
  INDISPONIVEL: 'Indisponível'
}
export const TOM_STATUS = {
  LIGADA: 'ok',
  DESLIGADA: 'neutro',
  EM_MANUTENCAO: 'info',
  ALERTA: 'atencao',
  INDISPONIVEL: 'critico'
}
export const FONTE = { SOLAR: 'Solar', BATERIA: 'Bateria', GERADOR: 'Gerador' }
export const MODO = {
  ECONOMICO: 'Econômico',
  NORMAL: 'Normal',
  INTENSIDADE_MAXIMA: 'Intensidade máxima',
  CERRACAO: 'Cerração'
}
export const NIVEL_ALERTA = { INFORMATIVO: 'Informativo', ATENCAO: 'Atenção', CRITICO: 'Crítico' }
export const TOM_NIVEL = { INFORMATIVO: 'info', ATENCAO: 'atencao', CRITICO: 'critico' }
export const TIPO_ALERTA = {
  BATERIA_BAIXA: 'Bateria baixa',
  COMBUSTIVEL_BAIXO: 'Combustível baixo',
  FALHA_TECNICA: 'Falha técnica',
  SEM_COMUNICACAO: 'Sem comunicação',
  MANUTENCAO_PENDENTE: 'Manutenção pendente',
  CONSUMO_ELEVADO: 'Consumo elevado'
}
export const TIPO_MANUTENCAO = { PREVENTIVA: 'Preventiva', CORRETIVA: 'Corretiva' }
export const STATUS_MANUTENCAO = {
  ABERTA: 'Aberta',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada'
}
export const TOM_MANUTENCAO = { ABERTA: 'atencao', EM_ANDAMENTO: 'info', CONCLUIDA: 'ok', CANCELADA: 'neutro' }
export const PERFIL = { ADMIN: 'Administrador', OPERADOR: 'Operador' }

export const COMANDOS = {
  LIGAR: 'Ligar',
  DESLIGAR: 'Desligar',
  ECONOMICO: 'Modo econômico',
  NORMAL: 'Modo normal',
  INTENSIDADE_MAXIMA: 'Intensidade máxima',
  CERRACAO: 'Modo cerração'
}

const FRASE_MODO = {
  ECONOMICO: 'ativação do modo econômico',
  NORMAL: 'ativação do modo normal',
  INTENSIDADE_MAXIMA: 'ativação do modo de intensidade máxima',
  CERRACAO: 'ativação do modo cerração'
}

/** Pergunta de confirmação. Para uma torre: { nome }. Para várias: { qtd }. */
export function perguntaComando(comando, { nome, qtd }) {
  const unica = !qtd || qtd === 1
  if (comando === 'LIGAR' || comando === 'DESLIGAR') {
    const verbo = comando === 'LIGAR' ? 'ligar' : 'desligar'
    return unica ? `Confirmar ${verbo} a ${nome}?` : `Confirmar ${verbo} as ${qtd} torres selecionadas?`
  }
  return unica
    ? `Confirmar ${FRASE_MODO[comando]} na ${nome}?`
    : `Confirmar ${FRASE_MODO[comando]} em ${qtd} torres?`
}
