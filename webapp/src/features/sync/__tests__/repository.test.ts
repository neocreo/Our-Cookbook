// Tests for the PendingSync queue's localStorage persistence. The module is
// a singleton that loads from storage on import, so each test re-imports a
// fresh instance (vi.resetModules) to simulate an app reload.

import { describe, it, expect, beforeEach, vi } from 'vitest'

describe('PendingSync queue persistence', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.resetModules()
  })

  it('survives an app reload', async () => {
    const first = await import('../repository')
    first.enqueueSync('CREATE', 'RECIPE', 'recipe-1', '{"id":"recipe-1"}', 'device-1')
    first.enqueueSync('DELETE', 'COOKBOOK', 'cb-1', '', 'device-1')
    expect(first.getPendingCount()).toBe(2)

    // Fresh module — as after a page reload.
    const reloaded = await import('../repository')
    expect(reloaded.getPendingCount()).toBe(2)
    const queue = reloaded.getPendingSyncQueue()
    expect(queue[0].entityId).toBe('recipe-1')
    expect(queue[0].entityType).toBe('RECIPE')
    expect(queue[1].entityType).toBe('COOKBOOK')
    expect(queue[1].operation).toBe('DELETE')
  })

  it('markSynced removes the entry and the removal persists', async () => {
    const first = await import('../repository')
    const entry = first.enqueueSync('UPDATE', 'RECIPE', 'recipe-1', '{}', 'device-1')
    first.markSynced(entry.id)

    const reloaded = await import('../repository')
    expect(reloaded.getPendingCount()).toBe(0)
  })

  it('markSyncFailed records the error and persists it', async () => {
    const first = await import('../repository')
    const entry = first.enqueueSync('CREATE', 'RECIPE', 'recipe-1', '{}', 'device-1')
    first.markSyncFailed(entry.id, 'network down')

    const reloaded = await import('../repository')
    const queued = reloaded.getPendingSyncQueue()
    expect(queued[0].retryCount).toBe(1)
    expect(queued[0].lastError).toBe('network down')
    expect(reloaded.getRetryable()).toHaveLength(1)
  })

  it('starts empty when storage is corrupted', async () => {
    localStorage.setItem('oc.pendingSync', '{not json')
    const mod = await import('../repository')
    expect(mod.getPendingCount()).toBe(0)
  })
})
