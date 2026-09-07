// Export and import recipes in Markdown and JSON formats.
// Markdown is human-readable; JSON is lossless (round-trips all fields
// including versionVector for Drive compatibility).

import type { Recipe } from '../types/recipe'
import { createRecipe } from '../types/recipe'
import { createIngredient } from '../types/ingredient'

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
  for (const ing of r.ingredients) {
    const amount = [ing.amount, ing.unit].filter(Boolean).join(' ')
    lines.push(`- ${amount ? amount + ' ' : ''}${ing.name}${ing.notes ? ` (${ing.notes})` : ''}`)
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

// --- Import ---

export function parseRecipesJSON(json: string): Recipe[] {
  const data = JSON.parse(json) as Recipe[]
  if (!Array.isArray(data)) throw new Error('Expected a JSON array of recipes')
  return data.map(normalizeRecipe)
}

function normalizeRecipe(r: Recipe): Recipe {
  // Ensure required fields exist; keep id/versionVector if present for
  // Drive compatibility, otherwise generate fresh.
  return {
    ...createRecipe({ title: r.title || 'Untitled', category: r.category || 'Uncategorized' }),
    ...r,
    ingredients: (r.ingredients ?? []).map((ing) => ({
      ...createIngredient(ing.name || ''),
      ...ing,
    })),
  }
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
