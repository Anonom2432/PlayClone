package com.example.playclone.domain.usecase

import com.example.playclone.data.repository.AppRepository
import com.example.playclone.domain.model.AppItem
import kotlinx.coroutines.flow.Flow

class GetAppsUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<AppItem>> {
        return repository.getAllApps()
    }
}
