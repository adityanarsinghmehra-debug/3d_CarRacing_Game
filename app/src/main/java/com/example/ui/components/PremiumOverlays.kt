package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.CarLivery
import com.example.game.GameHudState
import com.example.game.GameStatus
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HairlineCyan
import com.example.ui.theme.HairlineIndigo
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MagentaNeon
import com.example.ui.theme.ObsidianCardSolid
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RoseSunset
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PremiumOverlays(
  hud: GameHudState,
  onStartRace: () -> Unit,
  onResumeRace: () -> Unit,
  onRestartRace: () -> Unit,
  onReturnToGarage: () -> Unit,
  onSelectLivery: (CarLivery) -> Unit,
  modifier: Modifier = Modifier,
) {
  val showOverlay = hud.status != GameStatus.PLAYING

  AnimatedVisibility(
    visible = showOverlay,
    enter = fadeIn() + scaleIn(initialScale = 0.96f),
    exit = fadeOut() + scaleOut(targetScale = 0.96f),
    modifier = modifier.fillMaxSize(),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color(0x8805030E))
          .safeDrawingPadding()
          .padding(horizontal = 20.dp, vertical = 16.dp),
      contentAlignment = Alignment.Center,
    ) {
      when (hud.status) {
        GameStatus.MENU ->
          MenuGarageCard(
            hud = hud,
            onStartRace = onStartRace,
            onSelectLivery = onSelectLivery,
          )
        GameStatus.PAUSED ->
          PauseCard(
            hud = hud,
            onResume = onResumeRace,
            onRestart = onRestartRace,
            onGarage = onReturnToGarage,
          )
        GameStatus.GAME_OVER ->
          GameOverDebriefCard(
            hud = hud,
            onRaceAgain = onRestartRace,
            onGarage = onReturnToGarage,
          )
        GameStatus.PLAYING -> Unit
      }
    }
  }
}

@Composable
private fun GlassModalShell(
  borderBrush: Brush = Brush.linearGradient(listOf(HairlineCyan, HairlineIndigo, Color(0x55E879F9))),
  content: @Composable () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(28.dp),
    color = Color(0xEB0E0B22),
    border = BorderStroke(1.2.dp, borderBrush),
    tonalElevation = 12.dp,
    shadowElevation = 24.dp,
    modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
  ) {
    Column(
      modifier =
        Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      content()
    }
  }
}

@Composable
private fun MenuGarageCard(
  hud: GameHudState,
  onStartRace: () -> Unit,
  onSelectLivery: (CarLivery) -> Unit,
) {
  GlassModalShell {
    // Edition pill badge
    Row(
      modifier =
        Modifier.clip(RoundedCornerShape(99.dp))
          .background(Color(0x2822D3EE))
          .border(1.dp, HairlineCyan, RoundedCornerShape(99.dp))
          .padding(horizontal = 12.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Rounded.Speed,
        contentDescription = null,
        tint = CyanBright,
        modifier = Modifier.size(14.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = stringResource(R.string.edition_badge),
        style = MaterialTheme.typography.labelMedium,
        color = CyanBright,
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "NEON RUSH 3D",
      style =
        TextStyle(
          fontFamily = SpaceGroteskFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 32.sp,
          letterSpacing = 1.6.sp,
          brush = Brush.horizontalGradient(listOf(CyanBright, Color.White, MagentaNeon)),
        ),
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = stringResource(R.string.game_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Career Stats Strip
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      MiniStatTile(
        label = stringResource(R.string.label_best),
        value = hud.bestScore.toString(),
        valueColor = AmberGold,
        modifier = Modifier.weight(1f),
      )
      MiniStatTile(
        label = stringResource(R.string.label_top_speed),
        value = if (hud.allTimeTopSpeed > 0) "${hud.allTimeTopSpeed}" else "--",
        valueColor = CyanBright,
        modifier = Modifier.weight(1f),
      )
      MiniStatTile(
        label = stringResource(R.string.label_races),
        value = hud.totalRaces.toString(),
        valueColor = MagentaNeon,
        modifier = Modifier.weight(1f),
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Supercar Livery Picker
    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = stringResource(R.string.label_garage_livery),
        style = MaterialTheme.typography.labelMedium,
        color = TextMuted,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CarLivery.entries.forEach { livery ->
          val selected = hud.selectedLivery == livery
          Column(
            modifier =
              Modifier.weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) ObsidianSurface else ObsidianDeep)
                .border(
                  width = if (selected) 1.5.dp else 1.dp,
                  color = if (selected) livery.underglowColor else Color(0x3364748B),
                  shape = RoundedCornerShape(14.dp),
                )
                .clickable { onSelectLivery(livery) }
                .testTag("livery_${livery.id}")
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Box(
              modifier =
                Modifier.size(22.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(listOf(livery.highlightColor, livery.primaryColor))
                  )
                  .border(1.5.dp, livery.underglowColor, CircleShape)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
              text = livery.displayName.substringBefore(" "),
              style = MaterialTheme.typography.labelSmall,
              color = if (selected) TextPrimary else TextMuted,
              maxLines = 1,
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(22.dp))

    PrimaryGradientButton(
      text = stringResource(R.string.action_start_race),
      icon = Icons.Rounded.PlayArrow,
      testTag = "start_race_button",
      onClick = onStartRace,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = stringResource(R.string.controls_hint),
      style = MaterialTheme.typography.labelSmall,
      color = TextMuted,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun PauseCard(
  hud: GameHudState,
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onGarage: () -> Unit,
) {
  GlassModalShell {
    Text(
      text = stringResource(R.string.paused_title),
      style = MaterialTheme.typography.headlineMedium,
      color = TextPrimary,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = stringResource(R.string.paused_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(18.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MiniStatTile(
        label = stringResource(R.string.label_score),
        value = hud.score.toString(),
        valueColor = TextPrimary,
        modifier = Modifier.weight(1f),
      )
      MiniStatTile(
        label = "SPEED",
        value = "${hud.speedKmh} KM/H",
        valueColor = CyanBright,
        modifier = Modifier.weight(1f),
      )
    }

    Spacer(modifier = Modifier.height(22.dp))

    PrimaryGradientButton(
      text = stringResource(R.string.action_resume),
      icon = Icons.Rounded.PlayArrow,
      testTag = "resume_button",
      onClick = onResume,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      OutlinedButton(
        onClick = onRestart,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HairlineCyan),
        modifier = Modifier.weight(1f).height(50.dp).testTag("restart_button"),
      ) {
        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.action_restart), style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = onGarage,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HairlineIndigo),
        modifier = Modifier.weight(1f).height(50.dp).testTag("garage_button"),
      ) {
        Icon(
          Icons.Rounded.DirectionsCar,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.action_garage), style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}

@Composable
private fun GameOverDebriefCard(
  hud: GameHudState,
  onRaceAgain: () -> Unit,
  onGarage: () -> Unit,
) {
  GlassModalShell(
    borderBrush =
      Brush.linearGradient(
        if (hud.isNewRecord) listOf(AmberGold, CyanNeon, MagentaNeon)
        else listOf(RoseSunset, HairlineIndigo, CyanNeon)
      )
  ) {
    if (hud.isNewRecord) {
      Row(
        modifier =
          Modifier.clip(RoundedCornerShape(99.dp))
            .background(Color(0x33FBBF24))
            .border(1.dp, AmberGold, RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Rounded.AutoAwesome,
          contentDescription = null,
          tint = AmberGold,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = stringResource(R.string.new_record_badge),
          style = MaterialTheme.typography.labelMedium,
          color = AmberGold,
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    Text(
      text = stringResource(R.string.gameover_title),
      style =
        TextStyle(
          fontFamily = SpaceGroteskFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 26.sp,
          letterSpacing = 1.2.sp,
          brush =
            Brush.horizontalGradient(
              if (hud.isNewRecord) listOf(AmberGold, CyanBright)
              else listOf(RoseSunset, MagentaNeon)
            ),
        ),
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = stringResource(R.string.gameover_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(18.dp))

    // 2x2 Telemetry Debrief Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MiniStatTile(
        label = stringResource(R.string.label_score),
        value = hud.score.toString(),
        valueColor = TextPrimary,
        modifier = Modifier.weight(1f),
      )
      MiniStatTile(
        label = stringResource(R.string.label_best),
        value = hud.bestScore.toString(),
        valueColor = AmberGold,
        modifier = Modifier.weight(1f),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MiniStatTile(
        label = stringResource(R.string.label_top_speed),
        value = "${hud.peakSpeedKmh} KM/H",
        valueColor = CyanBright,
        modifier = Modifier.weight(1f),
      )
      MiniStatTile(
        label = stringResource(R.string.label_orbs),
        value = "${hud.orbsCollected} (${hud.nearMisses} NM)",
        valueColor = MagentaNeon,
        modifier = Modifier.weight(1f),
      )
    }

    Spacer(modifier = Modifier.height(22.dp))

    PrimaryGradientButton(
      text = stringResource(R.string.action_race_again),
      icon = Icons.Rounded.Refresh,
      testTag = "race_again_button",
      onClick = onRaceAgain,
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
      onClick = onGarage,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, HairlineIndigo),
      modifier = Modifier.fillMaxWidth().height(50.dp).testTag("gameover_garage_button"),
    ) {
      Icon(Icons.Rounded.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(stringResource(R.string.action_garage), style = MaterialTheme.typography.labelMedium)
    }
  }
}

@Composable
private fun MiniStatTile(
  label: String,
  value: String,
  valueColor: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(16.dp))
        .background(ObsidianCardSolid)
        .border(1.dp, Color(0x2B94A3B8), RoundedCornerShape(16.dp))
        .padding(horizontal = 10.dp, vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = TextMuted,
      maxLines = 1,
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = value,
      fontFamily = JetBrainsMonoFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 17.sp,
      color = valueColor,
      maxLines = 1,
    )
  }
}

@Composable
private fun PrimaryGradientButton(
  text: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  testTag: String,
  onClick: () -> Unit,
) {
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
    contentPadding = PaddingValues(),
    shape = RoundedCornerShape(18.dp),
    modifier =
      Modifier.fillMaxWidth()
        .height(54.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(
          Brush.horizontalGradient(listOf(IndigoElectric, MagentaNeon, CyanNeon))
        )
        .testTag(testTag),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TextPrimary,
        modifier = Modifier.size(22.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        letterSpacing = 1.4.sp,
      )
    }
  }
}
