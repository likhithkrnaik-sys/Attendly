package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val name: String, // e.g., "Internal Assessment 2", "Semester End Exam (SEE)"
    val examType: String, // "Internal Assessment", "Quiz", "SEE", "Lab Exam", "Midterm"
    val epochMillis: Long,
    val timeString: String = "09:30 AM",
    val venue: String = "Seminar Hall 2",
    val syllabusTopics: String = "",
    val maxMarks: Float = 50f
)
