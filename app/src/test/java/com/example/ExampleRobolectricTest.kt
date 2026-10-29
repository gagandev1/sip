package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesManager
import com.example.data.SampleDataGenerator
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies app_name is Sip`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sip", appName)
    }

    @Test
    fun `calculateSuggestedGoalMl produces accurate hydration goals`() {
        // 70 kg sedentary temperate: (70 * 33) * 1.0 + 150 = 2310 + 150 = 2460 -> rounded 2450
        val goal = UserProfile.calculateSuggestedGoalMl(70f, ActivityLevel.SEDENTARY, Climate.TEMPERATE)
        assertTrue("Goal should be in reasonable range for 70kg", goal in 2300..2600)

        // Active athlete in hot climate should have elevated requirement
        val hotGoal = UserProfile.calculateSuggestedGoalMl(80f, ActivityLevel.ATHLETE, Climate.HOT)
        assertTrue("Athlete in hot climate needs >= 3500ml", hotGoal >= 3500)
    }

    @Test
    fun `drink hydration factors calculate effective hydration`() {
        val water = WaterLogEntry("1", System.currentTimeMillis(), 500, DrinkType.WATER)
        assertEquals(500, water.effectiveHydrationMl)

        val tea = WaterLogEntry("2", System.currentTimeMillis(), 300, DrinkType.TEA)
        assertEquals(270, tea.effectiveHydrationMl) // 90%

        val coffee = WaterLogEntry("3", System.currentTimeMillis(), 250, DrinkType.COFFEE)
        assertEquals(200, coffee.effectiveHydrationMl) // 80%
    }

    @Test
    fun `preferencesManager persists and retrieves water logs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)
        prefs.clearAllData()

        val sampleLogs = SampleDataGenerator.createSampleLogs()
        assertTrue("Sample data generator should provide entries for 7 days", sampleLogs.isNotEmpty())
        prefs.saveWaterLogs(sampleLogs)

        val retrieved = prefs.getWaterLogs()
        assertEquals(sampleLogs.size, retrieved.size)
        assertEquals(sampleLogs.first().id, retrieved.first().id)
    }
}
