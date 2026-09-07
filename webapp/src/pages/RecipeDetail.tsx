// RecipeDetail — shows a single recipe with favorite toggle, edit, and delete.

import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Star, Trash2 } from 'lucide-react'
import { Button } from '../components/Button'
import { Tag } from '../components/Tag'
import { getRecipe, deleteRecipe, toggleFavorite } from '../features/recipe/repository'
import { getDeviceId } from '../lib/device'
import { totalRecipeTime } from '../types/recipe'
import type { Recipe } from '../types/recipe'

export function RecipeDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [recipe, setRecipe] = useState<Recipe | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    if (!id) return
    getRecipe(id).then((r) => {
      if (active) {
        setRecipe(r)
        setLoading(false)
      }
    })
    return () => {
      active = false
    }
  }, [id])

  if (loading) return <main className="page"><p className="text-muted">Loading…</p></main>
  if (!recipe) return <main className="page"><p className="text-muted">Recipe not found.</p></main>

  async function onFavorite() {
    if (!recipe) return
    await toggleFavorite(recipe.id, getDeviceId())
    const fresh = await getRecipe(recipe.id)
    if (fresh) setRecipe(fresh)
  }

  async function onDelete() {
    if (!recipe) return
    if (!window.confirm(`Delete "${recipe.title}"? This cannot be undone.`)) return
    await deleteRecipe(recipe.id)
    navigate('/')
  }

  const time = totalRecipeTime(recipe)

  return (
    <main className="page">
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <Link to="/" className="btn btn-ghost btn-icon" aria-label="Back">
          <ArrowLeft strokeWidth={2.75} />
        </Link>
      </div>

      <div className="recipe-hero">
        <h1>{recipe.title}</h1>
        {recipe.description && <p className="text-muted">{recipe.description}</p>}
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 }}>
          <Tag variant="accent">{recipe.category}</Tag>
          {recipe.tags.map((t) => (
            <Tag key={t} variant="neutral">
              {t}
            </Tag>
          ))}
        </div>
        <div className="card-meta" style={{ marginTop: 8 }}>
          {time != null && <span>{time} min total</span>}
          {recipe.servingSize != null && <span>· {recipe.servingSize} servings</span>}
          {recipe.source && <span>· {recipe.source}</span>}
        </div>
      </div>

      <section className="recipe-section">
        <h3>Ingredients</h3>
        {recipe.ingredients.length === 0 ? (
          <p className="text-muted">None</p>
        ) : (
          <ul className="ingredient-list">
            {recipe.ingredients.map((ing) => (
              <li key={ing.id}>{[ing.amount, ing.unit, ing.name].filter(Boolean).join(' ')}</li>
            ))}
          </ul>
        )}
      </section>

      <section className="recipe-section">
        <h3>Instructions</h3>
        {recipe.instructions.length === 0 ? (
          <p className="text-muted">None</p>
        ) : (
          <ol className="instruction-list">
            {recipe.instructions.map((step, i) => (
              <li key={i}>{step}</li>
            ))}
          </ol>
        )}
      </section>

      {recipe.notes && (
        <section className="recipe-section">
          <h3>Notes</h3>
          <p>{recipe.notes}</p>
        </section>
      )}

      <div className="form-actions">
        <Button variant="secondary" onClick={onFavorite}>
          <Star size={16} fill={recipe.isFavorite ? 'currentColor' : 'none'} strokeWidth={2.75} />
          {recipe.isFavorite ? 'Favorited' : 'Favorite'}
        </Button>
        <Link to={`/recipes/${recipe.id}/edit`} className="btn btn-secondary">
          <Pencil size={16} strokeWidth={2.75} /> Edit
        </Link>
        <Button variant="ghost" onClick={onDelete}>
          <Trash2 size={16} strokeWidth={2.75} /> Delete
        </Button>
      </div>
    </main>
  )
}
