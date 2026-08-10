package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.RecipeDao
import com.ourcookbook.data.db.entity.RecipeEntity
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.data.db.DatabaseConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Local data source implementation for Recipe operations
 * Handles database operations and entity-domain model conversion
 */
class RecipeLocalDataSource @Inject constructor(
    private val recipeDao: RecipeDao,
    private val checksumService: ChecksumService,
    private val converters: DatabaseConverters
) : IRecipeLocalDataSource {
    
    override suspend fun insert(recipe: RecipeEntity): Long {
        return recipeDao.insert(recipe)
    }
    
    override suspend fun insertAll(recipes: List<RecipeEntity>): List<Long> {
        return recipeDao.insertAll(recipes)
    }
    
    override suspend fun update(recipe: RecipeEntity): Int {
        return recipeDao.update(recipe)
    }
    
    override suspend fun updateAll(recipes: List<RecipeEntity>): Int {
        return recipeDao.updateAll(recipes)
    }
    
    override suspend fun delete(id: String): Int {
        return recipeDao.delete(id)
    }
    
    override suspend fun deleteAll(ids: List<String>): Int {
        return recipeDao.deleteAll(ids)
    }
    
    override suspend fun deleteAll(): Int {
        return recipeDao.deleteAll()
    }
    
    override suspend fun getById(id: String): RecipeEntity? {
        return recipeDao.getById(id)
    }
    
    override suspend fun getByIds(ids: List<String>): List<RecipeEntity> {
        return recipeDao.getByIds(ids)
    }
    
    override fun getAll(): Flow<List<RecipeEntity>> {
        return recipeDao.getAll()
    }
    
    override suspend fun getAllOnce(): List<RecipeEntity> {
        return recipeDao.getAllOnce()
    }
    
    override fun getFavorites(): Flow<List<RecipeEntity>> {
        return recipeDao.getFavorites()
    }
    
    override fun getByCategory(category: String): Flow<List<RecipeEntity>> {
        return recipeDao.getByCategory(category)
    }
    
    override fun getByDevice(deviceId: String): Flow<List<RecipeEntity>> {
        return recipeDao.getByDevice(deviceId)
    }
    
    override fun search(query: String): Flow<List<RecipeEntity>> {
        return recipeDao.search(query)
    }
    
    override suspend fun getByChecksum(checksum: String): RecipeEntity? {
        return recipeDao.getByChecksum(checksum)
    }
    
    override suspend fun getUpdatedSince(since: Instant): List<RecipeEntity> {
        return recipeDao.getUpdatedSince(since)
    }
    
    override suspend fun count(): Int {
        return recipeDao.count()
    }
    
    override suspend fun getRecent(limit: Int): List<RecipeEntity> {
        return recipeDao.getRecent(limit)
    }
    
    override suspend fun getTopRated(limit: Int): List<RecipeEntity> {
        return recipeDao.getTopRated(limit)
    }
    
    // Domain model conversion methods
    override suspend fun toDomainModel(entity: RecipeEntity): Recipe {
        return Recipe(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = entity.category,
            ingredients = converters.fromJsonToIngredients(entity.ingredientsJson),
            instructions = converters.fromJsonToInstructions(entity.instructionsJson),
            servingSize = entity.servingSize,
            prepTime = entity.prepTime,
            cookTime = entity.cookTime,
            rating = entity.rating,
            isFavorite = entity.isFavorite,
            imageUrl = entity.imageUrl,
            notes = entity.notes,
            source = entity.source,
            tags = entity.tagsJson?.let { converters.fromJsonToTags(it) } ?: emptyList(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            versionVector = converters.fromJsonToVersionVector(entity.versionVectorJson),
            checksum = entity.checksum,
            deviceId = entity.deviceId
        )
    }
    
    override suspend fun toEntity(domainModel: Recipe): RecipeEntity {
        return RecipeEntity(
            id = domainModel.id,
            title = domainModel.title,
            description = domainModel.description,
            category = domainModel.category,
            ingredientsJson = converters.fromIngredientsToJson(domainModel.ingredients),
            instructionsJson = converters.fromInstructionsToJson(domainModel.instructions),
            servingSize = domainModel.servingSize,
            prepTime = domainModel.prepTime,
            cookTime = domainModel.cookTime,
            rating = domainModel.rating,
            isFavorite = domainModel.isFavorite,
            imageUrl = domainModel.imageUrl,
            notes = domainModel.notes,
            source = domainModel.source,
            tagsJson = if (domainModel.tags.isNotEmpty()) converters.fromTagsToJson(domainModel.tags) else null,
            createdAt = domainModel.createdAt,
            updatedAt = domainModel.updatedAt,
            versionVectorJson = converters.fromVersionVectorToJson(domainModel.versionVector),
            checksum = domainModel.checksum,
            deviceId = domainModel.deviceId
        )
    }
    
    override suspend fun validateChecksum(entity: RecipeEntity): Boolean {
        val domainRecipe = toDomainModel(entity)
        val expectedChecksum = entity.checksum
        val actualChecksum = checksumService.calculateChecksum(domainRecipe)
        return actualChecksum == expectedChecksum
    }
    
    override suspend fun updateChecksum(entity: RecipeEntity): RecipeEntity {
        val domainRecipe = toDomainModel(entity)
        val updatedRecipe = checksumService.updateRecipeChecksum(domainRecipe)
        return toEntity(updatedRecipe)
    }
}

/**
 * Interface for Recipe local data source operations
 */
interface IRecipeLocalDataSource {
    suspend fun insert(recipe: RecipeEntity): Long
    suspend fun insertAll(recipes: List<RecipeEntity>): List<Long>
    suspend fun update(recipe: RecipeEntity): Int
    suspend fun updateAll(recipes: List<RecipeEntity>): Int
    suspend fun delete(id: String): Int
    suspend fun deleteAll(ids: List<String>): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): RecipeEntity?
    suspend fun getByIds(ids: List<String>): List<RecipeEntity>
    fun getAll(): Flow<List<RecipeEntity>>
    suspend fun getAllOnce(): List<RecipeEntity>
    fun getFavorites(): Flow<List<RecipeEntity>>
    fun getByCategory(category: String): Flow<List<RecipeEntity>>
    fun getByDevice(deviceId: String): Flow<List<RecipeEntity>>
    fun search(query: String): Flow<List<RecipeEntity>>
    suspend fun getByChecksum(checksum: String): RecipeEntity?
    suspend fun getUpdatedSince(since: Instant): List<RecipeEntity>
    suspend fun count(): Int
    suspend fun getRecent(limit: Int): List<RecipeEntity>
    suspend fun getTopRated(limit: Int): List<RecipeEntity>
    
    // Domain model conversion
    suspend fun toDomainModel(entity: RecipeEntity): Recipe
    suspend fun toEntity(domainModel: Recipe): RecipeEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: RecipeEntity): Boolean
    suspend fun updateChecksum(entity: RecipeEntity): RecipeEntity
}