# AGENTS.md — Our Cookbook

This file instructs any AI agent working in the Our Cookbook repository.
It selects the specialists for this project from the `agency-agents` repo
(`../../agency-agents/`) and binds the software design principles that all
work here must follow.

## Project context

Our Cookbook is an **Android-only** application. Every decision below is made
through that lens.

- **Platform**: Android, min SDK 26 (Android 8.0), target SDK 36
  (Android 16). Phones, tablets, and Chromebooks. No iOS target. Google
  Play requires targetSdk 36 for new apps and updates since 31 Aug 2026.
- **Language**: Kotlin 2.4.x.
- **UI**: Jetpack Compose with Material Design 3, Compose BOM 2026.08.00.
  Use the Compose Compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`)
  version that matches the Kotlin version — not the old `composeOptions`/
  `kotlinCompilerExtensionVersion` setup.
- **Architecture**: MVVM with Clean Architecture layering (data / domain / ui).
- **Build**: Android Studio Quail 4, AGP 9.2.0, Gradle 9.7.1 via the
  wrapper (`./gradlew`). AGP 9.2.0 requires Gradle 9.x.
- **Persistence**: Room over SQLite, SQLCipher for encryption.
- **DI**: Hilt.
- **Background work**: WorkManager.
- **Camera / OCR**: CameraX + ML Kit Text Recognition.
- **Sync**: Google Drive API v3, offline-first with conflict detection.
- **Export**: Markdown, PDF (iTextPDF 7), DOCX. QR sharing via ZXing.

Source layout lives under `app/src/main/java/com/ourcookbook/`:
`data/` (datasource local/remote, db, repository, service, model),
plus `domain/` and `ui/` packages. Match this layering when adding code.

## Selected agents

Activate the agent whose scope matches the task. Source files are in the
`agency-agents` repo.

### 1. Mobile App Builder — primary engineering agent
Source: `agency-agents/engineering/engineering-mobile-app-builder.md`

Use this agent for all feature implementation: screens, navigation,
platform integrations (camera, biometrics, push), offline-first sync,
and Android performance work.

For Our Cookbook, apply its Android-specific guidance and ignore the
iOS/SwiftUI and React Native/Flutter sections:
- Build screens with **Jetpack Compose** and Material 3 components.
- Use **Hilt** `@HiltViewModel` for state holders; expose `StateFlow` and
  collect with `collectAsStateWithLifecycle()`.
- Use `LazyColumn`/`LazyRow` with stable `key` for lists; paginate in the
  repository, not the UI.
- Offline-first: all writes go through Room first, then queue
  `PendingSync` and let WorkManager push to Drive.
- Follow Material Design 3 navigation patterns (bottom nav, nav host,
  sheets) — do not invent custom navigation.
- Performance budgets: cold start < 3 s, memory < 100 MB core, crash-free
  > 99.5%.

### 2. Backend Architect — architecture and data design
Source: `agency-agents/engineering/engineering-backend-architect.md`

Use this agent for repository/data-layer design, Room schema, Drive sync
contract, and conflict-resolution logic. Its backend principles map onto
the Android data layer: repositories are services, DAOs are the data
access layer, Drive is the remote data source.

- Repositories own one bounded context each (recipes, cookbooks, sync,
  search, export). Do not let a recipe repository reach into sync tables.
- Define repository interfaces in `domain/`; implement them in
  `data/repository`. Hilt binds interface to implementation.
- API/sync contracts (Drive file format, sync metadata schema) are costly
  to change — get them right early (YAGNI exception).
- Keep Drive traffic versioned and backwards-compatible; old app versions
  must still read shared cookbooks.

### 3. UI Designer — visual system and Compose components
Source: `agency-agents/design/design-ui-designer.md`

Use this agent for the Compose design system: color/typography/spacing
tokens, reusable components, states (loading, error, empty), and
accessibility.

- Maintain a Material 3 `ColorScheme`, `Typography`, and `Shapes` as the
  single token source; support light and dark themes.
- Reusable composables live in `ui/components/`; screens compose them.
  Do not duplicate styling across screens — extract on the third use
  (Rule of Three).
- Accessibility: 4.5:1 contrast for text, 48dp touch targets, semantic
  content descriptions, support for font scaling and reduced motion.
- Define component states (default, hover/pressed, focused, disabled,
  loading, error, empty) so implementation is unambiguous.
- Loading states use skeleton screens, not spinners, for content lists.

## Software design principles (binding)

These come from `agency-agents/Rules/software-design-principles.md` and
apply to all code written here. Priority order when they conflict:

1. **KISS** — choose the simpler, readable solution.
2. **YAGNI** — build for the current requirement; the exception is sync
   schema and Drive file format, which are costly to change later.
3. **Separation of Concerns** — keep `data` / `domain` / `ui` apart; a
   composable must not touch DAOs or Drive directly.
4. **DRY** — but not at the cost of clarity; some duplication beats a
   forced abstraction.
5. **SOLID** — apply gradually as the app grows.

Applied to this project:

- **Single Responsibility**: one `@HiltViewModel` per screen; one
  repository per bounded context; one DAO per entity group.
- **Dependency Inversion**: ViewModels depend on repository interfaces,
  not `Impl` classes. Repositories depend on `DataSource` interfaces,
  not concrete DAOs or Drive clients. Hilt wires the graph.
- **Composition over inheritance**: share behavior via composition and
  Hilt-bound collaborators, not deep class hierarchies.
- **Open/Closed**: add new export formats or sync strategies by
  implementing an interface, not by editing shared services.
- **Law of Demeter**: a composable calls its ViewModel; the ViewModel
  calls its repositories; do not chain `vm.repo.dao.drive.client...`.
- **Rule of Three**: extract a composable or utility on the third use,
  not the first.

## Working rules in this repo

- Read the target file and its callers before editing. Do not edit a
  file in the same turn you first read it.
- Match existing Kotlin style: 4-space indent, no trailing wildcard
  imports, `suspend` for repository methods that hit Room/Drive.
- Keep changes minimal; do not reformat untouched code.
- Prefer editing existing files over creating new ones.
- **Toolchain versions move every few weeks and mismatches break the
  build.** Before specifying a version in `build.gradle`, `libs.versions.toml`,
  or the wrapper, read the project's current versions
  (`gradle/libs.versions.toml`, root and module `build.gradle(.kts)`,
  `gradle/wrapper/gradle-wrapper.properties`). Follow what the project
  pins unless the task is to upgrade. If you propose an upgrade, state
  the full compatibility chain: Kotlin ↔ Compose Compiler plugin ↔ Compose
  BOM ↔ AGP ↔ Gradle, and why each step moves. **When you are unsure or
  the info may be stale, search the web for the current stable versions**
  of Android Studio, Kotlin, AGP, Gradle, and the Compose BOM, and cite
  the official source (developer.android.com, kotlinlang.org, gradle.org,
  blog.jetbrains.com). Do not guess version numbers.
- Verify with the existing Gradle tasks before claiming a change works:
  `./gradlew :app:compileDebugKotlin` and the relevant `:app:testDebugUnitTest`.
  State clearly if verification could not be run in this environment.
- Do not add author/license headers unless asked.
