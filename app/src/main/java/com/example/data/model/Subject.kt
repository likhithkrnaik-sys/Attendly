package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val faculty: String = "",
    val credits: Int = 4,
    val colorHex: String = "#38BDF8",
    val attendedClasses: Int = 0,
    val conductedClasses: Int = 0,
    val cancelledClasses: Int = 0,
    val category: String = "Theory", // Theory, Lab, Elective
    val placementRelevance: String = "Core technical interviews and problem solving"
)
