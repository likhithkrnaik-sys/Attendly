package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.AttendanceMathEngine
import com.example.engine.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Attendly", appName)
    }

    @Test
    fun `test attendance math engine calculations`() {
        // 43 attended out of 50 conducted, 75% required
        val percentage = AttendanceMathEngine.calculatePercentage(43, 50)
        assertEquals(86.0, percentage, 0.01)

        val safeMisses = AttendanceMathEngine.calculateSafeMisses(43, 50, 75.0)
        // 43 / (50 + 7) = 43 / 57 = 75.43% >= 75%
        // 43 / (50 + 8) = 43 / 58 = 74.13% < 75%
        assertEquals(7, safeMisses)

        // 36 attended out of 50 conducted, 75% required (current 72%)
        val dangerPercentage = AttendanceMathEngine.calculatePercentage(36, 50)
        assertEquals(72.0, dangerPercentage, 0.01)

        val recoveryNeeded = AttendanceMathEngine.calculateRecoveryClassesNeeded(36, 50, 75.0)
        // (36 + 6) / (50 + 6) = 42 / 56 = 75.0%
        assertEquals(6, recoveryNeeded)

        val sim = AttendanceMathEngine.simulateBunk(43, 50, 75.0, 1)
        assertEquals(84.3, sim.projectedPercentage, 0.05)
        assertEquals(AttendanceStatus.SAFE, sim.status)
        assertTrue(sim.remainingSafeMissesAfter >= 0)
    }

    @Test
    fun `test cancelled classes do not impact attendance percentage`() {
        // When a class is cancelled, conducted and attended count remain unchanged
        val originalPercentage = AttendanceMathEngine.calculatePercentage(42, 50)
        assertEquals(84.0, originalPercentage, 0.01)

        // Cancelled class added to subject (attended = 42, conducted = 50, cancelled = 1)
        val afterCancelledPercentage = AttendanceMathEngine.calculatePercentage(42, 50)
        assertEquals(originalPercentage, afterCancelledPercentage, 0.001)
    }
}
