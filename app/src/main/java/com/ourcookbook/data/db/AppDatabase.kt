package com.ourcookbook.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ourcookbook.data.db.dao.*
import com.ourcookbook.data.db.entity.*
import com.ourcookbook.data.model.SearchHistoryEntity
import com.ourcookbook.data.model.SavedSearchEntity
import net.sqlcipher.database.SupportFactory

/**
 * Main database class for the Cookbook app
 * Uses SQLCipher for encryption
 */
@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        RecipeImageEntity::class,
        DeviceEntity::class,
        DevicePreferencesEntity::class,
        CookbookEntity::class,
        SharingLinkEntity::class,
        SyncConflictEntity::class,
        SyncLogEntity::class,
        PendingSyncEntity::class,
        SyncMetadataEntity::class,
        DriveFileInfoEntity::class,
        TombstoneEntity::class,
        RecipeFtsEntity::class,
        SearchHistoryEntity::class,
        SavedSearchEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeImageDao(): RecipeImageDao
    abstract fun deviceDao(): DeviceDao
    abstract fun devicePreferencesDao(): DevicePreferencesDao
    abstract fun cookbookDao(): CookbookDao
    abstract fun sharingLinkDao(): SharingLinkDao
    abstract fun syncConflictDao(): SyncConflictDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun driveFileInfoDao(): DriveFileInfoDao
    abstract fun tombstoneDao(): TombstoneDao
    abstract fun recipeFtsDao(): RecipeFtsDao
    
    companion object {
        private const val DATABASE_NAME = "cookbook-db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
         fun getInstance(context: Context, passphrase: String): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(SupportFactory(passphrase.toByteArray()))
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun destroyInstance() {
            INSTANCE = null
        }
        
        // Migration for adding FTS5 table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS recipes_fts 
                    USING fts5(
                        id, 
                        title, 
                        description, 
                        ingredients, 
                        instructions, 
                        category,
                        tokenize='unicode61 remove_diacritics 2'
                    )
                """)
            }
        }
    }
}
