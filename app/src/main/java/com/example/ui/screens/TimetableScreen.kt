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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassSession
import com.example.data.model.Subject
import com.example.engine.AttendanceMathEngine
import com.example.engine.AttendanceStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: AttendlyViewModel,
    onOpenAddSession: () -> Unit,
    onOpenImportTimetable: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val selectedDay by viewModel.selectedTimetableDay.collectAsState()

    val daysOfWeek = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat"
    )

    val currentDaySessions = remember(sessions, selectedDay) {
        sessions.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startTime }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .testTag("timetable_screen"),
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
                            text = "Timetable",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Weekly Schedule & Periods",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onOpenImportTimetable,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandSurfaceVariant)
                                .testTag("import_timetable_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DocumentScanner,
                                contentDescription = "Import Timetable",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenAddSession,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoyalIndigo)
                                .testTag("add_session_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Class",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day Selector Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daysOfWeek) { (dayNum, dayLabel) ->
                        val isSelected = selectedDay == dayNum
                        val isToday = viewModel.currentDayOfWeek == dayNum

                        Surface(
                            onClick = { viewModel.setSelectedTimetableDay(dayNum) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) ElectricCyan else BrandSurfaceVariant,
                            modifier = Modifier
                                .width(52.dp)
                                .height(56.dp)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else BrandBorderSubtle,
                                    RoundedCornerShape(14.dp)
                                )
                                .testTag("day_tab_$dayLabel")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF003548) else TextPrimary
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF003548) else ElectricCyan)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick action: Mark all present for this day
        if (currentDaySessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentDaySessions.size} CLASSES SCHEDULED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = { viewModel.markAllTodayPresent(currentDaySessions) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AttendanceSafe.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("mark_all_present_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AttendanceSafe,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark All Present",
                            color = AttendanceSafe,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (currentDaySessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.EventAvailable,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Classes Scheduled",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add custom classes or import your timetable",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenAddSession,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Text(
                                text = "+ Add Class Session",
                                color = Color(0xFF003548),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Sessions List
        items(currentDaySessions) { session ->
            val sub = subjects.find { it.id == session.subjectId }
            val subPercentage = if (sub != null) {
                AttendanceMathEngine.calculatePercentage(sub.attendedClasses, sub.conductedClasses)
            } else 100.0

            val subStatus = when {
                subPercentage < profile.requiredAttendance -> AttendanceStatus.DANGER
                subPercentage < profile.requiredAttendance + 3.0 -> AttendanceStatus.CAUTION
                else -> AttendanceStatus.SAFE
            }

            val subjectColor = try {
                Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#38BDF8"))
            } catch (e: Exception) {
                ElectricCyan
            }

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BrandSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${session.startTime} - ${session.endTime}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BrandSurfaceElevated
                                ) {
                                    Text(
                                        text = session.classType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            StatusBadge(status = subStatus)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = session.subjectName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Room: ${session.room} ${if (session.faculty.isNotEmpty()) "• ${session.faculty}" else ""}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Attendance: %.1f%%".format(subPercentage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (subStatus) {
                                    AttendanceStatus.SAFE -> AttendanceSafe
                                    AttendanceStatus.CAUTION -> AttendanceCaution
                                    AttendanceStatus.DANGER -> AttendanceDanger
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BrandBorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions: Mark Present / Mark Absent / Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.deleteSession(session.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Session",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.markSessionCancelled(session.subjectId, session.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SubjectColorAmber.copy(alpha = 0.15f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("session_cancelled_${session.id}")
                                ) {
                                    Text(text = "Cancelled", color = SubjectColorAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.markSessionAttendance(session.subjectId, false) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceDanger.copy(alpha = 0.15f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("session_absent_${session.id}")
                                ) {
                                    Text(text = "Absent", color = AttendanceDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.markSessionAttendance(session.subjectId, true) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceSafe.copy(alpha = 0.2f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("session_present_${session.id}")
                                ) {
                                    Text(text = "Present", color = AttendanceSafe, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
