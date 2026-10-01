package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CloudSyncInfo
import com.example.data.model.Collaborator
import com.example.data.model.LicenseState
import com.example.data.model.getTierSpecForTierString
import com.example.ui.screens.OutlookBlue
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcTopAppBar(
    title: String = "Arc 360",
    subtitle: String? = "Unified Cloud Workspace",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    cloudSyncInfo: CloudSyncInfo,
    licenseState: LicenseState,
    collaborators: List<Collaborator>,
    unreadMailCount: Int = 0,
    onMailClick: () -> Unit = {},
    onCloudClick: () -> Unit,
    onCollabClick: () -> Unit,
    onLicenseClick: () -> Unit
) {
    val tierSpec = getTierSpecForTierString(licenseState.tier)

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ArcPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Text(
                        text = "360",
                        color = ArcPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            // Arc 360 Mail button
            Surface(
                onClick = onMailClick,
                shape = RoundedCornerShape(16.dp),
                color = OutlookBlue.copy(alpha = 0.12f),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Arc Mail",
                        tint = OutlookBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mail",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OutlookBlue
                    )
                    if (unreadMailCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = OutlookBlue,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$unreadMailCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Cloud Sync Indicator Pill
            Surface(
                onClick = onCloudClick,
                shape = RoundedCornerShape(16.dp),
                color = if (cloudSyncInfo.isOnline) StatusSuccess.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (cloudSyncInfo.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = "Cloud Status",
                        tint = if (cloudSyncInfo.isOnline) StatusSuccess else StatusWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (cloudSyncInfo.isOnline) "Synced" else "Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (cloudSyncInfo.isOnline) StatusSuccess else StatusWarning
                    )
                }
            }

            // License Storage Pill
            Surface(
                onClick = onLicenseClick,
                shape = RoundedCornerShape(16.dp),
                color = ArcPrimary.copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "License",
                        tint = ArcPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tierSpec.storageDisplay,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
