package com.example.data.repository

import android.content.Context
import com.example.data.db.ArcDatabase
import com.example.data.model.ActivityLogItem
import com.example.data.model.CloudSyncInfo
import com.example.data.model.Collaborator
import com.example.data.model.CommentEntity
import com.example.data.model.DocType
import com.example.data.model.DocumentEntity
import com.example.data.model.LicenseState
import com.example.data.model.MailMessageEntity
import com.example.data.model.ProductKeyEntity
import com.example.data.model.StorageUsageBreakdown
import com.example.data.model.getTierSpecForTierString
import com.example.data.security.EncryptedStorageManager
import com.example.data.security.VerificationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ArcRepository(
    private val database: ArcDatabase,
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val docDao = database.documentDao()
    private val keyDao = database.productKeyDao()
    private val commentDao = database.commentDao()
    private val mailDao = database.mailDao()
    private val encryptedStorage = EncryptedStorageManager(context)

    // Hardware ID & Security Service
    val hardwareIdService = com.example.data.security.DeviceHardwareIdService.getInstance(context)
    val deviceHardwareId: String = hardwareIdService.getHardwareId()
    val deviceHardwareDetails: com.example.data.security.DeviceHardwareDetails = hardwareIdService.getHardwareDetails()

    // Current license verification state
    val isLicenseVerified = MutableStateFlow(false)

    // Active Document being edited
    private val _activeDocument = MutableStateFlow<DocumentEntity?>(null)
    val activeDocument = _activeDocument.asStateFlow()

    // Cloud Sync State
    private val _cloudSyncState = MutableStateFlow(CloudSyncInfo())
    val cloudSyncState = _cloudSyncState.asStateFlow()

    // Real-Time Collaborators Presence
    private val _collaborators = MutableStateFlow<List<Collaborator>>(emptyList())
    val collaborators = _collaborators.asStateFlow()

    // Activity Log
    private val _activityLogs = MutableStateFlow<List<ActivityLogItem>>(emptyList())
    val activityLogs = _activityLogs.asStateFlow()

    // Offline & Sync Tracking
    val offlineDocsCount: Flow<Int> = docDao.getOfflineCachedCount()
    val outboxMailsCount: Flow<Int> = mailDao.getOutboxCount()

    // License State
    private val _licenseState = MutableStateFlow(LicenseState(isLicensed = false))
    val licenseState = _licenseState.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            val count = keyDao.getKeyCount()
            if (count == 0) {
                com.example.data.db.populateInitialData(database, context)
            }
            val keys = keyDao.getAllKeys().first()
            if (keys.isNotEmpty()) {
                encryptedStorage.saveKeysToEncryptedVault(keys)
            }
            val verified = encryptedStorage.getVerifiedLicense()
            if (verified != null && verified.isVerified) {
                isLicenseVerified.value = true
                val tierSpec = getTierSpecForTierString(verified.tier)
                _licenseState.value = LicenseState(
                    isLicensed = true,
                    activeKey = verified.activeKey,
                    tier = verified.tier,
                    ownerName = verified.ownerEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                    ownerEmail = verified.ownerEmail,
                    activationDate = verified.verifiedAt,
                    storageQuotaGb = tierSpec.storageGb
                )
                updateCloudSyncStorageQuota(verified.tier)
            } else {
                isLicenseVerified.value = false
                _licenseState.value = LicenseState(isLicensed = false)
            }

            initSampleCollaborators()
        }

        // Real-Time Dynamic Cloud Storage Calculation
        scope.launch(Dispatchers.IO) {
            combine(
                docDao.getAllDocuments(),
                mailDao.getAllMails()
            ) { docs, mails ->
                calculateStorageBreakdown(docs, mails)
            }.collect { breakdown ->
                _cloudSyncState.value = _cloudSyncState.value.copy(
                    storageUsedMb = breakdown.totalMb,
                    mailStorageMb = breakdown.mailMb,
                    docsStorageMb = breakdown.docsMb,
                    sheetsStorageMb = breakdown.sheetsMb,
                    slidesStorageMb = breakdown.slidesMb,
                    pdfsStorageMb = breakdown.pdfsMb,
                    creativeStorageMb = breakdown.creativeMb,
                    systemCacheMb = breakdown.systemMb
                )
            }
        }
    }

    private fun calculateStorageBreakdown(
        docs: List<DocumentEntity>,
        mails: List<MailMessageEntity>
    ): StorageUsageBreakdown {
        var mailBytes = 0L
        for (mail in mails) {
            mailBytes += 4096L // Email headers & recipient index
            mailBytes += mail.subject.toByteArray(Charsets.UTF_8).size
            mailBytes += mail.body.toByteArray(Charsets.UTF_8).size
            if (mail.hasAttachment) {
                mailBytes += (1.4f * 1024f * 1024f).toLong() // 1.4 MB realistic attachment payload
            }
        }

        var docsBytes = 0L
        var sheetsBytes = 0L
        var slidesBytes = 0L
        var pdfsBytes = 0L
        var creativeBytes = 0L

        for (doc in docs) {
            val contentBytes = doc.content.toByteArray(Charsets.UTF_8).size.toLong()
            val docType = try { DocType.valueOf(doc.typeName) } catch (_: Exception) { DocType.DOC }
            when (docType) {
                DocType.DOC -> docsBytes += (32 * 1024L) + contentBytes
                DocType.SHEET -> sheetsBytes += (48 * 1024L) + (contentBytes * 2L)
                DocType.SLIDE -> slidesBytes += (96 * 1024L) + (contentBytes * 3L)
                DocType.PDF -> pdfsBytes += (128 * 1024L) + (contentBytes * 2L)
                DocType.PHOTOSHOP, DocType.PREMIERE, DocType.INDESIGN, DocType.AFTER_EFFECTS, DocType.CANVAS -> {
                    creativeBytes += (160 * 1024L) + (contentBytes * 2L)
                }
            }
        }

        val systemCacheBytes = (1.5f * 1024f * 1024f).toLong() // Base cloud identity, security keys, sync journals

        val mailMb = mailBytes / (1024f * 1024f)
        val docsMb = docsBytes / (1024f * 1024f)
        val sheetsMb = sheetsBytes / (1024f * 1024f)
        val slidesMb = slidesBytes / (1024f * 1024f)
        val pdfsMb = pdfsBytes / (1024f * 1024f)
        val creativeMb = creativeBytes / (1024f * 1024f)
        val systemMb = systemCacheBytes / (1024f * 1024f)

        val totalMb = mailMb + docsMb + sheetsMb + slidesMb + pdfsMb + creativeMb + systemMb

        return StorageUsageBreakdown(
            totalMb = totalMb,
            mailMb = mailMb,
            docsMb = docsMb,
            sheetsMb = sheetsMb,
            slidesMb = slidesMb,
            pdfsMb = pdfsMb,
            creativeMb = creativeMb,
            systemMb = systemMb
        )
    }

    private fun initSampleCollaborators() {
        _collaborators.value = listOf(
            Collaborator(id = "c1", name = "Alice Lin", email = "alice.lin@enterprise.arc", role = "Editor", colorIndex = 0),
            Collaborator(id = "c2", name = "Carlos Silva", email = "carlos.s@enterprise.arc", role = "Reviewer", colorIndex = 1),
            Collaborator(id = "c3", name = "Maya Patel", email = "maya.patel@creative.arc", role = "Editor", colorIndex = 2),
            Collaborator(id = "c4", name = "David Chen", email = "david.c@finance.arc", role = "Viewer", colorIndex = 3)
        )
    }

    private fun updateCloudSyncStorageQuota(tier: String) {
        val tierSpec = getTierSpecForTierString(tier)
        _cloudSyncState.value = _cloudSyncState.value.copy(
            storageQuotaGb = tierSpec.storageGb,
            storageQuotaTb = tierSpec.storageTb,
            totalStorageMb = tierSpec.storageGb * 1024f,
            storageDisplayLimit = tierSpec.storageDisplay
        )
    }

    // Documents (Starts clean on new install, no sample documents)
    fun getAllDocuments(): Flow<List<DocumentEntity>> = docDao.getAllDocuments()
    fun getDocumentsByType(type: DocType): Flow<List<DocumentEntity>> = docDao.getDocumentsByType(type.name)
    fun getStarredDocuments(): Flow<List<DocumentEntity>> = docDao.getStarredDocuments()
    fun getDocumentByIdFlow(id: String): Flow<DocumentEntity?> = docDao.getDocumentByIdFlow(id)

    suspend fun getDocumentById(id: String): DocumentEntity? = withContext(Dispatchers.IO) {
        docDao.getDocumentById(id)
    }

    suspend fun createDocument(
        type: DocType,
        title: String = "Untitled ${type.displayName}",
        initialContent: String = ""
    ): DocumentEntity = withContext(Dispatchers.IO) {
        val isOnline = _cloudSyncState.value.isOnline
        val newDoc = DocumentEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            typeName = type.name,
            content = initialContent,
            authorName = _licenseState.value.ownerName.ifBlank { "Arc 360 User" },
            authorEmail = _licenseState.value.ownerEmail.ifBlank { "user@arc360.cloud" },
            syncStatus = if (isOnline) "SYNCED" else "CACHED_OFFLINE",
            isCloudSynced = isOnline,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        docDao.insertDocument(newDoc)
        _activeDocument.value = newDoc
        if (!isOnline) {
            _cloudSyncState.value = _cloudSyncState.value.copy(
                pendingMutations = _cloudSyncState.value.pendingMutations + 1,
                syncStatusText = "Working Offline • Document cached locally in Room"
            )
            addActivityLog("Room Cache (Offline)", "Created ${type.displayName} in Room offline cache: $title")
        } else {
            addActivityLog("Document Engine", "Created new ${type.displayName}: $title")
        }
        newDoc
    }

    suspend fun updateDocumentContent(
        id: String,
        content: String,
        newTitle: String? = null,
        deltaBytes: Long = 0L
    ) = withContext(Dispatchers.IO) {
        val current = docDao.getDocumentById(id) ?: return@withContext
        val isOnline = _cloudSyncState.value.isOnline
        val updated = current.copy(
            content = content,
            title = newTitle ?: current.title,
            updatedAt = System.currentTimeMillis(),
            versionNumber = current.versionNumber + 1,
            syncStatus = if (isOnline) "SYNCED" else "CACHED_OFFLINE",
            isCloudSynced = isOnline,
            fileSizeBytes = if (deltaBytes > 0) deltaBytes else content.toByteArray().size.toLong()
        )
        docDao.updateDocument(updated)
        _activeDocument.value = updated

        if (!isOnline) {
            _cloudSyncState.value = _cloudSyncState.value.copy(
                pendingMutations = _cloudSyncState.value.pendingMutations + 1,
                syncStatusText = "Working Offline • Saved to Room local cache"
            )
        }
    }

    suspend fun toggleDocumentStarred(doc: DocumentEntity) = withContext(Dispatchers.IO) {
        val newStarred = !doc.isStarred
        docDao.updateStarred(doc.id, newStarred)
        if (_activeDocument.value?.id == doc.id) {
            _activeDocument.value = _activeDocument.value?.copy(isStarred = newStarred)
        }
    }

    suspend fun deleteDocument(doc: DocumentEntity) = withContext(Dispatchers.IO) {
        docDao.deleteDocument(doc)
        if (_activeDocument.value?.id == doc.id) {
            _activeDocument.value = null
        }
        addActivityLog("Document Engine", "Deleted document: ${doc.title}")
    }

    fun setActiveDocument(doc: DocumentEntity?) {
        _activeDocument.value = doc
    }

    // Comments
    fun getCommentsForDocument(docId: String): Flow<List<CommentEntity>> = commentDao.getCommentsForDocument(docId)

    suspend fun addComment(docId: String, text: String, anchor: String = "General") = withContext(Dispatchers.IO) {
        val userEmail = _licenseState.value.ownerEmail.ifBlank { "User" }
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            documentId = docId,
            authorName = userEmail.substringBefore("@"),
            authorColorIndex = 0,
            text = text,
            targetAnchor = anchor
        )
        commentDao.insertComment(comment)
        addActivityLog("Collaboration", "Comment added on $anchor")
    }

    suspend fun toggleCommentResolved(comment: CommentEntity) = withContext(Dispatchers.IO) {
        commentDao.setResolved(comment.id, !comment.isResolved)
    }

    // Cloud Synchronization Actions
    suspend fun triggerCloudSync() = withContext(Dispatchers.IO) {
        if (!_cloudSyncState.value.isOnline) return@withContext
        _cloudSyncState.value = _cloudSyncState.value.copy(
            syncStatusText = "Synchronizing Room local cache with Arc 360 Cloud..."
        )
        delay(600)
        // 1. Sync all pending offline cached documents
        docDao.markAllDocsSynced(System.currentTimeMillis())

        // 2. Dispatch all pending Outbox emails
        val pendingOutbox = mailDao.getPendingOutboxMails()
        for (m in pendingOutbox) {
            mailDao.updateMailFolderAndSyncStatus(
                id = m.id,
                folder = "SENT",
                syncStatus = "SYNCED",
                timestamp = System.currentTimeMillis()
            )
        }

        val outboxCount = pendingOutbox.size
        _cloudSyncState.value = _cloudSyncState.value.copy(
            syncStatusText = if (outboxCount > 0) "All documents synced & $outboxCount offline emails sent" else "All local Room cache synced to Arc 360 Cloud",
            lastSyncTime = System.currentTimeMillis(),
            pendingMutations = 0,
            storageUsedMb = (_cloudSyncState.value.storageUsedMb + 0.1f).coerceAtMost(100000f)
        )
        addActivityLog("Room Sync Engine", "Cloud sync completed: $outboxCount outbox emails sent, all documents synced")
    }

    fun toggleOnlineMode() {
        val newOnline = !_cloudSyncState.value.isOnline
        _cloudSyncState.value = _cloudSyncState.value.copy(
            isOnline = newOnline,
            syncStatusText = if (newOnline) "Back online. Syncing changes..." else "Offline mode enabled (Cached locally in Room)"
        )
        if (newOnline) {
            scope.launch { triggerCloudSync() }
        }
    }

    fun addActivityLog(author: String, action: String) {
        val item = ActivityLogItem(
            id = UUID.randomUUID().toString(),
            authorName = author,
            actionText = action,
            timestamp = System.currentTimeMillis()
        )
        _activityLogs.value = listOf(item) + _activityLogs.value.take(19)
    }

    // License Verification with Key & Custom Email (Secret keys are NOT exposed to UI)
    suspend fun verifyKeyAgainstEncryptedStorage(keyInput: String, customEmail: String): VerificationResult = withContext(Dispatchers.IO) {
        if (!encryptedStorage.isVaultEncryptedFileExisting()) {
            val keys = keyDao.getAllKeys().first()
            if (keys.isNotEmpty()) {
                encryptedStorage.saveKeysToEncryptedVault(keys)
            }
        }
        val result = encryptedStorage.verifyAndActivateKey(keyInput, customEmail)
        if (result is VerificationResult.Success) {
            keyDao.deactivateAllKeys()
            keyDao.activateKey(result.key, result.ownerEmail, System.currentTimeMillis())
            isLicenseVerified.value = true
            val tierSpec = getTierSpecForTierString(result.tier)
            _licenseState.value = LicenseState(
                isLicensed = true,
                activeKey = result.key,
                tier = result.tier,
                ownerName = result.ownerEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                ownerEmail = result.ownerEmail,
                activationDate = System.currentTimeMillis(),
                storageQuotaGb = tierSpec.storageGb
            )
            updateCloudSyncStorageQuota(result.tier)
            addActivityLog("Arc 360 Auth", "Activated license under ${result.ownerEmail} (${tierSpec.badgeLabel})")

            // Seed initial welcome email to custom user inbox
            seedWelcomeMail(result.ownerEmail, result.tier, tierSpec.storageDisplay)
        }
        result
    }

    private suspend fun seedWelcomeMail(userEmail: String, tierName: String, storageDisplay: String) {
        val existing = mailDao.getAllMails().first()
        if (existing.isEmpty()) {
            val welcomeMail = MailMessageEntity(
                id = UUID.randomUUID().toString(),
                folder = "INBOX",
                senderName = "Arc 360 Systems",
                senderEmail = "welcome@arc360.cloud",
                recipientEmail = userEmail,
                subject = "Welcome to Arc 360 Mail (Outlook Alternative)!",
                body = """Hello and welcome to your new Arc 360 workspace!

Your device is now activated with:
• License Tier: $tierName
• Cloud Storage: $storageDisplay Cloud Allocated
• Custom Email Account: $userEmail

Arc 360 Mail gives you a full Outlook alternative experience with:
- Focused and Other smart inbox triage
- Rich composition, urgent priority tagging, and attachment simulation
- Real-time sub-50ms synchronized folders (Inbox, Sent, Drafts, Starred, Archive, Trash)
- Seamless integration with Arc Docs, Sheets, Slides, and Creative Studio

We're glad to have you on board!
— The Arc 360 Cloud Platform Team""",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                isStarred = true,
                hasAttachment = true,
                attachmentName = "Getting_Started_Arc_360.pdf",
                priority = "HIGH",
                category = "Focused"
            )
            mailDao.insertMail(welcomeMail)
        }
    }

    fun deactivateLicense() {
        encryptedStorage.deactivateLicense()
        isLicenseVerified.value = false
        _licenseState.value = LicenseState(isLicensed = false)
        scope.launch(Dispatchers.IO) {
            keyDao.deactivateAllKeys()
            addActivityLog("Arc 360 Auth", "Device license cleared")
        }
    }

    // Arc 360 Mail (Outlook alternative)
    fun getAllMails(): Flow<List<MailMessageEntity>> = mailDao.getAllMails()
    fun getMailsByFolder(folder: String): Flow<List<MailMessageEntity>> = mailDao.getMailsByFolder(folder)
    fun getStarredMails(): Flow<List<MailMessageEntity>> = mailDao.getStarredMails()
    fun getUnreadMailCount(): Flow<Int> = mailDao.getUnreadCount()

    suspend fun sendMail(
        recipientEmail: String,
        subject: String,
        body: String,
        priority: String = "NORMAL",
        hasAttachment: Boolean = false,
        attachmentName: String? = null
    ) = withContext(Dispatchers.IO) {
        val isOnline = _cloudSyncState.value.isOnline
        val userEmail = _licenseState.value.ownerEmail.ifBlank { "user@arc360.cloud" }
        val senderName = _licenseState.value.ownerName.ifBlank { "Arc 360 User" }
        val targetFolder = if (isOnline) "SENT" else "OUTBOX"
        val syncStatus = if (isOnline) "SYNCED" else "CACHED_OFFLINE"

        val sentMail = MailMessageEntity(
            id = UUID.randomUUID().toString(),
            folder = targetFolder,
            senderName = senderName,
            senderEmail = userEmail,
            recipientEmail = recipientEmail,
            subject = subject.ifBlank { "(No Subject)" },
            body = body,
            timestamp = System.currentTimeMillis(),
            isRead = true,
            isStarred = false,
            hasAttachment = hasAttachment,
            attachmentName = attachmentName,
            priority = priority,
            category = "Focused",
            syncStatus = syncStatus
        )
        mailDao.insertMail(sentMail)

        if (!isOnline) {
            _cloudSyncState.value = _cloudSyncState.value.copy(
                pendingMutations = _cloudSyncState.value.pendingMutations + 1,
                syncStatusText = "Working Offline • Email queued in Room Outbox"
            )
            addActivityLog("Arc Mail (Offline)", "Queued email to $recipientEmail in Room Outbox cache")
        } else {
            addActivityLog("Arc 360 Mail", "Sent email to $recipientEmail")
        }
    }

    suspend fun saveMailDraft(
        recipientEmail: String,
        subject: String,
        body: String
    ) = withContext(Dispatchers.IO) {
        val isOnline = _cloudSyncState.value.isOnline
        val userEmail = _licenseState.value.ownerEmail.ifBlank { "user@arc360.cloud" }
        val senderName = _licenseState.value.ownerName.ifBlank { "Arc 360 User" }
        val draft = MailMessageEntity(
            id = UUID.randomUUID().toString(),
            folder = "DRAFTS",
            senderName = senderName,
            senderEmail = userEmail,
            recipientEmail = recipientEmail,
            subject = subject.ifBlank { "(Draft)" },
            body = body,
            timestamp = System.currentTimeMillis(),
            isRead = true,
            isStarred = false,
            priority = "NORMAL",
            category = "Focused",
            syncStatus = if (isOnline) "SYNCED" else "CACHED_OFFLINE"
        )
        mailDao.insertMail(draft)
        if (!isOnline) {
            addActivityLog("Arc Mail (Offline)", "Draft saved to Room local cache")
        }
    }

    suspend fun markMailRead(id: String, isRead: Boolean) = withContext(Dispatchers.IO) {
        mailDao.markAsRead(id, isRead)
    }

    suspend fun toggleMailStar(id: String, isStarred: Boolean) = withContext(Dispatchers.IO) {
        mailDao.setStarred(id, isStarred)
    }

    suspend fun moveMailToFolder(id: String, folder: String) = withContext(Dispatchers.IO) {
        mailDao.moveToFolder(id, folder)
    }

    suspend fun deleteMail(id: String) = withContext(Dispatchers.IO) {
        mailDao.deleteMailById(id)
    }
}
