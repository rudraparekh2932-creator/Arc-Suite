package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val typeName: String, // DOC, SHEET, SLIDE, PDF, CANVAS
    val content: String,  // JSON or text representing document content
    val authorName: String = "Rudra Parekh",
    val authorEmail: String = "rudraparekh2932@gmail.com",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false,
    val isCloudSynced: Boolean = true,
    val syncStatus: String = "SYNCED", // SYNCED, PENDING, SYNCING, OFFLINE
    val versionNumber: Int = 1,
    val fileSizeBytes: Long = 1024L,
    val collaboratorsJson: String = "" // serializable list of active collaborators
) {
    val type: DocType
        get() = try {
            DocType.valueOf(typeName)
        } catch (_: Exception) {
            DocType.DOC
        }

    val previewSnippet: String
        get() = content.take(120).replace("\n", " ").ifBlank { "Empty ${type.displayName}" }

    val lastModified: Long
        get() = updatedAt
}
