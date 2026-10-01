package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusWarning

@Composable
fun OfflineStatusBanner(
    isOnline: Boolean,
    pendingOfflineMutations: Int,
    offlineDocsCount: Int,
    outboxMailCount: Int,
    onToggleOnline: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isOnline || pendingOfflineMutations > 0,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            color = if (!isOnline) Color(0xFFFFF3CD) else Color(0xFFE8F5E9),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (!isOnline) Color(0xFFFFEEBA) else Color(0xFFC8E6C9)
            ),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isOnline) Color(0xFF856404).copy(alpha = 0.15f) else Color(0xFF2E7D32).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (!isOnline) Icons.Default.WifiOff else Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = if (!isOnline) Color(0xFF856404) else Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (!isOnline) "Offline Mode (Room Caching Active)" else "Back Online",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isOnline) Color(0xFF856404) else Color(0xFF2E7D32)
                        )
                        val details = buildString {
                            if (offlineDocsCount > 0) append("$offlineDocsCount doc${if (offlineDocsCount > 1) "s" else ""} cached")
                            if (offlineDocsCount > 0 && outboxMailCount > 0) append(" • ")
                            if (outboxMailCount > 0) append("$outboxMailCount email${if (outboxMailCount > 1) "s" else ""} in outbox")
                            if (offlineDocsCount == 0 && outboxMailCount == 0) append("Edits & drafts are saved locally in Room SQLite")
                        }
                        Text(
                            text = details,
                            fontSize = 11.sp,
                            color = if (!isOnline) Color(0xFF856404).copy(alpha = 0.9f) else Color(0xFF2E7D32).copy(alpha = 0.9f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isOnline && pendingOfflineMutations > 0) {
                        Button(
                            onClick = onSyncNow,
                            modifier = Modifier.height(30.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onToggleOnline,
                        modifier = Modifier.height(30.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (!isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (!isOnline) "Go Online" else "Go Offline", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
