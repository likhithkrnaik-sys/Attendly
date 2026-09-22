package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.engine.AttendanceMathEngine
import com.example.engine.AttendanceStatus
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandGlassFill,
    borderColor: Color = BrandBorder,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Surface(
        modifier = clickableModifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.3f))
                ),
                shape = shape
            ),
        shape = shape,
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun AttendanceRing(
    percentage: Double,
    requiredPercentage: Double = 75.0,
    size: Dp = 180.dp,
    strokeWidth: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "attendance_ring"
    )

    val status = when {
        percentage < requiredPercentage -> AttendanceStatus.DANGER
        percentage < requiredPercentage + 3.0 -> AttendanceStatus.CAUTION
        else -> AttendanceStatus.SAFE
    }

    val arcColor = when (status) {
        AttendanceStatus.SAFE -> AttendanceSafe
        AttendanceStatus.CAUTION -> AttendanceCaution
        AttendanceStatus.DANGER -> AttendanceDanger
    }

    val glowColor = when (status) {
        AttendanceStatus.SAFE -> AttendanceSafeGlow
        AttendanceStatus.CAUTION -> AttendanceCautionGlow
        AttendanceStatus.DANGER -> AttendanceDangerGlow
    }

    val safetyMargin = percentage - requiredPercentage
    val marginText = if (safetyMargin >= 0) "+%.1f%%".format(safetyMargin) else "%.1f%%".format(safetyMargin)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Required threshold tick or marker (75% = 270 degrees sweep from -90)
            val reqAngle = -90f + (requiredPercentage.toFloat() * 3.6f)

            // Active Arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(arcColor.copy(alpha = 0.85f), arcColor)
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "%.1f%%".format(percentage),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (safetyMargin >= 0) AttendanceSafe.copy(alpha = 0.15f) else AttendanceDanger.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Margin: $marginText",
                    color = if (safetyMargin >= 0) AttendanceSafe else AttendanceDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Req: ${requiredPercentage.toInt()}%",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        AttendanceStatus.SAFE -> Triple(AttendanceSafe.copy(alpha = 0.16f), AttendanceSafe, "SAFE")
        AttendanceStatus.CAUTION -> Triple(AttendanceCaution.copy(alpha = 0.16f), AttendanceCaution, "CAUTION")
        AttendanceStatus.DANGER -> Triple(AttendanceDanger.copy(alpha = 0.16f), AttendanceDanger, "DANGER")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tintColor: Color = ElectricCyan
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = BrandSurfaceVariant,
        modifier = modifier
            .testTag(testTag)
            .border(1.dp, BrandBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SubjectAttendanceCard(
    subject: Subject,
    requiredPercentage: Double = 75.0,
    onSimulateMiss: () -> Unit,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onMarkCancelled: () -> Unit,
    onUndoCancelled: (() -> Unit)? = null,
    onAdjustAttendance: (newAttended: Int, newConducted: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelledConfirmation by remember { mutableStateOf(false) }

    val summary = remember(subject.attendedClasses, subject.conductedClasses, requiredPercentage) {
        AttendanceMathEngine.getAttendanceSummary(
            subject.attendedClasses,
            subject.conductedClasses,
            requiredPercentage
        )
    }

    val subjectAccent = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        ElectricCyan
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onSimulateMiss
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(subjectAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = subject.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${subject.code} • ${subject.category}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
            StatusBadge(status = summary.status)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { (summary.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when (summary.status) {
                AttendanceStatus.SAFE -> AttendanceSafe
                AttendanceStatus.CAUTION -> AttendanceCaution
                AttendanceStatus.DANGER -> AttendanceDanger
            },
            trackColor = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row of metrics: Current %, Attended/Conducted, Safe Misses / Recovery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "%.1f%%".format(summary.percentage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${summary.attended} / ${summary.conducted} conducted",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (summary.status == AttendanceStatus.DANGER) {
                    Text(
                        text = "Must attend: ${summary.recoveryClassesNeeded}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AttendanceDanger
                    )
                    Text(
                        text = "Safe misses: 0",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                } else {
                    Text(
                        text = "Can miss: ${summary.safeMisses} classes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.safeMisses > 2) AttendanceSafe else AttendanceCaution
                    )
                    Text(
                        text = if (summary.safeMisses > 0) "Buffer remaining" else "Borderline",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Cancelled Classes Pill / Indicator
        if (subject.cancelledClasses > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SubjectColorAmber.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SubjectColorAmber.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.EventBusy,
                            contentDescription = null,
                            tint = SubjectColorAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${subject.cancelledClasses} ${if (subject.cancelledClasses == 1) "class" else "classes"} cancelled",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubjectColorAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "• 0% impact",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    if (onUndoCancelled != null) {
                        Text(
                            text = "Undo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            modifier = Modifier
                                .clickable { onUndoCancelled() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("undo_cancelled_${subject.code}")
                        )
                    }
                }
            }
        }

        if (showCancelledConfirmation) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RoyalIndigo.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Class recorded as cancelled. Percentage protected!",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BrandBorderSubtle)
        Spacer(modifier = Modifier.height(10.dp))

        // PRIMARY ACTION BUTTONS: [Class Cancelled] [Absent] [Present]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option 1: Class Got Cancelled button
            OutlinedButton(
                onClick = {
                    onMarkCancelled()
                    showCancelledConfirmation = true
                },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SubjectColorAmber.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SubjectColorAmber.copy(alpha = 0.1f)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("btn_class_cancelled_${subject.code}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.EventBusy,
                    contentDescription = "Class Got Cancelled",
                    tint = SubjectColorAmber,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Class Cancelled",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubjectColorAmber,
                    maxLines = 1
                )
            }

            // Option 2: Absent
            Button(
                onClick = onMarkAbsent,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AttendanceDanger.copy(alpha = 0.18f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_absent_${subject.code}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Absent",
                    tint = AttendanceDanger,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Absent",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AttendanceDanger
                )
            }

            // Option 3: Present
            Button(
                onClick = onMarkPresent,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AttendanceSafe.copy(alpha = 0.22f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_present_${subject.code}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Present",
                    tint = AttendanceSafe,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Present",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AttendanceSafe
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Secondary Utility Row: "Can I miss? ➔" and Manual +/- Stepper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onSimulateMiss,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier.testTag("simulate_bunk_${subject.code}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Calculate,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Can I miss this? ➔",
                    fontSize = 11.sp,
                    color = ElectricCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Fine-tune adjustment steppers
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Adjust: ", fontSize = 10.sp, color = TextMuted)
                IconButton(
                    onClick = {
                        if (subject.conductedClasses > 0 && subject.attendedClasses > 0) {
                            onAdjustAttendance(subject.attendedClasses - 1, subject.conductedClasses - 1)
                        }
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(BrandSurfaceElevated)
                        .testTag("decrement_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Decrement",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        onAdjustAttendance(subject.attendedClasses + 1, subject.conductedClasses + 1)
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(BrandSurfaceElevated)
                        .testTag("increment_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Increment",
                        tint = ElectricCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
