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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Collaborator
import com.example.data.model.DocumentEntity
import com.example.ui.theme.ArcPrimary
import com.example.ui.theme.ColorSlideAmber
import com.example.ui.theme.StatusSuccess
import com.example.ui.utils.isTablet
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SlideModel(
    val id: String,
    val layout: String, // TITLE, BULLETS, TWO_COLUMN, QUOTE
    var title: String,
    var subtitle: String = "",
    var bullets: MutableList<String> = mutableListOf(),
    var leftTitle: String = "",
    var leftContent: String = "",
    var rightTitle: String = "",
    var rightContent: String = "",
    var quote: String = "",
    var quoteAuthor: String = "",
    var notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidesEditorScreen(
    document: DocumentEntity,
    collaborators: List<Collaborator>,
    onBack: () -> Unit,
    onSaveContent: (String, String?) -> Unit
) {
    BackHandler { onBack() }

    var title by remember(document.id) { mutableStateOf(document.title) }

    // Parse slides from JSON content
    val slides = remember(document.id) {
        val list = mutableStateListOf<SlideModel>()
        try {
            val jsonArray = JSONArray(document.content)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val layout = obj.optString("layout", "TITLE")
                val slide = SlideModel(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    layout = layout,
                    title = obj.optString("title", "Untitled Slide"),
                    subtitle = obj.optString("subtitle", ""),
                    leftTitle = obj.optString("leftTitle", ""),
                    leftContent = obj.optString("leftContent", ""),
                    rightTitle = obj.optString("rightTitle", ""),
                    rightContent = obj.optString("rightContent", ""),
                    quote = obj.optString("quote", ""),
                    quoteAuthor = obj.optString("quoteAuthor", ""),
                    notes = obj.optString("notes", "")
                )
                val bulletsArr = obj.optJSONArray("bullets")
                if (bulletsArr != null) {
                    for (b in 0 until bulletsArr.length()) {
                        slide.bullets.add(bulletsArr.getString(b))
                    }
                }
                list.add(slide)
            }
        } catch (_: Exception) {
            // Default 3 starter slides
            list.add(
                SlideModel(
                    id = "s1",
                    layout = "TITLE",
                    title = document.title,
                    subtitle = "Executive Presentation Deck"
                )
            )
            list.add(
                SlideModel(
                    id = "s2",
                    layout = "BULLETS",
                    title = "Key Strategic Objectives",
                    subtitle = "High-impact goals for Q4",
                    bullets = mutableListOf(
                        "Achieve 99.9% real-time collaborative uptime",
                        "Expand Arc Suite multi-seat enterprise customer base",
                        "Deliver high-precision PDF digital signing experience"
                    )
                )
            )
        }
        if (list.isEmpty()) {
            list.add(SlideModel(id = "s1", layout = "TITLE", title = "Welcome to Arc Slides", subtitle = "Click to present"))
        }
        list
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    var isPresentingMode by remember { mutableStateOf(false) }
    var showSpeakerNotes by remember { mutableStateOf(false) }

    fun saveDeck() {
        val array = JSONArray()
        for (s in slides) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("layout", s.layout)
            obj.put("title", s.title)
            obj.put("subtitle", s.subtitle)
            obj.put("leftTitle", s.leftTitle)
            obj.put("leftContent", s.leftContent)
            obj.put("rightTitle", s.rightTitle)
            obj.put("rightContent", s.rightContent)
            obj.put("quote", s.quote)
            obj.put("quoteAuthor", s.quoteAuthor)
            obj.put("notes", s.notes)
            val bArr = JSONArray()
            for (b in s.bullets) {
                bArr.put(b)
            }
            obj.put("bullets", bArr)
            array.put(obj)
        }
        onSaveContent(array.toString(), title)
    }

    val activeSlide = slides.getOrNull(currentSlideIndex) ?: slides.firstOrNull()

    // Fullscreen Presenter Mode
    if (isPresentingMode && activeSlide != null) {
        FullscreenSlideshow(
            activeSlide = activeSlide,
            currentIndex = currentSlideIndex,
            totalSlides = slides.size,
            onPrev = {
                if (currentSlideIndex > 0) currentSlideIndex--
            },
            onNext = {
                if (currentSlideIndex < slides.size - 1) currentSlideIndex++
            },
            onExit = { isPresentingMode = false }
        )
        return
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
                                saveDeck()
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
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Arc Slides Cloud • Slide ${currentSlideIndex + 1} of ${slides.size}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Present button
                    IconButton(onClick = { isPresentingMode = true }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Present Slideshow",
                            tint = ColorSlideAmber
                        )
                    }
                    // Speaker notes toggle
                    IconButton(onClick = { showSpeakerNotes = !showSpeakerNotes }) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Speaker Notes",
                            tint = if (showSpeakerNotes) ColorSlideAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { saveDeck() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Deck", tint = ColorSlideAmber)
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
            // Layout selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Layout:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                listOf("TITLE", "BULLETS", "TWO_COLUMN", "QUOTE").forEach { layoutType ->
                    val isSelected = activeSlide?.layout == layoutType
                    Surface(
                        onClick = {
                            if (activeSlide != null) {
                                val updated = activeSlide.copy(layout = layoutType)
                                slides[currentSlideIndex] = updated
                                saveDeck()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ColorSlideAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) ColorSlideAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = layoutType.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ColorSlideAmber else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Main Slide Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activeSlide != null) {
                    SlideEditorCard(
                        slide = activeSlide,
                        onUpdate = {
                            saveDeck()
                        }
                    )
                }
            }

            // Collapsible Speaker Notes Area
            AnimatedVisibility(visible = showSpeakerNotes && activeSlide != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Speaker Notes (Slide ${currentSlideIndex + 1}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ColorSlideAmber
                        )
                        OutlinedTextField(
                            value = activeSlide?.notes ?: "",
                            onValueChange = {
                                activeSlide?.notes = it
                                saveDeck()
                            },
                            placeholder = { Text("Add private speaker talking points...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Bottom Slides Strip (Thumbnails + Add Slide)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(slides) { idx, slide ->
                            val isSelected = idx == currentSlideIndex
                            Card(
                                onClick = { currentSlideIndex = idx },
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ColorSlideAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) ColorSlideAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.size(width = 68.dp, height = 48.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ColorSlideAmber else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Add Slide Button
                    IconButton(
                        onClick = {
                            val newSlide = SlideModel(
                                id = UUID.randomUUID().toString(),
                                layout = "BULLETS",
                                title = "New Topic",
                                subtitle = "Presentation Section",
                                bullets = mutableListOf("Point 1", "Point 2")
                            )
                            slides.add(newSlide)
                            currentSlideIndex = slides.size - 1
                            saveDeck()
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Slide", tint = ColorSlideAmber)
                    }

                    // Delete current slide if more than 1
                    if (slides.size > 1) {
                        IconButton(
                            onClick = {
                                slides.removeAt(currentSlideIndex)
                                currentSlideIndex = currentSlideIndex.coerceAtMost(slides.size - 1)
                                saveDeck()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Slide", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideEditorCard(
    slide: SlideModel,
    onUpdate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            when (slide.layout) {
                "TITLE" -> {
                    TextField(
                        value = slide.title,
                        onValueChange = {
                            slide.title = it
                            onUpdate()
                        },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        placeholder = { Text("Title of Slide", textAlign = TextAlign.Center) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ColorSlideAmber
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = slide.subtitle,
                        onValueChange = {
                            slide.subtitle = it
                            onUpdate()
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
                        placeholder = { Text("Subtitle goes here", textAlign = TextAlign.Center) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ColorSlideAmber.copy(alpha = 0.5f)
                        )
                    )
                }

                "BULLETS" -> {
                    TextField(
                        value = slide.title,
                        onValueChange = {
                            slide.title = it
                            onUpdate()
                        },
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        placeholder = { Text("Slide Heading") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ColorSlideAmber
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        slide.bullets.forEachIndexed { bIdx, bullet ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("• ", fontWeight = FontWeight.Bold, color = ColorSlideAmber)
                                TextField(
                                    value = bullet,
                                    onValueChange = {
                                        slide.bullets[bIdx] = it
                                        onUpdate()
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = ColorSlideAmber.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                slide.bullets.add("New Bullet Item")
                                onUpdate()
                            }
                        ) {
                            Text("+ Add Bullet Point", color = ColorSlideAmber, fontSize = 12.sp)
                        }
                    }
                }

                "TWO_COLUMN" -> {
                    TextField(
                        value = slide.title,
                        onValueChange = {
                            slide.title = it
                            onUpdate()
                        },
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        placeholder = { Text("Slide Heading") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ColorSlideAmber
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            TextField(
                                value = slide.leftTitle,
                                onValueChange = {
                                    slide.leftTitle = it
                                    onUpdate()
                                },
                                textStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                placeholder = { Text("Left Column") },
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            TextField(
                                value = slide.leftContent,
                                onValueChange = {
                                    slide.leftContent = it
                                    onUpdate()
                                },
                                textStyle = MaterialTheme.typography.bodySmall,
                                placeholder = { Text("Column content...") },
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            TextField(
                                value = slide.rightTitle,
                                onValueChange = {
                                    slide.rightTitle = it
                                    onUpdate()
                                },
                                textStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                placeholder = { Text("Right Column") },
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            TextField(
                                value = slide.rightContent,
                                onValueChange = {
                                    slide.rightContent = it
                                    onUpdate()
                                },
                                textStyle = MaterialTheme.typography.bodySmall,
                                placeholder = { Text("Column content...") },
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                        }
                    }
                }

                "QUOTE" -> {
                    TextField(
                        value = slide.quote,
                        onValueChange = {
                            slide.quote = it
                            onUpdate()
                        },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        ),
                        placeholder = { Text("\"Insert inspiring quote here...\"", textAlign = TextAlign.Center) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ColorSlideAmber
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = slide.quoteAuthor,
                        onValueChange = {
                            slide.quoteAuthor = it
                            onUpdate()
                        },
                        textStyle = MaterialTheme.typography.labelMedium.copy(
                            textAlign = TextAlign.Center,
                            color = ColorSlideAmber
                        ),
                        placeholder = { Text("— Author Name", textAlign = TextAlign.Center) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ColorSlideAmber.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FullscreenSlideshow(
    activeSlide: SlideModel,
    currentIndex: Int,
    totalSlides: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onExit: () -> Unit
) {
    BackHandler { onExit() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .clickable { onNext() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (activeSlide.layout) {
                "TITLE" -> {
                    Text(
                        text = activeSlide.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    if (activeSlide.subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = activeSlide.subtitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = ColorSlideAmber,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                "BULLETS" -> {
                    Text(
                        text = activeSlide.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        activeSlide.bullets.forEach { b ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✦ ", color = ColorSlideAmber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    text = b,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
                "QUOTE" -> {
                    Text(
                        text = "\"${activeSlide.quote}\"",
                        style = MaterialTheme.typography.headlineSmall,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "— ${activeSlide.quoteAuthor}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ColorSlideAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {
                    Text(
                        text = activeSlide.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = activeSlide.leftTitle, fontWeight = FontWeight.Bold, color = ColorSlideAmber)
                            Text(text = activeSlide.leftContent, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = activeSlide.rightTitle, fontWeight = FontWeight.Bold, color = ColorSlideAmber)
                            Text(text = activeSlide.rightContent, color = Color.White)
                        }
                    }
                }
            }
        }

        // Presenter Overlay Controls (Top Right Exit, Bottom Controls)
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Exit Presentation", tint = Color.White)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", tint = Color.White)
            }
            Text(
                text = "${currentIndex + 1} / $totalSlides",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}
