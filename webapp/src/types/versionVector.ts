// VersionVector — tracks changes for conflict detection. Ported from
// VersionVector.kt. Timestamps are ISO strings (Instant serialized).

export interface VersionVector {
  deviceId: string
  counter: number
  timestamp: string
}

export function createVersionVector(deviceId = ''): VersionVector {
  return { deviceId, counter: 0, timestamp: new Date().toISOString() }
}

export function incrementVersionVector(v: VersionVector, deviceId: string): VersionVector {
  return { deviceId, counter: v.counter + 1, timestamp: new Date().toISOString() }
}

export function isVersionNewer(a: VersionVector, b: VersionVector): boolean {
  if (a.counter > b.counter) return true
  if (a.counter < b.counter) return false
  return a.timestamp > b.timestamp
}
