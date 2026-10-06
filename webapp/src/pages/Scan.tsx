// Scan — capture/upload an image or PDF, run OCR (English or Swedish),
// parse the text into a recipe, let the user edit the result, and save
// into a cookbook.

import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { Camera, Upload, Loader2 } from 'lucide-react'
import { Button } from '../components/Button'
import { captureImage, runOcr, pdfPagesToImageUrls, type OcrLanguage } from '../lib/ocrService'
import { parseRecipeFromText, preprocessOcrText } from '../lib/ocrParser'
import { saveRecipe } from '../features/recipe/repository'
import { addRecipeToCookbook, fileUnfiledRecipes } from '../features/cookbook/repository'
import { useCookbooks } from '../hooks/useCookbooks'
import { useAppStore } from '../stores/appStore'
import { getDeviceId } from '../lib/device'
import type { RecipeInput } from '../types/recipe'

type Phase = 'idle' | 'recognizing' | 'reviewing' | 'saving'

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
    setIngredients((parsed.ingredients ?? []).map((i) => [i.amount, i.unit, i.name].filter(Boolean).join(' ')).join('\n'))
    setInstructions((parsed.instructions ?? []).join('\n'))
    setPhase('reviewing')
  }

  async function processImage(dataUrl: string) {
    setImagePreview(dataUrl)
    setPhase('recognizing')
    setError(null)
    setProgress(0)
    try {
      const text = await recognizePages([dataUrl])
      reviewText(text)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'OCR failed')
      setPhase('idle')
    }
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
    if (dataUrl) await processImage(dataUrl)
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
      ingredients: ingredients
        .split('\n')
        .map((l) => l.trim())
        .filter(Boolean)
        .map((line, i) => {
          const match = /^(\d+\s*\d*\/\d+|\d+\.\d+|\d+)\s*(oz|cup|cups|tbsp|tsp|lb|lbs|g|kg|ml|l|dl|cloves|cans|slices|pieces)?\s+(.*)$/i.exec(line)
          if (match) {
            return { id: crypto.randomUUID(), name: match[3], amount: match[1], unit: match[2] ?? null, notes: null, order: i }
          }
          return { id: crypto.randomUUID(), name: line, amount: null, unit: null, notes: null, order: i }
        }),
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

  if (phase === 'recognizing') {
    return (
      <main className="page">
        <h1 className="page-title">Scanning…</h1>
        {imagePreview && (
          <img src={imagePreview} alt="Scanning" className="washed" style={{ borderRadius: 'var(--radius-lg)', width: '100%', marginBottom: 16 }} />
        )}
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Loader2 size={20} strokeWidth={2.75} style={{ animation: 'spin 1s linear infinite' }} />
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
          <img src={imagePreview} alt="Source" className="washed" style={{ borderRadius: 'var(--radius-lg)', width: '100%', marginBottom: 16, maxHeight: 200, objectFit: 'cover' }} />
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
            className="input"
            value={ingredients}
            onChange={(e) => setIngredients(e.target.value)}
            style={{ minHeight: 120 }}
          />
        </div>
        <div className="form-row">
          <label htmlFor="instructions">Instructions (one per line)</label>
          <textarea
            id="instructions"
            className="input"
            value={instructions}
            onChange={(e) => setInstructions(e.target.value)}
            style={{ minHeight: 120 }}
          />
        </div>

        {ocrText && (
          <details style={{ marginBottom: 16 }}>
            <summary className="text-muted" style={{ cursor: 'pointer', fontSize: 13 }}>Raw OCR text</summary>
            <pre className="text-muted" style={{ fontSize: 12, whiteSpace: 'pre-wrap', marginTop: 8 }}>{preprocessOcrText(ocrText)}</pre>
            <Button variant="ghost" onClick={onEditRaw} style={{ marginTop: 4 }}>Re-parse from raw text</Button>
          </details>
        )}

        {error && <p className="text-muted" style={{ color: 'var(--color-accent-700)' }}>{error}</p>}

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
      <p className="text-muted" style={{ textAlign: 'center', marginBottom: 16 }}>
        Supported formats: JPG, PNG, PDF
      </p>

      <div style={{ display: 'flex', gap: 6, justifyContent: 'center', marginBottom: 16 }}>
        {LANGUAGES.map(({ value, label }) => (
          <button
            key={value}
            type="button"
            onClick={() => setScanLanguage(value)}
            aria-pressed={scanLanguage === value}
            style={{
              padding: '6px 14px',
              background: scanLanguage === value ? 'var(--color-accent)' : 'var(--color-surface-3)',
              color: scanLanguage === value ? 'var(--color-accent-contrast)' : 'var(--color-text)',
              border: 'none',
              borderRadius: '999px',
              cursor: 'pointer',
              fontSize: 13,
            }}
          >
            {label}
          </button>
        ))}
      </div>

      <div style={{ display: 'flex', gap: 16, justifyContent: 'center', marginBottom: 16 }}>
        <Button
          variant="primary"
          onClick={onCapture}
          style={{ flex: 1, maxWidth: '45%' }}
          aria-label="Take photo"
        >
          <Camera size={18} strokeWidth={2.75} />
        </Button>
        <Button
          variant="secondary"
          onClick={() => fileInput.current?.click()}
          style={{ flex: 1, maxWidth: '45%' }}
        >
          <Upload size={18} strokeWidth={2.75} /> Upload
        </Button>
      </div>

      <input ref={fileInput} type="file" accept="image/*,application/pdf" onChange={onFilePick} style={{ display: 'none' }} />

      {error && <p className="text-muted" style={{ color: 'var(--color-accent-700)', marginTop: 16 }}>{error}</p>}
    </main>
  )
}
