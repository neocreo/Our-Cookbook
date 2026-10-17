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

// IngredientSection — a named group of ingredients within a recipe, e.g.
// "Biscuits" and "Gravy" in a biscuits-and-gravy recipe. A recipe with a
// single unnamed section is the common case.

export interface IngredientSection {
  id: string
  name: string
  ingredients: Ingredient[]
}

export function createIngredientSection(
  name: string,
  ingredients: Ingredient[] = [],
): IngredientSection {
  return { id: crypto.randomUUID(), name, ingredients }
}

/** All sections' ingredients as one flat list — kept on Recipe.ingredients
 *  so older app versions and Drive-synced files keep working. */
export function flattenIngredientSections(sections: IngredientSection[]): Ingredient[] {
  return sections.flatMap((s) => s.ingredients)
}
