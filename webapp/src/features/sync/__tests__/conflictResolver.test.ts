import { describe, it, expect } from 'vitest'
import { detectConflicts, resolveConflict, type ConflictInfo } from '../conflictResolver'
import { createRecipe } from '../../../types/recipe'

function makeRecipe(id: string, counter = 0, updatedAt = '2026-01-01T00:00:00.000Z') {
  const r = createRecipe({ title: `Recipe ${id}`, category: 'Dinner' }, 'dev1')
  r.id = id
  r.versionVector.counter = counter
  r.updatedAt = updatedAt
  return r
}

describe('detectConflicts', () => {
  it('detects no conflicts when local and remote match', () => {
    const local = [makeRecipe('r1')]
    const remote = new Map([['r1', makeRecipe('r1')]])
    expect(detectConflicts(local, remote)).toHaveLength(0)
  })

  it('detects DELETED_REMOTE when local exists but remote does not', () => {
    const local = [makeRecipe('r1')]
    const remote = new Map()
    const conflicts = detectConflicts(local, remote)
    expect(conflicts).toHaveLength(1)
    expect(conflicts[0].conflictType).toBe('DELETED_REMOTE')
  })

  it('detects DELETED_LOCAL when remote exists but local does not', () => {
    const local: ReturnType<typeof makeRecipe>[] = []
    const remote = new Map([['r1', makeRecipe('r1')]])
    const conflicts = detectConflicts(local, remote)
    expect(conflicts).toHaveLength(1)
    expect(conflicts[0].conflictType).toBe('DELETED_LOCAL')
  })

  it('detects VERSION_MISMATCH when counters differ', () => {
    const local = [makeRecipe('r1', 2)]
    const remote = new Map([['r1', makeRecipe('r1', 1)]])
    const conflicts = detectConflicts(local, remote)
    expect(conflicts).toHaveLength(1)
    expect(conflicts[0].conflictType).toBe('VERSION_MISMATCH')
  })
})

describe('resolveConflict', () => {
  it('KEEP_LOCAL returns the local recipe', () => {
    const local = makeRecipe('r1')
    const conflict: ConflictInfo = {
      recipeId: 'r1',
      localRecipe: local,
      remoteRecipe: makeRecipe('r1', 1),
      conflictType: 'VERSION_MISMATCH',
      localVersion: 0,
      remoteVersion: 1,
      localChecksum: null,
      remoteChecksum: null,
    }
    const result = resolveConflict(conflict, 'KEEP_LOCAL')
    expect(result.recipe).toBe(local)
  })

  it('KEEP_REMOTE returns the remote recipe', () => {
    const remote = makeRecipe('r1', 1)
    const conflict: ConflictInfo = {
      recipeId: 'r1',
      localRecipe: makeRecipe('r1'),
      remoteRecipe: remote,
      conflictType: 'VERSION_MISMATCH',
      localVersion: 0,
      remoteVersion: 1,
      localChecksum: null,
      remoteChecksum: null,
    }
    const result = resolveConflict(conflict, 'KEEP_REMOTE')
    expect(result.recipe).toBe(remote)
  })

  it('MERGE combines tags from both', () => {
    const local = makeRecipe('r1')
    local.tags = ['dinner', 'easy']
    const remote = makeRecipe('r1', 1)
    remote.tags = ['dinner', 'beef']
    const conflict: ConflictInfo = {
      recipeId: 'r1',
      localRecipe: local,
      remoteRecipe: remote,
      conflictType: 'BOTH_MODIFIED',
      localVersion: 0,
      remoteVersion: 1,
      localChecksum: null,
      remoteChecksum: null,
    }
    const result = resolveConflict(conflict, 'MERGE')
    expect(result.recipe.tags).toEqual(expect.arrayContaining(['dinner', 'easy', 'beef']))
    expect(result.recipe.tags).toHaveLength(3)
  })

  it('KEEP_BOTH creates a copy with a new ID', () => {
    const remote = makeRecipe('r1', 1)
    remote.title = 'Original'
    const conflict: ConflictInfo = {
      recipeId: 'r1',
      localRecipe: makeRecipe('r1'),
      remoteRecipe: remote,
      conflictType: 'BOTH_MODIFIED',
      localVersion: 0,
      remoteVersion: 1,
      localChecksum: null,
      remoteChecksum: null,
    }
    const result = resolveConflict(conflict, 'KEEP_BOTH')
    expect(result.recipe.id).not.toBe('r1')
    expect(result.recipe.title).toContain('copy')
  })
})
