package com.example.data

import com.example.model.DailyCheckIn
import com.example.model.DrinkType
import com.example.model.WaterLogEntry
import java.text.SimpleDateFormat
import java.util.*

object SampleDataGenerator {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun createSampleLogs(baseTimestamp: Long = System.currentTimeMillis()): List<WaterLogEntry> {
        val list = mutableListOf<WaterLogEntry>()
        val cal = Calendar.getInstance().apply { timeInMillis = baseTimestamp }

        // Generate for last 7 days (day -6 up to day 0)
        val dayIntakes = listOf(
            listOf(
                Triple(8, 30, Pair(350, DrinkType.WATER)),
                Triple(11, 0, Pair(250, DrinkType.TEA)),
                Triple(13, 15, Pair(500, DrinkType.WATER)),
                Triple(15, 45, Pair(200, DrinkType.COFFEE)),
                Triple(18, 20, Pair(500, DrinkType.WATER)),
                Triple(20, 30, Pair(350, DrinkType.WATER)) // ~2150 ml
            ),
            listOf(
                Triple(7, 45, Pair(500, DrinkType.WATER)),
                Triple(10, 30, Pair(330, DrinkType.WATER)),
                Triple(13, 0, Pair(500, DrinkType.WATER)),
                Triple(16, 10, Pair(250, DrinkType.TEA)),
                Triple(19, 0, Pair(500, DrinkType.WATER)),
                Triple(21, 15, Pair(450, DrinkType.WATER)) // 2530 ml (Goal achieved)
            ),
            listOf(
                Triple(8, 15, Pair(330, DrinkType.WATER)),
                Triple(10, 45, Pair(250, DrinkType.COFFEE)),
                Triple(12, 30, Pair(500, DrinkType.WATER)),
                Triple(15, 0, Pair(330, DrinkType.WATER)),
                Triple(17, 30, Pair(350, DrinkType.JUICE)),
                Triple(19, 45, Pair(500, DrinkType.WATER)),
                Triple(21, 30, Pair(300, DrinkType.WATER)) // 2560 ml (Goal achieved)
            ),
            listOf(
                Triple(9, 0, Pair(500, DrinkType.WATER)),
                Triple(11, 30, Pair(250, DrinkType.TEA)),
                Triple(14, 0, Pair(500, DrinkType.WATER)),
                Triple(16, 30, Pair(250, DrinkType.WATER)),
                Triple(19, 15, Pair(500, DrinkType.WATER)),
                Triple(22, 0, Pair(300, DrinkType.WATER)) // 2300 ml
            ),
            listOf(
                Triple(7, 30, Pair(500, DrinkType.WATER)),
                Triple(9, 45, Pair(330, DrinkType.WATER)),
                Triple(12, 0, Pair(500, DrinkType.WATER)),
                Triple(14, 30, Pair(500, DrinkType.ELECTROLYTE)),
                Triple(17, 0, Pair(330, DrinkType.WATER)),
                Triple(19, 30, Pair(500, DrinkType.WATER)),
                Triple(21, 0, Pair(250, DrinkType.WATER)) // 2910 ml (Best day!)
            ),
            listOf(
                Triple(8, 0, Pair(400, DrinkType.WATER)),
                Triple(10, 30, Pair(250, DrinkType.COFFEE)),
                Triple(13, 0, Pair(500, DrinkType.WATER)),
                Triple(16, 0, Pair(330, DrinkType.WATER)),
                Triple(18, 30, Pair(500, DrinkType.WATER)),
                Triple(20, 45, Pair(400, DrinkType.WATER)),
                Triple(22, 0, Pair(250, DrinkType.WATER)) // 2630 ml (Goal achieved)
            ),
            // Today (current partial day)
            listOf(
                Triple(8, 0, Pair(450, DrinkType.WATER)),
                Triple(10, 15, Pair(250, DrinkType.TEA)),
                Triple(12, 30, Pair(500, DrinkType.WATER)),
                Triple(14, 45, Pair(350, DrinkType.WATER)) // 1550 ml so far
            )
        )

        for (dayOffset in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = baseTimestamp
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val entriesForDay = dayIntakes[6 - dayOffset]
            entriesForDay.forEachIndexed { index, (hour, minute, intake) ->
                dayCal.set(Calendar.HOUR_OF_DAY, hour)
                dayCal.set(Calendar.MINUTE, minute)
                dayCal.set(Calendar.SECOND, 0)
                list.add(
                    WaterLogEntry(
                        id = "sample_${dayOffset}_$index",
                        timestamp = dayCal.timeInMillis,
                        amountMl = intake.first,
                        drinkType = intake.second,
                        note = if (intake.second != DrinkType.WATER) "Refreshment" else ""
                    )
                )
            }
        }
        return list.sortedBy { it.timestamp }
    }

    fun createSampleCheckIns(baseTimestamp: Long = System.currentTimeMillis()): List<DailyCheckIn> {
        val list = mutableListOf<DailyCheckIn>()
        val sampleDays = listOf(
            // day -6
            Triple(3, 3, 7.0f) to listOf("Fatigue"),
            // day -5
            Triple(4, 4, 7.5f) to listOf("Clear skin"),
            // day -4
            Triple(5, 5, 8.0f) to listOf("High focus", "Clear skin"),
            // day -3
            Triple(3, 4, 6.5f) to listOf("Headache"),
            // day -2
            Triple(5, 5, 8.5f) to listOf("High focus", "Energetic"),
            // day -1
            Triple(4, 5, 7.8f) to listOf("Clear skin"),
            // day 0 (today)
            Triple(4, 4, 7.5f) to listOf("High focus")
        )

        var weightTracker = 70.4f
        for (dayOffset in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = baseTimestamp
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val dateStr = dateFormat.format(dayCal.time)
            val data = sampleDays[6 - dayOffset]
            weightTracker -= (0.05f * (6 - dayOffset) % 0.15f)
            list.add(
                DailyCheckIn(
                    dateString = dateStr,
                    mood = data.first.first,
                    energy = data.first.second,
                    sleepQuality = data.first.first,
                    hoursSlept = data.first.third,
                    weightKg = Math.round(weightTracker * 10f) / 10f,
                    symptoms = data.second
                )
            )
        }
        return list
    }
}
