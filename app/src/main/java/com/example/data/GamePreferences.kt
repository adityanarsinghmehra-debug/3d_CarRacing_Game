package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.game.CarLivery

class GamePreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  var bestScore: Int
    get() = prefs.getInt(KEY_BEST_SCORE, 0)
    set(value) = prefs.edit().putInt(KEY_BEST_SCORE, value).apply()

  var allTimeTopSpeed: Int
    get() = prefs.getInt(KEY_TOP_SPEED, 0)
    set(value) = prefs.edit().putInt(KEY_TOP_SPEED, value).apply()

  var totalRaces: Int
    get() = prefs.getInt(KEY_TOTAL_RACES, 0)
    set(value) = prefs.edit().putInt(KEY_TOTAL_RACES, value).apply()

  var isMuted: Boolean
    get() = prefs.getBoolean(KEY_MUTED, false)
    set(value) = prefs.edit().putBoolean(KEY_MUTED, value).apply()

  var selectedLivery: CarLivery
    get() = CarLivery.fromId(prefs.getString(KEY_LIVERY, CarLivery.INDIGO_PULSE.id))
    set(value) = prefs.edit().putString(KEY_LIVERY, value.id).apply()

  private companion object {
    const val PREFS_NAME = "neon_rush_3d_prefs"
    const val KEY_BEST_SCORE = "best_score"
    const val KEY_TOP_SPEED = "top_speed"
    const val KEY_TOTAL_RACES = "total_races"
    const val KEY_MUTED = "is_muted"
    const val KEY_LIVERY = "selected_livery"
  }
}
