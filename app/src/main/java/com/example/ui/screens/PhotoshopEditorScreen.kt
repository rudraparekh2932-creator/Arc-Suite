package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ColorAdobePs
import com.example.ui.theme.StatusSuccess
import org.json.JSONArray
import org.json.JSONObject

data class PsLayer(
    val id: String,
    var name: String,
    var blendMode: String = "Normal",
    var opacity: Float = 1.0f,
    var isVisible: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoshopEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }

    // Adjustment states
    var brightness by remember { mutableFloatStateOf(0.0f) } // -0.5 to 0.5
    var contrast by remember { mutableFloatStateOf(1.0f) }   // 0.5 to 2.0
    var saturation by remember { mutableFloatStateOf(1.0f) } // 0.0 to 2.0
    var vignette by remember { mutableFloatStateOf(0.2f) }   // 0.0 to 1.0
    var activePreset by remember { mutableStateOf("Cinematic Teal") }
    var activeTool by remember { mutableStateOf("ADJUST") } // ADJUST, BRUSH, CROP, HEAL, FILTERS

    var showLayersPanel by remember { mutableStateOf(false) }
    var retouchDots = remember { mutableStateListOf<Offset>() }

    val layers = remember(document.id) {
        mutableStateListOf(
            PsLayer("p1", "Subject (Raster Layer)", "Normal", 1.0f, true),
            PsLayer("p2", "Color Grade & LUTs", "Overlay", 0.85f, true),
            PsLayer("p3", "Vignette & Grain", "Soft Light", 0.6f, true)
        )
    }

    val presets = listOf(
        "Normal",
        "Cinematic Teal",
        "Noir Black & White",
        "Vivid HDR",
        "Warm Golden Hour",
        "Cyberpunk Neon"
    )

    fun applyPreset(presetName: String) {
        activePreset = presetName
        when (presetName) {
            "Normal" -> {
                brightness = 0.0f; contrast = 1.0f; saturation = 1.0f; vignette = 0.0f
            }
            "Cinematic Teal" -> {
                brightness = 0.05f; contrast = 1.25f; saturation = 1.3f; vignette = 0.35f
            }
            "Noir Black & White" -> {
                brightness = -0.05f; contrast = 1.4f; saturation = 0.0f; vignette = 0.45f
            }
            "Vivid HDR" -> {
                brightness = 0.1f; contrast = 1.35f; saturation = 1.6f; vignette = 0.2f
            }
            "Warm Golden Hour" -> {
                brightness = 0.08f; contrast = 1.1f; saturation = 1.25f; vignette = 0.25f
            }
            "Cyberpunk Neon" -> {
                brightness = 0.15f; contrast = 1.4f; saturation = 1.8f; vignette = 0.5f
            }
        }
    }

    fun savePhotoshopDoc() {
        val root = JSONObject()
        root.put("preset", activePreset)
        root.put("brightness", brightness.toDouble())
        root.put("contrast", contrast.toDouble())
        root.put("saturation", saturation.toDouble())
        root.put("vignette", vignette.toDouble())
        val lArray = JSONArray()
        for (l in layers) {
            val lObj = JSONObject()
            lObj.put("id", l.id)
            lObj.put("name", l.name)
            lObj.put("blendMode", l.blendMode)
            lObj.put("opacity", l.opacity.toDouble())
            lObj.put("visible", l.isVisible)
            lArray.put(lObj)
        }
        root.put("layers", lArray)
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
                            color = Color(0xFF001E36),
                            border = BorderStroke(1.5.dp, ColorAdobePs),
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Text(
                                text = "Ps",
                                color = ColorAdobePs,
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
                                    savePhotoshopDoc()
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Arc Photoshop Studio • ${layers.size} Layers Active",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showLayersPanel = !showLayersPanel }) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = if (showLayersPanel) ColorAdobePs else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { savePhotoshopDoc() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save PSD", tint = ColorAdobePs)
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
            // Photoshop Quick Tools Strip
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
                    PsToolChip("ADJUST", Icons.Default.Tune, activeTool == "ADJUST") { activeTool = "ADJUST" }
                    PsToolChip("FILTERS", Icons.Default.Filter, activeTool == "FILTERS") { activeTool = "FILTERS" }
                    PsToolChip("HEAL", Icons.Default.AutoFixHigh, activeTool == "HEAL") { activeTool = "HEAL" }
                    PsToolChip("BRUSH", Icons.Default.Brush, activeTool == "BRUSH") { activeTool = "BRUSH" }
                    PsToolChip("CROP", Icons.Default.Crop, activeTool == "CROP") { activeTool = "CROP" }
                }
            }

            // Central Photo Canvas with Live Image Shading & Color Matrix
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0B111E))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(activeTool) {
                            detectTapGestures { offset ->
                                if (activeTool == "HEAL") {
                                    retouchDots.add(offset)
                                    savePhotoshopDoc()
                                }
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Dynamic Rendered Canvas Surface representing photo bitmap
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Base plate with gradient simulating high-res photograph
                            val w = size.width
                            val h = size.height

                            val topTint = when (activePreset) {
                                "Noir Black & White" -> Color(0xFF333333)
                                "Cinematic Teal" -> Color(0xFF0D5C75)
                                "Warm Golden Hour" -> Color(0xFF9A3412)
                                "Cyberpunk Neon" -> Color(0xFF4C1D95)
                                else -> Color(0xFF1E3A8A)
                            }

                            val bottomTint = when (activePreset) {
                                "Noir Black & White" -> Color(0xFF888888)
                                "Cinematic Teal" -> Color(0xFFEA580C)
                                "Warm Golden Hour" -> Color(0xFFFBBF24)
                                "Cyberpunk Neon" -> Color(0xFF06B6D4)
                                else -> Color(0xFF047857)
                            }

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(topTint, bottomTint)
                                )
                            )

                            // Draw subtle geometric scene representing subject
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.8f * saturation.coerceIn(0f, 1.5f)), Color.Transparent),
                                    center = Offset(w * 0.4f, h * 0.35f),
                                    radius = w * 0.35f
                                ),
                                center = Offset(w * 0.4f, h * 0.35f),
                                radius = w * 0.35f
                            )

                            // Brightness & Contrast overlay
                            if (brightness != 0.0f) {
                                val bCol = if (brightness > 0) Color.White else Color.Black
                                drawRect(
                                    color = bCol.copy(alpha = kotlin.math.abs(brightness).coerceIn(0f, 0.6f))
                                )
                            }

                            // Vignette overlay
                            if (vignette > 0f) {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = vignette.coerceIn(0f, 0.9f))),
                                        center = Offset(w / 2f, h / 2f),
                                        radius = w * 0.65f
                                    )
                                )
                            }

                            // Retouch / Healing brush points
                            for (dot in retouchDots) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.65f),
                                    radius = 14f,
                                    center = dot
                                )
                            }
                        }

                        // HUD Overlay Info
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "RAW 4096x3072 • sRGB • LUT: $activePreset",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (activeTool == "HEAL") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ColorAdobePs,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Tap canvas to Spot-Heal (${retouchDots.size} healed)",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Tool Adjustment Drawer
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    when (activeTool) {
                        "ADJUST" -> {
                            // Sliders for Brightness, Contrast, Saturation, Vignette
                            Text(
                                text = "Color & Exposure Grading",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ColorAdobePs
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Exposure:", fontSize = 11.sp, modifier = Modifier.width(70.dp))
                                Slider(
                                    value = brightness,
                                    onValueChange = {
                                        brightness = it
                                        savePhotoshopDoc()
                                    },
                                    valueRange = -0.5f..0.5f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = ColorAdobePs, activeTrackColor = ColorAdobePs)
                                )
                                Text("${(brightness * 100).toInt()}%", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Contrast:", fontSize = 11.sp, modifier = Modifier.width(70.dp))
                                Slider(
                                    value = contrast,
                                    onValueChange = {
                                        contrast = it
                                        savePhotoshopDoc()
                                    },
                                    valueRange = 0.5f..2.0f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = ColorAdobePs, activeTrackColor = ColorAdobePs)
                                )
                                Text("${(contrast * 100).toInt()}%", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Saturation:", fontSize = 11.sp, modifier = Modifier.width(70.dp))
                                Slider(
                                    value = saturation,
                                    onValueChange = {
                                        saturation = it
                                        savePhotoshopDoc()
                                    },
                                    valueRange = 0.0f..2.0f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = ColorAdobePs, activeTrackColor = ColorAdobePs)
                                )
                                Text("${(saturation * 100).toInt()}%", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Vignette:", fontSize = 11.sp, modifier = Modifier.width(70.dp))
                                Slider(
                                    value = vignette,
                                    onValueChange = {
                                        vignette = it
                                        savePhotoshopDoc()
                                    },
                                    valueRange = 0.0f..1.0f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = ColorAdobePs, activeTrackColor = ColorAdobePs)
                                )
                                Text("${(vignette * 100).toInt()}%", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                            }
                        }

                        "FILTERS" -> {
                            Text(
                                text = "Color LUT Presets",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ColorAdobePs
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(presets) { p ->
                                    val isSelected = activePreset == p
                                    Surface(
                                        onClick = {
                                            applyPreset(p)
                                            savePhotoshopDoc()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) ColorAdobePs.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) ColorAdobePs else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = p,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) ColorAdobePs else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tool: $activeTool Active",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                if (activeTool == "HEAL" && retouchDots.isNotEmpty()) {
                                    Surface(
                                        onClick = { retouchDots.clear() },
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Clear Retouch Points",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Collapsible Layers & Blend Modes Panel
            AnimatedVisibility(visible = showLayersPanel) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Photoshop Layers (${layers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            IconButton(
                                onClick = {
                                    layers.add(PsLayer("p${layers.size + 1}", "Adjustment Layer ${layers.size + 1}", "Screen", 0.7f, true))
                                    savePhotoshopDoc()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Layer", tint = ColorAdobePs)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            layers.forEachIndexed { idx, layer ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    layer.isVisible = !layer.isVisible
                                                    savePhotoshopDoc()
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(text = layer.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(
                                                    text = "Blend: ${layer.blendMode} • ${(layer.opacity * 100).toInt()}%",
                                                    fontSize = 10.sp,
                                                    color = ColorAdobePs
                                                )
                                            }
                                        }

                                        if (layers.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    layers.removeAt(idx)
                                                    savePhotoshopDoc()
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PsToolChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ColorAdobePs.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) ColorAdobePs else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ColorAdobePs else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) ColorAdobePs else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
