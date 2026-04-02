package com.example.playclone

import com.example.playclone.domain.model.AppItem
import com.example.playclone.domain.usecase.AddAppUseCase
import com.example.playclone.domain.usecase.GetAppsUseCase
import com.example.playclone.domain.usecase.SearchAppsUseCase
import com.example.playclone.presentation.viewmodel.AppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be loading`() = runTest {
        val mockApps = emptyList<AppItem>()
        val viewModel = createViewModelWithApps(mockApps)
        
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.apps.isEmpty())
    }
    
    @Test
    fun `state should update when apps are loaded`() = runTest {
        val mockApps = listOf(
            createMockApp("1", "App One"),
            createMockApp("2", "App Two")
        )
        val viewModel = createViewModelWithApps(mockApps)
        
        advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.apps.size)
    }
    
    @Test
    fun `search query should filter apps`() = runTest {
        val mockApps = listOf(
            createMockApp("1", "Messenger"),
            createMockApp("2", "Music Player"),
            createMockApp("3", "Photo Editor")
        )
        val viewModel = createViewModelWithApps(mockApps)
        
        advanceUntilIdle()
        
        viewModel.onSearchQueryChange("Music")
        advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.value.apps.size)
        assertEquals("Music Player", viewModel.uiState.value.apps.first().name)
    }
    
    @Test
    fun `empty search query should show all apps`() = runTest {
        val mockApps = listOf(
            createMockApp("1", "Messenger"),
            createMockApp("2", "Music Player")
        )
        val viewModel = createViewModelWithApps(mockApps)
        
        advanceUntilIdle()
        
        viewModel.onSearchQueryChange("Music")
        advanceUntilIdle()
        
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()
        
        assertEquals(2, viewModel.uiState.value.apps.size)
    }
    
    @Test
    fun `markAsInstalled should update app state`() = runTest {
        val mockApps = listOf(createMockApp("1", "Test App"))
        val viewModel = createViewModelWithApps(mockApps)
        
        advanceUntilIdle()
        
        viewModel.markAsInstalled("1")
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.apps.first().isInstalled)
    }
    
    @Test
    fun `clearError should remove error message`() = runTest {
        val viewModel = createViewModelWithApps(emptyList())
        
        advanceUntilIdle()
        
        viewModel.clearError()
        advanceUntilIdle()
        
        assertEquals(null, viewModel.uiState.value.error)
    }
    
    private fun createViewModelWithApps(apps: List<AppItem>): AppsViewModel {
        val mockRepository = object : com.example.playclone.data.repository.AppRepository {
            override fun getAllApps() = flowOf(apps)
            override suspend fun getAppById(appId: String) = apps.find { it.id == appId }
            override fun searchApps(query: String) = flowOf(
                apps.filter { it.name.contains(query, ignoreCase = true) }
            )
            override suspend fun addApp(app: AppItem) = Result.success(app.id)
            override suspend fun updateApp(app: AppItem) = Result.success(Unit)
            override suspend fun deleteApp(appId: String) = Result.success(Unit)
            override suspend fun syncWithRemote() = Result.success(Unit)
            override suspend fun isRemoteAvailable() = true
        }
        
        return AppsViewModel(
            getAppsUseCase = GetAppsUseCase(mockRepository),
            searchAppsUseCase = SearchAppsUseCase(mockRepository),
            addAppUseCase = AddAppUseCase(mockRepository)
        )
    }
    
    private fun createMockApp(id: String, name: String): AppItem {
        return AppItem(
            id = id,
            name = name,
            description = "Description for $name",
            category = "Category",
            sizeMb = 100,
            rating = 4.5f,
            iconUrl = "",
            screenshotUrls = emptyList(),
            developerName = "Developer",
            version = "1.0.0",
            updatedAt = System.currentTimeMillis()
        )
    }
}
