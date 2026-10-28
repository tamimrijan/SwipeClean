package com.swipeclean.app.data

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate

enum class ThemeMode { SYSTEM, DARK, LIGHT }

data class AppSettings(
    val excludeScreenshots: Boolean = false,
    val excludeDownloads: Boolean = false,
    val excludeFavorites: Boolean = true,
    val excludedAlbums: Set<Long> = emptySet(),
    val sessionMin: Int = 10,
    val sessionMax: Int = 15,
    val haptics: Boolean = true,
    val dailyGoalEnabled: Boolean = true,
    val dailyGoal: Int = 30,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val dynamicColor: Boolean = false,
)

data class Stats(
    val sessions: Int = 0,
    val reviewed: Int = 0,
    val deleted: Int = 0,
    val bytesFreed: Long = 0,
    val reviewedToday: Int = 0,
    val day: Long = 0,
    val streak: Int = 0,
    val lastActiveDay: Long = -1,
) {
    fun withSession(count: Int): Stats {
        val today = LocalDate.now().toEpochDay()
        val base = if (day == today) this else copy(reviewedToday = 0, day = today)
        val newStreak = when (lastActiveDay) {
            today -> streak.coerceAtLeast(1)
            today - 1 -> streak + 1
            else -> 1
        }
        return base.copy(
            sessions = sessions + 1,
            reviewed = reviewed + count,
            reviewedToday = base.reviewedToday + count,
            streak = newStreak,
            lastActiveDay = today,
        )
    }

    /** Streak to display: it only counts while you were active today or yesterday. */
    val liveStreak: Int
        get() = if (lastActiveDay >= LocalDate.now().toEpochDay() - 1) streak else 0
}

/** Small local key-value store. Nothing here ever leaves the device. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("swipeclean", Context.MODE_PRIVATE)

    fun loadSettings() = AppSettings(
        excludeScreenshots = sp.getBoolean("ex_shots", false),
        excludeDownloads = sp.getBoolean("ex_downloads", false),
        excludeFavorites = sp.getBoolean("ex_favs", true),
        excludedAlbums = sp.getStringSet("ex_albums", emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet(),
        sessionMin = sp.getInt("s_min", 10),
        sessionMax = sp.getInt("s_max", 15),
        haptics = sp.getBoolean("haptics", true),
        dailyGoalEnabled = sp.getBoolean("goal_on", true),
        dailyGoal = sp.getInt("goal", 30),
        // New key so earlier installs also start in the light theme.
        themeMode = runCatching { ThemeMode.valueOf(sp.getString("theme_mode", "LIGHT")!!) }.getOrDefault(ThemeMode.LIGHT),
        // New key so earlier installs also start with dynamic color off.
        dynamicColor = sp.getBoolean("dynamic_color", false),
    )

    fun saveSettings(s: AppSettings) = sp.edit()
        .putBoolean("ex_shots", s.excludeScreenshots)
        .putBoolean("ex_downloads", s.excludeDownloads)
        .putBoolean("ex_favs", s.excludeFavorites)
        .putStringSet("ex_albums", s.excludedAlbums.map { it.toString() }.toSet())
        .putInt("s_min", s.sessionMin)
        .putInt("s_max", s.sessionMax)
        .putBoolean("haptics", s.haptics)
        .putBoolean("goal_on", s.dailyGoalEnabled)
        .putInt("goal", s.dailyGoal)
        .putString("theme_mode", s.themeMode.name)
        .putBoolean("dynamic_color", s.dynamicColor)
        .apply()

    fun loadStats(): Stats {
        val s = Stats(
            sessions = sp.getInt("st_sessions", 0),
            reviewed = sp.getInt("st_reviewed", 0),
            deleted = sp.getInt("st_deleted", 0),
            bytesFreed = sp.getLong("st_bytes", 0),
            reviewedToday = sp.getInt("st_today", 0),
            day = sp.getLong("st_day", 0),
            streak = sp.getInt("st_streak", 0),
            lastActiveDay = sp.getLong("st_last", -1),
        )
        val today = LocalDate.now().toEpochDay()
        return if (s.day != today) s.copy(reviewedToday = 0, day = today) else s
    }

    fun saveStats(s: Stats) = sp.edit()
        .putInt("st_sessions", s.sessions)
        .putInt("st_reviewed", s.reviewed)
        .putInt("st_deleted", s.deleted)
        .putLong("st_bytes", s.bytesFreed)
        .putInt("st_today", s.reviewedToday)
        .putLong("st_day", s.day)
        .putInt("st_streak", s.streak)
        .putLong("st_last", s.lastActiveDay)
        .apply()

    fun loadQueue(): Set<Long> =
        sp.getStringSet("queue", emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()

    fun saveQueue(ids: Set<Long>) =
        sp.edit().putStringSet("queue", ids.map { it.toString() }.toSet()).apply()

    /** Photo id -> number of the session it was last shown in. */
    fun loadHistory(): MutableMap<Long, Int> {
        val map = HashMap<Long, Int>()
        val raw = sp.getString("history", null) ?: return map
        runCatching {
            val json = JSONObject(raw)
            json.keys().forEach { key -> key.toLongOrNull()?.let { map[it] = json.getInt(key) } }
        }
        return map
    }

    fun saveHistory(history: MutableMap<Long, Int>) {
        if (history.size > MAX_HISTORY) {
            history.entries.sortedBy { it.value }
                .take(history.size - MAX_HISTORY)
                .forEach { history.remove(it.key) }
        }
        val json = JSONObject()
        history.forEach { (id, session) -> json.put(id.toString(), session) }
        sp.edit().putString("history", json.toString()).apply()
    }

    var askedForPermission: Boolean
        get() = sp.getBoolean("asked_perm", false)
        set(value) = sp.edit().putBoolean("asked_perm", value).apply()

    private companion object {
        const val MAX_HISTORY = 20_000
    }
}
