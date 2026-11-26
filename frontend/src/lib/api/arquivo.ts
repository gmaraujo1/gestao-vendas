import { paraApiError } from '@/lib/api/erros'
import { negocioAtualId, tokenAtual } from '@/lib/sessao'

/**
 * Baixa um arquivo da API (PDF) com o token e o negócio. No celular abre o "compartilhar" do
 * sistema (salvar, mandar no WhatsApp...); no PC, baixa direto.
 */
export async function baixarArquivo(caminho: string, query: Record<string, string | undefined>) {
  const params = new URLSearchParams(Object.entries(query).filter((e): e is [string, string] => !!e[1]))
  const resposta = await fetch(`${import.meta.env.VITE_API_URL ?? ''}${caminho}?${params}`, {
    headers: {
      Authorization: `Bearer ${tokenAtual() ?? ''}`,
      'X-Negocio-Id': String(negocioAtualId() ?? ''),
      Accept: 'application/pdf, application/json',
    },
  })
  if (!resposta.ok) {
    const corpo = await resposta.json().catch(() => null)
    throw paraApiError(resposta.status, corpo)
  }
  const nome = /filename="?([^";]+)"?/.exec(resposta.headers.get('Content-Disposition') ?? '')?.[1] ?? 'relatorio.pdf'
  const blob = await resposta.blob()
  const arquivo = new File([blob], nome, { type: blob.type || 'application/pdf' })

  const toque = matchMedia('(pointer: coarse)').matches
  if (toque && navigator.canShare?.({ files: [arquivo] })) {
    try {
      await navigator.share({ files: [arquivo], title: nome })
      return
    } catch (e) {
      // cancelado pelo usuário: não é erro; outros casos (ex.: o gesto expirou) caem no download
      if (e instanceof DOMException && e.name === 'AbortError') return
    }
  }
  const url = URL.createObjectURL(arquivo)
  const a = document.createElement('a')
  a.href = url
  a.download = nome
  a.rel = 'noopener'
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}
