package com.example.playclone.domain.usecase

import com.example.playclone.data.repository.AppRepository
import com.example.playclone.domain.model.AppItem

class AddAppUseCase(
    private val repository: AppRepository
) {
    suspend operator fun invoke(app: AppItem): Result<String> {
        return repository.addApp(app)
    }
}
