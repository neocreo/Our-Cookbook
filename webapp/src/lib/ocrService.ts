// OCR service — abstracts image capture and text recognition across
// platforms. On mobile, @capacitor/camera takes the photo; on web, a file
// input lets the user pick an image. OCR runs via tesseract.js in both cases
// (works in browser and Capacitor WebView), with a selectable recognition
// language (English/Swedish). PDFs are supported by rendering each page to
// an image with pdf.js before recognition. When Capacitor 8 is available,
// ML Kit can replace tesseract on mobile for better accuracy.

import { Capacitor } from '@capacitor/core'
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera'

export type OcrLanguage = 'eng' | 'swe'

type ProgressFn = (progress: number) => void

export interface OcrResult {
  text: string
}

/** Capture an image: camera on mobile, file picker on web. Returns a data URL. */
export async function captureImage(): Promise<string | null> {
  if (Capacitor.isNativePlatform()) {
    try {
      const photo = await Camera.getPhoto({
        quality: 90,
        resultType: CameraResultType.DataUrl,
        source: CameraSource.Camera,
        allowEditing: false,
      })
      return photo.dataUrl ?? null
    } catch {
      return null // user cancelled
    }
  }
  // Web: open a file picker via a hidden input
  return pickImageFile()
}

/** Let the user pick an image file (web only). Returns a data URL. */
export function pickImageFile(): Promise<string | null> {
  return new Promise((resolve) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.onchange = () => {
      const file = input.files?.[0]
      if (!file) return resolve(null)
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result as string)
      reader.onerror = () => resolve(null)
      reader.readAsDataURL(file)
    }
    input.click()
  })
}

/** Render each PDF page to an image data URL, ready for OCR. */
export async function pdfPagesToImageUrls(data: Uint8Array, maxPages = 10): Promise<string[]> {
  const pdfjs = await import('pdfjs-dist')
  const { default: workerUrl } = await import('pdfjs-dist/build/pdf.worker.min.mjs?url')
  pdfjs.GlobalWorkerOptions.workerSrc = workerUrl

  const task = pdfjs.getDocument({ data })
  const doc = await task.promise
  const pages: string[] = []
  const count = Math.min(doc.numPages, maxPages)
  for (let pageNumber = 1; pageNumber <= count; pageNumber++) {
    const page = await doc.getPage(pageNumber)
    const viewport = page.getViewport({ scale: 2 })
    const canvas = document.createElement('canvas')
    canvas.width = Math.ceil(viewport.width)
    canvas.height = Math.ceil(viewport.height)
    const context = canvas.getContext('2d')
    if (!context) throw new Error('Could not create a canvas for the PDF page')
    await page.render({ canvas, canvasContext: context, viewport }).promise
    pages.push(canvas.toDataURL('image/png'))
    page.cleanup()
  }
  await task.destroy()
  return pages
}

/** Run OCR on an image data URL using tesseract.js, in the given language. */
export async function runOcr(
  imageDataUrl: string,
  language: OcrLanguage = 'eng',
  onProgress?: ProgressFn,
): Promise<OcrResult> {
  const { createWorker } = await import('tesseract.js')
  const worker = await createWorker(language, 1, {
    logger: (m: { status: string; progress: number }) => {
      if (m.status === 'recognizing text' && onProgress) onProgress(m.progress)
    },
  })
  try {
    const { data } = await worker.recognize(imageDataUrl)
    return { text: data.text }
  } finally {
    await worker.terminate()
  }
}
