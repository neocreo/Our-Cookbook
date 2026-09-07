// Scan — capture/upload an image, run OCR, parse the text into a recipe,
// let the user edit the result, and save. On mobile the camera opens
// directly; on web a file picker lets the user upload a photo.

import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { Camera, Upload, Loader2 } from 'lucide-react'
import { Button } from '../components/Button'
import { captureImage, runOcr } from '../lib/ocrService'
import { parseRecipeFromText, preprocessOcrText } from '../lib/ocrParser'
import { saveRecipe } from '../features/recipe/repository'
import { getDeviceId } from '../lib/device'
import type { RecipeInput } from '../types/recipe'

type Phase = 'idle' | 'recognizing' | 'reviewing' | 'saving'

export function Scan() {
  const navigate = useNavigate()
  const [phase, setPhase] = useState<Phase>('idle')
  const [progress, setProgress] = useState(0)
  const [ocrText, setOcrText] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const fileInput = useRef<HTMLInputElement>(null)

  // Editable parsed recipe
  const [title, setTitle] = useState('')
  const [category, setCategory] = useState('')
  const [ingredients, setIngredients] = useState('')
  const [instructions, setInstructions] = useState('')

  async function processImage(dataUrl: string) {
    setImagePreview(dataUrl)
    setPhase('recognizing')
    setError(null)
    setProgress(0)
    try {
      const result = await runOcr(dataUrl, (p) => setProgress(p))
      const text = result.text
      setOcrText(text)
      const parsed = parseRecipeFromText(text)
      setTitle(parsed.title ?? '')
      setCategory(parsed.category ?? '')
      setIngredients((parsed.ingredients ?? []).map((i) => [i.amount, i.unit, i.name].filter(Boolean).join(' ')).join('\n'))
      setInstructions((parsed.instructions ?? []).join('\n'))
      setPhase('reviewing')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'OCR failed')
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
    const reader = new FileReader()
    reader.onload = () => {
      const dataUrl = reader.result as string
      processImage(dataUrl)
    }
    reader.readAsDataURL(file)
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
          const match = /^(\d+\s*\d*\/\d+|\d+\.\d+|\d+)\s*(oz|cup|cups|tbsp|tsp|lb|lbs|g|kg|ml|l|cloves|cans|slices|pieces)?\s+(.*)$/i.exec(line)
          if (match) {
            return { id: crypto.randomUUID(), name: match[3], amount: match[1], unit: match[2] ?? null, notes: null, order: i }
          }
          return { id: crypto.randomUUID(), name: line, amount: null, unit: null, notes: null, order: i }
        }),
      instructions: instructions.split('\n').map((l) => l.trim()).filter(Boolean),
    }
    try {
      const saved = await saveRecipe(input, getDeviceId())
      navigate(`/recipes/${saved.id}`, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save')
      setPhase('reviewing')
    }
  }

  function onEditRaw() {
    if (!ocrText) return
    const parsed = parseRecipeFromText(ocrText)
    setTitle(parsed.title ?? '')
    setCategory(parsed.category ?? '')
    setIngredients((parsed.ingredients ?? []).map((i) => [i.amount, i.unit, i.name].filter(Boolean).join(' ')).join('\n'))
    setInstructions((parsed.instructions ?? []).join('\n'))
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
          <Button variant="primary" onClick={onSave} disabled={phase === 'saving'}>
            {phase === 'saving' ? 'Saving…' : 'Save recipe'}
          </Button>
        </div>
      </main>
    )
  }

  return (
    <main className="page">
      <h1 className="page-title">Scan recipe</h1>
      <p className="text-muted">
        Take a photo of a recipe or upload an image. The app will recognize the
        text and parse it into a recipe you can edit and save.
      </p>

      <div className="onboarding-actions" style={{ marginTop: 16 }}>
        <Button variant="primary" block onClick={onCapture}>
          <Camera size={18} strokeWidth={2.75} />
          Take photo
        </Button>
        <input ref={fileInput} type="file" accept="image/*" onChange={onFilePick} style={{ display: 'none' }} />
        <Button variant="secondary" block onClick={() => fileInput.current?.click()}>
          <Upload size={18} strokeWidth={2.75} />
          Upload image
        </Button>
      </div>

      {error && <p className="text-muted" style={{ color: 'var(--color-accent-700)', marginTop: 16 }}>{error}</p>}
    </main>
  )
}
