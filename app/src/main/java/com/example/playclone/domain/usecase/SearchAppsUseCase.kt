package com.example.playclone.domain.usecase

import com.example.playclone.data.repository.AppRepository
import com.example.playclone.domain.model.AppItem
import kotlinx.coroutines.flow.Flow

class SearchAppsUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(query: String): Flow<List<AppItem>> {
        return repository.searchApps(query)
    }
}
