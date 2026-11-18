package com.example.game

import androidx.compose.ui.graphics.Color

enum class GameStatus {
  MENU,
  PLAYING,
  PAUSED,
  GAME_OVER,
}

enum class CarLivery(
  val id: String,
  val displayName: String,
  val primaryColor: Color,
  val highlightColor: Color,
  val underglowColor: Color,
  val spoilerColor: Color,
) {
  INDIGO_PULSE(
    id = "indigo_pulse",
    displayName = "Indigo Pulse",
    primaryColor = Color(0xFF4F46E5),
    highlightColor = Color(0xFF818CF8),
    underglowColor = Color(0xFF22D3EE),
    spoilerColor = Color(0xFF22D3EE),
  ),
  CYBER_CYAN(
    id = "cyber_cyan",
    displayName = "Cyber Cyan",
    primaryColor = Color(0xFF0891B2),
    highlightColor = Color(0xFF67E8F9),
    underglowColor = Color(0xFFE879F9),
    spoilerColor = Color(0xFFE879F9),
  ),
  CRIMSON_APEX(
    id = "crimson_apex",
    displayName = "Crimson Apex",
    primaryColor = Color(0xFFE11D48),
    highlightColor = Color(0xFFFB7185),
    underglowColor = Color(0xFFFBBF24),
    spoilerColor = Color(0xFFFBBF24),
  ),
  SOLAR_GOLD(
    id = "solar_gold",
    displayName = "Solar Gold",
    primaryColor = Color(0xFFD97706),
    highlightColor = Color(0xFFFCD34D),
    underglowColor = Color(0xFF38BDF8),
    spoilerColor = Color(0xFF38BDF8),
  );

  companion object {
    fun fromId(id: String?): CarLivery = entries.firstOrNull { it.id == id } ?: INDIGO_PULSE
  }
}

data class TrafficCar(
  val id: Long,
  val laneIndex: Int,
  var worldX: Float,
  var worldZ: Float,
  val speedFactor: Float,
  val bodyColor: Color,
  val accentColor: Color,
  var nearMissAwarded: Boolean = false,
)

data class EnergyOrb(
  val id: Long,
  val laneIndex: Int,
  var worldX: Float,
  var worldZ: Float,
  var phase: Float,
)

data class Particle(
  var worldX: Float,
  var worldY: Float,
  var worldZ: Float,
  var vx: Float,
  var vy: Float,
  var vz: Float,
  var life: Float,
  val maxLife: Float,
  val color: Color,
  val size: Float,
)

data class FloatingCallout(
  val id: Long,
  val text: String,
  val worldX: Float,
  var life: Float,
  val color: Color,
)

data class HighwayArch(
  var worldZ: Float,
  val isCyan: Boolean,
)

data class SkylineBuilding(
  val side: Int,
  val offsetX: Float,
  var worldZ: Float,
  val width: Float,
  val height: Float,
  val depth: Float,
  val windowColor: Color,
)

data class GameHudState(
  val status: GameStatus = GameStatus.MENU,
  val score: Int = 0,
  val bestScore: Int = 0,
  val speedKmh: Int = 128,
  val peakSpeedKmh: Int = 0,
  val nitroFuel: Float = 100f,
  val isBoosting: Boolean = false,
  val orbsCollected: Int = 0,
  val nearMisses: Int = 0,
  val comboMultiplier: Int = 1,
  val isNewRecord: Boolean = false,
  val isMuted: Boolean = false,
  val selectedLivery: CarLivery = CarLivery.INDIGO_PULSE,
  val totalRaces: Int = 0,
  val allTimeTopSpeed: Int = 0,
)
