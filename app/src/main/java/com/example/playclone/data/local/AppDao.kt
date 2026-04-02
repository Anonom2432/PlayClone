package com.example.playclone.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.playclone.data.model.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    
    @Query("SELECT * FROM apps ORDER BY updated_at DESC")
    fun getAllApps(): Flow<List<AppEntity>>
    
    @Query("SELECT * FROM apps WHERE id = :appId")
    suspend fun getAppById(appId: String): AppEntity?
    
    @Query("SELECT * FROM apps WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchApps(query: String): Flow<List<AppEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)
    
    @Update
    suspend fun updateApp(app: AppEntity)
    
    @Delete
    suspend fun deleteApp(app: AppEntity)
    
    @Query("DELETE FROM apps WHERE id = :appId")
    suspend fun deleteAppById(appId: String)
    
    @Query("SELECT COUNT(*) FROM apps")
    suspend fun getAppsCount(): Int
    
    @Query("SELECT * FROM apps WHERE is_installed = 1")
    fun getInstalledApps(): Flow<List<AppEntity>>
}
