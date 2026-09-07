# AGENTS.md — Our Cookbook

This file instructs any AI agent working in the Our Cookbook repository.

## Project context

Our Cookbook has **two surfaces** in this single repo:

- **`webapp/`** — the active codebase. A cross-platform app (Android + iOS +
  web/PWA) built with React + TypeScript + Capacitor. All new feature work
  happens here.
- **`app/`** — the legacy Android-only Kotlin/Compose app, kept on `master`
  as a frozen reference. **Do not modify it.** Port behaviour and field names
  from it when reimplementing in `webapp/`; the Drive sync file format must
  stay backwards-compatible with it.

### Active stack — `webapp/`

- **Language**: TypeScript 5.9.x.
- **Build**: Vite 8 (`npm run dev` / `npm run build`). `tsc -b` runs before
  `vite build`, so type errors fail the build.
- **UI**: React 19. **Components come from the "Organic" design system** (see
  below) — not Material, not a component library. Use the token CSS variables
  and the ported class names; never hard-code a hex, font, or px value the
  tokens already carry.
- **State**: Zustand (`stores/`), persisted to `localStorage` for onboarding
  and theme. No Redux, no context providers for global state.
- **Routing**: React Router v7 (`react-router-dom`), component API
  (`<BrowserRouter>`, `<Routes>`, `<NavLink>`). Works in the Capacitor WebView.
- **App shell**: Capacitor 7 (`@capacitor/core|cli|android|ios`).
  `capacitor.config.ts` → `appId: com.ourcookbook`, `appName: Our Cookbook`,
  `webDir: dist`. Native platforms are added later with `npx cap add`.
- **Persistence**: RxDB 17 with the Dexie storage (`rxdb/plugins/storage-dexie`,
  IndexedDB) on web. Native will swap to `rxdb/plugins/storage-sqlite` over
  `@capacitor-community/sqlite` (SQLCipher) — wire it behind the abstraction in
  `src/lib/db.ts`. Field names in the RxDB schemas mirror the Kotlin domain
  models so Drive-synced files stay readable by the old app.
- **Icons**: Lucide (`lucide-react`), `strokeWidth={2.75}`.
- **Lint/format**: ESLint 10 (flat config, `eslint.config.js`) +
  `typescript-eslint`. `npm run lint` must be green.
- **Test** (later phases): Vitest + Testing Library.
- **OCR/Camera** (Phase 3): `@capacitor/camera` + `@capacitor-mlkit/text-recognition`
  on mobile; on web, file upload (`<input type="file" accept="image/*">`).
- **Sync** (Phase 2): Google Drive API v3, offline-first, PendingSync queue,
  version vectors (ported from the Kotlin data layer).

### Source layout (`webapp/src/`)

```
components/   Organic UI components (Button, Card, TopBar, WashedImage, Tag, BottomNav)
pages/        Screens (Onboarding, Home, RecipeDetail, RecipeEdit, Settings, Search, Scan)
features/     Bounded contexts: features/recipe/repository.ts is the data access point
lib/          db.ts (RxDB), device.ts (silent device id), seed.ts (sample data)
stores/       Zustand stores (appStore.ts)
types/        Domain models ported from app/.../domain/model (Recipe, Cookbook, …)
theme/        tokens.css + fonts.css + global.css — the Organic design system
hooks/        useRecipes.ts (live subscription to the repository)
```

Layering mirrors the old app's clean architecture: `pages` → `hooks` →
`features/*/repository` → `lib/db`. A component must not touch RxDB or Drive
directly; a page must not import RxDB.

## Onboarding (binding)

The old "register your device" gate is **gone**. The first-run flow is exactly:

- Show the Onboarding screen: "Logga in med Google Drive" vs "Fortsätt offline".
- A device id is generated **silently** (`lib/device.ts`, UUID in
  `localStorage`) for sync metadata — the user never sees or enters it.
- Offline is a first-class choice, not a downgrade. Drive can be enabled
  later from Settings.
- **Auto-cookbook**: if `cookbooks` is empty when the user saves their first
  recipe, the repository creates "Mina recept" automatically and adds the
  recipe to it. The user can rename it later.
- App name in the UI is **"Our Cookbook"** (the design source's "The Kitchen
  Table" is not used).

## Design system — "Organic" (binding)

The look is ported from the design material in `_sources/` (Organic). It is the
app's visual identity — port it, don't reinvent it.

- `src/theme/tokens.css` is the source of truth: `:root` tokens (color, type,
  spacing, radius, shadow) plus a `[data-theme="dark"]` override. Theme is
  applied by setting `document.documentElement.dataset.theme`.
- `src/theme/global.css` holds the ported component classes (`.btn`, `.card`,
  `.tag`, `.nav`, `.input`, `.dialog`, `.washed`, …). React components apply
  these class names; do not write parallel styling.
- Fonts: Caprasimo (display) + Figtree (body), via Google Fonts.
- Direction: **left-aligned, asymmetric**, flush-left headings, whitespace on
  the right. **Over-round**: `--radius-lg` for containers, `999px` for
  buttons/inputs. Soft shapes, no sharp corners. `.washed` on every food photo.
- Warmth is the point: never desaturate the palette to grey; Caprasimo is the
  only display face.
- Interaction states come from the accent ramp; keyboard focus is the 2px
  accent `:focus-visible` ring — never the browser default.

## Software design principles (binding)

These come from `agency-agents/Rules/software-design-principles.md` and apply
to all code written here. Priority order when they conflict:

1. **KISS** — simpler, readable solution.
2. **YAGNI** — build for the current requirement; the exceptions are the sync
   schema and Drive file format, which are costly to change later.
3. **Separation of Concerns** — keep `pages` / `hooks` / `features` / `lib`
   apart.
4. **DRY** — but not at the cost of clarity; some duplication beats a forced
   abstraction.
5. **SOLID** — apply gradually.

- One bounded context per repository; a recipe repository does not reach into
  sync tables.
- Hooks depend on the repository, components depend on hooks; do not chain
  `component.hook.repo.db.rxdb…`.
- Add new export formats or sync strategies by implementing an interface, not
  by editing shared services.

### Other binding Rules

- `Rules/code-style.md` — StandardJS/ts-standard; TypeScript follows the same
  rules as JavaScript. Comments describe behaviour, not deliberation.
- `Rules/development-preferences.md` — npm is the package manager (this project
  uses npm); keep the same Node version across environments.
- `Rules/git-conventions.md` — branch names follow `<group>/<branch-name>`
  (`fix/`, `wip/`, `docs/`, `chore/`). Commit only when the user asks.
- `Rules/security.md` — never share env vars or secrets in output; do not
  commit anything unless the user explicitly says so.

## Selected agents

Activate the agent whose scope matches the task (sources in the `agency-agents`
repo at `../../agency-agents/`). Listed in priority order for this project.
Apply each agent's **React/Capacitor/TypeScript** guidance and ignore the
sections that don't apply to our stack (Android/Compose, iOS/SwiftUI, React
Native/Flutter, Laravel/Livewire, etc.).

### 1. Frontend Developer — primary engineering agent (active stack)
Source: `agency-agents/engineering/engineering-frontend-developer.md`

This is now the primary agent for feature work — the webapp is a React/TS SPA.
Its guidance maps directly onto our stack:

- Build screens with React + the Organic components; hold state in Zustand
  stores or local `useState`. Component libraries and design systems are its
  specialty.
- TypeScript throughout; proper tooling and type safety.
- Accessibility: WCAG 2.1 AA, semantic HTML, ARIA, keyboard navigation, screen
  reader support — a default requirement, not an afterthought.
- Performance: Core Web Vitals, code splitting and lazy loading (Phase 4),
  PWA/offline capabilities, bundle-size discipline.
- Mobile-first responsive design; cross-browser compatibility.
- Comprehensive unit/integration tests (Vitest + Testing Library, Phase 4).

### 2. Mobile App Builder — Capacitor native integration
Source: `agency-agents/engineering/engineering-mobile-app-builder.md`

Use for native platform integration via Capacitor: camera, biometrics, push
notifications, background work. **Ignore its "Current Android Toolchain"
section** (AGP/Kotlin/Compose BOM versions) — that is for the frozen Kotlin
`app/`, not the webapp. Apply only the cross-platform/Capacitor guidance:

- Offline-first architecture with intelligent sync (Drive sync, Phase 2).
- Camera and media processing (Phase 3 OCR).
- Performance: app startup < 3 s, memory footprint, responsive touch.
- Performance budgets: cold start < 3 s, lazy-load routes (Phase 4).

### 3. Backend Architect — data layer and sync design
Source: `agency-agents/engineering/engineering-backend-architect.md`

For repository/data-layer design, RxDB schema, Drive sync contract, and
conflict resolution. Its backend principles map onto the webapp data layer:
repositories are services, RxDB collections are the access layer, Drive is the
remote data source.

- Repositories own one bounded context each. Define repository interfaces and
  keep the storage adapter swappable in `lib/db.ts`.
- Drive file format and sync metadata are versioned and backwards-compatible;
  keep field names aligned with the Kotlin domain models.
- Security-first: encrypt data at rest (SQLCipher/Web Crypto), least privilege.
- Schema compliance and backwards compatibility validation.

### 4. UI Designer — the Organic design system
Source: `agency-agents/design/design-ui-designer.md`

For the Organic design system: tokens, components, states (loading, error,
empty), accessibility, dark mode. Its "design system first" approach is
exactly how `theme/` is structured.

- `theme/tokens.css` is the single token source; support light and dark.
- Component libraries with consistent visual language and interaction states.
- Reusable components live in `components/`; extract on the third use (Rule of
  Three).
- Accessibility: WCAG AA, 4.5:1 contrast, 44px touch targets, semantic labels.
- Loading states use skeletons, not spinners, for lists.
- Design handoff specs and component documentation.

### 5. UX Architect — navigation and information architecture
Source: `agency-agents/design/design-ux-architect.md`

For navigation structure, information architecture, layout foundations, and
the mobile-first responsive framework. Use when adding screens or reworking
flows.

- CSS design systems with variables, spacing scales, typography hierarchies.
- Layout frameworks using modern Grid/Flexbox; mobile-first breakpoints.
- Component architecture and naming conventions.
- Light/dark/system theme toggle (already implemented in `appStore.ts`).
- Interaction patterns and accessibility considerations.

### 6. DevOps Automator — CI pipeline (later)
Source: `agency-agents/engineering/engineering-devops-automator.md`

For the CI pipeline (Phase 0+): `npm run build` + `npm run lint` + `npx cap sync`
gated on PRs. Use when wiring GitHub Actions or release automation.

### 7. Test Results Analyzer — testing (Phase 4)
Source: `agency-agents/testing/testing-test-results-analyzer.md`

For Vitest + Testing Library result analysis and release-readiness assessment
in Phase 4. The other testing agents (`testing-evidence-collector`,
`testing-reality-checker`, `testing-performance-benchmarker`) are available for
evidence collection, cold-start benchmarking, and QA pass/fail checks.

## Working rules in this repo

- Read the target file and its callers before editing. Do not edit a file in
  the same turn you first read it.
- Match existing webapp style: 2-space indent, single quotes, no unused
  imports. Keep changes minimal; do not reformat untouched code.
- **Toolchain versions move.** Before pinning a version in `package.json` or
  `capacitor.config.ts`, read the project's current pins in
  `webapp/package.json`. Follow what the project pins unless the task is to
  upgrade. If you propose an upgrade, state the full compatibility chain
  (Node ↔ Capacitor ↔ Vite ↔ React ↔ TS) and why. When unsure or info may be
  stale, check the npm registry (`npm view <pkg> version`) — do not guess
  version numbers. Note: Capacitor 8+ requires Node ≥22; this project pins
  Capacitor 7 to stay on Node 20.
- Verify before claiming a change works, from `webapp/`:
  `npm run build`, `npm run lint`, and `npx cap sync` must all be green.
  State clearly if a check could not be run (e.g. no browser available for a
  visual runtime check).
- `app/` (Kotlin) is read-only reference. Do not edit it when working on the
  webapp.
- Do not add author/license headers unless asked.
