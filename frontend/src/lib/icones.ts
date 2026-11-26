import { Car, Fuel, Receipt, Wrench, type LucideIcon } from 'lucide-react'
import type { TipoDespesa } from '@/lib/rotulos'

/** Ícone de cada tipo de despesa. */
export const ICONES_DESPESA: Record<TipoDespesa, LucideIcon> = {
  GASOLINA: Fuel,
  PEDAGIO: Car,
  PNEU: Car,
  MECANICA: Wrench,
  OUTROS: Receipt,
}
