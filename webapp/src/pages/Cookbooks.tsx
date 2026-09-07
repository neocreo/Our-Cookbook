// Cookbooks — list of cookbooks with recipe counts. FAB to create a new one.

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { Card } from '../components/Card'
import { Button } from '../components/Button'
import { useCookbooks } from '../hooks/useCookbooks'
import { createCookbookNamed } from '../features/cookbook/repository'
import { getDeviceId } from '../lib/device'

export function Cookbooks() {
  const { cookbooks, loading } = useCookbooks()
  const [creating, setCreating] = useState(false)
  const [name, setName] = useState('')

  async function onCreate() {
    if (!name.trim()) return
    await createCookbookNamed(name.trim(), getDeviceId())
    setName('')
    setCreating(false)
  }

  return (
    <main className="page">
      <h1 className="page-title">Cookbooks</h1>

      {loading && <p className="text-muted">Loading…</p>}

      {!loading && cookbooks.length === 0 && !creating && (
        <p className="text-muted">No cookbooks yet. Create one to organize your recipes.</p>
      )}

      <div className="stack">
        {cookbooks.map((cb) => (
          <Link key={cb.id} to={`/cookbooks/${cb.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
            <Card as="article" elevation="sm" kicker="Cookbook" title={cb.name}>
              {cb.description ?? `${cb.recipeIds.length} recipe${cb.recipeIds.length === 1 ? '' : 's'}`}
            </Card>
          </Link>
        ))}
      </div>

      {creating && (
        <div className="card elev-md" style={{ marginTop: 8 }}>
          <div className="form-row">
            <label htmlFor="cb-name">Cookbook name</label>
            <input
              id="cb-name"
              className="input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              autoFocus
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
