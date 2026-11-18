package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonRushColorScheme =
  darkColorScheme(
    primary = CyanNeon,
    onPrimary = ObsidianVoid,
    primaryContainer = IndigoDeep,
    onPrimaryContainer = TextPrimary,
    secondary = MagentaNeon,
    onSecondary = ObsidianVoid,
    secondaryContainer = ObsidianSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = AmberGold,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = TextPrimary,
    surface = ObsidianDeep,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurface,
    onSurfaceVariant = TextSecondary,
    error = RoseSunset,
    onError = TextPrimary,
    outline = HairlineIndigo,
  )

@Composable
fun NeonRushTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = NeonRushColorScheme,
    typography = NeonRushTypography,
    content = content,
  )
}
