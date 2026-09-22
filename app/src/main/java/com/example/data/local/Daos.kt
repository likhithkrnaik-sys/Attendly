package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Query("UPDATE subjects SET attendedClasses = :attended, conductedClasses = :conducted WHERE id = :id")
    suspend fun updateAttendance(id: Long, attended: Int, conducted: Int)

    @Query("UPDATE subjects SET attendedClasses = :attended, conductedClasses = :conducted, cancelledClasses = :cancelled WHERE id = :id")
    suspend fun updateAttendanceWithCancelled(id: Long, attended: Int, conducted: Int, cancelled: Int)

    @Query("UPDATE subjects SET cancelledClasses = cancelledClasses + 1 WHERE id = :id")
    suspend fun incrementCancelledClasses(id: Long)

    @Query("UPDATE subjects SET cancelledClasses = CASE WHEN cancelledClasses > 0 THEN cancelledClasses - 1 ELSE 0 END WHERE id = :id")
    suspend fun decrementCancelledClasses(id: Long)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Long)
}

@Dao
interface ClassSessionDao {
    @Query("SELECT * FROM class_sessions ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSessions(): Flow<List<ClassSession>>

    @Query("SELECT * FROM class_sessions WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getSessionsForDay(dayOfWeek: Int): Flow<List<ClassSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ClassSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ClassSession>)

    @Delete
    suspend fun deleteSession(session: ClassSession)

    @Query("DELETE FROM class_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM class_sessions")
    suspend fun clearAllSessions()
}

@Dao
interface AttendanceRecordDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateEpochDay = :epochDay")
    fun getRecordsForDay(epochDay: Long): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY epochMillis ASC")
    fun getAllExams(): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<Exam>)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)
}

@Dao
interface MarkRecordDao {
    @Query("SELECT * FROM mark_records ORDER BY id ASC")
    fun getAllMarks(): Flow<List<MarkRecord>>

    @Query("SELECT * FROM mark_records WHERE subjectId = :subjectId")
    fun getMarksForSubject(subjectId: Long): Flow<List<MarkRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: MarkRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<MarkRecord>)

    @Query("DELETE FROM mark_records WHERE id = :id")
    suspend fun deleteMarkById(id: Long)
}

@Dao
interface AcademicNoteDao {
    @Query("SELECT * FROM academic_notes ORDER BY dateAddedMillis DESC")
    fun getAllNotes(): Flow<List<AcademicNote>>

    @Query("SELECT * FROM academic_notes WHERE subjectId = :subjectId ORDER BY dateAddedMillis DESC")
    fun getNotesForSubject(subjectId: Long): Flow<List<AcademicNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: AcademicNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<AcademicNote>)

    @Query("DELETE FROM academic_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}
