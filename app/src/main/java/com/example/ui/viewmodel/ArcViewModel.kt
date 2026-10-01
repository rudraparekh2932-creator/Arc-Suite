package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ArcDatabase
import com.example.data.model.DocType
import com.example.data.model.DocumentEntity
import com.example.data.model.MailMessageEntity
import com.example.data.repository.ArcRepository
import com.example.data.security.VerificationResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    MAIL,
    DOC_EDITOR,
    SHEET_EDITOR,
    SLIDE_EDITOR,
    PDF_EDITOR,
    CANVAS_EDITOR,
    PHOTOSHOP_EDITOR,
    PREMIERE_EDITOR,
    INDESIGN_EDITOR,
    AFTER_EFFECTS_EDITOR,
    LICENSE_MANAGER,
    LICENSE_VERIFICATION,
    CLOUD_SYNC_CENTER,
    COLLAB_HUB
}

class ArcViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ArcDatabase.getDatabase(application, viewModelScope)
    val repository = ArcRepository(database, application, viewModelScope)

    // Navigation State - defaults to LICENSE_VERIFICATION until verified
    private val _currentScreen = MutableStateFlow(Screen.LICENSE_VERIFICATION)
    val currentScreen = _currentScreen.asStateFlow()

    // Active Document being edited
    private val _activeDocument = MutableStateFlow<DocumentEntity?>(null)
    val activeDocument = _activeDocument.asStateFlow()

    // Filter & Search
    private val _selectedTypeFilter = MutableStateFlow<DocType?>(null)
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Toast/Snackbar events
    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents = _messageEvents.asSharedFlow()

    // Data streams from repository
    val cloudSyncState = repository.cloudSyncState
    val collaborators = repository.collaborators
    val activityLogs = repository.activityLogs
    val licenseState = repository.licenseState
    val isLicenseVerified = repository.isLicenseVerified
    val deviceHardwareId: String = repository.deviceHardwareId
    val deviceHardwareDetails: com.example.data.security.DeviceHardwareDetails = repository.deviceHardwareDetails

    // Verification Screen States
    private val _isVerifying = MutableStateFlow(false)
    val isVerifying = _isVerifying.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError = _verificationError.asStateFlow()

    private val _verificationSuccess = MutableStateFlow<VerificationResult.Success?>(null)
    val verificationSuccess = _verificationSuccess.asStateFlow()

    // Filtered documents (starts empty for clean slate on new install)
    val documents = combine(
        repository.getAllDocuments(),
        _selectedTypeFilter,
        _searchQuery
    ) { docs, typeFilter, query ->
        docs.filter { doc ->
            (typeFilter == null || doc.typeName == typeFilter.name) &&
                    (query.isBlank() || doc.title.contains(query, ignoreCase = true) || doc.content.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Arc 360 Mail (Outlook Alternative) States
    private val _selectedMailFolder = MutableStateFlow("INBOX")
    val selectedMailFolder = _selectedMailFolder.asStateFlow()

    private val _selectedMailCategory = MutableStateFlow("Focused") // Focused vs Other (Outlook style)
    val selectedMailCategory = _selectedMailCategory.asStateFlow()

    private val _mailSearchQuery = MutableStateFlow("")
    val mailSearchQuery = _mailSearchQuery.asStateFlow()

    private val _activeMail = MutableStateFlow<MailMessageEntity?>(null)
    val activeMail = _activeMail.asStateFlow()

    val mails = combine(
        repository.getAllMails(),
        _selectedMailFolder,
        _selectedMailCategory,
        _mailSearchQuery
    ) { allMails, folder, category, query ->
        allMails.filter { mail ->
            val folderMatch = if (folder == "STARRED") {
                mail.isStarred && mail.folder != "TRASH"
            } else {
                mail.folder.equals(folder, ignoreCase = true)
            }
            val categoryMatch = if (folder == "INBOX") {
                mail.category.equals(category, ignoreCase = true)
            } else {
                true
            }
            val queryMatch = query.isBlank() ||
                    mail.subject.contains(query, ignoreCase = true) ||
                    mail.senderName.contains(query, ignoreCase = true) ||
                    mail.senderEmail.contains(query, ignoreCase = true) ||
                    mail.body.contains(query, ignoreCase = true)
            folderMatch && categoryMatch && queryMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadMailCount = repository.getUnreadMailCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val outboxMailCount = repository.outboxMailsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val offlineDocsCount = repository.offlineDocsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingOfflineMutations = combine(offlineDocsCount, outboxMailCount) { docs, mails -> docs + mails }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Gemini Chatbot & Work Assistant Engine
    private val geminiService = com.example.data.ai.GeminiService(application)
    private val _showAiChatbot = MutableStateFlow(false)
    val showAiChatbot = _showAiChatbot.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking = _isAiThinking.asStateFlow()

    private val _chatTargetContext = MutableStateFlow("General") // "General", "Sheets", "Docs", "Mail"
    val chatTargetContext = _chatTargetContext.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<com.example.data.ai.ChatMessage>>(listOf(
        com.example.data.ai.ChatMessage(
            sender = com.example.data.ai.MessageSender.GEMINI_ASSISTANT,
            text = "👋 Hello! I am Arc Copilot, your Gemini everyday work & Excel formula assistant.\n\n" +
                    "I can help you with:\n" +
                    "• Generating complex Excel & Sheets formulas (XLOOKUP, SUMIFS, INDEX/MATCH, IFERROR, CAGR, PMT)\n" +
                    "• Drafting and polishing business emails\n" +
                    "• Summarizing documents and meeting notes\n" +
                    "• Offline Continuity: When you lose internet connection, all edits & outbox emails are safely cached locally in Room and sync automatically when reconnected!\n\n" +
                    "What would you like to build or solve today?",
            detectedFormula = "=XLOOKUP(A2, Products!A:A, Products!B:B, \"Not Found\", 0)"
        )
    ))
    val chatMessages = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.licenseState.collect { lic ->
                if (lic.isLicensed) {
                    if (_currentScreen.value == Screen.LICENSE_VERIFICATION) {
                        _currentScreen.value = Screen.DASHBOARD
                    }
                } else {
                    _currentScreen.value = Screen.LICENSE_VERIFICATION
                }
            }
        }
    }

    fun navigateTo(screen: Screen, document: DocumentEntity? = null) {
        if (!licenseState.value.isLicensed && screen != Screen.LICENSE_VERIFICATION) {
            _currentScreen.value = Screen.LICENSE_VERIFICATION
            return
        }
        if (document != null) {
            _activeDocument.value = document
        }
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (!licenseState.value.isLicensed) {
            _currentScreen.value = Screen.LICENSE_VERIFICATION
            return
        }
        if (_activeMail.value != null && _currentScreen.value == Screen.MAIL) {
            _activeMail.value = null
            return
        }
        if (_currentScreen.value != Screen.DASHBOARD) {
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    fun setTypeFilter(type: DocType?) {
        _selectedTypeFilter.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openDocument(doc: DocumentEntity) {
        _activeDocument.value = doc
        when (doc.type) {
            DocType.DOC -> navigateTo(Screen.DOC_EDITOR, doc)
            DocType.SHEET -> navigateTo(Screen.SHEET_EDITOR, doc)
            DocType.SLIDE -> navigateTo(Screen.SLIDE_EDITOR, doc)
            DocType.PDF -> navigateTo(Screen.PDF_EDITOR, doc)
            DocType.CANVAS -> navigateTo(Screen.CANVAS_EDITOR, doc)
            DocType.PHOTOSHOP -> navigateTo(Screen.PHOTOSHOP_EDITOR, doc)
            DocType.PREMIERE -> navigateTo(Screen.PREMIERE_EDITOR, doc)
            DocType.INDESIGN -> navigateTo(Screen.INDESIGN_EDITOR, doc)
            DocType.AFTER_EFFECTS -> navigateTo(Screen.AFTER_EFFECTS_EDITOR, doc)
        }
    }

    fun createDocument(type: DocType, title: String) {
        viewModelScope.launch {
            val newDoc = repository.createDocument(type, title)
            openDocument(newDoc)
            _messageEvents.emit("Created new ${type.displayName}")
        }
    }

    fun saveDocumentContent(content: String, updatedTitle: String? = null) {
        val current = _activeDocument.value ?: return
        viewModelScope.launch {
            repository.updateDocumentContent(current.id, content, updatedTitle)
        }
    }

    fun toggleStarred(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.toggleDocumentStarred(doc)
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            if (_activeDocument.value?.id == doc.id) {
                _activeDocument.value = null
                _currentScreen.value = Screen.DASHBOARD
            }
            _messageEvents.emit("Document '${doc.title}' deleted")
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.triggerCloudSync()
            _messageEvents.emit("Cloud synchronization completed")
        }
    }

    fun toggleOnlineMode() {
        repository.toggleOnlineMode()
        val online = repository.cloudSyncState.value.isOnline
        viewModelScope.launch {
            _messageEvents.emit(if (online) "Online: Cloud sync active" else "Offline mode: Cached locally")
        }
    }

    fun addDocumentComment(docId: String, text: String, anchor: String) {
        viewModelScope.launch {
            repository.addComment(docId, text, anchor)
            _messageEvents.emit("Comment posted")
        }
    }

    // License Verification with Key & Custom Email
    fun verifyProductKey(key: String, customEmail: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationSuccess.value = null
            kotlinx.coroutines.delay(650)
            val result = repository.verifyKeyAgainstEncryptedStorage(key, customEmail)
            _isVerifying.value = false
            when (result) {
                is VerificationResult.Success -> {
                    _verificationSuccess.value = result
                    _messageEvents.emit("License Activated for ${result.ownerEmail} (${result.tier})")
                    _currentScreen.value = Screen.DASHBOARD
                    onSuccess()
                }
                is VerificationResult.Failure -> {
                    _verificationError.value = result.reason
                    _messageEvents.emit(result.reason)
                }
            }
        }
    }

    fun deactivateLicense() {
        repository.deactivateLicense()
        _verificationSuccess.value = null
        _verificationError.value = null
        _currentScreen.value = Screen.LICENSE_VERIFICATION
    }

    fun resetVerificationState() {
        _verificationError.value = null
        _verificationSuccess.value = null
    }

    // Arc 360 Mail (Outlook Alternative) Methods
    fun setMailFolder(folder: String) {
        _selectedMailFolder.value = folder
        _activeMail.value = null
    }

    fun setMailCategory(category: String) {
        _selectedMailCategory.value = category
    }

    fun setMailSearchQuery(query: String) {
        _mailSearchQuery.value = query
    }

    fun openMail(mail: MailMessageEntity) {
        _activeMail.value = mail
        if (!mail.isRead) {
            viewModelScope.launch {
                repository.markMailRead(mail.id, true)
            }
        }
    }

    fun closeActiveMail() {
        _activeMail.value = null
    }

    fun sendMail(
        recipientEmail: String,
        subject: String,
        body: String,
        priority: String = "NORMAL",
        hasAttachment: Boolean = false,
        attachmentName: String? = null
    ) {
        viewModelScope.launch {
            repository.sendMail(recipientEmail, subject, body, priority, hasAttachment, attachmentName)
            _messageEvents.emit("Email sent to $recipientEmail")
        }
    }

    fun saveMailDraft(recipientEmail: String, subject: String, body: String) {
        viewModelScope.launch {
            repository.saveMailDraft(recipientEmail, subject, body)
            _messageEvents.emit("Draft saved")
        }
    }

    fun toggleMailStar(mail: MailMessageEntity) {
        viewModelScope.launch {
            repository.toggleMailStar(mail.id, !mail.isStarred)
        }
    }

    fun markMailRead(mail: MailMessageEntity, isRead: Boolean) {
        viewModelScope.launch {
            repository.markMailRead(mail.id, isRead)
        }
    }

    fun moveMailToFolder(mail: MailMessageEntity, folder: String) {
        viewModelScope.launch {
            repository.moveMailToFolder(mail.id, folder)
            if (_activeMail.value?.id == mail.id) {
                _activeMail.value = null
            }
            _messageEvents.emit("Moved to $folder")
        }
    }

    fun deleteMail(mail: MailMessageEntity) {
        viewModelScope.launch {
            if (mail.folder == "TRASH") {
                repository.deleteMail(mail.id)
                _messageEvents.emit("Email permanently deleted")
            } else {
                repository.moveMailToFolder(mail.id, "TRASH")
                _messageEvents.emit("Email moved to Trash")
            }
            if (_activeMail.value?.id == mail.id) {
                _activeMail.value = null
            }
        }
    }

    fun copyToClipboard(text: String, label: String = "Arc 360 Token") {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    // AI Chatbot & Everyday Work Assistant Methods
    fun openAiChat(context: String = "General") {
        _chatTargetContext.value = context
        _showAiChatbot.value = true
    }

    fun closeAiChat() {
        _showAiChatbot.value = false
    }

    fun clearAiChat() {
        _chatMessages.value = listOf(
            com.example.data.ai.ChatMessage(
                sender = com.example.data.ai.MessageSender.GEMINI_ASSISTANT,
                text = "✨ Chat reset. How can I help with your workplace tasks or Excel formulas today?",
                detectedFormula = "=IFERROR(VLOOKUP(A2, Data!A:C, 2, FALSE), \"-\")"
            )
        )
    }

    fun sendAiMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _isAiThinking.value) return

        val userMessage = com.example.data.ai.ChatMessage(
            sender = com.example.data.ai.MessageSender.USER,
            text = trimmed
        )
        _chatMessages.value = _chatMessages.value + userMessage
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val isCurrentlyOnline = cloudSyncState.value.isOnline
                val response = geminiService.generateWorkAssistantResponse(
                    prompt = trimmed,
                    conversationHistory = _chatMessages.value,
                    forceOffline = !isCurrentlyOnline
                )
                _chatMessages.value = _chatMessages.value + response
            } catch (e: Exception) {
                val fallback = com.example.data.ai.OfflineAssistantEngine.processQuery(trimmed)
                _chatMessages.value = _chatMessages.value + com.example.data.ai.ChatMessage(
                    sender = com.example.data.ai.MessageSender.GEMINI_ASSISTANT,
                    text = fallback.reply + "\n\n[Served via Room Offline Engine]",
                    detectedFormula = fallback.formula,
                    isOfflineResponse = true
                )
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun setCustomGeminiApiKey(key: String) {
        geminiService.setCustomApiKey(key)
        _messageEvents.tryEmit("Gemini API key updated")
    }
}
