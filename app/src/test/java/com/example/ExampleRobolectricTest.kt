package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GamePreferences
import com.example.game.CarLivery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Neon Rush 3D", appName)
  }

  @Test
  fun `game preferences persist records and livery`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = GamePreferences(context)
    prefs.bestScore = 2450
    prefs.allTimeTopSpeed = 318
    prefs.isMuted = true
    prefs.selectedLivery = CarLivery.CRIMSON_APEX

    val reloaded = GamePreferences(context)
    assertEquals(2450, reloaded.bestScore)
    assertEquals(318, reloaded.allTimeTopSpeed)
    assertTrue(reloaded.isMuted)
    assertEquals(CarLivery.CRIMSON_APEX, reloaded.selectedLivery)
  }
}
