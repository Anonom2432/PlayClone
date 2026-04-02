package com.example.playclone.data.repository

import android.util.Log
import com.example.playclone.data.local.AppDao
import com.example.playclone.data.model.AppEntity
import com.example.playclone.domain.model.AppItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstAppRepository(
    private val appDao: AppDao,
    private val remoteRepository: AppRepository? = null
) : AppRepository {
    
    private val TAG = "OfflineFirstRepo"
    
    override fun getAllApps(): Flow<List<AppItem>> {
        return appDao.getAllApps().map { entities ->
            entities.map { it.toAppItem() }
        }
    }
    
    override suspend fun getAppById(appId: String): AppItem? {
        return appDao.getAppById(appId)?.toAppItem()
    }
    
    override fun searchApps(query: String): Flow<List<AppItem>> {
        return appDao.searchApps(query).map { entities ->
            entities.map { it.toAppItem() }
        }
    }
    
    override suspend fun addApp(app: AppItem): Result<String> {
        return try {
            val appEntity = AppEntity.fromAppItem(app)
            appDao.insertApp(appEntity)
            
            Log.d(TAG, "Приложение сохранено локально: ${app.name}")
            
            remoteRepository?.let { remote ->
                if (remote.isRemoteAvailable()) {
                    val appForRemote = app.copy(id = "")
                    remote.addApp(appForRemote)
                        .onSuccess { remoteId ->
                            Log.d(TAG, "Приложение синхронизировано с Firebase: $remoteId")
                        }
                        .onFailure { error ->
                            Log.e(TAG, "Ошибка синхронизации с Firebase: ${error.message}")
                        }
                }
            }
            
            Result.success(app.id.ifEmpty { appEntity.id })
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка добавления приложения: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun updateApp(app: AppItem): Result<Unit> {
        return try {
            val appEntity = AppEntity.fromAppItem(app)
            appDao.updateApp(appEntity)
            
            Log.d(TAG, "Приложение обновлено локально: ${app.name}")
            
            remoteRepository?.let { remote ->
                if (remote.isRemoteAvailable()) {
                    remote.updateApp(app)
                        .onFailure { error ->
                            Log.e(TAG, "Ошибка обновления в Firebase: ${error.message}")
                        }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка обновления приложения: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteApp(appId: String): Result<Unit> {
        return try {
            appDao.deleteAppById(appId)
            
            Log.d(TAG, "Приложение удалено локально: $appId")
            
            remoteRepository?.let { remote ->
                if (remote.isRemoteAvailable()) {
                    remote.deleteApp(appId)
                        .onFailure { error ->
                            Log.e(TAG, "Ошибка удаления из Firebase: ${error.message}")
                        }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления приложения: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun syncWithRemote(): Result<Unit> {
        return remoteRepository?.syncWithRemote() ?: Result.success(Unit)
    }
    
    override suspend fun isRemoteAvailable(): Boolean {
        return remoteRepository?.isRemoteAvailable() ?: false
    }
}
