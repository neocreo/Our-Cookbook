// OCR service — abstracts image capture and text recognition across
// platforms. On mobile, @capacitor/camera takes the photo; on web, a file
// input lets the user pick an image. OCR runs via tesseract.js in both cases
// (works in browser and Capacitor WebView). When Capacitor 8 is available,
// ML Kit can replace tesseract on mobile for better accuracy.

import { Capacitor } from '@capacitor/core'
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera'

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

/** Run OCR on an image data URL using tesseract.js. */
export async function runOcr(
  imageDataUrl: string,
  onProgress?: ProgressFn,
): Promise<OcrResult> {
  const { createWorker } = await import('tesseract.js')
  const worker = await createWorker('eng', 1, {
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
