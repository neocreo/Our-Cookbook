// Cookbooks — list of cookbooks with recipe counts and thumbnails.
// FAB to create a new one.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { Button } from '../components/Button'
import { BookThumbnail } from '../components/BookThumbnail'
import { useCookbooks } from '../hooks/useCookbooks'
import { useRecipes } from '../hooks/useRecipes'
import { createCookbookNamed } from '../features/cookbook/repository'
import { getDeviceId } from '../lib/device'
import { useAppStore } from '../stores/appStore'
import { ownerDisplayName } from '../types/cookbook'
import type { Cookbook } from '../types/cookbook'

function CookbookEntry({ cookbook, allRecipes, owner }: { cookbook: Cookbook; allRecipes: Array<{ id: string; imageUrls: string[] }>; owner: string }) {
  const [expanded, setExpanded] = useState(false)

  // Get recipe images for this cookbook
  const recipeImages = allRecipes
    .filter((r) => cookbook.recipeIds.includes(r.id))
    .flatMap((r) => r.imageUrls ?? [])
    .slice(0, 4)

  return (
    <Link to={`/cookbooks/${cookbook.id}`} className="link-plain">
      <div className="entry">
        {/* Book-shaped thumbnail */}
        <div className="entry-thumb">
          <BookThumbnail recipeImageUrls={recipeImages} size={100} />
        </div>

        {/* Text content */}
        <div className="entry-body">
          <h3 className="entry-title">{cookbook.name}</h3>
          <div className="entry-meta">
            <span>Owner: {owner}</span>
          </div>
          {cookbook.description && (
            <div>
              <p className={expanded ? 'cookbook-desc' : 'cookbook-desc clamp-1'}>
                {cookbook.description}
              </p>
              <button
                type="button"
                className="btn-more"
                onClick={(e) => {
                  e.preventDefault()
                  e.stopPropagation()
                  setExpanded(!expanded)
                }}
              >
                {expanded ? 'less' : '...more'}
              </button>
            </div>
          )}
          <div className="cookbook-count">
            {cookbook.recipeIds.length} recipe{cookbook.recipeIds.length === 1 ? '' : 's'}
          </div>
        </div>
      </div>
    </Link>
  )
}

export function Cookbooks() {
  const { cookbooks, loading } = useCookbooks()
  const { recipes } = useRecipes()
  const userName = useAppStore((s) => s.userName)
  const [creating, setCreating] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  // Prepare recipe data for thumbnails
  const allRecipes = recipes.map((r) => ({
    id: r.id,
    imageUrls: r.imageUrls,
  }))

  async function onCreate() {
    if (!name.trim()) return
    await createCookbookNamed(name.trim(), getDeviceId(), description.trim() || null, userName)
    setName('')
    setDescription('')
    setCreating(false)
  }

  return (
    <main className="page">
      <h1 className="page-title">Cookbooks</h1>

      {loading && <p className="text-muted">Loading…</p>}

      {!loading && cookbooks.length === 0 && !creating && (
        <p className="text-muted">No cookbooks yet. Create one to organize your recipes.</p>
      )}

      <div className="mt-4">
        {cookbooks.map((cb) => (
          <CookbookEntry
            key={cb.id}
            cookbook={cb}
            allRecipes={allRecipes}
            owner={ownerDisplayName(cb, getDeviceId(), userName)}
          />
        ))}
      </div>

      {creating && (
        <div className="card elev-md mt-4">
          <h3 className="mb-4">Create Cookbook</h3>
          <div className="form-row">
            <label htmlFor="cb-name">Name</label>
            <input
              id="cb-name"
              className="input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              autoFocus
            />
          </div>
          <div className="form-row">
            <label htmlFor="cb-desc">Description (optional)</label>
            <textarea
              id="cb-desc"
              className="input"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
            />
          </div>
          <div className="form-actions">
            <Button variant="secondary" onClick={() => setCreating(false)}>
              Cancel
            </Button>
            <Button variant="primary" onClick={onCreate} disabled={!name.trim()}>
              Create
            </Button>
          </div>
        </div>
      )}

      <button
        type="button"
        className="fab"
        aria-label="New cookbook"
        onClick={() => setCreating((v) => !v)}
      >
        <Plus strokeWidth={2.75} />
      </button>
    </main>
  )
}
