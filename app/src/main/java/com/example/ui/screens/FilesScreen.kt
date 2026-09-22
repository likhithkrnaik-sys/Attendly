package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AcademicNote
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: AttendlyViewModel,
    onOpenAddNote: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var selectedNoteForIntelligence by remember { mutableStateOf<AcademicNote?>(null) }

    val filteredNotes = remember(notes, searchQuery, selectedSubjectFilter) {
        notes.filter { note ->
            val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) ||
                    note.summaryOrTopics.contains(searchQuery, ignoreCase = true) ||
                    note.subjectName.contains(searchQuery, ignoreCase = true)
            val matchesSubject = selectedSubjectFilter == "All" || note.subjectName == selectedSubjectFilter
            matchesSearch && matchesSubject
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .testTag("files_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Academic Files",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Notes, Question Papers & AI Topic Intelligence",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onOpenAddNote,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RoyalIndigo)
                            .testTag("add_file_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Upload Note",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search notes, topics, modules...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BrandSurfaceVariant,
                        unfocusedContainerColor = BrandSurfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorderSubtle, RoundedCornerShape(16.dp))
                        .testTag("notes_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubjectFilter == "All",
                            onClick = { selectedSubjectFilter = "All" },
                            label = { Text("All Subjects", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = Color(0xFF003548),
                                containerColor = BrandSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    items(subjects) { sub ->
                        FilterChip(
                            selected = selectedSubjectFilter == sub.name,
                            onClick = { selectedSubjectFilter = sub.name },
                            label = { Text(sub.code, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = Color(0xFF003548),
                                containerColor = BrandSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Notes List
        items(filteredNotes) { note ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                Surface(
                    onClick = { selectedNoteForIntelligence = note },
                    shape = RoundedCornerShape(18.dp),
                    color = BrandSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorderSubtle, RoundedCornerShape(18.dp))
                        .testTag("note_item_${note.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // File type icon box
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RoyalIndigo.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (note.noteType) {
                                    "PDF" -> Icons.Rounded.PictureAsPdf
                                    "PYQ" -> Icons.Rounded.Quiz
                                    "Cheatsheet" -> Icons.Rounded.Bookmark
                                    else -> Icons.Rounded.Description
                                },
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${note.subjectName} • ${note.module} • ${note.pageCount} pages",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            if (note.summaryOrTopics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SoftViolet,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Topics: ${note.summaryOrTopics.take(45)}...",
                                        fontSize = 11.sp,
                                        color = SoftViolet
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteNote(note.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // AI Note Intelligence Dialog
    selectedNoteForIntelligence?.let { note ->
        Dialog(onDismissRequest = { selectedNoteForIntelligence = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BrandSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI File Intelligence",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        IconButton(
                            onClick = { selectedNoteForIntelligence = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = note.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${note.subjectName} • ${note.module}",
                        fontSize = 12.sp,
                        color = ElectricCyan
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BrandSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrandBorderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "EXTRACTED CONCEPTS & EXAM TOPICS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val topics = note.summaryOrTopics.split(",")
                            topics.forEach { topic ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(AttendanceSafe)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = topic.trim(),
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { selectedNoteForIntelligence = null },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Close", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
