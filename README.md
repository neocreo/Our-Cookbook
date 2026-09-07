# Our Cookbook

A cross-platform recipe app built with React, TypeScript, and Capacitor.
Runs on Android, iOS, and the web as an installable PWA.

Gather your recipes in one warm, offline-first place. Scan recipes from
photos with OCR, organize them into cookbooks, sync across devices via
Google Drive, and export to Markdown or JSON.

## Features

- **Recipe management** — create, edit, list, favorite, search recipes
- **Cookbooks** — organize recipes into collections; auto-creates a
  default cookbook on first save
- **OCR scanning** — take a photo or upload an image, extract text with
  tesseract.js, parse into a structured recipe, edit and save
- **Search** — LIKE-based search over title, category, tags, and
  ingredients, with category and tag filters
- **Google Drive sync** — sign in with Google, sync recipes to a private
  Drive folder, conflict resolution with version vectors
- **Export/import** — Markdown (human-readable) and JSON (lossless,
  round-trips all fields)
- **Offline-first** — works fully offline; sync when you choose to
- **PWA** — installable on mobile/desktop, service worker for offline
- **Organic design system** — warm cream-and-terracotta theme with
  Caprasimo + Figtree typography, over-rounded shapes, washed photography

## Tech stack

| Layer | Choice |
|---|---|
| Language | TypeScript 5.9 |
| Build | Vite 8 |
| UI | React 19 |
| Design | Organic design system (ported from `_sources/`) |
| State | Zustand |
| Routing | React Router v7 |
| App shell | Capacitor 7 |
| Persistence | RxDB 17 with Dexie (IndexedDB), AJV-validated |
| Sync | Google Drive API v3 via Google Identity Services |
| OCR | tesseract.js (web + WebView); ML Kit on mobile when Capacitor 8 |
| Camera | @capacitor/camera |
| Icons | Lucide |
| Test | Vitest + Testing Library |
| Lint | ESLint 10 + typescript-eslint |
| PWA | vite-plugin-pwa (Workbox service worker) |

## Project structure

```
webapp/
├── src/
│   ├── components/        Organic UI (Button, Card, TopBar, Tag, BottomNav, WashedImage)
│   ├── pages/             Screens (Home, RecipeDetail, RecipeEdit, Cookbooks,
│   │                      CookbookDetail, Search, Scan, Settings, SyncStatus, Onboarding)
│   ├── features/
│   │   ├── recipe/        Recipe repository (CRUD, search, filters)
│   │   ├── cookbook/      Cookbook repository (create, rename, add/remove recipes)
│   │   └── sync/          PendingSync queue, conflict resolver, sync engine,
│   │                      Drive sync service
│   ├── lib/               db.ts (RxDB), device.ts, seed.ts, store.ts,
│   │                      driveClient.ts, googleConfig.ts, ocrParser.ts,
│   │                      ocrService.ts, exportImport.ts
│   ├── stores/            Zustand stores (appStore)
│   ├── types/             Domain models (Recipe, Cookbook, Ingredient, Device,
│   │                      VersionVector, Sync)
│   ├── theme/             tokens.css, fonts.css, global.css (Organic design)
│   ├── hooks/             useRecipes, useCookbooks
│   └── test/              Vitest setup
├── public/                favicon, app icons
├── capacitor.config.ts
├── vite.config.ts
├── vitest.config.ts
└── package.json
```

## Getting started

### Prerequisites

- Node.js 20+
- npm
- (For Android builds) Android SDK + JDK 21

### Install and run

```bash
cd webapp
npm install
npm run dev          # dev server at http://localhost:5173
```

### Build

```bash
npm run build        # tsc + vite build
npm run lint         # eslint
npm run test         # vitest
npx cap sync         # sync web assets to native projects
```

### Android debug APK

```bash
npm run build
npx cap sync android
cd android
JAVA_HOME=/path/to/jdk21 ANDROID_HOME=~/android-sdk ./gradlew assembleDebug
# APK at android/app/build/outputs/apk/debug/app-debug.apk
```

### Google Drive sync setup

The user never sees or touches OAuth config. As the developer, set your
Google Cloud client ID once:

1. Create a project at [console.cloud.google.com](https://console.cloud.google.com/)
2. Enable the Google Drive API
3. Create an OAuth client ID (Web application type)
4. Add authorized origins:
   - `http://localhost:5173` (dev)
   - `https://localhost` (Android + iOS WebView)
5. Set the client ID in `webapp/.env`:
   ```
   VITE_GOOGLE_CLIENT_ID=your-id.apps.googleusercontent.com
   ```

Users then just click "Logga in med Google" on the sync page and approve.

## Design system

The visual identity comes from the "Organic" design material in `_sources/`.
It is a warm, rounded, left-aligned system with:

- Colors: cream background (#f5ead8), terracotta accent (#c67139),
  sage second accent (#7a8a5e), with 100-900 tonal ramps
- Typography: Caprasimo (display) + Figtree (body)
- Over-rounded shapes, pill buttons (999px radius), washed photography
- Light and dark themes

See `AGENTS.md` for the full design system guidelines.

## License

Proprietary. All rights reserved.
