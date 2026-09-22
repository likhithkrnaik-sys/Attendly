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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.MarkRecord
import com.example.engine.AttendanceMathEngine
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicsScreen(
    viewModel: AttendlyViewModel,
    onOpenAddExam: () -> Unit,
    onOpenAddMarks: () -> Unit
) {
    val exams by viewModel.exams.collectAsState()
    val marks by viewModel.marks.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val overallStats by viewModel.overallAttendance.collectAsState()

    var selectedCareerTrack by remember { mutableStateOf("Software Engineering") }

    val careerTracks = listOf(
        "Software Engineering",
        "AI / ML",
        "Backend & Systems",
        "Data Science",
        "Cybersecurity"
    )

    val placementRelevanceMap = remember {
        mapOf(
            "Software Engineering" to listOf(
                "Data Structures & Algorithms" to "Tier 1 Critical: Core algorithmic coding interviews (LeetCode medium/hard, graphs, DP)",
                "Operating Systems" to "High: Concurrency, threads, memory leaks, and process synchronization questions",
                "Database Management Systems" to "High: Relational schema design, normalization, query optimization, indexing",
                "Computer Networks" to "Medium: HTTP/HTTPS, REST APIs, WebSockets, TCP vs UDP for distributed systems"
            ),
            "AI / ML" to listOf(
                "Discrete Mathematics" to "Tier 1 Critical: Combinatorics, probability distributions, graph theory, mathematical proofs",
                "Data Structures & Algorithms" to "High: Efficient matrix manipulations, search algorithms, tree-based models",
                "Database Management Systems" to "High: Feature stores, data pipelines, warehousing, SQL preprocessing"
            ),
            "Backend & Systems" to listOf(
                "Operating Systems" to "Tier 1 Critical: Kernel space vs user space, epoll, file systems, IPC, CPU scheduling",
                "Database Management Systems" to "Tier 1 Critical: ACID guarantees, distributed transactions, lock contention, replica lags",
                "Computer Networks" to "Tier 1 Critical: Socket programming, DNS resolution, TCP handshake, load balancing"
            ),
            "Data Science" to listOf(
                "Database Management Systems" to "Tier 1 Critical: Complex SQL aggregation, window functions, analytics schemas",
                "Discrete Mathematics" to "Tier 1 Critical: Linear algebra foundations, graph modeling, discrete probability",
                "Data Structures & Algorithms" to "Medium: Big-O analysis, algorithmic complexity of data pipelines"
            ),
            "Cybersecurity" to listOf(
                "Computer Networks" to "Tier 1 Critical: Packet sniffing, firewall rules, port scanning, SSL/TLS handshake",
                "Operating Systems" to "Tier 1 Critical: Buffer overflow exploitation, memory layout, privileges, secure boot",
                "Discrete Mathematics" to "High: Public key cryptography (RSA, Diffie-Hellman), elliptic curves"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .testTag("academics_screen"),
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
                            text = "Academics",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Exams, Marks Analysis & Placement Readiness",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onOpenAddExam,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandSurfaceVariant)
                                .testTag("academic_add_exam_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Event,
                                contentDescription = "Add Exam",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenAddMarks,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoyalIndigo)
                                .testTag("academic_add_marks_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Grade,
                                contentDescription = "Add Marks",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Semester Overview Stats Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SEMESTER ${profile.semester} PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Overall Attendance", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "%.1f%%".format(overallStats.percentage),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        Column {
                            Text(text = "Target GPA / Marks", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${profile.targetGpaOrMarks.toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricCyan
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Scheduled Exams", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${exams.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SoftViolet
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Exams Countdown
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXAM COUNTDOWNS (${exams.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    TextButton(
                        onClick = onOpenAddExam,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "+ New Exam", fontSize = 12.sp, color = ElectricCyan)
                    }
                }
            }
        }

        items(exams) { exam ->
            val now = System.currentTimeMillis()
            val diff = (exam.epochMillis - now).coerceAtLeast(0L)
            val days = diff / (24L * 60 * 60 * 1000)
            val hours = (diff % (24L * 60 * 60 * 1000)) / (60 * 60 * 1000)

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorderSubtle, RoundedCornerShape(16.dp))
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
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BrandSurfaceElevated
                                ) {
                                    Text(
                                        text = exam.examType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = exam.subjectName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${exam.name} • ${exam.venue} (${exam.timeString})",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            if (exam.syllabusTopics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Topics: ${exam.syllabusTopics}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Days countdown pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (days <= 7) AttendanceDanger.copy(alpha = 0.2f) else RoyalIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.border(
                                1.dp,
                                if (days <= 7) AttendanceDanger.copy(alpha = 0.5f) else SoftViolet.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$days d $hours h",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "REMAINING",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (days <= 7) AttendanceDanger else ElectricCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Marks Analysis & SEE Prediction
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTERNAL MARKS & SEE PREDICTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = onOpenAddMarks, contentPadding = PaddingValues(0.dp)) {
                        Text(text = "+ Enter Marks", fontSize = 12.sp, color = ElectricCyan)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Example Transparent SEE Predictor Card matching user specification:
                // "Data Structures CIE: 42/50, Target: 85, Required SEE: 43/50"
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Data Structures & Algorithms",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(text = "CIE 1: 42 / 50 • Target: 85%", fontSize = 11.sp, color = TextSecondary)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = AttendanceSafe.copy(alpha = 0.15f)) {
                            Text(
                                text = "Req SEE: 43 / 50",
                                color = AttendanceSafe,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To reach 85% overall grade, you need approximately 43 / 50 marks in the SEE final examination.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(marks) { mark ->
            val percentage = if (mark.totalMarks > 0) (mark.scoredMarks / mark.totalMarks) * 100f else 0f
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrandSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorderSubtle, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = mark.subjectName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(text = mark.componentName, fontSize = 11.sp, color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%.0f / %.0f".format(mark.scoredMarks, mark.totalMarks),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "%.1f%%".format(percentage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (percentage >= 75f) AttendanceSafe else AttendanceCaution
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Placement Subject Insights
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "PLACEMENT RELEVANCE & CAREER TRACKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Understand how semester subjects translate to technical interviews",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Career Track Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(careerTracks) { track ->
                        val isSelected = track == selectedCareerTrack
                        Surface(
                            onClick = { selectedCareerTrack = track },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) RoyalIndigo else BrandSurfaceVariant,
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) SoftViolet else BrandBorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            Text(
                                text = track,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val relevanceItems = placementRelevanceMap[selectedCareerTrack] ?: emptyList()
                relevanceItems.forEach { (subName, insight) ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BrandSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, BrandBorderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = subName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = insight,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
