package com.example.model

enum class DrinkType(
    val id: String,
    val displayName: String,
    val hydrationFactor: Float,
    val iconEmoji: String
) {
    WATER("water", "Water", 1.0f, "💧"),
    TEA("tea", "Tea", 0.9f, "🍵"),
    COFFEE("coffee", "Coffee", 0.8f, "☕"),
    JUICE("juice", "Juice", 0.85f, "🍊"),
    ELECTROLYTE("electrolyte", "Electrolyte", 1.0f, "⚡"),
    SMOOTHIE("smoothie", "Smoothie", 0.75f, "🥤");

    companion object {
        fun fromId(id: String): DrinkType = values().find { it.id == id } ?: WATER
    }
}

enum class ActivityLevel(val displayName: String, val multiplier: Float) {
    SEDENTARY("Sedentary", 1.0f),
    MODERATE("Moderate (3-4 days/wk)", 1.15f),
    ACTIVE("Active (5-6 days/wk)", 1.3f),
    ATHLETE("Athlete / High Intensity", 1.45f)
}

enum class Climate(val displayName: String, val extraMl: Int) {
    COLD("Cold / Air Conditioned", 0),
    TEMPERATE("Mild / Temperate", 150),
    WARM("Warm / Sunny", 350),
    HOT("Hot & Humid", 600)
}

enum class UnitSystem(val displayName: String) {
    METRIC("Metric (ml, kg)"),
    IMPERIAL("Imperial (fl oz, lb)")
}

enum class ReminderMode(val displayName: String, val description: String) {
    INTERVAL("Fixed Interval", "Regular reminders every set minutes during daytime"),
    SMART("Smart Hydration", "Intelligently spreads reminders and skips when ahead of pace"),
    CUSTOM("Custom Times", "Reminders at designated key moments during the day")
}

enum class ThemeMode(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("Auto (System)")
}

data class WaterLogEntry(
    val id: String,
    val timestamp: Long,
    val amountMl: Int,
    val drinkType: DrinkType = DrinkType.WATER,
    val note: String = ""
) {
    val effectiveHydrationMl: Int
        get() = (amountMl * drinkType.hydrationFactor).toInt()
}

data class DailyCheckIn(
    val dateString: String, // "YYYY-MM-DD"
    val mood: Int = 3, // 1 to 5
    val energy: Int = 3, // 1 to 5
    val sleepQuality: Int = 3, // 1 to 5
    val hoursSlept: Float = 7.5f,
    val weightKg: Float? = null,
    val symptoms: List<String> = emptyList()
)

data class UserProfile(
    val weightKg: Float = 70f,
    val heightCm: Float = 175f,
    val age: Int = 28,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val climate: Climate = Climate.TEMPERATE,
    val wakeUpTime: String = "07:00",
    val bedtime: String = "23:00",
    val dailyGoalMl: Int = 2500,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val reminderMode: ReminderMode = ReminderMode.SMART,
    val reminderIntervalMinutes: Int = 60,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val glassOpacity: Float = 0.65f,
    val reduceMotion: Boolean = false,
    val reduceTransparency: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
) {
    companion object {
        fun calculateSuggestedGoalMl(
            weightKg: Float,
            activity: ActivityLevel,
            climate: Climate
        ): Int {
            val base = (weightKg * 33f).toInt()
            val adjusted = (base * activity.multiplier).toInt() + climate.extraMl
            // Round to nearest 50 ml
            return ((adjusted + 25) / 50) * 50
        }
    }
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null
)

data class ChatMessage(
    val id: String,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)
