package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ColorAdobeAe
import com.example.ui.theme.ColorAdobeId
import com.example.ui.theme.ColorAdobePr
import com.example.ui.theme.ColorAdobePs
import com.example.ui.theme.ColorCanvasViolet
import com.example.ui.theme.ColorDocBlue
import com.example.ui.theme.ColorPdfRed
import com.example.ui.theme.ColorSheetGreen
import com.example.ui.theme.ColorSlideAmber

enum class DocType(
    val displayName: String,
    val badge: String,
    val fileExtension: String,
    val suiteLabel: String,
    val color: Color
) {
    DOC("Arc Doc", "Wd", ".arcdoc", "Word Processor", ColorDocBlue),
    SHEET("Arc Sheet", "Xl", ".arcsheet", "Spreadsheet", ColorSheetGreen),
    SLIDE("Arc Presentation", "Pt", ".arcslide", "Slide Deck", ColorSlideAmber),
    PDF("Arc PDF", "Ac", ".pdf", "PDF Editor & Acrobat", ColorPdfRed),
    CANVAS("Arc Creative", "Ai", ".arcart", "Illustrator & Vector", ColorCanvasViolet),
    PHOTOSHOP("Arc Photoshop", "Ps", ".psd", "Photo Editing & Retouching", ColorAdobePs),
    PREMIERE("Arc Premiere", "Pr", ".prproj", "Timeline Video Editor", ColorAdobePr),
    INDESIGN("Arc InDesign", "Id", ".indd", "Editorial & Publishing Layout", ColorAdobeId),
    AFTER_EFFECTS("Arc After Effects", "Ae", ".aep", "Motion Graphics & VFX", ColorAdobeAe);

    val label: String
        get() = displayName
}
