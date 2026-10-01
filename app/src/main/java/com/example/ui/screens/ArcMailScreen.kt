package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LicenseState
import com.example.data.model.MailMessageEntity
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.utils.isTablet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Outlook Azure Blue color theme
val OutlookBlue = Color(0xFF0078D4)
val OutlookDarkBlue = Color(0xFF004578)
val OutlookLightBlue = Color(0xFFDEECF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcMailScreen(
    mails: List<MailMessageEntity>,
    activeMail: MailMessageEntity?,
    selectedFolder: String,
    selectedCategory: String,
    searchQuery: String,
    unreadCount: Int,
    licenseState: LicenseState,
    onSelectFolder: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenMail: (MailMessageEntity) -> Unit,
    onCloseMail: () -> Unit,
    onSendMail: (recipient: String, subject: String, body: String, priority: String, hasAttachment: Boolean, attachmentName: String?) -> Unit,
    onSaveDraft: (recipient: String, subject: String, body: String) -> Unit,
    onToggleStar: (MailMessageEntity) -> Unit,
    onMarkRead: (MailMessageEntity, Boolean) -> Unit,
    onMoveToFolder: (MailMessageEntity, String) -> Unit,
    onDeleteMail: (MailMessageEntity) -> Unit,
    onBack: () -> Unit,
    isOnline: Boolean = true,
    pendingOfflineMutations: Int = 0,
    outboxCount: Int = 0,
    offlineDocsCount: Int = 0,
    onToggleOnline: () -> Unit = {},
    onSyncNow: () -> Unit = {},
    onOpenGeminiChat: () -> Unit = {}
) {
    var showComposeDialog by remember { mutableStateOf(false) }
    var composeTo by remember { mutableStateOf("") }
    var composeSubject by remember { mutableStateOf("") }
    var composeBody by remember { mutableStateOf("") }
    var composePriority by remember { mutableStateOf("NORMAL") }
    var composeHasAttachment by remember { mutableStateOf(false) }
    var composeAttachmentName by remember { mutableStateOf<String?>(null) }

    val tabletMode = isTablet()

    BackHandler {
        if (activeMail != null && !tabletMode) {
            onCloseMail()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeMail != null && !tabletMode) {
                            onCloseMail()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OutlookBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Arc 360 Mail",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = OutlookLightBlue
                                ) {
                                    Text(
                                        text = "Outlook Alternative",
                                        color = OutlookBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = licenseState.ownerEmail.ifBlank { "Corporate & Personal Mail Client" },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenGeminiChat,
                        modifier = Modifier.testTag("mail_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Mail Assistant",
                            tint = Color(0xFF9333EA)
                        )
                    }
                    IconButton(onClick = onToggleOnline) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Toggle Network Mode",
                            tint = if (isOnline) OutlookBlue else Color(0xFFD97706)
                        )
                    }
                    IconButton(onClick = {
                        composeTo = ""
                        composeSubject = ""
                        composeBody = ""
                        composePriority = "NORMAL"
                        showComposeDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "New Mail", tint = OutlookBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (!tabletMode && activeMail == null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FloatingActionButton(
                        onClick = onOpenGeminiChat,
                        containerColor = Color(0xFF673AB7),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("gemini_mail_copilot_fab")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Gemini Copilot")
                    }

                    FloatingActionButton(
                        onClick = {
                            composeTo = ""
                            composeSubject = ""
                            composeBody = ""
                            showComposeDialog = true
                        },
                        containerColor = OutlookBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("compose_mail_fab")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Compose Email")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (tabletMode) {
            // Tablet 3-Pane / 2-Pane Outlook Experience
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Left Column: Folders Navigation Rail
                Surface(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    MailFolderNavigationList(
                        selectedFolder = selectedFolder,
                        unreadCount = unreadCount,
                        outboxCount = outboxCount,
                        onSelectFolder = onSelectFolder,
                        onNewMail = {
                            composeTo = ""
                            composeSubject = ""
                            composeBody = ""
                            showComposeDialog = true
                        }
                    )
                }

                // Middle Column: Messages List
                Surface(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        com.example.ui.components.OfflineStatusBanner(
                            isOnline = isOnline,
                            pendingOfflineMutations = pendingOfflineMutations,
                            offlineDocsCount = offlineDocsCount,
                            outboxMailCount = outboxCount,
                            onToggleOnline = onToggleOnline,
                            onSyncNow = onSyncNow
                        )

                        MailSearchAndCategoryTabs(
                            selectedFolder = selectedFolder,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            onSearchChange = onSearchQueryChange,
                            onCategoryChange = onSelectCategory
                        )

                        MailMessageList(
                            mails = mails,
                            activeMailId = activeMail?.id,
                            onSelectMail = onOpenMail,
                            onToggleStar = onToggleStar
                        )
                    }
                }

                // Right Column: Reading Pane
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (activeMail != null) {
                        MailReadingPane(
                            mail = activeMail,
                            onToggleStar = { onToggleStar(activeMail) },
                            onMarkRead = { onMarkRead(activeMail, it) },
                            onReply = {
                                composeTo = activeMail.senderEmail
                                composeSubject = if (activeMail.subject.startsWith("RE:", ignoreCase = true)) activeMail.subject else "RE: ${activeMail.subject}"
                                composeBody = "\n\n--- Original Message ---\nFrom: ${activeMail.senderName} <${activeMail.senderEmail}>\nSent: ${formatMailDate(activeMail.timestamp)}\n\n${activeMail.body}"
                                showComposeDialog = true
                            },
                            onForward = {
                                composeTo = ""
                                composeSubject = if (activeMail.subject.startsWith("FW:", ignoreCase = true)) activeMail.subject else "FW: ${activeMail.subject}"
                                composeBody = "\n\n--- Forwarded Message ---\nFrom: ${activeMail.senderName} <${activeMail.senderEmail}>\nSubject: ${activeMail.subject}\n\n${activeMail.body}"
                                showComposeDialog = true
                            },
                            onDelete = { onDeleteMail(activeMail) },
                            onMoveToFolder = { onMoveToFolder(activeMail, it) }
                        )
                    } else {
                        EmptyMailReadingPlaceholder()
                    }
                }
            }
        } else {
            // Handheld / Phone Layout
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (activeMail != null) {
                    MailReadingPane(
                        mail = activeMail,
                        onToggleStar = { onToggleStar(activeMail) },
                        onMarkRead = { onMarkRead(activeMail, it) },
                        onReply = {
                            composeTo = activeMail.senderEmail
                            composeSubject = if (activeMail.subject.startsWith("RE:", ignoreCase = true)) activeMail.subject else "RE: ${activeMail.subject}"
                            composeBody = "\n\n--- Original Message ---\nFrom: ${activeMail.senderName} <${activeMail.senderEmail}>\n\n${activeMail.body}"
                            showComposeDialog = true
                        },
                        onForward = {
                            composeTo = ""
                            composeSubject = if (activeMail.subject.startsWith("FW:", ignoreCase = true)) activeMail.subject else "FW: ${activeMail.subject}"
                            composeBody = "\n\n--- Forwarded Message ---\nFrom: ${activeMail.senderName} <${activeMail.senderEmail}>\n\n${activeMail.body}"
                            showComposeDialog = true
                        },
                        onDelete = { onDeleteMail(activeMail) },
                        onMoveToFolder = { onMoveToFolder(activeMail, it) }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        com.example.ui.components.OfflineStatusBanner(
                            isOnline = isOnline,
                            pendingOfflineMutations = pendingOfflineMutations,
                            offlineDocsCount = offlineDocsCount,
                            outboxMailCount = outboxCount,
                            onToggleOnline = onToggleOnline,
                            onSyncNow = onSyncNow
                        )

                        // Quick Folder Pills Row
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val folders = listOf(
                                "INBOX" to "Inbox",
                                "OUTBOX" to "Outbox",
                                "SENT" to "Sent",
                                "DRAFTS" to "Drafts",
                                "STARRED" to "Starred",
                                "ARCHIVE" to "Archive",
                                "TRASH" to "Trash"
                            )
                            items(folders) { (fKey, fLabel) ->
                                val isSelected = selectedFolder.equals(fKey, ignoreCase = true)
                                Surface(
                                    onClick = { onSelectFolder(fKey) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) OutlookBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = fLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (fKey == "INBOX" && unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) Color.White else OutlookBlue,
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$unreadCount",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) OutlookBlue else Color.White
                                                    )
                                                }
                                            }
                                        }
                                        if (fKey == "OUTBOX" && outboxCount > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) Color.White else Color(0xFFD97706),
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$outboxCount",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color(0xFFD97706) else Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        MailSearchAndCategoryTabs(
                            selectedFolder = selectedFolder,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            onSearchChange = onSearchQueryChange,
                            onCategoryChange = onSelectCategory
                        )

                        MailMessageList(
                            mails = mails,
                            activeMailId = null,
                            onSelectMail = onOpenMail,
                            onToggleStar = onToggleStar
                        )
                    }
                }
            }
        }
    }

    // Compose Dialog
    if (showComposeDialog) {
        ComposeMailDialog(
            userEmail = licenseState.ownerEmail.ifBlank { "user@arc360.cloud" },
            to = composeTo,
            onToChange = { composeTo = it },
            subject = composeSubject,
            onSubjectChange = { composeSubject = it },
            body = composeBody,
            onBodyChange = { composeBody = it },
            priority = composePriority,
            onPriorityChange = { composePriority = it },
            hasAttachment = composeHasAttachment,
            attachmentName = composeAttachmentName,
            onToggleAttachment = {
                composeHasAttachment = !composeHasAttachment
                composeAttachmentName = if (composeHasAttachment) "Document_Attachment.pdf" else null
            },
            isOnline = isOnline,
            onOpenGeminiDraft = onOpenGeminiChat,
            onSend = {
                onSendMail(composeTo.trim(), composeSubject.trim(), composeBody.trim(), composePriority, composeHasAttachment, composeAttachmentName)
                showComposeDialog = false
            },
            onSaveDraft = {
                onSaveDraft(composeTo.trim(), composeSubject.trim(), composeBody.trim())
                showComposeDialog = false
            },
            onDismiss = { showComposeDialog = false }
        )
    }
}

@Composable
private fun MailFolderNavigationList(
    selectedFolder: String,
    unreadCount: Int,
    outboxCount: Int = 0,
    onSelectFolder: (String) -> Unit,
    onNewMail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Button(
            onClick = onNewMail,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OutlookBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Mail", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "FOLDERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val folderItems = listOf(
            Triple("INBOX", "Inbox", Icons.Default.Inbox),
            Triple("OUTBOX", "Outbox (Offline)", Icons.Default.CloudSync),
            Triple("SENT", "Sent Items", Icons.AutoMirrored.Filled.Send),
            Triple("DRAFTS", "Drafts", Icons.Default.Drafts),
            Triple("STARRED", "Starred", Icons.Default.Star),
            Triple("ARCHIVE", "Archive", Icons.Default.Archive),
            Triple("TRASH", "Deleted Items", Icons.Default.Delete)
        )

        folderItems.forEach { (key, label, icon) ->
            val isSelected = selectedFolder.equals(key, ignoreCase = true)
            Surface(
                onClick = { onSelectFolder(key) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) OutlookLightBlue else Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) OutlookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) OutlookBlue else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (key == "INBOX" && unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = OutlookBlue,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$unreadCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (key == "OUTBOX" && outboxCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$outboxCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MailSearchAndCategoryTabs(
    selectedFolder: String,
    selectedCategory: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search messages, senders, subject...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        )

        // Outlook Focused vs Other tabs (Only for INBOX)
        if (selectedFolder.equals("INBOX", ignoreCase = true)) {
            TabRow(
                selectedTabIndex = if (selectedCategory == "Focused") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OutlookBlue
            ) {
                Tab(
                    selected = selectedCategory == "Focused",
                    onClick = { onCategoryChange("Focused") },
                    text = {
                        Text(
                            text = "Focused",
                            fontWeight = if (selectedCategory == "Focused") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedCategory == "Other",
                    onClick = { onCategoryChange("Other") },
                    text = {
                        Text(
                            text = "Other",
                            fontWeight = if (selectedCategory == "Other") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MailMessageList(
    mails: List<MailMessageEntity>,
    activeMailId: String?,
    onSelectMail: (MailMessageEntity) -> Unit,
    onToggleStar: (MailMessageEntity) -> Unit
) {
    if (mails.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MailOutline,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No messages in this folder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(mails, key = { it.id }) { mail ->
                MailItemRow(
                    mail = mail,
                    isSelected = mail.id == activeMailId,
                    onClick = { onSelectMail(mail) },
                    onToggleStar = { onToggleStar(mail) }
                )
            }
        }
    }
}

@Composable
private fun MailItemRow(
    mail: MailMessageEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleStar: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) OutlookLightBlue.copy(alpha = 0.7f) else if (!mail.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else Color.Transparent,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator dot
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (!mail.isRead) OutlookBlue else Color.Transparent)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar initial circle
            Surface(
                shape = CircleShape,
                color = OutlookBlue.copy(alpha = 0.15f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = mail.senderName.take(1).uppercase(),
                        color = OutlookBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mail.senderName,
                        fontWeight = if (!mail.isRead) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatMailTime(mail.timestamp),
                        fontSize = 10.sp,
                        color = if (!mail.isRead) OutlookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (!mail.isRead) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mail.priority == "HIGH") {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Urgent",
                            tint = StatusError,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = mail.subject,
                        fontWeight = if (!mail.isRead) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = mail.body.take(80).replace("\n", " "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (mail.hasAttachment) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mail.attachmentName ?: "Attachment",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Star icon
            IconButton(
                onClick = onToggleStar,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (mail.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star",
                    tint = if (mail.isStarred) Color(0xFFEAA300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MailReadingPane(
    mail: MailMessageEntity,
    onToggleStar: () -> Unit,
    onMarkRead: (Boolean) -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onDelete: () -> Unit,
    onMoveToFolder: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Outlook Actions Header Toolbar
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onReply) {
                        Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Reply", tint = OutlookBlue)
                    }
                    IconButton(onClick = onForward) {
                        Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = "Forward", tint = OutlookBlue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleStar) {
                        Icon(
                            imageVector = if (mail.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star",
                            tint = if (mail.isStarred) Color(0xFFEAA300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onMarkRead(!mail.isRead) }) {
                        Icon(
                            imageVector = if (mail.isRead) Icons.Default.MarkEmailUnread else Icons.Default.MarkEmailRead,
                            contentDescription = "Read/Unread",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onMoveToFolder("ARCHIVE") }) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Header
        Text(
            text = mail.subject,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sender Info Card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = OutlookBlue,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = mail.senderName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mail.senderName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "<${mail.senderEmail}>",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "To: ${mail.recipientEmail}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatMailDate(mail.timestamp),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mail.hasAttachment && mail.attachmentName != null) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = OutlookLightBlue.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, OutlookBlue.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = OutlookBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mail.attachmentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Verified Cloud Attachment • Synced with Arc Storage",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Email Body Text
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = mail.body,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Reply Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onReply,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = OutlookBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reply")
            }

            OutlinedButton(
                onClick = onForward,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Forward")
            }
        }
    }
}

@Composable
private fun EmptyMailReadingPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Select a message to view its contents",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeMailDialog(
    userEmail: String,
    to: String,
    onToChange: (String) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    body: String,
    onBodyChange: (String) -> Unit,
    priority: String,
    onPriorityChange: (String) -> Unit,
    hasAttachment: Boolean,
    attachmentName: String?,
    onToggleAttachment: () -> Unit,
    isOnline: Boolean = true,
    onOpenGeminiDraft: () -> Unit = {},
    onSend: () -> Unit,
    onSaveDraft: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Message", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onOpenGeminiDraft,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF673AB7).copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF673AB7),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Draft with Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "From: $userEmail",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = to,
                    onValueChange = onToChange,
                    label = { Text("To") },
                    placeholder = { Text("colleague@company.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = onSubjectChange,
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { onPriorityChange(if (priority == "HIGH") "NORMAL" else "HIGH") },
                        shape = RoundedCornerShape(6.dp),
                        color = if (priority == "HIGH") StatusError.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = if (priority == "HIGH") StatusError else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (priority == "HIGH") "Urgent Priority" else "Normal Priority",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (priority == "HIGH") StatusError else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = onToggleAttachment,
                        shape = RoundedCornerShape(6.dp),
                        color = if (hasAttachment) OutlookLightBlue else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = if (hasAttachment) OutlookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasAttachment) "File Attached" else "Attach File",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasAttachment) OutlookBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (hasAttachment && attachmentName != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OutlookLightBlue.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, OutlookBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = OutlookBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = attachmentName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = body,
                    onValueChange = onBodyChange,
                    placeholder = { Text("Write your message here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                enabled = to.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isOnline) OutlookBlue else Color(0xFFD97706))
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.AutoMirrored.Filled.Send else Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isOnline) "Send" else "Queue in Outbox (Offline)")
            }
        },
        dismissButton = {
            TextButton(onClick = onSaveDraft) {
                Text("Save Draft")
            }
        }
    )
}

private fun formatMailTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return if (diff < 86400000L) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    } else {
        SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatMailDate(timestamp: Long): String {
    return SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
