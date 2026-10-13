// useRecipes — subscribes to the recipe repository and exposes a live list.
// Also lazily initializes the repository (and thus the database) on mount.

import { useEffect, useState } from 'react'
import { initRepository, subscribeRecipes } from '../features/recipe/repository'
import type { Recipe } from '../types/recipe'
import { getDeviceId } from '../lib/device'

export function useRecipes(): { recipes: Recipe[]; loading: boolean; error: string | null } {
  const [recipes, setRecipes] = useState<Recipe[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let unsub: (() => void) | null = null
    let active = true;

    (async () => {
      try {
        await initRepository(getDeviceId())
        if (!active) return
        unsub = subscribeRecipes((list) => {
          setRecipes(list)
          setLoading(false)
        })
      } catch (e) {
        if (active) {
          setError(e instanceof Error ? e.message : 'Failed to load recipes')
          setLoading(false)
        }
      }
    })()

    return () => {
      active = false
      if (unsub) unsub()
    }
  }, [])

  return { recipes, loading, error }
}
