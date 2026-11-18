package com.example

import androidx.compose.ui.graphics.Color
import com.example.game.CarLivery
import com.example.game.EnergyOrb
import com.example.game.GameEngine
import com.example.game.GameStatus
import com.example.game.TrafficCar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  private fun newEngine(best: Int = 100): GameEngine =
    GameEngine(
      initialBestScore = best,
      initialTopSpeed = 180,
      initialTotalRaces = 1,
      initialMuted = false,
      initialLivery = CarLivery.INDIGO_PULSE,
    )

  @Test
  fun steerRespectsLaneBoundsDuringActivePlay() {
    val engine = newEngine()
    // Cannot steer in MENU state
    assertFalse(engine.steer(-1))

    engine.startRace()
    assertEquals(1, engine.targetLane)

    assertTrue(engine.steer(-1))
    assertEquals(0, engine.targetLane)
    assertFalse(engine.steer(-1))
    assertEquals(0, engine.targetLane)

    assertTrue(engine.steer(1))
    assertTrue(engine.steer(1))
    assertEquals(2, engine.targetLane)
    assertFalse(engine.steer(1))
  }

  @Test
  fun orbPickupGrantsPointsAndNitroFuel() {
    val engine = newEngine()
    engine.startRace()
    // Drain some nitro first
    engine.isBoostingRequested = true
    repeat(30) { engine.step(1f / 60f) }
    engine.isBoostingRequested = false
    val fuelBefore = engine.nitroFuel
    val scoreBefore = engine.score

    engine.orbs.clear()
    engine.orbs.add(
      EnergyOrb(
        id = 99L,
        laneIndex = 1,
        worldX = engine.playerX,
        worldZ = GameEngine.PLAYER_Z,
        phase = 0f,
      )
    )

    val events = engine.step(1f / 60f)
    assertTrue(events.orbPickedUp)
    assertEquals(1, engine.orbsCollected)
    assertTrue(engine.score >= scoreBefore + 50f)
    assertTrue(engine.nitroFuel > fuelBefore)
  }

  @Test
  fun crashEndsRaceAndUpdatesBestRecord() {
    val engine = newEngine(best = 10)
    engine.startRace()
    repeat(40) { engine.step(1f / 60f) }

    engine.trafficCars.clear()
    engine.trafficCars.add(
      TrafficCar(
        id = 100L,
        laneIndex = 1,
        worldX = engine.playerX,
        worldZ = GameEngine.PLAYER_Z + 0.5f,
        speedFactor = 0.2f,
        bodyColor = Color.Red,
        accentColor = Color.Yellow,
      )
    )

    val events = engine.step(1f / 60f)
    assertTrue(events.crashed)
    assertEquals(GameStatus.GAME_OVER, engine.status)
    assertTrue(engine.isNewRecord)
    assertTrue(engine.bestScore > 10)
  }

  @Test
  fun pauseAndResumePreserveScoreAndState() {
    val engine = newEngine()
    engine.startRace()
    repeat(10) { engine.step(1f / 60f) }
    val pausedScore = engine.score

    engine.pauseRace()
    assertEquals(GameStatus.PAUSED, engine.status)
    engine.step(1f / 60f)
    assertEquals(pausedScore, engine.score, 0.001f)

    engine.resumeRace()
    assertEquals(GameStatus.PLAYING, engine.status)
    engine.step(1f / 60f)
    assertTrue(engine.score > pausedScore)
  }
}
