# Our Cookbook Android App

## 📱 Project Overview

**Our Cookbook** is an Android application for collecting, organizing, and discovering recipes across multiple devices. The app supports manual entry, OCR scanning from books/screens, ingredient-based search, and multiple export formats. Designed to work on Android phones, tablets, and Chromebooks.

## 🎯 Key Features

### Multi-Device Support
- Multiple users with separate Android devices can use the app
- Shared cookbooks via Google Drive sync
- Conflict detection to prevent overwrites when multiple people edit the same cookbook
- Offline-first with sync on reconnect

### Recipe Management
- Manual recipe entry with full metadata (title, category, servings, times, etc.)
- OCR scanning from books and screens using ML Kit
- Ingredient-based search with smart matching
- Multiple export formats: Markdown, PDF, DOCX
- Categories: Breakfasts, Mains, Desserts & Snacks, Sides, Sauces and Spices

### Data Organization
- Multiple cookbook collections
- Recipe favorites and ratings
- Tag-based organization
- Advanced search and filtering

### Sharing & Collaboration
- Google Drive sync for multi-device sharing
- Export/import cookbooks for simple sharing
- QR code sharing for easy device-to-device transfer
- Permission system for cookbook access

## 🏗️ Technical Architecture

### Technology Stack
- **Language**: Kotlin (primary)
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture
- **Database**: Room with SQLite (SQLCipher for encryption)
- **Dependency Injection**: Hilt
- **Background Processing**: WorkManager
- **Camera & OCR**: CameraX + ML Kit Text Recognition
- **Google Drive Integration**: Google Drive API v3
- **PDF Generation**: iTextPDF 7
- **QR Codes**: ZXing

### Platform Support
- **Minimum Android Version**: 8.0 (API 26+)
- **Target Devices**: Android phones, tablets, Chromebooks
- **Build System**: Gradle (Kotlin DSL)

## 📁 Project Structure

```
Our Cookbook/
├── app/                          # Android application
│   ├── build.gradle              # App-level build configuration
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/cookbook/
│   │   │   │   ├── data/                # Data layer
│   │   │   │   │   ├── model/           # Data models
│   │   │   │   │   ├── repository/      # Repositories
│   │   │   │   │   ├── datasource/      # Data sources
│   │   │   │   │   │   ├── local/        # Local database
│   │   │   │   │   │   ├── file/         # File operations
│   │   │   │   │   │   └── sync/         # Sync operations
│   │   │   │   │   └── db/             # Database
│   │   │   │   ├── di/                 # Dependency injection
│   │   │   │   ├── domain/             # Domain layer
│   │   │   │   │   ├── usecase/        # Use cases
│   │   │   │   │   └── model/          # Domain models
│   │   │   │   ├── ui/                 # UI layer
│   │   │   │   │   ├── theme/          # Theme and styling
│   │   │   │   │   ├── components/     # Reusable components
│   │   │   │   │   ├── screens/        # All app screens
│   │   │   │   │   │   ├── home/        # Home screen
│   │   │   │   │   │   ├── list/        # Recipe list
│   │   │   │   │   │   ├── detail/      # Recipe detail
│   │   │   │   │   │   ├── create/      # Create recipe
│   │   │   │   │   │   ├── edit/        # Edit recipe
│   │   │   │   │   │   ├── search/      # Search
│   │   │   │   │   │   ├── scan/        # OCR scanning
│   │   │   │   │   │   ├── import/      # Import
│   │   │   │   │   │   ├── profile/     # User profile
│   │   │   │   │   │   ├── cookbooks/   # Cookbook management
│   │   │   │   │   │   └── sync/        # Sync management
│   │   │   │   │   └── navigation/     # Navigation
│   │   │   │   └── utils/              # Utilities
│   │   │   └── assets/               # Static assets
│   │   └── test/                   # Tests
│   └── build/                     # Build outputs
├── build.gradle                   # Project-level build
├── settings.gradle                # Project settings
├── gradle.properties              # Gradle properties
├── project-specs/                # Project specifications
│   └── cookbook-android-setup.md  # Main specification
├── project-tasks/                # Task management
│   └── cookbook-android-tasklist.md # Comprehensive task list
├── project-docs/                 # Documentation
│   ├── COOKBOOK_DEVELOPMENT_PLAN.md  # Development plan
│   ├── TEAM_ASSEMBLY.md            # Team structure
│   ├── PIPELINE_STATUS.md          # Pipeline status
│   └── cookbook-architecture.md     # Technical architecture (TBD)
└── README.md                      # This file
```

## 🚀 Development Pipeline

This project uses an **autonomous agent pipeline** managed by **AgentsOrchestrator** with the following workflow:

### Pipeline Phases

1. **Phase 1: Project Foundation** (Weeks 1-4)
   - Project setup and configuration
   - Technical architecture design
   - Core data layer implementation
   - Basic UI foundation

2. **Phase 2: Enhanced Features** (Weeks 5-8)
   - Advanced search and filtering
   - Google Drive integration
   - Sync system completion
   - Export/import functionality

3. **Phase 3: Advanced Features** (Weeks 9-12)
   - OCR scanning implementation
   - User management and profiles
   - Cookbook management and sharing
   - Polish and optimization

4. **Phase 4: Testing & Production** (Weeks 13-16)
   - Comprehensive testing
   - Performance optimization
   - Security implementation
   - Final validation and deployment

### Quality Assurance
- **Continuous QA**: Every task must pass EvidenceQA validation
- **Screenshot Evidence**: Required for all UI implementations
- **Retry Logic**: Maximum 3 attempts per task with specific feedback
- **Quality Gates**: No task advances without passing QA
- **Final Validation**: testing-reality-checker performs comprehensive final testing

### Team Composition
| Role | Agent | Responsibilities |
|------|-------|------------------|
| Pipeline Manager | AgentsOrchestrator | Overall coordination and quality enforcement |
| Project Manager | project-manager-senior | Task breakdown and milestone tracking |
| Technical Architect | ArchitectUX | System architecture and UX design |
| Backend Developer | Backend Architect | Database, sync, backend services |
| Frontend Developer | Frontend Developer | UI, Compose, navigation |
| Mobile Developer | Mobile App Builder | Android platform, camera, OCR |
| DevOps Engineer | DevOps Automator | CI/CD, build, deployment |
| QA Engineer | EvidenceQA | Continuous quality validation |
| Integration Tester | testing-reality-checker | Final validation and production readiness |

## 📊 Project Metrics

- **Total Tasks**: 151
- **Estimated Effort**: ~1,210 hours
- **Timeline**: 16 weeks
- **Target Platforms**: Android 8.0+, Chromebooks
- **Quality Target**: > 80% first-pass success rate

## 🛠️ Setup & Installation

### Prerequisites
- Android Studio (latest stable version)
- Android SDK (API 26+)
- Java JDK 17+
- Kotlin 1.9+
- Git

### Clone the Repository
```bash
git clone https://github.com/your-username/Our-Cookbook.git
cd Our-Cookbook
```

### Build the Project
```bash
# Sync Gradle
./gradlew --refresh-dependencies

# Build debug APK
./gradlew assembleDebug

# Run on device/emulator
./gradlew installDebug
```

## 📋 Task Management

All development tasks are tracked in `project-tasks/cookbook-android-tasklist.md` with:
- Task IDs and descriptions
- Agent assignments
- Priority levels (High, Medium, Low)
- Dependencies
- Time estimates
- Status tracking

## 🎯 Getting Started for Developers

### For Backend Developers
1. Review data models in `app/src/main/java/com/example/cookbook/data/model/`
2. Implement repositories in `app/src/main/java/com/example/cookbook/data/repository/`
3. Create data sources in `app/src/main/java/com/example/cookbook/data/datasource/`
4. Follow the architecture patterns defined in `project-docs/cookbook-architecture.md`

### For Frontend Developers
1. Review UI components in `app/src/main/java/com/example/cookbook/ui/components/`
2. Implement screens in `app/src/main/java/com/example/cookbook/ui/screens/`
3. Create ViewModels in corresponding screen directories
4. Use the design system defined in `app/src/main/java/com/example/cookbook/ui/theme/`

### For Mobile Developers
1. Configure platform-specific features in AndroidManifest.xml
2. Implement CameraX and ML Kit integrations
3. Handle permissions and device capabilities
4. Optimize for Chromebook compatibility

## 🔒 Security & Privacy

### Data Protection
- SQLite database encrypted with SQLCipher
- Sensitive fields (notes, source) encrypted at rest
- Secure credential storage using Android Credential Manager
- Minimal Google Drive scope (drive.file only)

### Privacy Compliance
- GDPR compliance: Right to access, erasure, portability
- Data validation for all imports
- Privacy settings for users
- Secure data handling practices

## 📚 Documentation

- **Project Specification**: `project-specs/cookbook-android-setup.md`
- **Development Plan**: `project-docs/COOKBOOK_DEVELOPMENT_PLAN.md`
- **Team Structure**: `project-docs/TEAM_ASSEMBLY.md`
- **Pipeline Status**: `project-docs/PIPELINE_STATUS.md`
- **Task List**: `project-tasks/cookbook-android-tasklist.md`

## 🤝 Contributing

This project uses an autonomous agent pipeline for development. To contribute:

1. **For Agent Developers**: Follow the task assignments from AgentsOrchestrator
2. **For Manual Contributors**: Coordinate with AgentsOrchestrator for task assignments
3. **Quality Standards**: All contributions must pass EvidenceQA validation
4. **Documentation**: Update relevant documentation for any changes

## 📞 Support & Issues

- **Pipeline Issues**: Report to AgentsOrchestrator
- **Technical Issues**: Check pipeline status and task assignments
- **Quality Issues**: Contact EvidenceQA for validation concerns

## 📄 License

This project is proprietary. All rights reserved.

## 🏷️ Version Information

- **Version**: 1.0.0 (Planning Phase)
- **Last Updated**: August 8, 2026
- **Status**: Development Pipeline Initialized
- **Next Milestone**: Phase 1 Completion (Week 4)

---

## 🚀 Launch the Pipeline

To start the autonomous development pipeline:

```
Please spawn an agents-orchestrator to execute complete development pipeline 
for project-specs/cookbook-android-setup.md. 

Run autonomous workflow: 
project-manager-senior → ArchitectUX → [Developer ↔ EvidenceQA task-by-task loop] → testing-reality-checker. 

Each task must pass QA before advancing.
```

---

**Maintained by**: AgentsOrchestrator  
**Project Lead**: project-manager-senior  
**Technical Lead**: ArchitectUX  
**Status**: 🟢 Ready for Pipeline Execution