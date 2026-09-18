// ImageSlideshow — displays multiple images with swipe gestures and full-screen capability.

import { useState, useRef, useEffect } from 'react'
import { ArrowLeft, ArrowRight } from 'lucide-react'
import { WashedImage } from './WashedImage'

interface ImageSlideshowProps {
  imageUrls: string[]
  alt: string
  onFullScreen?: (index: number) => void
}

export function ImageSlideshow({ imageUrls, alt }: ImageSlideshowProps) {
  const [currentIndex, setCurrentIndex] = useState(0)
  const [touchStart, setTouchStart] = useState<number | null>(null)
  const [touchEnd, setTouchEnd] = useState<number | null>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  // Handle keyboard navigation
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'ArrowLeft') {
        setCurrentIndex((prev) => (prev === 0 ? imageUrls.length - 1 : prev - 1))
      } else if (e.key === 'ArrowRight') {
        setCurrentIndex((prev) => (prev === imageUrls.length - 1 ? 0 : prev + 1))
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [imageUrls.length])

  if (imageUrls.length === 0) return null
  if (imageUrls.length === 1) {
    return (
      <div style={{ position: 'relative', display: 'inline-block' }}>
        <WashedImage
          src={imageUrls[0]}
          alt={alt}
          style={{ width: '100%', maxHeight: 300, objectFit: 'cover', borderRadius: 'var(--radius-lg)' }}
        />
      </div>
    )
  }

  // Handle swipe gestures
  function handleTouchStart(e: React.TouchEvent) {
    setTouchStart(e.targetTouches[0].clientX)
  }

  function handleTouchMove(e: React.TouchEvent) {
    setTouchEnd(e.targetTouches[0].clientX)
  }

  function handleTouchEnd() {
    if (!touchStart || !touchEnd) return
    const diff = touchStart - touchEnd
    if (Math.abs(diff) > 50) {
      // Swipe threshold of 50px
      if (diff > 0) {
        // Left swipe - next image
        nextImage()
      } else {
        // Right swipe - previous image
        prevImage()
      }
    }
    setTouchStart(null)
    setTouchEnd(null)
  }

  function prevImage() {
    setCurrentIndex((prev) => (prev === 0 ? imageUrls.length - 1 : prev - 1))
  }

  function nextImage() {
    setCurrentIndex((prev) => (prev === imageUrls.length - 1 ? 0 : prev + 1))
  }

  function goToImage(index: number) {
    setCurrentIndex(index)
  }

  return (
    <div
      ref={containerRef}
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
      style={{
        position: 'relative',
        width: '100%',
        maxHeight: 400,
        overflow: 'hidden',
        borderRadius: 'var(--radius-lg)',
      }}
    >
      {/* Slideshow container */}
      <div
        style={{
          display: 'flex',
          transition: 'transform 0.3s ease',
          transform: `translateX(-${currentIndex * 100}%)`,
          height: '100%',
        }}
      >
        {imageUrls.map((url, index) => (
          <div
            key={index}
            style={{
              minWidth: '100%',
              flexShrink: 0,
              position: 'relative',
            }}
          >
            <WashedImage
              src={url}
              alt={`${alt} - Image ${index + 1}`}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          </div>
        ))}
      </div>

      {/* Navigation arrows */}
      <button
        type="button"
        onClick={prevImage}
        style={{
          position: 'absolute',
          left: 12,
          top: '50%',
          transform: 'translateY(-50%)',
          background: 'rgba(0,0,0,0.5)',
          border: 'none',
          borderRadius: '50%',
          width: 40,
          height: 40,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          color: 'white',
          zIndex: 10,
        }}
        aria-label="Previous image"
      >
        <ArrowLeft size={20} strokeWidth={2.75} />
      </button>

      <button
        type="button"
        onClick={nextImage}
        style={{
          position: 'absolute',
          right: 12,
          top: '50%',
          transform: 'translateY(-50%)',
          background: 'rgba(0,0,0,0.5)',
          border: 'none',
          borderRadius: '50%',
          width: 40,
          height: 40,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          color: 'white',
          zIndex: 10,
        }}
        aria-label="Next image"
      >
        <ArrowRight size={20} strokeWidth={2.75} />
      </button>

      {/* Image indicators */}
      <div
        style={{
          position: 'absolute',
          bottom: 12,
          left: '50%',
          transform: 'translateX(-50%)',
          display: 'flex',
          gap: 6,
          zIndex: 10,
        }}
      >
        {imageUrls.map((_, index) => (
          <div
            key={index}
            onClick={() => goToImage(index)}
            style={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              background: currentIndex === index ? 'var(--color-accent)' : 'rgba(255,255,255,0.5)',
              cursor: 'pointer',
            }}
            aria-label={`Go to image ${index + 1}`}
          />
        ))}
      </div>

      {/* Image counter */}
      <div
        style={{
          position: 'absolute',
          bottom: 12,
          right: 12,
          background: 'rgba(0,0,0,0.7)',
          color: 'white',
          padding: '4px 8px',
          borderRadius: 'var(--radius-sm)',
          fontSize: 12,
          zIndex: 10,
        }}
      >
        {currentIndex + 1} / {imageUrls.length}
      </div>
    </div>
  )
}
