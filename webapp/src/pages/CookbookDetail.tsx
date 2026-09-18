// CookbookDetail — shows the recipes in a cookbook with A-Z filtering.
// Supports in-place editing of title and description, and adding new recipes.

import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Plus, Trash2, X, BookOpen } from 'lucide-react'
import { Button } from '../components/Button'
import { WashedImage } from '../components/WashedImage'
import { Tag } from '../components/Tag'
import { getCookbook, deleteCookbook, renameCookbook } from '../features/cookbook/repository'
import { useRecipes } from '../hooks/useRecipes'
import { getFirstImageUrl, totalRecipeTime } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'
import type { Recipe } from '../types/recipe'

// A-Z letters including Swedish letters
const AZ_LETTERS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Å', 'Ä', 'Ö']

function formatTime(minutes: number | null): string | null {
  if (minutes == null) return null
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h} hr ${m} min` : `${h} hr`
}

function AZIndex({ onSelect, activeLetter }: { onSelect: (letter: string | null) => void; activeLetter: string | null }) {
  return (
    <div
      style={{
        position: 'fixed',
        left: 16,
        top: '50%',
        transform: 'translateY(-50%)',
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        zIndex: 10,
      }}
    >
      {AZ_LETTERS.map((letter) => (
        <button
          key={letter}
          type="button"
          onClick={() => onSelect(activeLetter === letter ? null : letter)}
          style={{
            width: 28,
            height: 28,
            borderRadius: '50%',
            border: 'none',
            background: activeLetter === letter ? 'var(--color-accent)' : 'var(--color-surface-3)',
            color: activeLetter === letter ? 'var(--color-accent-contrast)' : 'var(--color-text)',
            cursor: 'pointer',
            fontSize: 13,
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
          aria-label={`Filter by ${letter}`}
        >
          {letter}
        </button>
      ))}
    </div>
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
  const [cookbook, setCookbook] = useState<Cookbook | null>(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [filterLetter, setFilterLetter] = useState<string | null>(null)

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
    await renameCookbook(id, name.trim(), description.trim())
    setEditing(false)
    await refresh()
  }

  async function onDelete() {
    if (!id) return
    if (!window.confirm(`Delete "${cookbook?.name}"? Recipes are not deleted.`)) return
    await deleteCookbook(id)
    navigate('/cookbooks', { replace: true })
  }

  async function onRemove(recipeId: string) {
    if (!id) return
    const { removeRecipeFromCookbook } = await import('../features/cookbook/repository')
    await removeRecipeFromCookbook(id, recipeId)
    await refresh()
  }

  const members: Recipe[] = recipes.filter((r) => cookbook.recipeIds.includes(r.id))
  const filteredMembers = filterLetter
    ? members.filter((r) => r.title.toUpperCase().startsWith(filterLetter))
    : members
  const ownerName = `Device: ${cookbook.ownerDeviceId.substring(0, 8)}...`

  return (
    <main className="page">
      <AZIndex onSelect={setFilterLetter} activeLetter={filterLetter} />

      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <Link to="/cookbooks" className="btn btn-ghost btn-icon" aria-label="Back">
          <ArrowLeft strokeWidth={2.75} />
        </Link>
        <div style={{ flex: 1 }} />
        {editing ? (
          <>
            <Button variant="primary" onClick={onRename} disabled={!name.trim()}>
              <Pencil size={16} strokeWidth={2.75} /> Save
            </Button>
            <Button variant="ghost" onClick={() => { setEditing(false); setName(cookbook.name); setDescription(cookbook.description ?? '') }}>
              <X size={16} strokeWidth={2.75} /> Cancel
            </Button>
          </>
        ) : (
          <>
            <Button variant="secondary" onClick={() => setEditing(true)}>
              <Pencil size={16} strokeWidth={2.75} /> Edit
            </Button>
            <Button variant="ghost" onClick={onDelete}>
              <Trash2 size={16} strokeWidth={2.75} /> Delete
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
              />
            </div>
            <div className="form-row">
              <textarea
                className="input"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                style={{ fontSize: 14, minHeight: 60 }}
                placeholder="Cookbook description..."
              />
            </div>
          </div>
        ) : (
          <>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
              <h1 style={{ margin: 0 }}>{cookbook.name}</h1>
              <Tag variant="neutral">{`${members.length} recipe${members.length === 1 ? '' : 's'}`}</Tag>
            </div>
            {cookbook.description && <p className="text-muted" style={{ margin: '8px 0 0' }}>{cookbook.description}</p>}
            <div style={{ marginTop: 8, fontSize: 13, color: 'var(--color-text-muted)' }}>
              <span>Owner: {ownerName}</span>
            </div>
          </>
        )}
      </div>

      <div style={{ marginBottom: 16 }}>
        <Link to={`/recipes/new?cookbookId=${cookbook.id}`} className="btn btn-primary">
          <Plus size={16} strokeWidth={2.75} /> Add Recipe
        </Link>
      </div>

      {filterLetter && (
        <div style={{ marginBottom: 8, display: 'flex', alignItems: 'center', gap: 8 }}>
          <Tag variant="accent">{`Filter: ${filterLetter}`}</Tag>
          <button type="button" className="btn btn-ghost btn-icon" onClick={() => setFilterLetter(null)} aria-label="Clear">
            <X size={16} strokeWidth={2.75} />
          </button>
        </div>
      )}

      <div>
        {filteredMembers.length === 0 ? (
          <p className="text-muted">
            {filterLetter ? `No recipes starting with "${filterLetter}".` : 'No recipes in this cookbook yet.'}
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
    </main>
  )
}
