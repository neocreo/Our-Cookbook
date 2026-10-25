// RecipeDetail — shows a single recipe with slideshow, share, and copy functionality.

import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Star, Trash2, Share2, Copy, BookOpen } from 'lucide-react'
import { Button } from '../components/Button'
import { Tag } from '../components/Tag'
import { ImageSlideshow } from '../components/ImageSlideshow'
import { getRecipe, deleteRecipe, toggleFavorite } from '../features/recipe/repository'
import { getDeviceId } from '../lib/device'
import { totalRecipeTime, getIngredientSections } from '../types/recipe'
import { useCookbooks } from '../hooks/useCookbooks'
import { useT, formatRecipeTime } from '../i18n'
import type { Recipe } from '../types/recipe'

export function RecipeDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const t = useT()
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

  if (loading) return <main className="page"><p className="text-muted">{t('common.loading')}</p></main>
  if (!recipe) return <main className="page"><p className="text-muted">{t('recipeDetail.notFound')}</p></main>

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
  const time = recipe ? formatRecipeTime(totalRecipeTime(recipe), t) : null

  async function onFavorite() {
    if (!recipe) return
    await toggleFavorite(recipe.id, getDeviceId())
    const fresh = await getRecipe(recipe.id)
    if (fresh) setRecipe(fresh)
  }

  async function onDelete() {
    if (!recipe) return
    if (!window.confirm(t('recipeDetail.deleteConfirm', { title: recipe.title }))) return
    await deleteRecipe(recipe.id, getDeviceId())
    navigate('/')
  }

  // Format recipe as text for sharing
  function formatRecipeAsText(r: Recipe) {
    const time = formatRecipeTime(totalRecipeTime(r), t)
    const lines: string[] = [
      `=== ${r.title} ===`,
      '',
      t('recipeDetail.shareCategory', { x: r.category }),
      ...(r.description ? [t('recipeDetail.shareDescription', { x: r.description })] : []),
      '',
      t('recipeDetail.shareCookbook', { x: cookbook?.name || t('recipeDetail.shareCookbookNone') }),
      t('recipeDetail.shareTime', { x: time || t('recipeDetail.shareTimeNA') }),
      ...(r.servingSize ? [t('recipeDetail.shareServings', { x: r.servingSize })] : []),
      ...(r.source ? [t('recipeDetail.shareSource', { x: r.source })] : []),
      '',
      t('recipeDetail.shareIngredients'),
      ...getIngredientSections(r).flatMap((s) => [
        ...(s.name ? [`## ${s.name}`] : []),
        ...s.ingredients.map((ing) => `- ${[ing.amount, ing.unit].filter(Boolean).join(' ')} ${ing.name}${ing.notes ? ` (${ing.notes})` : ''}`),
      ]),
      '',
      t('recipeDetail.shareInstructions'),
      ...r.instructions.map((step, i) => `${i + 1}. ${step}`),
      '',
      ...(r.notes ? [t('recipeDetail.shareNotes', { x: r.notes })] : []),
      '',
      ...(r.tags.length > 0 ? [t('recipeDetail.shareTags', { x: r.tags.join(', ') })] : []),
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
        alert(t('recipeDetail.copied'))
      }
    } catch {
      // Fallback to clipboard if share fails
      await navigator.clipboard.writeText(text)
      alert(t('recipeDetail.copied'))
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
    if (!recipe) return
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
        <Link to=".." relative="path" className="btn btn-ghost btn-icon" aria-label={t('common.back')}>
          <ArrowLeft strokeWidth={2.75} />
        </Link>
        <div className="row-spacer" />
        {/* Action buttons on top-right */}
        <Button variant="ghost" onClick={handleCopy} aria-label={t('recipeDetail.copyAria')}>
          <Copy size={18} strokeWidth={2.75} />
        </Button>
        <Button variant="ghost" onClick={handleShare} aria-label={t('recipeDetail.shareAria')}>
          <Share2 size={18} strokeWidth={2.75} />
        </Button>
        <Button variant="secondary" onClick={onFavorite} aria-label={recipe.isFavorite ? t('recipeDetail.favoriteRemoveAria') : t('recipeDetail.favoriteAddAria')}>
          <Star size={18} fill={recipe.isFavorite ? 'currentColor' : 'none'} strokeWidth={2.75} />
        </Button>
        <Link to="edit" className="btn btn-secondary" aria-label={t('recipeDetail.editAria')}>
          <Pencil size={18} strokeWidth={2.75} />
        </Link>
        <Button variant="ghost" onClick={onDelete} aria-label={t('recipeDetail.deleteAria')}>
          <Trash2 size={18} strokeWidth={2.75} />
        </Button>
      </div>

      <div className="recipe-hero">
        {/* Only a real uploaded photo is shown inside the recipe — the
            fallback illustration is for list icons, not here. */}
        {(recipe.imageUrls ?? []).length > 0 && (
          <div className="mb-4">
            <ImageSlideshow imageUrls={recipe.imageUrls ?? []} alt={recipe.title} />
          </div>
        )}

        <h1>{recipe.title}</h1>
        {recipe.description && <p className="text-muted">{recipe.description}</p>}

        <div className="chip-row mt-2">
          <button
            type="button"
            className="btn-reset"
            onClick={handleCategoryClick}
            aria-label={t('common.filterBy', { x: recipe.category })}
          >
            <Tag variant="accent">{recipe.category}</Tag>
          </button>
          {recipe.tags.map((tag) => (
            <button
              key={tag}
              type="button"
              className="btn-reset"
              onClick={(e) => handleTagClick(e, tag)}
              aria-label={t('common.filterBy', { x: tag })}
            >
              <Tag variant="neutral">{tag}</Tag>
            </button>
          ))}
        </div>

        <div className="card-meta mt-2">
          {time != null && <span>{t('recipeDetail.total', { time })}</span>}
          {time && recipe.servingSize && <span> · </span>}
          {recipe.servingSize != null && <span>{t('common.servingsCount', { count: recipe.servingSize })}</span>}
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

      {getIngredientSections(recipe).map((s) => (
        <section className="recipe-section" key={s.id}>
          <h3>{s.name || t('recipeDetail.ingredients')}</h3>
          {s.ingredients.length === 0 ? (
            <p className="text-muted">{t('common.none')}</p>
          ) : (
            <ul className="ingredient-list">
              {s.ingredients.map((ing) => (
                <li key={ing.id}>
                  {[ing.amount, ing.unit].filter(Boolean).join(' ')} {ing.name}
                  {ing.notes && ` (${ing.notes})`}
                </li>
              ))}
            </ul>
          )}
        </section>
      ))}

      <section className="recipe-section">
        <h3>{t('recipeDetail.instructions')}</h3>
        {recipe.instructions.length === 0 ? (
          <p className="text-muted">{t('common.none')}</p>
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
          <h3>{t('recipeDetail.notes')}</h3>
          <p>{recipe.notes}</p>
        </section>
      )}
    </main>
  )
}
