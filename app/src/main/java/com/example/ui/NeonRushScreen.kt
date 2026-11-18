package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameStatus
import com.example.ui.components.NeonRush3DCanvas
import com.example.ui.components.PremiumHud
import com.example.ui.components.PremiumOverlays
import com.example.ui.components.TouchControlsBar

@Composable
fun NeonRushScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier,
) {
  val hud by viewModel.hudState.collectAsStateWithLifecycle()

  // Drive smooth 60fps simulation synced to Compose frame clock
  LaunchedEffect(Unit) {
    var lastFrameNanos = 0L
    while (true) {
      withFrameNanos { frameNanos ->
        if (lastFrameNanos != 0L) {
          val dtSeconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
          viewModel.onFrame(dtSeconds)
        } else {
          viewModel.onFrame(1f / 60f)
        }
        lastFrameNanos = frameNanos
      }
    }
  }

  // Pause safely when app moves to background
  LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.onAppBackgrounded() }

  // Back button pauses active race or exits overlays to Garage
  BackHandler(enabled = hud.status != GameStatus.MENU) {
    when (hud.status) {
      GameStatus.PLAYING -> viewModel.pauseRace()
      GameStatus.PAUSED,
      GameStatus.GAME_OVER -> viewModel.returnToGarage()
      GameStatus.MENU -> Unit
    }
  }

  var swipeAccumulated by remember { mutableFloatStateOf(0f) }
  var swipeTriggered by remember { mutableStateOf(false) }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .onPreviewKeyEvent { event ->
          if (hud.status != GameStatus.PLAYING) return@onPreviewKeyEvent false
          when {
            event.type == KeyEventType.KeyDown &&
              (event.key == Key.DirectionLeft || event.key == Key.A) -> {
              viewModel.steerLeft()
              true
            }
            event.type == KeyEventType.KeyDown &&
              (event.key == Key.DirectionRight || event.key == Key.D) -> {
              viewModel.steerRight()
              true
            }
            event.type == KeyEventType.KeyDown && event.key == Key.Spacebar -> {
              viewModel.setNitroBoosting(true)
              true
            }
            event.type == KeyEventType.KeyUp && event.key == Key.Spacebar -> {
              viewModel.setNitroBoosting(false)
              true
            }
            else -> false
          }
        }
        .pointerInput(hud.status) {
          if (hud.status != GameStatus.PLAYING) return@pointerInput
          detectHorizontalDragGestures(
            onDragStart = {
              swipeAccumulated = 0f
              swipeTriggered = false
            },
            onDragEnd = {
              swipeAccumulated = 0f
              swipeTriggered = false
            },
            onDragCancel = {
              swipeAccumulated = 0f
              swipeTriggered = false
            },
            onHorizontalDrag = { _, dragAmount ->
              if (swipeTriggered) return@detectHorizontalDragGestures
              swipeAccumulated += dragAmount
              if (swipeAccumulated > 48f) {
                viewModel.steerRight()
                swipeTriggered = true
              } else if (swipeAccumulated < -48f) {
                viewModel.steerLeft()
                swipeTriggered = true
              }
            },
          )
        }
  ) {
    // 1. Live 3D Perspective Highway Canvas
    NeonRush3DCanvas(
      engine = viewModel.engine,
      frameTick = viewModel.frameTick,
      modifier = Modifier.fillMaxSize(),
    )

    // 2. Top Telemetry HUD
    PremiumHud(
      hud = hud,
      onToggleMute = viewModel::toggleMute,
      onTogglePause = viewModel::togglePause,
      modifier = Modifier.align(Alignment.TopCenter),
    )

    // 3. Bottom Touch Steering & Nitro Controls (only during active play)
    AnimatedVisibility(
      visible = hud.status == GameStatus.PLAYING,
      enter = fadeIn() + slideInVertically { it / 2 },
      exit = fadeOut() + slideOutVertically { it / 2 },
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      TouchControlsBar(
        nitroFuel = hud.nitroFuel,
        isBoosting = hud.isBoosting,
        onSteerLeft = viewModel::steerLeft,
        onSteerRight = viewModel::steerRight,
        onNitroChange = viewModel::setNitroBoosting,
      )
    }

    // 4. Start/Garage, Pause, and Post-Race Telemetry Debrief Overlays
    PremiumOverlays(
      hud = hud,
      onStartRace = viewModel::startRace,
      onResumeRace = viewModel::resumeRace,
      onRestartRace = viewModel::startRace,
      onReturnToGarage = viewModel::returnToGarage,
      onSelectLivery = viewModel::selectLivery,
    )
  }
}
