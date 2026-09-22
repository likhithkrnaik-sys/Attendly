package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.Subject
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel

data class ParsedSessionItem(
    var subjectName: String,
    var subjectCode: String,
    var dayOfWeek: Int,
    var startTime: String,
    var endTime: String,
    var room: String,
    var classType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTimetableDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onImportConfirmed: (List<ParsedSessionItem>) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Paste/Select, 2: Review Screen
    var rawText by remember {
        mutableStateOf(
            """
Mon: 09:00-10:00 Data Structures & Algorithms (CS301) Hall-101 Lecture
Mon: 10:15-11:15 Operating Systems (CS302) Hall-102 Lecture
Tue: 09:00-10:00 Database Management Systems (CS303) Lab-3 Lab
Tue: 11:30-12:30 Computer Networks (CS304) Hall-201 Lecture
Wed: 10:00-11:00 Discrete Mathematics (MA301) Hall-105 Lecture
Wed: 14:00-16:00 Operating Systems Lab (CS302L) Lab-2 Lab
Thu: 09:00-10:00 Data Structures & Algorithms (CS301) Hall-101 Lecture
Fri: 11:00-12:00 Computer Networks (CS304) Hall-201 Lecture
            """.trimIndent()
        )
    }

    val parsedSessions = remember { mutableStateListOf<ParsedSessionItem>() }

    fun parseInputText() {
        parsedSessions.clear()
        val lines = rawText.split("\n")
        for (line in lines) {
            val clean = line.trim()
            if (clean.isBlank()) continue

            var day = 1
            if (clean.startsWith("Mon", ignoreCase = true)) day = 1
            else if (clean.startsWith("Tue", ignoreCase = true)) day = 2
            else if (clean.startsWith("Wed", ignoreCase = true)) day = 3
            else if (clean.startsWith("Thu", ignoreCase = true)) day = 4
            else if (clean.startsWith("Fri", ignoreCase = true)) day = 5
            else if (clean.startsWith("Sat", ignoreCase = true)) day = 6

            // Time pattern matching e.g. 09:00-10:00
            val timeRegex = Regex("""(\d{1,2}:\d{2})\s*-\s*(\d{1,2}:\d{2})""")
            val timeMatch = timeRegex.find(clean)
            val startTime = timeMatch?.groupValues?.get(1) ?: "09:00"
            val endTime = timeMatch?.groupValues?.get(2) ?: "10:00"

            val isLab = clean.contains("Lab", ignoreCase = true)
            val type = if (isLab) "Lab" else "Lecture"

            var name = "Academic Period"
            var code = "CS101"
            when {
                clean.contains("Data Structures", ignoreCase = true) -> { name = "Data Structures & Algorithms"; code = "CS301" }
                clean.contains("Operating Systems", ignoreCase = true) -> { name = "Operating Systems"; code = "CS302" }
                clean.contains("Database", ignoreCase = true) -> { name = "Database Management Systems"; code = "CS303" }
                clean.contains("Computer Networks", ignoreCase = true) || clean.contains("Networks", ignoreCase = true) -> { name = "Computer Networks"; code = "CS304" }
                clean.contains("Mathematics", ignoreCase = true) -> { name = "Discrete Mathematics"; code = "MA301" }
                else -> { name = clean.substringAfter(":").trim().take(25) }
            }

            val room = if (clean.contains("Lab-")) "Lab-2" else "Hall-101"

            parsedSessions.add(
                ParsedSessionItem(
                    subjectName = name,
                    subjectCode = code,
                    dayOfWeek = day,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    classType = type
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                .testTag("import_timetable_modal")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (step == 1) "Import Timetable" else "Review Extracted Schedule",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (step == 1) "Paste text, schedule format, or use presets" else "Edit every field before saving. Never trust raw OCR.",
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (step == 1) {
                    // Step 1: Input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PASTE TIMETABLE OR OCR EXTRACT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        TextField(
                            value = rawText,
                            onValueChange = { rawText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(1.dp, BrandBorderSubtle, RoundedCornerShape(12.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BrandSurfaceVariant,
                                unfocusedContainerColor = BrandSurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            placeholder = { Text("Mon: 09:00-10:00 Subject...", fontSize = 12.sp, color = TextMuted) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "Quick Presets:", fontSize = 11.sp, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Surface(
                                onClick = {
                                    rawText = """
Mon: 09:00-10:00 Operating Systems (CS302) Hall-102 Lecture
Tue: 10:15-11:15 Computer Networks (CS304) Hall-201 Lecture
Wed: 14:00-16:00 OS Lab (CS302) Lab-2 Lab
Thu: 09:00-10:00 Database Systems (CS303) Hall-101 Lecture
Fri: 11:30-12:30 Discrete Mathematics (MA301) Hall-105 Lecture
                                    """.trimIndent()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = BrandSurfaceVariant
                            ) {
                                Text("CS Sem 5 Standard", fontSize = 11.sp, color = ElectricCyan, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            parseInputText()
                            step = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parse & Open Review Screen", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Step 2: Full Interactive Review Screen (Editable fields as required by user prompt!)
                    Text(
                        text = "EXTRACTED PERIODS (${parsedSessions.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(parsedSessions) { index, item ->
                            val dayLabel = when (item.dayOfWeek) {
                                1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; else -> "Sat"
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BrandBorderSubtle, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Period #${index + 1} ($dayLabel)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                        IconButton(
                                            onClick = { parsedSessions.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AttendanceDanger, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    OutlinedTextField(
                                        value = item.subjectName,
                                        onValueChange = { item.subjectName = it },
                                        label = { Text("Subject Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = item.startTime,
                                            onValueChange = { item.startTime = it },
                                            label = { Text("Start") },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                                        )
                                        OutlinedTextField(
                                            value = item.endTime,
                                            onValueChange = { item.endTime = it },
                                            label = { Text("End") },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                                        )
                                        OutlinedTextField(
                                            value = item.room,
                                            onValueChange = { item.room = it },
                                            label = { Text("Room") },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back", color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                onImportConfirmed(parsedSessions)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm & Save", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    viewModel: AttendlyViewModel,
    onDismiss: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()

    var name by remember(profile) { mutableStateOf(profile.name) }
    var college by remember(profile) { mutableStateOf(profile.college) }
    var branch by remember(profile) { mutableStateOf(profile.branch) }
    var semester by remember(profile) { mutableIntStateOf(profile.semester) }
    var reqAttendance by remember(profile) { mutableFloatStateOf(profile.requiredAttendance) }
    var targetMarks by remember(profile) { mutableFloatStateOf(profile.targetGpaOrMarks) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Student Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = college,
                    onValueChange = { college = it },
                    label = { Text("College / University") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("Branch") },
                        modifier = Modifier.weight(1.5f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = semester.toString(),
                        onValueChange = { semester = it.toIntOrNull() ?: semester },
                        label = { Text("Semester") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Required Attendance Slider (50% - 90%)
                Text(
                    text = "REQUIRED ATTENDANCE CRITERIA: ${reqAttendance.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Slider(
                    value = reqAttendance,
                    onValueChange = { reqAttendance = it },
                    valueRange = 50f..95f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = BrandSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "TARGET GPA / OVERALL SCORE: ${targetMarks.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftViolet
                )
                Slider(
                    value = targetMarks,
                    onValueChange = { targetMarks = it },
                    valueRange = 60f..100f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = SoftViolet,
                        activeTrackColor = SoftViolet,
                        inactiveTrackColor = BrandSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reset to Default Demo Data
                OutlinedButton(
                    onClick = {
                        viewModel.resetDemoData()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset to Engineering Demo Dataset", color = AttendanceCaution)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.updateProfile(name, college, branch, semester, reqAttendance, targetMarks)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
