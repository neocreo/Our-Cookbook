// Integration tests for the recipe repository. Exercises the full RxDB
// persistence path: init → save → read → update → delete, and verifies
// that data survives a simulated app reload (module re-import).
//
// fake-indexeddb (set up in test/setup.ts) provides IndexedDB in jsdom,
// so RxDB's Dexie storage works exactly as in the browser.

import { describe, it, expect, beforeEach, vi } from 'vitest'

const DEVICE_ID = 'test-device-1'

// Each test gets a fresh module graph so the singleton store/db start clean.
// IndexedDB data persists across resets within the same test file, which is
// what lets us test the reload scenario.
async function freshImport() {
  vi.resetModules()
  const store = await import('../../../lib/store')
  const repo = await import('../repository')
  return { store, repo }
}

async function initRepo() {
  const { store, repo } = await freshImport()
  await repo.initRepository(DEVICE_ID)
  return { store, repo }
}

describe('recipe repository (RxDB persistence)', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('create + read', () => {
    it('saves a new recipe and reads it back', async () => {
      const { repo } = await initRepo()
      const created = await repo.saveRecipe(
        { title: 'Pancakes', category: 'Breakfast', ingredients: [], instructions: ['Mix', 'Fry'] },
        DEVICE_ID,
      )
      expect(created.id).toBeTruthy()
      expect(created.title).toBe('Pancakes')

      const fetched = await repo.getRecipe(created.id)
      expect(fetched).not.toBeNull()
      expect(fetched!.title).toBe('Pancakes')
      expect(fetched!.category).toBe('Breakfast')
      expect(fetched!.instructions).toEqual(['Mix', 'Fry'])
    })

    it('creates a default cookbook when saving a recipe and no cookbooks exist', async () => {
      const { repo } = await initRepo()
      const cookbookRepo = await import('../../cookbook/repository')

      // Remove all seeded cookbooks so the auto-cookbook path triggers
      const seeded = await cookbookRepo.listCookbooks()
      for (const cb of seeded) {
        await cookbookRepo.deleteCookbook(cb.id, DEVICE_ID)
      }
      expect(await cookbookRepo.listCookbooks()).toHaveLength(0)

      const created = await repo.saveRecipe({ title: 'First Recipe', category: 'Dinner' }, DEVICE_ID)

      const after = await cookbookRepo.listCookbooks()
      expect(after).toHaveLength(1)
      expect(after[0].name).toBe('Mina recept')
      expect(after[0].recipeIds).toContain(created.id)
    })

    it('saves a recipe with no images and reads back imageUrls as an empty array', async () => {
      const { repo } = await initRepo()
      const created = await repo.saveRecipe(
        { title: 'No Images', category: 'Misc' },
        DEVICE_ID,
      )
      const fetched = await repo.getRecipe(created.id)
      expect(fetched!.imageUrls).toEqual([])
    })
  })

  describe('update', () => {
    it('updates an existing recipe and the change persists', async () => {
      const { repo } = await initRepo()
      const created = await repo.saveRecipe({ title: 'Old Title', category: 'Dinner' }, DEVICE_ID)

      const updated = await repo.saveRecipe(
        { ...created, title: 'New Title' },
        DEVICE_ID,
      )
      expect(updated.title).toBe('New Title')
      expect(updated.versionVector.counter).toBe(1)

      const fetched = await repo.getRecipe(created.id)
      expect(fetched!.title).toBe('New Title')
    })

    it('toggleFavorite flips isFavorite and persists', async () => {
      const { repo } = await initRepo()
      const created = await repo.saveRecipe({ title: 'Fav Test', category: 'Dinner' }, DEVICE_ID)
      expect(created.isFavorite).toBe(false)

      await repo.toggleFavorite(created.id, DEVICE_ID)
      const fetched = await repo.getRecipe(created.id)
      expect(fetched!.isFavorite).toBe(true)

      await repo.toggleFavorite(created.id, DEVICE_ID)
      const fetched2 = await repo.getRecipe(created.id)
      expect(fetched2!.isFavorite).toBe(false)
    })

    it('preserves imageUrls when updating a recipe that has images', async () => {
      const { repo } = await initRepo()
      const created = await repo.saveRecipe(
        { title: 'With Image', category: 'Dinner', imageUrls: ['data:image/png;base64,abc'] },
        DEVICE_ID,
      )
      expect(created.imageUrls).toEqual(['data:image/png;base64,abc'])

      // Update the title only — images should be preserved
      const updated = await repo.saveRecipe({ ...created, title: 'Renamed' }, DEVICE_ID)
      expect(updated.imageUrls).toEqual(['data:image/png;base64,abc'])

      const fetched = await repo.getRecipe(created.id)
      expect(fetched!.imageUrls).toEqual(['data:image/png;base64,abc'])
    })

    it('clears images when updating with an empty imageUrls array (not undefined)', async () => {
      const { repo } = await initRepo()
      const created = await repo.saveRecipe(
        { title: 'Had Image', category: 'Dinner', imageUrls: ['data:image/png;base64,abc'] },
        DEVICE_ID,
      )

      // Simulate the edit form clearing all images — imageUrls should be [], not undefined
      const updated = await repo.saveRecipe({ ...created, imageUrls: [] }, DEVICE_ID)
      expect(updated.imageUrls).toEqual([])

      const fetched = await repo.getRecipe(created.id)
      // Must be an array, not undefined — this is the RecipeDetail.tsx:175 crash
      expect(Array.isArray(fetched!.imageUrls)).toBe(true)
      expect(fetched!.imageUrls).toEqual([])
    })
  })

  describe('delete', () => {
    it('deletes a recipe and it stays deleted', async () => {
      const { repo } = await initRepo()
      const created = await repo.saveRecipe({ title: 'Delete Me', category: 'Misc' }, DEVICE_ID)

      await repo.deleteRecipe(created.id, DEVICE_ID)
      const fetched = await repo.getRecipe(created.id)
      expect(fetched).toBeNull()
    })

    it('removes the deleted recipe from all cookbooks', async () => {
      const { repo } = await initRepo()
      const cookbookRepo = await import('../../cookbook/repository')
      const created = await repo.saveRecipe({ title: 'In Cookbook', category: 'Dinner' }, DEVICE_ID)

      // Explicitly add the recipe to a cookbook first
      const target = await cookbookRepo.createCookbookNamed('Temp Holder', DEVICE_ID)
      await cookbookRepo.addRecipeToCookbook(target.id, created.id, DEVICE_ID)

      const before = await cookbookRepo.getCookbook(target.id)
      expect(before!.recipeIds).toContain(created.id)

      await repo.deleteRecipe(created.id, DEVICE_ID)

      const after = await cookbookRepo.getCookbook(target.id)
      expect(after!.recipeIds).not.toContain(created.id)
    })
  })

  describe('search + filter helpers', () => {
    it('searches by title, category, ingredient, tag', async () => {
      const { repo } = await initRepo()
      const r1 = await repo.saveRecipe(
        { title: 'Chocolate Cake', category: 'Dessert', tags: ['sweet'], ingredients: [{ id: 'i1', name: 'cocoa', order: 0 }] },
        DEVICE_ID,
      )

      expect(repo.searchRecipes([r1], 'chocolate')).toHaveLength(1)
      expect(repo.searchRecipes([r1], 'dessert')).toHaveLength(1)
      expect(repo.searchRecipes([r1], 'cocoa')).toHaveLength(1)
      expect(repo.searchRecipes([r1], 'sweet')).toHaveLength(1)
      expect(repo.searchRecipes([r1], 'pizza')).toHaveLength(0)
      expect(repo.searchRecipes([r1], '')).toHaveLength(1)
    })

    it('collects categories and tags', async () => {
      const { repo } = await initRepo()
      const r1 = await repo.saveRecipe({ title: 'A', category: 'Dinner', tags: ['x', 'y'] }, DEVICE_ID)
      const r2 = await repo.saveRecipe({ title: 'B', category: 'Dessert', tags: ['y', 'z'] }, DEVICE_ID)

      expect(repo.allCategories([r1, r2])).toEqual(['Dessert', 'Dinner'])
      expect(repo.allTags([r1, r2])).toEqual(['x', 'y', 'z'])
    })
  })

  describe('persistence across reload', () => {
    it('recipe changes survive a simulated app reload', async () => {
      // First "session": create and update a recipe
      const { repo: repo1 } = await initRepo()
      const created = await repo1.saveRecipe(
        { title: 'Reload Test', category: 'Dinner', imageUrls: ['img1'] },
        DEVICE_ID,
      )
      await repo1.saveRecipe({ ...created, title: 'Reload Test Updated' }, DEVICE_ID)

      // Simulate reload: fresh module graph, same IndexedDB
      const { repo: repo2 } = await initRepo()
      const fetched = await repo2.getRecipe(created.id)
      expect(fetched).not.toBeNull()
      expect(fetched!.title).toBe('Reload Test Updated')
      expect(fetched!.imageUrls).toEqual(['img1'])
    })

    it('recipe with cleared images survives reload with imageUrls as []', async () => {
      const { repo: repo1 } = await initRepo()
      const created = await repo1.saveRecipe(
        { title: 'Clear Images', category: 'Misc', imageUrls: ['img1'] },
        DEVICE_ID,
      )
      // Clear images (empty array, not undefined)
      await repo1.saveRecipe({ ...created, imageUrls: [] }, DEVICE_ID)

      const { repo: repo2 } = await initRepo()
      const fetched = await repo2.getRecipe(created.id)
      expect(fetched).not.toBeNull()
      expect(Array.isArray(fetched!.imageUrls)).toBe(true)
      expect(fetched!.imageUrls).toEqual([])
    })

    it('deleted recipes stay deleted after reload', async () => {
      const { repo: repo1 } = await initRepo()
      const created = await repo1.saveRecipe({ title: 'Will Delete', category: 'Misc' }, DEVICE_ID)
      await repo1.deleteRecipe(created.id, DEVICE_ID)

      const { repo: repo2 } = await initRepo()
      const fetched = await repo2.getRecipe(created.id)
      expect(fetched).toBeNull()
    })

    it('favorite toggle persists across reload', async () => {
      const { repo: repo1 } = await initRepo()
      const created = await repo1.saveRecipe({ title: 'Fav Reload', category: 'Misc' }, DEVICE_ID)
      await repo1.toggleFavorite(created.id, DEVICE_ID)

      const { repo: repo2 } = await initRepo()
      const fetched = await repo2.getRecipe(created.id)
      expect(fetched!.isFavorite).toBe(true)
    })
  })

  describe('mode', () => {
    it('reports rxdb mode after successful init', async () => {
      const { repo, store } = await initRepo()
      expect(repo.getRepositoryMode()).toBe('rxdb')
      expect(store.getMode()).toBe('rxdb')
      expect(store.getDb()).not.toBeNull()
    })
  })
})
