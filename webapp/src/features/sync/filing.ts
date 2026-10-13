// Filing rules for the Drive projection — which cookbook folder a recipe's
// file belongs in, and how recipe titles become readable file names. Pure
// functions so the rules are unit-testable.

import type { Cookbook } from '../../types/cookbook'

/** Folder for recipes that are not in any cookbook. */
export const UNSORTED_FOLDER = 'Unsorted'

/**
 * The cookbook folder a recipe's file lives in. The UI files each recipe
 * into at most one cookbook; if data drift ever puts it in several, the
 * oldest cookbook wins so the placement stays deterministic.
 */
export function primaryCookbookId(recipeId: string, cookbooks: Cookbook[]): string | null {
  const holders = cookbooks
    .filter((cb) => cb.recipeIds.includes(recipeId))
    .sort((a, b) => a.createdAt.localeCompare(b.createdAt))
  return holders[0]?.id ?? null
}

/** Human-readable Drive file name for a recipe, safe for Drive naming. */
export function recipeFileName(title: string): string {
  const safe = title.trim().replace(/[/\\]+/g, '-')
  return `${safe || 'Untitled recipe'}.json`
}
