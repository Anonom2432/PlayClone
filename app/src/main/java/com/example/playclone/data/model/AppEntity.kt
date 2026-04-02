package com.example.playclone.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.playclone.util.Constants
import com.example.playclone.domain.model.AppItem

@Entity(tableName = Constants.APPS_TABLE)
data class AppEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "size_mb")
    val sizeMb: Int,
    
    @ColumnInfo(name = "rating")
    val rating: Float,
    
    @ColumnInfo(name = "icon_url")
    val iconUrl: String,
    
    @ColumnInfo(name = "screenshot_urls")
    val screenshotUrls: String,
    
    @ColumnInfo(name = "developer_name")
    val developerName: String,
    
    @ColumnInfo(name = "version")
    val version: String,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_installed")
    val isInstalled: Boolean = false
) {
    companion object {
        fun fromAppItem(appItem: AppItem): AppEntity {
            return AppEntity(
                id = appItem.id,
                name = appItem.name,
                description = appItem.description,
                category = appItem.category,
                sizeMb = appItem.sizeMb,
                rating = appItem.rating,
                iconUrl = appItem.iconUrl,
                screenshotUrls = appItem.screenshotUrls.joinToString("|"),
                developerName = appItem.developerName,
                version = appItem.version,
                updatedAt = appItem.updatedAt,
                isInstalled = appItem.isInstalled
            )
        }
    }
    
    fun toAppItem(): AppItem {
        return AppItem(
            id = this.id,
            name = this.name,
            description = this.description,
            category = this.category,
            sizeMb = this.sizeMb,
            rating = this.rating,
            iconUrl = this.iconUrl,
            screenshotUrls = this.screenshotUrls.split("|").filter { it.isNotEmpty() },
            developerName = this.developerName,
            version = this.version,
            updatedAt = this.updatedAt,
            isInstalled = this.isInstalled
        )
    }
}
