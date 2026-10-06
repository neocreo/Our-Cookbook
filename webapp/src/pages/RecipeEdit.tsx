// RecipeEdit — create or edit a recipe with edit-in-place layout.
// Supports drag-and-drop reordering of ingredients, multiple image uploads,
// and pre-selecting a cookbook.

import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { ArrowLeft, Plus, X, GripVertical } from 'lucide-react'
import { Button } from '../components/Button'
import { WashedImage } from '../components/WashedImage'
import { getRecipe, saveRecipe } from '../features/recipe/repository'
import { createCookbookNamed, addRecipeToCookbook, removeRecipeFromCookbook } from '../features/cookbook/repository'
import { getDeviceId } from '../lib/device'
import { createIngredient } from '../types/ingredient'
import { resizeImageToDataURI } from '../lib/imageUtils'
import { useCookbooks } from '../hooks/useCookbooks'
import { useAppStore } from '../stores/appStore'
import type { Ingredient } from '../types/ingredient'
import type { Recipe, RecipeInput } from '../types/recipe'

const UNITS = ['', 'tsp', 'tbsp', 'cup', 'cups', 'oz', 'lb', 'g', 'kg', 'ml', 'l', 'cloves', 'cans', 'slices', 'pieces', 'bunches', 'sticks']

const CREATE_NEW_COOKBOOK = '__create__'

export function RecipeEdit() {
  const { id } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const { cookbooks } = useCookbooks()

  const [title, setTitle] = useState('')
  const [category, setCategory] = useState('')
  const [description, setDescription] = useState('')
  const [source, setSource] = useState('')
  const [servings, setServings] = useState('')
  const [prepTime, setPrepTime] = useState('')
  const [cookTime, setCookTime] = useState('')
  const [tags, setTags] = useState<string[]>([])
  const [tagInput, setTagInput] = useState('')
  const [notes, setNotes] = useState('')
  const [imageUrls, setImageUrls] = useState<string[]>([])
  // Get initial cookbook from URL if present (for new recipes). Read straight
  // from the URL so it is correct even before the cookbooks list has loaded.
  const initialCookbookId = !isEdit ? searchParams.get('cookbookId') || '' : ''
  
  const [selectedCookbookId, setSelectedCookbookId] = useState<string | null>(initialCookbookId || null)
  const [creatingCookbook, setCreatingCookbook] = useState(false)
  const [newCookbookName, setNewCookbookName] = useState('')
  const [newCookbookDesc, setNewCookbookDesc] = useState('')
  const [ingredients, setIngredients] = useState<Ingredient[]>(!isEdit ? [createIngredient('', null, null, null, 0)] : [])
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null)
  const [instructions, setInstructions] = useState('')
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)

  const copyFromId = !isEdit ? searchParams.get('copyFrom') : null

  // Fill the form from a loaded recipe (edit mode or copy).
  function applyRecipeToForm(r: Recipe) {
    setTitle(r.title)
    setCategory(r.category)
    setDescription(r.description ?? '')
    setSource(r.source ?? '')
    setServings(r.servingSize != null ? String(r.servingSize) : '')
    setPrepTime(r.prepTime != null ? String(r.prepTime) : '')
    setCookTime(r.cookTime != null ? String(r.cookTime) : '')
    setTags(r.tags)
    setNotes(r.notes ?? '')
    setImageUrls(r.imageUrls ?? [])
    setIngredients(r.ingredients.length > 0 ? r.ingredients : [createIngredient('', null, null, null, 0)])
    setInstructions(r.instructions.join('\n'))
  }

  // Load recipe for edit
  useEffect(() => {
    if (!id) return
    let active = true
    getRecipe(id).then((r: Recipe | null) => {
      if (!active || !r) return
      applyRecipeToForm(r)
      setLoading(false)
    })
    return () => { active = false }
  }, [id])

  // Copy mode: prefill the form from the source recipe (saved as a new one)
  useEffect(() => {
    if (!copyFromId) return
    let active = true
    getRecipe(copyFromId).then((r: Recipe | null) => {
      if (!active || !r) return
      applyRecipeToForm(r)
    })
    return () => { active = false }
  }, [copyFromId])

  // Copy mode: default to the cookbook that holds the source recipe, until the
  // user picks another. Edit mode shows the recipe's current cookbook.
  // Derived here rather than set in an effect.
  const copyHolderId = copyFromId
    ? cookbooks.find((cb) => cb.recipeIds.includes(copyFromId))?.id
    : undefined
  const editHolderId = isEdit && id
    ? cookbooks.find((cb) => cb.recipeIds.includes(id))?.id
    : undefined
  const effectiveCookbookId = selectedCookbookId ?? copyHolderId ?? editHolderId ?? ''

  function handleDragStart(index: number) {
    setDraggedIndex(index)
  }

  function handleDragOver(_index: number, e: React.DragEvent) {
    e.preventDefault()
  }

  function handleDrop(targetIndex: number, e: React.DragEvent) {
    e.preventDefault()
    if (draggedIndex === null || draggedIndex === targetIndex) return
    setIngredients((prev) => {
      const newIngredients = [...prev]
      const [removed] = newIngredients.splice(draggedIndex, 1)
      newIngredients.splice(targetIndex, 0, removed)
      return newIngredients.map((ing, i) => ({ ...ing, order: i }))
    })
    setDraggedIndex(null)
  }

  function handleDragEnd() {
    setDraggedIndex(null)
  }

  function addIngredient() {
    setIngredients((prev) => [...prev, createIngredient('', null, null, null, prev.length)])
  }

  function removeIngredient(ingredientId: string) {
    setIngredients((prev) => {
      if (prev.length <= 1) return prev
      const filtered = prev.filter((ing) => ing.id !== ingredientId)
      return filtered.map((ing, i) => ({ ...ing, order: i }))
    })
  }

  function updateIngredient(id: string, field: keyof Ingredient, value: string | number | null) {
    setIngredients((prev) => prev.map((ing) => ing.id === id ? { ...ing, [field]: value } : ing))
  }

  function addTag() {
    if (!tagInput.trim()) return
    const newTag = tagInput.trim()
    if (!tags.includes(newTag)) {
      setTags([...tags, newTag])
    }
    setTagInput('')
  }

  function removeTag(tag: string) {
    setTags(tags.filter((t) => t !== tag))
  }

  function removeImage(index: number) {
    setImageUrls(imageUrls.filter((_, i) => i !== index))
  }

  async function addImageFromFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    try {
      const resized = await resizeImageToDataURI(file, 500)
      setImageUrls([...imageUrls, resized])
    } catch (error) {
      console.error('Failed to process image:', error)
    }
    e.target.value = ''
  }

  function insertStepMarkup() {
    setInstructions(`${instructions}\n[STEP]\n`)
  }

  async function onSave() {
    if (!title.trim() || !category.trim()) {
      window.alert('Title and category are required.')
      return
    }
    setSaving(true)

    const builtIngredients = ingredients
      .filter((ing) => ing.name.trim())
      .map((ing, i) => createIngredient(ing.name.trim(), ing.amount?.trim() || null, ing.unit?.trim() || null, ing.notes?.trim() || null, i))

    const builtInstructions = instructions
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean)

    const input: RecipeInput = {
      title: title.trim(),
      category: category.trim(),
      description: description.trim() || null,
      source: source.trim() || null,
      servingSize: servings ? Number(servings) : null,
      prepTime: prepTime ? Number(prepTime) : null,
      cookTime: cookTime ? Number(cookTime) : null,
      imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
      ingredients: builtIngredients,
      instructions: builtInstructions,
      tags,
      notes: notes.trim() || null,
    }

    try {
      // Merge the form values into the existing recipe so edits actually persist.
      let existing: Recipe | null = null
      if (id) existing = await getRecipe(id)
      const saved = await saveRecipe(existing ? { ...existing, ...input } : input, getDeviceId())

      const deviceId = getDeviceId()
      if (!isEdit) {
        if (effectiveCookbookId) {
          await addRecipeToCookbook(effectiveCookbookId, saved.id, deviceId)
        }
      } else {
        // Editing: apply the cookbook dropdown — move the recipe if it changed.
        const holder = cookbooks.find((cb) => cb.recipeIds.includes(saved.id))
        if (holder && holder.id !== effectiveCookbookId) {
          await removeRecipeFromCookbook(holder.id, saved.id, deviceId)
        }
        if (effectiveCookbookId && holder?.id !== effectiveCookbookId) {
          await addRecipeToCookbook(effectiveCookbookId, saved.id, deviceId)
        }
      }

      setSaving(false)
      navigate(`/recipes/${saved.id}`, { replace: true })
    } catch (err) {
      setSaving(false)
      window.alert(err instanceof Error ? err.message : 'Failed to save recipe')
    }
  }

  function onSelectCookbook(value: string) {
    if (value === CREATE_NEW_COOKBOOK) {
      setCreatingCookbook(true)
      // keep the current selection; the new cookbook is selected once created
      return
    }
    setSelectedCookbookId(value)
  }

  const userName = useAppStore((s) => s.userName)

  async function onCreateCookbook() {
    const name = newCookbookName.trim()
    if (!name) return
    const cookbook = await createCookbookNamed(name, getDeviceId(), newCookbookDesc.trim() || null, userName)
    setSelectedCookbookId(cookbook.id)
    setCreatingCookbook(false)
    setNewCookbookName('')
    setNewCookbookDesc('')
  }

  if (loading) return <main className="page"><p className="text-muted">Loading…</p></main>

  return (
    <main className="page edit-page">
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <button type="button" className="btn btn-ghost btn-icon" onClick={() => navigate(-1)} aria-label="Back">
          <ArrowLeft strokeWidth={2.75} />
        </button>
      </div>

      <h1 className="page-title" style={{ marginBottom: 16 }}>{isEdit ? 'Edit recipe' : 'New recipe'}</h1>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Title" style={{ fontSize: 20, fontWeight: 600 }} />
      </div>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <input className="input" value={category} onChange={(e) => setCategory(e.target.value)} placeholder="Category" />
      </div>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <select
          className="input"
          value={effectiveCookbookId}
          onChange={(e) => onSelectCookbook(e.target.value)}
          style={{ fontSize: 14 }}
          aria-label="Cookbook"
        >
          <option value="">Select a cookbook...</option>
          {cookbooks.map((cb) => <option key={cb.id} value={cb.id}>{cb.name}</option>)}
          <option value={CREATE_NEW_COOKBOOK}>+ Create new cookbook…</option>
        </select>
        {creatingCookbook && (
          <div className="card" style={{ gap: 8, padding: 12 }}>
            <input
              className="input"
              value={newCookbookName}
              onChange={(e) => setNewCookbookName(e.target.value)}
              placeholder="Cookbook name"
              autoFocus
            />
            <input
              className="input"
              value={newCookbookDesc}
              onChange={(e) => setNewCookbookDesc(e.target.value)}
              placeholder="Description (optional)"
            />
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <Button variant="secondary" onClick={() => setCreatingCookbook(false)}>Cancel</Button>
              <Button variant="primary" onClick={onCreateCookbook} disabled={!newCookbookName.trim()}>
                Create
              </Button>
            </div>
          </div>
        )}
      </div>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <textarea className="input" value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Description" rows={3} />
      </div>

      <div className="form-row" style={{ marginBottom: 16 }}>
        <label>Ingredients</label>
        <div className="line-list">
          {ingredients.map((ing, idx) => (
            <div key={ing.id} className="line-row" draggable onDragStart={() => handleDragStart(idx)} onDragOver={(e) => handleDragOver(idx, e)} onDrop={(e) => handleDrop(idx, e)} onDragEnd={handleDragEnd} style={{ cursor: 'grab' }}>
              <span style={{ cursor: 'grab', padding: '0 4px', opacity: 0.5 }}><GripVertical size={14} strokeWidth={2.75} /></span>
              <input className="input" placeholder="amount" value={ing.amount ?? ''} onChange={(e) => updateIngredient(ing.id, 'amount', e.target.value)} style={{ maxWidth: 80, flexShrink: 0 }} />
              <select className="input" value={ing.unit ?? ''} onChange={(e) => updateIngredient(ing.id, 'unit', e.target.value)} style={{ maxWidth: 100, flexShrink: 0 }}>
                {UNITS.map((u) => <option key={u} value={u}>{u}</option>)}
              </select>
              <input className="input" placeholder="ingredient name" value={ing.name} onChange={(e) => updateIngredient(ing.id, 'name', e.target.value)} style={{ flex: 1 }} />
              <button type="button" className="btn btn-ghost btn-icon" onClick={() => removeIngredient(ing.id)} aria-label="Remove"><X size={18} strokeWidth={2.75} /></button>
            </div>
          ))}
        </div>
        <Button variant="secondary" onClick={addIngredient} style={{ marginTop: 8 }}><Plus size={16} strokeWidth={2.75} /></Button>
      </div>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <textarea className="input" value={instructions} onChange={(e) => setInstructions(e.target.value)} placeholder="Instructions (one step per line)" rows={8} />
        <Button variant="secondary" onClick={insertStepMarkup} style={{ marginTop: 8 }}><Plus size={16} strokeWidth={2.75} /> Insert Step</Button>
      </div>

      <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
        <div className="form-row" style={{ flex: 1 }}>
          <label>Servings</label>
          <input className="input" type="number" value={servings} onChange={(e) => setServings(e.target.value)} />
        </div>
        <div className="form-row" style={{ flex: 1 }}>
          <label>Prep time (min)</label>
          <input className="input" type="number" value={prepTime} onChange={(e) => setPrepTime(e.target.value)} />
        </div>
        <div className="form-row" style={{ flex: 1 }}>
          <label>Cook time (min)</label>
          <input className="input" type="number" value={cookTime} onChange={(e) => setCookTime(e.target.value)} />
        </div>
      </div>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <label>Images</label>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 4 }}>
          {imageUrls.map((url, idx) => (
            <div key={idx} style={{ position: 'relative' }}>
              <WashedImage src={url} alt={`Recipe ${idx + 1}`} style={{ width: 80, height: 80, borderRadius: 'var(--radius-md)' }} />
              <button type="button" className="btn btn-ghost btn-icon" onClick={() => removeImage(idx)} style={{ position: 'absolute', top: 4, right: 4, background: 'rgba(0,0,0,0.5)' }} aria-label={`Remove ${idx + 1}`}>
                <X size={14} strokeWidth={2.75} />
              </button>
            </div>
          ))}
        </div>
        <input type="file" accept="image/*" onChange={addImageFromFile} style={{ display: 'none' }} id="image-upload" />
        <Button variant="secondary" onClick={() => document.getElementById('image-upload')?.click()}><Plus size={16} strokeWidth={2.75} /> Add image</Button>
      </div>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <label>Tags</label>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 4 }}>
          {tags.map((tag) => (
            <div key={tag} style={{ display: 'inline-flex', alignItems: 'center', gap: 4, background: 'var(--color-surface-3)', padding: '4px 8px', borderRadius: 'var(--radius-md)' }}>
              <span style={{ fontSize: 13 }}>{tag}</span>
              <button type="button" className="btn btn-ghost btn-icon" onClick={() => removeTag(tag)} style={{ padding: 0, height: 16, width: 16 }} aria-label={`Remove ${tag}`}>
                <X size={12} strokeWidth={2.75} />
              </button>
            </div>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 4, marginTop: 4 }}>
          <input className="input" value={tagInput} onChange={(e) => setTagInput(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())} placeholder="Add a tag" style={{ flex: 1 }} />
          <Button variant="secondary" onClick={addTag}><Plus size={16} strokeWidth={2.75} /></Button>
        </div>
      </div>

      <div className="form-row" style={{ marginBottom: 12 }}>
        <input className="input" value={source} onChange={(e) => setSource(e.target.value)} placeholder="Source" />
      </div>

      <div className="form-row" style={{ marginBottom: 16 }}>
        <textarea className="input" value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Notes" rows={3} />
      </div>

      <div className="edit-actions-bar">
        <Button variant="secondary" onClick={() => navigate(-1)} disabled={saving}>Cancel</Button>
        <Button variant="primary" onClick={onSave} disabled={saving}>{saving ? 'Saving…' : (isEdit ? 'Save changes' : 'Add recipe')}</Button>
      </div>
    </main>
  )
}
