package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Subject
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel

enum class AttendlyDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Rounded.Home),
    TIMETABLE("timetable", "Timetable", Icons.Rounded.CalendarToday),
    ATTENDANCE("attendance", "Attendance", Icons.Rounded.PieChart),
    ACADEMICS("academics", "Academics", Icons.Rounded.School),
    FILES("files", "Files", Icons.Rounded.Folder)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendlyTheme(darkTheme = true) {
                AttendlyApp()
            }
        }
    }
}

@Composable
fun AttendlyApp(viewModel: AttendlyViewModel = viewModel()) {
    var currentDestination by remember { mutableStateOf(AttendlyDestination.HOME) }

    val subjects by viewModel.subjects.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    // Dialog Visibility State
    var showBunkDialog by remember { mutableStateOf(false) }
    var bunkInitialSubject by remember { mutableStateOf<Subject?>(null) }
    var showAskDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showAddMarksDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandBackground,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            NavigationBar(
                containerColor = BrandSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = BrandBorderSubtle,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .testTag("attendly_bottom_nav")
            ) {
                AttendlyDestination.values().forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            selectedTextColor = ElectricCyan,
                            indicatorColor = RoyalIndigo.copy(alpha = 0.35f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentDestination, label = "tab_crossfade") { destination ->
                when (destination) {
                    AttendlyDestination.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTimetable = { currentDestination = AttendlyDestination.TIMETABLE },
                        onNavigateToAttendance = { currentDestination = AttendlyDestination.ATTENDANCE },
                        onNavigateToAcademics = { currentDestination = AttendlyDestination.ACADEMICS },
                        onNavigateToFiles = { currentDestination = AttendlyDestination.FILES },
                        onOpenBunkCalculator = { sub ->
                            bunkInitialSubject = sub
                            showBunkDialog = true
                        },
                        onOpenAskAttendly = { showAskDialog = true },
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenAddExam = { showAddExamDialog = true },
                        onOpenAddMarks = { showAddMarksDialog = true },
                        onOpenAddNote = { showAddNoteDialog = true }
                    )

                    AttendlyDestination.TIMETABLE -> TimetableScreen(
                        viewModel = viewModel,
                        onOpenAddSession = { showAddSessionDialog = true },
                        onOpenImportTimetable = { showImportDialog = true }
                    )

                    AttendlyDestination.ATTENDANCE -> AttendanceScreen(
                        viewModel = viewModel,
                        onOpenBunkCalculator = { sub ->
                            bunkInitialSubject = sub
                            showBunkDialog = true
                        },
                        onOpenAddSubject = { showAddSubjectDialog = true }
                    )

                    AttendlyDestination.ACADEMICS -> AcademicsScreen(
                        viewModel = viewModel,
                        onOpenAddExam = { showAddExamDialog = true },
                        onOpenAddMarks = { showAddMarksDialog = true }
                    )

                    AttendlyDestination.FILES -> FilesScreen(
                        viewModel = viewModel,
                        onOpenAddNote = { showAddNoteDialog = true }
                    )
                }
            }
        }

        // --- Dialogs ---

        if (showBunkDialog) {
            BunkCalculatorDialog(
                subjects = subjects,
                initialSubject = bunkInitialSubject,
                requiredPercentage = profile.requiredAttendance.toDouble(),
                onDismiss = { showBunkDialog = false }
            )
        }

        if (showAskDialog) {
            AskAttendlyDialog(
                viewModel = viewModel,
                onDismiss = { showAskDialog = false }
            )
        }

        if (showAddSubjectDialog) {
            AddSubjectDialog(
                onDismiss = { showAddSubjectDialog = false },
                onAdd = { name, code, faculty, credits, category, colorHex ->
                    viewModel.addSubject(name, code, faculty, credits, category, colorHex)
                }
            )
        }

        if (showAddSessionDialog) {
            AddSessionDialog(
                subjects = subjects,
                selectedDay = viewModel.selectedTimetableDay.value,
                onDismiss = { showAddSessionDialog = false },
                onAdd = { subId, subName, subCode, day, start, end, room, type ->
                    viewModel.addSession(subId, subName, subCode, day, start, end, room, type)
                }
            )
        }

        if (showAddExamDialog) {
            AddExamDialog(
                subjects = subjects,
                onDismiss = { showAddExamDialog = false },
                onAdd = { subId, subName, name, type, millis, time, venue, syllabus ->
                    viewModel.addExam(subId, subName, name, type, millis, time, venue, syllabus)
                }
            )
        }

        if (showAddMarksDialog) {
            // Re-using dialog structure for Marks
            AddMarksDialog(
                subjects = subjects,
                onDismiss = { showAddMarksDialog = false },
                onAdd = { subId, subName, component, scored, total, weight ->
                    viewModel.addMark(subId, subName, component, scored, total, weight)
                }
            )
        }

        if (showAddNoteDialog) {
            AddNoteDialog(
                subjects = subjects,
                onDismiss = { showAddNoteDialog = false },
                onAdd = { subId, subName, title, module, type, topics ->
                    viewModel.addNote(subId, subName, title, module, type, topics)
                }
            )
        }

        if (showImportDialog) {
            ImportTimetableDialog(
                subjects = subjects,
                onDismiss = { showImportDialog = false },
                onImportConfirmed = { items ->
                    items.forEach { item ->
                        val existingSub = subjects.find { it.code.equals(item.subjectCode, ignoreCase = true) || it.name.equals(item.subjectName, ignoreCase = true) }
                        val subjectId = existingSub?.id ?: (subjects.firstOrNull()?.id ?: 1L)
                        viewModel.addSession(
                            subjectId = subjectId,
                            subjectName = item.subjectName,
                            subjectCode = item.subjectCode,
                            dayOfWeek = item.dayOfWeek,
                            start = item.startTime,
                            end = item.endTime,
                            room = item.room,
                            type = item.classType
                        )
                    }
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

@Composable
fun AddMarksDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAdd: (subjectId: Long, subjectName: String, component: String, scored: Float, total: Float, weight: Float) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var componentName by remember { mutableStateOf("Internal Assessment 1") }
    var scoredMarks by remember { mutableStateOf("42") }
    var totalMarks by remember { mutableStateOf("50") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Add Internal Marks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = componentName,
                    onValueChange = { componentName = it },
                    label = { Text("Component (CIE 1, Quiz, SEE)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scoredMarks,
                        onValueChange = { scoredMarks = it },
                        label = { Text("Scored") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = { Text("Total") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val sub = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
                        if (sub != null) {
                            val scored = scoredMarks.toFloatOrNull() ?: 0f
                            val total = totalMarks.toFloatOrNull() ?: 50f
                            onAdd(sub.id, sub.name, componentName, scored, total, 25f)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Save Marks", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAdd: (subjectId: Long, subjectName: String, title: String, module: String, type: String, topics: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf("") }
    var module by remember { mutableStateOf("Unit 1") }
    var noteType by remember { mutableStateOf("PDF") }
    var topics by remember { mutableStateOf("Introduction, Core Theorems, Examples") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Upload Academic Document", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Document Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = module,
                        onValueChange = { module = it },
                        label = { Text("Module (Unit 1)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = noteType,
                        onValueChange = { noteType = it },
                        label = { Text("Type (PDF/PYQ)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = topics,
                    onValueChange = { topics = it },
                    label = { Text("AI Topics / Summary (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val sub = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
                        if (sub != null && title.isNotBlank()) {
                            onAdd(sub.id, sub.name, title, module, noteType, topics)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Save Document", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
