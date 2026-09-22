package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, code: String, faculty: String, credits: Int, category: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var faculty by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("4") }
    var category by remember { mutableStateOf("Theory") }
    val colors = listOf("#38BDF8", "#F43F5E", "#6366F1", "#10B981", "#F59E0B", "#A855F7")
    var selectedColor by remember { mutableStateOf(colors[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                .testTag("add_subject_dialog")
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
                    Text(text = "Add New Subject", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name (e.g. Operating Systems)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code (CS302)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { credits = it },
                        label = { Text("Credits") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    label = { Text("Faculty Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Category", fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    listOf("Theory", "Lab", "Elective").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Theme Color", fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                    colors.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = hex }
                                .border(if (selectedColor == hex) 3.dp else 0.dp, Color.White, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && code.isNotBlank()) {
                            onAdd(name, code, faculty, credits.toIntOrNull() ?: 4, category, selectedColor)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Create Subject", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddSessionDialog(
    subjects: List<Subject>,
    selectedDay: Int,
    onDismiss: () -> Unit,
    onAdd: (subjectId: Long, subjectName: String, subjectCode: String, dayOfWeek: Int, start: String, end: String, room: String, type: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var dayOfWeek by remember { mutableIntStateOf(selectedDay) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var room by remember { mutableStateOf("EC-101") }
    var classType by remember { mutableStateOf("Lecture") }

    val days = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
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
                    Text(text = "Add Class Session", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Select Subject", fontSize = 12.sp, color = TextMuted)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    subjects.forEach { s ->
                        val isSelected = s.id == selectedSubjectId
                        Surface(
                            onClick = { selectedSubjectId = s.id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RoyalIndigo else BrandSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${s.name} (${s.code})",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Day of Week", fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    days.forEach { (dNum, dName) ->
                        FilterChip(
                            selected = dayOfWeek == dNum,
                            onClick = { dayOfWeek = dNum },
                            label = { Text(dName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Hall (e.g. EC-101)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val sub = subjects.find { it.id == selectedSubjectId }
                        if (sub != null) {
                            onAdd(sub.id, sub.name, sub.code, dayOfWeek, startTime, endTime, room, classType)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Add to Timetable", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddExamDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAdd: (subjectId: Long, subjectName: String, name: String, type: String, epochMillis: Long, time: String, venue: String, syllabus: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var examName by remember { mutableStateOf("Internal Assessment 2") }
    var examType by remember { mutableStateOf("Internal Assessment") }
    var daysFromNow by remember { mutableStateOf("14") }
    var timeString by remember { mutableStateOf("10:00 AM") }
    var venue by remember { mutableStateOf("Hall A") }
    var syllabus by remember { mutableStateOf("Unit 3 & Unit 4") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Schedule New Exam", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = daysFromNow,
                        onValueChange = { daysFromNow = it },
                        label = { Text("In (days)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = timeString,
                        onValueChange = { timeString = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue / Room") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = syllabus,
                    onValueChange = { syllabus = it },
                    label = { Text("Syllabus / Modules Covered") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val sub = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
                        if (sub != null) {
                            val days = daysFromNow.toLongOrNull() ?: 10L
                            val targetMillis = System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000)
                            onAdd(sub.id, sub.name, examName, examType, targetMillis, timeString, venue, syllabus)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Save Exam Countdown", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
