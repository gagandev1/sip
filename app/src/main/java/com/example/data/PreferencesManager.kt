package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sip_app_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROFILE = "key_user_profile"
        private const val KEY_LOGS = "key_water_logs"
        private const val KEY_CHECK_INS = "key_health_check_ins"
        private const val KEY_ACHIEVEMENTS = "key_achievements"
        private const val KEY_CHAT_MESSAGES = "key_chat_messages"
        private const val KEY_HAS_SEEDED = "key_has_seeded_sample_data"
    }

    fun saveProfile(profile: UserProfile) {
        val obj = JSONObject().apply {
            put("weightKg", profile.weightKg.toDouble())
            put("heightCm", profile.heightCm.toDouble())
            put("age", profile.age)
            put("activityLevel", profile.activityLevel.name)
            put("climate", profile.climate.name)
            put("wakeUpTime", profile.wakeUpTime)
            put("bedtime", profile.bedtime)
            put("dailyGoalMl", profile.dailyGoalMl)
            put("unitSystem", profile.unitSystem.name)
            put("reminderMode", profile.reminderMode.name)
            put("reminderIntervalMinutes", profile.reminderIntervalMinutes)
            put("soundEnabled", profile.soundEnabled)
            put("hapticsEnabled", profile.hapticsEnabled)
            put("themeMode", profile.themeMode.name)
            put("hasCompletedOnboarding", profile.hasCompletedOnboarding)
        }
        prefs.edit().putString(KEY_PROFILE, obj.toString()).apply()
    }

    fun getProfile(): UserProfile {
        val raw = prefs.getString(KEY_PROFILE, null) ?: return UserProfile()
        return try {
            val obj = JSONObject(raw)
            UserProfile(
                weightKg = obj.optDouble("weightKg", 70.0).toFloat(),
                heightCm = obj.optDouble("heightCm", 175.0).toFloat(),
                age = obj.optInt("age", 28),
                activityLevel = runCatching { ActivityLevel.valueOf(obj.optString("activityLevel", "MODERATE")) }.getOrDefault(ActivityLevel.MODERATE),
                climate = runCatching { Climate.valueOf(obj.optString("climate", "TEMPERATE")) }.getOrDefault(Climate.TEMPERATE),
                wakeUpTime = obj.optString("wakeUpTime", "07:00"),
                bedtime = obj.optString("bedtime", "23:00"),
                dailyGoalMl = obj.optInt("dailyGoalMl", 2500),
                unitSystem = runCatching { UnitSystem.valueOf(obj.optString("unitSystem", "METRIC")) }.getOrDefault(UnitSystem.METRIC),
                reminderMode = runCatching { ReminderMode.valueOf(obj.optString("reminderMode", "SMART")) }.getOrDefault(ReminderMode.SMART),
                reminderIntervalMinutes = obj.optInt("reminderIntervalMinutes", 60),
                soundEnabled = obj.optBoolean("soundEnabled", true),
                hapticsEnabled = obj.optBoolean("hapticsEnabled", true),
                themeMode = runCatching { ThemeMode.valueOf(obj.optString("themeMode", "SYSTEM")) }.getOrDefault(ThemeMode.SYSTEM),
                glassOpacity = obj.optDouble("glassOpacity", 0.65).toFloat(),
                reduceMotion = obj.optBoolean("reduceMotion", false),
                reduceTransparency = obj.optBoolean("reduceTransparency", false),
                hasCompletedOnboarding = obj.optBoolean("hasCompletedOnboarding", false)
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun saveWaterLogs(logs: List<WaterLogEntry>) {
        val arr = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("id", log.id)
                put("timestamp", log.timestamp)
                put("amountMl", log.amountMl)
                put("drinkType", log.drinkType.id)
                put("note", log.note)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_LOGS, arr.toString()).apply()
    }

    fun getWaterLogs(): List<WaterLogEntry> {
        val raw = prefs.getString(KEY_LOGS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<WaterLogEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WaterLogEntry(
                        id = obj.getString("id"),
                        timestamp = obj.getLong("timestamp"),
                        amountMl = obj.getInt("amountMl"),
                        drinkType = DrinkType.fromId(obj.optString("drinkType", "water")),
                        note = obj.optString("note", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCheckIns(checkIns: List<DailyCheckIn>) {
        val arr = JSONArray()
        checkIns.forEach { checkIn ->
            val obj = JSONObject().apply {
                put("dateString", checkIn.dateString)
                put("mood", checkIn.mood)
                put("energy", checkIn.energy)
                put("sleepQuality", checkIn.sleepQuality)
                put("hoursSlept", checkIn.hoursSlept.toDouble())
                if (checkIn.weightKg != null) put("weightKg", checkIn.weightKg.toDouble())
                val symptomsArr = JSONArray()
                checkIn.symptoms.forEach { symptomsArr.put(it) }
                put("symptoms", symptomsArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CHECK_INS, arr.toString()).apply()
    }

    fun getCheckIns(): List<DailyCheckIn> {
        val raw = prefs.getString(KEY_CHECK_INS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<DailyCheckIn>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val symptomsList = mutableListOf<String>()
                val symptomsArr = obj.optJSONArray("symptoms")
                if (symptomsArr != null) {
                    for (j in 0 until symptomsArr.length()) {
                        symptomsList.add(symptomsArr.getString(j))
                    }
                }
                list.add(
                    DailyCheckIn(
                        dateString = obj.getString("dateString"),
                        mood = obj.optInt("mood", 3),
                        energy = obj.optInt("energy", 3),
                        sleepQuality = obj.optInt("sleepQuality", 3),
                        hoursSlept = obj.optDouble("hoursSlept", 7.5).toFloat(),
                        weightKg = if (obj.has("weightKg")) obj.getDouble("weightKg").toFloat() else null,
                        symptoms = symptomsList
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAchievements(achievements: List<Achievement>) {
        val arr = JSONArray()
        achievements.forEach { a ->
            val obj = JSONObject().apply {
                put("id", a.id)
                put("isUnlocked", a.isUnlocked)
                if (a.unlockedDate != null) put("unlockedDate", a.unlockedDate)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_ACHIEVEMENTS, arr.toString()).apply()
    }

    fun getAchievementsState(): Map<String, Pair<Boolean, String?>> {
        val raw = prefs.getString(KEY_ACHIEVEMENTS, null) ?: return emptyMap()
        return try {
            val arr = JSONArray(raw)
            val map = mutableMapOf<String, Pair<Boolean, String?>>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                val isUnlocked = obj.getBoolean("isUnlocked")
                val date = if (obj.has("unlockedDate")) obj.getString("unlockedDate") else null
                map[id] = Pair(isUnlocked, date)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveChatMessages(messages: List<ChatMessage>) {
        val arr = JSONArray()
        messages.takeLast(50).forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("role", m.role)
                put("text", m.text)
                put("timestamp", m.timestamp)
                put("isError", m.isError)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CHAT_MESSAGES, arr.toString()).apply()
    }

    fun getChatMessages(): List<ChatMessage> {
        val raw = prefs.getString(KEY_CHAT_MESSAGES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.getString("id"),
                        role = obj.getString("role"),
                        text = obj.getString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isError = obj.optBoolean("isError", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun hasSeededSampleData(): Boolean = prefs.getBoolean(KEY_HAS_SEEDED, false)

    fun markSampleDataSeeded(seeded: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEDED, seeded).apply()
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
