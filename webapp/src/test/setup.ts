import '@testing-library/jest-dom/vitest'
import { indexedDB, IDBKeyRange } from 'fake-indexeddb'

// RxDB's Dexie storage needs IndexedDB; jsdom doesn't provide one.
Object.defineProperty(globalThis, 'indexedDB', { value: indexedDB, writable: true })
Object.defineProperty(globalThis, 'IDBKeyRange', { value: IDBKeyRange, writable: true })
