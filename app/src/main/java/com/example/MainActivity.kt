package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.GamePreferences
import com.example.ui.GameViewModel
import com.example.ui.NeonRushScreen
import com.example.ui.theme.NeonRushTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    setContent {
      NeonRushTheme {
        val gameViewModel: GameViewModel =
          viewModel(
            factory =
              viewModelFactory {
                initializer {
                  val app =
                    checkNotNull(this[APPLICATION_KEY]) {
                      "APPLICATION_KEY missing from CreationExtras"
                    }
                  GameViewModel(prefs = GamePreferences(app))
                }
              }
          )
        NeonRushScreen(
          viewModel = gameViewModel,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}
