// Silent device id. Generated as a UUID and persisted in localStorage; used
// only for sync metadata. The user never sees or interacts with it.

const DEVICE_ID_KEY = 'oc.device.id'

export function getDeviceId(): string {
  let id = localStorage.getItem(DEVICE_ID_KEY)
  if (!id) {
    id = crypto.randomUUID()
    localStorage.setItem(DEVICE_ID_KEY, id)
  }
  return id
}
