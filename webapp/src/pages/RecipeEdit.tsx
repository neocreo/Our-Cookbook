// RecipeEdit — create or edit a recipe. Edits an existing recipe when an id is
// present in the route, otherwise creates a new one. On save the repository
// creates an auto-cookbook ("Mina recept") if the user has none yet.

import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Plus, X } from 'lucide-react'
import { Button } from '../components/Button'
import { getRecipe, saveRecipe } from '../features/recipe/repository'
import { getDeviceId } from '../lib/device'
import { createIngredient } from '../types/ingredient'
import type { Ingredient } from '../types/ingredient'
import type { Recipe, RecipeInput } from '../types/recipe'

interface IngredientRow {
  id: string
  amount: string
  unit: string
  name: string
}

export function RecipeEdit() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const [title, setTitle] = useState('')
  const [category, setCategory] = useState('')
  const [description, setDescription] = useState('')
  const [source, setSource] = useState('')
  const [servings, setServings] = useState('')
  const [prepTime, setPrepTime] = useState('')
  const [cookTime, setCookTime] = useState('')
  const [tags, setTags] = useState('')
  const [notes, setNotes] = useState('')
  const [ingredients, setIngredients] = useState<IngredientRow[]>([{ id: crypto.randomUUID(), amount: '', unit: '', name: '' }])
  const [instructions, setInstructions] = useState<string[]>([''])
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!id) return
    let active = true
    getRecipe(id).then((r: Recipe | null) => {
      if (!active || !r) return
      setTitle(r.title)
      setCategory(r.category)
      setDescription(r.description ?? '')
      setSource(r.source ?? '')
      setServings(r.servingSize != null ? String(r.servingSize) : '')
      setPrepTime(r.prepTime != null ? String(r.prepTime) : '')
      setCookTime(r.cookTime != null ? String(r.cookTime) : '')
      setTags(r.tags.join(', '))
      setNotes(r.notes ?? '')
      setIngredients(
        r.ingredients.length
          ? r.ingredients.map((i) => ({ id: i.id, amount: i.amount ?? '', unit: i.unit ?? '', name: i.name }))
          : [{ id: crypto.randomUUID(), amount: '', unit: '', name: '' }],
      )
      setInstructions(r.instructions.length ? r.instructions : [''])
      setLoading(false)
    })
    return () => {
      active = false
    }
  }, [id])

  function updateIngredient(rowId: string, field: keyof IngredientRow, value: string) {
    setIngredients((rows) => rows.map((r) => (r.id === rowId ? { ...r, [field]: value } : r)))
  }
  function addIngredient() {
    setIngredients((rows) => [...rows, { id: crypto.randomUUID(), amount: '', unit: '', name: '' }])
  }
  function removeIngredient(rowId: string) {
    setIngredients((rows) => (rows.length > 1 ? rows.filter((r) => r.id !== rowId) : rows))
  }

  function updateInstruction(idx: number, value: string) {
    setInstructions((lines) => lines.map((l, i) => (i === idx ? value : l)))
  }
  function addInstruction() {
    setInstructions((lines) => [...lines, ''])
  }
  function removeInstruction(idx: number) {
    setInstructions((lines) => (lines.length > 1 ? lines.filter((_, i) => i !== idx) : lines))
  }

  async function onSave() {
    if (!title.trim() || !category.trim()) {
      window.alert('Title and category are required.')
      return
    }
    setSaving(true)
    const builtIngredients: Ingredient[] = ingredients
      .filter((r) => r.name.trim())
      .map((r, i) =>
        createIngredient(r.name.trim(), r.amount.trim() || null, r.unit.trim() || null, null, i),
      )
    const builtInstructions = instructions.map((s) => s.trim()).filter(Boolean)
    const input: RecipeInput = {
      title: title.trim(),
      category: category.trim(),
      description: description.trim() || null,
      source: source.trim() || null,
      servingSize: servings.trim() ? Number(servings) : null,
      prepTime: prepTime.trim() ? Number(prepTime) : null,
      cookTime: cookTime.trim() ? Number(cookTime) : null,
      tags: tags.split(',').map((t) => t.trim()).filter(Boolean),
      notes: notes.trim() || null,
      ingredients: builtIngredients,
      instructions: builtInstructions,
    }

    let existing: Recipe | null = null
    if (id) existing = await getRecipe(id)
    const saved = await saveRecipe(existing ?? input, getDeviceId())
    setSaving(false)
    navigate(`/recipes/${saved.id}`, { replace: true })
  }

  if (loading) return <main className="page"><p className="text-muted">Loading…</p></main>

  return (
    <main className="page">
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <button
          type="button"
          className="btn btn-ghost btn-icon"
          onClick={() => navigate(-1)}
          aria-label="Back"
        >
          <ArrowLeft strokeWidth={2.75} />
        </button>
      </div>

      <h1 className="page-title">{isEdit ? 'Edit recipe' : 'New recipe'}</h1>

      <div className="form-row">
        <label htmlFor="title">Title</label>
        <input id="title" className="input" value={title} onChange={(e) => setTitle(e.target.value)} />
      </div>
      <div className="form-row">
        <label htmlFor="category">Category</label>
        <input id="category" className="input" value={category} onChange={(e) => setCategory(e.target.value)} placeholder="e.g. Dinner" />
      </div>
      <div className="form-row">
        <label htmlFor="description">Description</label>
        <textarea id="description" className="input" value={description} onChange={(e) => setDescription(e.target.value)} />
      </div>

      <div className="form-row">
        <label>Ingredients</label>
        <div className="line-list">
          {ingredients.map((row) => (
            <div className="line-row" key={row.id}>
              <input
                className="input"
                placeholder="amount"
                value={row.amount}
                onChange={(e) => updateIngredient(row.id, 'amount', e.target.value)}
                style={{ maxWidth: 80 }}
              />
              <input
                className="input"
                placeholder="unit"
                value={row.unit}
                onChange={(e) => updateIngredient(row.id, 'unit', e.target.value)}
                style={{ maxWidth: 80 }}
              />
              <input
                className="input"
                placeholder="name"
                value={row.name}
                onChange={(e) => updateIngredient(row.id, 'name', e.target.value)}
              />
              <button type="button" className="btn btn-ghost btn-icon" onClick={() => removeIngredient(row.id)} aria-label="Remove ingredient">
                <X size={18} strokeWidth={2.75} />
              </button>
            </div>
          ))}
        </div>
        <Button variant="secondary" onClick={addIngredient}>
          <Plus size={16} strokeWidth={2.75} /> Add ingredient
        </Button>
      </div>

      <div className="form-row">
        <label>Instructions</label>
        <div className="line-list">
          {instructions.map((line, idx) => (
            <div className="line-row" key={idx}>
              <input
                className="input"
                placeholder={`Step ${idx + 1}`}
                value={line}
                onChange={(e) => updateInstruction(idx, e.target.value)}
              />
              <button type="button" className="btn btn-ghost btn-icon" onClick={() => removeInstruction(idx)} aria-label="Remove step">
                <X size={18} strokeWidth={2.75} />
              </button>
            </div>
          ))}
        </div>
        <Button variant="secondary" onClick={addInstruction}>
          <Plus size={16} strokeWidth={2.75} /> Add step
        </Button>
      </div>

      <div className="form-row">
        <label htmlFor="servings">Servings</label>
        <input id="servings" className="input" type="number" value={servings} onChange={(e) => setServings(e.target.value)} />
      </div>
      <div className="form-row">
        <label htmlFor="prep">Prep time (min)</label>
        <input id="prep" className="input" type="number" value={prepTime} onChange={(e) => setPrepTime(e.target.value)} />
      </div>
      <div className="form-row">
        <label htmlFor="cook">Cook time (min)</label>
        <input id="cook" className="input" type="number" value={cookTime} onChange={(e) => setCookTime(e.target.value)} />
      </div>
      <div className="form-row">
        <label htmlFor="tags">Tags (comma-separated)</label>
        <input id="tags" className="input" value={tags} onChange={(e) => setTags(e.target.value)} />
      </div>
      <div className="form-row">
        <label htmlFor="source">Source</label>
        <input id="source" className="input" value={source} onChange={(e) => setSource(e.target.value)} />
      </div>
      <div className="form-row">
        <label htmlFor="notes">Notes</label>
        <textarea id="notes" className="input" value={notes} onChange={(e) => setNotes(e.target.value)} />
      </div>

      <div className="form-actions">
        <Button variant="secondary" onClick={() => navigate(-1)} disabled={saving}>
          Cancel
        </Button>
        <Button variant="primary" onClick={onSave} disabled={saving}>
          {saving ? 'Saving…' : isEdit ? 'Save changes' : 'Add recipe'}
        </Button>
      </div>
    </main>
  )
}
