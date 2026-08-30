package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.ui.viewmodel.CookbookSharingInfo
import com.ourcookbook.ui.viewmodel.ExportFormat
import com.ourcookbook.ui.viewmodel.Permission
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Cookbook operations
 * Defines the contract for cookbook data access in the domain layer
 */
interface CookbookRepository {
    
    // CRUD Operations
    suspend fun createCookbook(cookbook: Cookbook): String
    suspend fun updateCookbook(cookbook: Cookbook)
    suspend fun deleteCookbook(id: String)
    suspend fun getCookbookById(id: String): Cookbook?
    
    // Query Operations
    fun getCookbooksByOwner(deviceId: String): Flow<List<Cookbook>>
    fun getSharedCookbooks(): Flow<List<Cookbook>>
    fun getAllCookbooks(): Flow<List<Cookbook>>
    fun searchCookbooks(query: String): Flow<List<Cookbook>>
    
    // Utility Operations
    suspend fun getCookbookCount(): Int
    suspend fun getAllCookbooksOnce(): List<Cookbook>
    suspend fun addRecipeToCookbook(cookbookId: String, recipeId: String): Boolean
    suspend fun removeRecipeFromCookbook(cookbookId: String, recipeId: String): Boolean
    
    // Checksum Operations
    suspend fun validateCookbookChecksum(cookbookId: String): Boolean
    suspend fun updateCookbookChecksum(cookbookId: String): Boolean
    
    // Export/Import Operations
    suspend fun exportCookbook(cookbookId: String, format: com.ourcookbook.ui.viewmodel.ExportFormat, destinationFile: java.io.File)
    suspend fun importCookbook(sourceFile: java.io.File, format: com.ourcookbook.ui.viewmodel.ExportFormat): Cookbook
    
    // Sharing Operations
    suspend fun shareCookbook(cookbookId: String, userIds: List<String>, permissions: Set<com.ourcookbook.ui.viewmodel.Permission>)
    suspend fun generateSharingLink(cookbookId: String): String
    fun getSharingInfo(cookbookId: String): kotlinx.coroutines.flow.Flow<com.ourcookbook.ui.viewmodel.CookbookSharingInfo>
    suspend fun revokeSharing(cookbookId: String, userId: String)
}