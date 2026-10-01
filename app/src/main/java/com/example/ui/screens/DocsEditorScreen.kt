package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StrikethroughS
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorDocBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.utils.isTablet
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocsEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit,
    onAddComment: (String, String) -> Unit,
    isOnline: Boolean = true,
    pendingOfflineMutations: Int = 0,
    onToggleOnline: () -> Unit = {},
    onOpenGeminiChat: () -> Unit = {}
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }
    var textContent by remember(document.id) { mutableStateOf(document.content) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var showFindReplace by remember { mutableStateOf(false) }
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var showCommentDialog by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var isSavedNotification by remember { mutableStateOf(false) }

    val tabletMode = isTablet()

    // Word & Character count calculation
    val wordCount = remember(textContent) {
        if (textContent.isBlank()) 0 else textContent.trim().split("\\s+".toRegex()).size
    }
    val charCount = remember(textContent) { textContent.length }
    val readingTimeMin = remember(wordCount) { (wordCount / 200).coerceAtLeast(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        TextField(
                            value = title,
                            onValueChange = {
                                title = it
                                onSaveContent(textContent, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) StatusSuccess else Color(0xFFD97706),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Auto-saved to Room & Cloud • v${document.versionNumber}" else "Working Offline • Room Cache Active • v${document.versionNumber}",
                                fontSize = 10.sp,
                                color = if (isOnline) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFD97706),
                                fontWeight = if (!isOnline) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                },
                actions = {
                    // Gemini Work Copilot button
                    IconButton(
                        onClick = onOpenGeminiChat,
                        modifier = Modifier.testTag("docs_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Work Copilot",
                            tint = Color(0xFF9333EA)
                        )
                    }
                    // Toggle online/offline mode
                    IconButton(onClick = onToggleOnline) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Toggle Network Status",
                            tint = if (isOnline) MaterialTheme.colorScheme.primary else Color(0xFFD97706)
                        )
                    }
                    // Preview switch button
                    IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                        Icon(
                            imageVector = Icons.Default.Preview,
                            contentDescription = "Toggle Preview",
                            tint = if (isPreviewMode) ArcPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Comment button
                    IconButton(onClick = { showCommentDialog = true }) {
                        Icon(imageVector = Icons.Default.AddComment, contentDescription = "Add Comment")
                    }
                    // Save button
                    IconButton(
                        onClick = {
                            onSaveContent(textContent, title)
                            isSavedNotification = true
                        }
                    ) {
                        Icon(
                            imageVector = if (isSavedNotification) Icons.Default.Check else Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (isSavedNotification) StatusSuccess else ArcPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            com.example.ui.components.OfflineStatusBanner(
                isOnline = isOnline,
                pendingOfflineMutations = pendingOfflineMutations,
                offlineDocsCount = if (!isOnline) 1 else 0,
                outboxMailCount = 0,
                onToggleOnline = onToggleOnline,
                onSyncNow = { onSaveContent(textContent, title) }
            )

            // Collaborator Live Presence Banner
            CollaboratorPresenceBanner(collaborators = collaborators)

            // Formatting Toolbar (Word clone tools)
            FormattingToolbar(
                onInsertMarkdown = { prefix, suffix ->
                    textContent = "$textContent\n$prefix $suffix"
                    onSaveContent(textContent, title)
                },
                onToggleFindReplace = { showFindReplace = !showFindReplace }
            )

            // Find & Replace Strip
            AnimatedVisibility(visible = showFindReplace) {
                FindReplaceBar(
                    findText = findText,
                    onFindChange = { findText = it },
                    replaceText = replaceText,
                    onReplaceChange = { replaceText = it },
                    onReplaceAll = {
                        if (findText.isNotEmpty()) {
                            textContent = textContent.replace(findText, replaceText, ignoreCase = true)
                            onSaveContent(textContent, title)
                        }
                    },
                    onClose = { showFindReplace = false }
                )
            }

            // Document Body: Edit vs Preview Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(8.dp)
            ) {
                if (tabletMode && !isPreviewMode) {
                    // Tablet Split Screen: Editor on Left, Live Formatted Document Preview on Right
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "DOCUMENT SOURCE (MARKDOWN)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                TextField(
                                    value = textContent,
                                    onValueChange = {
                                        textContent = it
                                        onSaveContent(it, title)
                                    },
                                    placeholder = { Text("Write your document here. Supports headings (#), bold (**), bullet lists (*)...") },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("docs_editor_textarea"),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "LIVE RICH DOCUMENT PREVIEW",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorDocBlue,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                FormattedDocumentViewer(title = title, textContent = textContent)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        if (isPreviewMode) {
                            FormattedDocumentViewer(title = title, textContent = textContent)
                        } else {
                            TextField(
                                value = textContent,
                                onValueChange = {
                                    textContent = it
                                    onSaveContent(it, title)
                                },
                                placeholder = { Text("Write your document here. Supports headings (#), bold (**), bullet lists (*)...") },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("docs_editor_textarea"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }

            // Word Count & Status Footer
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$wordCount words • $charCount chars • ~$readingTimeMin min read",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cloud Sync Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusSuccess
                        )
                    }
                }
            }
        }
    }

    if (showCommentDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text("Collaborative Comment") },
            text = {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add comment or @mention a teammate...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            onAddComment(document.id, newCommentText)
                            newCommentText = ""
                            showCommentDialog = false
                        }
                    }
                ) {
                    Text("Post Comment")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCommentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FormattedDocumentViewer(title: String, textContent: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = ColorDocBlue
        )
        Spacer(modifier = Modifier.height(12.dp))
        textContent.lines().forEach { line ->
            when {
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArcPrimary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold, color = ArcPrimary)
                        Text(line.removePrefix("* "))
                    }
                }
                line.startsWith("> ") -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, ArcPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = line.removePrefix("> "),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CollaboratorPresenceBanner(collaborators: List<Collaborator>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Live Presence: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                collaborators.filter { it.isOnline }.forEach { c ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = c.color.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, c.color.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(horizontal = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(c.color)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${c.name.split(" ")[0]} (${c.role})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.color
                            )
                        }
                    }
                }
            }
            Text(
                text = "Multiplayer sync",
                fontSize = 10.sp,
                color = StatusSuccess,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FormattingToolbar(
    onInsertMarkdown: (prefix: String, suffix: String) -> Unit,
    onToggleFindReplace: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormatToolBtn(text = "H1", onClick = { onInsertMarkdown("#", "") })
            FormatToolBtn(text = "H2", onClick = { onInsertMarkdown("##", "") })
            FormatToolBtn(text = "H3", onClick = { onInsertMarkdown("###", "") })

            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.size(1.dp, 20.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.width(6.dp))

            FormatIconBtn(icon = Icons.Default.FormatBold, label = "Bold", onClick = { onInsertMarkdown("**", "**") })
            FormatIconBtn(icon = Icons.Default.FormatItalic, label = "Italic", onClick = { onInsertMarkdown("*", "*") })
            FormatIconBtn(icon = Icons.Default.FormatUnderlined, label = "Underline", onClick = { onInsertMarkdown("<u>", "</u>") })
            FormatIconBtn(icon = Icons.Default.StrikethroughS, label = "Strike", onClick = { onInsertMarkdown("~~", "~~") })

            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.size(1.dp, 20.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.width(6.dp))

            FormatIconBtn(icon = Icons.AutoMirrored.Filled.FormatListBulleted, label = "Bullet", onClick = { onInsertMarkdown("*", "") })
            FormatIconBtn(icon = Icons.Default.FormatQuote, label = "Quote", onClick = { onInsertMarkdown(">", "") })
            FormatIconBtn(icon = Icons.Default.Code, label = "Code", onClick = { onInsertMarkdown("```\n", "\n```") })
            FormatIconBtn(icon = Icons.Default.TableChart, label = "Table", onClick = {
                onInsertMarkdown("| Header 1 | Header 2 |\n|---|---|\n| Cell 1 | Cell 2 |", "")
            })
            FormatIconBtn(icon = Icons.Default.FindReplace, label = "Find", onClick = onToggleFindReplace)
        }
    }
}

@Composable
private fun FormatToolBtn(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FormatIconBtn(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun FindReplaceBar(
    findText: String,
    onFindChange: (String) -> Unit,
    replaceText: String,
    onReplaceChange: (String) -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = findText,
                onValueChange = onFindChange,
                placeholder = { Text("Find text...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = replaceText,
                onValueChange = onReplaceChange,
                placeholder = { Text("Replace with...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                onClick = onReplaceAll,
                shape = RoundedCornerShape(6.dp),
                color = ArcPrimary
            ) {
                Text(
                    text = "Replace",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}
