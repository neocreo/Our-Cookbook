// Ingredient — a single ingredient in a recipe. Ported from Ingredient.kt.

export interface Ingredient {
  id: string
  name: string
  amount?: string | null
  unit?: string | null
  notes?: string | null
  order: number
}

export function createIngredient(
  name: string,
  amount: string | null = null,
  unit: string | null = null,
  notes: string | null = null,
  order = 0,
): Ingredient {
  return { id: crypto.randomUUID(), name, amount, unit, notes, order }
}

export function ingredientDisplay(ing: Ingredient): string {
  const parts: string[] = []
  if (ing.amount) {
    parts.push(ing.unit ? `${ing.amount} ${ing.unit}` : ing.amount)
  }
  parts.push(ing.name)
  const base = parts.join(' ')
  return ing.notes ? `${base} (${ing.notes})` : base
}
