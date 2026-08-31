package com.ourcookbook.domain.repository

import android.net.Uri
import com.ourcookbook.data.datasource.ExportLocation
import com.ourcookbook.domain.model.BatchOperationResult
import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.model.ExportFileInfo
import com.ourcookbook.domain.model.ExportFormat
import com.ourcookbook.domain.model.ExportImportOperation
import com.ourcookbook.domain.model.ExportImportPreview
import com.ourcookbook.domain.model.ExportSettings
import com.ourcookbook.domain.model.ImportConflict
import com.ourcookbook.domain.model.ImportFileInfo
import com.ourcookbook.domain.model.ImportFormat
import com.ourcookbook.domain.model.ImportSettings
import com.ourcookbook.domain.model.Recipe
import java.io.File

/**
 * Repository Interface for Export/Import Operations
 */
interface ExportImportRepository {

    // ==================== EXPORT OPERATIONS ====================

    suspend fun exportRecipe(
        recipeId: String,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo

    suspend fun exportRecipes(
        recipeIds: List<String>,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo

    suspend fun exportCookbook(
        cookbookId: String,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo

    suspend fun exportAllRecipes(
        format: ExportFormat,
        settings: ExportSettings = ExportSettings()
    ): ExportFileInfo

    suspend fun batchExport(
        items: List<String>,
        format: ExportFormat,
        settings: ExportSettings = ExportSettings(),
        onProgress: (Int, Int) -> Unit
    ): BatchOperationResult

    // ==================== IMPORT OPERATIONS ====================

    suspend fun importFromFile(
        filePath: String,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings()
    ): Pair<List<Recipe>, ImportFileInfo>

    suspend fun importCookbookFromFile(
        filePath: String,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings()
    ): Triple<Cookbook, List<Recipe>, ImportFileInfo>

    suspend fun previewImport(
        filePath: String,
        format: ImportFormat
    ): ExportImportPreview

    suspend fun validateImportFile(
        filePath: String,
        format: ImportFormat
    ): ImportFileInfo

    suspend fun detectFileFormat(filePath: String): ImportFormat?

    suspend fun detectFileFormat(uri: Uri): ImportFormat?

    suspend fun batchImport(
        filePaths: List<String>,
        format: ImportFormat,
        settings: ImportSettings = ImportSettings(),
        onProgress: (Int, Int) -> Unit,
        onConflict: (ImportConflict) -> ConflictResolution
    ): BatchOperationResult

    // ==================== CONFLICT DETECTION ====================

    suspend fun detectConflicts(
        recipesToImport: List<Recipe>
    ): List<ImportConflict>

    suspend fun resolveConflict(
        conflict: ImportConflict,
        resolution: ConflictResolution
    ): Recipe?

    // ==================== OPERATION HISTORY ====================

    suspend fun saveOperationToHistory(operation: ExportImportOperation)

    suspend fun loadOperationHistory(limit: Int = 50): List<ExportImportOperation>

    suspend fun clearOperationHistory()

    suspend fun deleteOperationFromHistory(operationId: String)

    // ==================== FILE OPERATIONS ====================

    fun getExportLocations(): List<ExportLocation>

    fun isExportLocationAvailable(location: String): Boolean

    fun listFilesInDirectory(directory: String, extensions: List<String> = emptyList()): List<File>

    fun fileExists(filePath: String): Boolean

    fun getFileSize(filePath: String): Long

    fun deleteFile(filePath: String): Boolean

    // ==================== CLOUD INTEGRATION ====================

    suspend fun exportToDrive(
        filePath: String,
        folderId: String? = null,
        fileName: String? = null
    ): ExportFileInfo?

    suspend fun importFromDrive(fileId: String): Pair<ByteArray, String>?

    suspend fun listDriveFiles(): List<ExportFileInfo>

    suspend fun deleteDriveFile(fileId: String): Boolean

    // ==================== UTILITY METHODS ====================

    suspend fun generateChecksum(filePath: String): String

    suspend fun generateChecksum(content: ByteArray): String
}
