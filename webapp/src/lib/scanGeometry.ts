// scanGeometry — pure math for the scan stage (crop & region marking):
// contain-fit sizing, zoom/pan clamping and pinch steps. Kept free of DOM
// and React so it can be unit-tested directly.

export interface Size {
  w: number
  h: number
}

export interface Point {
  x: number
  y: number
}

/** Canvas position (top-left corner, stage coordinates) and zoom scale. */
export interface View {
  x: number
  y: number
  scale: number
}

export const MIN_SCALE = 1
export const MAX_SCALE = 4

function clamp(n: number, lo: number, hi: number) {
  return Math.min(hi, Math.max(lo, n))
}

/** Largest size with the aspect ratio of `natural` that fits inside `box`. */
export function containFit(natural: Size, box: Size): Size {
  if (natural.w <= 0 || natural.h <= 0 || box.w <= 0 || box.h <= 0) {
    return { w: 0, h: 0 }
  }
  const ar = natural.w / natural.h
  let w = box.w
  let h = w / ar
  if (h > box.h) {
    h = box.h
    w = h * ar
  }
  return { w, h }
}

/** The resting view: scale 1, canvas centered in the stage. */
export function centeredView(canvas: Size, stage: Size): View {
  return {
    x: (stage.w - canvas.w) / 2,
    y: (stage.h - canvas.h) / 2,
    scale: MIN_SCALE,
  }
}

/** Keep the zoomed canvas over the stage: when the image is larger than the
    stage there is no empty gap; at scale 1 the view snaps back to centered. */
export function clampView(view: View, canvas: Size, stage: Size): View {
  if (view.scale <= MIN_SCALE) return centeredView(canvas, stage)
  const w = canvas.w * view.scale
  const h = canvas.h * view.scale
  return {
    scale: view.scale,
    x: clamp(view.x, Math.min(0, stage.w - w), Math.max(0, stage.w - w)),
    y: clamp(view.y, Math.min(0, stage.h - h), Math.max(0, stage.h - h)),
  }
}

/** One pinch step: zoom around the midpoint, which may also move (pan).
    `start` holds the view, midpoint and finger distance at gesture start;
    `mid`/`dist` are the current values. All points are stage coordinates. */
export function pinchStep(
  start: { view: View; mid: Point; dist: number },
  mid: Point,
  dist: number,
  canvas: Size,
  stage: Size,
): View {
  if (start.dist <= 0) return start.view
  const scale = clamp(start.view.scale * (dist / start.dist), MIN_SCALE, MAX_SCALE)
  // Image point under the midpoint when the gesture started.
  const px = (start.mid.x - start.view.x) / start.view.scale
  const py = (start.mid.y - start.view.y) / start.view.scale
  // Place that same image point under the current midpoint.
  return clampView({ x: mid.x - scale * px, y: mid.y - scale * py, scale }, canvas, stage)
}
