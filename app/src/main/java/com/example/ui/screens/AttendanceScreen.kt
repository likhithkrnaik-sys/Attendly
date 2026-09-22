package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.engine.AttendanceMathEngine
import com.example.engine.AttendanceStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel

@Composable
fun AttendanceScreen(
    viewModel: AttendlyViewModel,
    onOpenBunkCalculator: (Subject?) -> Unit,
    onOpenAddSubject: () -> Unit
) {
    val overallStats by viewModel.overallAttendance.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val prioritySubjects by viewModel.prioritySubjects.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .testTag("attendance_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
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
                            text = "Attendance Engine",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Deterministic Planning & Risk Analysis",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onOpenAddSubject,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RoyalIndigo)
                            .testTag("add_subject_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subject",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Overall Attendance Hero
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SEMESTER ATTENDANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${overallStats.totalAttended} / ${overallStats.totalConducted} Total Classes",
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
                            size = 135.dp,
                            strokeWidth = 12.dp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandSurfaceVariant,
                                modifier = Modifier.width(140.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Safety Margin", fontSize = 10.sp, color = TextMuted)
                                    val marginText = if (overallStats.safetyMargin >= 0) "+%.1f%%".format(overallStats.safetyMargin) else "%.1f%%".format(overallStats.safetyMargin)
                                    Text(
                                        text = marginText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (overallStats.safetyMargin >= 0) AttendanceSafe else AttendanceDanger
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandSurfaceVariant,
                                modifier = Modifier.width(140.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Required Target", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "${overallStats.requiredPercentage.toInt()}%",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Prominent "Can I Miss This Class?" Banner
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Surface(
                    onClick = { onOpenBunkCalculator(subjects.firstOrNull()) },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1B4B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SoftViolet.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .testTag("can_i_miss_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Calculate,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CAN I MISS THIS CLASS?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricCyan,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Simulate how missing 1, 2, or 3 classes impacts your semester percentage before taking leave.",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SoftViolet,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Smart Priority: "Classes I should NOT miss"
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "CLASSES I SHOULD NOT MISS (PRIORITY RANKING)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                prioritySubjects.take(3).forEach { p ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when (p.status) {
                            AttendanceStatus.DANGER -> AttendanceDanger.copy(alpha = 0.12f)
                            AttendanceStatus.CAUTION -> AttendanceCaution.copy(alpha = 0.12f)
                            AttendanceStatus.SAFE -> BrandSurfaceVariant
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                1.dp,
                                when (p.status) {
                                    AttendanceStatus.DANGER -> AttendanceDanger.copy(alpha = 0.4f)
                                    AttendanceStatus.CAUTION -> AttendanceCaution.copy(alpha = 0.4f)
                                    AttendanceStatus.SAFE -> BrandBorderSubtle
                                },
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = p.subjectName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${p.currentPercentage}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when (p.status) {
                                            AttendanceStatus.DANGER -> AttendanceDanger
                                            AttendanceStatus.CAUTION -> AttendanceCaution
                                            AttendanceStatus.SAFE -> AttendanceSafe
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = p.primaryReason,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            StatusBadge(status = p.status)
                        }
                    }
                }
            }
        }

        // Subject-Wise Attendance Breakdown
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBJECT-WISE BREAKDOWN (${subjects.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Req: ${profile.requiredAttendance.toInt()}%",
                        fontSize = 11.sp,
                        color = ElectricCyan
                    )
                }
            }
        }

        items(subjects) { subject ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                SubjectAttendanceCard(
                    subject = subject,
                    requiredPercentage = profile.requiredAttendance.toDouble(),
                    onSimulateMiss = { onOpenBunkCalculator(subject) },
                    onMarkPresent = { viewModel.markClassPresent(subject.id) },
                    onMarkAbsent = { viewModel.markClassAbsent(subject.id) },
                    onMarkCancelled = { viewModel.markSubjectClassCancelled(subject.id) },
                    onUndoCancelled = if (subject.cancelledClasses > 0) {
                        { viewModel.undoCancelledClass(subject.id) }
                    } else null,
                    onAdjustAttendance = { newAttended, newConducted ->
                        viewModel.updateSubjectAttendance(subject.id, newAttended, newConducted, subject.cancelledClasses)
                    }
                )
            }
        }
    }
}
