package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AcademicRepository
import com.example.engine.AttendanceMathEngine
import com.example.engine.AttendanceStatus
import com.example.engine.BunkSimulationResult
import com.example.engine.PrioritySubjectInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

data class OverallAttendanceStats(
    val percentage: Double = 0.0,
    val totalAttended: Int = 0,
    val totalConducted: Int = 0,
    val requiredPercentage: Double = 75.0,
    val safetyMargin: Double = 0.0,
    val status: AttendanceStatus = AttendanceStatus.SAFE,
    val totalSafeMisses: Int = 0
)

data class AcademicInsightItem(
    val title: String,
    val description: String,
    val type: AttendanceStatus, // SAFE, CAUTION, DANGER
    val category: String // Attendance, Exam, Marks, General
)

class AttendlyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AcademicRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = AcademicRepository(db)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val userProfile: StateFlow<UserProfile> = repository.profile
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfile()
        )

    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val sessions: StateFlow<List<ClassSession>> = repository.allSessions
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val exams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val marks: StateFlow<List<MarkRecord>> = repository.allMarks
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val notes: StateFlow<List<AcademicNote>> = repository.allNotes
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Current Day of Week: 1 = Monday ... 7 = Sunday
    val currentDayOfWeek: Int = run {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar.MONDAY is 2, SUNDAY is 1
        when (day) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }

    // Selected Day for Timetable screen
    private val _selectedTimetableDay = MutableStateFlow(currentDayOfWeek)
    val selectedTimetableDay: StateFlow<Int> = _selectedTimetableDay.asStateFlow()

    fun setSelectedTimetableDay(day: Int) {
        _selectedTimetableDay.value = day
    }

    // Overall Attendance Metrics
    val overallAttendance: StateFlow<OverallAttendanceStats> = combine(
        subjects,
        userProfile
    ) { subs, profile ->
        if (subs.isEmpty()) {
            OverallAttendanceStats(
                percentage = 100.0,
                requiredPercentage = profile.requiredAttendance.toDouble(),
                safetyMargin = 100.0 - profile.requiredAttendance.toDouble(),
                status = AttendanceStatus.SAFE
            )
        } else {
            val totalAttended = subs.sumOf { it.attendedClasses }
            val totalConducted = subs.sumOf { it.conductedClasses }
            val req = profile.requiredAttendance.toDouble()
            val pct = AttendanceMathEngine.calculatePercentage(totalAttended, totalConducted)
            val margin = (pct - req).let { Math.round(it * 10.0) / 10.0 }
            val totalSafeMisses = AttendanceMathEngine.calculateSafeMisses(totalAttended, totalConducted, req)

            val status = when {
                pct < req -> AttendanceStatus.DANGER
                pct < req + 3.0 -> AttendanceStatus.CAUTION
                else -> AttendanceStatus.SAFE
            }

            OverallAttendanceStats(
                percentage = pct,
                totalAttended = totalAttended,
                totalConducted = totalConducted,
                requiredPercentage = req,
                safetyMargin = margin,
                status = status,
                totalSafeMisses = totalSafeMisses
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallAttendanceStats())

    // Priority Subjects: "Classes I should NOT miss"
    val prioritySubjects: StateFlow<List<PrioritySubjectInfo>> = combine(
        subjects,
        userProfile
    ) { subs, profile ->
        val req = profile.requiredAttendance.toDouble()
        subs.map { s ->
            val pct = AttendanceMathEngine.calculatePercentage(s.attendedClasses, s.conductedClasses)
            val safe = AttendanceMathEngine.calculateSafeMisses(s.attendedClasses, s.conductedClasses, req)
            val rec = AttendanceMathEngine.calculateRecoveryClassesNeeded(s.attendedClasses, s.conductedClasses, req)

            val status = when {
                pct < req -> AttendanceStatus.DANGER
                safe <= 1 || pct < req + 3.0 -> AttendanceStatus.CAUTION
                else -> AttendanceStatus.SAFE
            }

            val reason = when (status) {
                AttendanceStatus.DANGER -> "Below required threshold (${pct}% vs ${req}%). Must attend next $rec classes."
                AttendanceStatus.CAUTION -> "Only $safe safe miss remaining! Next absence risks danger zone."
                AttendanceStatus.SAFE -> "Comfortable buffer (+${(pct - req).toInt()}%). $safe safe misses remaining."
            }

            val advice = when (status) {
                AttendanceStatus.DANGER -> "DO NOT MISS ANY UPCOMING CLASSES."
                AttendanceStatus.CAUTION -> "Attend upcoming classes to build safe buffer."
                AttendanceStatus.SAFE -> "Safe to prioritize other subjects if needed."
            }

            // Priority rank: 1 is highest priority (Danger subjects first, then low safe misses)
            val rankScore = when (status) {
                AttendanceStatus.DANGER -> 1
                AttendanceStatus.CAUTION -> 2
                AttendanceStatus.SAFE -> 3
            }

            PrioritySubjectInfo(
                subjectId = s.id,
                subjectName = s.name,
                subjectCode = s.code,
                currentPercentage = pct,
                requiredPercentage = req,
                attended = s.attendedClasses,
                conducted = s.conductedClasses,
                safeMisses = safe,
                recoveryClasses = rec,
                priorityRank = rankScore,
                status = status,
                primaryReason = reason,
                actionAdvice = advice
            )
        }.sortedWith(compareBy({ it.priorityRank }, { it.currentPercentage }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart Dynamic Insights based on actual database values
    val dynamicInsights: StateFlow<List<AcademicInsightItem>> = combine(
        subjects,
        exams,
        marks,
        userProfile
    ) { subs, exList, mkList, profile ->
        val items = mutableListOf<AcademicInsightItem>()
        val req = profile.requiredAttendance.toDouble()

        // 1. Attendance danger insights
        val dangerSubs = subs.filter {
            AttendanceMathEngine.calculatePercentage(it.attendedClasses, it.conductedClasses) < req
        }
        dangerSubs.forEach { s ->
            val pct = AttendanceMathEngine.calculatePercentage(s.attendedClasses, s.conductedClasses)
            val rec = AttendanceMathEngine.calculateRecoveryClassesNeeded(s.attendedClasses, s.conductedClasses, req)
            items.add(
                AcademicInsightItem(
                    title = "Attendance Alert: ${s.name}",
                    description = "Current attendance is ${pct}%, below required ${req}%. Attend next $rec classes consecutively to recover.",
                    type = AttendanceStatus.DANGER,
                    category = "Attendance"
                )
            )
        }

        // 2. High buffer subjects
        val safeSubs = subs.filter {
            AttendanceMathEngine.calculateSafeMisses(it.attendedClasses, it.conductedClasses, req) >= 4
        }
        if (safeSubs.isNotEmpty()) {
            val topSafe = safeSubs.maxByOrNull { AttendanceMathEngine.calculateSafeMisses(it.attendedClasses, it.conductedClasses, req) }
            if (topSafe != null) {
                val misses = AttendanceMathEngine.calculateSafeMisses(topSafe.attendedClasses, topSafe.conductedClasses, req)
                items.add(
                    AcademicInsightItem(
                        title = "Strong Buffer in ${topSafe.name}",
                        description = "You have enough attendance buffer in ${topSafe.name} for $misses missed classes while staying above ${req}%.",
                        type = AttendanceStatus.SAFE,
                        category = "Attendance"
                    )
                )
            }
        }

        // 3. Upcoming Exam insights
        val now = System.currentTimeMillis()
        val upcoming = exList.filter { it.epochMillis > now }.sortedBy { it.epochMillis }
        if (upcoming.isNotEmpty()) {
            val next = upcoming.first()
            val days = (next.epochMillis - now) / (1000 * 60 * 60 * 24)
            items.add(
                AcademicInsightItem(
                    title = "Exam Approaching: ${next.subjectName}",
                    description = "${next.name} is in $days days. Review notes for ${next.syllabusTopics.take(40)}...",
                    type = if (days <= 7) AttendanceStatus.CAUTION else AttendanceStatus.SAFE,
                    category = "Exams"
                )
            )
        }

        // 4. Marks & SEE prediction insight
        if (mkList.isNotEmpty()) {
            val avgScore = mkList.map { (it.scoredMarks / it.totalMarks) * 100f }.average()
            items.add(
                AcademicInsightItem(
                    title = "Internal Marks Average: %.1f%%".format(avgScore),
                    description = "Target overall score is ${profile.targetGpaOrMarks}%. Aim for 80%+ in final SEE papers.",
                    type = AttendanceStatus.SAFE,
                    category = "Marks"
                )
            )
        }

        items
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun markSessionAttendance(subjectId: Long, isPresent: Boolean) {
        viewModelScope.launch {
            val epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            repository.markAttendance(subjectId, isPresent, epochDay)
        }
    }

    fun markSessionCancelled(subjectId: Long, classSessionId: Long? = null) {
        viewModelScope.launch {
            val epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            repository.markClassCancelled(subjectId, epochDay, classSessionId)
        }
    }

    fun markSubjectClassCancelled(subjectId: Long) {
        viewModelScope.launch {
            val epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            repository.markClassCancelled(subjectId, epochDay)
        }
    }

    fun undoCancelledClass(subjectId: Long) {
        viewModelScope.launch {
            repository.undoCancelledClass(subjectId)
        }
    }

    fun markClassPresent(subjectId: Long) {
        viewModelScope.launch {
            val epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            repository.markAttendance(subjectId, true, epochDay)
        }
    }

    fun markClassAbsent(subjectId: Long) {
        viewModelScope.launch {
            val epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            repository.markAttendance(subjectId, false, epochDay)
        }
    }

    fun markAllTodayPresent(sessionsToday: List<ClassSession>) {
        viewModelScope.launch {
            val epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            sessionsToday.forEach { session ->
                repository.markAttendance(session.subjectId, true, epochDay, session.id)
            }
        }
    }

    fun updateSubjectAttendance(subjectId: Long, attended: Int, conducted: Int, cancelled: Int = 0) {
        viewModelScope.launch {
            repository.updateSubjectAttendance(subjectId, attended, conducted, cancelled)
        }
    }

    fun updateRequiredAttendance(newRequirement: Float) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.saveProfile(current.copy(requiredAttendance = newRequirement.coerceIn(50f, 95f)))
        }
    }

    fun updateProfile(name: String, college: String, branch: String, semester: Int, requiredAtt: Float, targetMarks: Float) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                name = name,
                college = college,
                branch = branch,
                semester = semester,
                requiredAttendance = requiredAtt,
                targetGpaOrMarks = targetMarks
            )
            repository.saveProfile(updated)
        }
    }

    fun addSubject(name: String, code: String, faculty: String, credits: Int, category: String, colorHex: String) {
        viewModelScope.launch {
            repository.addSubject(
                Subject(
                    name = name,
                    code = code,
                    faculty = faculty,
                    credits = credits,
                    category = category,
                    colorHex = colorHex,
                    attendedClasses = 0,
                    conductedClasses = 0
                )
            )
        }
    }

    fun deleteSubject(subjectId: Long) {
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
        }
    }

    fun addSession(subjectId: Long, subjectName: String, subjectCode: String, dayOfWeek: Int, start: String, end: String, room: String, type: String) {
        viewModelScope.launch {
            repository.addSession(
                ClassSession(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    subjectCode = subjectCode,
                    dayOfWeek = dayOfWeek,
                    startTime = start,
                    endTime = end,
                    room = room,
                    classType = type
                )
            )
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun addExam(subjectId: Long, subjectName: String, name: String, type: String, epochMillis: Long, time: String, venue: String, syllabus: String) {
        viewModelScope.launch {
            repository.addExam(
                Exam(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    name = name,
                    examType = type,
                    epochMillis = epochMillis,
                    timeString = time,
                    venue = venue,
                    syllabusTopics = syllabus
                )
            )
        }
    }

    fun deleteExam(examId: Long) {
        viewModelScope.launch {
            repository.deleteExam(examId)
        }
    }

    fun addMark(subjectId: Long, subjectName: String, component: String, scored: Float, total: Float, weight: Float) {
        viewModelScope.launch {
            repository.addMark(
                MarkRecord(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    componentName = component,
                    scoredMarks = scored,
                    totalMarks = total,
                    weightPercent = weight
                )
            )
        }
    }

    fun deleteMark(markId: Long) {
        viewModelScope.launch {
            repository.deleteMark(markId)
        }
    }

    fun addNote(subjectId: Long, subjectName: String, title: String, module: String, type: String, topics: String) {
        viewModelScope.launch {
            repository.addNote(
                AcademicNote(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    title = title,
                    module = module,
                    noteType = type,
                    summaryOrTopics = topics
                )
            )
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToDefaultDemo()
        }
    }

    /**
     * Ask Attendly: Deterministic grounded Q&A over real student database.
     */
    fun answerStudentQuery(query: String): String {
        val q = query.lowercase().trim()
        val subs = subjects.value
        val req = userProfile.value.requiredAttendance.toDouble()
        val exList = exams.value

        // Check if query is about missing a class / bunking
        subs.forEach { s ->
            if (q.contains(s.name.lowercase()) || q.contains(s.code.lowercase())) {
                if (q.contains("miss") || q.contains("bunk") || q.contains("can i") || q.contains("attendance")) {
                    val sim = AttendanceMathEngine.simulateBunk(s.attendedClasses, s.conductedClasses, req, 1)
                    val safe = AttendanceMathEngine.calculateSafeMisses(s.attendedClasses, s.conductedClasses, req)
                    val rec = AttendanceMathEngine.calculateRecoveryClassesNeeded(s.attendedClasses, s.conductedClasses, req)
                    return buildString {
                        append("For ${s.name} (${s.code}):\n")
                        append("• Current Attendance: ${sim.currentPercentage}% (${s.attendedClasses}/${s.conductedClasses} classes)\n")
                        append("• Required: ${req}%\n")
                        if (sim.currentPercentage < req) {
                            append("• Status: DANGER (Below Threshold!)\n")
                            append("• You MUST NOT miss this class! You need to attend the next $rec classes consecutively to reach ${req}%.\n")
                        } else {
                            append("• Safe Misses: $safe classes remaining.\n")
                            append("• If you miss 1 class, your attendance drops to ${sim.projectedPercentage}%.\n")
                            append("• Recommendation: ${sim.recommendation}\n")
                        }
                        append("• Math Proof: ${sim.mathematicalProof}")
                    }
                }
            }
        }

        // Generic bunk question
        if (q.contains("can i miss") || q.contains("bunk")) {
            val danger = subs.filter { AttendanceMathEngine.calculatePercentage(it.attendedClasses, it.conductedClasses) < req }
            val safe = subs.filter { AttendanceMathEngine.calculateSafeMisses(it.attendedClasses, it.conductedClasses, req) >= 2 }
            return buildString {
                append("Here is your Bunk Safety summary:\n")
                if (danger.isNotEmpty()) {
                    append("❌ DO NOT MISS:\n")
                    danger.forEach {
                        val rec = AttendanceMathEngine.calculateRecoveryClassesNeeded(it.attendedClasses, it.conductedClasses, req)
                        append("  - ${it.name}: Below requirement! Need $rec consecutive classes.\n")
                    }
                }
                if (safe.isNotEmpty()) {
                    append("✅ SAFELY MISSABLE (Has Buffer):\n")
                    safe.forEach {
                        val m = AttendanceMathEngine.calculateSafeMisses(it.attendedClasses, it.conductedClasses, req)
                        append("  - ${it.name}: Can miss up to $m classes.\n")
                    }
                }
            }
        }

        // Exam question
        if (q.contains("exam") || q.contains("test") || q.contains("cie") || q.contains("see")) {
            val now = System.currentTimeMillis()
            val upcoming = exList.filter { it.epochMillis > now }.sortedBy { it.epochMillis }
            if (upcoming.isEmpty()) return "You have no upcoming exams scheduled in Attendly."
            return buildString {
                append("Upcoming Exam Schedule:\n")
                upcoming.take(3).forEach {
                    val days = (it.epochMillis - now) / (1000 * 60 * 60 * 24)
                    append("• ${it.subjectName} - ${it.name}\n")
                    append("  Date: In $days days (${it.timeString}) at ${it.venue}\n")
                    if (it.syllabusTopics.isNotEmpty()) append("  Syllabus: ${it.syllabusTopics}\n")
                }
            }
        }

        // Weak/Low subjects question
        if (q.contains("weak") || q.contains("focus") || q.contains("attention") || q.contains("danger")) {
            val lowest = subs.sortedBy { AttendanceMathEngine.calculatePercentage(it.attendedClasses, it.conductedClasses) }
            val first = lowest.firstOrNull() ?: return "No subjects registered."
            val pct = AttendanceMathEngine.calculatePercentage(first.attendedClasses, first.conductedClasses)
            val rec = AttendanceMathEngine.calculateRecoveryClassesNeeded(first.attendedClasses, first.conductedClasses, req)
            return "Your most critical subject right now is ${first.name} with ${pct}% attendance (below ${req}%). You should prioritize attending all classes for this subject and need $rec consecutive classes to safely recover."
        }

        // Notes question
        if (q.contains("note") || q.contains("material") || q.contains("pdf")) {
            val noteList = notes.value
            if (noteList.isEmpty()) return "You have no notes uploaded yet in Attendly."
            return buildString {
                append("You have ${noteList.size} notes stored across your semester subjects:\n")
                noteList.take(4).forEach {
                    append("• [${it.noteType}] ${it.subjectName}: ${it.title} (${it.module})\n")
                }
            }
        }

        // Default helpful response
        return "Attendly Academic Assistant: You have ${subs.size} subjects tracked with an overall attendance of ${overallAttendance.value.percentage}%. Ask me specific questions like 'Can I miss OS class?', 'When is my next exam?', or 'What subjects should I focus on?'"
    }
}
