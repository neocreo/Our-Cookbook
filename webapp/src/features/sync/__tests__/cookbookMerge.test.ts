// Tests for the cookbook merge rules — last write wins, local deletion wins,
// remote-only cookbooks are pulled.

import { describe, it, expect } from 'vitest'
import { mergeCookbooks } from '../cookbookMerge'
import { createCookbook } from '../../../types/cookbook'

function cookbook(overrides: Partial<ReturnType<typeof createCookbook>>) {
  return { ...createCookbook('Book', 'device-a', null, []), ...overrides }
}

describe('mergeCookbooks', () => {
  it('pushes a local-only cookbook to the merged list', () => {
    const local = [cookbook({ id: 'a', name: 'Local only' })]
    const result = mergeCookbooks(local, [], [])
    expect(result.merged.map((cb) => cb.id)).toEqual(['a'])
    expect(result.remoteChanged).toBe(true)
    expect(result.toSaveLocally).toEqual([])
  })

  it('pulls a remote-only cookbook to the device', () => {
    const remote = [cookbook({ id: 'b', name: 'Remote only' })]
    const result = mergeCookbooks([], remote, [])
    expect(result.toSaveLocally.map((cb) => cb.id)).toEqual(['b'])
    expect(result.remoteChanged).toBe(false)
    expect(result.merged.map((cb) => cb.id)).toEqual(['b'])
  })

  it('keeps the remote version when remote is newer', () => {
    const local = [cookbook({ id: 'a', updatedAt: '2026-01-01T00:00:00Z' })]
    const remote = [cookbook({ id: 'a', name: 'Renamed elsewhere', updatedAt: '2026-02-01T00:00:00Z' })]
    const result = mergeCookbooks(local, remote, [])
    expect(result.toSaveLocally[0]?.name).toBe('Renamed elsewhere')
    expect(result.merged[0]?.name).toBe('Renamed elsewhere')
    expect(result.remoteChanged).toBe(false)
  })

  it('keeps the local version when local is newer', () => {
    const local = [cookbook({ id: 'a', name: 'Renamed here', updatedAt: '2026-02-01T00:00:00Z' })]
    const remote = [cookbook({ id: 'a', updatedAt: '2026-01-01T00:00:00Z' })]
    const result = mergeCookbooks(local, remote, [])
    expect(result.merged[0]?.name).toBe('Renamed here')
    expect(result.remoteChanged).toBe(true)
    expect(result.toSaveLocally).toEqual([])
  })

  it('does nothing when both sides are identical', () => {
    const same = [cookbook({ id: 'a', updatedAt: '2026-01-01T00:00:00Z' })]
    const result = mergeCookbooks(same, same, [])
    expect(result.remoteChanged).toBe(false)
    expect(result.toSaveLocally).toEqual([])
  })

  it('lets a local deletion win over a remote cookbook', () => {
    const local = [cookbook({ id: 'a' })]
    const remote = [cookbook({ id: 'a', name: 'Still on Drive' }), cookbook({ id: 'b' })]
    const result = mergeCookbooks(local, remote, ['a'])
    expect(result.merged.map((cb) => cb.id)).toEqual(['b'])
    expect(result.remoteChanged).toBe(true)
    expect(result.toSaveLocally.map((cb) => cb.id)).toEqual(['b'])
  })
})
