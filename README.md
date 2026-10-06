# Our Open Cookbook

A cross-platform recipe app built with React, TypeScript, and Capacitor.
Runs on Android, iOS, and the web as an installable PWA.

Gather your recipes in one warm, offline-first place. Scan recipes from
photos with OCR, organize them into cookbooks, sync across devices via
Google Drive, and export to Markdown or JSON.

## Features

- **Recipe management** — create, edit, list, favorite, search recipes;
  multiple images per recipe (resized to 500x500 data URIs), share via the
  native share sheet with clipboard fallback, duplicate a recipe into the
  create form
- **Cookbooks** — organize recipes into collections; auto-creates a
  default cookbook ("Mina recept") on first save; book-shaped thumbnails
  built from up to four recipe images, owner/description display, and an
  A-Ö index in cookbook detail
- **OCR scanning** — take a photo or upload a JPG, PNG, or PDF (pages
  rendered with pdf.js), recognize text with tesseract.js in English or
  Swedish, parse into a structured recipe, edit and save into a cookbook
- **Search** — LIKE-based search over title, category, tags, and
  ingredients, with category and cookbook filters
- **Home** — Latest / Favorites / Categories tabs with thumbnail-rich
  recipe entries (cookbook, category, ingredient count, time, servings)
- **Google Drive sync** — sign in with Google, sync recipes to the
  app-owned private folder (`appDataFolder`) in the user's Drive, conflict
  resolution with version vectors
- **Export/import** — Markdown (human-readable) and JSON (lossless,
  round-trips all fields)
- **Offline-first** — works fully offline; sync when you choose to
- **PWA** — installable on mobile/desktop, service worker for offline
- **Organic design system** — warm cream-and-terracotta theme with
  Trocchi + Figtree typography, over-rounded shapes, washed photography,
  light and dark themes

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
│   ├── components/        Organic UI (Button, Card, TopBar, Tag, BottomNav,
│   │                      WashedImage, BookThumbnail, ImageSlideshow)
│   ├── pages/             Screens (Home, RecipeDetail, RecipeEdit, Cookbooks,
│   │                      CookbookDetail, Search, Scan, Settings, SyncStatus, Onboarding)
│   ├── features/
│   │   ├── recipe/        Recipe repository (CRUD, search, filters)
│   │   ├── cookbook/      Cookbook repository (create, rename, add/remove recipes)
│   │   └── sync/          PendingSync queue, conflict resolver, sync engine,
│   │                      Drive sync service (+ tests)
│   ├── lib/               db.ts (RxDB), device.ts, seed.ts, store.ts,
│   │                      driveClient.ts, googleConfig.ts, ocrParser.ts,
│   │                      ocrService.ts, exportImport.ts, imageUtils.ts
│   ├── stores/            Zustand stores (appStore)
│   ├── types/             Domain models (Recipe, Cookbook, Ingredient, Device,
│   │                      VersionVector, Sync) (+ tests)
│   ├── theme/             tokens.css, fonts.css, global.css (Organic design)
│   ├── hooks/             useRecipes, useCookbooks
│   └── test/              Vitest setup
├── public/                favicon, PWA icons
├── .env.example           Template for VITE_GOOGLE_CLIENT_ID
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

### Build and verify

```bash
npm run build        # tsc + vite build (also emits the PWA service worker)
npm run lint         # eslint
npm run test         # vitest (26 tests: domain models, OCR parser, conflict resolver)
npx cap sync         # sync web assets to native projects (no-op until platforms are added)
```

### Android debug APK

Native platform projects are not committed to the repo — add them once:

```bash
cd webapp
npm run build
npx cap add android   # first time only; creates webapp/android
npx cap sync android
cd android
JAVA_HOME=/path/to/jdk21 ANDROID_HOME=~/android-sdk ./gradlew assembleDebug
# APK at android/app/build/outputs/apk/debug/app-debug.apk
```

(iOS is the same with `npx cap add ios`; the WebView origin is forced to
`https://localhost` on both platforms so a single Google-authorized origin
works everywhere.)

### Google Drive sync setup

The user never sees or touches OAuth config. As the developer, set your
Google Cloud client ID once:

1. Create a project at [console.cloud.google.com](https://console.cloud.google.com/)
2. Enable the Google Drive API
3. Create an OAuth client ID (Web application type)
4. Add authorized origins:
   - `http://localhost:5173` (dev)
   - `https://localhost` (Android + iOS WebView)
5. Set the client ID in `webapp/.env` (template in `.env.example`):
   ```bash
   cp .env.example .env
   # then set: VITE_GOOGLE_CLIENT_ID=your-id.apps.googleusercontent.com
   ```

Users then just click "Logga in med Google" on the sync page and approve.
The app requests only the `drive.file` scope and stores synced files in
the app-owned private `appDataFolder` in the user's Drive — it never sees
the user's other Drive files.

## Design system

The visual identity comes from the "Organic" design material in `_sources/`.
It is a warm, rounded, left-aligned system with:

- Colors: cream background (#f5ead8), terracotta accent (#c67139),
  sage second accent (#7a8a5e), with 100-900 tonal ramps
- Typography: Trocchi (display) + Figtree (body)
- Over-rounded shapes, pill buttons (999px radius), washed photography
- Light and dark themes

See `AGENTS.md` for the full design system guidelines.

## Repository notes

- `feedback/` — user feedback round-trips that drive the UI/UX iteration.
- `_sources/` — the original Organic design PDF (committed before the
  directory was added to `.gitignore`, which now guards against further
  large binary commits).
- The legacy Kotlin/Compose Android app was removed on this branch. Its
  source remains available in git history (`git show master:app/src/...`),
  and the Drive sync file format is kept field-compatible with it.

## License

Proprietary. All rights reserved.
