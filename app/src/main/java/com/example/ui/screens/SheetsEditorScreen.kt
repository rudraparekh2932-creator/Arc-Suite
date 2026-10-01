package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorSheetGreen
import com.example.ui.theme.StatusSuccess
import com.example.ui.utils.isTablet
import org.json.JSONObject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetsEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit,
    isOnline: Boolean = true,
    pendingOfflineMutations: Int = 0,
    onToggleOnline: () -> Unit = {},
    onOpenGeminiChat: () -> Unit = {}
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }

    // Parse initial cells from JSON
    val cellData = remember(document.id) {
        mutableStateMapOf<String, String>().apply {
            try {
                val json = JSONObject(document.content)
                val cellsObj = json.optJSONObject("cells") ?: JSONObject()
                val keys = cellsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    put(k, cellsObj.optString(k, ""))
                }
            } catch (_: Exception) {
                // Fallback default grid
                put("A1", "Item")
                put("B1", "Amount")
                put("A2", "Product A")
                put("B2", "1200")
                put("A3", "Product B")
                put("B3", "3400")
                put("A4", "Total")
                put("B4", "=SUM(B2:B3)")
            }
        }
    }

    var selectedCell by remember { mutableStateOf("B2") }
    var editingFormula by remember { mutableStateOf(cellData["B2"] ?: "") }
    var showCellEditDialog by remember { mutableStateOf(false) }

    val tabletMode = isTablet()
    val columns = remember(tabletMode) {
        if (tabletMode) listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
        else listOf("A", "B", "C", "D", "E", "F", "G", "H")
    }
    val rowCount = if (tabletMode) 35 else 25
    val cellWidth = if (tabletMode) 96.dp else 80.dp
    val totalGridWidth = 44.dp + cellWidth * columns.size

    // Helper to evaluate formulas
    fun evaluateFormula(formula: String): String {
        if (!formula.startsWith("=")) return formula
        val upper = formula.uppercase().trim()
        try {
            // Check for SUM(X1:Y2)
            if (upper.startsWith("=SUM(") && upper.endsWith(")")) {
                val range = upper.removePrefix("=SUM(").removeSuffix(")")
                val parts = range.split(":")
                if (parts.size == 2) {
                    val startCol = parts[0][0]
                    val startRow = parts[0].substring(1).toIntOrNull() ?: 1
                    val endCol = parts[1][0]
                    val endRow = parts[1].substring(1).toIntOrNull() ?: 1

                    var sum = 0.0
                    for (c in startCol..endCol) {
                        for (r in startRow..endRow) {
                            val key = "$c$r"
                            val cellVal = cellData[key] ?: ""
                            val evaluated = if (cellVal.startsWith("=")) evaluateFormula(cellVal) else cellVal
                            val num = evaluated.toDoubleOrNull() ?: 0.0
                            sum += num
                        }
                    }
                    return if (sum % 1.0 == 0.0) sum.toLong().toString() else String.format(Locale.US, "%.2f", sum)
                }
            }

            // Check for AVERAGE(X1:Y2)
            if (upper.startsWith("=AVERAGE(") && upper.endsWith(")")) {
                val range = upper.removePrefix("=AVERAGE(").removeSuffix(")")
                val parts = range.split(":")
                if (parts.size == 2) {
                    val startCol = parts[0][0]
                    val startRow = parts[0].substring(1).toIntOrNull() ?: 1
                    val endCol = parts[1][0]
                    val endRow = parts[1].substring(1).toIntOrNull() ?: 1

                    var sum = 0.0
                    var count = 0
                    for (c in startCol..endCol) {
                        for (r in startRow..endRow) {
                            val key = "$c$r"
                            val cellVal = cellData[key] ?: ""
                            val evaluated = if (cellVal.startsWith("=")) evaluateFormula(cellVal) else cellVal
                            val num = evaluated.toDoubleOrNull()
                            if (num != null) {
                                sum += num
                                count++
                            }
                        }
                    }
                    if (count == 0) return "0"
                    val avg = sum / count
                    return String.format(Locale.US, "%.2f", avg)
                }
            }

            // Simple subtraction or addition e.g. =B5-B11 or =A1+B1
            if (upper.contains("-")) {
                val parts = upper.removePrefix("=").split("-")
                if (parts.size == 2) {
                    val v1 = evaluateFormula(cellData[parts[0].trim()] ?: parts[0].trim()).toDoubleOrNull() ?: 0.0
                    val v2 = evaluateFormula(cellData[parts[1].trim()] ?: parts[1].trim()).toDoubleOrNull() ?: 0.0
                    val res = v1 - v2
                    return if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.2f", res)
                }
            }

            if (upper.contains("+")) {
                val parts = upper.removePrefix("=").split("+")
                if (parts.size == 2) {
                    val v1 = evaluateFormula(cellData[parts[0].trim()] ?: parts[0].trim()).toDoubleOrNull() ?: 0.0
                    val v2 = evaluateFormula(cellData[parts[1].trim()] ?: parts[1].trim()).toDoubleOrNull() ?: 0.0
                    val res = v1 + v2
                    return if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.2f", res)
                }
            }

            return formula
        } catch (_: Exception) {
            return "#ERR"
        }
    }

    fun saveSpreadsheet() {
        val root = JSONObject()
        root.put("activeCell", selectedCell)
        val cellsObj = JSONObject()
        for ((k, v) in cellData) {
            if (v.isNotBlank()) {
                cellsObj.put(k, v)
            }
        }
        root.put("cells", cellsObj)
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
                    Column {
                        TextField(
                            value = title,
                            onValueChange = {
                                title = it
                                saveSpreadsheet()
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
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) StatusSuccess else Color(0xFFD97706),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Arc Sheets Cloud Engine • Room Synced" else "Working Offline • Room Cache Active",
                                fontSize = 10.sp,
                                color = if (isOnline) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFD97706),
                                fontWeight = if (!isOnline) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenGeminiChat,
                        modifier = Modifier.testTag("sheets_topbar_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Formula Bot",
                            tint = Color(0xFF9333EA)
                        )
                    }
                    IconButton(onClick = onToggleOnline) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Toggle Network Status",
                            tint = if (isOnline) MaterialTheme.colorScheme.primary else Color(0xFFD97706)
                        )
                    }
                    IconButton(onClick = { saveSpreadsheet() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Sheet", tint = ColorSheetGreen)
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
            com.example.ui.components.OfflineStatusBanner(
                isOnline = isOnline,
                pendingOfflineMutations = pendingOfflineMutations,
                offlineDocsCount = if (!isOnline) 1 else 0,
                outboxMailCount = 0,
                onToggleOnline = onToggleOnline,
                onSyncNow = { saveSpreadsheet() }
            )

            // Collaborator Cell Lock indicator
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ColorSheetGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Collaborators in sheet: Carlos Silva (Cell B4), Maya Patel (Cell C2)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Formula Bar (Excel clone)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ColorSheetGreen.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = selectedCell,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = ColorSheetGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Formula",
                        tint = ColorSheetGreen,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedTextField(
                        value = editingFormula,
                        onValueChange = {
                            editingFormula = it
                            cellData[selectedCell] = it
                            saveSpreadsheet()
                        },
                        placeholder = { Text("Enter value or formula like =SUM(B2:D2)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sheets_formula_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            cellData[selectedCell] = editingFormula
                            saveSpreadsheet()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Apply", tint = ColorSheetGreen)
                    }

                    IconButton(
                        onClick = onOpenGeminiChat,
                        modifier = Modifier.testTag("sheets_formula_bar_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Formula Assistant",
                            tint = Color(0xFF9333EA)
                        )
                    }
                }
            }

            // Quick Formula Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FormulaChip("=SUM(B2:D2)") {
                    editingFormula = it
                    cellData[selectedCell] = it
                    saveSpreadsheet()
                }
                FormulaChip("=AVERAGE(B2:B10)") {
                    editingFormula = it
                    cellData[selectedCell] = it
                    saveSpreadsheet()
                }
                FormulaChip("=B5-B11") {
                    editingFormula = it
                    cellData[selectedCell] = it
                    saveSpreadsheet()
                }
                FormulaChip("$ Format") {
                    val raw = cellData[selectedCell] ?: ""
                    val num = raw.removePrefix("$").toDoubleOrNull()
                    if (num != null) {
                        val formatted = "$${String.format(Locale.US, "%,.2f", num)}"
                        cellData[selectedCell] = formatted
                        editingFormula = formatted
                        saveSpreadsheet()
                    }
                }
                FormulaChip("% Percent") {
                    val raw = cellData[selectedCell] ?: ""
                    val num = raw.removeSuffix("%").toDoubleOrNull()
                    if (num != null) {
                        val formatted = "${num}%"
                        cellData[selectedCell] = formatted
                        editingFormula = formatted
                        saveSpreadsheet()
                    }
                }
            }

            // 2D Interactive Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                LazyColumn(
                    modifier = Modifier.width(totalGridWidth)
                ) {
                    // Header Row (Column Letters)
                    item {
                        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                            // Row index header spacer
                            Box(
                                modifier = Modifier
                                    .size(44.dp, 32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            columns.forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .size(cellWidth, 32.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = col,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Grid Data Rows
                    items((1..rowCount).toList()) { rowIdx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            // Row number cell
                            Box(
                                modifier = Modifier
                                    .size(44.dp, 36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$rowIdx",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Columns
                            columns.forEach { col ->
                                val cellKey = "$col$rowIdx"
                                val rawContent = cellData[cellKey] ?: ""
                                val displayVal = if (rawContent.startsWith("=")) evaluateFormula(rawContent) else rawContent
                                val isSelected = selectedCell == cellKey
                                val isCarlosCollab = (cellKey == "B4")

                                Box(
                                    modifier = Modifier
                                        .size(cellWidth, 36.dp)
                                        .background(
                                            if (isSelected) ColorSheetGreen.copy(alpha = 0.15f)
                                            else if (isCarlosCollab) ArcPrimary.copy(alpha = 0.1f)
                                            else if (rowIdx % 2 == 0) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        )
                                        .clickable {
                                            selectedCell = cellKey
                                            editingFormula = cellData[cellKey] ?: ""
                                        },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = displayVal,
                                        fontSize = 11.sp,
                                        fontWeight = if (displayVal.startsWith("Total") || rawContent.startsWith("=")) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    if (isCarlosCollab) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(ArcPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calculation Summary Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeVal = cellData[selectedCell] ?: ""
                    val evalVal = if (activeVal.startsWith("=")) evaluateFormula(activeVal) else activeVal
                    Text(
                        text = "Active: $selectedCell = '$evalVal'",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorSheetGreen
                    )
                    Text(
                        text = "Total cells defined: ${cellData.size}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaChip(label: String, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(label) },
        shape = RoundedCornerShape(12.dp),
        color = ColorSheetGreen.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, ColorSheetGreen.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ColorSheetGreen,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
