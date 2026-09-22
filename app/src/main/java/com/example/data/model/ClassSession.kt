package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_sessions")
data class ClassSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "10:00"
    val room: String,      // e.g. "EC-101"
    val classType: String = "Lecture", // Lecture, Lab, Tutorial
    val faculty: String = ""
)
