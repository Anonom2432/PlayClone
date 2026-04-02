package com.example.playclone.di

import android.content.Context
import com.example.playclone.data.local.AppDao
import com.example.playclone.data.local.AppDatabase
import com.example.playclone.data.remote.FirebaseAppRepository
import com.example.playclone.data.repository.AppRepository
import com.example.playclone.data.repository.LocalMockRepository
import com.example.playclone.data.repository.OfflineFirstAppRepository
import com.example.playclone.domain.usecase.AddAppUseCase
import com.example.playclone.domain.usecase.GetAppsUseCase
import com.example.playclone.domain.usecase.SearchAppsUseCase
import java.io.File

class AppContainer(private val context: Context) {
    
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
    
    private val appDao: AppDao by lazy {
        database.appDao()
    }
    
    private fun hasFirebaseConfig(): Boolean {
        val googleServicesFile = File(context.applicationInfo.dataDir, "google-services.json")
        return googleServicesFile.exists() || 
               File(context.filesDir.parent, "google-services.json").exists() ||
               context.resources.getIdentifier("google_app_id", "string", context.packageName) != 0
    }
    
    private val repository: AppRepository by lazy {
        val hasFirebase = hasFirebaseConfig()
        
        if (hasFirebase) {
            try {
                val firebaseRepo = FirebaseAppRepository.getInstance(appDao)
                OfflineFirstAppRepository(appDao, firebaseRepo)
            } catch (e: Exception) {
                LocalMockRepository(appDao)
            }
        } else {
            LocalMockRepository(appDao)
        }
    }
    
    val getAppsUseCase: GetAppsUseCase by lazy {
        GetAppsUseCase(repository)
    }
    
    val searchAppsUseCase: SearchAppsUseCase by lazy {
        SearchAppsUseCase(repository)
    }
    
    val addAppUseCase: AddAppUseCase by lazy {
        AddAppUseCase(repository)
    }
}
