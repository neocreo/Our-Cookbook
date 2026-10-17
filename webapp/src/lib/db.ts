// Local persistence via RxDB. Storage is pluggable: Dexie (IndexedDB) on web,
// and `rxdb/plugins/storage-sqlite` over @capacitor-community/sqlite on native
// (wired in a later phase). The abstraction keeps the repository storage-agnostic.
//
// The schema field names mirror the Kotlin domain models so Drive-synced
// files remain backwards-compatible with the old app.

import { createRxDatabase, addRxPlugin, type RxDatabase, type RxCollection } from 'rxdb'
import { getRxStorageDexie } from 'rxdb/plugins/storage-dexie'
import { wrappedValidateAjvStorage } from 'rxdb/plugins/validate-ajv'
import { RxDBMigrationSchemaPlugin } from 'rxdb/plugins/migration-schema'
import type { Recipe } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'
import { buildSeed } from './seed'

export type RecipeCollection = RxCollection<Recipe>
export type CookbookCollection = RxCollection<Cookbook>
export interface AppDatabase {
  recipes: RecipeCollection
  cookbooks: CookbookCollection
}

const ingredientItemSchema = {
  type: 'object',
  properties: {
    id: { type: 'string' },
    name: { type: 'string' },
    amount: { type: ['string', 'null'] },
    unit: { type: ['string', 'null'] },
    notes: { type: ['string', 'null'] },
    order: { type: 'number' },
  },
  required: ['id', 'name', 'order'],
} as const

const recipeSchema = {
  version: 1,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    title: { type: 'string' },
    description: { type: ['string', 'null'] },
    category: { type: 'string' },
    ingredients: { type: 'array', items: ingredientItemSchema },
    // v1 added ingredientSections (optional): named ingredient lists, e.g.
    // "Biscuits" and "Gravy" in a biscuits-and-gravy recipe.
    ingredientSections: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string' },
          name: { type: 'string' },
          ingredients: { type: 'array', items: ingredientItemSchema },
        },
        required: ['id', 'name', 'ingredients'],
      },
    },
    instructions: { type: 'array', items: { type: 'string' } },
    servingSize: { type: ['number', 'null'] },
    prepTime: { type: ['number', 'null'] },
    cookTime: { type: ['number', 'null'] },
    rating: { type: ['number', 'null'] },
    isFavorite: { type: 'boolean' },
    imageUrls: { type: 'array', items: { type: 'string' } },
    notes: { type: ['string', 'null'] },
    source: { type: ['string', 'null'] },
    tags: { type: 'array', items: { type: 'string' } },
    createdAt: { type: 'string' },
    updatedAt: { type: 'string' },
    versionVector: {
      type: 'object',
      properties: {
        deviceId: { type: 'string' },
        counter: { type: 'number' },
        timestamp: { type: 'string' },
      },
      required: ['deviceId', 'counter', 'timestamp'],
    },
    checksum: { type: 'string' },
    deviceId: { type: 'string' },
  },
  required: ['id', 'title', 'category', 'createdAt', 'updatedAt'],
} as const

const cookbookSchema = {
  version: 1,
  primaryKey: 'id',
  type: 'object',
  properties: {
    id: { type: 'string', maxLength: 100 },
    name: { type: 'string' },
    description: { type: ['string', 'null'] },
    ownerDeviceId: { type: 'string' },
    ownerName: { type: ['string', 'null'] },
    isShared: { type: 'boolean' },
    sharingLink: { type: ['string', 'null'] },
    imageUri: { type: ['string', 'null'] },
    createdAt: { type: 'string' },
    updatedAt: { type: 'string' },
    recipeIds: { type: 'array', items: { type: 'string' } },
  },
  required: ['id', 'name', 'ownerDeviceId', 'createdAt', 'updatedAt'],
} as const

let dbPromise: Promise<AppDatabase> | null = null

// Required by the cookbook schema's migrationStrategies (v1 added ownerName).
addRxPlugin(RxDBMigrationSchemaPlugin)

export function initDatabase(deviceId: string): Promise<AppDatabase> {
  if (dbPromise) return dbPromise

  dbPromise = (async () => {
    const db: RxDatabase = await createRxDatabase({
      name: 'ourcookbook',
      // AJV-wrapped Dexie storage: IndexedDB on web, with schema validation so
      // malformed documents are rejected at write time.
      storage: wrappedValidateAjvStorage({ storage: getRxStorageDexie() }),
      // Close stale instances on hot-reload (React HMR reloads code without
      // reloading the process; the old RxDB handle would otherwise block
      // re-creation with DB8).
      closeDuplicates: true,
    })

    const collections = await db.addCollections({
      recipes: {
        schema: recipeSchema,
        migrationStrategies: {
          // v1 added ingredientSections (optional) — old docs simply lack it.
          1: (doc) => doc,
        },
      },
      cookbooks: {
        schema: cookbookSchema,
        migrationStrategies: {
          // v1 added ownerName (nullable) so cookbooks can show a person, not a device.
          1: (doc) => ({ ...doc, ownerName: doc.ownerName ?? null }),
        },
      },
    })

    const appDb: AppDatabase = {
      recipes: collections.recipes as RecipeCollection,
      cookbooks: collections.cookbooks as CookbookCollection,
    }

    await seedIfEmpty(appDb, deviceId)
    return appDb
  })().catch((err) => {
    // Surface the failure; the repository falls back to a session store so the
    // app still works for verification if IndexedDB is unavailable.
    console.error('[db] RxDB initialization failed', err)
    dbPromise = null
    throw err
  })

  return dbPromise
}

async function seedIfEmpty(db: AppDatabase, deviceId: string): Promise<void> {
  const count = await db.recipes.count().exec()
  if (count > 0) return
  const { cookbooks, recipes } = buildSeed(deviceId)
  await db.recipes.bulkInsert(recipes)
  await db.cookbooks.bulkInsert(cookbooks)
}
