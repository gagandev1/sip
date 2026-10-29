package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.data.SampleDataGenerator
import com.example.model.*
import com.example.util.NotificationHelper
import com.example.util.SoundHapticManager
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "SipViewModel"

class SipViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = PreferencesManager(application)
    val soundHaptic = SoundHapticManager(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val tipRepository = com.example.data.TipRepository(application)
    private var tipOffset = 0
    private val _currentTip = MutableStateFlow(tipRepository.getDailyTip())
    val currentTip: StateFlow<com.example.data.HydrationTip> = _currentTip.asStateFlow()

    fun nextTip() {
        tipOffset++
        _currentTip.value = tipRepository.getDailyTip(tipOffset)
    }

    // User Profile & Settings
    private val _profile = MutableStateFlow(prefsManager.getProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    // Logs & Health
    private val _waterLogs = MutableStateFlow<List<WaterLogEntry>>(emptyList())
    val waterLogs: StateFlow<List<WaterLogEntry>> = _waterLogs.asStateFlow()

    private val _checkIns = MutableStateFlow<List<DailyCheckIn>>(emptyList())
    val checkIns: StateFlow<List<DailyCheckIn>> = _checkIns.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    // Animation states
    private val _isPouring = MutableStateFlow(false)
    val isPouring: StateFlow<Boolean> = _isPouring.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private val _lastAddedEntry = MutableStateFlow<WaterLogEntry?>(null)
    val lastAddedEntry: StateFlow<WaterLogEntry?> = _lastAddedEntry.asStateFlow()

    // AI Chatbot
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // Lazy Firebase AI model
    private val geminiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are "Sip Coach", an encouraging, warm, evidence-based hydration and wellness companion in the Sip Android app.
                    Your goal is to help users stay healthily hydrated, optimize their energy, focus, sleep, and physical performance.
                    Provide clear, friendly, and scientifically sound advice on water intake, electrolytes, caffeinated beverage pacing, climate adjustments, and workout hydration.
                    Always maintain a gentle, positive tone that never guilt-trips. Keep answers concise, actionable, and formatted with clean paragraphs or bullet points.
                    Include a brief disclaimer that advice is for wellness guidance and not medical diagnosis.
                    """.trimIndent()
                )
            }
        )
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val loadedProfile = prefsManager.getProfile()
        _profile.value = loadedProfile

        var logs = prefsManager.getWaterLogs()
        var health = prefsManager.getCheckIns()

        // Seed initial 7-day sample data if never initialized or empty
        if (!prefsManager.hasSeededSampleData() && logs.isEmpty()) {
            logs = SampleDataGenerator.createSampleLogs()
            health = SampleDataGenerator.createSampleCheckIns()
            prefsManager.saveWaterLogs(logs)
            prefsManager.saveCheckIns(health)
            prefsManager.markSampleDataSeeded(true)
        }

        _waterLogs.value = logs
        _checkIns.value = health
        _chatMessages.value = prefsManager.getChatMessages().ifEmpty {
            listOf(
                ChatMessage(
                    id = "welcome_msg",
                    role = "model",
                    text = "Hello! I'm your Sip Coach 💧. How can I support your hydration and wellness goals today? Feel free to ask about optimal daily water targets, workout hydration, or how drinking water impacts your sleep and energy!"
                )
            )
        }

        evaluateAchievements(logs)
    }

    fun completeOnboarding(updatedProfile: UserProfile) {
        val finalProfile = updatedProfile.copy(hasCompletedOnboarding = true)
        _profile.value = finalProfile
        prefsManager.saveProfile(finalProfile)
        if (finalProfile.reminderMode != ReminderMode.INTERVAL) {
            NotificationHelper.createNotificationChannel(getApplication())
        }
    }

    fun updateProfile(newProfile: UserProfile) {
        _profile.value = newProfile
        prefsManager.saveProfile(newProfile)
        evaluateAchievements(_waterLogs.value)
    }

    // Today's summary calculations
    fun getTodayString(): String = dateFormat.format(Date())

    fun getTodayLogs(): List<WaterLogEntry> {
        val todayStr = getTodayString()
        return _waterLogs.value.filter {
            dateFormat.format(Date(it.timestamp)) == todayStr
        }.sortedByDescending { it.timestamp }
    }

    fun getTodayIntakeMl(): Int {
        return getTodayLogs().sumOf { it.effectiveHydrationMl }
    }

    fun addWater(amountMl: Int, drinkType: DrinkType = DrinkType.WATER, note: String = "") {
        val prevTodayIntake = getTodayIntakeMl()
        val goal = _profile.value.dailyGoalMl

        val newEntry = WaterLogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            amountMl = amountMl,
            drinkType = drinkType,
            note = note
        )

        val updated = _waterLogs.value + newEntry
        _waterLogs.value = updated
        _lastAddedEntry.value = newEntry
        prefsManager.saveWaterLogs(updated)

        // Haptic & Sound feedback
        if (_profile.value.hapticsEnabled) {
            soundHaptic.triggerLightHaptic()
        }
        if (_profile.value.soundEnabled) {
            soundHaptic.playWaterDropSound()
        }

        // Trigger pour animation
        _isPouring.value = true
        viewModelScope.launch {
            delay(1200)
            _isPouring.value = false
        }

        // Check goal smashed event
        val newTodayIntake = prevTodayIntake + newEntry.effectiveHydrationMl
        if (prevTodayIntake < goal && newTodayIntake >= goal) {
            _showCelebration.value = true
            if (_profile.value.hapticsEnabled) {
                soundHaptic.triggerSuccessHaptic()
            }
            viewModelScope.launch {
                delay(4000)
                _showCelebration.value = false
            }
        }

        evaluateAchievements(updated)
    }

    fun dismissCelebration() {
        _showCelebration.value = false
    }

    fun undoLastEntry() {
        val last = _lastAddedEntry.value ?: _waterLogs.value.lastOrNull() ?: return
        val updated = _waterLogs.value.filter { it.id != last.id }
        _waterLogs.value = updated
        _lastAddedEntry.value = null
        prefsManager.saveWaterLogs(updated)
        if (_profile.value.hapticsEnabled) {
            soundHaptic.triggerLightHaptic()
        }
        evaluateAchievements(updated)
    }

    fun deleteEntry(entryId: String) {
        val updated = _waterLogs.value.filter { it.id != entryId }
        _waterLogs.value = updated
        prefsManager.saveWaterLogs(updated)
        evaluateAchievements(updated)
    }

    fun saveCheckIn(checkIn: DailyCheckIn) {
        val existing = _checkIns.value.filter { it.dateString != checkIn.dateString }
        val updated = existing + checkIn
        _checkIns.value = updated
        prefsManager.saveCheckIns(updated)
    }

    fun getTodayCheckIn(): DailyCheckIn? {
        val today = getTodayString()
        return _checkIns.value.find { it.dateString == today }
    }

    // Stats calculations
    fun calculateStreak(): Pair<Int, Int> { // (currentStreak, longestStreak)
        val logs = _waterLogs.value
        val goal = _profile.value.dailyGoalMl
        if (logs.isEmpty()) return Pair(0, 0)

        val dayTotals = logs.groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { (_, entries) -> entries.sumOf { it.effectiveHydrationMl } }

        val cal = Calendar.getInstance()
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0

        // Check backwards from today or yesterday
        val todayStr = dateFormat.format(cal.time)
        val todayTotal = dayTotals[todayStr] ?: 0
        val isTodayMet = todayTotal >= goal

        if (isTodayMet) {
            currentStreak = 1
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yestStr = dateFormat.format(cal.time)
            if ((dayTotals[yestStr] ?: 0) >= goal) {
                // streak still alive from yesterday
            } else {
                cal.add(Calendar.DAY_OF_YEAR, 1) // revert
            }
        }

        while (true) {
            val dateStr = dateFormat.format(cal.time)
            if ((dayTotals[dateStr] ?: 0) >= goal) {
                currentStreak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        // Calculate all-time longest streak
        val sortedDays = dayTotals.keys.sorted()
        if (sortedDays.isNotEmpty()) {
            val checkCal = Calendar.getInstance()
            var prevDate: Date? = null
            for (day in sortedDays) {
                val total = dayTotals[day] ?: 0
                if (total >= goal) {
                    val curDate = dateFormat.parse(day)
                    if (prevDate != null) {
                        checkCal.time = prevDate
                        checkCal.add(Calendar.DAY_OF_YEAR, 1)
                        if (dateFormat.format(checkCal.time) == day) {
                            tempStreak++
                        } else {
                            tempStreak = 1
                        }
                    } else {
                        tempStreak = 1
                    }
                    prevDate = curDate
                    if (tempStreak > longestStreak) longestStreak = tempStreak
                } else {
                    tempStreak = 0
                    prevDate = null
                }
            }
        }
        if (currentStreak > longestStreak) longestStreak = currentStreak
        return Pair(currentStreak, longestStreak)
    }

    fun calculateDailyAverageMl(): Int {
        val logs = _waterLogs.value
        if (logs.isEmpty()) return 0
        val dayTotals = logs.groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { (_, entries) -> entries.sumOf { it.effectiveHydrationMl } }
        val sum = dayTotals.values.sum()
        return if (dayTotals.isNotEmpty()) sum / dayTotals.size else 0
    }

    fun calculateBestDayMl(): Int {
        val logs = _waterLogs.value
        if (logs.isEmpty()) return 0
        val dayTotals = logs.groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { (_, entries) -> entries.sumOf { it.effectiveHydrationMl } }
        return dayTotals.values.maxOrNull() ?: 0
    }

    fun calculateGoalCompletionRate(): Int {
        val logs = _waterLogs.value
        val goal = _profile.value.dailyGoalMl
        if (logs.isEmpty()) return 0
        val dayTotals = logs.groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { (_, entries) -> entries.sumOf { it.effectiveHydrationMl } }
        val metDays = dayTotals.values.count { it >= goal }
        return if (dayTotals.isNotEmpty()) ((metDays.toFloat() / dayTotals.size) * 100).toInt() else 0
    }

    fun getDrinkTypeBreakdown(): Map<DrinkType, Int> {
        val logs = _waterLogs.value
        val map = mutableMapOf<DrinkType, Int>()
        DrinkType.values().forEach { map[it] = 0 }
        logs.forEach {
            map[it.drinkType] = (map[it.drinkType] ?: 0) + it.effectiveHydrationMl
        }
        return map
    }

    private fun evaluateAchievements(logs: List<WaterLogEntry>) {
        val state = prefsManager.getAchievementsState()
        val todayStr = getTodayString()
        val totalVolumeMl = logs.sumOf { it.effectiveHydrationMl }
        val (curStreak, longStreak) = calculateStreak()

        val earlyBirdLogged = logs.any {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.HOUR_OF_DAY) < 9 && it.amountMl >= 500
        }

        val allAchievements = listOf(
            Achievement(
                id = "first_sip",
                title = "First Sip",
                description = "Logged your first hydrating sip",
                iconEmoji = "💧",
                isUnlocked = state["first_sip"]?.first ?: (logs.isNotEmpty()),
                unlockedDate = state["first_sip"]?.second ?: if (logs.isNotEmpty()) todayStr else null
            ),
            Achievement(
                id = "streak_3",
                title = "3-Day Wave",
                description = "Hit your daily water goal 3 days in a row",
                iconEmoji = "🌊",
                isUnlocked = state["streak_3"]?.first ?: (longStreak >= 3),
                unlockedDate = state["streak_3"]?.second ?: if (longStreak >= 3) todayStr else null
            ),
            Achievement(
                id = "streak_7",
                title = "7-Day Flow",
                description = "Mastered a full week of consistent hydration",
                iconEmoji = "✨",
                isUnlocked = state["streak_7"]?.first ?: (longStreak >= 7),
                unlockedDate = state["streak_7"]?.second ?: if (longStreak >= 7) todayStr else null
            ),
            Achievement(
                id = "streak_30",
                title = "Hydration Hero",
                description = "Maintained a 30-day hydration habit",
                iconEmoji = "🏆",
                isUnlocked = state["streak_30"]?.first ?: (longStreak >= 30),
                unlockedDate = state["streak_30"]?.second ?: if (longStreak >= 30) todayStr else null
            ),
            Achievement(
                id = "volume_10l",
                title = "10L Milestone",
                description = "Consumed more than 10 liters of healthy fluids",
                iconEmoji = "🐳",
                isUnlocked = state["volume_10l"]?.first ?: (totalVolumeMl >= 10000),
                unlockedDate = state["volume_10l"]?.second ?: if (totalVolumeMl >= 10000) todayStr else null
            ),
            Achievement(
                id = "early_bird",
                title = "Early Bird",
                description = "Drank 500 ml or more before 9:00 AM",
                iconEmoji = "🌅",
                isUnlocked = state["early_bird"]?.first ?: earlyBirdLogged,
                unlockedDate = state["early_bird"]?.second ?: if (earlyBirdLogged) todayStr else null
            )
        )

        _achievements.value = allAchievements
        prefsManager.saveAchievements(allAchievements)
    }

    // Health Insights local rule-based correlation
    fun getHealthInsights(): List<String> {
        val checkIns = _checkIns.value
        val logs = _waterLogs.value
        val goal = _profile.value.dailyGoalMl
        val list = mutableListOf<String>()

        if (checkIns.size < 3) {
            list.add("Log your daily mood, sleep, and water for a few days to unlock personal wellness correlations.")
            return list
        }

        val dayTotals = logs.groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { (_, entries) -> entries.sumOf { it.effectiveHydrationMl } }

        val goalMetDays = checkIns.filter { (dayTotals[it.dateString] ?: 0) >= goal }
        val goalMissedDays = checkIns.filter { (dayTotals[it.dateString] ?: 0) < goal }

        if (goalMetDays.isNotEmpty() && goalMissedDays.isNotEmpty()) {
            val avgEnergyMet = goalMetDays.map { it.energy }.average()
            val avgEnergyMissed = goalMissedDays.map { it.energy }.average()
            if (avgEnergyMet > avgEnergyMissed) {
                val pct = (((avgEnergyMet - avgEnergyMissed) / avgEnergyMissed) * 100).toInt()
                list.add("On days you hit your water goal, your self-reported energy was $pct% higher.")
            }

            val avgSleepMet = goalMetDays.map { it.sleepQuality }.average()
            val avgSleepMissed = goalMissedDays.map { it.sleepQuality }.average()
            if (avgSleepMet > avgSleepMissed) {
                list.add("Hydration consistency strongly correlates with deeper, more refreshing sleep quality.")
            }
        }

        val headacheDays = checkIns.filter { it.symptoms.contains("Headache") }
        if (headacheDays.isNotEmpty()) {
            val avgIntakeHeadache = headacheDays.map { dayTotals[it.dateString] ?: 0 }.average()
            val overallAvg = calculateDailyAverageMl()
            if (avgIntakeHeadache < overallAvg) {
                list.add("Headaches occurred primarily on days where your water intake fell below your average.")
            }
        }

        if (list.isEmpty()) {
            list.add("Your hydration levels are consistently supporting balanced energy and daily wellness.")
        }
        return list
    }

    // AI Chatbot multi-turn interaction
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            text = userText.trim(),
            timestamp = System.currentTimeMillis()
        )
        val currentList = _chatMessages.value + userMsg
        _chatMessages.value = currentList
        _isAiThinking.value = true
        _aiError.value = null

        val modelMsgId = UUID.randomUUID().toString()
        val initialModelMsg = ChatMessage(
            id = modelMsgId,
            role = "model",
            text = "",
            timestamp = System.currentTimeMillis()
        )
        _chatMessages.value = currentList + initialModelMsg

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                // Build conversation context
                val promptBuilder = StringBuilder()
                val todayIntake = getTodayIntakeMl()
                val goal = _profile.value.dailyGoalMl
                promptBuilder.append("[User Context: Daily goal: ${goal}ml, Current today intake: ${todayIntake}ml, Activity: ${_profile.value.activityLevel.displayName}, Climate: ${_profile.value.climate.displayName}]\n")
                promptBuilder.append("User message: $userText")

                withTimeout(60_000L) {
                    geminiModel.generateContentStream(promptBuilder.toString()).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        _chatMessages.value = _chatMessages.value.map {
                            if (it.id == modelMsgId) it.copy(text = accumulated.toString()) else it
                        }
                    }
                }

                if (accumulated.isBlank()) {
                    _chatMessages.value = _chatMessages.value.map {
                        if (it.id == modelMsgId) it.copy(text = "Stay hydrated! Remember to sip water consistently throughout your day. 💧") else it
                    }
                }
                prefsManager.saveChatMessages(_chatMessages.value)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini stream timed out", e)
                _aiError.value = "Response timed out. Please try asking again."
                _chatMessages.value = _chatMessages.value.map {
                    if (it.id == modelMsgId) it.copy(text = "I'm having a brief connection delay. Please tap retry or ask another question! 💧", isError = true) else it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini chat failed", e)
                _aiError.value = e.localizedMessage ?: "AI interaction failed."
                _chatMessages.value = _chatMessages.value.map {
                    if (it.id == modelMsgId) it.copy(
                        text = "I'm currently unable to connect to the hydration server (${e.localizedMessage ?: "Network error"}). In the meantime, remember to drink a fresh glass of water! 💧",
                        isError = true
                    ) else it
                }
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    // Sample data management & Export
    fun clearSampleData() {
        _waterLogs.value = emptyList()
        _checkIns.value = emptyList()
        prefsManager.saveWaterLogs(emptyList())
        prefsManager.saveCheckIns(emptyList())
        prefsManager.markSampleDataSeeded(false)
        evaluateAchievements(emptyList())
    }

    fun seedSampleData() {
        val logs = SampleDataGenerator.createSampleLogs()
        val health = SampleDataGenerator.createSampleCheckIns()
        _waterLogs.value = logs
        _checkIns.value = health
        prefsManager.saveWaterLogs(logs)
        prefsManager.saveCheckIns(health)
        prefsManager.markSampleDataSeeded(true)
        evaluateAchievements(logs)
    }

    fun resetAllData() {
        prefsManager.clearAllData()
        _profile.value = UserProfile()
        _waterLogs.value = emptyList()
        _checkIns.value = emptyList()
        _achievements.value = emptyList()
        _chatMessages.value = emptyList()
    }

    fun exportDataAsJson(): String {
        val root = JSONObject()
        val logsArr = JSONArray()
        _waterLogs.value.forEach {
            logsArr.put(JSONObject().apply {
                put("timestamp", it.timestamp)
                put("amountMl", it.amountMl)
                put("drinkType", it.drinkType.id)
                put("note", it.note)
            })
        }
        root.put("logs", logsArr)

        val healthArr = JSONArray()
        _checkIns.value.forEach {
            healthArr.put(JSONObject().apply {
                put("date", it.dateString)
                put("mood", it.mood)
                put("energy", it.energy)
                put("sleepQuality", it.sleepQuality)
                put("hoursSlept", it.hoursSlept)
                if (it.weightKg != null) put("weightKg", it.weightKg)
            })
        }
        root.put("health", healthArr)
        return root.toString(2)
    }
}
