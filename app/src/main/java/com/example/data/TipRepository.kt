package com.example.data

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar

data class HydrationTip(
    val id: Int,
    val tip: String,
    val category: String
)

class TipRepository(private val context: Context) {
    private val tips: List<HydrationTip> by lazy { loadTips() }

    private fun loadTips(): List<HydrationTip> {
        return try {
            val inputStream = context.assets.open("hydration_tips.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<HydrationTip>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    HydrationTip(
                        id = obj.getInt("id"),
                        tip = obj.getString("tip"),
                        category = obj.getString("category")
                    )
                )
            }
            list
        } catch (e: Exception) {
            listOf(
                HydrationTip(1, "Start your morning with a glass of water to reactivate metabolism.", "Morning"),
                HydrationTip(2, "Small frequent sips absorb better than drinking large amounts at once.", "Habit"),
                HydrationTip(3, "Your brain is ~75% water. Sipping regularly sustains mental clarity.", "Focus")
            )
        }
    }

    fun getDailyTip(dayOfYearOffset: Int = 0): HydrationTip {
        if (tips.isEmpty()) return HydrationTip(0, "Stay hydrated. Feel better.", "Daily")
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + dayOfYearOffset
        val index = (dayOfYear % tips.size + tips.size) % tips.size
        return tips[index]
    }

    fun getAllTips(): List<HydrationTip> = tips
}
