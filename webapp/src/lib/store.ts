// Shared storage state. Holds the RxDB database handle (or the memory-mode
// fallback maps) so each bounded-context repository (recipe, cookbook) can
// access the same storage without depending on each other.
//
// initStore() is called once on app launch; repositories read from getDb() /
// getMemRecipes() / getMemCookbooks() depending on the active mode.

import type { Recipe } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'
import { initDatabase, type AppDatabase } from './db'
import { buildSeed } from './seed'

export type Mode = 'init' | 'rxdb' | 'memory'

let mode: Mode = 'init'
let appDb: AppDatabase | null = null
const memRecipes = new Map<string, Recipe>()
const memCookbooks = new Map<string, Cookbook>()

export async function initStore(deviceId: string): Promise<void> {
  if (mode !== 'init') return
  try {
    appDb = await initDatabase(deviceId)
    mode = 'rxdb'
  } catch {
    mode = 'memory'
    appDb = null
    const { cookbooks, recipes } = buildSeed(deviceId)
    for (const r of recipes) memRecipes.set(r.id, r)
    for (const c of cookbooks) memCookbooks.set(c.id, c)
  }
}

export function getMode(): Mode {
  return mode
}

export function getDb(): AppDatabase | null {
  return appDb
}

export function getMemRecipes(): Map<string, Recipe> {
  return memRecipes
}

export function getMemCookbooks(): Map<string, Cookbook> {
  return memCookbooks
}
