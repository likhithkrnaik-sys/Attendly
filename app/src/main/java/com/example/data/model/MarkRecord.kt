package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mark_records")
data class MarkRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val componentName: String, // CIE 1, CIE 2, Assignment, Lab Test, Quiz, SEE
    val scoredMarks: Float,
    val totalMarks: Float,
    val weightPercent: Float = 25f
)
