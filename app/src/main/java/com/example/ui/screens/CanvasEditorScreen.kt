package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorCanvasViolet
import com.example.ui.theme.StatusSuccess
import org.json.JSONObject
import java.util.UUID

enum class CanvasTool {
    BRUSH,
    ERASER,
    RECTANGLE,
    CIRCLE,
    ARROW,
    STAR_STAMP
}

data class DrawPathItem(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val layerId: String,
    val tool: CanvasTool = CanvasTool.BRUSH,
    val startOffset: Offset = Offset.Zero,
    val endOffset: Offset = Offset.Zero
)

data class LayerItem(
    val id: String,
    var name: String,
    var isVisible: Boolean = true,
    var opacity: Float = 1.0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }

    // Layers
    val layers = remember(document.id) {
        mutableStateListOf(
            LayerItem("l1", "Background Layer", isVisible = true, opacity = 1f),
            LayerItem("l2", "Vector Shapes", isVisible = true, opacity = 1f),
            LayerItem("l3", "Annotations & Branding", isVisible = true, opacity = 0.9f)
        )
    }
    var activeLayerIndex by remember { mutableIntStateOf(1) }

    // Drawing Tool States
    var selectedTool by remember { mutableStateOf(CanvasTool.BRUSH) }
    var selectedColor by remember { mutableStateOf(ColorCanvasViolet) }
    var strokeWidthDp by remember { mutableStateOf(8f) }
    var showLayersSheet by remember { mutableStateOf(false) }

    // Path Strokes
    val pathItems = remember(document.id) { mutableStateListOf<DrawPathItem>() }
    val undonePaths = remember { mutableStateListOf<DrawPathItem>() }

    var currentPath by remember { mutableStateOf<Path?>(null) }
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

    fun saveCanvas() {
        val json = JSONObject()
        json.put("layersCount", layers.size)
        json.put("elementsCount", pathItems.size)
        onSaveContent(json.toString(), title)
    }

    val activeLayer = layers.getOrNull(activeLayerIndex) ?: layers.firstOrNull()

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
                        TextField(
                            value = title,
                            onValueChange = {
                                title = it
                                saveCanvas()
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
                                text = "Arc Creative Studio • Layer ${activeLayerIndex + 1} (${activeLayer?.name})",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = {
                            if (pathItems.isNotEmpty()) {
                                val removed = pathItems.removeLast()
                                undonePaths.add(removed)
                                saveCanvas()
                            }
                        },
                        enabled = pathItems.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    // Redo
                    IconButton(
                        onClick = {
                            if (undonePaths.isNotEmpty()) {
                                val restored = undonePaths.removeLast()
                                pathItems.add(restored)
                                saveCanvas()
                            }
                        },
                        enabled = undonePaths.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    // Layers Toggle
                    IconButton(onClick = { showLayersSheet = !showLayersSheet }) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = if (showLayersSheet) ColorCanvasViolet else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Save
                    IconButton(onClick = { saveCanvas() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Art", tint = ColorCanvasViolet)
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
            // Creative Palette Toolbar (Photoshop/Illustrator tools)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brush
                    ToolBtn(
                        icon = Icons.Default.Brush,
                        label = "Brush",
                        isSelected = selectedTool == CanvasTool.BRUSH,
                        color = ColorCanvasViolet,
                        onClick = { selectedTool = CanvasTool.BRUSH }
                    )
                    // Eraser
                    ToolBtn(
                        icon = Icons.Default.Clear,
                        label = "Eraser",
                        isSelected = selectedTool == CanvasTool.ERASER,
                        color = ColorCanvasViolet,
                        onClick = { selectedTool = CanvasTool.ERASER }
                    )
                    // Rectangle Shape
                    ToolBtn(
                        icon = Icons.Default.CropSquare,
                        label = "Rect",
                        isSelected = selectedTool == CanvasTool.RECTANGLE,
                        color = ColorCanvasViolet,
                        onClick = { selectedTool = CanvasTool.RECTANGLE }
                    )
                    // Circle Shape
                    ToolBtn(
                        icon = Icons.Default.Circle,
                        label = "Circle",
                        isSelected = selectedTool == CanvasTool.CIRCLE,
                        color = ColorCanvasViolet,
                        onClick = { selectedTool = CanvasTool.CIRCLE }
                    )
                    // Arrow
                    ToolBtn(
                        icon = Icons.Default.NorthEast,
                        label = "Arrow",
                        isSelected = selectedTool == CanvasTool.ARROW,
                        color = ColorCanvasViolet,
                        onClick = { selectedTool = CanvasTool.ARROW }
                    )
                    // Star Stamp
                    ToolBtn(
                        icon = Icons.Default.AutoAwesome,
                        label = "Stamp",
                        isSelected = selectedTool == CanvasTool.STAR_STAMP,
                        color = ColorCanvasViolet,
                        onClick = { selectedTool = CanvasTool.STAR_STAMP }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(1.dp, 24.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.width(8.dp))

                    // Color Swatches
                    listOf(
                        ColorCanvasViolet,
                        ArcPrimary,
                        Color(0xFF10B981),
                        Color(0xFFF59E0B),
                        Color(0xFFEF4444),
                        Color(0xFFEC4899),
                        Color(0xFF0F172A),
                        Color(0xFFFFFFFF)
                    ).forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColor = col }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == col) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (col == Color.White) Color.Black else Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Stroke width picker chips
                    listOf(3f to "Fine", 8f to "Med", 18f to "Bold").forEach { (width, label) ->
                        Surface(
                            onClick = { strokeWidthDp = width },
                            shape = RoundedCornerShape(10.dp),
                            color = if (strokeWidthDp == width) ColorCanvasViolet.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (strokeWidthDp == width) FontWeight.Bold else FontWeight.Normal,
                                color = if (strokeWidthDp == width) ColorCanvasViolet else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Main Interactive Multi-Layer Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedTool, selectedColor, strokeWidthDp, activeLayer) {
                            val layerId = activeLayer?.id ?: "l1"
                            detectDragGestures(
                                onDragStart = { offset ->
                                    dragStartOffset = offset
                                    dragCurrentOffset = offset
                                    if (selectedTool == CanvasTool.BRUSH || selectedTool == CanvasTool.ERASER) {
                                        val p = Path().apply { moveTo(offset.x, offset.y) }
                                        currentPath = p
                                        val col = if (selectedTool == CanvasTool.ERASER) Color(0xFF0F172A) else selectedColor
                                        pathItems.add(DrawPathItem(p, col, strokeWidthDp, layerId, selectedTool))
                                        undonePaths.clear()
                                    }
                                },
                                onDrag = { change, _ ->
                                    dragCurrentOffset = change.position
                                    if (selectedTool == CanvasTool.BRUSH || selectedTool == CanvasTool.ERASER) {
                                        currentPath?.lineTo(change.position.x, change.position.y)
                                    }
                                },
                                onDragEnd = {
                                    val start = dragStartOffset
                                    val end = dragCurrentOffset
                                    if (start != null && end != null && selectedTool != CanvasTool.BRUSH && selectedTool != CanvasTool.ERASER) {
                                        val shapePath = Path()
                                        when (selectedTool) {
                                            CanvasTool.RECTANGLE -> {
                                                val left = minOf(start.x, end.x)
                                                val top = minOf(start.y, end.y)
                                                val w = kotlin.math.abs(end.x - start.x)
                                                val h = kotlin.math.abs(end.y - start.y)
                                                shapePath.addRect(androidx.compose.ui.geometry.Rect(left, top, left + w, top + h))
                                            }
                                            CanvasTool.CIRCLE -> {
                                                val left = minOf(start.x, end.x)
                                                val top = minOf(start.y, end.y)
                                                val w = kotlin.math.abs(end.x - start.x)
                                                val h = kotlin.math.abs(end.y - start.y)
                                                shapePath.addOval(androidx.compose.ui.geometry.Rect(left, top, left + w, top + h))
                                            }
                                            CanvasTool.ARROW -> {
                                                shapePath.moveTo(start.x, start.y)
                                                shapePath.lineTo(end.x, end.y)
                                            }
                                            CanvasTool.STAR_STAMP -> {
                                                shapePath.moveTo(end.x, end.y - 30f)
                                                shapePath.lineTo(end.x + 20f, end.y + 20f)
                                                shapePath.lineTo(end.x - 20f, end.y - 10f)
                                                shapePath.lineTo(end.x + 20f, end.y - 10f)
                                                shapePath.lineTo(end.x - 20f, end.y + 20f)
                                                shapePath.close()
                                            }
                                            else -> {}
                                        }
                                        pathItems.add(
                                            DrawPathItem(
                                                shapePath,
                                                selectedColor,
                                                strokeWidthDp,
                                                layerId,
                                                selectedTool,
                                                start,
                                                end
                                            )
                                        )
                                        undonePaths.clear()
                                        saveCanvas()
                                    }
                                    dragStartOffset = null
                                    dragCurrentOffset = null
                                    currentPath = null
                                }
                            )
                        }
                ) {
                    // Draw paths filtered by visible layers and with layer opacity
                    for (item in pathItems) {
                        val layer = layers.find { it.id == item.layerId }
                        if (layer?.isVisible != false) {
                            val alpha = (layer?.opacity ?: 1.0f).coerceIn(0f, 1f)
                            drawPath(
                                path = item.path,
                                color = item.color.copy(alpha = item.color.alpha * alpha),
                                style = Stroke(
                                    width = item.strokeWidth.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Temporary preview of shape during drag
                    val s = dragStartOffset
                    val e = dragCurrentOffset
                    if (s != null && e != null) {
                        when (selectedTool) {
                            CanvasTool.RECTANGLE -> {
                                val left = minOf(s.x, e.x)
                                val top = minOf(s.y, e.y)
                                val w = kotlin.math.abs(e.x - s.x)
                                val h = kotlin.math.abs(e.y - s.y)
                                drawRect(
                                    color = selectedColor.copy(alpha = 0.6f),
                                    topLeft = Offset(left, top),
                                    size = Size(w, h),
                                    style = Stroke(width = strokeWidthDp.dp.toPx())
                                )
                            }
                            CanvasTool.CIRCLE -> {
                                val left = minOf(s.x, e.x)
                                val top = minOf(s.y, e.y)
                                val w = kotlin.math.abs(e.x - s.x)
                                val h = kotlin.math.abs(e.y - s.y)
                                drawOval(
                                    color = selectedColor.copy(alpha = 0.6f),
                                    topLeft = Offset(left, top),
                                    size = Size(w, h),
                                    style = Stroke(width = strokeWidthDp.dp.toPx())
                                )
                            }
                            CanvasTool.ARROW -> {
                                drawLine(
                                    color = selectedColor.copy(alpha = 0.8f),
                                    start = s,
                                    end = e,
                                    strokeWidth = strokeWidthDp.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            else -> {}
                        }
                    }
                }

                // Collaborator active cursor indicator simulation
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArcPrimary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Maya Patel is drawing on Layer 3",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Layer Management Side/Bottom Sheet
            AnimatedVisibility(visible = showLayersSheet) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Layers Manager (${layers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            IconButton(
                                onClick = {
                                    layers.add(LayerItem("l${layers.size + 1}", "Layer ${layers.size + 1}"))
                                    activeLayerIndex = layers.size - 1
                                    saveCanvas()
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Layer", tint = ColorCanvasViolet)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            layers.forEachIndexed { idx, layer ->
                                val isSelected = idx == activeLayerIndex
                                Surface(
                                    onClick = { activeLayerIndex = idx },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ColorCanvasViolet.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, if (isSelected) ColorCanvasViolet else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                                                    saveCanvas()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle visibility",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = layer.name,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${(layer.opacity * 100).toInt()}%",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (layers.size > 1) {
                                                IconButton(
                                                    onClick = {
                                                        layers.removeAt(idx)
                                                        activeLayerIndex = activeLayerIndex.coerceAtMost(layers.size - 1)
                                                        saveCanvas()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete Layer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
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
}

@Composable
private fun ToolBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
