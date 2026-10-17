import { describe, it, expect } from 'vitest'
import { centeredView, clampView, containFit, pinchStep } from '../scanGeometry'

describe('containFit', () => {
  it('fits a landscape image to the box width', () => {
    expect(containFit({ w: 1000, h: 500 }, { w: 400, h: 300 })).toEqual({ w: 400, h: 200 })
  })

  it('fits a portrait image to the box height', () => {
    expect(containFit({ w: 500, h: 1000 }, { w: 400, h: 300 })).toEqual({ w: 150, h: 300 })
  })

  it('returns zero for unknown sizes', () => {
    expect(containFit({ w: 0, h: 100 }, { w: 400, h: 300 })).toEqual({ w: 0, h: 0 })
    expect(containFit({ w: 100, h: 100 }, { w: 0, h: 0 })).toEqual({ w: 0, h: 0 })
  })
})

describe('centeredView', () => {
  it('centers the canvas in the stage at scale 1', () => {
    expect(centeredView({ w: 200, h: 100 }, { w: 400, h: 300 })).toEqual({ x: 100, y: 100, scale: 1 })
  })
})

describe('clampView', () => {
  const canvas = { w: 200, h: 100 }
  const stage = { w: 400, h: 300 }

  it('snaps back to centered at scale 1', () => {
    expect(clampView({ x: -40, y: 12, scale: 1 }, canvas, stage)).toEqual({ x: 100, y: 100, scale: 1 })
  })

  it('keeps a zoomed image covering the stage (no empty gap)', () => {
    // scale 3 → 600x300: x stays within [400-600, 0], y within [300-300, 0]
    expect(clampView({ x: -50, y: 20, scale: 3 }, canvas, stage)).toEqual({ x: -50, y: 0, scale: 3 })
    expect(clampView({ x: 30, y: 0, scale: 3 }, canvas, stage)).toEqual({ x: 0, y: 0, scale: 3 })
  })

  it('allows panning when the zoomed image is smaller than the stage', () => {
    // scale 2 → 400x200: x within [0, 0], y within [0, 100]
    expect(clampView({ x: 5, y: 10, scale: 2 }, canvas, stage)).toEqual({ x: 0, y: 10, scale: 2 })
  })
})

describe('pinchStep', () => {
  const stage = { w: 400, h: 300 }

  it('zooms around the midpoint, keeping the same image point under it', () => {
    // canvas 200x100 centered in the stage → view {x:100, y:100, scale:1}
    const canvas = { w: 200, h: 100 }
    const start = { view: { x: 100, y: 100, scale: 1 }, mid: { x: 200, y: 150 }, dist: 100 }
    const view = pinchStep(start, { x: 200, y: 150 }, 200, canvas, stage)
    // the image point under the start midpoint was (100, 50); at scale 2 it
    // must still sit at the midpoint (200, 150)
    expect(view).toEqual({ x: 0, y: 50, scale: 2 })
    expect(view.x + 2 * 100).toBe(200)
    expect(view.y + 2 * 50).toBe(150)
  })

  it('pans along with the midpoint', () => {
    // canvas 100x50 centered → view {x:150, y:125, scale:1}
    const canvas = { w: 100, h: 50 }
    const start = { view: { x: 150, y: 125, scale: 1 }, mid: { x: 200, y: 150 }, dist: 100 }
    const view = pinchStep(start, { x: 250, y: 150 }, 200, canvas, stage)
    expect(view).toEqual({ x: 150, y: 100, scale: 2 })
  })

  it('clamps the scale to the maximum', () => {
    const canvas = { w: 200, h: 100 }
    const start = { view: { x: 100, y: 100, scale: 1 }, mid: { x: 200, y: 150 }, dist: 100 }
    expect(pinchStep(start, { x: 200, y: 150 }, 10000, canvas, stage).scale).toBe(4)
  })

  it('snaps to centered when pinching out past scale 1', () => {
    const canvas = { w: 200, h: 100 }
    const zoomed = { view: { x: 0, y: 50, scale: 2 }, mid: { x: 200, y: 150 }, dist: 100 }
    expect(pinchStep(zoomed, { x: 200, y: 150 }, 40, canvas, stage)).toEqual({ x: 100, y: 100, scale: 1 })
  })

  it('ignores a zero starting finger distance', () => {
    const start = { view: { x: 0, y: 50, scale: 2 }, mid: { x: 200, y: 150 }, dist: 0 }
    expect(pinchStep(start, { x: 200, y: 150 }, 50, { w: 200, h: 100 }, stage)).toEqual(start.view)
  })
})
