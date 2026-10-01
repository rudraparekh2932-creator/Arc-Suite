package com.example.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "arc_mail_messages")
data class MailMessageEntity(
    @PrimaryKey
    val id: String,
    val folder: String = "INBOX", // INBOX, SENT, DRAFTS, STARRED, ARCHIVE, TRASH
    val senderName: String,
    val senderEmail: String,
    val recipientEmail: String,
    val subject: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val hasAttachment: Boolean = false,
    val attachmentName: String? = null,
    val priority: String = "NORMAL", // HIGH, NORMAL, LOW
    val category: String = "Focused", // Focused, Other (Outlook inbox categories)
    val syncStatus: String = "SYNCED" // SYNCED, CACHED_OFFLINE, PENDING_SYNC
)

@Dao
interface MailDao {
    @Query("SELECT * FROM arc_mail_messages ORDER BY timestamp DESC")
    fun getAllMails(): Flow<List<MailMessageEntity>>

    @Query("SELECT * FROM arc_mail_messages WHERE folder = :folder ORDER BY timestamp DESC")
    fun getMailsByFolder(folder: String): Flow<List<MailMessageEntity>>

    @Query("SELECT * FROM arc_mail_messages WHERE isStarred = 1 AND folder != 'TRASH' ORDER BY timestamp DESC")
    fun getStarredMails(): Flow<List<MailMessageEntity>>

    @Query("SELECT * FROM arc_mail_messages WHERE id = :id")
    fun getMailByIdFlow(id: String): Flow<MailMessageEntity?>

    @Query("SELECT * FROM arc_mail_messages WHERE id = :id")
    suspend fun getMailById(id: String): MailMessageEntity?

    @Query("SELECT COUNT(*) FROM arc_mail_messages WHERE folder = 'INBOX' AND isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM arc_mail_messages WHERE folder = 'OUTBOX'")
    fun getOutboxCount(): Flow<Int>

    @Query("SELECT * FROM arc_mail_messages WHERE folder = 'OUTBOX' OR syncStatus = 'CACHED_OFFLINE'")
    suspend fun getPendingOutboxMails(): List<MailMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMail(mail: MailMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMails(mails: List<MailMessageEntity>)

    @Update
    suspend fun updateMail(mail: MailMessageEntity)

    @Delete
    suspend fun deleteMail(mail: MailMessageEntity)

    @Query("DELETE FROM arc_mail_messages WHERE id = :id")
    suspend fun deleteMailById(id: String)

    @Query("UPDATE arc_mail_messages SET isRead = :isRead WHERE id = :id")
    suspend fun markAsRead(id: String, isRead: Boolean)

    @Query("UPDATE arc_mail_messages SET isStarred = :isStarred WHERE id = :id")
    suspend fun setStarred(id: String, isStarred: Boolean)

    @Query("UPDATE arc_mail_messages SET folder = :folder WHERE id = :id")
    suspend fun moveToFolder(id: String, folder: String)

    @Query("UPDATE arc_mail_messages SET folder = :folder, syncStatus = :syncStatus, timestamp = :timestamp WHERE id = :id")
    suspend fun updateMailFolderAndSyncStatus(id: String, folder: String, syncStatus: String, timestamp: Long = System.currentTimeMillis())
}
