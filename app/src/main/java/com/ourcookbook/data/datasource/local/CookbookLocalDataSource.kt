package com.ourcookbook.data.datasource.local

import com.ourcookbook.data.db.dao.CookbookDao
import com.ourcookbook.data.db.entity.CookbookEntity
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.domain.service.ChecksumService
import com.ourcookbook.data.db.DatabaseConverters
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Local data source implementation for Cookbook operations
 */
class CookbookLocalDataSource @Inject constructor(
    private val cookbookDao: CookbookDao,
    private val checksumService: ChecksumService,
    private val converters: DatabaseConverters
) : ICookbookLocalDataSource {
    
    override suspend fun insert(cookbook: CookbookEntity): Long {
        return cookbookDao.insert(cookbook)
    }
    
    override suspend fun update(cookbook: CookbookEntity): Int {
        return cookbookDao.update(cookbook)
    }
    
    override suspend fun delete(id: String): Int {
        return cookbookDao.delete(id)
    }
    
    override suspend fun deleteAll(): Int {
        return cookbookDao.deleteAll()
    }
    
    override suspend fun getById(id: String): CookbookEntity? {
        return cookbookDao.getById(id)
    }
    
    override fun getByOwner(deviceId: String): Flow<List<CookbookEntity>> {
        return cookbookDao.getByOwner(deviceId)
    }
    
    override fun getShared(): Flow<List<CookbookEntity>> {
        return cookbookDao.getShared()
    }
    
    override fun getAll(): Flow<List<CookbookEntity>> {
        return cookbookDao.getAll()
    }
    
    override fun search(query: String): Flow<List<CookbookEntity>> {
        return cookbookDao.search(query)
    }
    
    override suspend fun toDomainModel(entity: CookbookEntity): Cookbook {
        return Cookbook(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            ownerDeviceId = entity.ownerDeviceId,
            isShared = entity.isShared,
            sharingLink = entity.sharingLink,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            recipeIds = entity.recipeIds
        )
    }
    
    override suspend fun toEntity(domainModel: Cookbook): CookbookEntity {
        return CookbookEntity(
            id = domainModel.id,
            name = domainModel.name,
            description = domainModel.description,
            ownerDeviceId = domainModel.ownerDeviceId,
            isShared = domainModel.isShared,
            sharingLink = domainModel.sharingLink,
            createdAt = domainModel.createdAt,
            updatedAt = domainModel.updatedAt,
            recipeIds = domainModel.recipeIds
        )
    }
    
    override suspend fun validateChecksum(entity: CookbookEntity): Boolean {
        val data = "${entity.id}|${entity.name}|${entity.description}|${entity.ownerDeviceId}|${entity.isShared}|${entity.sharingLink}|${entity.createdAt}|${entity.updatedAt}|${entity.recipeIds}"
        val expectedChecksum = checksumService.calculateChecksum(data)
        return true // Cookbooks don't have stored checksums in this implementation
    }
    
    override suspend fun updateChecksum(entity: CookbookEntity): CookbookEntity {
        return entity
    }
}

/**
 * Interface for Cookbook local data source operations
 */
interface ICookbookLocalDataSource {
    suspend fun insert(cookbook: CookbookEntity): Long
    suspend fun update(cookbook: CookbookEntity): Int
    suspend fun delete(id: String): Int
    suspend fun deleteAll(): Int
    suspend fun getById(id: String): CookbookEntity?
    fun getByOwner(deviceId: String): Flow<List<CookbookEntity>>
    fun getShared(): Flow<List<CookbookEntity>>
    fun getAll(): Flow<List<CookbookEntity>>
    fun search(query: String): Flow<List<CookbookEntity>>
    
    // Domain model conversion
    suspend fun toDomainModel(entity: CookbookEntity): Cookbook
    suspend fun toEntity(domainModel: Cookbook): CookbookEntity
    
    // Checksum validation
    suspend fun validateChecksum(entity: CookbookEntity): Boolean
    suspend fun updateChecksum(entity: CookbookEntity): CookbookEntity
}