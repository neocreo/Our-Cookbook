// RecipeDetail — shows a single recipe with slideshow, share, and copy functionality.

import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Star, Trash2, Share2, Copy, BookOpen } from 'lucide-react'
import { Button } from '../components/Button'
import { Tag } from '../components/Tag'
import { ImageSlideshow } from '../components/ImageSlideshow'
import { getRecipe, deleteRecipe, toggleFavorite } from '../features/recipe/repository'
import { getDeviceId } from '../lib/device'
import { totalRecipeTime } from '../types/recipe'
import { useCookbooks } from '../hooks/useCookbooks'
import type { Recipe } from '../types/recipe'

function formatTime(minutes: number | null): string | null {
  if (minutes == null) return null
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h} hr ${m} min` : `${h} hr`
}

export function RecipeDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [recipe, setRecipe] = useState<Recipe | null>(null)
  const [loading, setLoading] = useState(true)
  const { cookbooks } = useCookbooks()

  useEffect(() => {
    let active = true
    if (!id) return
    getRecipe(id).then((r) => {
      if (active) {
        setRecipe(r)
        setLoading(false)
      }
    })
    return () => { active = false }
  }, [id])

  if (loading) return <main className="page"><p className="text-muted">Loading…</p></main>
  if (!recipe) return <main className="page"><p className="text-muted">Recipe not found.</p></main>

  // Find cookbook containing this recipe
  function getCookbookForRecipe() {
    if (!recipe) return null
    for (const cb of cookbooks) {
      if (cb.recipeIds.includes(recipe.id)) {
        return cb
      }
    }
    return null
  }

  const cookbook = getCookbookForRecipe()
  const time = recipe ? formatTime(totalRecipeTime(recipe)) : null

  async function onFavorite() {
    if (!recipe) return
    await toggleFavorite(recipe.id, getDeviceId())
    const fresh = await getRecipe(recipe.id)
    if (fresh) setRecipe(fresh)
  }

  async function onDelete() {
    if (!recipe) return
    if (!window.confirm(`Delete "${recipe.title}"? This cannot be undone.`)) return
    await deleteRecipe(recipe.id, getDeviceId())
    navigate('/')
  }

  // Format recipe as text for sharing
  function formatRecipeAsText(r: Recipe) {
    const time = formatTime(totalRecipeTime(r))
    const lines: string[] = [
      `=== ${r.title} ===`,
      '',
      `Category: ${r.category}`,
      ...(r.description ? [`Description: ${r.description}`] : []),
      '',
      `Cookbook: ${cookbook?.name || 'None'}`,
      `Time: ${time || 'N/A'}`,
      ...(r.servingSize ? [`Servings: ${r.servingSize}`] : []),
      ...(r.source ? [`Source: ${r.source}`] : []),
      '',
      '--- Ingredients ---',
      ...r.ingredients.map((ing) => `- ${[ing.amount, ing.unit].filter(Boolean).join(' ')} ${ing.name}${ing.notes ? ` (${ing.notes})` : ''}`),
      '',
      '--- Instructions ---',
      ...r.instructions.map((step, i) => `${i + 1}. ${step}`),
      '',
      ...(r.notes ? [`Notes: ${r.notes}`] : []),
      '',
      ...(r.tags.length > 0 ? [`Tags: ${r.tags.join(', ')}`] : []),
    ]
    return lines.join('\n')
  }

  // Share functionality
  async function handleShare() {
    if (!recipe) return
    const text = formatRecipeAsText(recipe)
    try {
      if (navigator.share) {
        await navigator.share({
          title: recipe.title,
          text,
        })
      } else {
        await navigator.clipboard.writeText(text)
        alert('Recipe copied to clipboard!')
      }
    } catch {
      // Fallback to clipboard if share fails
      await navigator.clipboard.writeText(text)
      alert('Recipe copied to clipboard!')
    }
  }

  // Copy recipe to another cookbook
  function handleCopy() {
    if (!recipe) return
    navigate(`/recipes/new?copyFrom=${recipe.id}`)
  }

  // Navigate to category search
  function handleCategoryClick(e: React.MouseEvent) {
    e.preventDefault()
    if (!recipe) return
    navigate(`/search?category=${encodeURIComponent(recipe.category)}`)
  }

  // Navigate to tag search
  function handleTagClick(e: React.MouseEvent, tag: string) {
    e.preventDefault()
    navigate(`/search?tag=${encodeURIComponent(tag)}`)
  }

  // Navigate to cookbook
  function handleCookbookClick(e: React.MouseEvent) {
    e.preventDefault()
    if (cookbook) {
      navigate(`/cookbooks/${cookbook.id}`)
    }
  }

  return (
    <main className="page">
      <div className="row mb-2">
        <Link to=".." relative="path" className="btn btn-ghost btn-icon" aria-label="Back">
          <ArrowLeft strokeWidth={2.75} />
        </Link>
        <div className="row-spacer" />
        {/* Action buttons on top-right */}
        <Button variant="ghost" onClick={handleCopy} aria-label="Copy recipe">
          <Copy size={18} strokeWidth={2.75} />
        </Button>
        <Button variant="ghost" onClick={handleShare} aria-label="Share recipe">
          <Share2 size={18} strokeWidth={2.75} />
        </Button>
        <Button variant="secondary" onClick={onFavorite} aria-label={recipe.isFavorite ? 'Remove from favorites' : 'Add to favorites'}>
          <Star size={18} fill={recipe.isFavorite ? 'currentColor' : 'none'} strokeWidth={2.75} />
        </Button>
        <Link to="edit" className="btn btn-secondary" aria-label="Edit recipe">
          <Pencil size={18} strokeWidth={2.75} />
        </Link>
        <Button variant="ghost" onClick={onDelete} aria-label="Delete recipe">
          <Trash2 size={18} strokeWidth={2.75} />
        </Button>
      </div>

      <div className="recipe-hero">
        {/* Image slideshow */}
        {recipe.imageUrls.length > 0 && (
          <div className="mb-4">
            <ImageSlideshow imageUrls={recipe.imageUrls} alt={recipe.title} />
          </div>
        )}

        <h1>{recipe.title}</h1>
        {recipe.description && <p className="text-muted">{recipe.description}</p>}

        <div className="chip-row mt-2">
          <button
            type="button"
            className="btn-reset"
            onClick={handleCategoryClick}
            aria-label={`Filter by ${recipe.category}`}
          >
            <Tag variant="accent">{recipe.category}</Tag>
          </button>
          {recipe.tags.map((t) => (
            <button
              key={t}
              type="button"
              className="btn-reset"
              onClick={(e) => handleTagClick(e, t)}
              aria-label={`Filter by ${t}`}
            >
              <Tag variant="neutral">{t}</Tag>
            </button>
          ))}
        </div>

        <div className="card-meta mt-2">
          {time != null && <span>{time} total</span>}
          {time && recipe.servingSize && <span> · </span>}
          {recipe.servingSize != null && <span>{recipe.servingSize} servings</span>}
          {((time || recipe.servingSize) && cookbook) && <span> · </span>}
          {cookbook && (
            <button
              type="button"
              className="btn-reset btn-reset-accent"
              onClick={handleCookbookClick}
            >
              <BookOpen size={14} strokeWidth={2.75} />
              {cookbook.name}
            </button>
          )}
          {recipe.source && <span> · {recipe.source}</span>}
        </div>
      </div>

      <section className="recipe-section">
        <h3>Ingredients</h3>
        {recipe.ingredients.length === 0 ? (
          <p className="text-muted">None</p>
        ) : (
          <ul className="ingredient-list">
            {recipe.ingredients.map((ing) => (
              <li key={ing.id}>
                {[ing.amount, ing.unit].filter(Boolean).join(' ')} {ing.name}
                {ing.notes && ` (${ing.notes})`}
              </li>
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
    </main>
  )
}
