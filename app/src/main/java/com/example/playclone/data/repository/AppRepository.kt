package com.example.playclone.data.repository

import com.example.playclone.data.model.AppEntity
import com.example.playclone.domain.model.AppItem
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    
    fun getAllApps(): Flow<List<AppItem>>
    
    suspend fun getAppById(appId: String): AppItem?
    
    fun searchApps(query: String): Flow<List<AppItem>>
    
    suspend fun addApp(app: AppItem): Result<String>
    
    suspend fun updateApp(app: AppItem): Result<Unit>
    
    suspend fun deleteApp(appId: String): Result<Unit>
    
    suspend fun syncWithRemote(): Result<Unit>
    
    suspend fun isRemoteAvailable(): Boolean
}
