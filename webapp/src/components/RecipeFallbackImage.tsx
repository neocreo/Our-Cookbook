// RecipeFallbackImage — the bundled illustration shown as the icon wherever
// a recipe has no photo: list thumbnails (Home, Search, CookbookDetail).
// Mirrors the cookbook fallback in BookThumbnail. Class name carries the
// size variant (thumb thumb-64, thumb thumb-48).

interface RecipeFallbackImageProps {
  className?: string
}

export function RecipeFallbackImage({ className = 'thumb thumb-64' }: RecipeFallbackImageProps) {
  return <img src="/recipe-fallback.png" alt="" className={className} />
}
