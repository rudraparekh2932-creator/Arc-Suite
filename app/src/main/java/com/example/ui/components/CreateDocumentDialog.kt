package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocType
import com.example.ui.theme.ArcPrimary

@Composable
fun CreateDocumentDialog(
    initialType: DocType = DocType.DOC,
    onDismiss: () -> Unit,
    onCreate: (DocType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create in Arc Suite", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Select Office or Adobe Tool:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable module selectors
                Column(
                    modifier = Modifier
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModuleOption(
                        type = DocType.DOC,
                        badge = "Wd",
                        icon = Icons.Default.Description,
                        title = "Arc Docs (Word)",
                        desc = "Formatted documents & reports",
                        isSelected = selectedType == DocType.DOC,
                        onClick = { selectedType = DocType.DOC }
                    )
                    ModuleOption(
                        type = DocType.SHEET,
                        badge = "Xl",
                        icon = Icons.Default.TableChart,
                        title = "Arc Sheets (Excel)",
                        desc = "Formulas, calculation grids & data",
                        isSelected = selectedType == DocType.SHEET,
                        onClick = { selectedType = DocType.SHEET }
                    )
                    ModuleOption(
                        type = DocType.SLIDE,
                        badge = "Pt",
                        icon = Icons.Default.PresentToAll,
                        title = "Arc Slides (PowerPoint)",
                        desc = "Presentation decks & presenter notes",
                        isSelected = selectedType == DocType.SLIDE,
                        onClick = { selectedType = DocType.SLIDE }
                    )
                    ModuleOption(
                        type = DocType.PDF,
                        badge = "Ac",
                        icon = Icons.Default.PictureAsPdf,
                        title = "Arc PDF (Acrobat)",
                        desc = "PDF markup, highlight & signatures",
                        isSelected = selectedType == DocType.PDF,
                        onClick = { selectedType = DocType.PDF }
                    )
                    ModuleOption(
                        type = DocType.PHOTOSHOP,
                        badge = "Ps",
                        icon = Icons.Default.AutoFixHigh,
                        title = "Photoshop (Ps)",
                        desc = "Photo grading, retouching & layers",
                        isSelected = selectedType == DocType.PHOTOSHOP,
                        onClick = { selectedType = DocType.PHOTOSHOP }
                    )
                    ModuleOption(
                        type = DocType.PREMIERE,
                        badge = "Pr",
                        icon = Icons.Default.Movie,
                        title = "Premiere Pro (Pr)",
                        desc = "Non-linear multi-track video timeline",
                        isSelected = selectedType == DocType.PREMIERE,
                        onClick = { selectedType = DocType.PREMIERE }
                    )
                    ModuleOption(
                        type = DocType.INDESIGN,
                        badge = "Id",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "InDesign (Id)",
                        desc = "Multi-column editorial publication layouts",
                        isSelected = selectedType == DocType.INDESIGN,
                        onClick = { selectedType = DocType.INDESIGN }
                    )
                    ModuleOption(
                        type = DocType.AFTER_EFFECTS,
                        badge = "Ae",
                        icon = Icons.Default.MovieFilter,
                        title = "After Effects (Ae)",
                        desc = "Motion graphics, keyframes & VFX",
                        isSelected = selectedType == DocType.AFTER_EFFECTS,
                        onClick = { selectedType = DocType.AFTER_EFFECTS }
                    )
                    ModuleOption(
                        type = DocType.CANVAS,
                        badge = "Ai",
                        icon = Icons.Default.Brush,
                        title = "Arc Studio (Illustrator)",
                        desc = "Multi-layer vector & drawing canvas",
                        isSelected = selectedType == DocType.CANVAS,
                        onClick = { selectedType = DocType.CANVAS }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project / Document Title") },
                    placeholder = { Text("e.g. Master Design 2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Untitled ${selectedType.displayName}" }
                    onCreate(selectedType, finalTitle)
                },
                colors = ButtonDefaults.buttonColors(containerColor = selectedType.color)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ModuleOption(
    type: DocType,
    badge: String,
    icon: ImageVector,
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) type.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) type.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Surface(
                color = type.color,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
