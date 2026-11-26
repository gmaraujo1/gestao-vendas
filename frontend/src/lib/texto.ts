/** "Gustavo Muller de Araujo" -> "GA" (avatar). */
export function iniciais(nome?: string) {
  const partes = (nome ?? '').trim().split(/\s+/).filter(Boolean)
  const primeira = partes[0]?.[0] ?? '?'
  const ultima = partes.length > 1 ? (partes[partes.length - 1]?.[0] ?? '') : ''
  return (primeira + ultima).toUpperCase()
}

/** Só os dígitos do telefone, com DDI do Brasil para o WhatsApp (wa.me/55...). */
export function linkWhatsApp(telefone?: string | null) {
  const digitos = (telefone ?? '').replace(/\D/g, '')
  if (digitos.length < 10) return null
  return `https://wa.me/${digitos.startsWith('55') && digitos.length > 11 ? digitos : `55${digitos}`}`
}

/** "11987654321" -> "(11) 98765-4321" (deixa como veio se não reconhecer). */
export function formatarTelefone(telefone?: string | null) {
  const d = (telefone ?? '').replace(/\D/g, '')
  if (d.length === 11) return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`
  if (d.length === 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`
  return telefone ?? ''
}
