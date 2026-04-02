package com.example.playclone.domain.model

data class AppItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val sizeMb: Int,
    val rating: Float,
    val iconUrl: String,
    val screenshotUrls: List<String>,
    val developerName: String,
    val version: String,
    val updatedAt: Long,
    val isInstalled: Boolean = false
) {
    companion object {
        fun createEmpty(): AppItem {
            return AppItem(
                id = "",
                name = "",
                description = "",
                category = "",
                sizeMb = 0,
                rating = 0f,
                iconUrl = "",
                screenshotUrls = emptyList(),
                developerName = "",
                version = "1.0.0",
                updatedAt = 0L
            )
        }
    }
}
