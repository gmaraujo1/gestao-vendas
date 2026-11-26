/** Erro de uma chamada à API. A mensagem vem do backend ({ message }) quando existe. */
export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const MENSAGENS_PADRAO: Record<number, string> = {
  400: 'Dados inválidos. Confira os campos.',
  401: 'Sessão expirada. Entre novamente.',
  403: 'Você não tem permissão para isso.',
  404: 'Registro não encontrado.',
  409: 'Já existe um registro com esses dados.',
  422: 'Operação não permitida.',
}

/** Converte a resposta de erro do openapi-fetch em ApiError com mensagem amigável. */
export function paraApiError(status: number, corpo: unknown): ApiError {
  const mensagem =
    corpo && typeof corpo === 'object' && 'message' in corpo && typeof corpo.message === 'string'
      ? corpo.message
      : (MENSAGENS_PADRAO[status] ?? 'Erro inesperado. Tente de novo.')
  return new ApiError(status, mensagem)
}

/** Texto para mostrar ao usuário (toast, estado de erro) a partir de qualquer erro. */
export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof ApiError) return erro.message
  if (erro instanceof TypeError) return 'Sem conexão com o servidor. Confira a internet e tente de novo.'
  return 'Algo deu errado. Tente de novo.'
}
