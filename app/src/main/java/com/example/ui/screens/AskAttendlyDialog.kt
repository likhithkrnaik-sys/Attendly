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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.ui.theme.*
import com.example.viewmodel.AttendlyViewModel

data class ChatMessage(
    val sender: String, // "user" or "attendly"
    val text: String,
    val time: String = "Now"
)

@Composable
fun AskAttendlyDialog(
    viewModel: AttendlyViewModel,
    onDismiss: () -> Unit
) {
    var queryInput by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "attendly",
                text = "Hi! I'm your Attendly Academic Assistant. Ask me anything about your attendance buffer, upcoming exams, bunk safety, or revision notes!"
            )
        )
    }

    val sampleQueries = listOf(
        "Can I miss tomorrow's OS class?",
        "When is my next exam?",
        "What subjects am I weak in?",
        "Which classes have high attendance buffer?"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                .testTag("ask_attendly_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RoyalIndigo.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ask Attendly",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Grounded in your real academic data",
                                fontSize = 11.sp,
                                color = AttendanceSafe
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_ask_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Suggestion chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleQueries.take(2).forEach { sample ->
                        Surface(
                            onClick = {
                                queryInput = sample
                                val ans = viewModel.answerStudentQuery(sample)
                                messages.add(ChatMessage("user", sample))
                                messages.add(ChatMessage("attendly", ans))
                                queryInput = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = BrandSurfaceVariant,
                            modifier = Modifier.border(1.dp, BrandBorderSubtle, RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = sample,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                color = if (isUser) RoyalIndigo else BrandSurfaceVariant,
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .border(
                                        1.dp,
                                        if (isUser) SoftViolet.copy(alpha = 0.5f) else BrandBorderSubtle,
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Query Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandSurfaceVariant, RoundedCornerShape(16.dp))
                        .border(1.dp, BrandBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = queryInput,
                        onValueChange = { queryInput = it },
                        placeholder = { Text("Ask about attendance, exams, bunk...", fontSize = 12.sp, color = TextMuted) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ask_attendly_input")
                    )

                    IconButton(
                        onClick = {
                            if (queryInput.isNotBlank()) {
                                val userQ = queryInput.trim()
                                queryInput = ""
                                val response = viewModel.answerStudentQuery(userQ)
                                messages.add(ChatMessage("user", userQ))
                                messages.add(ChatMessage("attendly", response))
                            }
                        },
                        modifier = Modifier.testTag("send_query_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
