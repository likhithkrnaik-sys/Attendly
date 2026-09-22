package com.example.engine

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

enum class AttendanceStatus {
    SAFE,
    CAUTION,
    DANGER
}

data class AttendanceSummary(
    val percentage: Double,
    val attended: Int,
    val conducted: Int,
    val requiredPercentage: Double,
    val safetyMargin: Double, // percentage - required
    val safeMisses: Int,
    val recoveryClassesNeeded: Int,
    val status: AttendanceStatus,
    val statusMessage: String
)

data class BunkSimulationResult(
    val missCount: Int,
    val currentPercentage: Double,
    val projectedPercentage: Double,
    val percentageDrop: Double,
    val requiredPercentage: Double,
    val remainingSafeMissesAfter: Int,
    val status: AttendanceStatus,
    val recommendation: String,
    val mathematicalProof: String
)

data class PrioritySubjectInfo(
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String,
    val currentPercentage: Double,
    val requiredPercentage: Double,
    val attended: Int,
    val conducted: Int,
    val safeMisses: Int,
    val recoveryClasses: Int,
    val priorityRank: Int, // 1 = Highest priority (avoid missing)
    val status: AttendanceStatus,
    val primaryReason: String,
    val actionAdvice: String
)

object AttendanceMathEngine {

    /**
     * Calculates the exact attendance percentage.
     * Guaranteed to never produce NaN or Infinity.
     * When conducted is 0, defaults to 100.0% (new semester clean buffer).
     */
    fun calculatePercentage(attended: Int, conducted: Int): Double {
        if (conducted <= 0) return 100.0
        val clampedAttended = attended.coerceIn(0, conducted)
        val raw = (clampedAttended.toDouble() / conducted.toDouble()) * 100.0
        return (raw * 10.0).let { Math.round(it) / 10.0 } // 1 decimal place
    }

    /**
     * Calculates the maximum classes that can be missed while remaining >= requiredPercentage.
     * Formula:
     *   attended / (conducted + x) >= R
     *   x <= (attended - conducted * R) / R
     * Guaranteed to never be negative.
     */
    fun calculateSafeMisses(attended: Int, conducted: Int, requiredPercentage: Double): Int {
        if (conducted <= 0) return 0
        val r = (requiredPercentage / 100.0).coerceIn(0.01, 0.999)
        val numerator = attended.toDouble() - (conducted.toDouble() * r)
        if (numerator <= 0.0) return 0
        val maxMisses = floor(numerator / r).toInt()
        return max(0, maxMisses)
    }

    /**
     * Calculates the minimum consecutive classes a student must attend to reach or exceed requiredPercentage.
     * Formula:
     *   (attended + x) / (conducted + x) >= R
     *   x * (1 - R) >= conducted * R - attended
     *   x >= (conducted * R - attended) / (1 - R)
     * Returns 0 if already at or above requirement.
     */
    fun calculateRecoveryClassesNeeded(attended: Int, conducted: Int, requiredPercentage: Double): Int {
        if (conducted <= 0) return 0
        val current = calculatePercentage(attended, conducted)
        if (current >= requiredPercentage) return 0

        val r = (requiredPercentage / 100.0).coerceIn(0.01, 0.99)
        val numerator = (conducted.toDouble() * r) - attended.toDouble()
        if (numerator <= 0.0) return 0

        val denominator = 1.0 - r
        if (denominator <= 0.0001) return 99 // Near 100% required

        val needed = ceil(numerator / denominator).toInt()
        return max(1, needed)
    }

    /**
     * Returns complete deterministic attendance summary with transparent status message.
     */
    fun getAttendanceSummary(attended: Int, conducted: Int, requiredPercentage: Double = 75.0): AttendanceSummary {
        val percentage = calculatePercentage(attended, conducted)
        val safetyMargin = (percentage - requiredPercentage).let { Math.round(it * 10.0) / 10.0 }
        val safeMisses = calculateSafeMisses(attended, conducted, requiredPercentage)
        val recovery = calculateRecoveryClassesNeeded(attended, conducted, requiredPercentage)

        val status = when {
            percentage < requiredPercentage -> AttendanceStatus.DANGER
            percentage < requiredPercentage + 3.0 || safeMisses <= 1 -> AttendanceStatus.CAUTION
            else -> AttendanceStatus.SAFE
        }

        val message = when (status) {
            AttendanceStatus.DANGER -> "Below threshold (${percentage}%). Must attend next $recovery classes consecutively."
            AttendanceStatus.CAUTION -> "Borderline buffer (+${safetyMargin}%). Only $safeMisses safe miss remaining."
            AttendanceStatus.SAFE -> "Comfortable buffer (+${safetyMargin}%). Can safely miss up to $safeMisses classes."
        }

        return AttendanceSummary(
            percentage = percentage,
            attended = attended,
            conducted = conducted,
            requiredPercentage = requiredPercentage,
            safetyMargin = safetyMargin,
            safeMisses = safeMisses,
            recoveryClassesNeeded = recovery,
            status = status,
            statusMessage = message
        )
    }

    /**
     * Simulates the exact impact of missing [missCount] upcoming classes.
     * Transparently outputs mathematical reasoning, remaining safe misses, and status.
     */
    fun simulateBunk(
        attended: Int,
        conducted: Int,
        requiredPercentage: Double,
        missCount: Int
    ): BunkSimulationResult {
        val currentPercentage = calculatePercentage(attended, conducted)
        val projectedConducted = conducted + missCount
        val projectedPercentage = calculatePercentage(attended, projectedConducted)
        val drop = (currentPercentage - projectedPercentage).let { Math.round(it * 10.0) / 10.0 }
        val remainingSafeMissesAfter = calculateSafeMisses(attended, projectedConducted, requiredPercentage)

        val status = when {
            projectedPercentage < requiredPercentage -> AttendanceStatus.DANGER
            projectedPercentage < requiredPercentage + 2.0 -> AttendanceStatus.CAUTION
            else -> AttendanceStatus.SAFE
        }

        val recommendation = when (status) {
            AttendanceStatus.SAFE -> {
                if (missCount == 1) {
                    "Missing 1 class is safe and keeps attendance at ${projectedPercentage}%, comfortably above ${requiredPercentage}%."
                } else {
                    "Missing $missCount classes keeps you at ${projectedPercentage}%, still above the required threshold."
                }
            }
            AttendanceStatus.CAUTION -> {
                "Missing $missCount leaves almost no safety margin (${projectedPercentage}% vs required ${requiredPercentage}%). Next missed class will drop you into the danger zone."
            }
            AttendanceStatus.DANGER -> {
                val neededToRecover = calculateRecoveryClassesNeeded(attended, projectedConducted, requiredPercentage)
                "Missing $missCount will pull attendance down to ${projectedPercentage}%, violating the ${requiredPercentage}% requirement. You would need to attend $neededToRecover subsequent classes without absence."
            }
        }

        val mathProof = if (projectedConducted > 0) {
            "Proof: $attended / ($conducted + $missCount) = $attended / $projectedConducted = ${projectedPercentage}% (Target: ${requiredPercentage}%)"
        } else {
            "New semester baseline"
        }

        return BunkSimulationResult(
            missCount = missCount,
            currentPercentage = currentPercentage,
            projectedPercentage = projectedPercentage,
            percentageDrop = drop,
            requiredPercentage = requiredPercentage,
            remainingSafeMissesAfter = remainingSafeMissesAfter,
            status = status,
            recommendation = recommendation,
            mathematicalProof = mathProof
        )
    }

    /**
     * Computes the required SEE (Semester End Exam) marks to achieve target overall grade.
     * e.g., CIE = 42/50 (weight 50%), SEE = ? / 50 (weight 50%), Target = 85/100
     */
    fun calculateRequiredSeeMarks(
        cieScored: Float,
        cieTotal: Float,
        seeTotal: Float,
        targetOverallPercent: Float,
        cieWeight: Float = 50f,
        seeWeight: Float = 50f
    ): Float {
        val ciePercentage = if (cieTotal > 0) (cieScored / cieTotal) * 100f else 0f
        val cieContribution = (ciePercentage * (cieWeight / 100f))
        val neededFromSee = targetOverallPercent - cieContribution

        if (neededFromSee <= 0f) return 0f
        val seePercentageNeeded = (neededFromSee / (seeWeight / 100f))
        val seeMarksNeeded = (seePercentageNeeded / 100f) * seeTotal

        return max(0f, (Math.round(seeMarksNeeded * 10f) / 10f))
    }
}
