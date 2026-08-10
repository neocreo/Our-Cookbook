package com.ourcookbook.domain.repository

import com.ourcookbook.domain.model.Cookbook
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
    suspend fun addRecipeToCookbook(cookbookId: String, recipeId: String): Boolean
    suspend fun removeRecipeFromCookbook(cookbookId: String, recipeId: String): Boolean
    
    // Checksum Operations
    suspend fun validateCookbookChecksum(cookbookId: String): Boolean
    suspend fun updateCookbookChecksum(cookbookId: String): Boolean
}