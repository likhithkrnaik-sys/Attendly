package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AcademicRepository(private val db: AppDatabase) {

    private val subjectDao = db.subjectDao()
    private val sessionDao = db.classSessionDao()
    private val attendanceDao = db.attendanceRecordDao()
    private val examDao = db.examDao()
    private val markDao = db.markRecordDao()
    private val noteDao = db.academicNoteDao()
    private val profileDao = db.userProfileDao()

    val profile: Flow<UserProfile?> = profileDao.getProfile()
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val allSessions: Flow<List<ClassSession>> = sessionDao.getAllSessions()
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val allExams: Flow<List<Exam>> = examDao.getAllExams()
    val allMarks: Flow<List<MarkRecord>> = markDao.getAllMarks()
    val allNotes: Flow<List<AcademicNote>> = noteDao.getAllNotes()

    fun getSessionsForDay(dayOfWeek: Int): Flow<List<ClassSession>> =
        sessionDao.getSessionsForDay(dayOfWeek)

    suspend fun saveProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdateProfile(profile)
    }

    suspend fun markAttendance(
        subjectId: Long,
        isPresent: Boolean,
        epochDay: Long,
        classSessionId: Long? = null
    ) = withContext(Dispatchers.IO) {
        val subject = subjectDao.getSubjectById(subjectId) ?: return@withContext
        val newAttended = if (isPresent) subject.attendedClasses + 1 else subject.attendedClasses
        val newConducted = subject.conductedClasses + 1
        subjectDao.updateAttendance(subjectId, newAttended, newConducted)

        attendanceDao.insertRecord(
            AttendanceRecord(
                subjectId = subjectId,
                classSessionId = classSessionId,
                dateEpochDay = epochDay,
                status = if (isPresent) "PRESENT" else "ABSENT"
            )
        )
    }

    suspend fun markClassCancelled(
        subjectId: Long,
        epochDay: Long,
        classSessionId: Long? = null
    ) = withContext(Dispatchers.IO) {
        // Increment cancelled count on the subject without altering conducted or attended
        subjectDao.incrementCancelledClasses(subjectId)

        // Insert record with CANCELLED status
        attendanceDao.insertRecord(
            AttendanceRecord(
                subjectId = subjectId,
                classSessionId = classSessionId,
                dateEpochDay = epochDay,
                status = "CANCELLED"
            )
        )
    }

    suspend fun undoCancelledClass(subjectId: Long) = withContext(Dispatchers.IO) {
        subjectDao.decrementCancelledClasses(subjectId)
    }

    suspend fun updateSubjectAttendance(subjectId: Long, attended: Int, conducted: Int, cancelled: Int = 0) =
        withContext(Dispatchers.IO) {
            subjectDao.updateAttendanceWithCancelled(
                subjectId,
                attended.coerceAtLeast(0),
                conducted.coerceAtLeast(0),
                cancelled.coerceAtLeast(0)
            )
        }

    suspend fun addSubject(subject: Subject): Long = withContext(Dispatchers.IO) {
        subjectDao.insertSubject(subject)
    }

    suspend fun updateSubject(subject: Subject) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subjectId: Long) = withContext(Dispatchers.IO) {
        subjectDao.deleteSubjectById(subjectId)
    }

    suspend fun addSession(session: ClassSession): Long = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionById(sessionId)
    }

    suspend fun addExam(exam: Exam): Long = withContext(Dispatchers.IO) {
        examDao.insertExam(exam)
    }

    suspend fun deleteExam(examId: Long) = withContext(Dispatchers.IO) {
        examDao.deleteExamById(examId)
    }

    suspend fun addMark(mark: MarkRecord): Long = withContext(Dispatchers.IO) {
        markDao.insertMark(mark)
    }

    suspend fun deleteMark(markId: Long) = withContext(Dispatchers.IO) {
        markDao.deleteMarkById(markId)
    }

    suspend fun addNote(note: AcademicNote): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun deleteNote(noteId: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(noteId)
    }

    /**
     * Seeds initial engineering semester data if database is empty.
     */
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentProfile = profileDao.getProfile().firstOrNull()
        if (currentProfile != null) return@withContext

        // 1. Initial User Profile
        profileDao.insertOrUpdateProfile(
            UserProfile(
                id = 1,
                name = "Aarav Sharma",
                college = "National Institute of Technology",
                branch = "Computer Science & Engineering",
                semester = 5,
                section = "A",
                requiredAttendance = 75.0f,
                isOnboardingCompleted = true,
                targetGpaOrMarks = 85.0f
            )
        )

        // 2. Initial Subjects with specific test scenarios matching prompt
        val dsaId = subjectDao.insertSubject(
            Subject(
                name = "Data Structures & Algorithms",
                code = "CS301",
                faculty = "Dr. S. K. Raman",
                credits = 4,
                colorHex = "#38BDF8", // Cyan
                attendedClasses = 42,
                conductedClasses = 50, // 84.0% -> Safe misses = 6
                cancelledClasses = 3,
                category = "Theory",
                placementRelevance = "Highest relevance for technical coding rounds and FAANG interviews"
            )
        )

        val osId = subjectDao.insertSubject(
            Subject(
                name = "Operating Systems",
                code = "CS302",
                faculty = "Prof. Ananya Sen",
                credits = 4,
                colorHex = "#F43F5E", // Rose / Red
                attendedClasses = 36,
                conductedClasses = 50, // 72.0% -> Danger! Must attend 6 consecutive classes!
                cancelledClasses = 2,
                category = "Theory",
                placementRelevance = "Critical for CS core technical interviews (threads, memory, deadlocks)"
            )
        )

        val dbmsId = subjectDao.insertSubject(
            Subject(
                name = "Database Management Systems",
                code = "CS303",
                faculty = "Prof. V. Rajesh",
                credits = 4,
                colorHex = "#6366F1", // Indigo
                attendedClasses = 41,
                conductedClasses = 48, // 85.4%
                cancelledClasses = 1,
                category = "Theory",
                placementRelevance = "Essential for Backend Engineering, SQL indexing, ACID transactions"
            )
        )

        val mathId = subjectDao.insertSubject(
            Subject(
                name = "Discrete Mathematics",
                code = "MA301",
                faculty = "Dr. M. Krishnan",
                credits = 4,
                colorHex = "#10B981", // Emerald
                attendedClasses = 43,
                conductedClasses = 50, // 86.0% -> Safe misses = 7
                cancelledClasses = 2,
                category = "Theory",
                placementRelevance = "Fundamental for Data Science, ML theory, cryptography, algorithm analysis"
            )
        )

        val cnId = subjectDao.insertSubject(
            Subject(
                name = "Computer Networks",
                code = "CS304",
                faculty = "Dr. Pradeep Rao",
                credits = 4,
                colorHex = "#F59E0B", // Amber
                attendedClasses = 31,
                conductedClasses = 40, // 77.5% -> Caution! 1 safe miss
                cancelledClasses = 1,
                category = "Theory",
                placementRelevance = "Key for DevOps, Systems, Cloud infrastructure, TCP/IP fundamentals"
            )
        )

        val labId = subjectDao.insertSubject(
            Subject(
                name = "OS & Networks Lab",
                code = "CS305L",
                faculty = "Prof. Ananya Sen",
                credits = 2,
                colorHex = "#A855F7", // Purple
                attendedClasses = 14,
                conductedClasses = 14, // 100.0% -> Perfect attendance test case!
                cancelledClasses = 0,
                category = "Lab",
                placementRelevance = "Hands-on Linux shell scripting, socket programming, systems debugging"
            )
        )

        // 3. Timetable Schedule (Monday to Friday)
        val sessions = listOf(
            // Monday
            ClassSession(subjectId = dsaId, subjectName = "Data Structures & Algorithms", subjectCode = "CS301", dayOfWeek = 1, startTime = "09:00", endTime = "10:00", room = "EC-101", faculty = "Dr. S. K. Raman"),
            ClassSession(subjectId = osId, subjectName = "Operating Systems", subjectCode = "CS302", dayOfWeek = 1, startTime = "10:00", endTime = "11:00", room = "EC-101", faculty = "Prof. Ananya Sen"),
            ClassSession(subjectId = dbmsId, subjectName = "Database Management Systems", subjectCode = "CS303", dayOfWeek = 1, startTime = "11:15", endTime = "12:15", room = "CS-204", faculty = "Prof. V. Rajesh"),
            ClassSession(subjectId = mathId, subjectName = "Discrete Mathematics", subjectCode = "MA301", dayOfWeek = 1, startTime = "13:15", endTime = "14:15", room = "LH-3", faculty = "Dr. M. Krishnan"),

            // Tuesday
            ClassSession(subjectId = osId, subjectName = "Operating Systems", subjectCode = "CS302", dayOfWeek = 2, startTime = "09:00", endTime = "10:00", room = "EC-101", faculty = "Prof. Ananya Sen"),
            ClassSession(subjectId = cnId, subjectName = "Computer Networks", subjectCode = "CS304", dayOfWeek = 2, startTime = "10:00", endTime = "11:00", room = "CS-204", faculty = "Dr. Pradeep Rao"),
            ClassSession(subjectId = labId, subjectName = "OS & Networks Lab", subjectCode = "CS305L", dayOfWeek = 2, startTime = "11:15", endTime = "13:15", room = "Systems Lab 1", classType = "Lab", faculty = "Prof. Ananya Sen"),

            // Wednesday
            ClassSession(subjectId = dsaId, subjectName = "Data Structures & Algorithms", subjectCode = "CS301", dayOfWeek = 3, startTime = "09:00", endTime = "10:00", room = "EC-101", faculty = "Dr. S. K. Raman"),
            ClassSession(subjectId = dbmsId, subjectName = "Database Management Systems", subjectCode = "CS303", dayOfWeek = 3, startTime = "10:00", endTime = "11:00", room = "CS-204", faculty = "Prof. V. Rajesh"),
            ClassSession(subjectId = mathId, subjectName = "Discrete Mathematics", subjectCode = "MA301", dayOfWeek = 3, startTime = "11:15", endTime = "12:15", room = "LH-3", faculty = "Dr. M. Krishnan"),
            ClassSession(subjectId = cnId, subjectName = "Computer Networks", subjectCode = "CS304", dayOfWeek = 3, startTime = "13:15", endTime = "14:15", room = "CS-204", faculty = "Dr. Pradeep Rao"),

            // Thursday
            ClassSession(subjectId = osId, subjectName = "Operating Systems", subjectCode = "CS302", dayOfWeek = 4, startTime = "09:00", endTime = "10:00", room = "EC-101", faculty = "Prof. Ananya Sen"),
            ClassSession(subjectId = dsaId, subjectName = "Data Structures & Algorithms", subjectCode = "CS301", dayOfWeek = 4, startTime = "10:00", endTime = "11:00", room = "EC-101", faculty = "Dr. S. K. Raman"),
            ClassSession(subjectId = mathId, subjectName = "Discrete Mathematics", subjectCode = "MA301", dayOfWeek = 4, startTime = "11:15", endTime = "12:15", room = "LH-3", faculty = "Dr. M. Krishnan"),

            // Friday
            ClassSession(subjectId = dbmsId, subjectName = "Database Management Systems", subjectCode = "CS303", dayOfWeek = 5, startTime = "09:00", endTime = "10:00", room = "CS-204", faculty = "Prof. V. Rajesh"),
            ClassSession(subjectId = cnId, subjectName = "Computer Networks", subjectCode = "CS304", dayOfWeek = 5, startTime = "10:00", endTime = "11:00", room = "CS-204", faculty = "Dr. Pradeep Rao"),
            ClassSession(subjectId = osId, subjectName = "Operating Systems", subjectCode = "CS302", dayOfWeek = 5, startTime = "11:15", endTime = "12:15", room = "EC-101", faculty = "Prof. Ananya Sen")
        )
        sessionDao.insertSessions(sessions)

        // 4. Upcoming Exams
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000
        val exams = listOf(
            Exam(
                subjectId = osId,
                subjectName = "Operating Systems",
                name = "Internal Assessment 2",
                examType = "Internal Assessment",
                epochMillis = now + (12L * dayMillis) + (18L * 3600 * 1000), // ~12 days 18 hours matching spec!
                timeString = "10:00 AM",
                venue = "Main Exam Hall A",
                syllabusTopics = "Virtual Memory, Paging, Page Replacement, File Systems & Disk Scheduling",
                maxMarks = 50f
            ),
            Exam(
                subjectId = dsaId,
                subjectName = "Data Structures & Algorithms",
                name = "Midterm Coding Practical",
                examType = "Lab Exam",
                epochMillis = now + (18L * dayMillis),
                timeString = "02:00 PM",
                venue = "Lab 3B",
                syllabusTopics = "Dynamic Programming, Graph Traversals (BFS/DFS), Minimum Spanning Tree",
                maxMarks = 50f
            ),
            Exam(
                subjectId = mathId,
                subjectName = "Discrete Mathematics",
                name = "Internal Assessment 2",
                examType = "Internal Assessment",
                epochMillis = now + (24L * dayMillis),
                timeString = "09:30 AM",
                venue = "Room 104",
                syllabusTopics = "Recurrence Relations, Generating Functions, Graph Isomorphism",
                maxMarks = 50f
            ),
            Exam(
                subjectId = dbmsId,
                subjectName = "Database Management Systems",
                name = "Semester End Exam (SEE)",
                examType = "SEE",
                epochMillis = now + (45L * dayMillis),
                timeString = "09:30 AM",
                venue = "Examination Complex Hall 1",
                syllabusTopics = "Full Syllabus (Modules 1-5)",
                maxMarks = 100f
            )
        )
        examDao.insertExams(exams)

        // 5. Marks Data
        val marks = listOf(
            MarkRecord(subjectId = dsaId, subjectName = "Data Structures & Algorithms", componentName = "CIE 1", scoredMarks = 42f, totalMarks = 50f, weightPercent = 50f),
            MarkRecord(subjectId = dsaId, subjectName = "Data Structures & Algorithms", componentName = "Assignment 1", scoredMarks = 10f, totalMarks = 10f, weightPercent = 10f),
            MarkRecord(subjectId = osId, subjectName = "Operating Systems", componentName = "CIE 1", scoredMarks = 34f, totalMarks = 50f, weightPercent = 50f),
            MarkRecord(subjectId = dbmsId, subjectName = "Database Management Systems", componentName = "CIE 1", scoredMarks = 45f, totalMarks = 50f, weightPercent = 50f),
            MarkRecord(subjectId = mathId, subjectName = "Discrete Mathematics", componentName = "CIE 1", scoredMarks = 44f, totalMarks = 50f, weightPercent = 50f),
            MarkRecord(subjectId = cnId, subjectName = "Computer Networks", componentName = "CIE 1", scoredMarks = 38f, totalMarks = 50f, weightPercent = 50f)
        )
        markDao.insertMarks(marks)

        // 6. Notes / Files with AI Topic Intelligence
        val notes = listOf(
            AcademicNote(
                subjectId = osId,
                subjectName = "Operating Systems",
                title = "Unit 3 - Process Synchronization & Deadlocks.pdf",
                module = "Unit 3",
                noteType = "PDF",
                summaryOrTopics = "Critical Section Problem, Peterson's Algorithm, Semaphores, Monitors, Banker's Algorithm, Resource Allocation Graph",
                pageCount = 28
            ),
            AcademicNote(
                subjectId = osId,
                subjectName = "Operating Systems",
                title = "Memory Management & Virtual Memory Notes.pdf",
                module = "Unit 4",
                noteType = "Lecture Notes",
                summaryOrTopics = "Paging, Segmentation, TLB, Page Fault Handling, FIFO vs LRU vs Optimal Replacement",
                pageCount = 34
            ),
            AcademicNote(
                subjectId = dsaId,
                subjectName = "Data Structures & Algorithms",
                title = "Unit 2 - Trees, Heaps & AVL Balanced Trees.pdf",
                module = "Unit 2",
                noteType = "PDF",
                summaryOrTopics = "BST Insertion & Deletion, Rotations, Min/Max Heapify, Priority Queue Applications",
                pageCount = 22
            ),
            AcademicNote(
                subjectId = dsaId,
                subjectName = "Data Structures & Algorithms",
                title = "Important Interview Algorithms & PYQ.pdf",
                module = "Exam Prep",
                noteType = "PYQ",
                summaryOrTopics = "Dijkstra's Algorithm, Bellman-Ford, Kruskal's MST, 0/1 Knapsack DP formulation",
                pageCount = 18
            ),
            AcademicNote(
                subjectId = dbmsId,
                subjectName = "Database Management Systems",
                title = "Normalization & SQL Indexing Cheatsheet.pdf",
                module = "Unit 3",
                noteType = "Cheatsheet",
                summaryOrTopics = "1NF, 2NF, 3NF, BCNF Decomposition, B+ Tree Indexing, Query Optimization",
                pageCount = 12
            ),
            AcademicNote(
                subjectId = mathId,
                subjectName = "Discrete Mathematics",
                title = "Recurrence Relations & Graph Theory Guide.pdf",
                module = "Unit 4",
                noteType = "Lecture Notes",
                summaryOrTopics = "Homogeneous & Non-homogeneous relations, Master Theorem, Planar Graphs, Euler's Formula",
                pageCount = 20
            )
        )
        noteDao.insertNotes(notes)
    }

    suspend fun resetToDefaultDemo() = withContext(Dispatchers.IO) {
        sessionDao.clearAllSessions()
        // re-seed
        seedInitialDataIfEmpty()
    }
}
