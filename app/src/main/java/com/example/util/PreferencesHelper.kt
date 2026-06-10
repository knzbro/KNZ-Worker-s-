package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean("app_lock_enabled", false)
        set(value) = prefs.edit().putBoolean("app_lock_enabled", value).apply()

    var isChallengeRunning: Boolean
        get() = prefs.getBoolean("challenge_running", false)
        set(value) = prefs.edit().putBoolean("challenge_running", value).apply()

    var challengeStartTimeMs: Long
        get() = prefs.getLong("challenge_start_time", -1L)
        set(value) = prefs.edit().putLong("challenge_start_time", value).apply()

    var activeTimersJson: String
        get() = prefs.getString("active_timers_json", "{}") ?: "{}"
        set(value) = prefs.edit().putString("active_timers_json", value).apply()
}
