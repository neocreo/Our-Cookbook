// Integration tests for the cookbook repository. Exercises the full RxDB
// persistence path: init → create → read → rename → add/remove recipes →
// delete, and verifies that data survives a simulated app reload.

import { describe, it, expect, beforeEach, vi } from 'vitest'

const DEVICE_ID = 'test-device-1'

async function freshImport() {
  vi.resetModules()
  const store = await import('../../../lib/store')
  const recipeRepo = await import('../../recipe/repository')
  const cookbookRepo = await import('../repository')
  return { store, recipeRepo, cookbookRepo }
}

async function initRepos() {
  const { store, recipeRepo, cookbookRepo } = await freshImport()
  await recipeRepo.initRepository(DEVICE_ID)
  return { store, recipeRepo, cookbookRepo }
}

describe('cookbook repository (RxDB persistence)', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('create + read', () => {
    it('creates a cookbook and reads it back', async () => {
      const { cookbookRepo } = await initRepos()
      const cb = await cookbookRepo.createCookbookNamed('My Book', DEVICE_ID, 'A description', 'Alice')
      expect(cb.id).toBeTruthy()
      expect(cb.name).toBe('My Book')
      expect(cb.description).toBe('A description')
      expect(cb.ownerName).toBe('Alice')
      expect(cb.recipeIds).toEqual([])

      const fetched = await cookbookRepo.getCookbook(cb.id)
      expect(fetched).not.toBeNull()
      expect(fetched!.name).toBe('My Book')
      expect(fetched!.ownerName).toBe('Alice')
    })

    it('lists all cookbooks including seeded ones', async () => {
      const { cookbookRepo } = await initRepos()
      const all = await cookbookRepo.listCookbooks()
      // Seed data creates 3 cookbooks
      expect(all.length).toBeGreaterThanOrEqual(3)
    })
  })

  describe('rename', () => {
    it('renames a cookbook and the change is readable immediately', async () => {
      const { cookbookRepo } = await initRepos()
      const cb = await cookbookRepo.createCookbookNamed('Original', DEVICE_ID)

      await cookbookRepo.renameCookbook(cb.id, 'Renamed', 'New desc', DEVICE_ID)

      const fetched = await cookbookRepo.getCookbook(cb.id)
      expect(fetched!.name).toBe('Renamed')
      expect(fetched!.description).toBe('New desc')
    })

    it('can clear the description (empty string)', async () => {
      const { cookbookRepo } = await initRepos()
      const cb = await cookbookRepo.createCookbookNamed('With Desc', DEVICE_ID, 'Some desc')

      await cookbookRepo.renameCookbook(cb.id, 'With Desc', '', DEVICE_ID)

      const fetched = await cookbookRepo.getCookbook(cb.id)
      expect(fetched!.description).toBe('')
    })
  })

  describe('add / remove recipes', () => {
    it('adds a recipe to a cookbook', async () => {
      const { recipeRepo, cookbookRepo } = await initRepos()
      const recipe = await recipeRepo.saveRecipe({ title: 'Test Recipe', category: 'Dinner' }, DEVICE_ID)
      const cb = await cookbookRepo.createCookbookNamed('Target', DEVICE_ID)

      await cookbookRepo.addRecipeToCookbook(cb.id, recipe.id, DEVICE_ID)

      const fetched = await cookbookRepo.getCookbook(cb.id)
      expect(fetched!.recipeIds).toContain(recipe.id)
    })

    it('does not add the same recipe twice', async () => {
      const { recipeRepo, cookbookRepo } = await initRepos()
      const recipe = await recipeRepo.saveRecipe({ title: 'Dup Test', category: 'Dinner' }, DEVICE_ID)
      const cb = await cookbookRepo.createCookbookNamed('Dup Book', DEVICE_ID)

      await cookbookRepo.addRecipeToCookbook(cb.id, recipe.id, DEVICE_ID)
      await cookbookRepo.addRecipeToCookbook(cb.id, recipe.id, DEVICE_ID)

      const fetched = await cookbookRepo.getCookbook(cb.id)
      expect(fetched!.recipeIds.filter((id) => id === recipe.id)).toHaveLength(1)
    })

    it('removes a recipe from a cookbook', async () => {
      const { recipeRepo, cookbookRepo } = await initRepos()
      const recipe = await recipeRepo.saveRecipe({ title: 'Remove Me', category: 'Dinner' }, DEVICE_ID)
      const cb = await cookbookRepo.createCookbookNamed('Remove Book', DEVICE_ID)

      await cookbookRepo.addRecipeToCookbook(cb.id, recipe.id, DEVICE_ID)
      await cookbookRepo.removeRecipeFromCookbook(cb.id, recipe.id, DEVICE_ID)

      const fetched = await cookbookRepo.getCookbook(cb.id)
      expect(fetched!.recipeIds).not.toContain(recipe.id)
    })
  })

  describe('delete', () => {
    it('deletes a cookbook', async () => {
      const { cookbookRepo } = await initRepos()
      const cb = await cookbookRepo.createCookbookNamed('Delete Me', DEVICE_ID)

      await cookbookRepo.deleteCookbook(cb.id, DEVICE_ID)

      const fetched = await cookbookRepo.getCookbook(cb.id)
      expect(fetched).toBeNull()
    })
  })

  describe('fileUnfiledRecipes', () => {
    it('files unfiled recipes into the default cookbook', async () => {
      const { recipeRepo, cookbookRepo } = await initRepos()
      const recipe = await recipeRepo.saveRecipe({ title: 'Unfiled', category: 'Misc' }, DEVICE_ID)

      // Remove the recipe from any cookbook it might be in
      const all = await cookbookRepo.listCookbooks()
      for (const cb of all) {
        if (cb.recipeIds.includes(recipe.id)) {
          await cookbookRepo.removeRecipeFromCookbook(cb.id, recipe.id, DEVICE_ID)
        }
      }

      const filed = await cookbookRepo.fileUnfiledRecipes(DEVICE_ID, [recipe.id])
      expect(filed).toBe(1)

      const after = await cookbookRepo.listCookbooks()
      const holder = after.find((cb) => cb.recipeIds.includes(recipe.id))
      expect(holder).toBeDefined()
      expect(holder!.name).toBe('Mina recept')
    })
  })

  describe('persistence across reload', () => {
    it('cookbook rename survives a simulated app reload', async () => {
      // First "session": create and rename
      const { cookbookRepo: repo1 } = await initRepos()
      const cb = await repo1.createCookbookNamed('Before Reload', DEVICE_ID, 'desc1')
      await repo1.renameCookbook(cb.id, 'After Reload', 'desc2', DEVICE_ID)

      // Simulate reload
      const { cookbookRepo: repo2 } = await initRepos()
      const fetched = await repo2.getCookbook(cb.id)
      expect(fetched).not.toBeNull()
      expect(fetched!.name).toBe('After Reload')
      expect(fetched!.description).toBe('desc2')
    })

    it('added recipes survive a simulated app reload', async () => {
      const { recipeRepo: r1, cookbookRepo: c1 } = await initRepos()
      const recipe = await r1.saveRecipe({ title: 'Persist Recipe', category: 'Dinner' }, DEVICE_ID)
      const cb = await c1.createCookbookNamed('Persist Book', DEVICE_ID)
      await c1.addRecipeToCookbook(cb.id, recipe.id, DEVICE_ID)

      const { cookbookRepo: c2 } = await initRepos()
      const fetched = await c2.getCookbook(cb.id)
      expect(fetched!.recipeIds).toContain(recipe.id)
    })

    it('removed recipes stay removed after reload', async () => {
      const { recipeRepo: r1, cookbookRepo: c1 } = await initRepos()
      const recipe = await r1.saveRecipe({ title: 'Temp Recipe', category: 'Dinner' }, DEVICE_ID)
      const cb = await c1.createCookbookNamed('Temp Book', DEVICE_ID)
      await c1.addRecipeToCookbook(cb.id, recipe.id, DEVICE_ID)
      await c1.removeRecipeFromCookbook(cb.id, recipe.id, DEVICE_ID)

      const { cookbookRepo: c2 } = await initRepos()
      const fetched = await c2.getCookbook(cb.id)
      expect(fetched!.recipeIds).not.toContain(recipe.id)
    })

    it('deleted cookbooks stay deleted after reload', async () => {
      const { cookbookRepo: c1 } = await initRepos()
      const cb = await c1.createCookbookNamed('Delete After Reload', DEVICE_ID)
      await c1.deleteCookbook(cb.id, DEVICE_ID)

      const { cookbookRepo: c2 } = await initRepos()
      const fetched = await c2.getCookbook(cb.id)
      expect(fetched).toBeNull()
    })

    it('newly created cookbook survives reload', async () => {
      const { cookbookRepo: c1 } = await initRepos()
      const cb = await c1.createCookbookNamed('New After Reload', DEVICE_ID, 'A brand new book', 'Bob')

      const { cookbookRepo: c2 } = await initRepos()
      const fetched = await c2.getCookbook(cb.id)
      expect(fetched).not.toBeNull()
      expect(fetched!.name).toBe('New After Reload')
      expect(fetched!.ownerName).toBe('Bob')
    })
  })

  describe('mode', () => {
    it('reports rxdb mode after successful init', async () => {
      const { store } = await initRepos()
      expect(store.getMode()).toBe('rxdb')
      expect(store.getDb()).not.toBeNull()
    })
  })
})
