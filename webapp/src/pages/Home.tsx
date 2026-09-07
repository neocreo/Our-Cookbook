// Home — the recipe list. Live-subscribes to the repository and renders a
// card per recipe. A FAB opens the new-recipe form.

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

  return (
    <main className="page">
      <h1 className="page-title">Recipes</h1>

      {loading && <p className="text-muted">Loading recipes…</p>}
      {error && <p className="text-muted">Could not load recipes: {error}</p>}

      {!loading && recipes.length === 0 && (
        <div className="stack">
          <p className="text-muted">No recipes yet. Tap the + button to add your first one.</p>
        </div>
      )}

      <div className="stack">
        {recipes.map((r) => {
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
