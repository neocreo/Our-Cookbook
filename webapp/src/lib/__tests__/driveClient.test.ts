import { describe, it, expect, vi, afterEach } from 'vitest'
import { DriveApiClient } from '../../lib/driveClient'

function jsonResponse(body: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response
}

describe('DriveApiClient.ensureRootFolder', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('finds the root folder by name — /files/root is never called (drive.file scope)', async () => {
    const urls: string[] = []
    vi.stubGlobal('fetch', vi.fn(async (input: RequestInfo | URL) => {
      const url = String(input)
      urls.push(url)
      if (url.includes('/files?q=')) {
        expect(url).toContain('OurOpenCookbook')
        return jsonResponse({ files: [{ id: 'root-folder-id', name: 'OurOpenCookbook' }] })
      }
      throw new Error(`unexpected url: ${url}`)
    }))
    const client = new DriveApiClient('token')
    await expect(client.ensureRootFolder()).resolves.toBe('root-folder-id')
    expect(urls).toHaveLength(1)
    expect(urls[0]).not.toContain('/files/root')
  })

  it('creates the folder at the top of My Drive when not found', async () => {
    vi.stubGlobal('fetch', vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input)
      if (url.includes('/files?q=') && !init?.method) {
        return jsonResponse({ files: [] })
      }
      if (url.endsWith('/files') && init?.method === 'POST') {
        const body = JSON.parse(String(init.body))
        expect(body).toEqual({
          name: 'OurOpenCookbook',
          mimeType: 'application/vnd.google-apps.folder',
        })
        return jsonResponse({ id: 'new-root-id' })
      }
      throw new Error(`unexpected url: ${url}`)
    }))
    const client = new DriveApiClient('token')
    await expect(client.ensureRootFolder()).resolves.toBe('new-root-id')
  })
})
