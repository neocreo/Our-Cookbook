// Home — the recipe list with tabs for Favorites, Categories, and Latest.
// Live-subscribes to the repository and renders recipe entries with thumbnails.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { WashedImage } from '../components/WashedImage'
import { RecipeFallbackImage } from '../components/RecipeFallbackImage'
import { useRecipes } from '../hooks/useRecipes'
import { useCookbooks } from '../hooks/useCookbooks'
import { totalRecipeTime, getFirstImageUrl } from '../types/recipe'
import { allCategories } from '../features/recipe/repository'
import { useT, formatRecipeTime } from '../i18n'
import type { Recipe } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'

type TabType = 'latest' | 'favorites' | 'categories'

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
  const t = useT()
  const time = formatRecipeTime(totalRecipeTime(recipe), t)
  const imageUrl = getFirstImageUrl(recipe)

  return (
    <Link to={`/recipes/${recipe.id}`} className="link-plain">
      <div className="entry">
        {/* Thumbnail */}
        <div className="entry-thumb">
          {imageUrl ? (
            <WashedImage
              src={imageUrl}
              alt={recipe.title}
              className="thumb thumb-64"
            />
          ) : (
            <RecipeFallbackImage />
          )}
        </div>

        {/* Text content */}
        <div className="entry-body">
          <div className="entry-meta">
            <span>{cookbook?.name || t('common.unknown')}</span>
            <span>|</span>
            <span>{recipe.category}</span>
          </div>
          <h3 className="entry-title">{recipe.title}</h3>
          <div className="entry-stats">
            <span>{t('common.ingredientsCount', { count: recipe.ingredients.length })}</span>
            {time && <span>{time}</span>}
            {recipe.servingSize && <span>{t('common.servingsCount', { count: recipe.servingSize })}</span>}
          </div>
        </div>
      </div>
    </Link>
  )
}

export function Home() {
  const t = useT()
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
    <main className="page">
      {/* Tab Navigation */}
      <div className="tab-row">
        <button
          type="button"
          className={activeTab === 'latest' ? 'btn btn-primary' : 'btn btn-secondary'}
          onClick={() => {
            setActiveTab('latest')
            setSelectedCategory(null)
          }}
        >
          {t('home.latest')}
        </button>
        <button
          type="button"
          className={activeTab === 'favorites' ? 'btn btn-primary' : 'btn btn-secondary'}
          onClick={() => {
            setActiveTab('favorites')
            setSelectedCategory(null)
          }}
        >
          {t('home.favorites')}
        </button>
        <button
          type="button"
          className={activeTab === 'categories' ? 'btn btn-primary' : 'btn btn-secondary'}
          onClick={() => setActiveTab('categories')}
        >
          {t('common.categories')}
        </button>
      </div>

      {/* Categories as clickable buttons — only once the Categories tab is chosen */}
      {activeTab === 'categories' && categories.length > 0 && (
        <div className="chip-row mb-4">
          {categories.map((c) => (
            <button
              key={c}
              type="button"
              className="chip"
              aria-pressed={selectedCategory === c}
              onClick={() => handleCategoryButtonClick(c)}
            >
              {c}
            </button>
          ))}
        </div>
      )}

      {loading && <p className="text-muted">{t('common.loadingRecipes')}</p>}
      {error && <p className="text-muted">{t('home.loadError', { error })}</p>}

      {!loading && filteredRecipes.length === 0 && activeTab !== 'categories' && (
        <p className="text-muted">
          {activeTab === 'favorites' ? t('home.noFavorites') : t('home.noRecipes')}
        </p>
      )}

      {!loading && activeTab === 'categories' && selectedCategory && filteredRecipes.length === 0 && (
        <p className="text-muted">{t('home.noInCategory', { category: selectedCategory })}</p>
      )}

      {/* Recipe entries with separators */}
      <div>
        {displayedRecipes.map((r, index) => (
          <div key={r.id}>
            {index > 0 && <hr className="entry-divider" />}
            <RecipeEntry recipe={r} cookbook={getCookbookForRecipe(r, cookbooks)} />
          </div>
        ))}
      </div>

      {displayedRecipes.length > 0 && filteredRecipes.length > 5 && (
        <p className="text-muted text-center mt-4">
          {t('home.showing', { shown: 5, total: filteredRecipes.length })}
        </p>
      )}

      <Link to="/recipes/new" className="fab" aria-label={t('home.addRecipeAria')}>
        <Plus strokeWidth={2.75} />
      </Link>
    </main>
  )
}
