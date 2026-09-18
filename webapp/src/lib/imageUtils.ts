// Image processing utilities for the cookbook app.
// Handles resizing images to a target size and converting to data URI.

/**
 * Resize an image file to a maximum size (maintaining aspect ratio) and return as data URI.
 * Uses createImageBitmap API when available for better performance.
 * Falls back to Image object for browsers without createImageBitmap.
 */
export async function resizeImageToDataURI(
  file: File,
  maxSize: number = 500,
  quality: number = 0.8,
): Promise<string> {
  try {
    // Check if createImageBitmap is available
    if (typeof createImageBitmap !== 'undefined') {
      return await resizeWithCreateImageBitmap(file, maxSize, quality)
    }
    // Fallback for browsers without createImageBitmap
    return await resizeWithImageObject(file, maxSize, quality)
  } catch (error) {
    console.error('[imageUtils] Failed to resize image:', error)
    // If resizing fails, return the original file as data URI
    return await fileToDataURI(file)
  }
}

/**
 * Resize using createImageBitmap API (preferred method).
 */
async function resizeWithCreateImageBitmap(
  file: File,
  maxSize: number,
  quality: number,
): Promise<string> {
  const bitmap = await createImageBitmap(file)
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  
  if (!ctx) {
    throw new Error('Could not get canvas context')
  }

  // Calculate dimensions maintaining aspect ratio
  const ratio = Math.min(maxSize / bitmap.width, maxSize / bitmap.height)
  const newWidth = Math.floor(bitmap.width * ratio)
  const newHeight = Math.floor(bitmap.height * ratio)

  canvas.width = newWidth
  canvas.height = newHeight

  ctx.drawImage(bitmap, 0, 0, newWidth, newHeight)
  return canvas.toDataURL('image/jpeg', quality)
}

/**
 * Fallback resize using Image object.
 */
async function resizeWithImageObject(
  file: File,
  maxSize: number,
  quality: number,
): Promise<string> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    
    if (!ctx) {
      reject(new Error('Could not get canvas context'))
      return
    }

    img.onload = () => {
      try {
        const ratio = Math.min(maxSize / img.width, maxSize / img.height)
        const newWidth = Math.floor(img.width * ratio)
        const newHeight = Math.floor(img.height * ratio)

        canvas.width = newWidth
        canvas.height = newHeight

        ctx.drawImage(img, 0, 0, newWidth, newHeight)
        resolve(canvas.toDataURL('image/jpeg', quality))
      } catch (error) {
        reject(error)
      }
    }

    img.onerror = () => {
      reject(new Error('Failed to load image'))
    }

    img.src = URL.createObjectURL(file)
  })
}

/**
 * Convert a file to data URI without resizing.
 */
async function fileToDataURI(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = () => reject(new Error('Failed to read file'))
    reader.readAsDataURL(file)
  })
}

/**
 * Check if a file is an image.
 */
export function isImageFile(file: File): boolean {
  return file.type.startsWith('image/')
}

/**
 * Get file extension from a data URI.
 */
export function getExtensionFromDataURI(dataURI: string): string {
  const match = dataURI.match(/^data:image\/([a-zA-Z]*);/)
  return match ? match[1].toLowerCase() : 'jpg'
}

/**
 * Convert data URI to blob.
 */
export function dataURIToBlob(dataURI: string): Blob {
  const byteString = atob(dataURI.split(',')[1])
  const mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]
  const ab = new ArrayBuffer(byteString.length)
  const ia = new Uint8Array(ab)
  
  for (let i = 0; i < byteString.length; i++) {
    ia[i] = byteString.charCodeAt(i)
  }
  
  return new Blob([ab], { type: mimeString })
}
