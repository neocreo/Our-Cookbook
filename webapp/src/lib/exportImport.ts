// Export and import recipes in Markdown and JSON formats.
// Markdown is human-readable; JSON is lossless (round-trips all fields
// including versionVector for Drive compatibility).

import type { Recipe } from '../types/recipe'
import { createRecipe, getIngredientSections } from '../types/recipe'
import { createIngredient, createIngredientSection, flattenIngredientSections, type Ingredient, type IngredientSection } from '../types/ingredient'

// --- Markdown ---

export function recipesToMarkdown(recipes: Recipe[]): string {
  return recipes.map(recipeToMarkdown).join('\n\n---\n\n')
}

function recipeToMarkdown(r: Recipe): string {
  const lines: string[] = [`# ${r.title}`]
  if (r.category) lines.push(`*Category:* ${r.category}`)
  if (r.description) lines.push('', r.description)
  const meta: string[] = []
  if (r.servingSize != null) meta.push(`Servings: ${r.servingSize}`)
  if (r.prepTime != null) meta.push(`Prep: ${r.prepTime} min`)
  if (r.cookTime != null) meta.push(`Cook: ${r.cookTime} min`)
  if (r.source) meta.push(`Source: ${r.source}`)
  if (meta.length) lines.push('', meta.join(' · '))
  if (r.tags.length) lines.push(`*Tags:* ${r.tags.join(', ')}`)
  lines.push('', '## Ingredients')
  for (const section of getIngredientSections(r)) {
    if (section.name) lines.push('', `### ${section.name}`)
    for (const ing of section.ingredients) {
      const amount = [ing.amount, ing.unit].filter(Boolean).join(' ')
      lines.push(`- ${amount ? amount + ' ' : ''}${ing.name}${ing.notes ? ` (${ing.notes})` : ''}`)
    }
  }
  lines.push('', '## Instructions')
  r.instructions.forEach((step, i) => lines.push(`${i + 1}. ${step}`))
  if (r.notes) lines.push('', '## Notes', r.notes)
  return lines.join('\n')
}

// --- JSON ---

export function recipesToJSON(recipes: Recipe[]): string {
  return JSON.stringify(recipes, null, 2)
}

// --- section-header detection ---
//
// Older cookbooks (and the Kotlin app) stored multi-part recipes as a flat
// list where a bare label row — "Biscuits", "Gravy", "Marinade", "For the
// filling" — marks the start of each part. Detect those rows at import and
// turn them into real named ingredient lists.

/** Does this row look like a bare section label rather than an ingredient? */
function isHeaderShaped(ing: Ingredient): boolean {
  const name = (ing.name ?? '').trim()
  if (!name) return false
  if (ing.amount || ing.unit || ing.notes) return false
  if (name.includes(',')) return false
  if (/^[(-*]/.test(name)) return false
  if (/to taste/i.test(name)) return false
  if (/^(or|and)$/i.test(name)) return false
  const words = name.split(/\s+/)
  if (words.length > 4 && !(name.endsWith(')') && words.length <= 8)) return false
  return true
}

/** Split a flat ingredient list into named sections at bare label rows.
 *  Returns null when nothing clearly looks like sections — fewer than two
 *  label rows, or a label with no ingredients after it. Conservative by
 *  design: a missed label stays a harmless row, while a false one would
 *  hide real ingredients behind a wrong heading. */
export function detectIngredientSections(ingredients: Ingredient[]): IngredientSection[] | null {
  const shaped = ingredients.map(isHeaderShaped)
  // A label introduces what follows: real ingredients, or a run of further
  // labels that eventually reaches them. Filled backwards so a chain of bare
  // rows ("Fresh Rosemary", "Fresh Thyme", …) only counts when it truly
  // leads to ingredient rows.
  const isLabel: boolean[] = new Array(ingredients.length).fill(false)
  for (let i = ingredients.length - 1; i >= 0; i--) {
    if (!shaped[i]) continue
    const next = ingredients[i + 1]
    if (!next) continue
    isLabel[i] = Boolean(next.amount || next.unit) || isLabel[i + 1]
  }
  if (isLabel.filter(Boolean).length < 2) return null

  const lead: Ingredient[] = []
  const found: { name: string; items: Ingredient[] }[] = []
  ingredients.forEach((ing, i) => {
    if (isLabel[i]) {
      found.push({ name: ing.name.trim(), items: [] })
    } else if (found.length > 0) {
      found[found.length - 1].items.push(ing)
    } else {
      lead.push(ing)
    }
  })
  const sections = found
    .filter((s) => s.items.length > 0)
    .map((s) => createIngredientSection(s.name, s.items))
  if (lead.length > 0) sections.unshift(createIngredientSection('', lead))
  // A single unnamed section is just the original list.
  if (sections.length === 0 || (sections.length === 1 && sections[0].name === '')) return null
  return sections
}

// --- Import ---

export function parseRecipesJSON(json: string): Recipe[] {
  const data = JSON.parse(json) as Recipe[]
  if (!Array.isArray(data)) throw new Error('Expected a JSON array of recipes')
  return data.map(normalizeRecipe)
}

function normalizeRecipe(r: Recipe & { imageUrl?: string | null }): Recipe {
  // Ensure required fields exist; keep id/versionVector if present for
  // Drive compatibility, otherwise generate fresh.
  // Migrate imageUrl to imageUrls if present
  const normalizeIngredients = (list: Ingredient[]) =>
    list.map((ing) => ({ ...createIngredient(ing.name || ''), ...ing }))
  let sections = (r.ingredientSections ?? []).map((s) => ({
    id: s.id || crypto.randomUUID(),
    name: s.name ?? '',
    ingredients: normalizeIngredients(s.ingredients ?? []),
  }))
  // No stored sections? Look for bare label rows ("Biscuits", "Gravy", …)
  // in the flat list and turn them into named sections.
  if (sections.length === 0) {
    const detected = detectIngredientSections(normalizeIngredients(r.ingredients ?? []))
    if (detected) sections = detected
  }
  const base = {
    ...createRecipe({ title: r.title || 'Untitled', category: r.category || 'Uncategorized' }),
    ...r,
    // The flat list mirrors the sections — label rows are not ingredients.
    ingredients: sections.length > 0
      ? flattenIngredientSections(sections)
      : normalizeIngredients(r.ingredients ?? []),
    ...(sections.length > 0 ? { ingredientSections: sections } : {}),
  }

  // Handle backwards compatibility for imageUrl
  if ('imageUrl' in r && r.imageUrl && !r.imageUrls) {
    return { ...base, imageUrls: [r.imageUrl] }
  }

  return base
}

// --- file helpers ---

export function downloadFile(filename: string, content: string, mime: string): void {
  const blob = new Blob([content], { type: mime })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

export function readFileText(file: File): Promise<string> {
  return file.text()
}
