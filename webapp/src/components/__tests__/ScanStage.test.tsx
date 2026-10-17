import { describe, it, expect, beforeAll, vi } from 'vitest'
import { render, fireEvent } from '@testing-library/react'
import { ScanStage } from '../ScanStage'

// The stage is a 400x300 box; the test image is 1000x500, so the canvas is
// 400x200 centered → its top edge sits at y=50 in stage coordinates.
const STAGE = { w: 400, h: 300 }

// jsdom has no ResizeObserver and no pointer capture; stub both.
beforeAll(() => {
  vi.stubGlobal('ResizeObserver', class {
    callback: ResizeObserverCallback
    constructor(callback: ResizeObserverCallback) {
      this.callback = callback
    }
    observe(_target: Element) {
      const entry = { contentRect: { width: STAGE.w, height: STAGE.h } } as unknown as ResizeObserverEntry
      this.callback([entry], this as unknown as ResizeObserver)
    }
    unobserve() {}
    disconnect() {}
  })
  Element.prototype.setPointerCapture = () => {}
  Element.prototype.releasePointerCapture = () => {}
  Element.prototype.hasPointerCapture = () => false
})

function rect(left: number, top: number, width: number, height: number): DOMRect {
  return {
    left, top, width, height,
    right: left + width, bottom: top + height,
    x: left, y: top,
    toJSON: () => ({}),
  } as DOMRect
}

// jsdom has no PointerEvent; a MouseEvent with a pointerId works the same
// for React's pointer handlers.
function pointer(type: string, pointerId: number, x: number, y: number) {
  const e = new MouseEvent(type, { clientX: x, clientY: y, bubbles: true, cancelable: true })
  Object.defineProperty(e, 'pointerId', { value: pointerId })
  return e
}

function renderStage(props: Partial<React.ComponentProps<typeof ScanStage>> = {}) {
  const onChange = vi.fn()
  const onCommit = vi.fn()
  const utils = render(
    <ScanStage
      src="data:image/png;base64,test"
      alt="recipe"
      value={null}
      onChange={onChange}
      onCommit={onCommit}
      color="var(--color-region-title)"
      {...props}
    />,
  )
  const stage = utils.container.querySelector('.scan-stage') as HTMLDivElement
  const canvas = utils.container.querySelector('.scan-canvas') as HTMLDivElement
  const img = utils.container.querySelector('img') as HTMLImageElement
  // jsdom neither loads images nor does layout; provide both.
  Object.defineProperty(img, 'naturalWidth', { value: 1000, configurable: true })
  Object.defineProperty(img, 'naturalHeight', { value: 500, configurable: true })
  stage.getBoundingClientRect = () => rect(0, 0, STAGE.w, STAGE.h)
  canvas.getBoundingClientRect = () => {
    const left = parseFloat(canvas.style.left) || 0
    const top = parseFloat(canvas.style.top) || 0
    const w = parseFloat(canvas.style.width) || 0
    const h = parseFloat(canvas.style.height) || 0
    const m = /scale\(([\d.]+)\)/.exec(canvas.style.transform)
    const s = m ? parseFloat(m[1]) : 1
    return rect(left, top, w * s, h * s)
  }
  fireEvent.load(img)
  return { ...utils, stage, canvas, onChange, onCommit }
}

describe('ScanStage', () => {
  it('renders every confirmed rectangle in its own colour', () => {
    const confirmed = [
      { region: { x: 0.1, y: 0.1, w: 0.5, h: 0.2 }, color: 'var(--color-region-title)' },
      { region: { x: 0.1, y: 0.4, w: 0.5, h: 0.5 }, color: 'var(--color-region-ingredients)' },
      { region: { x: 0.2, y: 0.6, w: 0.3, h: 0.3 }, color: 'var(--color-region-instructions)' },
    ]
    const { container } = renderStage({ confirmed, onCommit: undefined })
    const rects = container.querySelectorAll('.scan-rect')
    expect(rects).toHaveLength(3)
    const first = rects[0] as HTMLElement
    expect(first.style.getPropertyValue('--rx')).toBe('0.1')
    expect(first.style.getPropertyValue('--ry')).toBe('0.1')
    expect(first.style.getPropertyValue('--rw')).toBe('0.5')
    expect(first.style.getPropertyValue('--rh')).toBe('0.2')
    expect(first.style.getPropertyValue('--region-color')).toBe('var(--color-region-title)')
    expect((rects[2] as HTMLElement).style.getPropertyValue('--region-color')).toBe('var(--color-region-instructions)')
  })

  it('commits the drawn rectangle on release (regions step)', () => {
    const { stage, onChange, onCommit } = renderStage()
    fireEvent(stage, pointer('pointerdown', 1, 100, 100))
    fireEvent(stage, pointer('pointermove', 1, 200, 180))
    fireEvent(stage, pointer('pointerup', 1, 200, 180))
    // canvas 400x200 at y=50: (100,100) → (0.25, 0.25), (200,180) → (0.5, 0.65)
    expect(onCommit).toHaveBeenCalledWith({ x: 0.25, y: 0.25, w: 0.25, h: 0.4 })
    expect(onChange).toHaveBeenLastCalledWith(null)
  })

  it('keeps the drawn rectangle as a draft when there is no onCommit (crop step)', () => {
    const { stage, onChange, onCommit } = renderStage({ onCommit: undefined })
    fireEvent(stage, pointer('pointerdown', 1, 100, 100))
    fireEvent(stage, pointer('pointermove', 1, 200, 180))
    fireEvent(stage, pointer('pointerup', 1, 200, 180))
    expect(onCommit).not.toHaveBeenCalled()
    expect(onChange).toHaveBeenLastCalledWith({ x: 0.25, y: 0.25, w: 0.25, h: 0.4 })
  })

  it('discards a tap without a drag', () => {
    const { stage, onChange, onCommit } = renderStage()
    fireEvent(stage, pointer('pointerdown', 1, 100, 100))
    fireEvent(stage, pointer('pointerup', 1, 100, 100))
    expect(onCommit).not.toHaveBeenCalled()
    expect(onChange).toHaveBeenLastCalledWith(null)
  })

  it('zooms with a two-finger pinch and stops drawing', () => {
    const { stage, canvas, onChange } = renderStage()
    fireEvent(stage, pointer('pointerdown', 1, 150, 150))
    fireEvent(stage, pointer('pointerdown', 2, 250, 150))
    // the second finger cancels the half-drawn rectangle
    expect(onChange).toHaveBeenLastCalledWith(null)
    fireEvent(stage, pointer('pointermove', 2, 350, 150))
    expect(canvas.style.transform).toBe('scale(2)')
  })

  it('returns to scale 1 when pinching out', () => {
    const { stage, canvas } = renderStage()
    fireEvent(stage, pointer('pointerdown', 1, 150, 150))
    fireEvent(stage, pointer('pointerdown', 2, 250, 150))
    fireEvent(stage, pointer('pointermove', 2, 350, 150))
    expect(canvas.style.transform).toBe('scale(2)')
    fireEvent(stage, pointer('pointermove', 2, 190, 150))
    expect(canvas.style.transform).toBe('scale(1)')
  })

  it('can draw again after a pinch', () => {
    const { stage, onCommit } = renderStage()
    fireEvent(stage, pointer('pointerdown', 1, 150, 150))
    fireEvent(stage, pointer('pointerdown', 2, 250, 150))
    fireEvent(stage, pointer('pointerup', 2, 250, 150))
    fireEvent(stage, pointer('pointerup', 1, 150, 150))
    fireEvent(stage, pointer('pointerdown', 3, 100, 100))
    fireEvent(stage, pointer('pointermove', 3, 200, 180))
    fireEvent(stage, pointer('pointerup', 3, 200, 180))
    expect(onCommit).toHaveBeenCalledWith({ x: 0.25, y: 0.25, w: 0.25, h: 0.4 })
  })

  it('maps the drawn rectangle to image coordinates when zoomed', () => {
    const { stage, onCommit } = renderStage()
    // zoom 2x around the pinch midpoint (250,150): the 400x200 canvas
    // becomes 800x400 at (-150,-50)
    fireEvent(stage, pointer('pointerdown', 1, 150, 150))
    fireEvent(stage, pointer('pointerdown', 2, 250, 150))
    fireEvent(stage, pointer('pointermove', 2, 350, 150))
    fireEvent(stage, pointer('pointerup', 2, 350, 150))
    fireEvent(stage, pointer('pointerup', 1, 150, 150))
    // screen (200,100) → canvas (175,75) → image (0.4375, 0.375);
    // screen (400,300) → canvas (275,175) → image (0.6875, 0.875)
    fireEvent(stage, pointer('pointerdown', 3, 200, 100))
    fireEvent(stage, pointer('pointermove', 3, 400, 300))
    fireEvent(stage, pointer('pointerup', 3, 400, 300))
    expect(onCommit).toHaveBeenCalledWith({ x: 0.4375, y: 0.375, w: 0.25, h: 0.5 })
  })
})
