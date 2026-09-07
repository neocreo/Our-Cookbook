// Home — the recipe list. Live-subscribes to the repository and renders a
// card per recipe. A FAB opens the new-recipe form. A favorites toggle
// filters to starred recipes.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Star } from 'lucide-react'
import { Card } from '../components/Card'
import { useRecipes } from '../hooks/useRecipes'
import { totalRecipeTime } from '../types/recipe'

function formatTime(minutes: number | null): string | null {
  if (minutes == null) return null
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h} hr ${m} min` : `${h} hr`
}

export function Home() {
  const { recipes, loading, error } = useRecipes()
  const [favoritesOnly, setFavoritesOnly] = useState(false)

  const shown = favoritesOnly ? recipes.filter((r) => r.isFavorite) : recipes

  return (
    <main className="page">
      <h1 className="page-title">Home</h1>

      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
        <button
          type="button"
          className={`btn ${favoritesOnly ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setFavoritesOnly((v) => !v)}
          aria-pressed={favoritesOnly}
        >
          <Star size={16} fill={favoritesOnly ? 'currentColor' : 'none'} strokeWidth={2.75} />
          Favorites
        </button>
      </div>

      {loading && <p className="text-muted">Loading recipes…</p>}
      {error && <p className="text-muted">Could not load recipes: {error}</p>}

      {!loading && shown.length === 0 && (
        <p className="text-muted">
          {favoritesOnly
            ? 'No favorite recipes yet. Tap the star on a recipe to add it here.'
            : 'No recipes yet. Tap the + button to add your first one.'}
        </p>
      )}

      <div className="stack">
        {shown.map((r) => {
          const time = formatTime(totalRecipeTime(r))
          const meta = [time, r.servingSize ? `${r.servingSize} servings` : null]
            .filter(Boolean)
            .join(' · ')
          return (
            <Link key={r.id} to={`/recipes/${r.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <Card
                as="article"
                elevation="sm"
                kicker={r.category}
                title={r.title}
                meta={
                  <>
                    {meta && <span>{meta}</span>}
                    {r.isFavorite && <Star size={13} fill="currentColor" strokeWidth={2.75} />}
                  </>
                }
              >
                {r.description ?? (r.ingredients.length ? `${r.ingredients.length} ingredients` : null)}
              </Card>
            </Link>
          )
        })}
      </div>

      <Link to="/recipes/new" className="fab" aria-label="Add recipe">
        <Plus strokeWidth={2.75} />
      </Link>
    </main>
  )
}
