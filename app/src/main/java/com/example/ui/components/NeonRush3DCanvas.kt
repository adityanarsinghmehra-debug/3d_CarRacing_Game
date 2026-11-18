package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.game.CarLivery
import com.example.game.EnergyOrb
import com.example.game.GameEngine
import com.example.game.GameStatus
import com.example.game.HighwayArch
import com.example.game.SkylineBuilding
import com.example.game.TrafficCar
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MagentaNeon
import com.example.ui.theme.RoseSunset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class ProjectedPoint(val x: Float, val y: Float, val scale: Float)

@Composable
fun NeonRush3DCanvas(
  engine: GameEngine,
  frameTick: Long,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()

  Canvas(modifier = modifier.fillMaxSize()) {
    // Consume frameTick so Compose invalidates draw pass smoothly at 60fps
    @Suppress("UNUSED_EXPRESSION")
    frameTick

    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@Canvas

    val shakeX =
      if (engine.shakeIntensity > 0f) {
        sin(engine.elapsedTime * 68f) * engine.shakeIntensity * 18f
      } else 0f
    val shakeY =
      if (engine.shakeIntensity > 0f) {
        cos(engine.elapsedTime * 53f) * engine.shakeIntensity * 14f
      } else 0f

    val horizonY = h * 0.36f + shakeY
    val cx = w * 0.5f + shakeX
    val camY = 4.25f
    val fovScale = (h * 0.86f) * (1f - engine.fovZoomProgress * 0.14f)

    fun project(wx: Float, wy: Float, wz: Float): ProjectedPoint {
      val safeZ = wz.coerceAtLeast(1.2f)
      val scale = fovScale / safeZ
      return ProjectedPoint(
        x = cx + (wx - engine.cameraX) * scale,
        y = horizonY + (camY - wy) * scale,
        scale = scale,
      )
    }

    // 1. Synthwave Sky & Stars
    drawSkyAndSun(w, h, horizonY, cx - engine.cameraX * 8f, engine.elapsedTime)

    // 2. Distant Neon Mountain Ridge
    drawMountainRidge(w, horizonY, engine.cameraX)

    // 3. Ground Plane & Cyber Grid
    drawGroundAndGrid(w, h, horizonY, ::project, engine.roadScroll)

    // 4. 3D Perspective Highway Asphalt, Neon Rails & Dashed Dividers
    drawHighway(horizonY, ::project, engine.roadScroll)

    // 5. Distant Skyline Buildings (sorted back-to-front)
    val sortedBuildings = engine.buildings.sortedByDescending { it.worldZ }
    for (b in sortedBuildings) {
      drawBuilding(b, ::project, w, h)
    }

    // 6. Interleaved Far-to-Near 3D Entities (Arches, Traffic, Orbs)
    drawSortedTrackEntities(
      arches = engine.arches,
      traffic = engine.trafficCars,
      orbs = engine.orbs,
      project = ::project,
      elapsed = engine.elapsedTime,
    )

    // 7. Player Supercar (at PLAYER_Z)
    drawPlayerSupercar(
      playerX = engine.playerX,
      tiltDeg = engine.playerTilt,
      livery = engine.selectedLivery,
      isBoosting = engine.isBoostingActive,
      crashed = engine.status == GameStatus.GAME_OVER,
      elapsed = engine.elapsedTime,
      project = ::project,
    )

    // 8. 3D Particles
    for (p in engine.particles) {
      val pt = project(p.worldX, p.worldY, p.worldZ)
      val radius = (p.size * pt.scale * 0.032f).coerceIn(2f, 22f)
      drawCircle(
        color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
        radius = radius,
        center = Offset(pt.x, pt.y),
      )
    }

    // 9. Floating Score/Near-Miss Callouts
    drawCallouts(engine, ::project, textMeasurer)

    // 10. Hyper-Speed Warp Streaks on screen edges when Nitro active
    if (engine.isBoostingActive || engine.fovZoomProgress > 0.15f) {
      drawWarpStreaks(w, h, engine.elapsedTime, engine.fovZoomProgress)
    }
  }
}

private fun DrawScope.drawSkyAndSun(
  w: Float,
  h: Float,
  horizonY: Float,
  sunCenterX: Float,
  elapsed: Float,
) {
  drawRect(
    brush =
      Brush.verticalGradient(
        colors = listOf(Color(0xFF04020C), Color(0xFF10082B), Color(0xFF2B0F4A)),
        startY = 0f,
        endY = horizonY + 12f,
      ),
    size = Size(w, horizonY + 12f),
  )

  // Subtle deterministic starfield
  for (i in 0 until 36) {
    val sx = ((i * 193 + 47) % 1000) / 1000f * w
    val sy = ((i * 97 + 19) % 1000) / 1000f * (horizonY * 0.85f)
    val twinkle = 0.25f + 0.55f * (0.5f + 0.5f * sin(elapsed * 2.2f + i))
    drawCircle(
      color = Color.White.copy(alpha = twinkle),
      radius = if (i % 4 == 0) 2.2f else 1.3f,
      center = Offset(sx, sy),
    )
  }

  // Synthwave Horizon Sun + Bloom
  val sunRadius = (minOf(w, h) * 0.16f).coerceIn(58f, 128f)
  val sunY = horizonY - sunRadius * 0.36f
  drawCircle(
    brush =
      Brush.radialGradient(
        colors = listOf(Color(0x66F43F5E), Color(0x22E879F9), Color.Transparent),
        center = Offset(sunCenterX, sunY),
        radius = sunRadius * 2.2f,
      ),
    radius = sunRadius * 2.2f,
    center = Offset(sunCenterX, sunY),
  )

  drawCircle(
    brush =
      Brush.verticalGradient(
        colors = listOf(Color(0xFFFDE047), Color(0xFFFB7185), Color(0xFFE11D48)),
        startY = sunY - sunRadius,
        endY = sunY + sunRadius,
      ),
    radius = sunRadius,
    center = Offset(sunCenterX, sunY),
  )

  // Retro scanline cutouts across lower half of the sun
  for (band in 0 until 5) {
    val bandY = sunY + sunRadius * (0.12f + band * 0.17f)
    val bandH = (sunRadius * (0.04f + band * 0.014f))
    if (bandY < horizonY) {
      drawRect(
        color = Color(0xFF1F0C3B),
        topLeft = Offset(sunCenterX - sunRadius - 4f, bandY),
        size = Size(sunRadius * 2f + 8f, bandH),
      )
    }
  }
}

private fun DrawScope.drawMountainRidge(w: Float, horizonY: Float, cameraX: Float) {
  val ridge = Path()
  val parallax = -cameraX * 5f
  ridge.moveTo(0f, horizonY)
  val peaks =
    listOf(
      0f to 0.04f,
      0.08f to 0.11f,
      0.17f to 0.06f,
      0.26f to 0.14f,
      0.36f to 0.05f,
      0.47f to 0.03f,
      0.55f to 0.03f,
      0.66f to 0.08f,
      0.77f to 0.15f,
      0.88f to 0.07f,
      1.0f to 0.09f,
    )
  for ((px, py) in peaks) {
    ridge.lineTo(px * w + parallax * 0.25f, horizonY - py * horizonY * 0.85f)
  }
  ridge.lineTo(w, horizonY)
  ridge.close()
  drawPath(ridge, color = Color(0xFF120826))

  // Neon ridge top stroke
  val outline = Path()
  peaks.forEachIndexed { idx, (px, py) ->
    val x = px * w + parallax * 0.25f
    val y = horizonY - py * horizonY * 0.85f
    if (idx == 0) outline.moveTo(x, y) else outline.lineTo(x, y)
  }
  drawPath(outline, color = Color(0x66E879F9), style = Stroke(width = 2f))
}

private fun DrawScope.drawGroundAndGrid(
  w: Float,
  h: Float,
  horizonY: Float,
  project: (Float, Float, Float) -> ProjectedPoint,
  roadScroll: Float,
) {
  drawRect(
    brush =
      Brush.verticalGradient(
        colors = listOf(Color(0xFF110726), Color(0xFF070412)),
        startY = horizonY,
        endY = h,
      ),
    topLeft = Offset(0f, horizonY),
    size = Size(w, h - horizonY),
  )

  // Subtle perspective cyber-grid horizontal lines outside road
  val steps = 14
  for (i in 0 until steps) {
    val z = 5f + ((i + (1f - roadScroll) * 2f) % steps) * 10.5f
    val left = project(-48f, 0f, z)
    val right = project(48f, 0f, z)
    val alpha = (1f - z / 155f).coerceIn(0.03f, 0.24f)
    drawLine(
      color = Color(0xFF6366F1).copy(alpha = alpha),
      start = Offset(left.x, left.y),
      end = Offset(right.x, right.y),
      strokeWidth = 1.4f,
    )
  }
}

private fun DrawScope.drawHighway(
  horizonY: Float,
  project: (Float, Float, Float) -> ProjectedPoint,
  roadScroll: Float,
) {
  val nearZ = 2.0f
  val farZ = 165f
  val nL = project(-GameEngine.ROAD_HALF_WIDTH, 0f, nearZ)
  val nR = project(GameEngine.ROAD_HALF_WIDTH, 0f, nearZ)
  val fL = project(-GameEngine.ROAD_HALF_WIDTH, 0f, farZ)
  val fR = project(GameEngine.ROAD_HALF_WIDTH, 0f, farZ)

  // Asphalt trapezoid
  val roadPath =
    Path().apply {
      moveTo(fL.x, fL.y)
      lineTo(fR.x, fR.y)
      lineTo(nR.x, nR.y)
      lineTo(nL.x, nL.y)
      close()
    }
  drawPath(
    path = roadPath,
    brush =
      Brush.verticalGradient(
        colors = listOf(Color(0xFF14172C), Color(0xFF0E1324), Color(0xFF090D1A)),
        startY = horizonY,
        endY = nL.y,
      ),
  )

  // Neon outer edge rails (Cyan left, Magenta right) with glow pass
  drawLine(CyanNeon.copy(alpha = 0.28f), Offset(fL.x, fL.y), Offset(nL.x, nL.y), strokeWidth = 14f)
  drawLine(CyanNeon, Offset(fL.x, fL.y), Offset(nL.x, nL.y), strokeWidth = 4.2f)

  drawLine(
    MagentaNeon.copy(alpha = 0.28f),
    Offset(fR.x, fR.y),
    Offset(nR.x, nR.y),
    strokeWidth = 14f,
  )
  drawLine(MagentaNeon, Offset(fR.x, fR.y), Offset(nR.x, nR.y), strokeWidth = 4.2f)

  // Dashed lane dividers in 3D perspective
  val dashSpacing = 12f
  val dashLength = 5.4f
  val offset = (1f - roadScroll) * dashSpacing
  var z = 2.5f + offset
  while (z < 150f) {
    val zEnd = (z + dashLength).coerceAtMost(152f)
    val alpha = (1f - z / 155f).coerceIn(0.12f, 0.88f)
    for (divX in listOf(-1.6f, 1.6f)) {
      val p1 = project(divX, 0f, z)
      val p2 = project(divX, 0f, zEnd)
      val strokeW = (p1.scale * 0.11f).coerceIn(1.5f, 7f)
      drawLine(
        color = Color(0xFFE2E8F0).copy(alpha = alpha),
        start = Offset(p1.x, p1.y),
        end = Offset(p2.x, p2.y),
        strokeWidth = strokeW,
        cap = StrokeCap.Round,
      )
    }
    z += dashSpacing
  }
}

private fun DrawScope.drawBuilding(
  b: SkylineBuilding,
  project: (Float, Float, Float) -> ProjectedPoint,
  screenW: Float,
  screenH: Float,
) {
  val innerX = b.side * b.offsetX
  val outerX = b.side * (b.offsetX + b.width)
  val baseInner = project(innerX, 0f, b.worldZ)
  val topInner = project(innerX, b.height, b.worldZ)
  val baseOuter = project(outerX, 0f, b.worldZ)
  val topOuter = project(outerX, b.height, b.worldZ)

  val minX = minOf(baseInner.x, baseOuter.x)
  val maxX = maxOf(baseInner.x, baseOuter.x)
  if (maxX < -60f || minX > screenW + 60f) return
  val topY = topInner.y
  val bottomY = baseInner.y.coerceAtMost(screenH + 20f)
  val boxW = (maxX - minX).coerceAtLeast(3f)
  val boxH = (bottomY - topY).coerceAtLeast(4f)

  val fogFade = (1f - (b.worldZ / 185f)).coerceIn(0.18f, 0.92f)
  drawRect(
    color = Color(0xFF13113A).copy(alpha = fogFade),
    topLeft = Offset(minX, topY),
    size = Size(boxW, boxH),
  )

  // Glowing inner edge neon strip
  drawLine(
    color = b.windowColor.copy(alpha = fogFade * 0.65f),
    start = Offset(baseInner.x, topY),
    end = Offset(baseInner.x, bottomY),
    strokeWidth = (baseInner.scale * 0.14f).coerceIn(1f, 3.5f),
  )

  // Lit horizontal window bands
  if (boxW > 10f && boxH > 18f) {
    val rows = (boxH / 16f).toInt().coerceIn(2, 8)
    for (r in 1 until rows) {
      val wy = topY + (boxH * r / rows)
      drawLine(
        color = b.windowColor.copy(alpha = fogFade * 0.30f),
        start = Offset(minX + boxW * 0.18f, wy),
        end = Offset(maxX - boxW * 0.18f, wy),
        strokeWidth = 2f,
      )
    }
  }
}

private sealed interface TrackRenderItem {
  val z: Float

  data class ArchItem(val arch: HighwayArch) : TrackRenderItem {
    override val z: Float get() = arch.worldZ
  }

  data class TrafficItem(val car: TrafficCar) : TrackRenderItem {
    override val z: Float get() = car.worldZ
  }

  data class OrbItem(val orb: EnergyOrb) : TrackRenderItem {
    override val z: Float get() = orb.worldZ
  }
}

private fun DrawScope.drawSortedTrackEntities(
  arches: List<HighwayArch>,
  traffic: List<TrafficCar>,
  orbs: List<EnergyOrb>,
  project: (Float, Float, Float) -> ProjectedPoint,
  elapsed: Float,
) {
  val items = ArrayList<TrackRenderItem>(arches.size + traffic.size + orbs.size)
  arches.forEach { items.add(TrackRenderItem.ArchItem(it)) }
  traffic.forEach { items.add(TrackRenderItem.TrafficItem(it)) }
  orbs.forEach { items.add(TrackRenderItem.OrbItem(it)) }
  items.sortByDescending { it.z }

  for (item in items) {
    when (item) {
      is TrackRenderItem.ArchItem -> drawArch(item.arch, project)
      is TrackRenderItem.TrafficItem -> drawTrafficCar(item.car, project)
      is TrackRenderItem.OrbItem -> drawEnergyOrb(item.orb, project, elapsed)
    }
  }
}

private fun DrawScope.drawArch(
  arch: HighwayArch,
  project: (Float, Float, Float) -> ProjectedPoint,
) {
  val archX = 5.95f
  val archH = 5.6f
  val bL = project(-archX, 0f, arch.worldZ)
  val tL = project(-archX, archH, arch.worldZ)
  val tR = project(archX, archH, arch.worldZ)
  val bR = project(archX, 0f, arch.worldZ)

  val color = if (arch.isCyan) CyanNeon else MagentaNeon
  val alpha = (1f - arch.worldZ / 185f).coerceIn(0.12f, 0.92f)
  val strokeW = (bL.scale * 0.22f).coerceIn(2f, 11f)

  val path =
    Path().apply {
      moveTo(bL.x, bL.y)
      lineTo(tL.x, tL.y)
      lineTo(tR.x, tR.y)
      lineTo(bR.x, bR.y)
    }

  drawPath(path, color = color.copy(alpha = alpha * 0.25f), style = Stroke(width = strokeW * 2.4f))
  drawPath(path, color = color.copy(alpha = alpha), style = Stroke(width = strokeW))
}

private fun DrawScope.drawTrafficCar(
  car: TrafficCar,
  project: (Float, Float, Float) -> ProjectedPoint,
) {
  val base = project(car.worldX, 0f, car.worldZ)
  val carW = (base.scale * 1.85f).coerceAtLeast(8f)
  val carH = (base.scale * 0.62f).coerceAtLeast(4f)
  val cabW = carW * 0.78f
  val cabH = carH * 0.72f

  val left = base.x - carW * 0.5f
  val bottom = base.y

  // Ground shadow
  drawOval(
    color = Color.Black.copy(alpha = 0.55f),
    topLeft = Offset(left - carW * 0.08f, bottom - carH * 0.16f),
    size = Size(carW * 1.16f, carH * 0.34f),
  )

  // Tires
  val tireW = carW * 0.18f
  val tireH = carH * 0.42f
  drawRoundRect(
    color = Color(0xFF0F172A),
    topLeft = Offset(left + carW * 0.04f, bottom - tireH),
    size = Size(tireW, tireH),
    cornerRadius = CornerRadius(4f, 4f),
  )
  drawRoundRect(
    color = Color(0xFF0F172A),
    topLeft = Offset(left + carW - tireW - carW * 0.04f, bottom - tireH),
    size = Size(tireW, tireH),
    cornerRadius = CornerRadius(4f, 4f),
  )

  // Upper cabin & windshield
  val bodyTop = bottom - carH - tireH * 0.32f
  val cabLeft = base.x - cabW * 0.5f
  val cabPath =
    Path().apply {
      moveTo(cabLeft + cabW * 0.12f, bodyTop - cabH)
      lineTo(cabLeft + cabW * 0.88f, bodyTop - cabH)
      lineTo(cabLeft + cabW, bodyTop + 2f)
      lineTo(cabLeft, bodyTop + 2f)
      close()
    }
  drawPath(cabPath, color = Color(0xFF0F172A))

  // Main chassis
  drawRoundRect(
    brush =
      Brush.verticalGradient(
        colors = listOf(car.accentColor, car.bodyColor),
        startY = bodyTop,
        endY = bodyTop + carH,
      ),
    topLeft = Offset(left, bodyTop),
    size = Size(carW, carH),
    cornerRadius = CornerRadius(carW * 0.12f, carW * 0.12f),
  )

  // Glowing taillights
  val lightW = carW * 0.22f
  val lightH = (carH * 0.25f).coerceAtLeast(2f)
  val lightY = bodyTop + carH * 0.38f
  drawRoundRect(
    color = RoseSunset,
    topLeft = Offset(left + carW * 0.09f, lightY),
    size = Size(lightW, lightH),
    cornerRadius = CornerRadius(3f, 3f),
  )
  drawRoundRect(
    color = RoseSunset,
    topLeft = Offset(left + carW - lightW - carW * 0.09f, lightY),
    size = Size(lightW, lightH),
    cornerRadius = CornerRadius(3f, 3f),
  )
}

private fun DrawScope.drawEnergyOrb(
  orb: EnergyOrb,
  project: (Float, Float, Float) -> ProjectedPoint,
  elapsed: Float,
) {
  val bobY = 0.92f + sin(elapsed * 4.5f + orb.phase) * 0.16f
  val pt = project(orb.worldX, bobY, orb.worldZ)
  val ground = project(orb.worldX, 0f, orb.worldZ)
  val r = (pt.scale * 0.58f).coerceIn(4f, 44f)

  // Ground neon reflection
  drawOval(
    brush =
      Brush.radialGradient(
        colors = listOf(CyanNeon.copy(alpha = 0.55f), Color.Transparent),
        center = Offset(ground.x, ground.y),
        radius = r * 1.4f,
      ),
    topLeft = Offset(ground.x - r * 1.4f, ground.y - r * 0.45f),
    size = Size(r * 2.8f, r * 0.9f),
  )

  // Outer aura
  drawCircle(
    brush =
      Brush.radialGradient(
        colors = listOf(CyanNeon.copy(alpha = 0.65f), Color.Transparent),
        center = Offset(pt.x, pt.y),
        radius = r * 2.1f,
      ),
    radius = r * 2.1f,
    center = Offset(pt.x, pt.y),
  )

  // Rotating faceted diamond
  val rotDeg = (orb.phase * 180f / PI.toFloat()) % 360f
  rotate(degrees = rotDeg, pivot = Offset(pt.x, pt.y)) {
    val diamond =
      Path().apply {
        moveTo(pt.x, pt.y - r)
        lineTo(pt.x + r * 0.82f, pt.y)
        lineTo(pt.x, pt.y + r)
        lineTo(pt.x - r * 0.82f, pt.y)
        close()
      }
    drawPath(diamond, color = CyanNeon)
    drawPath(diamond, color = Color.White.copy(alpha = 0.85f), style = Stroke(width = (r * 0.16f).coerceAtLeast(1.5f)))
  }
}

private fun DrawScope.drawPlayerSupercar(
  playerX: Float,
  tiltDeg: Float,
  livery: CarLivery,
  isBoosting: Boolean,
  crashed: Boolean,
  elapsed: Float,
  project: (Float, Float, Float) -> ProjectedPoint,
) {
  val base = project(playerX, 0f, GameEngine.PLAYER_Z)
  val farAhead = project(playerX, 0f, GameEngine.PLAYER_Z + 14f)

  val carW = (base.scale * 2.05f).coerceAtLeast(32f)
  val carH = (base.scale * 0.66f).coerceAtLeast(14f)

  // Headlight beams projected onto asphalt ahead
  val beamLeft =
    Path().apply {
      moveTo(base.x - carW * 0.34f, base.y - carH * 0.4f)
      lineTo(farAhead.x - farAhead.scale * 1.55f, farAhead.y)
      lineTo(farAhead.x + farAhead.scale * 1.55f, farAhead.y)
      lineTo(base.x + carW * 0.34f, base.y - carH * 0.4f)
      close()
    }
  drawPath(
    beamLeft,
    brush =
      Brush.verticalGradient(
        colors = listOf(Color.Transparent, livery.underglowColor.copy(alpha = 0.22f)),
        startY = farAhead.y,
        endY = base.y,
      ),
  )

  // Underglow halo on road
  drawOval(
    brush =
      Brush.radialGradient(
        colors = listOf(livery.underglowColor.copy(alpha = 0.75f), Color.Transparent),
        center = Offset(base.x, base.y + carH * 0.05f),
        radius = carW * 0.92f,
      ),
    topLeft = Offset(base.x - carW * 0.85f, base.y - carH * 0.28f),
    size = Size(carW * 1.7f, carH * 0.68f),
  )

  val pivot = Offset(base.x, base.y - carH * 0.45f)
  rotate(degrees = tiltDeg, pivot = pivot) {
    val left = base.x - carW * 0.5f
    val bottom = base.y
    val tireW = carW * 0.19f
    val tireH = carH * 0.44f

    // Rear tires
    drawRoundRect(
      color = Color(0xFF0B0F1A),
      topLeft = Offset(left + carW * 0.02f, bottom - tireH),
      size = Size(tireW, tireH),
      cornerRadius = CornerRadius(6f, 6f),
    )
    drawRoundRect(
      color = Color(0xFF0B0F1A),
      topLeft = Offset(left + carW - tireW - carW * 0.02f, bottom - tireH),
      size = Size(tireW, tireH),
      cornerRadius = CornerRadius(6f, 6f),
    )

    val bodyTop = bottom - carH - tireH * 0.34f

    // Sleek dark glass cabin
    val cabW = carW * 0.72f
    val cabH = carH * 0.68f
    val cabLeft = base.x - cabW * 0.5f
    val cabinPath =
      Path().apply {
        moveTo(cabLeft + cabW * 0.18f, bodyTop - cabH)
        lineTo(cabLeft + cabW * 0.82f, bodyTop - cabH)
        lineTo(cabLeft + cabW, bodyTop + 3f)
        lineTo(cabLeft, bodyTop + 3f)
        close()
      }
    drawPath(
      cabinPath,
      brush =
        Brush.verticalGradient(
          colors = listOf(Color(0xFF1E293B), Color(0xFF090D18)),
          startY = bodyTop - cabH,
          endY = bodyTop,
        ),
    )
    drawPath(
      cabinPath,
      color = livery.highlightColor.copy(alpha = 0.55f),
      style = Stroke(width = 2.2f),
    )

    // Wide supercar rear chassis
    val chassisPath =
      Path().apply {
        moveTo(left + carW * 0.08f, bodyTop)
        lineTo(left + carW * 0.92f, bodyTop)
        lineTo(left + carW, bodyTop + carH * 0.65f)
        lineTo(left + carW * 0.95f, bodyTop + carH)
        lineTo(left + carW * 0.05f, bodyTop + carH)
        lineTo(left, bodyTop + carH * 0.65f)
        close()
      }
    drawPath(
      chassisPath,
      brush =
        Brush.verticalGradient(
          colors = listOf(livery.highlightColor, livery.primaryColor),
          startY = bodyTop,
          endY = bodyTop + carH,
        ),
    )

    // Rear GT Wing Spoiler
    val wingY = bodyTop - cabH * 0.36f
    val wingW = carW * 0.98f
    val wingLeft = base.x - wingW * 0.5f
    // Wing struts
    drawLine(
      color = Color(0xFF0F172A),
      start = Offset(base.x - carW * 0.24f, bodyTop),
      end = Offset(base.x - carW * 0.26f, wingY),
      strokeWidth = 5f,
    )
    drawLine(
      color = Color(0xFF0F172A),
      start = Offset(base.x + carW * 0.24f, bodyTop),
      end = Offset(base.x + carW * 0.26f, wingY),
      strokeWidth = 5f,
    )
    drawRoundRect(
      color = livery.spoilerColor,
      topLeft = Offset(wingLeft, wingY - 4f),
      size = Size(wingW, (carH * 0.16f).coerceAtLeast(6f)),
      cornerRadius = CornerRadius(4f, 4f),
    )

    // LED Taillight bar
    val tailW = carW * 0.24f
    val tailH = (carH * 0.22f).coerceAtLeast(4f)
    val tailY = bodyTop + carH * 0.36f
    val tailColor = if (crashed) Color(0xFFFBBF24) else RoseSunset
    drawRoundRect(
      color = tailColor,
      topLeft = Offset(left + carW * 0.09f, tailY),
      size = Size(tailW, tailH),
      cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
      color = tailColor,
      topLeft = Offset(left + carW - tailW - carW * 0.09f, tailY),
      size = Size(tailW, tailH),
      cornerRadius = CornerRadius(4f, 4f),
    )

    // Twin Nitro Exhaust Jet Flames
    if (isBoosting && !crashed) {
      val flicker = 0.85f + 0.35f * sin(elapsed * 45f)
      val flameH = carH * 1.15f * flicker
      val flameY = bodyTop + carH * 0.72f
      for (sideOffset in listOf(-carW * 0.25f, carW * 0.25f)) {
        val fx = base.x + sideOffset
        val outerFlame =
          Path().apply {
            moveTo(fx - carW * 0.08f, flameY)
            lineTo(fx + carW * 0.08f, flameY)
            lineTo(fx, flameY + flameH)
            close()
          }
        val innerFlame =
          Path().apply {
            moveTo(fx - carW * 0.04f, flameY)
            lineTo(fx + carW * 0.04f, flameY)
            lineTo(fx, flameY + flameH * 0.62f)
            close()
          }
        drawPath(outerFlame, color = CyanNeon.copy(alpha = 0.9f))
        drawPath(innerFlame, color = Color.White)
      }
    }
  }
}

private fun DrawScope.drawCallouts(
  engine: GameEngine,
  project: (Float, Float, Float) -> ProjectedPoint,
  textMeasurer: TextMeasurer,
) {
  for (c in engine.callouts) {
    val pt = project(c.worldX, 2.2f + (1f - c.life) * 1.8f, GameEngine.PLAYER_Z)
    val style =
      TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = c.color.copy(alpha = c.life.coerceIn(0f, 1f)),
      )
    val result = textMeasurer.measure(c.text, style)
    drawText(
      textLayoutResult = result,
      topLeft = Offset(pt.x - result.size.width * 0.5f, pt.y - result.size.height * 0.5f),
    )
  }
}

private fun DrawScope.drawWarpStreaks(
  w: Float,
  h: Float,
  elapsed: Float,
  intensity: Float,
) {
  val cx = w * 0.5f
  val cy = h * 0.36f
  val streakCount = 16
  for (i in 0 until streakCount) {
    val angle = (i * (2f * PI.toFloat() / streakCount)) + 0.15f
    val phase = ((elapsed * 3.8f + i * 0.27f) % 1f)
    val rStart = minOf(w, h) * (0.28f + phase * 0.35f)
    val rEnd = rStart + minOf(w, h) * 0.16f
    drawLine(
      color = (if (i % 2 == 0) CyanNeon else MagentaNeon).copy(alpha = 0.36f * intensity),
      start = Offset(cx + cos(angle) * rStart, cy + sin(angle) * rStart),
      end = Offset(cx + cos(angle) * rEnd, cy + sin(angle) * rEnd),
      strokeWidth = 2.4f,
      cap = StrokeCap.Round,
    )
  }
}
