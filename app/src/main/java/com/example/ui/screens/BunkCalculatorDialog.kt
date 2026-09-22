package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Subject
import com.example.engine.AttendanceMathEngine
import com.example.engine.AttendanceStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BunkCalculatorDialog(
    subjects: List<Subject>,
    initialSubject: Subject?,
    requiredPercentage: Double,
    onDismiss: () -> Unit
) {
    var selectedSubjectId by remember {
        mutableStateOf(initialSubject?.id ?: subjects.firstOrNull()?.id ?: 0L)
    }
    var missCount by remember { mutableIntStateOf(1) }

    val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()

    val simulationResult = remember(currentSubject, missCount, requiredPercentage) {
        if (currentSubject != null) {
            AttendanceMathEngine.simulateBunk(
                attended = currentSubject.attendedClasses,
                conducted = currentSubject.conductedClasses,
                requiredPercentage = requiredPercentage,
                missCount = missCount
            )
        } else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                .testTag("bunk_calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Can I Miss This Class?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Deterministic Bunk Impact Engine",
                            fontSize = 12.sp,
                            color = ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_bunk_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Selector Dropdown / Chips
                Text(
                    text = "SELECT SUBJECT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.take(4).forEach { s ->
                        val isSelected = s.id == selectedSubjectId
                        Surface(
                            onClick = { selectedSubjectId = s.id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else BrandSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else BrandBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Text(
                                text = s.code,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentSubject != null && simulationResult != null) {
                    // Current Subject Details
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BrandSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrandBorderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = currentSubject.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${simulationResult.currentPercentage}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current record: ${currentSubject.attendedClasses} attended / ${currentSubject.conductedClasses} conducted",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Number of Classes to Miss (Stepper)
                    Text(
                        text = "CLASSES YOU WANT TO MISS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 4).forEach { count ->
                                val isSelected = missCount == count
                                Surface(
                                    onClick = { missCount = count },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) RoyalIndigo else BrandSurfaceVariant,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .border(
                                            1.dp,
                                            if (isSelected) SoftViolet else BrandBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Direct Stepper (+ / -)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(BrandSurfaceVariant, RoundedCornerShape(12.dp))
                                .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            IconButton(
                                onClick = { if (missCount > 1) missCount-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Decrease",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "$missCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { missCount++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Increase",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Simulation Outcome Box
                    val outcomeCardBg by animateColorAsState(
                        targetValue = when (simulationResult.status) {
                            AttendanceStatus.SAFE -> Color(0xFF064E3B).copy(alpha = 0.35f)
                            AttendanceStatus.CAUTION -> Color(0xFF78350F).copy(alpha = 0.35f)
                            AttendanceStatus.DANGER -> Color(0xFF7F1D1D).copy(alpha = 0.35f)
                        },
                        label = "outcome_color"
                    )

                    val outcomeBorder by animateColorAsState(
                        targetValue = when (simulationResult.status) {
                            AttendanceStatus.SAFE -> AttendanceSafe
                            AttendanceStatus.CAUTION -> AttendanceCaution
                            AttendanceStatus.DANGER -> AttendanceDanger
                        },
                        label = "outcome_border"
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = outcomeCardBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, outcomeBorder, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = simulationResult.status)
                                Text(
                                    text = "After $missCount miss(es)",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Projected Attendance",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "%.1f%%".format(simulationResult.projectedPercentage),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Drop Impact",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "-%.1f%%".format(simulationResult.percentageDrop),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AttendanceDanger
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BrandBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = simulationResult.recommendation,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Transparent mathematical proof
                            Text(
                                text = simulationResult.mathematicalProof,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_bunk_calc_btn")
                ) {
                    Text(
                        text = "Understood",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003548),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
