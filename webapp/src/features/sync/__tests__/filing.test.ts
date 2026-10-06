// Tests for the filing rules — primary cookbook selection and file naming.

import { describe, it, expect } from 'vitest'
import { primaryCookbookId, recipeFileName, UNSORTED_FOLDER } from '../filing'
import { createCookbook } from '../../../types/cookbook'

function cookbook(id: string, createdAt: string, recipeIds: string[]) {
  return { ...createCookbook(`Book ${id}`, 'device-a', null, recipeIds), id, createdAt }
}

describe('primaryCookbookId', () => {
  it('returns the cookbook containing the recipe', () => {
    const books = [cookbook('a', '2026-01-01T00:00:00Z', ['r1'])]
    expect(primaryCookbookId('r1', books)).toBe('a')
  })

  it('returns null when the recipe is not in any cookbook', () => {
    const books = [cookbook('a', '2026-01-01T00:00:00Z', ['r1'])]
    expect(primaryCookbookId('r2', books)).toBeNull()
  })

  it('picks the oldest cookbook when several contain the recipe', () => {
    const books = [
      cookbook('new', '2026-03-01T00:00:00Z', ['r1']),
      cookbook('old', '2026-01-01T00:00:00Z', ['r1']),
      cookbook('mid', '2026-02-01T00:00:00Z', ['r1']),
    ]
    expect(primaryCookbookId('r1', books)).toBe('old')
  })
})

describe('recipeFileName', () => {
  it('uses the recipe title', () => {
    expect(recipeFileName('Sunday Pot Roast')).toBe('Sunday Pot Roast.json')
  })

  it('strips path separators so the name is Drive-safe', () => {
    expect(recipeFileName('Best cake ever/in the world')).toBe('Best cake ever-in the world.json')
  })

  it('falls back when the title is blank', () => {
    expect(recipeFileName('   ')).toBe('Untitled recipe.json')
  })
})

describe('UNSORTED_FOLDER', () => {
  it('is a stable folder name', () => {
    expect(UNSORTED_FOLDER).toBe('Unsorted')
  })
})
