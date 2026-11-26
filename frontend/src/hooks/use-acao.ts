import { useMutation, useQueryClient, type QueryKey } from '@tanstack/react-query'
import { toast } from 'sonner'
import { mensagemDeErro } from '@/lib/api/erros'

/**
 * Mutação padrão das telas: avisa o sucesso (toast), mostra o erro da API e atualiza as
 * listas afetadas (invalida as chaves informadas).
 */
export function useAcao<TEntrada, TSaida>({
  executar,
  sucesso,
  invalidar = [],
  aoConcluir,
}: {
  executar: (entrada: TEntrada) => Promise<TSaida>
  sucesso?: string | ((saida: TSaida) => string)
  invalidar?: QueryKey[]
  aoConcluir?: (saida: TSaida) => void
}) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: executar,
    onSuccess: (saida) => {
      // primeiro o que depende do sucesso (ex.: limpar o rascunho da venda já gravada), sem
      // esperar a rede: as listas abertas se atualizam sozinhas depois
      aoConcluir?.(saida)
      if (sucesso) toast.success(typeof sucesso === 'function' ? sucesso(saida) : sucesso)
      for (const queryKey of invalidar) void queryClient.invalidateQueries({ queryKey })
    },
    onError: (erro) => toast.error(mensagemDeErro(erro)),
  })
}
