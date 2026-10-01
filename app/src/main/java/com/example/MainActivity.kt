package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AfterEffectsEditorScreen
import com.example.ui.screens.ArcMailScreen
import com.example.ui.screens.CanvasEditorScreen
import com.example.ui.screens.CloudSyncCenterScreen
import com.example.ui.screens.CollaboratorHubScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocsEditorScreen
import com.example.ui.screens.InDesignEditorScreen
import com.example.ui.screens.LicenseManagerScreen
import com.example.ui.screens.LicenseVerificationScreen
import com.example.ui.screens.PdfEditorScreen
import com.example.ui.screens.PhotoshopEditorScreen
import com.example.ui.screens.PremiereEditorScreen
import com.example.ui.screens.SheetsEditorScreen
import com.example.ui.screens.SlidesEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ArcViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ArcSuiteApp()
            }
        }
    }
}

@Composable
fun ArcSuiteApp(viewModel: ArcViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeDocument by viewModel.activeDocument.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val cloudSyncInfo by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val licenseState by viewModel.licenseState.collectAsStateWithLifecycle()
    val collaborators by viewModel.collaborators.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isVerifying by viewModel.isVerifying.collectAsStateWithLifecycle()
    val verificationError by viewModel.verificationError.collectAsStateWithLifecycle()
    val verificationSuccess by viewModel.verificationSuccess.collectAsStateWithLifecycle()

    // Mail states
    val mails by viewModel.mails.collectAsStateWithLifecycle()
    val activeMail by viewModel.activeMail.collectAsStateWithLifecycle()
    val selectedMailFolder by viewModel.selectedMailFolder.collectAsStateWithLifecycle()
    val selectedMailCategory by viewModel.selectedMailCategory.collectAsStateWithLifecycle()
    val mailSearchQuery by viewModel.mailSearchQuery.collectAsStateWithLifecycle()
    val unreadMailCount by viewModel.unreadMailCount.collectAsStateWithLifecycle()

    // Offline and Gemini Chatbot states
    val outboxMailCount by viewModel.outboxMailCount.collectAsStateWithLifecycle()
    val offlineDocsCount by viewModel.offlineDocsCount.collectAsStateWithLifecycle()
    val pendingOfflineMutations by viewModel.pendingOfflineMutations.collectAsStateWithLifecycle()
    val showAiChatbot by viewModel.showAiChatbot.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val chatTargetContext by viewModel.chatTargetContext.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messageEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(
                        documents = documents,
                        cloudSyncInfo = cloudSyncInfo,
                        licenseState = licenseState,
                        collaborators = collaborators,
                        unreadMailCount = unreadMailCount,
                        selectedTypeFilter = selectedTypeFilter,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterSelect = { viewModel.setTypeFilter(it) },
                        onOpenDoc = { viewModel.openDocument(it) },
                        onCreateDoc = { type, title -> viewModel.createDocument(type, title) },
                        onToggleStar = { viewModel.toggleStarred(it) },
                        onDeleteDoc = { viewModel.deleteDocument(it) },
                        onOpenMail = { viewModel.navigateTo(Screen.MAIL) },
                        onOpenCloudSync = { viewModel.navigateTo(Screen.CLOUD_SYNC_CENTER) },
                        onOpenCollabHub = { viewModel.navigateTo(Screen.COLLAB_HUB) },
                        onOpenLicenseManager = { viewModel.navigateTo(Screen.LICENSE_MANAGER) },
                        pendingOfflineMutations = pendingOfflineMutations,
                        offlineDocsCount = offlineDocsCount,
                        outboxMailCount = outboxMailCount,
                        onToggleOnline = { viewModel.toggleOnlineMode() },
                        onSyncNow = { viewModel.triggerCloudSync() },
                        onOpenGeminiChat = { viewModel.openAiChat("General") }
                    )
                }

                Screen.MAIL -> {
                    ArcMailScreen(
                        mails = mails,
                        activeMail = activeMail,
                        selectedFolder = selectedMailFolder,
                        selectedCategory = selectedMailCategory,
                        searchQuery = mailSearchQuery,
                        unreadCount = unreadMailCount,
                        licenseState = licenseState,
                        onSelectFolder = { viewModel.setMailFolder(it) },
                        onSelectCategory = { viewModel.setMailCategory(it) },
                        onSearchQueryChange = { viewModel.setMailSearchQuery(it) },
                        onOpenMail = { viewModel.openMail(it) },
                        onCloseMail = { viewModel.closeActiveMail() },
                        onSendMail = { recipient, subject, body, priority, hasAttachment, attachmentName ->
                            viewModel.sendMail(recipient, subject, body, priority, hasAttachment, attachmentName)
                        },
                        onSaveDraft = { recipient, subject, body ->
                            viewModel.saveMailDraft(recipient, subject, body)
                        },
                        onToggleStar = { viewModel.toggleMailStar(it) },
                        onMarkRead = { mail, isRead -> viewModel.markMailRead(mail, isRead) },
                        onMoveToFolder = { mail, folder -> viewModel.moveMailToFolder(mail, folder) },
                        onDeleteMail = { viewModel.deleteMail(it) },
                        onBack = { viewModel.navigateBack() },
                        isOnline = cloudSyncInfo.isOnline,
                        pendingOfflineMutations = pendingOfflineMutations,
                        outboxCount = outboxMailCount,
                        offlineDocsCount = offlineDocsCount,
                        onToggleOnline = { viewModel.toggleOnlineMode() },
                        onSyncNow = { viewModel.triggerCloudSync() },
                        onOpenGeminiChat = { viewModel.openAiChat("Mail") }
                    )
                }

                Screen.DOC_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        DocsEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) },
                            onAddComment = { docId, comment -> viewModel.addDocumentComment(docId, comment, "Docs") },
                            isOnline = cloudSyncInfo.isOnline,
                            pendingOfflineMutations = pendingOfflineMutations,
                            onToggleOnline = { viewModel.toggleOnlineMode() },
                            onOpenGeminiChat = { viewModel.openAiChat("Docs") }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.SHEET_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        SheetsEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) },
                            isOnline = cloudSyncInfo.isOnline,
                            pendingOfflineMutations = pendingOfflineMutations,
                            onToggleOnline = { viewModel.toggleOnlineMode() },
                            onOpenGeminiChat = { viewModel.openAiChat("Sheets") }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.SLIDE_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        SlidesEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.PDF_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        PdfEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) },
                            isOnline = cloudSyncInfo.isOnline,
                            pendingOfflineMutations = pendingOfflineMutations,
                            onToggleOnline = { viewModel.toggleOnlineMode() },
                            onOpenGeminiChat = { viewModel.openAiChat("PDF") }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.CANVAS_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        CanvasEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.PHOTOSHOP_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        PhotoshopEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.PREMIERE_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        PremiereEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.INDESIGN_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        InDesignEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.AFTER_EFFECTS_EDITOR -> {
                    val doc = activeDocument
                    if (doc != null) {
                        AfterEffectsEditorScreen(
                            document = doc,
                            collaborators = collaborators,
                            onBack = { viewModel.navigateBack() },
                            onSaveContent = { content, title -> viewModel.saveDocumentContent(content, title) }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                Screen.LICENSE_MANAGER -> {
                    LicenseManagerScreen(
                        licenseState = licenseState,
                        cloudSyncInfo = cloudSyncInfo,
                        onActivateNewKey = { key, email -> viewModel.verifyProductKey(key, email) },
                        onDeactivateLicense = { viewModel.deactivateLicense() },
                        onNavigateToCloudSync = { viewModel.navigateTo(Screen.CLOUD_SYNC_CENTER) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.LICENSE_VERIFICATION -> {
                    LicenseVerificationScreen(
                        isVerifying = isVerifying,
                        verificationError = verificationError,
                        verificationSuccess = verificationSuccess,
                        onVerifyKey = { key, email ->
                            viewModel.verifyProductKey(key, email)
                        },
                        onSuccessContinue = {
                            viewModel.navigateTo(Screen.DASHBOARD)
                        },
                        onBack = if (licenseState.isLicensed) {
                            { viewModel.navigateBack() }
                        } else null
                    )
                }

                Screen.CLOUD_SYNC_CENTER -> {
                    CloudSyncCenterScreen(
                        cloudSyncInfo = cloudSyncInfo,
                        activityLogs = activityLogs,
                        onTriggerSync = { viewModel.triggerCloudSync() },
                        onToggleOnline = { viewModel.toggleOnlineMode() },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.COLLAB_HUB -> {
                    CollaboratorHubScreen(
                        collaborators = collaborators,
                        onCopyInviteCode = { viewModel.copyToClipboard(it, "Invite Code") },
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
            )

            if (showAiChatbot) {
                com.example.ui.components.GeminiChatbotSheet(
                    messages = chatMessages,
                    isThinking = isAiThinking,
                    isOnline = cloudSyncInfo.isOnline,
                    targetContext = chatTargetContext,
                    onSendMessage = { prompt -> viewModel.sendAiMessage(prompt) },
                    onClearChat = { viewModel.clearAiChat() },
                    onCopyText = { text, label -> viewModel.copyToClipboard(text, label) },
                    onInsertText = { text ->
                        when (currentScreen) {
                            Screen.DOC_EDITOR -> {
                                val doc = activeDocument
                                if (doc != null) {
                                    val newContent = if (doc.content.isBlank()) text else "${doc.content}\n\n$text"
                                    viewModel.saveDocumentContent(newContent, null)
                                    viewModel.copyToClipboard(text, "Document Content")
                                }
                            }
                            Screen.SHEET_EDITOR -> {
                                viewModel.copyToClipboard(text, "Excel Formula")
                            }
                            Screen.PDF_EDITOR -> {
                                viewModel.copyToClipboard(text, "PDF Annotation")
                            }
                            Screen.MAIL -> {
                                viewModel.copyToClipboard(text, "Email Content")
                            }
                            else -> {
                                viewModel.copyToClipboard(text, "Arc Copilot")
                            }
                        }
                    },
                    onDismiss = { viewModel.closeAiChat() }
                )
            }
        }
    }
}
