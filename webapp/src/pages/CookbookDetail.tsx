// CookbookDetail — shows the recipes in a cookbook with an A-Ö index rail and
// category filter. Supports in-place editing of title and description.

import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Plus, Trash2, X, Check } from 'lucide-react'
import { Button } from '../components/Button'
import { WashedImage } from '../components/WashedImage'
import { RecipeFallbackImage } from '../components/RecipeFallbackImage'
import { Tag } from '../components/Tag'
import { getCookbook, deleteCookbook, renameCookbook } from '../features/cookbook/repository'
import { deleteRecipe } from '../features/recipe/repository'
import { useRecipes } from '../hooks/useRecipes'
import { getDeviceId } from '../lib/device'
import { useAppStore } from '../stores/appStore'
import { useT, formatRecipeTime } from '../i18n'
import { ownerDisplayName, type Cookbook } from '../types/cookbook'
import { getFirstImageUrl, totalRecipeTime } from '../types/recipe'
import type { Recipe } from '../types/recipe'

// A-Ö including Swedish letters
const AZ_LETTERS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Å', 'Ä', 'Ö']

// Numeric titles (e.g. "3-ingredient pasta") sort under "123".
const NUMERIC = '123'

// The index letter a title sorts under; digits go to "123", anything else
// outside A-Ö falls under '#'.
function indexLetter(title: string): string {
  const first = title.trim().toUpperCase().charAt(0)
  if (AZ_LETTERS.includes(first)) return first
  if (first >= '0' && first <= '9') return NUMERIC
  return '#'
}

// Rail order: A-Ö, then "123", then '#' for remaining symbols.
function indexRank(letter: string): number {
  if (letter === NUMERIC) return AZ_LETTERS.length
  if (letter === '#') return AZ_LETTERS.length + 1
  return AZ_LETTERS.indexOf(letter)
}

function AZIndex({ letters, onSelect, activeLetter }: {
  letters: string[]
  onSelect: (letter: string | null) => void
  activeLetter: string | null
}) {
  const t = useT()
  return (
    <>
      {letters.map((letter) => (
        <button
          key={letter}
          type="button"
          onClick={() => onSelect(activeLetter === letter ? null : letter)}
          aria-pressed={activeLetter === letter}
          aria-label={t('common.filterBy', { x: letter })}
        >
          {letter}
        </button>
      ))}
    </>
  )
}

function RecipeRow({ recipe, cookbook, onRemove, editMode }: {
  recipe: Recipe
  cookbook: Cookbook
  onRemove: (recipeId: string) => void
  editMode: boolean
}) {
  const t = useT()
  const imageUrl = getFirstImageUrl(recipe)
  const time = formatRecipeTime(totalRecipeTime(recipe), t)

  return (
    <Link to={`/recipes/${recipe.id}`} className="link-plain">
      <div className="entry">
        <div className="entry-thumb">
          {imageUrl ? (
            <WashedImage
              src={imageUrl}
              alt={recipe.title}
              className="thumb thumb-48"
            />
          ) : (
            <RecipeFallbackImage className="thumb thumb-48" />
          )}
        </div>

        <div className="entry-body">
          <div className="entry-meta">
            <span>{cookbook.name}</span>
            <span>|</span>
            <span>{recipe.category}</span>
          </div>
          <h4 className="entry-title">{recipe.title}</h4>
          <div className="entry-stats">
            <span>{t('common.ingredientsCount', { count: recipe.ingredients.length })}</span>
            {time && <span>{time}</span>}
            {recipe.servingSize && <span>{t('common.servingsCount', { count: recipe.servingSize })}</span>}
          </div>
        </div>

        {editMode && (
          <button
            type="button"
            className="btn btn-ghost btn-icon flex-none"
            onClick={(e) => {
              e.preventDefault()
              e.stopPropagation()
              onRemove(recipe.id)
            }}
            aria-label={t('cookbookDetail.removeAria', { title: recipe.title })}
          >
            <X size={16} strokeWidth={2.75} />
          </button>
        )}
      </div>
    </Link>
  )
}

export function CookbookDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const t = useT()
  const { recipes } = useRecipes()
  const userName = useAppStore((s) => s.userName)
  const [cookbook, setCookbook] = useState<Cookbook | null>(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [filterLetter, setFilterLetter] = useState<string | null>(null)
  const [filterCategory, setFilterCategory] = useState<string | null>(null)
  const deleteDialog = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    if (!id) return
    let active = true
    getCookbook(id).then((cb) => {
      if (!active) return
      setCookbook(cb)
      if (cb) {
        setName(cb.name)
        setDescription(cb.description ?? '')
      }
      setLoading(false)
    })
    return () => { active = false }
  }, [id])

  if (loading) return <main className="page"><p className="text-muted">{t('common.loading')}</p></main>
  if (!cookbook) return <main className="page"><p className="text-muted">{t('cookbookDetail.notFound')}</p></main>

  async function refresh() {
    if (!id) return
    const cb = await getCookbook(id)
    if (cb) setCookbook(cb)
  }

  async function onRename() {
    if (!id || !name.trim()) return
    await renameCookbook(id, name.trim(), description.trim(), getDeviceId())
    setEditing(false)
    await refresh()
  }

  function onDelete() {
    deleteDialog.current?.showModal()
  }

  // Recipes go first: if one fails, the cookbook and its remaining recipes
  // stay intact and the delete can be retried.
  async function confirmDelete(deleteRecipesToo: boolean) {
    if (!id || !cookbook) return
    deleteDialog.current?.close()
    const deviceId = getDeviceId()
    if (deleteRecipesToo) {
      for (const recipeId of cookbook.recipeIds) {
        await deleteRecipe(recipeId, deviceId)
      }
    }
    await deleteCookbook(id, deviceId)
    navigate('/cookbooks', { replace: true })
  }

  async function onRemove(recipeId: string) {
    if (!id) return
    const { removeRecipeFromCookbook } = await import('../features/cookbook/repository')
    await removeRecipeFromCookbook(id, recipeId, getDeviceId())
    await refresh()
  }

  const members: Recipe[] = recipes.filter((r) => cookbook.recipeIds.includes(r.id))
  const owner = ownerDisplayName(cookbook, getDeviceId(), userName)

  // Categories in this cookbook; picking one narrows what the index shows.
  const categories = [...new Set(members.map((r) => r.category))].sort()
  const afterCategory = filterCategory
    ? members.filter((r) => r.category === filterCategory)
    : members
  const letters = [...new Set(afterCategory.map((r) => indexLetter(r.title)))]
    .sort((a, b) => indexRank(a) - indexRank(b))
  const filteredMembers = filterLetter
    ? afterCategory.filter((r) => indexLetter(r.title) === filterLetter)
    : afterCategory

  function selectCategory(c: string | null) {
    setFilterCategory(c)
    setFilterLetter(null)
  }

  return (
    <main className="page">
      <div className="cookbook-layout">
        <aside className="az-rail" aria-label={t('cookbookDetail.indexAria')}>
          <AZIndex letters={letters} onSelect={setFilterLetter} activeLetter={filterLetter} />
        </aside>

        <div className="cookbook-content">
          <div className="row mb-2">
            <Link to="/cookbooks" className="btn btn-ghost btn-icon" aria-label={t('common.back')}>
              <ArrowLeft strokeWidth={2.75} />
            </Link>
            <div className="row-spacer" />
            {editing ? (
              <>
                <Button variant="primary" onClick={onRename} disabled={!name.trim()} aria-label={t('cookbookDetail.saveAria')}>
                  <Check size={18} strokeWidth={2.75} />
                </Button>
                <Button variant="ghost" onClick={() => { setEditing(false); setName(cookbook.name); setDescription(cookbook.description ?? '') }} aria-label={t('cookbookDetail.cancelEditAria')}>
                  <X size={18} strokeWidth={2.75} />
                </Button>
              </>
            ) : (
              <>
                <Button variant="secondary" onClick={() => setEditing(true)} aria-label={t('cookbookDetail.editAria')}>
                  <Pencil size={18} strokeWidth={2.75} />
                </Button>
                <Button variant="ghost" onClick={onDelete} aria-label={t('cookbookDetail.deleteAria')}>
                  <Trash2 size={18} strokeWidth={2.75} />
                </Button>
              </>
            )}
          </div>

          <div className="recipe-hero">
            {editing ? (
              <div className="stack">
                <div className="form-row">
                  <input
                    className="input input-heading"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    autoFocus
                    aria-label={t('cookbookDetail.nameAria')}
                  />
                </div>
                <div className="form-row">
                  <textarea
                    className="input input-md"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder={t('cookbookDetail.descPlaceholder')}
                    aria-label={t('cookbookDetail.descAria')}
                  />
                </div>
              </div>
            ) : (
              <>
                <h1 className="mb-1">{cookbook.name}</h1>
                {cookbook.description && <p className="text-muted mb-0">{cookbook.description}</p>}
                <div className="cookbook-meta">
                  <span>{t('cookbooks.owner', { owner })}</span>
                  <span>·</span>
                  <strong>{t('common.recipeCount', { count: members.length })}</strong>
                </div>
              </>
            )}
          </div>

          {/* Category filter and add-recipe share one row — chips left, a
              large + on the right — so the button never pushes content around.
              The index drills down within the chosen category. */}
          {!editing && (
            <div className="row mb-4">
              {categories.length > 0 && (
                <div className="chip-row">
                  <button
                    type="button"
                    className="chip"
                    onClick={() => selectCategory(null)}
                    aria-pressed={filterCategory == null}
                  >
                    {t('common.all')}
                  </button>
                  {categories.map((c) => (
                    <button
                      key={c}
                      type="button"
                      className="chip"
                      onClick={() => selectCategory(filterCategory === c ? null : c)}
                      aria-pressed={filterCategory === c}
                    >
                      {c}
                    </button>
                  ))}
                </div>
              )}
              <div className="row-spacer" />
              <Link
                to={`/recipes/new?cookbookId=${cookbook.id}`}
                className="btn btn-primary btn-icon btn-icon-lg"
                aria-label={t('cookbookDetail.addRecipeAria')}
              >
                <Plus size={24} strokeWidth={2.75} />
              </Link>
            </div>
          )}

          {filterLetter && (
            <div className="row mb-2">
              <Tag variant="accent">{t('cookbookDetail.filterTag', { letter: filterLetter })}</Tag>
              <button type="button" className="btn btn-ghost btn-icon" onClick={() => setFilterLetter(null)} aria-label={t('cookbookDetail.clearLetterAria')}>
                <X size={16} strokeWidth={2.75} />
              </button>
            </div>
          )}

          <div>
            {filteredMembers.length === 0 ? (
              <p className="text-muted">
                {filterLetter
                  ? t('cookbookDetail.noLetter', { letter: filterLetter })
                  : members.length === 0
                    ? t('cookbookDetail.noRecipesYet')
                    : t('cookbookDetail.noInCategory')}
              </p>
            ) : (
              filteredMembers.map((r, index) => (
                <div key={r.id}>
                  {index > 0 && <hr className="entry-divider entry-divider-sm" />}
                  <RecipeRow recipe={r} cookbook={cookbook} onRemove={onRemove} editMode={editing} />
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <dialog
        ref={deleteDialog}
        className="dialog"
        aria-labelledby="delete-cookbook-title"
        onClick={(e) => {
          // Clicks on the ::backdrop retarget to the dialog element; only
          // dismiss when the click lands outside the card itself.
          const dlg = deleteDialog.current
          if (!dlg) return
          const rect = dlg.getBoundingClientRect()
          const onBackdrop =
            e.clientX < rect.left || e.clientX > rect.right ||
            e.clientY < rect.top || e.clientY > rect.bottom
          if (onBackdrop) dlg.close()
        }}
      >
        <div className="dialog-title" id="delete-cookbook-title">
          {t('cookbookDetail.deleteTitle', { name: cookbook.name })}
        </div>
        <p className="dialog-body">
          {t('cookbookDetail.deleteBody', {
            count: t('common.recipeCount', { count: cookbook.recipeIds.length }),
          })}
        </p>
        <div className="dialog-actions">
          <Button variant="ghost" onClick={() => deleteDialog.current?.close()}>{t('common.cancel')}</Button>
          <Button variant="secondary" onClick={() => confirmDelete(false)}>{t('cookbookDetail.keepRecipes')}</Button>
          <Button variant="primary" onClick={() => confirmDelete(true)}>{t('cookbookDetail.deleteThemToo')}</Button>
        </div>
      </dialog>
    </main>
  )
}
