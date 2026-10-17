// Recipe — core domain model. Ported from Recipe.kt; field names match the
// Kotlin app so Drive-synced files stay backwards-compatible.

import type { Ingredient, IngredientSection } from './ingredient'
import { flattenIngredientSections } from './ingredient'
import type { VersionVector } from './versionVector'
import { createVersionVector, incrementVersionVector } from './versionVector'

export interface Recipe {
  id: string
  title: string
  description: string | null
  category: string
  ingredients: Ingredient[]
  // Named ingredient lists (e.g. "Biscuits" and "Gravy"). Optional: recipes
  // with a single list only use `ingredients`. When present, `ingredients`
  // mirrors the flattened sections for backwards compatibility.
  ingredientSections?: IngredientSection[]
  instructions: string[]
  servingSize: number | null
  prepTime: number | null // minutes
  cookTime: number | null // minutes
  rating: number | null
  isFavorite: boolean
  imageUrls: string[]
  notes: string | null
  source: string | null
  tags: string[]
  createdAt: string // ISO
  updatedAt: string // ISO
  versionVector: VersionVector
  checksum: string
  deviceId: string
}

export interface RecipeInput {
  title: string
  description?: string | null
  category: string
  ingredients?: Ingredient[]
  ingredientSections?: IngredientSection[]
  instructions?: string[]
  servingSize?: number | null
  prepTime?: number | null
  cookTime?: number | null
  rating?: number | null
  imageUrls?: string[]
  notes?: string | null
  source?: string | null
  tags?: string[]
  isFavorite?: boolean
}

export function createRecipe(input: RecipeInput, deviceId = ''): Recipe {
  const now = new Date().toISOString()
  return {
    id: crypto.randomUUID(),
    title: input.title,
    description: input.description ?? null,
    category: input.category,
    // Sections are the structured source; the flat list mirrors them so
    // older versions (and Drive-synced files) keep showing all ingredients.
    ingredients: input.ingredientSections
      ? flattenIngredientSections(input.ingredientSections)
      : (input.ingredients ?? []),
    ...(input.ingredientSections ? { ingredientSections: input.ingredientSections } : {}),
    instructions: input.instructions ?? [],
    servingSize: input.servingSize ?? null,
    prepTime: input.prepTime ?? null,
    cookTime: input.cookTime ?? null,
    rating: input.rating ?? null,
    isFavorite: input.isFavorite ?? false,
    imageUrls: input.imageUrls ?? [],
    notes: input.notes ?? null,
    source: input.source ?? null,
    tags: input.tags ?? [],
    createdAt: now,
    updatedAt: now,
    versionVector: createVersionVector(deviceId),
    checksum: '',
    deviceId,
  }
}

export function withRecipeUpdate(recipe: Recipe, deviceId: string): Recipe {
  return {
    ...recipe,
    updatedAt: new Date().toISOString(),
    versionVector: incrementVersionVector(recipe.versionVector, deviceId),
  }
}

/** The recipe's ingredient lists. Recipes saved before sections existed (or
 *  with a single list) are presented as one unnamed section. */
export function getIngredientSections(recipe: Recipe): IngredientSection[] {
  if (recipe.ingredientSections && recipe.ingredientSections.length > 0) {
    return recipe.ingredientSections
  }
  return [{ id: 'default', name: '', ingredients: recipe.ingredients ?? [] }]
}

export function totalRecipeTime(r: Recipe): number | null {
  if (r.prepTime == null && r.cookTime == null) return null
  if (r.prepTime == null) return r.cookTime
  if (r.cookTime == null) return r.prepTime
  return r.prepTime + r.cookTime
}

// Backwards compatibility helpers for migration from imageUrl to imageUrls
export function migrateRecipeImage(recipe: Recipe & { imageUrl?: string | null }): Recipe {
  return {
    ...recipe,
    imageUrls: recipe.imageUrl ? [recipe.imageUrl] : (recipe.imageUrls ?? []),
  }
}

export function getFirstImageUrl(recipe: Recipe): string | null {
  const urls = recipe.imageUrls ?? []
  return urls.length > 0 ? urls[0] : null
}
