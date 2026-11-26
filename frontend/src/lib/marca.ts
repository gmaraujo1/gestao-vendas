/**
 * Identidade visual de cada negócio. O slug vai em data-negocio no <html> e escolhe as cores
 * definidas no index.css; theme-color pinta a barra do navegador no celular.
 */
export type Marca = {
  slug: 'laticinio' | 'pao-nino'
  nomeCurto: string
  descricao: string
  corBarra: { claro: string; escuro: string }
}

const MARCAS: Record<string, Marca> = {
  LATICINIO: {
    slug: 'laticinio',
    nomeCurto: 'Laticínio',
    descricao: 'Leites, queijos e derivados',
    corBarra: { claro: '#f5f8fc', escuro: '#0c1430' },
  },
  PAO_NINO: {
    slug: 'pao-nino',
    nomeCurto: 'Pão Nino',
    descricao: 'Pães e padaria',
    corBarra: { claro: '#fbf8f2', escuro: '#1b140e' },
  },
}

const NEUTRA = { claro: '#f7f8fa', escuro: '#0f121a' }

/** Pelo código do negócio vindo da API (LATICINIO, PAO_NINO). */
export function marcaDe(codigo?: string | null): Marca | null {
  return (codigo && MARCAS[codigo]) || null
}

export function marcaPorSlug(slug?: string | null): Marca | null {
  return Object.values(MARCAS).find((m) => m.slug === slug) ?? null
}

/** Pinta a página com a cor do negócio (null = identidade neutra do login). */
export function aplicarMarca(slug: Marca['slug'] | null) {
  const html = document.documentElement
  if (slug) html.dataset.negocio = slug
  else delete html.dataset.negocio
  atualizarCorBarra()
}

/** theme-color acompanha o tema e o negócio (barra de status do celular). */
export function atualizarCorBarra() {
  const html = document.documentElement
  const cores = marcaPorSlug(html.dataset.negocio)?.corBarra ?? NEUTRA
  const cor = html.classList.contains('dark') ? cores.escuro : cores.claro
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', cor)
}
