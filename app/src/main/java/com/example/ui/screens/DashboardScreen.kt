package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.CloudSyncInfo
import com.example.data.model.Collaborator
import com.example.data.model.DocType
import com.example.data.model.DocumentEntity
import com.example.data.model.LicenseState
import com.example.data.model.getTierSpecForTierString
import com.example.ui.components.ArcTopAppBar
import com.example.ui.components.CreateDocumentDialog
import com.example.ui.components.TierStorageComparisonCard
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorCanvasViolet
import com.example.ui.theme.ColorDocBlue
import com.example.ui.theme.ColorPdfRed
import com.example.ui.theme.ColorSheetGreen
import com.example.ui.theme.ColorSlideAmber
import com.example.ui.theme.StatusSuccess
import com.example.ui.utils.isTablet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    documents: List<DocumentEntity>,
    cloudSyncInfo: CloudSyncInfo,
    licenseState: LicenseState,
    collaborators: List<Collaborator>,
    unreadMailCount: Int = 0,
    selectedTypeFilter: DocType?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (DocType?) -> Unit,
    onOpenDoc: (DocumentEntity) -> Unit,
    onCreateDoc: (DocType, String) -> Unit,
    onToggleStar: (DocumentEntity) -> Unit,
    onDeleteDoc: (DocumentEntity) -> Unit,
    onOpenMail: () -> Unit = {},
    onOpenCloudSync: () -> Unit,
    onOpenCollabHub: () -> Unit,
    onOpenLicenseManager: () -> Unit,
    pendingOfflineMutations: Int = 0,
    offlineDocsCount: Int = 0,
    outboxMailCount: Int = 0,
    onToggleOnline: () -> Unit = {},
    onSyncNow: () -> Unit = {},
    onOpenGeminiChat: () -> Unit = {}
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var createInitialType by remember { mutableStateOf(DocType.DOC) }

    val tabletMode = isTablet()

    Scaffold(
        topBar = {
            ArcTopAppBar(
                title = "Arc 360",
                subtitle = "Unified Cloud Workspace",
                cloudSyncInfo = cloudSyncInfo,
                licenseState = licenseState,
                collaborators = collaborators,
                unreadMailCount = unreadMailCount,
                onMailClick = onOpenMail,
                onCloudClick = onOpenCloudSync,
                onCollabClick = onOpenCollabHub,
                onLicenseClick = onOpenLicenseManager
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FloatingActionButton(
                    onClick = onOpenGeminiChat,
                    containerColor = Color(0xFF673AB7),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("gemini_copilot_fab")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Arc Copilot (Gemini AI)")
                }

                FloatingActionButton(
                    onClick = {
                        createInitialType = selectedTypeFilter ?: DocType.DOC
                        showCreateDialog = true
                    },
                    containerColor = ArcPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_document_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Document")
                }
            }
        }
    ) { paddingValues ->
        if (tabletMode) {
            // Tablet: Responsive 2-Pane Split Workspace
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Left Pane: Hero Banner, Tier Comparison, Storage Details
                Column(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    com.example.ui.components.OfflineStatusBanner(
                        isOnline = cloudSyncInfo.isOnline,
                        pendingOfflineMutations = pendingOfflineMutations,
                        offlineDocsCount = offlineDocsCount,
                        outboxMailCount = outboxMailCount,
                        onToggleOnline = onToggleOnline,
                        onSyncNow = onSyncNow
                    )

                    SuiteHeroBanner(
                        licenseState = licenseState,
                        cloudSyncInfo = cloudSyncInfo,
                        docCount = documents.size,
                        onOpenLicense = onOpenLicenseManager,
                        onOpenCloud = onOpenCloudSync
                    )

                    TierStorageComparisonCard(
                        currentTier = licenseState.tier,
                        onSelectTier = { onOpenLicenseManager() }
                    )
                }

                // Right Pane: Launchers, Search, Filter & Adaptive Document Grid
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(top = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    SuiteModuleLaunchRow(
                        onLaunch = { type ->
                            createInitialType = type
                            showCreateDialog = true
                        },
                        onOpenMail = onOpenMail
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_search_input"),
                        placeholder = { Text("Search files, formulas, slides, PDFs...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FilterChipRow(
                        selectedType = selectedTypeFilter,
                        onSelect = onFilterSelect
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTypeFilter != null) "${selectedTypeFilter.displayName}s" else "Recent Files",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${documents.size} items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (documents.isEmpty()) {
                        EmptyDocumentsPlaceholder(
                            searchQuery = searchQuery,
                            selectedType = selectedTypeFilter,
                            onCreate = {
                                createInitialType = selectedTypeFilter ?: DocType.DOC
                                showCreateDialog = true
                            }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 280.dp),
                            contentPadding = PaddingValues(bottom = 70.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(documents, key = { it.id }) { doc ->
                                DocumentCard(
                                    doc = doc,
                                    onClick = { onOpenDoc(doc) },
                                    onToggleStar = { onToggleStar(doc) },
                                    onDelete = { onDeleteDoc(doc) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Handheld / Phone Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    com.example.ui.components.OfflineStatusBanner(
                        isOnline = cloudSyncInfo.isOnline,
                        pendingOfflineMutations = pendingOfflineMutations,
                        offlineDocsCount = offlineDocsCount,
                        outboxMailCount = outboxMailCount,
                        onToggleOnline = onToggleOnline,
                        onSyncNow = onSyncNow
                    )
                }

                // 1. Suite Quick Stats / Hero Banner
                item {
                    SuiteHeroBanner(
                        licenseState = licenseState,
                        cloudSyncInfo = cloudSyncInfo,
                        docCount = documents.size,
                        onOpenLicense = onOpenLicenseManager,
                        onOpenCloud = onOpenCloudSync
                    )
                }

                // 2. Suite Module Quick Launchers
                item {
                    SuiteModuleLaunchRow(
                        onLaunch = { type ->
                            createInitialType = type
                            showCreateDialog = true
                        },
                        onOpenMail = onOpenMail
                    )
                }

                // 3. Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("dashboard_search_input"),
                        placeholder = { Text("Search files, formulas, slides, PDFs...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }

                // 4. Module Filter Chips
                item {
                    FilterChipRow(
                        selectedType = selectedTypeFilter,
                        onSelect = onFilterSelect
                    )
                }

                // 5. Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTypeFilter != null) "${selectedTypeFilter.displayName}s" else "Recent Files",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${documents.size} items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 6. Documents List or Empty State
                if (documents.isEmpty()) {
                    item {
                        EmptyDocumentsPlaceholder(
                            searchQuery = searchQuery,
                            selectedType = selectedTypeFilter,
                            onCreate = {
                                createInitialType = selectedTypeFilter ?: DocType.DOC
                                showCreateDialog = true
                            }
                        )
                    }
                } else {
                    items(documents, key = { it.id }) { doc ->
                        DocumentCard(
                            doc = doc,
                            onClick = { onOpenDoc(doc) },
                            onToggleStar = { onToggleStar(doc) },
                            onDelete = { onDeleteDoc(doc) }
                        )
                    }
                }

                // 7. Tier Storage Comparison Card
                item {
                    Box(modifier = Modifier.padding(16.dp)) {
                        TierStorageComparisonCard(
                            currentTier = licenseState.tier,
                            onSelectTier = { onOpenLicenseManager() }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDocumentDialog(
            initialType = createInitialType,
            onDismiss = { showCreateDialog = false },
            onCreate = { type, title ->
                showCreateDialog = false
                onCreateDoc(type, title)
            }
        )
    }
}

@Composable
private fun SuiteHeroBanner(
    licenseState: LicenseState,
    cloudSyncInfo: CloudSyncInfo,
    docCount: Int,
    onOpenLicense: () -> Unit,
    onOpenCloud: () -> Unit
) {
    val tierSpec = getTierSpecForTierString(licenseState.tier)
    val tierColor = Color(tierSpec.primaryColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Arc 360 Workspace",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = licenseState.ownerEmail.ifBlank { "Personal Account" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tierSpec.tierName,
                        fontSize = 12.sp,
                        color = tierColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tierColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${tierSpec.storageDisplay} CLOUD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        onClick = onOpenLicense,
                        shape = RoundedCornerShape(8.dp),
                        color = StatusSuccess.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Licensed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cloud Storage Progress Bar (Accurate & Dynamic)
            com.example.ui.components.CompactStorageBar(
                cloudSyncInfo = cloudSyncInfo,
                barColor = tierColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (cloudSyncInfo.isOnline) "● Real-time peer sync active" else "○ Working offline (queued)",
                    fontSize = 11.sp,
                    color = if (cloudSyncInfo.isOnline) StatusSuccess else ColorSlideAmber,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Manage Cloud →",
                    fontSize = 11.sp,
                    color = ArcPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenCloud() }
                )
            }
        }
    }
}

@Composable
private fun SuiteModuleLaunchRow(
    onLaunch: (DocType) -> Unit,
    onOpenMail: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Arc 360 Suite Applications",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Office, Creative & Mail",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ArcPrimary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Arc 360 Mail (Outlook Alternative) First!
            item {
                AdobeLaunchButton("Ol", "Arc Mail", OutlookBlue) { onOpenMail() }
            }
            // Office Suite
            item {
                AdobeLaunchButton("Wd", "Word Docs", ColorDocBlue) { onLaunch(DocType.DOC) }
            }
            item {
                AdobeLaunchButton("Xl", "Excel Sheets", ColorSheetGreen) { onLaunch(DocType.SHEET) }
            }
            item {
                AdobeLaunchButton("Pt", "PowerPoint", ColorSlideAmber) { onLaunch(DocType.SLIDE) }
            }
            item {
                AdobeLaunchButton("Ac", "Acrobat PDF", ColorPdfRed) { onLaunch(DocType.PDF) }
            }
            // Creative Cloud
            item {
                AdobeLaunchButton("Ps", "Photoshop", Color(0xFF31A8FF)) { onLaunch(DocType.PHOTOSHOP) }
            }
            item {
                AdobeLaunchButton("Pr", "Premiere", Color(0xFF9999FF)) { onLaunch(DocType.PREMIERE) }
            }
            item {
                AdobeLaunchButton("Id", "InDesign", Color(0xFFFF3366)) { onLaunch(DocType.INDESIGN) }
            }
            item {
                AdobeLaunchButton("Ae", "After Effects", Color(0xFF818CF8)) { onLaunch(DocType.AFTER_EFFECTS) }
            }
            item {
                AdobeLaunchButton("Ai", "Illustrator", ColorCanvasViolet) { onLaunch(DocType.CANVAS) }
            }
        }
    }
}

@Composable
private fun AdobeLaunchButton(
    badge: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, color),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = badge,
                    color = color,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun FilterChipRow(
    selectedType: DocType?,
    onSelect: (DocType?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChipItem(
                label = "All Files",
                isSelected = selectedType == null,
                color = ArcPrimary,
                onClick = { onSelect(null) }
            )
        }
        items(DocType.entries.toTypedArray()) { type ->
            FilterChipItem(
                label = type.displayName,
                isSelected = selectedType == type,
                color = type.color,
                onClick = { onSelect(type) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DocumentCard(
    doc: DocumentEntity,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Card(
        onClick = onClick,
        modifier = modifier
            .testTag("doc_card_${doc.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = doc.type.color.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = doc.type.label.take(2).uppercase(),
                        color = doc.type.color,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = doc.previewSnippet,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateFormat.format(Date(doc.lastModified)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Sync",
                        tint = if (doc.syncStatus == "SYNCED") StatusSuccess else ColorSlideAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (doc.syncStatus == "SYNCED") "Synced to Cloud" else "Queued local",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onToggleStar) {
                Icon(
                    imageVector = if (doc.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star",
                    tint = if (doc.isStarred) ColorSlideAmber else MaterialTheme.colorScheme.outline
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = ColorPdfRed) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = ColorPdfRed)
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDocumentsPlaceholder(
    searchQuery: String,
    selectedType: DocType?,
    onCreate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) "No files found for '$searchQuery'" else "Welcome to Arc 360",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) "Try a different search query." else "You have a clean workspace with no documents yet. Create your first doc, spreadsheet, slide deck, or check your Arc Mail.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            onClick = onCreate,
            shape = RoundedCornerShape(8.dp),
            color = ArcPrimary
        ) {
            Text(
                text = "+ Create Document",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
