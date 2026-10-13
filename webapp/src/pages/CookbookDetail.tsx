// CookbookDetail — shows the recipes in a cookbook with an A-Ö index rail and
// category filter. Supports in-place editing of title and description.

import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Plus, Trash2, X, BookOpen, Check } from 'lucide-react'
import { Button } from '../components/Button'
import { WashedImage } from '../components/WashedImage'
import { Tag } from '../components/Tag'
import { getCookbook, deleteCookbook, renameCookbook } from '../features/cookbook/repository'
import { deleteRecipe } from '../features/recipe/repository'
import { useRecipes } from '../hooks/useRecipes'
import { getDeviceId } from '../lib/device'
import { useAppStore } from '../stores/appStore'
import { ownerDisplayName, type Cookbook } from '../types/cookbook'
import { getFirstImageUrl, totalRecipeTime } from '../types/recipe'
import type { Recipe } from '../types/recipe'

// A-Ö including Swedish letters
const AZ_LETTERS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Å', 'Ä', 'Ö']

function formatTime(minutes: number | null): string | null {
  if (minutes == null) return null
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h} hr ${m} min` : `${h} hr`
}

// The index letter a title sorts under; titles outside A-Ö fall under '#'.
function indexLetter(title: string): string {
  const first = title.trim().toUpperCase().charAt(0)
  return AZ_LETTERS.includes(first) ? first : '#'
}

function AZIndex({ letters, onSelect, activeLetter }: {
  letters: string[]
  onSelect: (letter: string | null) => void
  activeLetter: string | null
}) {
  return (
    <>
      {letters.map((letter) => (
        <button
          key={letter}
          type="button"
          onClick={() => onSelect(activeLetter === letter ? null : letter)}
          aria-pressed={activeLetter === letter}
          aria-label={`Filter by ${letter}`}
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
  const imageUrl = getFirstImageUrl(recipe)
  const time = formatTime(totalRecipeTime(recipe))

  return (
    <div className="entry">
      <div className="entry-thumb">
        {imageUrl ? (
          <WashedImage
            src={imageUrl}
            alt={recipe.title}
            className="thumb thumb-48"
          />
        ) : (
          <div className="thumb-placeholder thumb-48">
            <BookOpen size={20} className="icon-muted" strokeWidth={2.75} />
          </div>
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
          <span>{recipe.ingredients.length} ingredients</span>
          {time && <span>{time}</span>}
          {recipe.servingSize && <span>{recipe.servingSize} servings</span>}
        </div>
      </div>

      {editMode && (
        <button
          type="button"
          className="btn btn-ghost btn-icon flex-none"
          onClick={() => onRemove(recipe.id)}
          aria-label={`Remove ${recipe.title}`}
        >
          <X size={16} strokeWidth={2.75} />
        </button>
      )}
    </div>
  )
}

export function CookbookDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
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

  if (loading) return <main className="page"><p className="text-muted">Loading…</p></main>
  if (!cookbook) return <main className="page"><p className="text-muted">Cookbook not found.</p></main>

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
    .sort((a, b) => {
      if (a === '#') return 1
      if (b === '#') return -1
      return AZ_LETTERS.indexOf(a) - AZ_LETTERS.indexOf(b)
    })
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
        <aside className="az-rail" aria-label="Recipe index">
          <AZIndex letters={letters} onSelect={setFilterLetter} activeLetter={filterLetter} />
        </aside>

        <div className="cookbook-content">
          <div className="row mb-2">
            <Link to="/cookbooks" className="btn btn-ghost btn-icon" aria-label="Back">
              <ArrowLeft strokeWidth={2.75} />
            </Link>
            <div className="row-spacer" />
            {editing ? (
              <>
                <Button variant="primary" onClick={onRename} disabled={!name.trim()} aria-label="Save changes">
                  <Check size={18} strokeWidth={2.75} />
                </Button>
                <Button variant="ghost" onClick={() => { setEditing(false); setName(cookbook.name); setDescription(cookbook.description ?? '') }} aria-label="Cancel editing">
                  <X size={18} strokeWidth={2.75} />
                </Button>
              </>
            ) : (
              <>
                <Button variant="secondary" onClick={() => setEditing(true)} aria-label="Edit cookbook">
                  <Pencil size={18} strokeWidth={2.75} />
                </Button>
                <Button variant="ghost" onClick={onDelete} aria-label="Delete cookbook">
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
                    aria-label="Cookbook name"
                  />
                </div>
                <div className="form-row">
                  <textarea
                    className="input input-md"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Cookbook description..."
                    aria-label="Cookbook description"
                  />
                </div>
              </div>
            ) : (
              <>
                <h1 className="mb-1">{cookbook.name}</h1>
                {cookbook.description && <p className="text-muted mb-0">{cookbook.description}</p>}
                <div className="cookbook-meta">
                  <span>Owner: {owner}</span>
                  <span>·</span>
                  <strong>{`${members.length} recipe${members.length === 1 ? '' : 's'}`}</strong>
                </div>
              </>
            )}
          </div>

          {/* Category filter — under the title and owner info; the index then
              drills down within the chosen category. */}
          {!editing && categories.length > 0 && (
            <div className="chip-row mb-4">
              <button
                type="button"
                className="chip"
                onClick={() => selectCategory(null)}
                aria-pressed={filterCategory == null}
              >
                All
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

          {!editing && (
            <div className="mb-4">
              <Link to={`/recipes/new?cookbookId=${cookbook.id}`} className="btn btn-primary">
                <Plus size={16} strokeWidth={2.75} /> Add Recipe
              </Link>
            </div>
          )}

          {filterLetter && (
            <div className="row mb-2">
              <Tag variant="accent">{`Filter: ${filterLetter}`}</Tag>
              <button type="button" className="btn btn-ghost btn-icon" onClick={() => setFilterLetter(null)} aria-label="Clear letter filter">
                <X size={16} strokeWidth={2.75} />
              </button>
            </div>
          )}

          <div>
            {filteredMembers.length === 0 ? (
              <p className="text-muted">
                {filterLetter
                  ? `No recipes starting with "${filterLetter}".`
                  : members.length === 0
                    ? 'No recipes in this cookbook yet.'
                    : 'No recipes in this category.'}
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
        <div className="dialog-title" id="delete-cookbook-title">Delete "{cookbook.name}"?</div>
        <p className="dialog-body">
          The cookbook contains {cookbook.recipeIds.length} recipe{cookbook.recipeIds.length === 1 ? '' : 's'}.
          Choose whether to delete them as well — this cannot be undone.
        </p>
        <div className="dialog-actions">
          <Button variant="ghost" onClick={() => deleteDialog.current?.close()}>Cancel</Button>
          <Button variant="secondary" onClick={() => confirmDelete(false)}>Keep the recipes</Button>
          <Button variant="primary" onClick={() => confirmDelete(true)}>Delete them too</Button>
        </div>
      </dialog>
    </main>
  )
}
