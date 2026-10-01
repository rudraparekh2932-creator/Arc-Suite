package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ColorAdobePr
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class PrClip(
    val id: String,
    var title: String,
    var startSec: Float,
    var durationSec: Float,
    val color: Color,
    var transition: String = "Cut" // Cut, Cross Dissolve, Dip to Black
)

data class PrTrack(
    val id: String,
    val name: String,
    val isVideo: Boolean,
    val clips: MutableList<PrClip>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiereEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }

    var isPlaying by remember { mutableStateOf(false) }
    var playheadTime by remember { mutableFloatStateOf(0.0f) }
    val totalDuration = 32.0f
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var activeTool by remember { mutableStateOf("SELECT") } // SELECT, RAZOR, TRANSITION

    // Tracks: V2 (Overlays), V1 (Main Footage), A1 (Dialogue), A2 (Music)
    val tracks = remember(document.id) {
        mutableStateListOf(
            PrTrack(
                "t1", "V2 • Titles & Graphics", isVideo = true,
                mutableListOf(
                    PrClip("c1", "Title Card 3D", 0.0f, 4.5f, Color(0xFF9999FF), "Dip to Black"),
                    PrClip("c2", "Lower Third Info", 14.0f, 6.0f, Color(0xFF38BDF8), "Cross Dissolve")
                )
            ),
            PrTrack(
                "t2", "V1 • Master Camera 4K", isVideo = true,
                mutableListOf(
                    PrClip("c3", "Scene 01 Setup", 0.0f, 12.0f, Color(0xFF6366F1)),
                    PrClip("c4", "Scene 02 Close-Up", 12.0f, 10.0f, Color(0xFF8B5CF6), "Cross Dissolve"),
                    PrClip("c5", "Scene 03 Action Cut", 22.0f, 10.0f, Color(0xFFA78BFA), "Cross Dissolve")
                )
            ),
            PrTrack(
                "t3", "A1 • Boom Mic Audio", isVideo = false,
                mutableListOf(
                    PrClip("c6", "Voice Dialogue Track", 0.0f, 30.0f, Color(0xFF10B981))
                )
            ),
            PrTrack(
                "t4", "A2 • Soundtrack Score", isVideo = false,
                mutableListOf(
                    PrClip("c7", "Cinematic Strings Loop", 2.0f, 28.0f, Color(0xFFF59E0B))
                )
            )
        )
    }

    // Playback loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(50)
            playheadTime += 0.05f * playbackSpeed
            if (playheadTime >= totalDuration) {
                playheadTime = 0.0f
                isPlaying = false
            }
        }
    }

    fun savePremiereDoc() {
        val root = JSONObject()
        root.put("duration", totalDuration.toDouble())
        root.put("playhead", playheadTime.toDouble())
        root.put("speed", playbackSpeed.toDouble())
        onSaveContent(root.toString(), title)
    }

    // Active Clip Under Playhead on Track V1
    val currentMasterClip = remember(playheadTime) {
        tracks[1].clips.find { playheadTime >= it.startSec && playheadTime <= (it.startSec + it.durationSec) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2A0845),
                            border = BorderStroke(1.5.dp, ColorAdobePr),
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Text(
                                text = "Pr",
                                color = ColorAdobePr,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Column {
                            TextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    savePremiereDoc()
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            Text(
                                text = "Arc Premiere Sequence • 3840x2160 (4K 60fps)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { savePremiereDoc() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Project", tint = ColorAdobePr)
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
            // Program Monitor (Video Playback Preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF090D16)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Simulated video footage rendering based on active clip & playhead
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val clipColor = currentMasterClip?.color ?: Color(0xFF312E81)
                            drawRect(color = clipColor.copy(alpha = 0.85f))

                            // Draw cinematic scene silhouette
                            val offsetRatio = (playheadTime / totalDuration)
                            drawCircle(
                                color = Color.White.copy(alpha = 0.9f),
                                radius = 40f,
                                center = Offset(w * (0.2f + offsetRatio * 0.6f), h * 0.45f)
                            )
                        }

                        // Video Title HUD Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = currentMasterClip?.title ?: "No Media",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "TC: ${String.format(Locale.US, "00:%02d.%03d", playheadTime.toInt(), ((playheadTime % 1f) * 1000).toInt())}",
                                color = ColorAdobePr,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Top Right 4K badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "4K DCI • Rec.709",
                                color = Color.White,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Transport Controls Bar (Play, Pause, Fast-Forward, Speed)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { playheadTime = 0.0f }) {
                            Icon(Icons.Default.Stop, contentDescription = "Rewind to Start")
                        }
                        IconButton(onClick = { playheadTime = (playheadTime - 5.0f).coerceAtLeast(0f) }) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Skip -5s")
                        }
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ColorAdobePr)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { playheadTime = (playheadTime + 5.0f).coerceAtMost(totalDuration) }) {
                            Icon(Icons.Default.FastForward, contentDescription = "Skip +5s")
                        }
                    }

                    // Tool selector chips: Select, Razor (Cut), Transitions
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PrToolBtn("SELECT", Icons.Default.Movie, activeTool == "SELECT") { activeTool = "SELECT" }
                        PrToolBtn("RAZOR", Icons.Default.ContentCut, activeTool == "RAZOR") { activeTool = "RAZOR" }
                        PrToolBtn("TRANS", Icons.Default.Transform, activeTool == "TRANS") { activeTool = "TRANS" }
                    }
                }
            }

            // Time Scrubber Slider
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", playheadTime)}s",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAdobePr
                    )
                    Slider(
                        value = playheadTime,
                        onValueChange = {
                            playheadTime = it
                            savePremiereDoc()
                        },
                        valueRange = 0.0f..totalDuration,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = ColorAdobePr, activeTrackColor = ColorAdobePr)
                    )
                    Text(
                        text = "${totalDuration.toInt()}s",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            // Non-Linear Multi-Track Timeline (V2, V1, A1, A2)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .width(720.dp)
                        .padding(8.dp)
                ) {
                    // Time Ruler (0s, 5s, 10s, 15s, 20s, 25s, 30s)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(4.dp)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0, 5, 10, 15, 20, 25, 30).forEach { s ->
                            Text(
                                text = "${s}s",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Video Tracks & Audio Tracks
                    tracks.forEach { track ->
                        Text(
                            text = track.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (track.isVideo) ColorAdobePr else Color(0xFF34D399),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        // Track Strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(Color(0xFF1E293B).copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(2.dp)
                        ) {
                            track.clips.forEach { clip ->
                                val leftOffset = 700.dp * (clip.startSec / totalDuration)
                                val clipWidth = 700.dp * (clip.durationSec / totalDuration)

                                Surface(
                                    onClick = {
                                        if (activeTool == "RAZOR") {
                                            // Split clip at playhead
                                            val splitPoint = playheadTime - clip.startSec
                                            if (splitPoint > 1f && splitPoint < clip.durationSec - 1f) {
                                                val oldDuration = clip.durationSec
                                                clip.durationSec = splitPoint
                                                track.clips.add(
                                                    PrClip(
                                                        id = "split_${System.currentTimeMillis()}",
                                                        title = "${clip.title} (Part 2)",
                                                        startSec = playheadTime,
                                                        durationSec = oldDuration - splitPoint,
                                                        color = clip.color
                                                    )
                                                )
                                                savePremiereDoc()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    color = clip.color.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .padding(start = leftOffset)
                                        .width(clipWidth.coerceAtLeast(30.dp))
                                        .height(34.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (track.isVideo) Icons.Default.Videocam else Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = clip.title,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Playhead line marker
                            val playheadOffset = 700.dp * (playheadTime / totalDuration)
                            Box(
                                modifier = Modifier
                                    .padding(start = playheadOffset)
                                    .width(2.dp)
                                    .height(34.dp)
                                    .background(Color.Red)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PrToolBtn(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) ColorAdobePr.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) ColorAdobePr else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ColorAdobePr else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) ColorAdobePr else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
