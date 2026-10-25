// RecipeEdit — create or edit a recipe with edit-in-place layout.
// Ingredients and instructions are entered one per line (the same format as
// the scan review); a recipe can have several named ingredient lists (e.g.
// "Biscuits" and "Gravy"). Category is picked from existing categories or
// entered as a new one. Supports multiple image uploads and pre-selecting
// a cookbook.

import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { ArrowLeft, Plus, X } from 'lucide-react'
import { Button } from '../components/Button'
import { Tag } from '../components/Tag'
import { WashedImage } from '../components/WashedImage'
import { getRecipe, saveRecipe, allCategories } from '../features/recipe/repository'
import { createCookbookNamed, addRecipeToCookbook, removeRecipeFromCookbook } from '../features/cookbook/repository'
import { getDeviceId } from '../lib/device'
import { createIngredientSection, flattenIngredientSections, ingredientDisplay } from '../types/ingredient'
import { parseIngredientsFromText } from '../lib/ocrParser'
import { resizeImageToDataURI } from '../lib/imageUtils'
import { useCookbooks } from '../hooks/useCookbooks'
import { useRecipes } from '../hooks/useRecipes'
import { useAppStore } from '../stores/appStore'
import { getIngredientSections } from '../types/recipe'
import { useT } from '../i18n'
import type { Recipe, RecipeInput } from '../types/recipe'

const CREATE_NEW_COOKBOOK = '__create__'
const CREATE_NEW_CATEGORY = '__new_category__'

// Editable draft of one ingredient list: a name plus the raw one-per-line text.
type SectionDraft = { id: string; name: string; text: string }

export function RecipeEdit() {
  const t = useT()
  const { id } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const { cookbooks } = useCookbooks()
  const { recipes } = useRecipes()
  const categories = allCategories(recipes)

  const [title, setTitle] = useState('')
  const [category, setCategory] = useState('')
  // 'pick' = choose an existing category, 'new' = type a category name.
  const [categoryMode, setCategoryMode] = useState<'pick' | 'new'>('pick')
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
  // One unnamed list by default; "Add ingredient list" appends more.
  const [sections, setSections] = useState<SectionDraft[]>([{ id: crypto.randomUUID(), name: '', text: '' }])
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
    setSections(
      getIngredientSections(r).map((s) => ({
        id: s.id,
        name: s.name,
        text: s.ingredients.map(ingredientDisplay).join('\n'),
      })),
    )
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

  function updateSection(id: string, patch: Partial<SectionDraft>) {
    setSections(sections.map((s) => (s.id === id ? { ...s, ...patch } : s)))
  }

  function addSection() {
    setSections([...sections, { id: crypto.randomUUID(), name: '', text: '' }])
  }

  function removeSection(id: string) {
    setSections(sections.filter((s) => s.id !== id))
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

  async function onSave() {
    if (!title.trim() || !category.trim()) {
      window.alert(t('edit.titleCategoryRequired'))
      return
    }

    // One ingredient per line: "amount unit name" — parsed the same way as
    // the scan review, so any unit text is kept as typed. Empty lists are
    // dropped; with more than one list, every list must have a name.
    const builtSections = sections
      .map((s) => createIngredientSection(s.name.trim(), parseIngredientsFromText(s.text)))
      .filter((s) => s.ingredients.length > 0)
    if (builtSections.length > 1 && builtSections.some((s) => !s.name)) {
      window.alert(t('edit.listNeedsName'))
      return
    }
    const builtIngredients = flattenIngredientSections(builtSections)

    setSaving(true)

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
      imageUrls,
      ingredients: builtIngredients,
      instructions: builtInstructions,
      tags,
      notes: notes.trim() || null,
    }
    // Persist the structure only when it carries more than the default
    // single unnamed list.
    if (builtSections.length > 1 || builtSections.some((s) => s.name !== '')) {
      input.ingredientSections = builtSections
    }

    try {
      // Merge the form values into the existing recipe so edits actually persist.
      let existing: Recipe | null = null
      if (id) existing = await getRecipe(id)
      const merged = existing ? { ...existing, ...input } : input
      // A single unnamed list means no sections — drop any stored structure.
      if (!input.ingredientSections) delete merged.ingredientSections
      const saved = await saveRecipe(merged, getDeviceId())

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
      window.alert(err instanceof Error ? err.message : t('edit.saveFailed'))
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

  if (loading) return <main className="page"><p className="text-muted">{t('common.loading')}</p></main>

  return (
    <main className="page edit-page">
      <div className="row mb-2">
        <button type="button" className="btn btn-ghost btn-icon" onClick={() => navigate(-1)} aria-label={t('common.back')}>
          <ArrowLeft strokeWidth={2.75} />
        </button>
      </div>

      <h1 className="page-title">{isEdit ? t('edit.editTitle') : t('edit.newTitle')}</h1>

      <div className="form-row">
        <input className="input input-title" value={title} onChange={(e) => setTitle(e.target.value)} placeholder={t('edit.titlePlaceholder')} />
      </div>

      <div className="form-row">
        <select
          className="input"
          value={categoryMode === 'new' ? CREATE_NEW_CATEGORY : category}
          onChange={(e) => {
            const v = e.target.value
            if (v === CREATE_NEW_CATEGORY) {
              setCategoryMode('new')
            } else {
              setCategoryMode('pick')
              setCategory(v)
            }
          }}
          aria-label={t('edit.categoryAria')}
        >
          <option value="">{t('edit.selectCategory')}</option>
          {categories.map((c) => <option key={c} value={c}>{c}</option>)}
          {category && !categories.includes(category) && <option value={category}>{category}</option>}
          <option value={CREATE_NEW_CATEGORY}>{t('edit.newCategoryOption')}</option>
        </select>
        {categoryMode === 'new' && (
          <input
            className="input"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            placeholder={t('edit.categoryNamePlaceholder')}
            autoFocus
          />
        )}
      </div>

      <div className="form-row">
        <select
          className="input"
          value={effectiveCookbookId}
          onChange={(e) => onSelectCookbook(e.target.value)}
          aria-label={t('edit.cookbookAria')}
        >
          <option value="">{t('edit.selectCookbook')}</option>
          {cookbooks.map((cb) => <option key={cb.id} value={cb.id}>{cb.name}</option>)}
          <option value={CREATE_NEW_COOKBOOK}>{t('edit.createNewCookbookOption')}</option>
        </select>
        {creatingCookbook && (
          <div className="card">
            <input
              className="input"
              value={newCookbookName}
              onChange={(e) => setNewCookbookName(e.target.value)}
              placeholder={t('edit.cookbookNamePlaceholder')}
              autoFocus
            />
            <input
              className="input"
              value={newCookbookDesc}
              onChange={(e) => setNewCookbookDesc(e.target.value)}
              placeholder={t('cookbooks.descriptionOptional')}
            />
            <div className="row-end">
              <Button variant="secondary" onClick={() => setCreatingCookbook(false)}>{t('common.cancel')}</Button>
              <Button variant="primary" onClick={onCreateCookbook} disabled={!newCookbookName.trim()}>
                {t('common.create')}
              </Button>
            </div>
          </div>
        )}
      </div>

      <div className="form-row">
        <textarea className="input" value={description} onChange={(e) => setDescription(e.target.value)} placeholder={t('edit.descriptionPlaceholder')} rows={3} />
      </div>

      <div className="form-row">
        <label>{sections.length > 1 ? t('edit.ingredientListsLabel') : t('edit.ingredientsLabel')}</label>
        {sections.map((s, idx) => (
          <div key={s.id} className="ingredient-section">
            {(sections.length > 1 || s.name) && (
              <div className="row">
                <input
                  className="input"
                  value={s.name}
                  onChange={(e) => updateSection(s.id, { name: e.target.value })}
                  placeholder={t('edit.listNamePlaceholder')}
                  aria-label={t('edit.listNameAria', { n: idx + 1 })}
                />
                {sections.length > 1 && (
                  <button type="button" className="btn btn-ghost btn-icon" onClick={() => removeSection(s.id)} aria-label={t('edit.removeListAria', { n: idx + 1 })}>
                    <X size={18} strokeWidth={2.75} />
                  </button>
                )}
              </div>
            )}
            <textarea
              className="input input-tall"
              value={s.text}
              onChange={(e) => updateSection(s.id, { text: e.target.value })}
              placeholder={'2 cups flour\n1 tsp salt'}
              rows={6}
              aria-label={t('edit.ingredientsAria', { n: idx + 1 })}
            />
          </div>
        ))}
        <Button variant="secondary" onClick={addSection}><Plus size={16} strokeWidth={2.75} /> {t('edit.addIngredientList')}</Button>
      </div>

      <div className="form-row">
        <label htmlFor="instructions">{t('edit.instructionsLabel')}</label>
        <textarea
          id="instructions"
          className="input input-tall"
          value={instructions}
          onChange={(e) => setInstructions(e.target.value)}
          rows={8}
        />
      </div>

      <div className="form-grid">
        <div className="form-row">
          <label>{t('edit.servings')}</label>
          <input className="input" type="number" value={servings} onChange={(e) => setServings(e.target.value)} />
        </div>
        <div className="form-row">
          <label>{t('edit.prepTime')}</label>
          <input className="input" type="number" value={prepTime} onChange={(e) => setPrepTime(e.target.value)} />
        </div>
        <div className="form-row">
          <label>{t('edit.cookTime')}</label>
          <input className="input" type="number" value={cookTime} onChange={(e) => setCookTime(e.target.value)} />
        </div>
      </div>

      <div className="form-row">
        <label>{t('edit.images')}</label>
        <div className="row-wrap mt-1">
          {imageUrls.map((url, idx) => (
            <div key={idx} className="image-thumb">
              <WashedImage src={url} alt={t('edit.imageAria', { n: idx + 1 })} className="thumb thumb-80" />
              <button type="button" className="btn btn-ghost btn-icon image-remove" onClick={() => removeImage(idx)} aria-label={t('edit.removeImageAria', { n: idx + 1 })}>
                <X size={14} strokeWidth={2.75} />
              </button>
            </div>
          ))}
        </div>
        <input type="file" accept="image/*" onChange={addImageFromFile} hidden id="image-upload" />
        <Button variant="secondary" onClick={() => document.getElementById('image-upload')?.click()}><Plus size={16} strokeWidth={2.75} /> {t('edit.addImage')}</Button>
      </div>

      <div className="form-row">
        <label>{t('edit.tags')}</label>
        <div className="chip-row mt-1">
          {tags.map((tag) => (
            <Tag key={tag} variant="neutral">
              {tag}
              <button type="button" className="tag-remove" onClick={() => removeTag(tag)} aria-label={t('edit.removeTagAria', { tag })}>
                <X size={12} strokeWidth={2.75} />
              </button>
            </Tag>
          ))}
        </div>
        <div className="tag-add">
          <input className="input" value={tagInput} onChange={(e) => setTagInput(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())} placeholder={t('edit.addTagPlaceholder')} />
          <Button variant="secondary" onClick={addTag}><Plus size={16} strokeWidth={2.75} /></Button>
        </div>
      </div>

      <div className="form-row">
        <input className="input" value={source} onChange={(e) => setSource(e.target.value)} placeholder={t('edit.sourcePlaceholder')} />
      </div>

      <div className="form-row">
        <textarea className="input" value={notes} onChange={(e) => setNotes(e.target.value)} placeholder={t('edit.notesPlaceholder')} rows={3} />
      </div>

      <div className="edit-actions-bar">
        <Button variant="secondary" onClick={() => navigate(-1)} disabled={saving}>{t('common.cancel')}</Button>
        <Button variant="primary" onClick={onSave} disabled={saving}>{saving ? t('edit.saving') : (isEdit ? t('edit.saveChanges') : t('edit.addRecipe'))}</Button>
      </div>
    </main>
  )
}
