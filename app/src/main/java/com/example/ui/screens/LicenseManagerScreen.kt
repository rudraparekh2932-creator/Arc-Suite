package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CloudSyncInfo
import com.example.data.model.LicenseState
import com.example.data.model.getTierSpecForTierString
import com.example.ui.components.TierStorageComparisonCard
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.utils.isTablet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseManagerScreen(
    licenseState: LicenseState,
    cloudSyncInfo: CloudSyncInfo,
    hardwareDetails: com.example.data.security.DeviceHardwareDetails? = null,
    onActivateNewKey: (String, String) -> Unit,
    onDeactivateLicense: () -> Unit,
    onNavigateToCloudSync: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var upgradeKeyInput by remember { mutableStateOf("") }
    var upgradeEmailInput by remember { mutableStateOf(licenseState.ownerEmail) }
    var showDeactivateConfirmDialog by remember { mutableStateOf(false) }

    val tabletMode = isTablet()
    val activeTierSpec = getTierSpecForTierString(licenseState.tier)

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
                            text = "Arc 360 License & Device Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Encrypted Hardware-Bound Subscription • ${activeTierSpec.storageDisplay} Cloud Storage",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCloudSync) {
                        Icon(Icons.Default.Sync, contentDescription = "Cloud Sync Center", tint = ArcPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (tabletMode) {
            // Tablet: Responsive 2-Pane Side-by-Side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Active License Card + Upgrade Form + Actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActiveLicenseDetailCard(
                        licenseState = licenseState,
                        cloudSyncInfo = cloudSyncInfo,
                        hardwareDetails = hardwareDetails
                    )

                    UpgradeLicenseCard(
                        keyInput = upgradeKeyInput,
                        onKeyChange = { upgradeKeyInput = it },
                        emailInput = upgradeEmailInput,
                        onEmailChange = { upgradeEmailInput = it },
                        onActivate = {
                            if (upgradeKeyInput.isNotBlank() && upgradeEmailInput.isNotBlank()) {
                                onActivateNewKey(upgradeKeyInput.trim(), upgradeEmailInput.trim())
                                upgradeKeyInput = ""
                            }
                        }
                    )

                    LicenseSecurityActionsCard(
                        onDeactivateClick = { showDeactivateConfirmDialog = true },
                        onNavigateCloud = onNavigateToCloudSync
                    )
                }

                // Right Column: Official 5-Tier Matrix
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TierStorageComparisonCard(
                        currentTier = licenseState.tier,
                        onSelectTier = null
                    )

                    VaultSecurityGuaranteeCard()
                }
            }
        } else {
            // Handheld / Phone Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActiveLicenseDetailCard(
                    licenseState = licenseState,
                    cloudSyncInfo = cloudSyncInfo,
                    hardwareDetails = hardwareDetails
                )

                UpgradeLicenseCard(
                    keyInput = upgradeKeyInput,
                    onKeyChange = { upgradeKeyInput = it },
                    emailInput = upgradeEmailInput,
                    onEmailChange = { upgradeEmailInput = it },
                    onActivate = {
                        if (upgradeKeyInput.isNotBlank() && upgradeEmailInput.isNotBlank()) {
                            onActivateNewKey(upgradeKeyInput.trim(), upgradeEmailInput.trim())
                            upgradeKeyInput = ""
                        }
                    }
                )

                TierStorageComparisonCard(
                    currentTier = licenseState.tier,
                    onSelectTier = null
                )

                LicenseSecurityActionsCard(
                    onDeactivateClick = { showDeactivateConfirmDialog = true },
                    onNavigateCloud = onNavigateToCloudSync
                )

                VaultSecurityGuaranteeCard()
            }
        }
    }

    if (showDeactivateConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateConfirmDialog = false },
            icon = { Icon(Icons.Default.Security, contentDescription = null, tint = StatusError) },
            title = { Text("Deactivate Device License?") },
            text = {
                Text("This will clear the encrypted license key from this device. You will need to enter your product key and email again to access your workspace.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeactivateConfirmDialog = false
                        onDeactivateLicense()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Deactivate License")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ActiveLicenseDetailCard(
    licenseState: LicenseState,
    cloudSyncInfo: CloudSyncInfo,
    hardwareDetails: com.example.data.security.DeviceHardwareDetails? = null
) {
    val tierSpec = getTierSpecForTierString(licenseState.tier)
    val tierColor = Color(tierSpec.primaryColorHex)
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(licenseState.activationDate) {
        if (licenseState.activationDate > 0) dateFormat.format(Date(licenseState.activationDate)) else "Today"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, tierColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(tierColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = tierColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tierSpec.tierName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified & Active on this device",
                                fontSize = 11.sp,
                                color = StatusSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Storage badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tierColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = tierSpec.storageDisplay,
                        color = tierColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "License Account", value = licenseState.ownerEmail.ifBlank { "Personal Account" })
                    DetailRow(label = "Hardware Binding", value = hardwareDetails?.hardwareId ?: licenseState.deviceName)
                    if (hardwareDetails != null) {
                        DetailRow(label = "Device Hardware", value = "${hardwareDetails.brand} ${hardwareDetails.model} (${hardwareDetails.manufacturer})")
                        DetailRow(label = "Board & Arch", value = "${hardwareDetails.board} • ${hardwareDetails.supportedAbis.firstOrNull() ?: "arm64"}")
                        DetailRow(label = "Secure Android ID", value = hardwareDetails.maskedAndroidId)
                    }
                    DetailRow(label = "Device Seats", value = "${licenseState.seatsUsed} Active / ${tierSpec.seatsDescription}")
                    DetailRow(label = "Activation Date", value = formattedDate)
                    DetailRow(label = "Status", value = "Perpetual Enterprise License")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cloud Storage Meter (Accurate & Segmented)
            com.example.ui.components.CompactStorageBar(
                cloudSyncInfo = cloudSyncInfo,
                barColor = tierColor
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun UpgradeLicenseCard(
    keyInput: String,
    onKeyChange: (String) -> Unit,
    emailInput: String,
    onEmailChange: (String) -> Unit,
    onActivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = ArcPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Upgrade / Switch Product Key",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Enter a new Arc 360 license key to expand storage or seats.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = keyInput,
                onValueChange = onKeyChange,
                placeholder = { Text("ARCS-XXXX-XXXX-XXXX-XXXX") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upgrade_key_input"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = onEmailChange,
                label = { Text("License Email") },
                placeholder = { Text("user@company.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onActivate,
                enabled = keyInput.isNotBlank() && emailInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("upgrade_key_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ArcPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify & Activate New Key", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LicenseSecurityActionsCard(
    onDeactivateClick: () -> Unit,
    onNavigateCloud: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Device & Account Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Button(
                onClick = onNavigateCloud,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ArcPrimary.copy(alpha = 0.12f), contentColor = ArcPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Cloud Sync Center", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onDeactivateClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                border = BorderStroke(1.dp, StatusError.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deactivate License on This Device")
            }
        }
    }
}

@Composable
private fun VaultSecurityGuaranteeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = ArcPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Encrypted Hardware Protection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "All license tokens are cryptographically secured using AES-GCM 256-bit encryption. Raw product keys are shielded and never revealed to prevent unauthorized reproduction.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
