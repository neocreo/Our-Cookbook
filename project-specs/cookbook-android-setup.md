# Android Cookbook App - Project Specification

## Project Overview
**Project Name**: Our Cookbook Android App
**Version**: 1.0.0
**Target Platform**: Android 8.0+ (API 26+), Chromebooks
**Primary Goal**: Multi-device recipe management with Google Drive sync

## Core Requirements

### 1. Multi-Device Support
- Multiple users with separate Android devices can use the app
- Shared cookbooks via Google Drive sync
- Conflict detection before pushing data to prevent overwrites
- Offline-first with sync on reconnect

### 2. Recipe Management
- Manual recipe entry with full metadata
- OCR scanning from books/screens using ML Kit
- Ingredient-based search functionality
- Multiple export formats (Markdown, PDF, DOCX)
- Categories: Breakfasts, Mains, Desserts & Snacks, Sides, Sauces and Spices

### 3. Data Model Requirements
- Recipe schema with versioning and checksums
- Device management and preferences
- Cookbook collections with sharing capabilities
- Sync metadata and conflict resolution
- Tombstone handling for deletions

### 4. Technical Architecture
- **Language**: Kotlin (primary)
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room with SQLite (SQLCipher for encryption)
- **Dependency Injection**: Hilt
- **Background Processing**: WorkManager
- **Google Drive Integration**: For multi-device sync

### 5. Key Features to Implement

#### Phase 1: Core Functionality (MVP)
- Project setup with Compose + Room
- Complete data layer with all models
- Checksum service and version vector tracking
- Core screens: Home, List, Detail
- Manual recipe creation/editing
- Basic search functionality
- Local storage with Room
- Cookbook management
- Device registration

#### Phase 2: Enhanced Features
- Full-text search with FTS5
- Category and tag filtering
- Delete functionality with tombstones
- Favorites system
- Markdown import/export
- Rating system
- Google Drive authentication
- Sync metadata tracking
- Basic pull changes from Drive
- Checksum-based conflict detection

#### Phase 3: Advanced Features
- OCR scanning with CameraX + ML Kit
- Ingredient-based search
- PDF and DOCX export
- File import (multiple formats)
- Batch operations
- Responsive design for tablets
- Export/import for sharing
- Push changes to Google Drive
- Batched Drive operations
- Conflict resolution UI
- Sync status indicators

## Technical Specifications

### Data Models Required
1. **Recipe**: Full schema with ingredients, instructions, metadata
2. **Ingredient**: Amount (imperial/metric), name, notes
3. **RecipeImage**: Multiple images per recipe with typed storage
4. **Device**: Device ID, name, preferences, capabilities
5. **DevicePreferences**: Theme, measurement system, sync settings
6. **Cookbook**: Collections with sharing and sync capabilities
7. **SharingLink**: Token-based sharing with permissions
8. **SyncConflict**: Conflict detection and resolution
9. **SyncLog**: Audit trail for sync operations
10. **PendingSync**: Queue for offline changes
11. **SyncMetadata**: Per-device sync state
12. **DriveFileInfo**: Google Drive file metadata
13. **Tombstone**: Deletion markers

### Security Requirements
- SQLite encryption with SQLCipher
- Secure credential storage using Android Credential Manager
- Minimal Google Drive scope (drive.file only)
- Data validation for all imports
- Privacy compliance (GDPR right to access/erasure)

### Performance Requirements
- Database indexing for fast queries
- Pagination for recipe lists
- Lazy loading of images
- Background processing for exports
- Caching for search results

## Project Structure

```
Our Cookbook/
├── app/
│   ├── build.gradle
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/cookbook/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── repository/
│   │   │   │   │   ├── datasource/
│   │   │   │   │   └── db/
│   │   │   │   ├── di/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── usecase/
│   │   │   │   │   └── model/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── screens/
│   │   │   │   │   └── navigation/
│   │   │   │   └── utils/
│   │   │   └── assets/
│   │   └── test/
│   └── build/
├── build.gradle (project)
├── settings.gradle
└── gradle.properties
```

## Dependencies
- AndroidX Core, Compose, Room, WorkManager
- Hilt for dependency injection
- CameraX for image capture
- ML Kit for OCR
- Google Drive API
- iTextPDF for PDF generation
- Jackson for JSON processing
- ZXing for QR codes
- SQLCipher for encryption

## Quality Requirements
- Unit tests for ViewModels, UseCases, Repositories
- UI tests for critical user journeys
- Integration tests for database operations
- Manual testing on various device sizes
- Chromebook-specific testing
- Continuous QA validation with EvidenceQA

## Success Criteria
- All tasks must pass QA validation before advancing
- Each implementation task requires screenshot evidence
- Maximum 3 retry attempts per task
- Final integration testing with testing-reality-checker
- Production-ready quality standards

## Team Roles Required
1. **Backend Architect**: Database design, sync system, conflict resolution
2. **Frontend Developer**: Jetpack Compose UI, navigation, responsive design
3. **DevOps Automator**: CI/CD, build configuration, deployment
4. **EvidenceQA**: Quality assurance, testing, validation
5. **Mobile App Builder**: Android-specific implementation, platform integration
6. **project-manager-senior**: Task management, coordination, planning

## Deliverables
1. Complete Android application with all specified features
2. Comprehensive test suite
3. Documentation for all systems
4. Production-ready deployment pipeline
5. QA validation reports with screenshot evidence