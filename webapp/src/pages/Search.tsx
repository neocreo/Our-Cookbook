// Search — LIKE-based search with compact category and cookbook filters.
// Shows results using the same layout as Home Favorites.

import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Search as SearchIcon, ChevronDown, ChevronUp } from 'lucide-react'
import { WashedImage } from '../components/WashedImage'
import { RecipeFallbackImage } from '../components/RecipeFallbackImage'
import { Tag } from '../components/Tag'
import { useRecipes } from '../hooks/useRecipes'
import { useCookbooks } from '../hooks/useCookbooks'
import { searchRecipes, allCategories } from '../features/recipe/repository'
import { totalRecipeTime, getFirstImageUrl } from '../types/recipe'
import { useT, formatRecipeTime } from '../i18n'
import type { Recipe } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'

function getCookbookForRecipe(recipe: Recipe, cookbooks: Cookbook[]): Cookbook | null {
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

export function Search() {
  const t = useT()
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
      <h1 className="page-title">{t('nav.recipes')}</h1>

      {/* Search input */}
      <div className="search-field">
        <input
          className="input search-input"
          placeholder={t('search.placeholder')}
          value={query}
          onChange={(e) => updateQuery(e.target.value)}
        />
        <SearchIcon
          size={16}
          strokeWidth={2.75}
          className="search-icon"
        />
      </div>

      {/* Filter pickers — side by side, fold out on tap */}
      <div className="filter-pickers">
        {allCategoriesList.length > 0 && (
          <button
            type="button"
            className={`btn ${activeCategory ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setCategoriesExpanded(!categoriesExpanded)}
            aria-expanded={categoriesExpanded}
            aria-pressed={Boolean(activeCategory)}
          >
            <span className="ellipsis">
              {activeCategory || t('common.categories')}
            </span>
            {categoriesExpanded ? <ChevronUp size={16} strokeWidth={2.75} /> : <ChevronDown size={16} strokeWidth={2.75} />}
          </button>
        )}
        {cookbooks.length > 0 && (
          <button
            type="button"
            className={`btn ${activeCookbook ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setCookbooksExpanded(!cookbooksExpanded)}
            aria-expanded={cookbooksExpanded}
            aria-pressed={Boolean(activeCookbook)}
          >
            <span className="ellipsis">
              {cookbooks.find((c) => c.id === activeCookbook)?.name || t('nav.cookbooks')}
            </span>
            {cookbooksExpanded ? <ChevronUp size={16} strokeWidth={2.75} /> : <ChevronDown size={16} strokeWidth={2.75} />}
          </button>
        )}
      </div>

      {/* Categories filter options */}
      {allCategoriesList.length > 0 && categoriesExpanded && (
        <div className="chip-row mb-3">
          {allCategoriesList.map((c) => (
            <button
              key={c}
              type="button"
              className="chip"
              onClick={() => updateCategory(activeCategory === c ? null : c)}
              aria-pressed={activeCategory === c}
            >
              {c}
            </button>
          ))}
        </div>
      )}

      {/* Cookbooks filter options */}
      {cookbooks.length > 0 && cookbooksExpanded && (
        <div className="chip-row mb-3">
          {cookbooks.map((cb) => (
            <button
              key={cb.id}
              type="button"
              className="chip"
              onClick={() => updateCookbook(activeCookbook === cb.id ? null : cb.id)}
              aria-pressed={activeCookbook === cb.id}
            >
              {cb.name}
            </button>
          ))}
        </div>
      )}

      {/* Active filters */}
      {(activeCategory || activeTag || activeCookbook) && (
        <div className="chip-row mb-4">
          {activeCategory && (
            <Tag variant="accent">
              {t('search.categoryFilter', { x: activeCategory })}
              <button type="button" className="tag-remove" onClick={() => updateCategory(null)}>
                ×
              </button>
            </Tag>
          )}
          {activeTag && (
            <Tag variant="accent">
              {t('search.tagFilter', { x: activeTag })}
              <button type="button" className="tag-remove" onClick={() => updateTag(null)}>
                ×
              </button>
            </Tag>
          )}
          {activeCookbook && (
            <Tag variant="accent">
              {t('search.cookbookFilter', { x: cookbooks.find((c) => c.id === activeCookbook)?.name || activeCookbook })}
              <button type="button" className="tag-remove" onClick={() => updateCookbook(null)}>
                ×
              </button>
            </Tag>
          )}
        </div>
      )}

      {/* Results */}
      <div className="mt-3">
        {loading && <p className="text-muted">{t('common.loading')}</p>}
        {!loading && results.length === 0 && query && (
          <p className="text-muted">{t('search.noMatch', { query })}</p>
        )}
        {!loading && results.length === 0 && !query && !activeCategory && !activeTag && !activeCookbook && (
          <p className="text-muted">{t('search.noQuery')}</p>
        )}
        {results.map((r, index) => (
          <div key={r.id}>
            {index > 0 && <hr className="entry-divider" />}
            <RecipeEntry recipe={r} cookbook={getCookbookForRecipe(r, cookbooks)} />
          </div>
        ))}
      </div>
    </main>
  )
}
