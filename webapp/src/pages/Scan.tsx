// Scan — capture/upload an image or PDF, run OCR (English or Swedish),
// parse the text into a recipe, let the user edit the result, and save
// into a cookbook.

import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { Camera, Upload, Loader2, Check } from 'lucide-react'
import { Button } from '../components/Button'
import { ScanStage } from '../components/ScanStage'
import { captureImage, runOcr, pdfPagesToImageUrls, type OcrLanguage } from '../lib/ocrService'
import { parseRecipeFromText, parseIngredientsFromText, parseInstructionsFromText, inferCategoryFromTitle, preprocessOcrText, stripLeadingSectionHeaders } from '../lib/ocrParser'
import { cropImageRegion, type ImageRegion } from '../lib/imageUtils'
import { saveRecipe } from '../features/recipe/repository'
import { addRecipeToCookbook, fileUnfiledRecipes } from '../features/cookbook/repository'
import { useCookbooks } from '../hooks/useCookbooks'
import { useAppStore } from '../stores/appStore'
import { getDeviceId } from '../lib/device'
import { ingredientDisplay } from '../types/ingredient'
import type { RecipeInput } from '../types/recipe'

type Phase = 'idle' | 'cropping' | 'regions' | 'recognizing' | 'reviewing' | 'saving'

type RegionMode = 'title' | 'ingredients' | 'instructions'

const REGION_MODES: { mode: RegionMode; label: string; className: string; token: string }[] = [
  { mode: 'title', label: 'Title', className: 'region-btn-title', token: 'var(--color-region-title)' },
  { mode: 'ingredients', label: 'Ingredients', className: 'region-btn-ingredients', token: 'var(--color-region-ingredients)' },
  { mode: 'instructions', label: 'Instructions', className: 'region-btn-instructions', token: 'var(--color-region-instructions)' },
]

const LANGUAGES: { value: OcrLanguage; label: string }[] = [
  { value: 'swe', label: 'Svenska' },
  { value: 'eng', label: 'English' },
]

export function Scan() {
  const navigate = useNavigate()
  const { cookbooks } = useCookbooks()
  const scanLanguage = useAppStore((s) => s.scanLanguage)
  const setScanLanguage = useAppStore((s) => s.setScanLanguage)
  const [phase, setPhase] = useState<Phase>('idle')
  const [progress, setProgress] = useState(0)
  const [ocrText, setOcrText] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [sourceImage, setSourceImage] = useState<string | null>(null)
  const [cropDraft, setCropDraft] = useState<ImageRegion | null>(null)
  const [regionDraft, setRegionDraft] = useState<ImageRegion | null>(null)
  const [activeMode, setActiveMode] = useState<RegionMode>('title')
  const [markedRegions, setMarkedRegions] = useState<Partial<Record<RegionMode, ImageRegion>>>({})
  const [selectedCookbookId, setSelectedCookbookId] = useState('')
  const fileInput = useRef<HTMLInputElement>(null)

  // Editable parsed recipe
  const [title, setTitle] = useState('')
  const [category, setCategory] = useState('')
  const [ingredients, setIngredients] = useState('')
  const [instructions, setInstructions] = useState('')

  // OCR every page image and combine the text; progress spans all pages.
  async function recognizePages(images: string[]) {
    const texts: string[] = []
    for (let i = 0; i < images.length; i++) {
      const result = await runOcr(images[i], scanLanguage, (p) =>
        setProgress((i + p) / images.length),
      )
      texts.push(result.text)
    }
    return texts.join('\n\n')
  }

  function reviewText(text: string) {
    setOcrText(text)
    const parsed = parseRecipeFromText(text)
    setTitle(parsed.title ?? '')
    setCategory(parsed.category ?? '')
    setIngredients((parsed.ingredients ?? []).map(ingredientDisplay).join('\n'))
    setInstructions((parsed.instructions ?? []).join('\n'))
    setPhase('reviewing')
  }

  // Images go through crop + optional region marking; PDFs skip straight to OCR.
  function processImage(dataUrl: string) {
    setSourceImage(dataUrl)
    setImagePreview(null)
    setOcrText('')
    setCropDraft({ x: 0, y: 0, w: 1, h: 1 })
    setRegionDraft(null)
    setMarkedRegions({})
    setError(null)
    setPhase('cropping')
  }

  // Confirm the crop rectangle: the cropped image becomes the recipe image
  // and the base for region marking and OCR.
  async function confirmCrop() {
    if (!sourceImage) return
    setError(null)
    try {
      const region = cropDraft ?? { x: 0, y: 0, w: 1, h: 1 }
      const isFullImage = region.x <= 0 && region.y <= 0 && region.w >= 1 && region.h >= 1
      const cropped = isFullImage ? sourceImage : await cropImageRegion(sourceImage, region)
      setImagePreview(cropped)
      setRegionDraft(null)
      setMarkedRegions({})
      setPhase('regions')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not crop the image')
    }
  }

  // A finished drag is committed straight away; drawing again for the same
  // mode replaces the rectangle.
  function commitRegionDraft(region: ImageRegion) {
    setMarkedRegions((prev) => ({ ...prev, [activeMode]: region }))
    setRegionDraft(null)
  }

  function clearRegions() {
    setMarkedRegions({})
    setRegionDraft(null)
  }

  function onCancel() {
    setPhase('idle')
    setSourceImage(null)
    setImagePreview(null)
    setCropDraft(null)
    setRegionDraft(null)
    setMarkedRegions({})
    setError(null)
  }

  // OCR each marked region separately; with no regions, scan the whole image.
  async function runOcrFromRegions() {
    if (!imagePreview) return
    setPhase('recognizing')
    setError(null)
    setProgress(0)
    try {
      const entries = (Object.entries(markedRegions) as [RegionMode, ImageRegion][])
      if (entries.length === 0) {
        const text = await recognizePages([imagePreview])
        reviewText(text)
        return
      }
      const texts: Record<RegionMode, string> = { title: '', ingredients: '', instructions: '' }
      for (let i = 0; i < entries.length; i++) {
        const [mode, region] = entries[i]
        // 2x scale: tesseract reads small crops more accurately.
        const regionImage = await cropImageRegion(imagePreview, region, 2)
        const result = await runOcr(regionImage, scanLanguage, (p) =>
          setProgress((i + p) / entries.length),
        )
        texts[mode] = result.text
      }
      reviewFromRegions(texts.title, texts.ingredients, texts.instructions)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'OCR failed')
      setPhase('regions')
    }
  }

  // Build the review form from per-region OCR text. A marked rectangle often
  // includes the section heading sitting directly above the list — strip
  // leading headers so they don't end up as the first ingredient/step/title.
  function reviewFromRegions(titleText: string, ingredientsText: string, instructionsText: string) {
    setOcrText([titleText, ingredientsText, instructionsText].filter(Boolean).join('\n\n'))
    const cleanTitle = stripLeadingSectionHeaders(titleText)
    const cleanIngredients = stripLeadingSectionHeaders(ingredientsText)
    const cleanInstructions = stripLeadingSectionHeaders(instructionsText)
    const titleLine = cleanTitle.split('\n').map((l) => l.trim()).find(Boolean) ?? ''
    setTitle(titleLine.slice(0, 100))
    setCategory(titleLine ? inferCategoryFromTitle(titleLine) : '')
    const ings = parseIngredientsFromText(cleanIngredients)
    setIngredients(ings.map(ingredientDisplay).join('\n'))
    setInstructions(parseInstructionsFromText(cleanInstructions).join('\n'))
    setPhase('reviewing')
  }

  async function processPdf(bytes: Uint8Array) {
    setPhase('recognizing')
    setError(null)
    setProgress(0)
    setImagePreview(null)
    try {
      const pages = await pdfPagesToImageUrls(bytes)
      if (pages.length === 0) throw new Error('The PDF has no pages.')
      setImagePreview(pages[0])
      const text = await recognizePages(pages)
      reviewText(text)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not read the PDF')
      setPhase('idle')
    }
  }

  async function onCapture() {
    const dataUrl = await captureImage()
    if (dataUrl) processImage(dataUrl)
  }

  async function onFilePick(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
      const buffer = await file.arrayBuffer()
      await processPdf(new Uint8Array(buffer))
    } else {
      const reader = new FileReader()
      reader.onload = () => {
        const dataUrl = reader.result as string
        processImage(dataUrl)
      }
      reader.readAsDataURL(file)
    }
    e.target.value = ''
  }

  async function onSave() {
    if (!title.trim()) {
      setError('Title is required.')
      return
    }
    setPhase('saving')
    setError(null)
    const input: RecipeInput = {
      title: title.trim(),
      category: category.trim() || 'Uncategorized',
      // Parse the same way as RecipeEdit so any unit text is kept as typed.
      ingredients: parseIngredientsFromText(ingredients),
      instructions: instructions.split('\n').map((l) => l.trim()).filter(Boolean),
      imageUrls: imagePreview ? [imagePreview] : undefined,
    }
    try {
      const deviceId = getDeviceId()
      const saved = await saveRecipe(input, deviceId)
      // Every recipe belongs to a cookbook: the chosen one, or the default.
      if (selectedCookbookId) {
        await addRecipeToCookbook(selectedCookbookId, saved.id, deviceId)
      } else {
        await fileUnfiledRecipes(deviceId, [saved.id])
      }
      navigate(`/recipes/${saved.id}`, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save')
      setPhase('reviewing')
    }
  }

  function onEditRaw() {
    if (!ocrText) return
    reviewText(ocrText)
  }

  if (phase === 'cropping' && sourceImage) {
    return (
      <div className="scan-fullscreen">
        <header className="scan-bar">
          <h1 className="scan-title">Crop image</h1>
          <div className="row-spacer" />
          <Button variant="ghost" onClick={onCancel}>Cancel</Button>
        </header>
        <ScanStage
          src={sourceImage}
          alt="Recipe to scan"
          value={cropDraft}
          onChange={setCropDraft}
          color="var(--color-accent)"
        />
        <footer className="scan-bar scan-bar-col">
          <p className="text-muted text-sm scan-hint">
            Drag to select the area with the recipe — everything outside is cropped away.
            Pinch with two fingers to zoom.
          </p>
          {error && <p className="text-muted text-error">{error}</p>}
          <div className="row">
            <div className="row-spacer" />
            <Button variant="primary" onClick={confirmCrop}>
              <Check size={18} strokeWidth={2.75} /> Use this area
            </Button>
          </div>
        </footer>
      </div>
    )
  }

  if (phase === 'regions' && imagePreview) {
    const active = REGION_MODES.find((m) => m.mode === activeMode) ?? REGION_MODES[0]
    const confirmed = (Object.entries(markedRegions) as [RegionMode, ImageRegion][]).map(
      ([mode, region]) => ({ region, color: `var(--color-region-${mode})` }),
    )
    const markedLabels = REGION_MODES.filter((m) => markedRegions[m.mode]).map((m) => m.label)
    const remainingLabels = REGION_MODES.filter((m) => !markedRegions[m.mode]).map((m) => m.label)
    const hint =
      markedLabels.length === 0
        ? 'Optional — draw a rectangle around each area, or skip and scan the whole image. Pinch with two fingers to zoom.'
        : remainingLabels.length > 0
          ? `Marked: ${markedLabels.join(', ')}. Draw ${remainingLabels.join(' / ').toLowerCase()} — draw again to replace.`
          : 'All areas marked — run OCR when ready.'
    return (
      <div className="scan-fullscreen">
        <header className="scan-bar">
          <h1 className="scan-title">Mark the text</h1>
          <div className="row-spacer" />
          <Button variant="ghost" onClick={onCancel}>Cancel</Button>
        </header>
        <ScanStage
          src={imagePreview}
          alt="Cropped recipe"
          value={regionDraft}
          onChange={setRegionDraft}
          onCommit={commitRegionDraft}
          color={active.token}
          confirmed={confirmed}
        />
        <footer className="scan-bar scan-bar-col">
          <div className="chip-row">
            {REGION_MODES.map((m) => (
              <button
                key={m.mode}
                type="button"
                className={`region-btn ${m.className}`}
                aria-pressed={activeMode === m.mode}
                onClick={() => setActiveMode(m.mode)}
              >
                {m.label}
                {markedRegions[m.mode] && <Check size={12} strokeWidth={2.75} />}
              </button>
            ))}
          </div>
          <p className="text-muted text-sm scan-hint">{hint}</p>
          {error && <p className="text-muted text-error">{error}</p>}
          <div className="row">
            <Button variant="secondary" onClick={clearRegions} disabled={confirmed.length === 0}>
              Clear
            </Button>
            <div className="row-spacer" />
            <Button variant="primary" onClick={runOcrFromRegions}>
              Run OCR
            </Button>
          </div>
        </footer>
      </div>
    )
  }

  if (phase === 'recognizing') {
    return (
      <main className="page">
        <h1 className="page-title">Scanning…</h1>
        {imagePreview && (
          <img src={imagePreview} alt="Scanning" className="washed img-cover mb-4" />
        )}
        <div className="row">
          <Loader2 size={20} strokeWidth={2.75} className="spin" />
          <span className="text-muted">Recognizing text… {Math.round(progress * 100)}%</span>
        </div>
      </main>
    )
  }

  if (phase === 'reviewing' || phase === 'saving') {
    return (
      <main className="page">
        <h1 className="page-title">Review recipe</h1>

        {imagePreview && (
          <img src={imagePreview} alt="Source" className="washed img-cover scan-preview mb-4" />
        )}

        <div className="form-row">
          <label htmlFor="title">Title</label>
          <input id="title" className="input" value={title} onChange={(e) => setTitle(e.target.value)} />
        </div>
        <div className="form-row">
          <label htmlFor="category">Category</label>
          <input id="category" className="input" value={category} onChange={(e) => setCategory(e.target.value)} />
        </div>
        <div className="form-row">
          <label htmlFor="scan-cookbook">Cookbook</label>
          <select
            id="scan-cookbook"
            className="input"
            value={selectedCookbookId}
            onChange={(e) => setSelectedCookbookId(e.target.value)}
          >
            <option value="">Mina recept (default)</option>
            {cookbooks.map((cb) => (
              <option key={cb.id} value={cb.id}>{cb.name}</option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label htmlFor="ingredients">Ingredients (one per line)</label>
          <textarea
            id="ingredients"
            className="input input-tall"
            value={ingredients}
            onChange={(e) => setIngredients(e.target.value)}
          />
        </div>
        <div className="form-row">
          <label htmlFor="instructions">Instructions (one per line)</label>
          <textarea
            id="instructions"
            className="input input-tall"
            value={instructions}
            onChange={(e) => setInstructions(e.target.value)}
          />
        </div>

        {ocrText && (
          <details className="mb-4">
            <summary className="text-muted raw-toggle">Raw OCR text</summary>
            <pre className="text-muted ocr-text">{preprocessOcrText(ocrText)}</pre>
            <Button variant="ghost" className="mt-1" onClick={onEditRaw}>Re-parse from raw text</Button>
          </details>
        )}

        {error && <p className="text-muted text-error">{error}</p>}

        <div className="form-actions">
          <Button variant="secondary" onClick={() => { setPhase('idle'); setImagePreview(null); setError(null) }}>
            Discard
          </Button>
          <Button variant="primary" onClick={onSave} disabled={phase === 'saving' || !title.trim()}>
            {phase === 'saving' ? 'Saving…' : 'Save recipe'}
          </Button>
        </div>
      </main>
    )
  }

  return (
    <main className="page">
      <h1 className="page-title">Scan recipe</h1>
      <p className="text-muted text-center mb-4">
        Supported formats: JPG, PNG, PDF
      </p>

      <div className="chip-row chip-row-center mb-4">
        {LANGUAGES.map(({ value, label }) => (
          <button
            key={value}
            type="button"
            className="chip"
            onClick={() => setScanLanguage(value)}
            aria-pressed={scanLanguage === value}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="scan-actions">
        <Button
          variant="primary"
          onClick={onCapture}
          aria-label="Take photo"
        >
          <Camera size={18} strokeWidth={2.75} />
        </Button>
        <Button
          variant="secondary"
          onClick={() => fileInput.current?.click()}
        >
          <Upload size={18} strokeWidth={2.75} /> Upload
        </Button>
      </div>

      <input ref={fileInput} type="file" accept="image/*,application/pdf" onChange={onFilePick} hidden />

      {error && <p className="text-muted text-error mt-4">{error}</p>}
    </main>
  )
}
