// CookbookDetail — shows the recipes in a cookbook. Rename, delete the
// cookbook, and add/remove recipes (via an "add recipe" picker that lists all
// recipes not yet in the cookbook).

import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Plus, Trash2, X } from 'lucide-react'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { Tag } from '../components/Tag'
import { getCookbook, deleteCookbook, renameCookbook, addRecipeToCookbook, removeRecipeFromCookbook } from '../features/cookbook/repository'
import { useRecipes } from '../hooks/useRecipes'
import type { Cookbook } from '../types/cookbook'
import type { Recipe } from '../types/recipe'

export function CookbookDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { recipes } = useRecipes()
  const [cookbook, setCookbook] = useState<Cookbook | null>(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState('')
  const [adding, setAdding] = useState(false)

  useEffect(() => {
    if (!id) return
    let active = true
    getCookbook(id).then((cb) => {
      if (!active) return
      setCookbook(cb)
      setName(cb?.name ?? '')
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
    await renameCookbook(id, name.trim())
    setEditing(false)
    await refresh()
  }

  async function onDelete() {
    if (!id) return
    if (!window.confirm(`Delete the cookbook "${cookbook?.name}"? The recipes themselves are not deleted.`)) return
    await deleteCookbook(id)
    navigate('/cookbooks', { replace: true })
  }

  async function onAdd(recipeId: string) {
    if (!id) return
    await addRecipeToCookbook(id, recipeId)
    await refresh()
  }

  async function onRemove(recipeId: string) {
    if (!id) return
    await removeRecipeFromCookbook(id, recipeId)
    await refresh()
  }

  const inCookbook = cookbook.recipeIds
  const members: Recipe[] = recipes.filter((r) => inCookbook.includes(r.id))
  const candidates: Recipe[] = recipes.filter((r) => !inCookbook.includes(r.id))

  return (
    <main className="page">
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <Link to="/cookbooks" className="btn btn-ghost btn-icon" aria-label="Back">
          <ArrowLeft strokeWidth={2.75} />
        </Link>
      </div>

      <div className="recipe-hero">
        {editing ? (
          <div className="form-row">
            <input className="input" value={name} onChange={(e) => setName(e.target.value)} autoFocus />
            <div className="form-actions">
              <Button variant="secondary" onClick={() => { setEditing(false); setName(cookbook.name) }}>
                Cancel
              </Button>
              <Button variant="primary" onClick={onRename} disabled={!name.trim()}>
                Save
              </Button>
            </div>
          </div>
        ) : (
          <>
            <h1>{cookbook.name}</h1>
            {cookbook.description && <p className="text-muted">{cookbook.description}</p>}
            <div className="card-meta" style={{ marginTop: 4 }}>
              <Tag variant="accent-2">{`${members.length} recipe${members.length === 1 ? '' : 's'}`}</Tag>
            </div>
          </>
        )}
      </div>

      {!editing && (
        <div className="form-actions">
          <Button variant="secondary" onClick={() => setEditing(true)}>
            <Pencil size={16} strokeWidth={2.75} /> Rename
          </Button>
          <Button variant="ghost" onClick={onDelete}>
            <Trash2 size={16} strokeWidth={2.75} /> Delete
          </Button>
        </div>
      )}

      <section className="recipe-section">
        <h3>Recipes</h3>
        {members.length === 0 ? (
          <p className="text-muted">No recipes in this cookbook yet.</p>
        ) : (
          <div className="stack">
            {members.map((r) => (
              <div key={r.id} className="line-row" style={{ alignItems: 'center' }}>
                <Link to={`/recipes/${r.id}`} style={{ textDecoration: 'none', color: 'inherit', flex: 1 }}>
                  <Card as="article" elevation="sm" kicker={r.category} title={r.title}>
                    {r.description ?? `${r.ingredients.length} ingredients`}
                  </Card>
                </Link>
                <button
                  type="button"
                  className="btn btn-ghost btn-icon"
                  onClick={() => onRemove(r.id)}
                  aria-label={`Remove ${r.title} from cookbook`}
                >
                  <X size={18} strokeWidth={2.75} />
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

      {adding && candidates.length > 0 && (
        <section className="recipe-section">
          <h3>Add a recipe</h3>
          <div className="stack">
            {candidates.map((r) => (
              <div key={r.id} className="line-row" style={{ alignItems: 'center' }}>
                <span style={{ flex: 1 }}>{r.title} <span className="text-muted">· {r.category}</span></span>
                <button
                  type="button"
                  className="btn btn-ghost btn-icon"
                  onClick={() => onAdd(r.id)}
                  aria-label={`Add ${r.title} to cookbook`}
                >
                  <Plus size={18} strokeWidth={2.75} />
                </button>
              </div>
            ))}
          </div>
        </section>
      )}

      <button
        type="button"
        className="fab"
        aria-label="Add recipe to cookbook"
        onClick={() => setAdding((v) => !v)}
        disabled={candidates.length === 0}
      >
        <Plus strokeWidth={2.75} />
      </button>
    </main>
  )
}
