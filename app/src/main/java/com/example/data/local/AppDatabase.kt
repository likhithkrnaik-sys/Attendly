package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserProfile::class,
        Subject::class,
        ClassSession::class,
        AttendanceRecord::class,
        Exam::class,
        MarkRecord::class,
        AcademicNote::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun subjectDao(): SubjectDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao
    abstract fun examDao(): ExamDao
    abstract fun markRecordDao(): MarkRecordDao
    abstract fun academicNoteDao(): AcademicNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendly_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
