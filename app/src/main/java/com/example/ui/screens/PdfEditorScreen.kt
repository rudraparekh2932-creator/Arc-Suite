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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorPdfRed
import com.example.ui.theme.StatusSuccess
import org.json.JSONArray
import org.json.JSONObject

data class PdfPageModel(
    val pageNumber: Int,
    var heading: String,
    var body: String,
    var hasSignature: Boolean = false,
    var signedBy: String = "",
    var signedDate: String = "",
    var stamp: String = "", // APPROVED, CONFIDENTIAL, VERIFIED
    var rotation: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfEditorScreen(
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

    val pages = remember(document.id) {
        val list = mutableStateListOf<PdfPageModel>()
        try {
            val json = JSONObject(document.content)
            val pagesArr = json.optJSONArray("pages")
            if (pagesArr != null) {
                for (i in 0 until pagesArr.length()) {
                    val p = pagesArr.getJSONObject(i)
                    list.add(
                        PdfPageModel(
                            pageNumber = p.optInt("pageNumber", i + 1),
                            heading = p.optString("heading", "PAGE ${i + 1}"),
                            body = p.optString("body", "Sample PDF body text."),
                            hasSignature = p.optBoolean("hasSignature", false),
                            signedBy = p.optString("signedBy", ""),
                            signedDate = p.optString("signedDate", ""),
                            stamp = p.optString("stamp", "")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Default 2 pages
            list.add(
                PdfPageModel(
                    pageNumber = 1,
                    heading = "ENTERPRISE SPECIFICATION DOCUMENT",
                    body = "This document contains verified technical architectures and collaborative specifications for Arc Suite.\n\nSection 1: Security and Cryptographic Validation.\nAll signatures are cryptographically bound to the active Arc Suite product key.",
                    hasSignature = true,
                    signedBy = "Rudra Parekh",
                    signedDate = "Sept 2026",
                    stamp = "APPROVED"
                )
            )
            list.add(
                PdfPageModel(
                    pageNumber = 2,
                    heading = "CLOUD REPLICATION ADDENDUM",
                    body = "Data synchronization guarantees zero-loss local caching during intermittent connectivity.\n\nCertified compliant under SOC-2 and ISO 27001 standards.",
                    stamp = "CONFIDENTIAL"
                )
            )
        }
        if (list.isEmpty()) {
            list.add(PdfPageModel(pageNumber = 1, heading = "Document Page 1", body = "Start writing or annotating this PDF."))
        }
        list
    }

    var currentPageIndex by remember { mutableIntStateOf(0) }
    var showSignaturePad by remember { mutableStateOf(false) }
    var showStampDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var activeHighlightColor by remember { mutableStateOf<Color?>(null) }

    fun savePdf() {
        val root = JSONObject()
        root.put("totalPages", pages.size)
        root.put("currentPage", currentPageIndex + 1)
        val pArr = JSONArray()
        for (p in pages) {
            val pObj = JSONObject()
            pObj.put("pageNumber", p.pageNumber)
            pObj.put("heading", p.heading)
            pObj.put("body", p.body)
            pObj.put("hasSignature", p.hasSignature)
            pObj.put("signedBy", p.signedBy)
            pObj.put("signedDate", p.signedDate)
            pObj.put("stamp", p.stamp)
            pObj.put("rotation", p.rotation)
            pArr.put(pObj)
        }
        root.put("pages", pArr)
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
                    Column {
                        TextField(
                            value = title,
                            onValueChange = {
                                title = it
                                savePdf()
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
                                text = if (isOnline) "Arc Acrobat Engine • Page ${currentPageIndex + 1} of ${pages.size}" else "Working Offline • Room Cache • Page ${currentPageIndex + 1} of ${pages.size}",
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
                        modifier = Modifier.testTag("pdf_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini PDF Assistant",
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
                    IconButton(onClick = { savePdf() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save PDF", tint = ColorPdfRed)
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
                onSyncNow = { savePdf() }
            )

            // PDF Markup Toolbar (Adobe Acrobat clone)
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
                    // Sign tool
                    Surface(
                        onClick = { showSignaturePad = true },
                        shape = RoundedCornerShape(8.dp),
                        color = ColorPdfRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ColorPdfRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Gesture, contentDescription = "Sign", tint = ColorPdfRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("e-Signature", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorPdfRed)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Stamp tool
                    Surface(
                        onClick = { showStampDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Stamp", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stamp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Highlight tools
                    listOf(
                        Color(0xFFFEF08A) to "Yellow",
                        Color(0xFFA7F3D0) to "Green",
                        Color(0xFFA5F3FC) to "Cyan",
                        Color(0xFFFECDD3) to "Coral"
                    ).forEach { (col, name) ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable {
                                    activeHighlightColor = if (activeHighlightColor == col) null else col
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (activeHighlightColor == col) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Black))
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Rotate page
                    IconButton(
                        onClick = {
                            if (activePage != null) {
                                activePage.rotation = (activePage.rotation + 90f) % 360f
                                savePdf()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(18.dp))
                    }

                    // Add Page
                    IconButton(
                        onClick = {
                            val newP = PdfPageModel(
                                pageNumber = pages.size + 1,
                                heading = "PAGE ${pages.size + 1}",
                                body = "Additional PDF page section."
                            )
                            pages.add(newP)
                            currentPageIndex = pages.size - 1
                            savePdf()
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Page", tint = ColorPdfRed)
                    }

                    // Delete Page
                    if (pages.size > 1) {
                        IconButton(
                            onClick = {
                                pages.removeAt(currentPageIndex)
                                currentPageIndex = currentPageIndex.coerceAtMost(pages.size - 1)
                                savePdf()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // PDF Page Viewer & Inking Sheet
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF334155))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activePage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1f / 1.35f)
                            .rotate(activePage.rotation),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // PDF Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ARC PORTABLE DOCUMENT",
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "PAGE ${activePage.pageNumber} OF ${pages.size}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Page Heading (Editable)
                            TextField(
                                value = activePage.heading,
                                onValueChange = {
                                    activePage.heading = it
                                    savePdf()
                                },
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = ColorPdfRed.copy(alpha = 0.5f),
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Body Text (with Highlight overlay effect)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(activeHighlightColor?.copy(alpha = 0.35f) ?: Color.Transparent, RoundedCornerShape(4.dp))
                            ) {
                                TextField(
                                    value = activePage.body,
                                    onValueChange = {
                                        activePage.body = it
                                        savePdf()
                                    },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF1E293B),
                                        lineHeight = 22.sp
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Security Stamp (if present)
                            if (activePage.stamp.isNotEmpty()) {
                                Surface(
                                    color = when (activePage.stamp) {
                                        "APPROVED" -> StatusSuccess.copy(alpha = 0.15f)
                                        "CONFIDENTIAL" -> ColorPdfRed.copy(alpha = 0.15f)
                                        else -> ArcPrimary.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(
                                        2.dp,
                                        when (activePage.stamp) {
                                            "APPROVED" -> StatusSuccess
                                            "CONFIDENTIAL" -> ColorPdfRed
                                            else -> ArcPrimary
                                        }
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "★ ${activePage.stamp} ★",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        letterSpacing = 2.sp,
                                        color = when (activePage.stamp) {
                                            "APPROVED" -> StatusSuccess
                                            "CONFIDENTIAL" -> ColorPdfRed
                                            else -> ArcPrimary
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Digital Signature Stamp Box
                            if (activePage.hasSignature) {
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusSuccess,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Digitally Signed by ${activePage.signedBy}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "Hardware Key Authenticated • Date: ${activePage.signedDate}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Page Pager Controls
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentPageIndex > 0) currentPageIndex--
                        },
                        enabled = currentPageIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Page")
                    }

                    Text(
                        text = "Page ${currentPageIndex + 1} of ${pages.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            if (currentPageIndex < pages.size - 1) currentPageIndex++
                        },
                        enabled = currentPageIndex < pages.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page")
                    }
                }
            }
        }
    }

    // Signature Pad Modal
    if (showSignaturePad && activePage != null) {
        SignatureCaptureDialog(
            signerName = "Rudra Parekh",
            onDismiss = { showSignaturePad = false },
            onSignatureConfirmed = {
                activePage.hasSignature = true
                activePage.signedBy = "Rudra Parekh"
                activePage.signedDate = "Sept 27, 2026"
                savePdf()
                showSignaturePad = false
            }
        )
    }

    // Stamp Selection Dialog
    if (showStampDialog && activePage != null) {
        AlertDialog(
            onDismissRequest = { showStampDialog = false },
            title = { Text("Apply Document Stamp") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("APPROVED", "CONFIDENTIAL", "FINALIZED", "VERIFIED", "REMOVE STAMP").forEach { stampName ->
                        Surface(
                            onClick = {
                                activePage.stamp = if (stampName == "REMOVE STAMP") "" else stampName
                                savePdf()
                                showStampDialog = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stampName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStampDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SignatureCaptureDialog(
    signerName: String,
    onDismiss: () -> Unit,
    onSignatureConfirmed: () -> Unit
) {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Draw e-Signature", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Sign using your finger on the touch pad below:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newP = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = newP
                                    paths.add(newP)
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (p in paths) {
                            drawPath(
                                path = p,
                                color = Color(0xFF1E3A8A),
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                    Text(
                        text = "Sign on the line above ────────────────",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Signer: $signerName", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { paths.clear() }) {
                        Text("Clear Pad", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSignatureConfirmed,
                colors = ButtonDefaults.buttonColors(containerColor = ColorPdfRed)
            ) {
                Text("Apply Signature")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
