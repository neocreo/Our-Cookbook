// Search — LIKE-based search over title, category, tags, and ingredients.
// Matches the Kotlin app's search approach (no FTS). Also supports filtering
// by category and tag.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Search as SearchIcon } from 'lucide-react'
import { Card } from '../components/Card'
import { Tag } from '../components/Tag'
import { useRecipes } from '../hooks/useRecipes'
import { searchRecipes, allCategories, allTags } from '../features/recipe/repository'
import { totalRecipeTime } from '../types/recipe'

export function Search() {
  const { recipes, loading } = useRecipes()
  const [query, setQuery] = useState('')
  const [activeCategory, setActiveCategory] = useState<string | null>(null)
  const [activeTag, setActiveTag] = useState<string | null>(null)

  const categories = allCategories(recipes)
  const tags = allTags(recipes)

  let results = searchRecipes(recipes, query)
  if (activeCategory) results = results.filter((r) => r.category === activeCategory)
  if (activeTag) results = results.filter((r) => r.tags.includes(activeTag))

  return (
    <main className="page">
      <h1 className="page-title">Search</h1>

      <div className="field" style={{ position: 'relative' }}>
        <input
          className="input"
          placeholder="Search recipes, ingredients, tags…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={{ paddingLeft: 36 }}
        />
        <SearchIcon
          size={16}
          strokeWidth={2.75}
          style={{ position: 'absolute', left: 12, top: 10, opacity: 0.5 }}
        />
      </div>

      {categories.length > 0 && (
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 }}>
          {categories.map((c) => (
            <button
              key={c}
              type="button"
              onClick={() => setActiveCategory((v) => (v === c ? null : c))}
              style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
            >
              <Tag variant={activeCategory === c ? 'accent' : 'neutral'}>{c}</Tag>
            </button>
          ))}
        </div>
      )}

      {tags.length > 0 && (
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 }}>
          {tags.map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setActiveTag((v) => (v === t ? null : t))}
              style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
            >
              <Tag variant={activeTag === t ? 'accent-2' : 'neutral'}>{t}</Tag>
            </button>
          ))}
        </div>
      )}

      <div className="stack" style={{ marginTop: 12 }}>
        {loading && <p className="text-muted">Loading…</p>}
        {!loading && results.length === 0 && (
          <p className="text-muted">No recipes match "{query}".</p>
        )}
        {results.map((r) => {
          const time = totalRecipeTime(r)
          return (
            <Link key={r.id} to={`/recipes/${r.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <Card as="article" elevation="sm" kicker={r.category} title={r.title}>
                {time != null ? `${time} min` : ''}{r.servingSize ? ` · ${r.servingSize} servings` : ''}
              </Card>
            </Link>
          )
        })}
      </div>
    </main>
  )
}
