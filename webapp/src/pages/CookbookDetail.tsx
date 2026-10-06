// CookbookDetail — shows the recipes in a cookbook with an A-Ö index rail and
// category filter. Supports in-place editing of title and description.

import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Plus, Trash2, X, BookOpen, Check } from 'lucide-react'
import { Button } from '../components/Button'
import { WashedImage } from '../components/WashedImage'
import { Tag } from '../components/Tag'
import { getCookbook, deleteCookbook, renameCookbook } from '../features/cookbook/repository'
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
    <div
      style={{
        display: 'flex',
        gap: 12,
        padding: '12px 0',
        alignItems: 'flex-start',
      }}
    >
      <div style={{ flexShrink: 0 }}>
        {imageUrl ? (
          <WashedImage
            src={imageUrl}
            alt={recipe.title}
            style={{ width: 48, height: 48, borderRadius: 'var(--radius-md)' }}
          />
        ) : (
          <div
            style={{
              width: 48,
              height: 48,
              background: 'var(--color-surface-3)',
              borderRadius: 'var(--radius-md)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <BookOpen size={20} style={{ opacity: 0.5 }} strokeWidth={2.75} />
          </div>
        )}
      </div>

      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'baseline', marginBottom: 4 }}>
          <span style={{ fontSize: 13, color: 'var(--color-text-muted)' }}>{cookbook.name}</span>
          <span style={{ color: 'var(--color-text-muted)' }}>|</span>
          <span style={{ fontSize: 13, color: 'var(--color-text-muted)' }}>{recipe.category}</span>
        </div>
        <h4 style={{ margin: '0 0 4px', fontSize: 15, fontWeight: 600, color: 'var(--color-text)' }}>
          {recipe.title}
        </h4>
        <div style={{ display: 'flex', gap: 12, fontSize: 13, color: 'var(--color-text-muted)' }}>
          <span>{recipe.ingredients.length} ingredients</span>
          {time && <span>{time}</span>}
          {recipe.servingSize && <span>{recipe.servingSize} servings</span>}
        </div>
      </div>

      {editMode && (
        <button
          type="button"
          className="btn btn-ghost btn-icon"
          onClick={() => onRemove(recipe.id)}
          aria-label={`Remove ${recipe.title}`}
          style={{ flexShrink: 0 }}
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

  async function onDelete() {
    if (!id) return
    if (!window.confirm(`Delete "${cookbook?.name}"? Recipes are not deleted.`)) return
    await deleteCookbook(id, getDeviceId())
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
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
            <Link to="/cookbooks" className="btn btn-ghost btn-icon" aria-label="Back">
              <ArrowLeft strokeWidth={2.75} />
            </Link>
            <div style={{ flex: 1 }} />
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

          <div className="recipe-hero" style={{ marginBottom: 16 }}>
            {editing ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div className="form-row">
                  <input
                    className="input"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    style={{ fontSize: 24, fontWeight: 600, padding: 8 }}
                    autoFocus
                    aria-label="Cookbook name"
                  />
                </div>
                <div className="form-row">
                  <textarea
                    className="input"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    style={{ fontSize: 14, minHeight: 60 }}
                    placeholder="Cookbook description..."
                    aria-label="Cookbook description"
                  />
                </div>
              </div>
            ) : (
              <>
                <h1 style={{ marginBottom: 4 }}>{cookbook.name}</h1>
                {cookbook.description && <p className="text-muted" style={{ margin: 0 }}>{cookbook.description}</p>}
                <div style={{ marginTop: 8, fontSize: 13, color: 'var(--color-text-muted)', display: 'flex', alignItems: 'baseline', gap: 8 }}>
                  <span>Owner: {owner}</span>
                  <span>·</span>
                  <span style={{ fontWeight: 600 }}>{`${members.length} recipe${members.length === 1 ? '' : 's'}`}</span>
                </div>
              </>
            )}
          </div>

          {/* Category filter — under the title and owner info; the index then
              drills down within the chosen category. */}
          {!editing && categories.length > 0 && (
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 16 }}>
              <button
                type="button"
                onClick={() => selectCategory(null)}
                aria-pressed={filterCategory == null}
                style={{
                  padding: '6px 12px',
                  background: filterCategory == null ? 'var(--color-accent)' : 'var(--color-surface-3)',
                  color: filterCategory == null ? 'var(--color-accent-contrast)' : 'var(--color-text)',
                  border: 'none',
                  borderRadius: '999px',
                  cursor: 'pointer',
                  fontSize: 13,
                }}
              >
                All
              </button>
              {categories.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => selectCategory(filterCategory === c ? null : c)}
                  aria-pressed={filterCategory === c}
                  style={{
                    padding: '6px 12px',
                    background: filterCategory === c ? 'var(--color-accent)' : 'var(--color-surface-3)',
                    color: filterCategory === c ? 'var(--color-accent-contrast)' : 'var(--color-text)',
                    border: 'none',
                    borderRadius: '999px',
                    cursor: 'pointer',
                    fontSize: 13,
                  }}
                >
                  {c}
                </button>
              ))}
            </div>
          )}

          {!editing && (
            <div style={{ marginBottom: 16 }}>
              <Link to={`/recipes/new?cookbookId=${cookbook.id}`} className="btn btn-primary">
                <Plus size={16} strokeWidth={2.75} /> Add Recipe
              </Link>
            </div>
          )}

          {filterLetter && (
            <div style={{ marginBottom: 8, display: 'flex', alignItems: 'center', gap: 8 }}>
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
                  {index > 0 && (
                    <hr
                      style={{
                        margin: '0 0 0 60px',
                        border: 0,
                        borderTop: '1px solid var(--color-primary)',
                        opacity: 0.3,
                      }}
                    />
                  )}
                  <RecipeRow recipe={r} cookbook={cookbook} onRemove={onRemove} editMode={editing} />
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </main>
  )
}
