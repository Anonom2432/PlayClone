package com.example.playclone.data.repository

import com.example.playclone.data.local.AppDao
import com.example.playclone.data.model.AppEntity
import com.example.playclone.domain.model.AppItem
import com.example.playclone.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class LocalMockRepository(
    private val appDao: AppDao
) : AppRepository {
    
    private val mockApps = listOf(
        AppItem(
            id = "mock_1",
            name = "Мессенджер Pro",
            description = "Быстрый и безопасный мессенджер с поддержкой голосовых и видеозвонков.",
            category = "Общение",
            sizeMb = 85,
            rating = 4.5f,
            iconUrl = "",
            screenshotUrls = emptyList(),
            developerName = "ChatCorp",
            version = "3.2.1",
            updatedAt = System.currentTimeMillis() - 3600000
        ),
        AppItem(
            id = "mock_2",
            name = "FitTracker",
            description = "Ваш персональный тренер в кармане. Отслеживайте тренировки и калории.",
            category = "Здоровье и фитнес",
            sizeMb = 42,
            rating = 4.7f,
            iconUrl = "",
            screenshotUrls = emptyList(),
            developerName = "HealthTech",
            version = "2.0.5",
            updatedAt = System.currentTimeMillis() - 7200000
        ),
        AppItem(
            id = "mock_3",
            name = "Photo Editor Plus",
            description = "Профессиональное редактирование фото на вашем смартфоне.",
            category = "Фото и видео",
            sizeMb = 128,
            rating = 4.3f,
            iconUrl = "",
            screenshotUrls = emptyList(),
            developerName = "CreativeApps",
            version = "5.1.0",
            updatedAt = System.currentTimeMillis() - 10800000
        ),
        AppItem(
            id = "mock_4",
            name = "News Reader",
            description = "Все новости мира в одном приложении с умной лентой.",
            category = "Новости",
            sizeMb = 35,
            rating = 4.1f,
            iconUrl = "",
            screenshotUrls = emptyList(),
            developerName = "NewsMedia",
            version = "1.8.3",
            updatedAt = System.currentTimeMillis() - 14400000
        ),
        AppItem(
            id = "mock_5",
            name = "Music Player",
            description = "Лучший музыкальный плеер с эквалайзером и онлайн-радио.",
            category = "Музыка",
            sizeMb = 56,
            rating = 4.6f,
            iconUrl = "",
            screenshotUrls = emptyList(),
            developerName = "AudioTech",
            version = "4.0.2",
            updatedAt = System.currentTimeMillis() - 18000000
        )
    )
    
    override fun getAllApps(): Flow<List<AppItem>> {
        return appDao.getAllApps().map { entities ->
            if (entities.isEmpty()) mockApps else entities.map { it.toAppItem() }
        }
    }
    
    override suspend fun getAppById(appId: String): AppItem? {
        return appDao.getAppById(appId)?.toAppItem() ?: mockApps.find { it.id == appId }
    }
    
    override fun searchApps(query: String): Flow<List<AppItem>> {
        return appDao.searchApps(query).map { entities ->
            if (entities.isEmpty()) {
                mockApps.filter { app ->
                    app.name.contains(query, ignoreCase = true) ||
                    app.description.contains(query, ignoreCase = true)
                }
            } else {
                entities.map { it.toAppItem() }
            }
        }
    }
    
    override suspend fun addApp(app: AppItem): Result<String> {
        return try {
            val newId = if (app.id.isNotEmpty()) app.id else UUID.randomUUID().toString()
            val newApp = app.copy(id = newId, updatedAt = System.currentTimeMillis())
            appDao.insertApp(AppEntity.fromAppItem(newApp))
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateApp(app: AppItem): Result<Unit> {
        return try {
            appDao.updateApp(AppEntity.fromAppItem(app))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteApp(appId: String): Result<Unit> {
        return try {
            appDao.deleteAppById(appId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncWithRemote(): Result<Unit> = Result.success(Unit)
    
    override suspend fun isRemoteAvailable(): Boolean = false
}
