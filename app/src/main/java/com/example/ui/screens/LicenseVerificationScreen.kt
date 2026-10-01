package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.getTierSpecForTierString
import com.example.data.security.VerificationResult
import com.example.ui.components.TierStorageComparisonCard
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.utils.isTablet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseVerificationScreen(
    isVerifying: Boolean,
    verificationError: String?,
    verificationSuccess: VerificationResult.Success?,
    onVerifyKey: (String, String) -> Unit,
    onSuccessContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val clipboardManager = LocalClipboardManager.current
    var productKeyInput by remember { mutableStateOf("") }
    var customEmailInput by remember { mutableStateOf("") }

    val tabletMode = isTablet()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArcPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Arc 360 Device Activation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cryptographic License & Storage Provisioning",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (tabletMode) {
            // Tablet: Responsive 2-Column Side-by-Side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Key & Custom Email Input Form + Status
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VerificationHeaderSection()

                    Spacer(modifier = Modifier.height(16.dp))

                    KeyInputCard(
                        productKeyInput = productKeyInput,
                        onKeyChange = { productKeyInput = it },
                        customEmailInput = customEmailInput,
                        onEmailChange = { customEmailInput = it },
                        onPaste = {
                            val clip = clipboardManager.getText()
                            if (clip != null) {
                                productKeyInput = clip.text.trim().uppercase()
                            }
                        },
                        isVerifying = isVerifying,
                        onVerify = {
                            onVerifyKey(productKeyInput.trim(), customEmailInput.trim())
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimatedVisibility(visible = verificationError != null) {
                        VerificationErrorBanner(error = verificationError)
                    }

                    AnimatedVisibility(visible = verificationSuccess != null) {
                        VerificationSuccessBanner(
                            success = verificationSuccess,
                            onContinue = onSuccessContinue
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Right Column: Official 5-Tier Overview
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TierStorageComparisonCard(
                        currentTier = verificationSuccess?.tier ?: "",
                        onSelectTier = null
                    )

                    SecurityNoticeCard()
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerificationHeaderSection()

                Spacer(modifier = Modifier.height(16.dp))

                KeyInputCard(
                    productKeyInput = productKeyInput,
                    onKeyChange = { productKeyInput = it },
                    customEmailInput = customEmailInput,
                    onEmailChange = { customEmailInput = it },
                    onPaste = {
                        val clip = clipboardManager.getText()
                        if (clip != null) {
                            productKeyInput = clip.text.trim().uppercase()
                        }
                    },
                    isVerifying = isVerifying,
                    onVerify = {
                        onVerifyKey(productKeyInput.trim(), customEmailInput.trim())
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(visible = verificationError != null) {
                    VerificationErrorBanner(error = verificationError)
                }

                AnimatedVisibility(visible = verificationSuccess != null) {
                    VerificationSuccessBanner(
                        success = verificationSuccess,
                        onContinue = onSuccessContinue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TierStorageComparisonCard(
                    currentTier = verificationSuccess?.tier ?: "",
                    onSelectTier = null
                )

                Spacer(modifier = Modifier.height(14.dp))

                SecurityNoticeCard()
            }
        }
    }
}

@Composable
private fun VerificationHeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ArcPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = ArcPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Activate Arc 360 Workspace",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter your 20-character Arc 360 Product Key and custom email to bind this device to your cloud storage quota.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun KeyInputCard(
    productKeyInput: String,
    onKeyChange: (String) -> Unit,
    customEmailInput: String,
    onEmailChange: (String) -> Unit,
    onPaste: () -> Unit,
    isVerifying: Boolean,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Key header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = ArcPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ARC 360 PRODUCT KEY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    onClick = onPaste,
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste Key", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = productKeyInput,
                onValueChange = onKeyChange,
                placeholder = { Text("ARCS-XXXX-XXXX-XXXX-XXXX") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verification_key_input"),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Email Field
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = ArcPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CUSTOM EMAIL ADDRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customEmailInput,
                onValueChange = onEmailChange,
                placeholder = { Text("e.g. user@company.com or personal@gmail.com") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verification_email_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            val canSubmit = productKeyInput.isNotBlank() && customEmailInput.isNotBlank() && !isVerifying

            Button(
                onClick = onVerify,
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ArcPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Verifying Hardware Cryptographic Vault...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify & Unlock Arc 360", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun VerificationErrorBanner(error: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, StatusError.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = StatusError,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Verification Failed",
                    fontWeight = FontWeight.Bold,
                    color = StatusError,
                    fontSize = 13.sp
                )
                Text(
                    text = error ?: "Please verify your key and email address.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun VerificationSuccessBanner(
    success: VerificationResult.Success?,
    onContinue: () -> Unit
) {
    val spec = getTierSpecForTierString(success?.tier ?: "")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.12f)),
        border = BorderStroke(1.5.dp, StatusSuccess.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Arc 360 License Activated!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = StatusSuccess
                    )
                    Text(
                        text = "Device registered to ${success?.ownerEmail}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Tier: ${success?.tier}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ArcPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Storage Quota: ${spec.storageDisplay} Cloud Storage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = StatusSuccess
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Account: ${success?.ownerEmail}",
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Token: ${success?.activationToken}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Open Arc 360 Workspace →", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SecurityNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = ArcPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Zero-Knowledge Hardware Vault",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Your product key is cryptographically signed and stored in device-bound encrypted hardware storage. Keys are confidential and never exposed in the interface.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
