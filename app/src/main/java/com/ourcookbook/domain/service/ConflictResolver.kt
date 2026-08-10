package com.ourcookbook.domain.service

import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.ConflictResolutionResult
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.model.ResolutionAction
import com.ourcookbook.domain.model.SyncConflict
import java.time.Instant
import java.util.UUID

/**
 * Service interface for conflict detection and resolution
 * Provides functionality for identifying and resolving sync conflicts
 */
interface ConflictResolver {
    
    /**
     * Detect if there is a conflict between local and remote recipes
     * 
     * @param localRecipe The local recipe
     * @param remoteRecipe The remote recipe
     * @return SyncConflict if conflict detected, null if no conflict
     */
    suspend fun detectConflict(
        localRecipe: Recipe,
        remoteRecipe: Recipe
    ): SyncConflict?
    
    /**
     * Resolve a detected conflict using the specified strategy
     * 
     * @param conflict The conflict to resolve
     * @param strategy The resolution strategy to use
     * @return The result of the conflict resolution
     */
    suspend fun resolveConflict(
        conflict: SyncConflict,
        strategy: ConflictResolution
    ): ConflictResolutionResult
    
    /**
     * Check if two recipes have conflicting changes
     * 
     * @param localRecipe The local recipe
     * @param remoteRecipe The remote recipe
     * @return true if recipes have conflicting changes
     */
    fun hasConflict(localRecipe: Recipe, remoteRecipe: Recipe): Boolean
    
    /**
     * Check if a conflict can be automatically resolved
     * 
     * @param conflict The conflict to check
     * @return true if conflict can be auto-resolved
     */
    fun canAutoResolve(conflict: SyncConflict): Boolean
    
    /**
     * Auto-resolve a conflict if possible
     * 
     * @param conflict The conflict to auto-resolve
     * @return ConflictResolutionResult if auto-resolved, null if manual resolution needed
     */
    suspend fun autoResolve(conflict: SyncConflict): ConflictResolutionResult?
}
