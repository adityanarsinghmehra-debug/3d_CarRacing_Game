package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.audio.SoundEngine
import com.example.data.GamePreferences
import com.example.game.CarLivery
import com.example.game.GameEngine
import com.example.game.GameHudState
import com.example.game.GameStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
  private val prefs: GamePreferences,
  private val soundEngine: SoundEngine = SoundEngine(),
) : ViewModel() {

  val engine =
    GameEngine(
      initialBestScore = prefs.bestScore,
      initialTopSpeed = prefs.allTimeTopSpeed,
      initialTotalRaces = prefs.totalRaces,
      initialMuted = prefs.isMuted,
      initialLivery = prefs.selectedLivery,
    )

  private val _hudState = MutableStateFlow(engine.toHudState())
  val hudState: StateFlow<GameHudState> = _hudState.asStateFlow()

  var frameTick by mutableLongStateOf(0L)
    private set

  init {
    soundEngine.setMuted(prefs.isMuted)
    soundEngine.start()
  }

  fun onFrame(deltaSeconds: Float) {
    val events = engine.step(deltaSeconds)
    if (events.orbPickedUp) soundEngine.playOrbPickup()
    if (events.nearMissTriggered) soundEngine.playNearMiss()
    if (events.nitroIgnited) soundEngine.playNitroIgnite()
    if (events.crashed) {
      soundEngine.playCrash()
      soundEngine.updateEngine(0f, active = false)
      persistStats()
    } else if (engine.status == GameStatus.PLAYING) {
      val kmh = engine.toHudState().speedKmh
      soundEngine.updateEngine((kmh / 340f).coerceIn(0f, 1.25f), active = true)
    } else {
      soundEngine.updateEngine(0f, active = false)
    }

    _hudState.value = engine.toHudState()
    frameTick++
  }

  fun startRace() {
    engine.startRace()
    _hudState.value = engine.toHudState()
  }

  fun pauseRace() {
    engine.pauseRace()
    soundEngine.updateEngine(0f, active = false)
    _hudState.value = engine.toHudState()
  }

  fun resumeRace() {
    engine.resumeRace()
    _hudState.value = engine.toHudState()
  }

  fun togglePause() {
    when (engine.status) {
      GameStatus.PLAYING -> pauseRace()
      GameStatus.PAUSED -> resumeRace()
      else -> Unit
    }
  }

  fun returnToGarage() {
    engine.returnToMenu()
    soundEngine.updateEngine(0f, active = false)
    _hudState.value = engine.toHudState()
  }

  fun steerLeft() {
    if (engine.steer(-1)) soundEngine.playSteer()
  }

  fun steerRight() {
    if (engine.steer(1)) soundEngine.playSteer()
  }

  fun setNitroBoosting(boosting: Boolean) {
    engine.isBoostingRequested = boosting
  }

  fun selectLivery(livery: CarLivery) {
    engine.selectedLivery = livery
    prefs.selectedLivery = livery
    _hudState.value = engine.toHudState()
  }

  fun toggleMute() {
    val nextMuted = !engine.isMuted
    engine.isMuted = nextMuted
    prefs.isMuted = nextMuted
    soundEngine.setMuted(nextMuted)
    _hudState.value = engine.toHudState()
  }

  fun onAppBackgrounded() {
    if (engine.status == GameStatus.PLAYING) {
      pauseRace()
    } else {
      soundEngine.updateEngine(0f, active = false)
    }
  }

  private fun persistStats() {
    prefs.bestScore = engine.bestScore
    prefs.allTimeTopSpeed = engine.allTimeTopSpeed
    prefs.totalRaces = engine.totalRaces
  }

  override fun onCleared() {
    super.onCleared()
    soundEngine.release()
  }
}
