// Recipe — core domain model. Ported from Recipe.kt; field names match the
// Kotlin app so Drive-synced files stay backwards-compatible.

import type { Ingredient } from './ingredient'
import type { VersionVector } from './versionVector'
import { createVersionVector, incrementVersionVector } from './versionVector'

export interface Recipe {
  id: string
  title: string
  description: string | null
  category: string
  ingredients: Ingredient[]
  instructions: string[]
  servingSize: number | null
  prepTime: number | null // minutes
  cookTime: number | null // minutes
  rating: number | null
  isFavorite: boolean
  imageUrl: string | null
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
  instructions?: string[]
  servingSize?: number | null
  prepTime?: number | null
  cookTime?: number | null
  rating?: number | null
  imageUrl?: string | null
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
    ingredients: input.ingredients ?? [],
    instructions: input.instructions ?? [],
    servingSize: input.servingSize ?? null,
    prepTime: input.prepTime ?? null,
    cookTime: input.cookTime ?? null,
    rating: input.rating ?? null,
    isFavorite: input.isFavorite ?? false,
    imageUrl: input.imageUrl ?? null,
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

export function totalRecipeTime(r: Recipe): number | null {
  if (r.prepTime == null && r.cookTime == null) return null
  if (r.prepTime == null) return r.cookTime
  if (r.cookTime == null) return r.prepTime
  return r.prepTime + r.cookTime
}
