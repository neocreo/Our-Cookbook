// Device — a user device. Ported from Device.kt. The device id is generated
// silently for sync metadata; the user never sees it (see lib/device.ts).

export type DeviceCapability =
  | 'CAMERA'
  | 'INTERNET'
  | 'BLUETOOTH'
  | 'LARGE_SCREEN'
  | 'TOUCHSCREEN'
  | 'KEYBOARD'

export interface Device {
  id: string
  name: string
  deviceId: string
  capabilities: DeviceCapability[]
  createdAt: string // ISO
  lastSeenAt: string // ISO
}

export function createDevice(name: string, deviceId: string, capabilities: DeviceCapability[] = []): Device {
  const now = new Date().toISOString()
  return { id: crypto.randomUUID(), name, deviceId, capabilities, createdAt: now, lastSeenAt: now }
}
