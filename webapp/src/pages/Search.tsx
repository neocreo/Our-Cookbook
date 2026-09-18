// Search — LIKE-based search with compact category and cookbook filters.
// Shows results using the same layout as Home Favorites.

import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Search as SearchIcon, ChevronDown, ChevronUp } from 'lucide-react'
import { WashedImage } from '../components/WashedImage'
import { Tag } from '../components/Tag'
import { useRecipes } from '../hooks/useRecipes'
import { useCookbooks } from '../hooks/useCookbooks'
import { searchRecipes, allCategories } from '../features/recipe/repository'
import { totalRecipeTime, getFirstImageUrl } from '../types/recipe'
import type { Recipe } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'

function formatTime(minutes: number | null): string | null {
  if (minutes == null) return null
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h} hr ${m} min` : `${h} hr`
}

function getCookbookForRecipe(recipe: Recipe, cookbooks: Cookbook[]): Cookbook | null {
  for (const cb of cookbooks) {
    if (cb.recipeIds.includes(recipe.id)) {
      return cb
    }
  }
  return null
}

function RecipeEntry({ recipe, cookbook }: { recipe: Recipe; cookbook: Cookbook | null }) {
  const time = formatTime(totalRecipeTime(recipe))
  const imageUrl = getFirstImageUrl(recipe)

  return (
    <Link to={`/recipes/${recipe.id}`} style={{ textDecoration: 'none', color: 'inherit', display: 'block' }}>
      <div style={{ display: 'flex', gap: 12, padding: '12px 0', alignItems: 'flex-start' }}>
        <div style={{ flexShrink: 0 }}>
          {imageUrl ? (
            <WashedImage
              src={imageUrl}
              alt={recipe.title}
              style={{ width: 64, height: 64, borderRadius: 'var(--radius-md)' }}
            />
          ) : (
            <div
              style={{
                width: 64,
                height: 64,
                background: 'var(--color-surface-3)',
                borderRadius: 'var(--radius-md)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <span style={{ fontSize: 24, opacity: 0.5 }}>📖</span>
            </div>
          )}
        </div>

        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', gap: 8, alignItems: 'baseline', marginBottom: 4 }}>
            <span style={{ fontSize: 13, color: 'var(--color-text-muted)' }}>
              {cookbook?.name || 'Unknown'}
            </span>
            <span style={{ color: 'var(--color-text-muted)' }}>|</span>
            <span style={{ fontSize: 13, color: 'var(--color-text-muted)' }}>
              {recipe.category}
            </span>
          </div>
          <h3 style={{ margin: '0 0 4px', fontSize: 16, fontWeight: 600, color: 'var(--color-text)' }}>
            {recipe.title}
          </h3>
          <div style={{ display: 'flex', gap: 12, fontSize: 13, color: 'var(--color-text-muted)' }}>
            <span>{recipe.ingredients.length} ingredients</span>
            {time && <span>{time}</span>}
            {recipe.servingSize && <span>{recipe.servingSize} servings</span>}
          </div>
        </div>
      </div>
    </Link>
  )
}

export function Search() {
  const { recipes, loading } = useRecipes()
  const { cookbooks } = useCookbooks()
  const [searchParams, setSearchParams] = useSearchParams()
  const [query, setQuery] = useState(searchParams.get('query') || '')
  const [categoriesExpanded, setCategoriesExpanded] = useState(false)
  const [cookbooksExpanded, setCookbooksExpanded] = useState(false)

  const allCategoriesList = allCategories(recipes)

  // Get selected category from URL
  const activeCategory = searchParams.get('category')
  const activeTag = searchParams.get('tag')
  const activeCookbook = searchParams.get('cookbook')

  // Filter recipes
  let results = searchRecipes(recipes, query)
  if (activeCategory) results = results.filter((r) => r.category === activeCategory)
  if (activeTag) results = results.filter((r) => r.tags.includes(activeTag))
  if (activeCookbook) {
    const cb = cookbooks.find((c) => c.id === activeCookbook)
    if (cb) results = results.filter((r) => cb.recipeIds.includes(r.id))
  }

  function updateQuery(q: string) {
    setQuery(q)
    const params = new URLSearchParams(searchParams)
    if (q) params.set('query', q)
    else params.delete('query')
    setSearchParams(params)
  }

  function updateCategory(c: string | null) {
    const params = new URLSearchParams(searchParams)
    if (c) params.set('category', c)
    else params.delete('category')
    params.delete('query')
    setSearchParams(params)
    setQuery(c || '')
    setCategoriesExpanded(false)
  }

  function updateTag(t: string | null) {
    const params = new URLSearchParams(searchParams)
    if (t) params.set('tag', t)
    else params.delete('tag')
    setSearchParams(params)
  }

  function updateCookbook(c: string | null) {
    const params = new URLSearchParams(searchParams)
    if (c) params.set('cookbook', c)
    else params.delete('cookbook')
    setSearchParams(params)
    setCookbooksExpanded(false)
  }

  return (
    <main className="page">
      <h1 className="page-title">Search</h1>

      {/* Search input */}
      <div style={{ position: 'relative', marginBottom: 12 }}>
        <input
          className="input"
          placeholder="Search recipes, ingredients, tags…"
          value={query}
          onChange={(e) => updateQuery(e.target.value)}
          style={{ paddingLeft: 36 }}
        />
        <SearchIcon
          size={16}
          strokeWidth={2.75}
          style={{ position: 'absolute', left: 12, top: 10, opacity: 0.5 }}
        />
      </div>

      {/* Categories filter - compact by default */}
      {allCategoriesList.length > 0 && (
        <div style={{ marginBottom: categoriesExpanded ? 12 : 0 }}>
          <button
            type="button"
            onClick={() => setCategoriesExpanded(!categoriesExpanded)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              padding: 0,
              width: '100%',
              textAlign: 'left',
            }}
          >
            <span style={{ fontWeight: 500 }}>Categories</span>
            {categoriesExpanded ? <ChevronUp size={16} strokeWidth={2.75} /> : <ChevronDown size={16} strokeWidth={2.75} />}
          </button>
          {categoriesExpanded && (
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 }}>
              {allCategoriesList.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => updateCategory(activeCategory === c ? null : c)}
                  style={{
                    padding: '6px 12px',
                    background: activeCategory === c ? 'var(--color-accent)' : 'var(--color-surface-3)',
                    color: activeCategory === c ? 'var(--color-accent-contrast)' : 'var(--color-text)',
                    border: 'none',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    fontSize: 13,
                  }}
                >
                  {c}
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Cookbooks filter - compact by default */}
      {cookbooks.length > 0 && (
        <div style={{ marginBottom: cookbooksExpanded ? 12 : 0 }}>
          <button
            type="button"
            onClick={() => setCookbooksExpanded(!cookbooksExpanded)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              padding: 0,
              width: '100%',
              textAlign: 'left',
            }}
          >
            <span style={{ fontWeight: 500 }}>Cookbooks</span>
            {cookbooksExpanded ? <ChevronUp size={16} strokeWidth={2.75} /> : <ChevronDown size={16} strokeWidth={2.75} />}
          </button>
          {cookbooksExpanded && (
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 }}>
              {cookbooks.map((cb) => (
                <button
                  key={cb.id}
                  type="button"
                  onClick={() => updateCookbook(activeCookbook === cb.id ? null : cb.id)}
                  style={{
                    padding: '6px 12px',
                    background: activeCookbook === cb.id ? 'var(--color-accent)' : 'var(--color-surface-3)',
                    color: activeCookbook === cb.id ? 'var(--color-accent-contrast)' : 'var(--color-text)',
                    border: 'none',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    fontSize: 13,
                  }}
                >
                  {cb.name}
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Active filters */}
      {(activeCategory || activeTag || activeCookbook) && (
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 16, fontSize: 13 }}>
          {activeCategory && (
            <Tag variant="accent">
              Category: {activeCategory}
              <button
                type="button"
                onClick={() => updateCategory(null)}
                style={{
                  marginLeft: 4,
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: 0,
                }}
              >
                ×
              </button>
            </Tag>
          )}
          {activeTag && (
            <Tag variant="accent">
              Tag: {activeTag}
              <button
                type="button"
                onClick={() => updateTag(null)}
                style={{
                  marginLeft: 4,
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: 0,
                }}
              >
                ×
              </button>
            </Tag>
          )}
          {activeCookbook && (
            <Tag variant="accent">
              Cookbook: {cookbooks.find((c) => c.id === activeCookbook)?.name || activeCookbook}
              <button
                type="button"
                onClick={() => updateCookbook(null)}
                style={{
                  marginLeft: 4,
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: 0,
                }}
              >
                ×
              </button>
            </Tag>
          )}
        </div>
      )}

      {/* Results */}
      <div style={{ marginTop: 12 }}>
        {loading && <p className="text-muted">Loading…</p>}
        {!loading && results.length === 0 && query && (
          <p className="text-muted">No recipes match "{query}".</p>
        )}
        {!loading && results.length === 0 && !query && !activeCategory && !activeTag && !activeCookbook && (
          <p className="text-muted">Enter a search term or select a filter to see results.</p>
        )}
        {results.map((r, index) => (
          <div key={r.id}>
            {index > 0 && (
              <hr
                style={{
                  margin: '0 0 0 76px',
                  border: 0,
                  borderTop: '1px solid var(--color-primary)',
                  opacity: 0.3,
                }}
              />
            )}
            <RecipeEntry recipe={r} cookbook={getCookbookForRecipe(r, cookbooks)} />
          </div>
        ))}
      </div>
    </main>
  )
}
