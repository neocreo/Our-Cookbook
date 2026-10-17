// ScanStage — the image stage for the scan flow's crop and region-marking
// steps. One finger drags a selection rectangle, reported in normalized
// (0..1) image coordinates; two fingers pinch to zoom and pan, like a map.
// The selection layer sits inside the transformed canvas, so pointer
// positions map onto image space at any zoom level. In the regions step a
// finished drag is committed immediately (onCommit); in the crop step the
// draft is kept until the parent confirms it.

import { useEffect, useRef, useState } from 'react'
import type { CSSProperties } from 'react'
import type { ImageRegion } from '../lib/imageUtils'
import { centeredView, containFit, pinchStep } from '../lib/scanGeometry'
import type { Point, Size, View } from '../lib/scanGeometry'

export interface ConfirmedRegion {
  region: ImageRegion
  color: string
}

interface ScanStageProps {
  src: string
  alt: string
  /** Rectangle currently being drawn (controlled by the parent). */
  value: ImageRegion | null
  onChange: (region: ImageRegion | null) => void
  /** Regions step: a finished, large-enough drag is committed at once. */
  onCommit?: (region: ImageRegion) => void
  /** CSS colour of the rectangle being drawn. */
  color: string
  /** Already-committed rectangles, rendered in their own colours. */
  confirmed?: ConfirmedRegion[]
}

/** Smaller drags count as taps and are discarded. */
const MIN_REGION = 0.02

function clamp01(n: number) {
  return Math.min(1, Math.max(0, n))
}

function rectStyle(region: ImageRegion, color: string): CSSProperties {
  return {
    '--rx': region.x,
    '--ry': region.y,
    '--rw': region.w,
    '--rh': region.h,
    '--region-color': color,
  } as CSSProperties
}

export function ScanStage({ src, alt, value, onChange, onCommit, color, confirmed = [] }: ScanStageProps) {
  const stageRef = useRef<HTMLDivElement>(null)
  const canvasRef = useRef<HTMLDivElement>(null)
  const [stageSize, setStageSize] = useState<Size>({ w: 0, h: 0 })
  const [loaded, setLoaded] = useState<{ src: string; size: Size } | null>(null)
  // The view the user zoomed to, remembered together with the canvas and
  // stage sizes it belongs to; when those change, the view falls back to
  // centered (derived below, no reset effect needed).
  const [zoom, setZoom] = useState<{ canvas: Size; stage: Size; view: View } | null>(null)
  const pointersRef = useRef(new Map<number, Point>())
  const drawStartRef = useRef<Point | null>(null)
  const pinchStartRef = useRef<{ view: View; mid: Point; dist: number } | null>(null)

  const natural = loaded !== null && loaded.src === src ? loaded.size : { w: 0, h: 0 }
  const canvas = containFit(natural, stageSize)
  const ready = canvas.w > 0 && canvas.h > 0
  const view =
    zoom !== null &&
    zoom.canvas.w === canvas.w && zoom.canvas.h === canvas.h &&
    zoom.stage.w === stageSize.w && zoom.stage.h === stageSize.h
      ? zoom.view
      : centeredView(canvas, stageSize)

  // Follow the stage size (rotation, on-screen keyboard, split screen).
  useEffect(() => {
    const el = stageRef.current
    if (!el || typeof ResizeObserver === 'undefined') return
    const ro = new ResizeObserver((entries) => {
      const rect = entries[0].contentRect
      setStageSize({ w: rect.width, h: rect.height })
    })
    ro.observe(el)
    return () => ro.disconnect()
  }, [])

  function onImageLoad(e: React.SyntheticEvent<HTMLImageElement>) {
    const img = e.currentTarget
    setLoaded({ src, size: { w: img.naturalWidth, h: img.naturalHeight } })
  }

  function toStage(clientX: number, clientY: number): Point {
    const el = stageRef.current
    if (!el) return { x: 0, y: 0 }
    const rect = el.getBoundingClientRect()
    return { x: clientX - rect.left, y: clientY - rect.top }
  }

  // The canvas is transformed by the view, so its bounding rect already
  // includes zoom and pan: dividing by it yields image coordinates.
  function toNormalized(clientX: number, clientY: number): Point {
    const el = canvasRef.current
    if (!el) return { x: 0, y: 0 }
    const rect = el.getBoundingClientRect()
    if (rect.width <= 0 || rect.height <= 0) return { x: 0, y: 0 }
    return {
      x: clamp01((clientX - rect.left) / rect.width),
      y: clamp01((clientY - rect.top) / rect.height),
    }
  }

  function pinchSnapshot(): { mid: Point; dist: number } {
    const pts = [...pointersRef.current.values()]
    return {
      mid: { x: (pts[0].x + pts[1].x) / 2, y: (pts[0].y + pts[1].y) / 2 },
      dist: Math.hypot(pts[0].x - pts[1].x, pts[0].y - pts[1].y),
    }
  }

  function handlePointerDown(e: React.PointerEvent<HTMLDivElement>) {
    if (!ready) return
    e.preventDefault()
    pointersRef.current.set(e.pointerId, toStage(e.clientX, e.clientY))
    if (e.currentTarget.setPointerCapture) {
      e.currentTarget.setPointerCapture(e.pointerId)
    }
    if (pointersRef.current.size === 1) {
      const start = toNormalized(e.clientX, e.clientY)
      drawStartRef.current = start
      onChange({ x: start.x, y: start.y, w: 0, h: 0 })
    } else if (pointersRef.current.size === 2) {
      // Second finger: switch to pinch and drop any half-drawn rectangle.
      drawStartRef.current = null
      onChange(null)
      const { mid, dist } = pinchSnapshot()
      pinchStartRef.current = { view, mid, dist }
    }
  }

  function handlePointerMove(e: React.PointerEvent<HTMLDivElement>) {
    if (!pointersRef.current.has(e.pointerId)) return
    pointersRef.current.set(e.pointerId, toStage(e.clientX, e.clientY))
    const pinchStart = pinchStartRef.current
    if (pointersRef.current.size >= 2 && pinchStart) {
      const { mid, dist } = pinchSnapshot()
      const next = pinchStep(pinchStart, mid, dist, canvas, stageSize)
      setZoom({ canvas, stage: stageSize, view: next })
      return
    }
    const start = drawStartRef.current
    if (!start) return
    const current = toNormalized(e.clientX, e.clientY)
    onChange({
      x: Math.min(start.x, current.x),
      y: Math.min(start.y, current.y),
      w: Math.abs(current.x - start.x),
      h: Math.abs(current.y - start.y),
    })
  }

  function handlePointerUp(e: React.PointerEvent<HTMLDivElement>) {
    pointersRef.current.delete(e.pointerId)
    if (e.currentTarget.hasPointerCapture?.(e.pointerId)) {
      e.currentTarget.releasePointerCapture(e.pointerId)
    }
    if (pointersRef.current.size < 2) {
      pinchStartRef.current = null
    }
    if (pointersRef.current.size > 0) return
    const start = drawStartRef.current
    drawStartRef.current = null
    if (!start) return
    const end = toNormalized(e.clientX, e.clientY)
    const region: ImageRegion = {
      x: Math.min(start.x, end.x),
      y: Math.min(start.y, end.y),
      w: Math.abs(end.x - start.x),
      h: Math.abs(end.y - start.y),
    }
    if (region.w < MIN_REGION || region.h < MIN_REGION) {
      // A tap, not a drag: discard (the crop step falls back to the full image).
      onChange(null)
      return
    }
    if (onCommit) {
      onCommit(region)
      onChange(null)
    } else {
      onChange(region)
    }
  }

  return (
    <div
      ref={stageRef}
      className="scan-stage"
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onPointerCancel={handlePointerUp}
    >
      <div
        ref={canvasRef}
        className="scan-canvas"
        style={{
          left: view.x,
          top: view.y,
          width: canvas.w,
          height: canvas.h,
          transform: `scale(${view.scale})`,
          display: ready ? undefined : 'none',
        }}
      >
        <img src={src} alt={alt} className="scan-image" draggable={false} onLoad={onImageLoad} />
        <div className="scan-overlay">
          {confirmed.map((c, i) => (
            <div key={i} className="scan-rect" style={rectStyle(c.region, c.color)} />
          ))}
          {value && value.w > 0 && value.h > 0 && (
            <div className="scan-rect scan-rect-active" style={rectStyle(value, color)} />
          )}
        </div>
      </div>
    </div>
  )
}
