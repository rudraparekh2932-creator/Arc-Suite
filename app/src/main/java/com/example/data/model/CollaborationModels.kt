package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.CollabColors
import java.util.Locale

data class Collaborator(
    val id: String,
    val name: String,
    val email: String,
    val role: String, // Owner, Editor, Reviewer, Viewer
    val colorIndex: Int = 0,
    val isOnline: Boolean = true,
    val currentPosition: String = "", // e.g. "Section 2", "Cell B4", "Slide 3"
    val avatarInitial: String = name.take(1).uppercase()
) {
    val color: Color
        get() = CollabColors[colorIndex % CollabColors.size]
}

@Entity(tableName = "document_comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val documentId: String,
    val authorName: String,
    val authorColorIndex: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false,
    val targetAnchor: String = "General"
)

data class ActivityLogItem(
    val id: String,
    val authorName: String,
    val actionText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val iconName: String = "edit"
)

data class StorageUsageBreakdown(
    val totalMb: Float,
    val mailMb: Float,
    val docsMb: Float,
    val sheetsMb: Float,
    val slidesMb: Float,
    val pdfsMb: Float,
    val creativeMb: Float,
    val systemMb: Float
)

data class CloudSyncInfo(
    val isOnline: Boolean = true,
    val syncStatusText: String = "All changes synced to Arc Cloud",
    val lastSyncTime: Long = System.currentTimeMillis(),
    val pendingMutations: Int = 0,
    val storageUsedMb: Float = 0f,
    val storageQuotaGb: Int = 500,
    val storageQuotaTb: Float = 0.5f,
    val totalStorageMb: Float = 500f * 1024f,
    val storageDisplayLimit: String = "500 GB",
    val autoSyncEnabled: Boolean = true,
    val wifiOnly: Boolean = false,
    val conflictResolution: String = "Merge (Smart Auto-Resolve)",
    val mailStorageMb: Float = 0f,
    val docsStorageMb: Float = 0f,
    val sheetsStorageMb: Float = 0f,
    val slidesStorageMb: Float = 0f,
    val pdfsStorageMb: Float = 0f,
    val creativeStorageMb: Float = 0f,
    val systemCacheMb: Float = 1.2f
) {
    val usedPercentage: Float
        get() = if (totalStorageMb > 0f) (storageUsedMb / totalStorageMb) * 100f else 0f

    val formattedUsed: String
        get() = when {
            storageUsedMb <= 0.001f -> "0.0 MB"
            storageUsedMb < 0.1f -> "${String.format(Locale.US, "%.0f", storageUsedMb * 1024)} KB"
            storageUsedMb < 1000f -> "${String.format(Locale.US, "%.1f", storageUsedMb)} MB"
            else -> "${String.format(Locale.US, "%.2f", storageUsedMb / 1024f)} GB"
        }

    val formattedAvailable: String
        get() {
            val freeMb = (totalStorageMb - storageUsedMb).coerceAtLeast(0f)
            val freeGb = freeMb / 1024f
            return when {
                freeGb >= 1000f -> "${String.format(Locale.US, "%.2f", freeGb / 1024f)} TB"
                freeGb >= 1f -> "${String.format(Locale.US, "%.1f", freeGb)} GB"
                else -> "${String.format(Locale.US, "%.1f", freeMb)} MB"
            }
        }

    val formattedPercentage: String
        get() = when {
            usedPercentage <= 0.0001f -> "< 0.01%"
            usedPercentage < 0.01f -> "< 0.01%"
            usedPercentage < 1f -> "${String.format(Locale.US, "%.2f", usedPercentage)}%"
            else -> "${String.format(Locale.US, "%.1f", usedPercentage)}%"
        }
}
