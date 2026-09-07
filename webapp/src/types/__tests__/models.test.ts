import { describe, it, expect } from 'vitest'
import { createRecipe, withRecipeUpdate, totalRecipeTime } from '../../types/recipe'
import { createIngredient } from '../../types/ingredient'
import { createVersionVector, incrementVersionVector, isVersionNewer } from '../../types/versionVector'
import { mergeSyncVersionVectors, createSyncVersionVector, withIncrementedSyncVersion } from '../../types/versionVector'

describe('Recipe', () => {
  it('creates a recipe with defaults', () => {
    const r = createRecipe({ title: 'Test', category: 'Dinner' }, 'dev1')
    expect(r.title).toBe('Test')
    expect(r.category).toBe('Dinner')
    expect(r.id).toMatch(/^[0-9a-f-]+$/)
    expect(r.isFavorite).toBe(false)
    expect(r.ingredients).toHaveLength(0)
  })

  it('increments version vector on update', () => {
    const r = createRecipe({ title: 'Test', category: 'Dinner' }, 'dev1')
    const updated = withRecipeUpdate(r, 'dev1')
    expect(updated.versionVector.counter).toBe(1)
    expect(updated.versionVector.deviceId).toBe('dev1')
  })

  it('calculates total time', () => {
    expect(totalRecipeTime(createRecipe({ title: 'T', category: 'C', prepTime: 10, cookTime: 20 }))).toBe(30)
    expect(totalRecipeTime(createRecipe({ title: 'T', category: 'C', prepTime: 10 }))).toBe(10)
    expect(totalRecipeTime(createRecipe({ title: 'T', category: 'C' }))).toBeNull()
  })
})

describe('VersionVector', () => {
  it('is newer when counter is higher', () => {
    const a = createVersionVector('dev1')
    const b = incrementVersionVector(a, 'dev1')
    expect(isVersionNewer(b, a)).toBe(true)
    expect(isVersionNewer(a, b)).toBe(false)
  })

  it('breaks ties with timestamp', () => {
    const a = { deviceId: 'd', counter: 1, timestamp: '2026-01-01T00:00:00.000Z' }
    const b = { deviceId: 'd', counter: 1, timestamp: '2026-01-02T00:00:00.000Z' }
    expect(isVersionNewer(b, a)).toBe(true)
  })
})

describe('SyncVersionVector', () => {
  it('merges two vectors keeping the newest per device', () => {
    const a = createSyncVersionVector()
    const b = withIncrementedSyncVersion(a, 'dev1')
    const c = withIncrementedSyncVersion(b, 'dev2')
    const merged = mergeSyncVersionVectors(b, c)
    expect(merged.versions.get('dev1')?.counter).toBe(1)
    expect(merged.versions.get('dev2')?.counter).toBe(1)
  })
})

describe('Ingredient', () => {
  it('creates an ingredient with order', () => {
    const i = createIngredient('flour', '2', 'cups', null, 0)
    expect(i.name).toBe('flour')
    expect(i.amount).toBe('2')
    expect(i.order).toBe(0)
  })
})
