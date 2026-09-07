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

// SyncVersionVector — tracks version vectors across multiple devices for
// distributed conflict detection. Ported from SyncVersionVector.kt.

export interface SyncVersionVector {
  versions: Map<string, VersionVector> // deviceId -> VersionVector
}

export function createSyncVersionVector(): SyncVersionVector {
  return { versions: new Map() }
}

export function mergeSyncVersionVectors(
  a: SyncVersionVector,
  b: SyncVersionVector,
): SyncVersionVector {
  const merged = new Map<string, VersionVector>()
  const keys = new Set([...a.versions.keys(), ...b.versions.keys()])
  for (const deviceId of keys) {
    const local = a.versions.get(deviceId)
    const remote = b.versions.get(deviceId)
    if (!local) merged.set(deviceId, remote!)
    else if (!remote) merged.set(deviceId, local)
    else merged.set(deviceId, isVersionNewer(local, remote) ? local : remote)
  }
  return { versions: merged }
}

export function getSyncVersionForDevice(
  v: SyncVersionVector,
  deviceId: string,
): VersionVector | undefined {
  return v.versions.get(deviceId)
}

export function withIncrementedSyncVersion(
  v: SyncVersionVector,
  deviceId: string,
): SyncVersionVector {
  const current = v.versions.get(deviceId) ?? createVersionVector(deviceId)
  return { versions: new Map(v.versions).set(deviceId, incrementVersionVector(current, deviceId)) }
}
