// BookThumbnail — book-shaped thumbnail for cookbooks, showing 1-4 recipe images

import { WashedImage } from './WashedImage'

interface BookThumbnailProps {
  recipeImageUrls: string[]
  size?: number
}

export function BookThumbnail({ recipeImageUrls, size = 120 }: BookThumbnailProps) {
  // Take up to 4 images
  const images = recipeImageUrls.slice(0, 4)

  // Book shape with shadow
  const bookWidth = size * 0.75
  const bookHeight = size

  return (
    <div
      style={{
        position: 'relative',
        width: bookWidth,
        height: bookHeight,
        background: 'var(--color-surface-3)',
        borderRadius: 'var(--radius-md)',
        boxShadow: 'var(--shadow-md)',
        transform: 'perspective(400px) rotateY(-5deg)',
        overflow: 'hidden',
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {/* Book spine on the left */}
      <div
        style={{
          position: 'absolute',
          left: 0,
          top: 0,
          bottom: 0,
          width: size * 0.1,
          background: 'linear-gradient(90deg, var(--color-surface-4) 0%, var(--color-surface-3) 100%)',
        }}
      />

      {/* Page area */}
      <div
        style={{
          position: 'absolute',
          left: size * 0.1,
          top: size * 0.05,
          right: 0,
          bottom: size * 0.05,
          background: 'var(--color-surface-2)',
          display: 'grid',
          gridTemplateColumns: images.length > 2 ? 'repeat(2, 1fr)' : '1fr',
          gridTemplateRows: images.length > 2 ? 'repeat(2, 1fr)' : '1fr',
          gap: 2,
          padding: 4,
          borderRadius: '0 var(--radius-md) var(--radius-md) 0',
        }}
      >
        {images.length > 0 ? (
          images.map((url, idx) => (
            <WashedImage
              key={idx}
              src={url}
              alt={`Recipe ${idx + 1}`}
              style={{
                width: '100%',
                height: '100%',
                borderRadius: 'var(--radius-sm)',
                objectFit: 'cover',
              }}
            />
          ))
        ) : (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 24,
              opacity: 0.5,
              gridColumn: '1 / -1',
              gridRow: '1 / -1',
            }}
          >
            📖
          </div>
        )}
      </div>

      {/* Book cover decoration - slight shadow on right edge */}
      <div
        style={{
          position: 'absolute',
          right: 0,
          top: 0,
          bottom: 0,
          width: size * 0.05,
          background: 'linear-gradient(90deg, transparent 0%, rgba(0,0,0,0.1) 100%)',
        }}
      />
    </div>
  )
}
