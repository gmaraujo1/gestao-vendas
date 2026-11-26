import { useQuery } from '@tanstack/react-query'
import { ArrowDownLeft, ArrowUpRight, PackageX, Repeat } from 'lucide-react'
import { Link, useParams } from 'react-router'
import { Etiqueta } from '@/components/Etiqueta'
import { EstadosLista } from '@/components/lista/EstadosLista'
import { Cartao, Pagina, Secao } from '@/components/pagina/Pagina'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio } from '@/lib/api/client'
import { formatarDataHora, formatarQuantidade } from '@/lib/formatadores'
import { MOTIVOS_PERDA } from '@/lib/rotulos'

export function DetalheTroca() {
  const id = Number(useParams().id)
  const negocioId = useNegocioId()
  const troca = useQuery({
    queryKey: ['trocas', negocioId, 'detalhe', id],
    queryFn: () => dados(api.GET('/api/troca/{id}', { params: { path: { id }, header: doNegocio(negocioId) } })),
  })
  const t = troca.data
  const movs = t?.movimentacoes ?? []
  const devolvidos = movs.filter((m) => m.tipo === 'TROCA_ENTRADA')
  const perdas = movs.filter((m) => m.tipo === 'PERDA')
  const entregues = movs.filter((m) => m.tipo === 'TROCA_SAIDA')

  return (
    <Pagina voltar="/trocas" titulo={`Troca #${id}`} subtitulo={t ? formatarDataHora(t.dataHora) : undefined}>
      <EstadosLista carregando={troca.isPending} erro={troca.error} vazio={false} tentarDeNovo={() => void troca.refetch()} iconeVazio={Repeat} tituloVazio="">
        {t && (
          <>
            <Cartao className="p-5">
              <p className="text-sm text-muted-foreground">Cliente</p>
              <Link to={`/clientes/${t.clienteId}`} className="text-lg font-semibold underline-offset-4 hover:underline">
                {t.clienteNome}
              </Link>
              {t.observacao && <p className="mt-3 rounded-xl bg-muted/70 p-3 text-sm">{t.observacao}</p>}
            </Cartao>
            <Secao titulo={<span className="flex items-center gap-1.5"><ArrowDownLeft className="size-4" aria-hidden="true" /> Devolveu</span>}>
              <Cartao className="divide-y overflow-hidden">
                {devolvidos.map((m) => {
                  const virouPerda = perdas.find((p) => p.produtoId === m.produtoId)
                  return (
                    <div key={m.id} className="flex items-center gap-3 px-4 py-3.5">
                      <p className="min-w-0 flex-1 font-medium">{m.produtoDescricao}</p>
                      {virouPerda ? (
                        <Etiqueta tom="perigo">
                          <PackageX className="mr-1 size-3.5" aria-hidden="true" />
                          Perda · {virouPerda.motivoPerda ? MOTIVOS_PERDA[virouPerda.motivoPerda] : ''}
                        </Etiqueta>
                      ) : (
                        <Etiqueta tom="sucesso">Voltou ao estoque</Etiqueta>
                      )}
                      <span className="numeros font-semibold">{formatarQuantidade(m.quantidade)}</span>
                    </div>
                  )
                })}
              </Cartao>
            </Secao>
            <Secao titulo={<span className="flex items-center gap-1.5"><ArrowUpRight className="size-4" aria-hidden="true" /> Levou</span>}>
              {entregues.length === 0 ? (
                <p className="px-1 text-sm text-muted-foreground">Nada (só devolução).</p>
              ) : (
                <Cartao className="divide-y overflow-hidden">
                  {entregues.map((m) => (
                    <div key={m.id} className="flex items-center gap-3 px-4 py-3.5">
                      <p className="min-w-0 flex-1 font-medium">{m.produtoDescricao}</p>
                      <span className="numeros font-semibold">{formatarQuantidade(Math.abs(m.quantidade ?? 0))}</span>
                    </div>
                  ))}
                </Cartao>
              )}
            </Secao>
          </>
        )}
      </EstadosLista>
    </Pagina>
  )
}
