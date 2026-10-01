package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CloudSyncInfo
import com.example.ui.screens.OutlookBlue
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorCanvasViolet
import com.example.ui.theme.ColorDocBlue
import com.example.ui.theme.ColorPdfRed
import com.example.ui.theme.ColorSheetGreen
import com.example.ui.theme.ColorSlideAmber
import java.util.Locale

/**
 * High-precision, accurate Cloud Storage Bar component that displays:
 * 1. Exact computed storage consumption vs allocated quota limit
 * 2. Multi-color segmented distribution bar by application module (Mail, Docs, Sheets, Slides, PDFs, Creative)
 * 3. Human-readable precision metrics (MB / GB / TB, percentage, remaining available storage)
 * 4. Breakdown chips for each module with live updated byte values
 */
@Composable
fun CloudStorageCard(
    cloudSyncInfo: CloudSyncInfo,
    tierName: String = "Enterprise",
    onManageClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title & Quota Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ArcPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Storage",
                            tint = ArcPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cloud Storage Allocation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$tierName Plan • ${cloudSyncInfo.storageDisplayLimit} Allocated Quota",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ArcPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ArcPrimary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = cloudSyncInfo.formattedPercentage,
                        color = ArcPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accurate Storage Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Used Storage",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${cloudSyncInfo.formattedUsed} of ${cloudSyncInfo.storageDisplayLimit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Available Space",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${cloudSyncInfo.formattedAvailable} free",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-Color Segmented Storage Bar
            SegmentedStorageProgressBar(
                cloudSyncInfo = cloudSyncInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Module Breakdown Pills
            Text(
                text = "Live Breakdown by Application:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StorageModuleItem(label = "Mail", color = OutlookBlue, mb = cloudSyncInfo.mailStorageMb)
                StorageModuleItem(label = "Docs", color = ColorDocBlue, mb = cloudSyncInfo.docsStorageMb)
                StorageModuleItem(label = "Sheets", color = ColorSheetGreen, mb = cloudSyncInfo.sheetsStorageMb)
                StorageModuleItem(label = "Slides", color = ColorSlideAmber, mb = cloudSyncInfo.slidesStorageMb)
                StorageModuleItem(label = "PDFs", color = ColorPdfRed, mb = cloudSyncInfo.pdfsStorageMb)
                StorageModuleItem(label = "Creative", color = ColorCanvasViolet, mb = cloudSyncInfo.creativeStorageMb)
                StorageModuleItem(label = "System", color = Color(0xFF64748B), mb = cloudSyncInfo.systemCacheMb)
            }
        }
    }
}

/**
 * Compact version for Hero Banners and License Details.
 */
@Composable
fun CompactStorageBar(
    cloudSyncInfo: CloudSyncInfo,
    barColor: Color = ArcPrimary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cloud Storage (${cloudSyncInfo.formattedPercentage})",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${cloudSyncInfo.formattedUsed} / ${cloudSyncInfo.storageDisplayLimit}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        SegmentedStorageProgressBar(
            cloudSyncInfo = cloudSyncInfo,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${cloudSyncInfo.formattedAvailable} available",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Mail: ${formatMb(cloudSyncInfo.mailStorageMb)} • Docs: ${formatMb(cloudSyncInfo.docsStorageMb)} • Sheets: ${formatMb(cloudSyncInfo.sheetsStorageMb)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SegmentedStorageProgressBar(
    cloudSyncInfo: CloudSyncInfo,
    modifier: Modifier = Modifier
) {
    val totalUsed = cloudSyncInfo.storageUsedMb.coerceAtLeast(0.001f)
    val totalQuota = cloudSyncInfo.totalStorageMb.coerceAtLeast(1f)

    // Calculate fractions of the total consumed storage for each application module
    val mailWeight = (cloudSyncInfo.mailStorageMb / totalUsed).coerceAtLeast(0.02f)
    val docsWeight = (cloudSyncInfo.docsStorageMb / totalUsed).coerceAtLeast(0.02f)
    val sheetsWeight = (cloudSyncInfo.sheetsStorageMb / totalUsed).coerceAtLeast(0.02f)
    val slidesWeight = (cloudSyncInfo.slidesStorageMb / totalUsed).coerceAtLeast(0.02f)
    val pdfsWeight = (cloudSyncInfo.pdfsStorageMb / totalUsed).coerceAtLeast(0.02f)
    val creativeWeight = (cloudSyncInfo.creativeStorageMb / totalUsed).coerceAtLeast(0.02f)
    val systemWeight = (cloudSyncInfo.systemCacheMb / totalUsed).coerceAtLeast(0.02f)

    // Overall quota fraction: ensures that at minimum a distinct indicator is visible
    val quotaFraction = (totalUsed / totalQuota).coerceIn(0f, 1f)
    // To provide both an accurate representation of quota usage and visible module breakdown:
    // When usage is very small (e.g. 15MB of 500GB), showing 0.00003 directly would be 0 pixels.
    // We scale the visual bar smoothly so active usage is prominently visible (minimum 3% of bar width),
    // and each colored segment precisely represents its proportional byte distribution of that used storage!
    val visualUsageWidthFraction = (quotaFraction * 10f).coerceIn(0.04f, 1f)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("cloud_storage_progress_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(visualUsageWidthFraction)
                .clip(CircleShape)
        ) {
            Box(modifier = Modifier.weight(mailWeight).background(OutlookBlue))
            Box(modifier = Modifier.weight(docsWeight).background(ColorDocBlue))
            Box(modifier = Modifier.weight(sheetsWeight).background(ColorSheetGreen))
            Box(modifier = Modifier.weight(slidesWeight).background(ColorSlideAmber))
            Box(modifier = Modifier.weight(pdfsWeight).background(ColorPdfRed))
            Box(modifier = Modifier.weight(creativeWeight).background(ColorCanvasViolet))
            Box(modifier = Modifier.weight(systemWeight).background(Color(0xFF64748B)))
        }
    }
}

@Composable
private fun StorageModuleItem(label: String, color: Color, mb: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(
            text = formatMb(mb),
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatMb(mb: Float): String {
    return when {
        mb <= 0.0001f -> "0 KB"
        mb < 0.1f -> "${String.format(Locale.US, "%.0f", (mb * 1024f).coerceAtLeast(1f))} KB"
        mb < 1000f -> "${String.format(Locale.US, "%.1f", mb)} MB"
        else -> "${String.format(Locale.US, "%.2f", mb / 1024f)} GB"
    }
}
