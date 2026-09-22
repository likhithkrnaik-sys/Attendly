package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_notes")
data class AcademicNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val title: String,
    val module: String = "Unit 1", // Unit 1, Unit 2, Question Paper, Cheatsheet
    val noteType: String = "PDF", // PDF, Notes, PYQ, Lab Manual
    val summaryOrTopics: String = "",
    val pageCount: Int = 14,
    val dateAddedMillis: Long = System.currentTimeMillis()
)
