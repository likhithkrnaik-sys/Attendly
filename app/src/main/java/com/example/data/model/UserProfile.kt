package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Likhith",
    val college: String = "National Institute of Engineering",
    val branch: String = "Computer Science & Engineering",
    val semester: Int = 5,
    val section: String = "B",
    val requiredAttendance: Float = 75.0f,
    val isOnboardingCompleted: Boolean = true,
    val targetGpaOrMarks: Float = 85.0f
)
