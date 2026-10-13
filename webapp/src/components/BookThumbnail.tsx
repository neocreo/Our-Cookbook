// BookThumbnail — tilted thumbnail card for cookbooks, showing 1-4 recipe
// images in a grid directly inside the card. Cookbooks without any recipe
// images fall back to a bundled cover illustration. The size prop feeds the
// --book-size variable; every proportion derives from it in global.css.

import type { CSSProperties } from 'react'
import { WashedImage } from './WashedImage'

interface BookThumbnailProps {
  recipeImageUrls: string[]
  size?: number
}

export function BookThumbnail({ recipeImageUrls, size = 120 }: BookThumbnailProps) {
  // Take up to 4 images
  const images = recipeImageUrls.slice(0, 4)
  // The one dynamic value: the base size everything else scales from.
  const bookStyle = { '--book-size': `${size}px` } as CSSProperties

  return (
    <div
      className={['book-thumbnail', images.length > 2 ? 'book-thumbnail-grid' : ''].filter(Boolean).join(' ')}
      style={bookStyle}
    >
      {images.length > 0 ? (
        images.map((url, idx) => (
          <WashedImage
            key={idx}
            src={url}
            alt={`Recipe ${idx + 1}`}
            className="img-fill"
          />
        ))
      ) : (
        <img
          src="/cookbook-cover-fallback.png"
          alt=""
          className="img-fill book-cover-fallback"
        />
      )}
    </div>
  )
}
