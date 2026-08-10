package com.ourcookbook.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Export/Import Domain Models
 * Task 2.1.09: Export/Import Screen Implementation
 */

/**
 * Export format options
 */
enum class ExportFormat {
    JSON, MARKDOWN, PDF, DOCX
}

/**
 * Import format options
 */
enum class ImportFormat {
    JSON, MARKDOWN
}

/**
 * Export target types
 */
enum class ExportTarget {
    INDIVIDUAL_RECIPE,
    ENTIRE_COOKBOOK,
    ALL_RECIPES
}

/**
 * Import target types
 */
enum class ImportTarget {
    INDIVIDUAL_RECIPE,
    COOKBOOK,
    MULTIPLE_FILES
}

/**
 * Export/Import operation status
 */
enum class OperationStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
}

/**
 * Export/Import operation type
 */
enum class OperationType {
    EXPORT, IMPORT
}

/**
 * Model representing an export/import operation
 */
data class ExportImportOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val format: String, // ExportFormat or ImportFormat as string
    val target: String, // ExportTarget or ImportTarget as string
    val status: OperationStatus = OperationStatus.PENDING,
    val timestamp: Instant = Instant.now(),
    val fileCount: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val filePaths: List<String> = emptyList(),
    val errorMessages: List<String> = emptyList(),
    val progress: Float = 0f,
    val totalItems: Int = 0,
    val processedItems: Int = 0
) {
    val isCompleted: Boolean get() = status == OperationStatus.COMPLETED
    val isFailed: Boolean get() = status == OperationStatus.FAILED
    val isInProgress: Boolean get() = status == OperationStatus.IN_PROGRESS
    val hasErrors: Boolean get() = errorMessages.isNotEmpty()
    
    // For display purposes
    val displayName: String get() = when (type) {
        OperationType.EXPORT -> "Export to $format"
        OperationType.IMPORT -> "Import from $format"
    }
    
    val statusDisplay: String get() = when (status) {
        OperationStatus.PENDING -> "Pending"
        OperationStatus.IN_PROGRESS -> "In Progress"
        OperationStatus.COMPLETED -> "Completed"
        OperationStatus.FAILED -> "Failed"
        OperationStatus.CANCELLED -> "Cancelled"
    }
}

/**
 * Model for export settings
 */
data class ExportSettings(
    val format: ExportFormat = ExportFormat.JSON,
    val target: ExportTarget = ExportTarget.INDIVIDUAL_RECIPE,
    val includeImages: Boolean = true,
    val includeMetadata: Boolean = true,
    val exportLocation: String = "Downloads",
    val fileNamePattern: String = "cookbook_export_{timestamp}",
    val overwriteExisting: Boolean = false
)

/**
 * Model for import settings
 */
data class ImportSettings(
    val format: ImportFormat = ImportFormat.JSON,
    val target: ImportTarget = ImportTarget.INDIVIDUAL_RECIPE,
    val conflictResolution: ConflictResolutionStrategy = ConflictResolutionStrategy.ASK,
    val importLocation: String = "Internal Storage"
)

/**
 * Conflict resolution strategies
 */
enum class ConflictResolutionStrategy {
    ASK, // Ask user for each conflict
    OVERWRITE, // Overwrite existing
    SKIP, // Skip duplicates
    MERGE // Try to merge data
}

/**
 * Model for conflict detection during import
 */
data class ImportConflict(
    val recipeId: String,
    val existingRecipe: Recipe,
    val newRecipe: Recipe,
    val conflictType: ConflictType
)

/**
 * Types of conflicts that can occur during import
 */
enum class ConflictType {
    DUPLICATE_ID, // Same recipe ID
    DUPLICATE_TITLE, // Same title
    VERSION_CONFLICT, // Different versions of same recipe
    CHECKSUM_MISMATCH // Different checksums
}

/**
 * Model for export/import preview data
 */
data class ExportImportPreview(
    val operationId: String,
    val items: List<PreviewItem>,
    val totalCount: Int,
    val format: String,
    val estimatedSize: Long // in bytes
)

/**
 * Preview item for export/import
 */
data class PreviewItem(
    val id: String,
    val name: String,
    val type: String, // "recipe", "cookbook", etc.
    val size: Long, // in bytes
    val status: PreviewStatus = PreviewStatus.READY
)

/**
 * Preview item status
 */
enum class PreviewStatus {
    READY, PROCESSING, ERROR
}

/**
 * Model for batch operation result
 */
data class BatchOperationResult(
    val operationId: String,
    val totalItems: Int,
    val successfulItems: Int,
    val failedItems: Int,
    val failedItemIds: List<String> = emptyList(),
    val errorMessages: Map<String, String> = emptyMap(),
    val timestamp: Instant = Instant.now()
) {
    val successRate: Float get() = if (totalItems > 0) successfulItems.toFloat() / totalItems else 0f
    val hasFailures: Boolean get() = failedItems > 0
}

/**
 * Model for export file information
 */
data class ExportFileInfo(
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val format: ExportFormat,
    val createdAt: Instant = Instant.now(),
    val checksum: String = "",
    val recipeCount: Int = 0,
    val cookbookCount: Int = 0
)

/**
 * Model for import file information
 */
data class ImportFileInfo(
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val format: ImportFormat,
    val detectedRecipes: Int = 0,
    val detectedCookbooks: Int = 0,
    val isValid: Boolean = false,
    val validationErrors: List<String> = emptyList()
)