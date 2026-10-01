package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE typeName = :typeName ORDER BY updatedAt DESC")
    fun getDocumentsByType(typeName: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isStarred = 1 ORDER BY updatedAt DESC")
    fun getStarredDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentByIdFlow(id: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("UPDATE documents SET isStarred = :isStarred WHERE id = :id")
    suspend fun updateStarred(id: String, isStarred: Boolean)

    @Query("UPDATE documents SET syncStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM documents WHERE syncStatus = 'CACHED_OFFLINE' OR syncStatus = 'PENDING_SYNC'")
    fun getOfflineCachedCount(): Flow<Int>

    @Query("SELECT * FROM documents WHERE syncStatus = 'CACHED_OFFLINE' OR syncStatus = 'PENDING_SYNC'")
    suspend fun getOfflineCachedDocs(): List<DocumentEntity>

    @Query("UPDATE documents SET syncStatus = 'SYNCED', isCloudSynced = 1, updatedAt = :timestamp WHERE syncStatus != 'SYNCED'")
    suspend fun markAllDocsSynced(timestamp: Long = System.currentTimeMillis())
}
