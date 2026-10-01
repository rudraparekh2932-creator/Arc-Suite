package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLogItem
import com.example.data.model.CloudSyncInfo
import com.example.ui.components.TierStorageComparisonCard
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorCanvasViolet
import com.example.ui.theme.ColorDocBlue
import com.example.ui.theme.ColorPdfRed
import com.example.ui.theme.ColorSheetGreen
import com.example.ui.theme.ColorSlideAmber
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.utils.isTablet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncCenterScreen(
    cloudSyncInfo: CloudSyncInfo,
    activityLogs: List<ActivityLogItem>,
    onTriggerSync: () -> Unit,
    onToggleOnline: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var isSyncingNow by remember { mutableStateOf(false) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(false) }
    var selectedConflictPolicy by remember { mutableStateOf("Smart Auto-Merge (Differential)") }

    val dateFormat = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()) }
    val tabletMode = isTablet()

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
                        Text(
                            text = "Cloud Sync & Storage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Differential Snapshot Engine • ${cloudSyncInfo.storageDisplayLimit} Allocated",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (tabletMode) {
            // Tablet: Responsive 2-Pane Side-by-Side Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Pane: Status, Quota, & Tier Matrix
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SyncStatusCard(
                        cloudSyncInfo = cloudSyncInfo,
                        dateFormat = dateFormat,
                        isSyncingNow = isSyncingNow,
                        onTriggerSync = {
                            isSyncingNow = true
                            onTriggerSync()
                        },
                        onToggleOnline = onToggleOnline
                    )

                    StorageQuotaMeterCard(cloudSyncInfo = cloudSyncInfo)

                    TierStorageComparisonCard(
                        currentTier = "${cloudSyncInfo.storageQuotaTb} TB"
                    )
                }

                // Right Pane: Preferences & Revision Log
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SyncPreferencesCard(
                        autoSyncEnabled = autoSyncEnabled,
                        onAutoSyncChange = { autoSyncEnabled = it },
                        wifiOnly = wifiOnly,
                        onWifiOnlyChange = { wifiOnly = it },
                        selectedConflictPolicy = selectedConflictPolicy,
                        onPolicySelect = { selectedConflictPolicy = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Cloud Sync & Revision History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activityLogs, key = { it.id }) { log ->
                            ActivityLogItemRow(log = log, dateFormat = dateFormat)
                        }
                    }
                }
            }
        } else {
            // Phone: Single Column Vertical Scroll
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Sync Status Card
                item {
                    SyncStatusCard(
                        cloudSyncInfo = cloudSyncInfo,
                        dateFormat = dateFormat,
                        isSyncingNow = isSyncingNow,
                        onTriggerSync = {
                            isSyncingNow = true
                            onTriggerSync()
                        },
                        onToggleOnline = onToggleOnline
                    )
                }

                // 2. Storage Quota Meter
                item {
                    StorageQuotaMeterCard(cloudSyncInfo = cloudSyncInfo)
                }

                // 3. 4-Tier Cloud Storage Matrix
                item {
                    TierStorageComparisonCard(
                        currentTier = "${cloudSyncInfo.storageQuotaTb} TB"
                    )
                }

                // 4. Sync Settings
                item {
                    SyncPreferencesCard(
                        autoSyncEnabled = autoSyncEnabled,
                        onAutoSyncChange = { autoSyncEnabled = it },
                        wifiOnly = wifiOnly,
                        onWifiOnlyChange = { wifiOnly = it },
                        selectedConflictPolicy = selectedConflictPolicy,
                        onPolicySelect = { selectedConflictPolicy = it }
                    )
                }

                // 5. Sync & Revision Log Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Cloud Sync & Revision History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                items(activityLogs, key = { it.id }) { log ->
                    ActivityLogItemRow(log = log, dateFormat = dateFormat)
                }
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    cloudSyncInfo: CloudSyncInfo,
    dateFormat: SimpleDateFormat,
    isSyncingNow: Boolean,
    onTriggerSync: () -> Unit,
    onToggleOnline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (cloudSyncInfo.isOnline) StatusSuccess else StatusWarning)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (cloudSyncInfo.isOnline) "Connected to Arc Cloud" else "Offline Cache Mode",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "Last synced: ${dateFormat.format(Date(cloudSyncInfo.lastSyncTime))}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = cloudSyncInfo.syncStatusText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTriggerSync,
                    enabled = cloudSyncInfo.isOnline && !isSyncingNow,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("force_sync_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync Now")
                }

                Button(
                    onClick = onToggleOnline,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_offline_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cloudSyncInfo.isOnline) ColorSlideAmber else StatusSuccess
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (cloudSyncInfo.isOnline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (cloudSyncInfo.isOnline) "Go Offline" else "Go Online")
                }
            }
        }
    }
}

@Composable
private fun StorageQuotaMeterCard(cloudSyncInfo: CloudSyncInfo) {
    com.example.ui.components.CloudStorageCard(cloudSyncInfo = cloudSyncInfo)
}

@Composable
private fun SyncPreferencesCard(
    autoSyncEnabled: Boolean,
    onAutoSyncChange: (Boolean) -> Unit,
    wifiOnly: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    selectedConflictPolicy: String,
    onPolicySelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Synchronization Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Background Auto-Sync", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = "Sync mutations continuously in real-time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = onAutoSyncChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = ArcPrimary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Wi-Fi Only Sync", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = "Conserve mobile cellular data", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = wifiOnly,
                    onCheckedChange = onWifiOnlyChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = ArcPrimary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Conflict Resolution Policy:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            listOf("Smart Auto-Merge (Differential)", "Server Wins (Authoritative)", "Local Wins (Latest Device)").forEach { policy ->
                Surface(
                    onClick = { onPolicySelect(policy) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedConflictPolicy == policy) ArcPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (selectedConflictPolicy == policy) ArcPrimary else Color.Transparent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = policy,
                            fontSize = 12.sp,
                            fontWeight = if (selectedConflictPolicy == policy) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLogItemRow(log: ActivityLogItem, dateFormat: SimpleDateFormat) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ArcPrimary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.actionText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = "By ${log.authorName} • ${dateFormat.format(Date(log.timestamp))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StorageModulePill(label: String, color: Color, size: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = size, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
