package com.example.playclone.presentation.screens.addapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.playclone.R
import com.example.playclone.domain.model.AppItem
import com.example.playclone.presentation.viewmodel.AppsViewModel
import com.example.playclone.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppScreen(
    viewModel: AppsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var sizeMb by remember { mutableStateOf("") }
    var developerName by remember { mutableStateOf("") }
    var iconUri by remember { mutableStateOf<Uri?>(null) }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val saveErrorText = stringResource(id = R.string.save_error)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val iconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        iconUri = uri
    }
    
    val screenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        screenshotUri = uri
    }
    
    val isNameValid = name.length >= Constants.MIN_APP_NAME_LENGTH &&
                      name.length <= Constants.MAX_APP_NAME_LENGTH
    val isDescriptionValid = description.length >= Constants.MIN_DESCRIPTION_LENGTH &&
                             description.length <= Constants.MAX_DESCRIPTION_LENGTH
    val isCategoryValid = category.isNotBlank()
    val isSizeValid = sizeMb.toIntOrNull()?.let {
        it in Constants.MIN_APP_SIZE_MB..Constants.MAX_APP_SIZE_MB
    } ?: false
    val isDeveloperValid = developerName.isNotBlank()
    
    val isFormValid = isNameValid && isDescriptionValid && isCategoryValid &&
                      isSizeValid && isDeveloperValid
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.add_app),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_icon),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .clickable { iconLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(iconUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(id = R.string.app_icon),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.app_screenshot),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .height(280.dp)
                    .clickable { screenshotLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (screenshotUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(screenshotUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(id = R.string.app_screenshot),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.add_screenshot),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.app_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isNotEmpty() && !isNameValid,
                supportingText = {
                    if (name.isNotEmpty() && !isNameValid) {
                        Text(
                            text = stringResource(
                                id = R.string.name_length_error,
                                Constants.MIN_APP_NAME_LENGTH,
                                Constants.MAX_APP_NAME_LENGTH
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(id = R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = description.isNotEmpty() && !isDescriptionValid,
                supportingText = {
                    if (description.isNotEmpty() && !isDescriptionValid) {
                        Text(
                            text = stringResource(
                                id = R.string.description_length_error,
                                Constants.MIN_DESCRIPTION_LENGTH,
                                Constants.MAX_DESCRIPTION_LENGTH
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(id = R.string.category)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(id = R.string.category_placeholder)) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = sizeMb,
                onValueChange = { sizeMb = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(id = R.string.size_mb)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = sizeMb.isNotEmpty() && !isSizeValid,
                supportingText = {
                    if (sizeMb.isNotEmpty() && !isSizeValid) {
                        Text(
                            text = stringResource(
                                id = R.string.size_range_error,
                                Constants.MIN_APP_SIZE_MB,
                                Constants.MAX_APP_SIZE_MB
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                placeholder = { Text("100") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = developerName,
                onValueChange = { developerName = it },
                label = { Text(stringResource(id = R.string.developer)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = developerName.isNotEmpty() && !isDeveloperValid
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    
                    val app = AppItem(
                        id = "",
                        name = name.trim(),
                        description = description.trim(),
                        category = category.trim().ifEmpty { Constants.DEFAULT_CATEGORY },
                        sizeMb = sizeMb.toIntOrNull() ?: 0,
                        rating = Constants.DEFAULT_RATING,
                        iconUrl = iconUri?.toString() ?: "",
                        screenshotUrls = screenshotUri?.let { listOf(it.toString()) } ?: emptyList(),
                        developerName = developerName.trim(),
                        version = "1.0.0",
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    viewModel.addApp(app) { result ->
                        isLoading = false
                        result.onSuccess {
                            onNavigateBack()
                        }.onFailure { error ->
                            errorMessage = error.message ?: saveErrorText
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.save),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
