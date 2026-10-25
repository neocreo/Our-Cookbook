# Changelog

All notable changes to Our Open Cookbook are documented here. Each version
entry carries a **release name** that is used verbatim for the Play Console
release (e.g. `1.0.0 — First Serving`).

Format: [Keep a Changelog](https://keepachangelog.com/), versioning follows
the Android `versionName` (and `versionCode` increments on every upload).

## [Unreleased]

Nothing yet.

## [1.0.0] — First Serving — 2026-10-25

Initial internal test release. versionCode 1, minSdk 24.

### Added

- Recipe management: create, edit, favorite, search, and organize recipes
  into cookbooks, with per-line ingredients and named ingredient lists
- OCR scanning: photograph or upload a recipe (JPG/PNG/PDF), mark scan
  regions with pinch-zoom, and parse into a structured recipe
  (English or Swedish)
- Google Drive sync: sign in with Google and sync to the app's folder in
  the user's own Drive — voluntary, for backup and for sharing across
  devices. Native (WebView) sign-in navigates to Google and captures the
  token from the redirect
- Export/import as Markdown and JSON
- PWA: installable with offline service worker
- Website at opencookbook.online (landing page, privacy policy) deployed
  via FTP pipeline

### Changed

- Seed data is a single Example Cookbook with five recipes from Gwen and
  Daniel's kitchen (breakfast, lunch, hot sauce, chili, cherry pie)

### Fixed

- Drive root folder lookup no longer calls `/files/root`, which 404s under
  the `drive.file` scope — the folder is found by name or created at the
  top of My Drive
- minSdkVersion raised from 23 to 24 (Play automatic protection)
- targetSdkVersion/compileSdkVersion raised from 35 to 36 (Play requirement),
  Android Gradle plugin 8.7.2 → 8.9.2

## Naming convention

Release names follow the pattern `<versionName> — <Short Evocative Name>`.
Pick the name from the dominant theme of the release when the changelog
entry is written, and keep it consistent between Play Console releases and
git tags (`git tag v1.0.0 "1.0.0 — First Serving"`).
