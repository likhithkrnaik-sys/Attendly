package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassSession
import com.example.data.model.Subject
import com.example.engine.AttendanceMathEngine
import com.example.engine.AttendanceStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: AttendlyViewModel,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAcademics: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onOpenBunkCalculator: (Subject?) -> Unit,
    onOpenAskAttendly: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddExam: () -> Unit,
    onOpenAddMarks: () -> Unit,
    onOpenAddNote: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val overallStats by viewModel.overallAttendance.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val insights by viewModel.dynamicInsights.collectAsState()

    val todayDayOfWeek = viewModel.currentDayOfWeek
    val todaySessions = remember(sessions, todayDayOfWeek) {
        sessions.filter { it.dayOfWeek == todayDayOfWeek }.sortedBy { it.startTime }
    }

    val nextExam = remember(exams) {
        val now = System.currentTimeMillis()
        exams.filter { it.epochMillis > now }.minByOrNull { it.epochMillis }
    }

    // Greeting time based
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top App Bar / Header
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
                            text = "$greeting, ${profile.name.split(" ").firstOrNull() ?: "Student"}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Today's Academic Overview • Sem ${profile.semester}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenAskAttendly,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoyalIndigo.copy(alpha = 0.25f))
                                .testTag("home_ask_attendly_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Ask Attendly",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandSurfaceVariant)
                                .testTag("home_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Attendance Gauge Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToAttendance
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OVERALL ATTENDANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${overallStats.totalAttended} / ${overallStats.totalConducted} Classes",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                        StatusBadge(status = overallStats.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AttendanceRing(
                            percentage = overallStats.percentage,
                            requiredPercentage = overallStats.requiredPercentage,
                            size = 140.dp,
                            strokeWidth = 12.dp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandSurfaceVariant,
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Required", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "${overallStats.requiredPercentage.toInt()}%",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandSurfaceVariant,
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Safe Miss Buffer", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "${overallStats.totalSafeMisses} classes",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (overallStats.totalSafeMisses > 3) AttendanceSafe else AttendanceCaution
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tap to see details action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View Subject Breakdown",
                            fontSize = 12.sp,
                            color = ElectricCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickActionChip(
                            label = "Can I Miss?",
                            icon = Icons.Rounded.Calculate,
                            testTag = "quick_can_i_miss",
                            tintColor = ElectricCyan,
                            onClick = { onOpenBunkCalculator(subjects.firstOrNull()) }
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Mark All Present",
                            icon = Icons.Rounded.CheckCircle,
                            testTag = "quick_mark_all_present",
                            tintColor = AttendanceSafe,
                            onClick = { viewModel.markAllTodayPresent(todaySessions) }
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "+ Add Exam",
                            icon = Icons.Rounded.Event,
                            testTag = "quick_add_exam",
                            tintColor = SoftViolet,
                            onClick = onOpenAddExam
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "+ Add Marks",
                            icon = Icons.Rounded.Grade,
                            testTag = "quick_add_marks",
                            tintColor = SubjectColorAmber,
                            onClick = onOpenAddMarks
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "+ Upload Notes",
                            icon = Icons.Rounded.UploadFile,
                            testTag = "quick_upload_notes",
                            tintColor = SubjectColorRose,
                            onClick = onOpenAddNote
                        )
                    }
                }
            }
        }

        // Smart Attendance Insight
        item {
            val topInsight = insights.firstOrNull()
            if (topInsight != null) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "SMART ATTENDANCE INSIGHT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = when (topInsight.type) {
                            AttendanceStatus.SAFE -> AttendanceSafe.copy(alpha = 0.12f)
                            AttendanceStatus.CAUTION -> AttendanceCaution.copy(alpha = 0.12f)
                            AttendanceStatus.DANGER -> AttendanceDanger.copy(alpha = 0.12f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                when (topInsight.type) {
                                    AttendanceStatus.SAFE -> AttendanceSafe.copy(alpha = 0.4f)
                                    AttendanceStatus.CAUTION -> AttendanceCaution.copy(alpha = 0.4f)
                                    AttendanceStatus.DANGER -> AttendanceDanger.copy(alpha = 0.4f)
                                },
                                RoundedCornerShape(18.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = when (topInsight.type) {
                                    AttendanceStatus.SAFE -> Icons.Default.CheckCircle
                                    AttendanceStatus.CAUTION -> Icons.Default.Warning
                                    AttendanceStatus.DANGER -> Icons.Default.Error
                                },
                                contentDescription = null,
                                tint = when (topInsight.type) {
                                    AttendanceStatus.SAFE -> AttendanceSafe
                                    AttendanceStatus.CAUTION -> AttendanceCaution
                                    AttendanceStatus.DANGER -> AttendanceDanger
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = topInsight.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = topInsight.description,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Exam Hero Card
        if (nextExam != null) {
            item {
                val now = System.currentTimeMillis()
                val diffMillis = (nextExam.epochMillis - now).coerceAtLeast(0L)
                val daysRemaining = diffMillis / (24L * 60 * 60 * 1000)
                val hoursRemaining = (diffMillis % (24L * 60 * 60 * 1000)) / (60 * 60 * 1000)

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UPCOMING EXAM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "In $daysRemaining days $hoursRemaining hrs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftViolet
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToAcademics
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nextExam.subjectName.uppercase(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${nextExam.name} • ${nextExam.venue}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            // Countdown Pill
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = RoyalIndigo.copy(alpha = 0.25f),
                                modifier = Modifier.border(1.dp, SoftViolet.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "$daysRemaining",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "DAYS LEFT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Classes List
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S CLASSES (${todaySessions.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    if (todaySessions.isNotEmpty()) {
                        TextButton(
                            onClick = onNavigateToTimetable,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Full Timetable ➔",
                                fontSize = 12.sp,
                                color = ElectricCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (todaySessions.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrandSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrandBorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Weekend,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No classes scheduled for today",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Text(
                                text = "Enjoy your day or catch up on revision!",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        items(todaySessions) { session ->
            val sub = subjects.find { it.id == session.subjectId }
            val subPercentage = if (sub != null) {
                AttendanceMathEngine.calculatePercentage(sub.attendedClasses, sub.conductedClasses)
            } else 100.0

            val subStatus = when {
                subPercentage < profile.requiredAttendance -> AttendanceStatus.DANGER
                subPercentage < profile.requiredAttendance + 3.0 -> AttendanceStatus.CAUTION
                else -> AttendanceStatus.SAFE
            }

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorderSubtle, RoundedCornerShape(16.dp))
                        .testTag("class_session_${session.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${session.startTime} - ${session.endTime}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Room: ${session.room}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = session.subjectName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Attendance: %.1f%%".format(subPercentage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when (subStatus) {
                                        AttendanceStatus.SAFE -> AttendanceSafe
                                        AttendanceStatus.CAUTION -> AttendanceCaution
                                        AttendanceStatus.DANGER -> AttendanceDanger
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(status = subStatus)
                            }
                        }

                        // Cancelled / Absent / Present fast action buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.markSessionCancelled(session.subjectId, session.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SubjectColorAmber.copy(alpha = 0.15f))
                                    .testTag("mark_cancelled_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EventBusy,
                                    contentDescription = "Class Cancelled",
                                    tint = SubjectColorAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { viewModel.markSessionAttendance(session.subjectId, false) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AttendanceDanger.copy(alpha = 0.15f))
                                    .testTag("mark_absent_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Mark Absent",
                                    tint = AttendanceDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { viewModel.markSessionAttendance(session.subjectId, true) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AttendanceSafe.copy(alpha = 0.2f))
                                    .testTag("mark_present_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Mark Present",
                                    tint = AttendanceSafe,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
