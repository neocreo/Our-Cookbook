import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { cropImageRegion } from '../imageUtils'

// jsdom has no canvas backend and never loads images. Fake both so the
// source rectangle handed to drawImage can be asserted: this is the proof
// that the region the user marked is exactly the region that gets cropped.
const drawImageCalls: unknown[][] = []

class FakeImage {
  naturalWidth = 1000
  naturalHeight = 500
  onload: (() => void) | null = null
  onerror: (() => void) | null = null
  private _src = ''
  get src() {
    return this._src
  }
  set src(value: string) {
    this._src = value
    queueMicrotask(() => this.onload?.())
  }
}

beforeEach(() => {
  drawImageCalls.length = 0
  vi.stubGlobal('Image', FakeImage)
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockImplementation(() => ({
    drawImage: (...args: unknown[]) => {
      drawImageCalls.push(args)
    },
  }) as unknown as CanvasRenderingContext2D)
  vi.spyOn(HTMLCanvasElement.prototype, 'toDataURL').mockReturnValue('data:image/jpeg;base64,fake')
})

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
})

function sourceRect(call: unknown[]) {
  const [, sx, sy, sw, sh] = call as [unknown, number, number, number, number]
  return [sx, sy, sw, sh]
}

function destRect(call: unknown[]) {
  const [, , , , , dx, dy, dw, dh] = call as [unknown, number, number, number, number, number, number, number, number]
  return [dx, dy, dw, dh]
}

describe('cropImageRegion', () => {
  it('crops exactly the marked region (normalized coords → source pixels)', async () => {
    // image is 1000x500: x=0.25 → 250, y=0.4 → 200, w=0.5 → 500, h=0.2 → 100
    await cropImageRegion('data:image/png;base64,x', { x: 0.25, y: 0.4, w: 0.5, h: 0.2 })
    expect(drawImageCalls).toHaveLength(1)
    expect(sourceRect(drawImageCalls[0])).toEqual([250, 200, 500, 100])
    expect(destRect(drawImageCalls[0])).toEqual([0, 0, 500, 100])
  })

  it('upscales the crop by the scale factor without moving it', async () => {
    await cropImageRegion('data:image/png;base64,x', { x: 0.25, y: 0.4, w: 0.5, h: 0.2 }, 2)
    expect(sourceRect(drawImageCalls[0])).toEqual([250, 200, 500, 100])
    expect(destRect(drawImageCalls[0])).toEqual([0, 0, 1000, 200])
  })

  it('crops the full image for a full-size region', async () => {
    await cropImageRegion('data:image/png;base64,x', { x: 0, y: 0, w: 1, h: 1 })
    expect(sourceRect(drawImageCalls[0])).toEqual([0, 0, 1000, 500])
  })

  it('clamps the region to the image bounds', async () => {
    await cropImageRegion('data:image/png;base64,x', { x: -0.5, y: 0.9, w: 2, h: 2 })
    expect(sourceRect(drawImageCalls[0])).toEqual([0, 450, 1000, 50])
  })
})
