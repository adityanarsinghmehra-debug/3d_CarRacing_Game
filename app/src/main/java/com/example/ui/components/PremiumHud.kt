package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun PremiumHud(
  hud: GameHudState,
  onToggleMute: () -> Unit,
  onTogglePause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    // Top-Left: Score & Best Record Telemetry Pod
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = ObsidianCard,
      border =
        BorderStroke(
          width = 1.dp,
          brush = Brush.linearGradient(listOf(HairlineCyan, HairlineIndigo)),
        ),
      tonalElevation = 4.dp,
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(R.string.label_score),
            style = MaterialTheme.typography.labelMedium,
            color = CyanNeon,
          )
          AnimatedVisibility(
            visible = hud.isBoosting && hud.status == GameStatus.PLAYING,
            enter = fadeIn(),
            exit = fadeOut(),
          ) {
            Row(
              modifier =
                Modifier.padding(start = 8.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    Brush.horizontalGradient(listOf(CyanNeon, MagentaNeon))
                  )
                  .padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = ObsidianVoid,
                modifier = Modifier.size(11.dp),
              )
              Text(
                text = "x2",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = ObsidianVoid,
              )
            }
          }
        }

        Text(
          text = hud.score.toString(),
          fontFamily = JetBrainsMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 26.sp,
          color = TextPrimary,
          letterSpacing = 0.5.sp,
          modifier = Modifier.testTag("hud_score_text"),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.EmojiEvents,
            contentDescription = null,
            tint = AmberGold,
            modifier = Modifier.size(13.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${stringResource(R.string.label_best)} ${hud.bestScore}",
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = AmberGold,
          )
        }
      }
    }

    // Top-Right: Audio/Pause Action Pills + Speed & Nitro Telemetry Pod
    Column(
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
          onClick = onToggleMute,
          colors =
            IconButtonDefaults.iconButtonColors(
              containerColor = ObsidianCard,
              contentColor = if (hud.isMuted) TextMuted else CyanBright,
            ),
          modifier =
            Modifier.size(48.dp)
              .border(1.dp, HairlineIndigo, CircleShape)
              .testTag("mute_button"),
        ) {
          Icon(
            imageVector =
              if (hud.isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
            contentDescription = stringResource(R.string.cd_toggle_sound),
            modifier = Modifier.size(20.dp),
          )
        }

        if (hud.status == GameStatus.PLAYING || hud.status == GameStatus.PAUSED) {
          IconButton(
            onClick = onTogglePause,
            colors =
              IconButtonDefaults.iconButtonColors(
                containerColor = ObsidianCard,
                contentColor = TextPrimary,
              ),
            modifier =
              Modifier.size(48.dp)
                .border(1.dp, HairlineCyan, CircleShape)
                .testTag("pause_button"),
          ) {
            Icon(
              imageVector =
                if (hud.status == GameStatus.PAUSED) Icons.Rounded.PlayArrow
                else Icons.Rounded.Pause,
              contentDescription = stringResource(R.string.cd_pause_game),
              modifier = Modifier.size(22.dp),
            )
          }
        }
      }

      AnimatedVisibility(
        visible = hud.status == GameStatus.PLAYING || hud.status == GameStatus.PAUSED,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = ObsidianCard,
          border =
            BorderStroke(
              width = 1.dp,
              color = if (hud.isBoosting) MagentaNeon else HairlineCyan,
            ),
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End,
          ) {
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = hud.speedKmh.toString(),
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (hud.isBoosting) MagentaNeon else CyanBright,
                modifier = Modifier.testTag("hud_speed_text"),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "KM/H",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 2.dp),
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val fraction = (hud.nitroFuel / 100f).coerceIn(0f, 1f)
            Box(
              modifier =
                Modifier.width(104.dp)
                  .height(7.dp)
                  .clip(RoundedCornerShape(99.dp))
                  .background(Color(0xFF1E293B))
            ) {
              Box(
                modifier =
                  Modifier.fillMaxWidth(fraction)
                    .height(7.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                      Brush.horizontalGradient(
                        listOf(CyanNeon, IndigoElectric, MagentaNeon)
                      )
                    )
              )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
              text = "${stringResource(R.string.action_nitro)} ${hud.nitroFuel.toInt()}%",
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.Medium,
              fontSize = 10.sp,
              color = TextMuted,
            )
          }
        }
      }
    }
  }
}
