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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideogameAsset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ColorAdobeAe
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.util.Locale

enum class AeEffect {
    NEON_GLOW,
    PARTICLES,
    LENS_FLARE,
    GLITCH_MATRIX,
    SHOCKWAVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AfterEffectsEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }

    // Motion State
    var isPlaying by remember { mutableStateOf(true) }
    var animTime by remember { mutableFloatStateOf(0.0f) }
    val compDuration = 10.0f

    // Transform parameters
    var scaleAnim by remember { mutableFloatStateOf(100f) } // 50% to 200%
    var rotationAnim by remember { mutableFloatStateOf(0f) } // 0 to 360 deg
    var opacityAnim by remember { mutableFloatStateOf(100f) } // 0 to 100%
    var activeEffect by remember { mutableStateOf(AeEffect.NEON_GLOW) }

    // Continuous playback loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(33) // ~30 fps
            animTime += 0.033f
            if (animTime >= compDuration) {
                animTime = 0.0f
            }
        }
    }

    fun saveAeDoc() {
        val root = JSONObject()
        root.put("fps", 30)
        root.put("duration", compDuration.toDouble())
        root.put("effect", activeEffect.name)
        root.put("scale", scaleAnim.toDouble())
        root.put("rotation", rotationAnim.toDouble())
        root.put("opacity", opacityAnim.toDouble())
        onSaveContent(root.toString(), title)
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
                            color = Color(0xFF1E0836),
                            border = BorderStroke(1.5.dp, ColorAdobeAe),
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Text(
                                text = "Ae",
                                color = ColorAdobeAe,
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
                                    saveAeDoc()
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
                                text = "Arc After Effects • Comp 1920x1080 @ 30fps",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { saveAeDoc() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Project", tint = ColorAdobeAe)
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
            // VFX Generator Preset Chips
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AeEffectChip("Neon Glow", Icons.Default.AutoAwesome, activeEffect == AeEffect.NEON_GLOW) {
                        activeEffect = AeEffect.NEON_GLOW
                        saveAeDoc()
                    }
                    AeEffectChip("Particles", Icons.Default.BlurOn, activeEffect == AeEffect.PARTICLES) {
                        activeEffect = AeEffect.PARTICLES
                        saveAeDoc()
                    }
                    AeEffectChip("Lens Flare", Icons.Default.Flare, activeEffect == AeEffect.LENS_FLARE) {
                        activeEffect = AeEffect.LENS_FLARE
                        saveAeDoc()
                    }
                    AeEffectChip("Glitch FX", Icons.Default.VideogameAsset, activeEffect == AeEffect.GLITCH_MATRIX) {
                        activeEffect = AeEffect.GLITCH_MATRIX
                        saveAeDoc()
                    }
                    AeEffectChip("Shockwave", Icons.Default.Stream, activeEffect == AeEffect.SHOCKWAVE) {
                        activeEffect = AeEffect.SHOCKWAVE
                        saveAeDoc()
                    }
                }
            }

            // Real-Time Motion Graphics Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF070712))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Canvas performing dynamic motion calculations
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val center = Offset(w / 2f, h / 2f)

                            // Animated orbit angle based on animTime
                            val phase = animTime * 2f
                            val dynamicScale = (scaleAnim / 100f) * (1f + 0.15f * kotlin.math.sin(phase))
                            val currentRot = rotationAnim + (animTime * 36f)

                            when (activeEffect) {
                                AeEffect.NEON_GLOW -> {
                                    // Multi-layer glowing concentric rings
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(ColorAdobeAe.copy(alpha = 0.8f * (opacityAnim / 100f)), Color.Transparent),
                                            center = center,
                                            radius = (w * 0.35f) * dynamicScale
                                        ),
                                        center = center,
                                        radius = (w * 0.35f) * dynamicScale
                                    )
                                    drawCircle(
                                        color = Color(0xFF38BDF8),
                                        radius = (w * 0.25f) * dynamicScale,
                                        center = center,
                                        style = Stroke(width = 4.dp.toPx())
                                    )
                                    drawCircle(
                                        color = ColorAdobeAe,
                                        radius = (w * 0.18f) * dynamicScale,
                                        center = center,
                                        style = Stroke(width = 6.dp.toPx())
                                    )
                                }

                                AeEffect.PARTICLES -> {
                                    // 24 floating animated particle stars
                                    for (i in 0..24) {
                                        val angle = (i * 15f + animTime * 45f) * (Math.PI / 180f)
                                        val dist = ((i * 14f + animTime * 60f) % (w * 0.45f))
                                        val px = center.x + kotlin.math.cos(angle).toFloat() * dist
                                        val py = center.y + kotlin.math.sin(angle).toFloat() * dist
                                        drawCircle(
                                            color = if (i % 2 == 0) Color(0xFF38BDF8) else Color(0xFFF43F5E),
                                            radius = (3f + (i % 5)).dp.toPx(),
                                            center = Offset(px, py)
                                        )
                                    }
                                }

                                AeEffect.LENS_FLARE -> {
                                    // Optical anamorphic flare line & iris artifacts
                                    val flareX = center.x + kotlin.math.sin(phase) * (w * 0.3f)
                                    val flareY = center.y + kotlin.math.cos(phase) * (h * 0.2f)

                                    drawLine(
                                        color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                                        start = Offset(0f, flareY),
                                        end = Offset(w, flareY),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 24.dp.toPx(),
                                        center = Offset(flareX, flareY)
                                    )
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            listOf(Color(0xFFF43F5E).copy(alpha = 0.6f), Color.Transparent),
                                            center = Offset(flareX, flareY),
                                            radius = 120.dp.toPx()
                                        ),
                                        center = Offset(flareX, flareY),
                                        radius = 120.dp.toPx()
                                    )
                                }

                                AeEffect.GLITCH_MATRIX -> {
                                    // Digital glitch block slices
                                    drawRect(color = Color(0xFF1E1B4B))
                                    for (g in 0..6) {
                                        val barY = (h * (g / 7f) + (animTime * 120f) % h) % h
                                        val barH = (12f + (g * 4f))
                                        drawRect(
                                            color = if (g % 2 == 0) Color(0xFF06B6D4).copy(alpha = 0.6f) else Color(0xFFEC4899).copy(alpha = 0.6f),
                                            topLeft = Offset(0f, barY),
                                            size = androidx.compose.ui.geometry.Size(w, barH)
                                        )
                                    }
                                }

                                AeEffect.SHOCKWAVE -> {
                                    // Kinetic expanding shockwave ring
                                    val ringR = ((animTime * 180f) % (w * 0.5f))
                                    drawCircle(
                                        color = ColorAdobeAe.copy(alpha = (1f - (ringR / (w * 0.5f))).coerceIn(0f, 1f)),
                                        radius = ringR,
                                        center = center,
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 20.dp.toPx(),
                                        center = center
                                    )
                                }
                            }
                        }

                        // HUD Overlay Timecode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = "FRAME ${ (animTime * 30).toInt() } • 00:${String.format(Locale.US, "%02d:%02d", animTime.toInt(), ((animTime % 1f) * 30).toInt())}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = ColorAdobeAe,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ColorAdobeAe.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, ColorAdobeAe)
                            ) {
                                Text(
                                    text = activeEffect.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = ColorAdobeAe,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Transport Playback Bar
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
                        IconButton(onClick = { animTime = 0.0f }) {
                            Icon(Icons.Default.Stop, contentDescription = "Rewind to 0s")
                        }
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ColorAdobeAe)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
                    }

                    // Scrubber
                    Slider(
                        value = animTime,
                        onValueChange = {
                            animTime = it
                            saveAeDoc()
                        },
                        valueRange = 0.0f..compDuration,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(thumbColor = ColorAdobeAe, activeTrackColor = ColorAdobeAe)
                    )

                    Text(
                        text = "${String.format(Locale.US, "%.1f", animTime)}s / ${compDuration.toInt()}s",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Keyframe Motion Properties Adjuster
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transformation Keyframe Rig",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ColorAdobeAe
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Diamond, contentDescription = null, tint = ColorAdobeAe, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Keyframes Active", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Scale:", fontSize = 11.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = scaleAnim,
                            onValueChange = {
                                scaleAnim = it
                                saveAeDoc()
                            },
                            valueRange = 50f..200f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = ColorAdobeAe, activeTrackColor = ColorAdobeAe)
                        )
                        Text("${scaleAnim.toInt()}%", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rotation:", fontSize = 11.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = rotationAnim,
                            onValueChange = {
                                rotationAnim = it
                                saveAeDoc()
                            },
                            valueRange = 0f..360f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = ColorAdobeAe, activeTrackColor = ColorAdobeAe)
                        )
                        Text("${rotationAnim.toInt()}°", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Opacity:", fontSize = 11.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = opacityAnim,
                            onValueChange = {
                                opacityAnim = it
                                saveAeDoc()
                            },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = ColorAdobeAe, activeTrackColor = ColorAdobeAe)
                        )
                        Text("${opacityAnim.toInt()}%", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AeEffectChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ColorAdobeAe.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) ColorAdobeAe else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ColorAdobeAe else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) ColorAdobeAe else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
