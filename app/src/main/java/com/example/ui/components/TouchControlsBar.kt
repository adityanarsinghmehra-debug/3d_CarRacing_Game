package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.MagentaNeon
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardSolid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TouchControlsBar(
  nitroFuel: Float,
  isBoosting: Boolean,
  onSteerLeft: () -> Unit,
  onSteerRight: () -> Unit,
  onNitroChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 18.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left & Right Steering Pads
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      SteerPadButton(
        icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
        contentDesc = stringResource(R.string.cd_steer_left),
        testTag = "steer_left_button",
        onTrigger = onSteerLeft,
      )
      SteerPadButton(
        icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDesc = stringResource(R.string.cd_steer_right),
        testTag = "steer_right_button",
        onTrigger = onSteerRight,
      )
    }

    // Hold-to-Boost Nitro Button
    val hasFuel = nitroFuel > 2f
    val boostScale by
      animateFloatAsState(
        targetValue = if (isBoosting) 0.94f else 1f,
        animationSpec = spring(),
        label = "nitroScale",
      )
    val nitroDesc = stringResource(R.string.cd_nitro_boost)
    val brush =
      if (hasFuel) {
        Brush.horizontalGradient(
          if (isBoosting) listOf(MagentaNeon, CyanNeon)
          else listOf(IndigoElectric, CyanNeon)
        )
      } else {
        Brush.horizontalGradient(listOf(ObsidianCardSolid, ObsidianCardSolid))
      }

    Box(
      modifier =
        Modifier.scale(boostScale)
          .height(66.dp)
          .widthIn(min = 132.dp)
          .clip(RoundedCornerShape(22.dp))
          .background(brush)
          .border(
            width = if (isBoosting) 2.dp else 1.2.dp,
            color = if (hasFuel) Color.White.copy(alpha = 0.45f) else Color(0x3394A3B8),
            shape = RoundedCornerShape(22.dp),
          )
          .semantics {
            role = Role.Button
            contentDescription = nitroDesc
          }
          .testTag("nitro_button")
          .pointerInput(hasFuel) {
            detectTapGestures(
              onPress = {
                if (hasFuel) {
                  onNitroChange(true)
                  tryAwaitRelease()
                  onNitroChange(false)
                }
              }
            )
          }
          .padding(horizontal = 22.dp),
      contentAlignment = Alignment.Center,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Rounded.Bolt,
          contentDescription = null,
          tint = if (hasFuel) TextPrimary else TextMuted,
          modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = stringResource(R.string.action_nitro),
          style = MaterialTheme.typography.titleMedium,
          color = if (hasFuel) TextPrimary else TextMuted,
          letterSpacing = 1.4.sp,
        )
      }
    }
  }
}

@Composable
private fun SteerPadButton(
  icon: ImageVector,
  contentDesc: String,
  testTag: String,
  onTrigger: () -> Unit,
) {
  var pressed by remember { mutableStateOf(false) }
  val scale by
    animateFloatAsState(
      targetValue = if (pressed) 0.92f else 1f,
      animationSpec = spring(),
      label = "steerScale",
    )
  val borderColor by
    animateColorAsState(
      targetValue = if (pressed) CyanNeon else Color(0x666366F1),
      label = "steerBorder",
    )
  val bgColor by
    animateColorAsState(
      targetValue = if (pressed) Color(0x994F46E5) else ObsidianCard,
      label = "steerBg",
    )

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = bgColor,
    border = BorderStroke(1.5.dp, borderColor),
    tonalElevation = 6.dp,
    modifier =
      Modifier.scale(scale)
        .size(66.dp)
        .semantics {
          role = Role.Button
          contentDescription = contentDesc
        }
        .testTag(testTag)
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = {
              pressed = true
              onTrigger()
              tryAwaitRelease()
              pressed = false
            }
          )
        },
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TextPrimary,
        modifier = Modifier.size(34.dp),
      )
    }
  }
}
