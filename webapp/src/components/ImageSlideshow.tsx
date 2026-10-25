// ImageSlideshow — displays multiple images with swipe gestures and full-screen capability.

import { useT } from '../i18n'
import { useState, useRef, useEffect } from 'react'
import { ArrowLeft, ArrowRight } from 'lucide-react'
import { WashedImage } from './WashedImage'

interface ImageSlideshowProps {
  imageUrls: string[]
  alt: string
  onFullScreen?: (index: number) => void
}

export function ImageSlideshow({ imageUrls, alt }: ImageSlideshowProps) {
  const t = useT()
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
      <div className="slideshow-single">
        <WashedImage
          src={imageUrls[0]}
          alt={alt}
          className="img-cover slideshow-single-img"
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
      className="slideshow"
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
    >
      {/* Slideshow container — the track offset is dynamic, so it stays inline */}
      <div
        className="slideshow-track"
        style={{ transform: `translateX(-${currentIndex * 100}%)` }}
      >
        {imageUrls.map((url, index) => (
          <div key={index} className="slideshow-slide">
            <WashedImage
              src={url}
              alt={`${alt} - Image ${index + 1}`}
              className="slideshow-img"
            />
          </div>
        ))}
      </div>

      {/* Navigation arrows */}
      <button
        type="button"
        className="slideshow-nav slideshow-nav-prev"
        onClick={prevImage}
        aria-label={t('slideshow.previousAria')}
      >
        <ArrowLeft size={20} strokeWidth={2.75} />
      </button>

      <button
        type="button"
        className="slideshow-nav slideshow-nav-next"
        onClick={nextImage}
        aria-label={t('slideshow.nextAria')}
      >
        <ArrowRight size={20} strokeWidth={2.75} />
      </button>

      {/* Image indicators */}
      <div className="slideshow-dots">
        {imageUrls.map((_, index) => (
          <div
            key={index}
            className={['slideshow-dot', currentIndex === index ? 'slideshow-dot-active' : ''].filter(Boolean).join(' ')}
            onClick={() => goToImage(index)}
            aria-label={`Go to image ${index + 1}`}
          />
        ))}
      </div>

      {/* Image counter */}
      <div className="slideshow-counter">
        {currentIndex + 1} / {imageUrls.length}
      </div>
    </div>
  )
}
