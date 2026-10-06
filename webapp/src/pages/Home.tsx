// Home — the recipe list with tabs for Favorites, Categories, and Latest.
// Live-subscribes to the repository and renders recipe entries with thumbnails.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { WashedImage } from '../components/WashedImage'
import { useRecipes } from '../hooks/useRecipes'
import { useCookbooks } from '../hooks/useCookbooks'
import { totalRecipeTime, getFirstImageUrl } from '../types/recipe'
import { allCategories } from '../features/recipe/repository'
import type { Recipe } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'

type TabType = 'latest' | 'favorites' | 'categories'

function formatTime(minutes: number | null): string | null {
  if (minutes == null) return null
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h} hr ${m} min` : `${h} hr`
}

function getCookbookForRecipe(recipe: Recipe, cookbooks: Cookbook[]): Cookbook | null {
  // Find which cookbook contains this recipe
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
        {/* Thumbnail */}
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

        {/* Text content */}
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

export function Home() {
  const { recipes, loading, error } = useRecipes()
  const { cookbooks } = useCookbooks()
  const [activeTab, setActiveTab] = useState<TabType>('latest')
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null)

  const categories = allCategories(recipes)

  // Sort recipes by updatedAt (latest first)
  const sortedRecipes = [...recipes].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))

  // Filter recipes based on active tab
  const filteredRecipes = (() => {
    switch (activeTab) {
      case 'favorites':
        return sortedRecipes.filter((r) => r.isFavorite)
      case 'categories':
        return selectedCategory
          ? sortedRecipes.filter((r) => r.category === selectedCategory)
          : []
      case 'latest':
      default:
        return sortedRecipes
    }
  })()

  // Limit to 5 entries
  const displayedRecipes = filteredRecipes.slice(0, 5)

  const handleCategoryButtonClick = (category: string) => {
    setSelectedCategory(category)
    setActiveTab('categories')
  }

  return (
    <main className="page" style={{ paddingTop: 15 }}>
      {/* Tab Navigation */}
      <div
        style={{
          display: 'flex',
          gap: 4,
          marginBottom: 16,
          borderBottom: '1px solid var(--color-surface-4)',
          paddingBottom: 8,
        }}
      >
        <button
          type="button"
          className={activeTab === 'latest' ? 'btn btn-primary' : 'btn btn-secondary'}
          onClick={() => {
            setActiveTab('latest')
            setSelectedCategory(null)
          }}
          style={{ flex: 1 }}
        >
          Latest
        </button>
        <button
          type="button"
          className={activeTab === 'favorites' ? 'btn btn-primary' : 'btn btn-secondary'}
          onClick={() => {
            setActiveTab('favorites')
            setSelectedCategory(null)
          }}
          style={{ flex: 1 }}
        >
          Favorites
        </button>
        <button
          type="button"
          className={activeTab === 'categories' ? 'btn btn-primary' : 'btn btn-secondary'}
          onClick={() => setActiveTab('categories')}
          style={{ flex: 1 }}
        >
          Categories
        </button>
      </div>

      {/* Categories as clickable buttons — only once the Categories tab is chosen */}
      {activeTab === 'categories' && categories.length > 0 && (
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 16 }}>
          {categories.map((c) => (
            <button
              key={c}
              type="button"
              onClick={() => handleCategoryButtonClick(c)}
              style={{
                padding: '6px 12px',
                background: selectedCategory === c ? 'var(--color-accent)' : 'var(--color-surface-3)',
                color: selectedCategory === c ? 'var(--color-accent-contrast)' : 'var(--color-text)',
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

      {loading && <p className="text-muted">Loading recipes…</p>}
      {error && <p className="text-muted">Could not load recipes: {error}</p>}

      {!loading && filteredRecipes.length === 0 && activeTab !== 'categories' && (
        <p className="text-muted">
          {activeTab === 'favorites'
            ? 'No favorite recipes yet. Tap the star on a recipe to add it here.'
            : 'No recipes yet. Tap the + button to add your first one.'}
        </p>
      )}

      {!loading && activeTab === 'categories' && selectedCategory && filteredRecipes.length === 0 && (
        <p className="text-muted">No recipes in category "{selectedCategory}".</p>
      )}

      {/* Recipe entries with separators */}
      <div>
        {displayedRecipes.map((r, index) => (
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

      {displayedRecipes.length > 0 && filteredRecipes.length > 5 && (
        <p style={{ textAlign: 'center', color: 'var(--color-text-muted)', marginTop: 16 }}>
          Showing 5 of {filteredRecipes.length} recipes
        </p>
      )}

      <Link to="/recipes/new" className="fab" aria-label="Add recipe">
        <Plus strokeWidth={2.75} />
      </Link>
    </main>
  )
}
