package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ViewColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ColorAdobeId
import com.example.ui.theme.StatusSuccess
import org.json.JSONArray
import org.json.JSONObject

data class IdPageModel(
    val pageNumber: Int,
    var type: String, // COVER, SPREAD_LEFT, SPREAD_RIGHT
    var headline: String,
    var subheading: String,
    var author: String,
    var body: String,
    var accentColorHex: String = "#FF3366",
    var dropCap: Boolean = true,
    var columnsCount: Int = 2
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InDesignEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }

    val pages = remember(document.id) {
        val list = mutableStateListOf<IdPageModel>()
        try {
            val json = JSONObject(document.content)
            val pArr = json.optJSONArray("pages")
            if (pArr != null) {
                for (i in 0 until pArr.length()) {
                    val o = pArr.getJSONObject(i)
                    list.add(
                        IdPageModel(
                            pageNumber = o.optInt("pageNumber", i + 1),
                            type = o.optString("type", "SPREAD_LEFT"),
                            headline = o.optString("headline", "Editorial Title"),
                            subheading = o.optString("subheading", "Sub-headline text"),
                            author = o.optString("author", "Arc Studio"),
                            body = o.optString("body", "Editorial article content paragraph with rich column layouts."),
                            accentColorHex = o.optString("accentColor", "#FF3366"),
                            dropCap = o.optBoolean("dropCap", true)
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.add(
                IdPageModel(
                    pageNumber = 1,
                    type = "COVER",
                    headline = "ARC MAGAZINE ISSUE 01",
                    subheading = "The Architecture of Modern Digital Creativity",
                    author = "By Elena Rostova",
                    body = "Inside this issue: Vector mastery, multi-track timeline editing, and the future of typographic cloud publication."
                )
            )
            list.add(
                IdPageModel(
                    pageNumber = 2,
                    type = "SPREAD_LEFT",
                    headline = "The Swiss Grid Tradition",
                    subheading = "Order, clarity, and rational composition",
                    author = "By David Chen",
                    body = "Josef Müller-Brockmann and the pioneers of the International Typographic Style understood that visual hierarchy is not arbitrary. By aligning text frames into strict columns and maintaining consistent baseline leading, reading becomes intuitive."
                )
            )
        }
        list
    }

    var currentPageIndex by remember { mutableIntStateOf(0) }
    var showGuides by remember { mutableStateOf(true) }

    fun saveInDesignDoc() {
        val root = JSONObject()
        root.put("pageCount", pages.size)
        val arr = JSONArray()
        for (p in pages) {
            val o = JSONObject()
            o.put("pageNumber", p.pageNumber)
            o.put("type", p.type)
            o.put("headline", p.headline)
            o.put("subheading", p.subheading)
            o.put("author", p.author)
            o.put("body", p.body)
            o.put("accentColor", p.accentColorHex)
            o.put("dropCap", p.dropCap)
            o.put("columnsCount", p.columnsCount)
            arr.put(o)
        }
        root.put("pages", arr)
        onSaveContent(root.toString(), title)
    }

    val activePage = pages.getOrNull(currentPageIndex) ?: pages.firstOrNull()

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
                            color = Color(0xFF380218),
                            border = BorderStroke(1.5.dp, ColorAdobeId),
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Text(
                                text = "Id",
                                color = ColorAdobeId,
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
                                    saveInDesignDoc()
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
                                text = "Arc InDesign Layout • A4 (210 x 297 mm) • Bleed 3mm",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showGuides = !showGuides }) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Toggle Grid Guides",
                            tint = if (showGuides) ColorAdobeId else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { saveInDesignDoc() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save INDD", tint = ColorAdobeId)
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
            // Typography & Column Controls Strip
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Columns:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf(1, 2, 3).forEach { cols ->
                        val isSelected = activePage?.columnsCount == cols
                        Surface(
                            onClick = {
                                activePage?.columnsCount = cols
                                saveInDesignDoc()
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) ColorAdobeId.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) ColorAdobeId else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "$cols Col",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ColorAdobeId else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        onClick = {
                            if (activePage != null) {
                                activePage.dropCap = !activePage.dropCap
                                saveInDesignDoc()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (activePage?.dropCap == true) ColorAdobeId.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (activePage?.dropCap == true) ColorAdobeId else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Drop Cap ${if (activePage?.dropCap == true) "ON" else "OFF"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activePage?.dropCap == true) ColorAdobeId else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        onClick = {
                            if (activePage != null) {
                                activePage.type = if (activePage.type == "COVER") "SPREAD_LEFT" else "COVER"
                                saveInDesignDoc()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Layout: ${activePage?.type}",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Desktop Publishing Page Preview Canvas (A4 Aspect Ratio Sheet)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF262626))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activePage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1f / 1.414f), // ISO 216 standard A4 ratio
                        shape = RoundedCornerShape(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Bleed & Margin Guideline
                            if (showGuides) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "MARGIN GUIDE: 12.7mm • BLEED 3mm",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ColorAdobeId
                                    )
                                    Text(
                                        text = "PAGE ${activePage.pageNumber}",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ColorAdobeId
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (activePage.type == "COVER") {
                                // Magazine Cover Layout
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "ARC EXCLUSIVE",
                                            color = ColorAdobeId,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        TextField(
                                            value = activePage.headline,
                                            onValueChange = {
                                                activePage.headline = it
                                                saveInDesignDoc()
                                            },
                                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            ),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = ColorAdobeId
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                TextField(
                                    value = activePage.subheading,
                                    onValueChange = {
                                        activePage.subheading = it
                                        saveInDesignDoc()
                                    },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = ColorAdobeId.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                TextField(
                                    value = activePage.body,
                                    onValueChange = {
                                        activePage.body = it
                                        saveInDesignDoc()
                                    },
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF475569),
                                        lineHeight = 18.sp
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent
                                    )
                                )
                            } else {
                                // Multi-Column Editorial Article Spread Layout
                                TextField(
                                    value = activePage.headline,
                                    onValueChange = {
                                        activePage.headline = it
                                        saveInDesignDoc()
                                    },
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A)
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = ColorAdobeId
                                    )
                                )

                                Text(
                                    text = "By ${activePage.author} • InDesign Typographic Spread",
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Render columns (1, 2, or 3 columns)
                                val colCount = activePage.columnsCount
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val textChunks = activePage.body.chunked((activePage.body.length / colCount).coerceAtLeast(10))

                                    for (cIdx in 0 until colCount) {
                                        val chunk = textChunks.getOrNull(cIdx) ?: ""
                                        Column(modifier = Modifier.weight(1f)) {
                                            if (cIdx == 0 && activePage.dropCap && chunk.isNotEmpty()) {
                                                // Drop Cap initial letter
                                                Row(verticalAlignment = Alignment.Top) {
                                                    Text(
                                                        text = chunk.take(1),
                                                        fontSize = 28.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = ColorAdobeId,
                                                        lineHeight = 28.sp,
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    )
                                                    Text(
                                                        text = chunk.drop(1),
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF1E293B),
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = chunk,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF1E293B),
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Editable Raw Body Field
                                OutlinedTextField(
                                    value = activePage.body,
                                    onValueChange = {
                                        activePage.body = it
                                        saveInDesignDoc()
                                    },
                                    label = { Text("Edit Story Text Content") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Page Navigation Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(pages) { idx, page ->
                            val isSelected = idx == currentPageIndex
                            Surface(
                                onClick = { currentPageIndex = idx },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) ColorAdobeId.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) ColorAdobeId else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "Page ${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ColorAdobeId else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Add Page Button
                    IconButton(
                        onClick = {
                            val newPage = IdPageModel(
                                pageNumber = pages.size + 1,
                                type = "SPREAD_RIGHT",
                                headline = "Editorial Feature ${pages.size + 1}",
                                subheading = "Visual narrative and typography layout",
                                author = "Arc Studio",
                                body = "Add your article body text here to create multi-column editorial pages."
                            )
                            pages.add(newPage)
                            currentPageIndex = pages.size - 1
                            saveInDesignDoc()
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Page", tint = ColorAdobeId)
                    }

                    if (pages.size > 1) {
                        IconButton(
                            onClick = {
                                pages.removeAt(currentPageIndex)
                                currentPageIndex = currentPageIndex.coerceAtMost(pages.size - 1)
                                saveInDesignDoc()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
