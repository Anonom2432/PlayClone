package com.example.playclone.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playclone.domain.usecase.AddAppUseCase
import com.example.playclone.domain.usecase.GetAppsUseCase
import com.example.playclone.domain.usecase.SearchAppsUseCase
import com.example.playclone.presentation.viewmodel.AppsViewModel

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AppsViewModel::class.java) -> {
                AppsViewModel(
                    getAppsUseCase = container.getAppsUseCase,
                    searchAppsUseCase = container.searchAppsUseCase,
                    addAppUseCase = container.addAppUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Неизвестный ViewModel: ${modelClass.name}")
        }
    }
}
