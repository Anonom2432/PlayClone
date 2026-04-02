package com.example.playclone.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.playclone.data.local.AppDao
import com.example.playclone.data.model.AppEntity
import com.example.playclone.data.repository.AppRepository
import com.example.playclone.domain.model.AppItem
import com.example.playclone.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseAppRepository(
    private val appDao: AppDao,
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AppRepository {
    
    private var appsListener: ListenerRegistration? = null
    private val TAG = "FirebaseAppRepository"
    
    init {
        signInAnonymously()
    }
    
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Анонимный вход выполнен, userId: ${auth.currentUser?.uid}")
                    setupRealtimeSync()
                } else {
                    Log.e(TAG, "Ошибка анонимного входа: ${task.exception?.message}")
                }
            }
    }
    
    private fun setupRealtimeSync() {
        appsListener = firestore.collection(Constants.APPS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Ошибка прослушивания Firestore: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.documentChanges?.forEach { change ->
                    val appData = change.document.data
                    val appId = change.document.id
                    
                    if (appData != null) {
                        val appEntity = AppEntity(
                            id = appId,
                            name = appData["name"] as? String ?: "",
                            description = appData["description"] as? String ?: "",
                            category = appData["category"] as? String ?: Constants.DEFAULT_CATEGORY,
                            sizeMb = (appData["sizeMb"] as? Long)?.toInt() ?: 0,
                            rating = (appData["rating"] as? Double)?.toFloat() ?: Constants.DEFAULT_RATING,
                            iconUrl = appData["iconUrl"] as? String ?: "",
                            screenshotUrls = (appData["screenshotUrls"] as? List<*>)?.joinToString("|") { it.toString() } ?: "",
                            developerName = appData["developerName"] as? String ?: "",
                            version = appData["version"] as? String ?: "1.0.0",
                            updatedAt = (appData["updatedAt"] as? Long) ?: System.currentTimeMillis(),
                            isInstalled = false
                        )
                        
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    appDao.insertApp(appEntity)
                                }
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    appDao.deleteAppById(appId)
                                }
                            }
                        }
                    }
                }
            }
        
        Log.d(TAG, "Синхронизация в реальном времени настроена")
    }
    
    override fun getAllApps(): Flow<List<AppItem>> = callbackFlow {
        val listener = firestore.collection(Constants.APPS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Ошибка получения приложений: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val apps = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { data ->
                        AppItem(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            category = data["category"] as? String ?: Constants.DEFAULT_CATEGORY,
                            sizeMb = (data["sizeMb"] as? Long)?.toInt() ?: 0,
                            rating = (data["rating"] as? Double)?.toFloat() ?: Constants.DEFAULT_RATING,
                            iconUrl = data["iconUrl"] as? String ?: "",
                            screenshotUrls = (data["screenshotUrls"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                            developerName = data["developerName"] as? String ?: "",
                            version = data["version"] as? String ?: "1.0.0",
                            updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                } ?: emptyList()
                
                trySend(apps)
            }
        
        awaitClose { listener?.remove() }
    }
    
    override suspend fun getAppById(appId: String): AppItem? {
        return try {
            val doc = firestore.collection(Constants.APPS_COLLECTION)
                .document(appId)
                .get()
                .await()
            
            doc.data?.let { data ->
                AppItem(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    category = data["category"] as? String ?: Constants.DEFAULT_CATEGORY,
                    sizeMb = (data["sizeMb"] as? Long)?.toInt() ?: 0,
                    rating = (data["rating"] as? Double)?.toFloat() ?: Constants.DEFAULT_RATING,
                    iconUrl = data["iconUrl"] as? String ?: "",
                    screenshotUrls = (data["screenshotUrls"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    developerName = data["developerName"] as? String ?: "",
                    version = data["version"] as? String ?: "1.0.0",
                    updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения приложения: ${e.message}")
            null
        }
    }
    
    override fun searchApps(query: String): Flow<List<AppItem>> = callbackFlow {
        val listener = firestore.collection(Constants.APPS_COLLECTION)
            .orderBy("name")
            .startAt(query.lowercase())
            .endAt(query.lowercase() + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Ошибка поиска: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val apps = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { data ->
                        AppItem(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            category = data["category"] as? String ?: Constants.DEFAULT_CATEGORY,
                            sizeMb = (data["sizeMb"] as? Long)?.toInt() ?: 0,
                            rating = (data["rating"] as? Double)?.toFloat() ?: Constants.DEFAULT_RATING,
                            iconUrl = data["iconUrl"] as? String ?: "",
                            screenshotUrls = (data["screenshotUrls"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                            developerName = data["developerName"] as? String ?: "",
                            version = data["version"] as? String ?: "1.0.0",
                            updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                } ?: emptyList()
                
                trySend(apps)
            }
        
        awaitClose { listener?.remove() }
    }
    
    override suspend fun addApp(app: AppItem): Result<String> {
        return try {
            val appId = UUID.randomUUID().toString()
            
            val iconUrl = if (app.iconUrl.isNotEmpty() && app.iconUrl.startsWith("content://")) {
                uploadImageToStorage(app.iconUrl, "${Constants.ICONS_STORAGE_PATH}/$appId")
            } else {
                app.iconUrl
            }
            
            val screenshotUrls = app.screenshotUrls.mapNotNull { url ->
                if (url.isNotEmpty() && url.startsWith("content://")) {
                    uploadImageToStorage(url, "${Constants.SCREENSHOTS_STORAGE_PATH}/$appId/${UUID.randomUUID()}")
                } else {
                    url
                }
            }
            
            val appData = mapOf(
                "name" to app.name,
                "description" to app.description,
                "category" to app.category,
                "sizeMb" to app.sizeMb,
                "rating" to app.rating,
                "iconUrl" to iconUrl,
                "screenshotUrls" to screenshotUrls,
                "developerName" to app.developerName,
                "version" to app.version,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(Constants.APPS_COLLECTION)
                .document(appId)
                .set(appData)
                .await()
            
            val appEntity = AppEntity.fromAppItem(app.copy(id = appId, iconUrl = iconUrl, screenshotUrls = screenshotUrls))
            appDao.insertApp(appEntity)
            
            Log.d(TAG, "Приложение добавлено: ${app.name}")
            Result.success(appId)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка добавления приложения: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun updateApp(app: AppItem): Result<Unit> {
        return try {
            val appData = mapOf(
                "name" to app.name,
                "description" to app.description,
                "category" to app.category,
                "sizeMb" to app.sizeMb,
                "rating" to app.rating,
                "iconUrl" to app.iconUrl,
                "screenshotUrls" to app.screenshotUrls,
                "developerName" to app.developerName,
                "version" to app.version,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(Constants.APPS_COLLECTION)
                .document(app.id)
                .update(appData)
                .await()
            
            appDao.updateApp(AppEntity.fromAppItem(app))
            
            Log.d(TAG, "Приложение обновлено: ${app.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка обновления приложения: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteApp(appId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.APPS_COLLECTION)
                .document(appId)
                .delete()
                .await()
            
            appDao.deleteAppById(appId)
            
            Log.d(TAG, "Приложение удалено: $appId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления приложения: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun syncWithRemote(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(Constants.APPS_COLLECTION).get().await()
            
            val apps = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    AppEntity(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        category = data["category"] as? String ?: Constants.DEFAULT_CATEGORY,
                        sizeMb = (data["sizeMb"] as? Long)?.toInt() ?: 0,
                        rating = (data["rating"] as? Double)?.toFloat() ?: Constants.DEFAULT_RATING,
                        iconUrl = data["iconUrl"] as? String ?: "",
                        screenshotUrls = (data["screenshotUrls"] as? List<*>)?.joinToString("|") { it.toString() } ?: "",
                        developerName = data["developerName"] as? String ?: "",
                        version = data["version"] as? String ?: "1.0.0",
                        updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis(),
                        isInstalled = false
                    )
                }
            }
            
            appDao.insertApps(apps)
            
            Log.d(TAG, "Синхронизация завершена: ${apps.size} приложений")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка синхронизации: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun isRemoteAvailable(): Boolean {
        return try {
            firestore.collection(Constants.APPS_COLLECTION).limit(1).get().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase недоступен: ${e.message}")
            false
        }
    }
    
    private suspend fun uploadImageToStorage(contentUri: String, path: String): String {
        return try {
            val ref = storage.reference.child(path)
            val uri = Uri.parse(contentUri)

            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть InputStream для $contentUri")

            ref.putStream(inputStream).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "Изображение загружено: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки изображения: ${e.message}")
            ""
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: FirebaseAppRepository? = null
        
        fun getInstance(appDao: AppDao, context: Context): FirebaseAppRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseAppRepository(appDao, context)
                INSTANCE = instance
                instance
            }
        }
    }
}
