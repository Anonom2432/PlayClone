package com.example.playclone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playclone.domain.model.AppItem
import com.example.playclone.domain.usecase.AddAppUseCase
import com.example.playclone.domain.usecase.GetAppsUseCase
import com.example.playclone.domain.usecase.SearchAppsUseCase
import com.example.playclone.util.Constants
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppsUiState(
    val apps: List<AppItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedApp: AppItem? = null
)

@OptIn(FlowPreview::class)
class AppsViewModel(
    private val getAppsUseCase: GetAppsUseCase,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val addAppUseCase: AddAppUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()
    
    private var isInitialLoad = true
    
    init {
        loadApps()
    }
    
    private fun loadApps() {
        viewModelScope.launch {
            getAppsUseCase()
                .onEach { apps ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            apps = apps,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                .catch { error ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            error = error.message ?: "Неизвестная ошибка",
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                .launchIn(this)
        }
    }
    
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadApps()
    }
    
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            loadApps()
            return
        }
        
        viewModelScope.launch {
            searchAppsUseCase(query)
                .debounce(Constants.SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .onEach { apps ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            apps = apps,
                            isLoading = false
                        )
                    }
                }
                .catch { error ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            error = error.message ?: "Ошибка поиска"
                        )
                    }
                }
                .launchIn(this)
        }
    }
    
    fun setSearchActive(isActive: Boolean) {
        _uiState.update { 
            it.copy(
                isSearchActive = isActive,
                searchQuery = if (!isActive) "" else it.searchQuery
            ) 
        }
        if (!isActive) {
            loadApps()
        }
    }
    
    fun addApp(app: AppItem, onComplete: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = addAppUseCase(app)
            onComplete(result)
        }
    }
    
    fun selectApp(app: AppItem?) {
        _uiState.update { it.copy(selectedApp = app) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun markAsInstalled(appId: String) {
        viewModelScope.launch {
            val currentApps = _uiState.value.apps
            val updatedApps = currentApps.map { app ->
                if (app.id == appId) app.copy(isInstalled = true) else app
            }
            _uiState.update { it.copy(apps = updatedApps) }
        }
    }
}
