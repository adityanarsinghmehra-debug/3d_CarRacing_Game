package com.example.game

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
  initialBestScore: Int,
  initialTopSpeed: Int,
  initialTotalRaces: Int,
  initialMuted: Boolean,
  initialLivery: CarLivery,
) {
  companion object {
    val LANE_X = floatArrayOf(-3.2f, 0f, 3.2f)
    const val PLAYER_Z = 9.5f
    const val ROAD_HALF_WIDTH = 5.5f
    const val ARCH_SPACING = 24f
    const val ARCH_COUNT = 8
    const val BUILDING_COUNT = 26
    const val FAR_Z = 168f
    const val NEAR_CULL_Z = 2.5f

    private val TRAFFIC_PALETTE =
      listOf(
        Color(0xFFE11D48) to Color(0xFFFB7185),
        Color(0xFFF59E0B) to Color(0xFFFCD34D),
        Color(0xFF10B981) to Color(0xFF34D399),
        Color(0xFFEC4899) to Color(0xFFF472B6),
        Color(0xFF0EA5E9) to Color(0xFF38BDF8),
      )
  }

  private val random = Random(System.currentTimeMillis())
  private var nextEntityId = 1L

  var status: GameStatus = GameStatus.MENU
    private set

  var selectedLivery: CarLivery = initialLivery
  var isMuted: Boolean = initialMuted

  var targetLane: Int = 1
    private set

  var playerX: Float = 0f
    private set

  var playerTilt: Float = 0f
    private set

  var cameraX: Float = 0f
    private set

  var fovZoomProgress: Float = 0f
    private set

  var roadScroll: Float = 0f
    private set

  var score: Float = 0f
    private set

  var bestScore: Int = initialBestScore
    private set

  var allTimeTopSpeed: Int = initialTopSpeed
    private set

  var totalRaces: Int = initialTotalRaces
    private set

  var peakSpeedKmh: Int = 0
    private set

  var baseSpeed: Float = 0.62f
    private set

  var nitroFuel: Float = 100f
    private set

  var isBoostingRequested: Boolean = false
  var isBoostingActive: Boolean = false
    private set

  var orbsCollected: Int = 0
    private set

  var nearMisses: Int = 0
    private set

  var comboMultiplier: Int = 1
    private set

  var isNewRecord: Boolean = false
    private set

  var shakeIntensity: Float = 0f
    private set

  var elapsedTime: Float = 0f
    private set

  val trafficCars = mutableListOf<TrafficCar>()
  val orbs = mutableListOf<EnergyOrb>()
  val arches = mutableListOf<HighwayArch>()
  val buildings = mutableListOf<SkylineBuilding>()
  val particles = mutableListOf<Particle>()
  val callouts = mutableListOf<FloatingCallout>()

  private var menuAutopilotTimer = 0f

  init {
    seedEnvironment()
  }

  private fun seedEnvironment() {
    arches.clear()
    for (i in 0 until ARCH_COUNT) {
      arches.add(HighwayArch(worldZ = 16f + i * ARCH_SPACING, isCyan = i % 2 == 0))
    }

    buildings.clear()
    val windowColors = listOf(Color(0xFF22D3EE), Color(0xFFE879F9), Color(0xFF818CF8))
    for (i in 0 until BUILDING_COUNT) {
      val side = if (i % 2 == 0) -1 else 1
      val w = 4.2f + random.nextFloat() * 4.5f
      val h = 10f + random.nextFloat() * 26f
      val d = 5f + random.nextFloat() * 4f
      val offsetX = 11f + random.nextFloat() * 14f
      buildings.add(
        SkylineBuilding(
          side = side,
          offsetX = offsetX,
          worldZ = 14f + i * 6.2f,
          width = w,
          height = h,
          depth = d,
          windowColor = windowColors[i % windowColors.size],
        )
      )
    }
  }

  fun startRace() {
    trafficCars.clear()
    orbs.clear()
    particles.clear()
    callouts.clear()
    score = 0f
    baseSpeed = 0.62f
    nitroFuel = 100f
    isBoostingRequested = false
    isBoostingActive = false
    targetLane = 1
    playerX = 0f
    playerTilt = 0f
    cameraX = 0f
    fovZoomProgress = 0f
    orbsCollected = 0
    nearMisses = 0
    comboMultiplier = 1
    peakSpeedKmh = 0
    isNewRecord = false
    shakeIntensity = 0f
    status = GameStatus.PLAYING
  }

  fun pauseRace() {
    if (status == GameStatus.PLAYING) {
      status = GameStatus.PAUSED
      isBoostingRequested = false
      isBoostingActive = false
    }
  }

  fun resumeRace() {
    if (status == GameStatus.PAUSED) {
      status = GameStatus.PLAYING
    }
  }

  fun returnToMenu() {
    trafficCars.clear()
    orbs.clear()
    particles.clear()
    callouts.clear()
    isBoostingRequested = false
    isBoostingActive = false
    targetLane = 1
    playerX = 0f
    playerTilt = 0f
    shakeIntensity = 0f
    status = GameStatus.MENU
  }

  fun steer(dir: Int): Boolean {
    if (status != GameStatus.PLAYING) return false
    val next = (targetLane + dir).coerceIn(0, 2)
    if (next == targetLane) return false
    targetLane = next
    return true
  }

  data class StepEvents(
    val orbPickedUp: Boolean = false,
    val nearMissTriggered: Boolean = false,
    val crashed: Boolean = false,
    val nitroIgnited: Boolean = false,
  )

  fun step(deltaSeconds: Float): StepEvents {
    val dt = (deltaSeconds * 60f).coerceIn(0.15f, 2.5f)
    elapsedTime += deltaSeconds.coerceIn(0.001f, 0.05f)

    if (shakeIntensity > 0f) {
      shakeIntensity = (shakeIntensity - 0.045f * dt).coerceAtLeast(0f)
    }

    updateParticlesAndCallouts(dt)

    if (status == GameStatus.PAUSED) {
      return StepEvents()
    }

    if (status == GameStatus.MENU) {
      // Smooth autopilot cruise for animated background behind Start/Garage card
      menuAutopilotTimer += deltaSeconds
      if (menuAutopilotTimer > 2.8f) {
        menuAutopilotTimer = 0f
        targetLane = (targetLane + 1) % 3
      }
      val menuSpeed = 0.54f * dt
      updatePlayerKinematics(dt, boosting = false)
      scrollEnvironment(menuSpeed)
      return StepEvents()
    }

    if (status == GameStatus.GAME_OVER) {
      // Gentle coast-down background after crash
      scrollEnvironment(0.08f * dt)
      return StepEvents()
    }

    // --- PLAYING STATE ---
    val wasBoosting = isBoostingActive
    isBoostingActive = isBoostingRequested && nitroFuel > 1.2f
    val nitroIgnited = !wasBoosting && isBoostingActive

    if (isBoostingActive) {
      nitroFuel = (nitroFuel - 0.82f * dt).coerceAtLeast(0f)
      if (nitroFuel <= 0.5f) {
        isBoostingActive = false
      }
    } else {
      nitroFuel = (nitroFuel + 0.16f * dt).coerceAtMost(100f)
    }

    val boostExtra = if (isBoostingActive) 0.48f else 0f
    val currentSpeed = (baseSpeed + boostExtra) * dt
    baseSpeed = (baseSpeed + 0.00032f * dt).coerceAtMost(1.32f)

    comboMultiplier = if (isBoostingActive) 2 else 1
    score += currentSpeed * 3.6f * comboMultiplier

    val kmh = ((baseSpeed + boostExtra) * 225f).toInt()
    if (kmh > peakSpeedKmh) peakSpeedKmh = kmh

    updatePlayerKinematics(dt, isBoostingActive)
    scrollEnvironment(currentSpeed)

    // Spawn traffic & energy orbs fairly
    if (random.nextFloat() < 0.028f * dt && trafficCars.size < 7) {
      trySpawnTraffic()
    }
    if (random.nextFloat() < 0.020f * dt && orbs.size < 4) {
      trySpawnOrb()
    }

    var orbPickedUp = false
    var nearMissTriggered = false

    // Move traffic & detect collisions / near misses
    val trafficIter = trafficCars.listIterator()
    while (trafficIter.hasNext()) {
      val tc = trafficIter.next()
      tc.worldZ -= (currentSpeed - tc.speedFactor * dt)

      val dz = abs(tc.worldZ - PLAYER_Z)
      val dx = abs(tc.worldX - playerX)

      if (dz < 2.85f && dx < 1.38f) {
        // CRASH!
        triggerCrash()
        return StepEvents(crashed = true)
      }

      if (!tc.nearMissAwarded && dz < 3.4f && dx in 1.42f..3.65f && tc.worldZ < PLAYER_Z + 1.5f) {
        tc.nearMissAwarded = true
        nearMisses++
        val bonus = 25 * comboMultiplier
        score += bonus
        nearMissTriggered = true
        spawnBurst(
          x = (playerX + tc.worldX) * 0.5f,
          y = 0.7f,
          z = PLAYER_Z,
          color = Color(0xFFFBBF24),
          count = 10,
        )
        callouts.add(
          FloatingCallout(
            id = nextEntityId++,
            text = "NEAR MISS +$bonus",
            worldX = playerX,
            life = 1.0f,
            color = Color(0xFFFBBF24),
          )
        )
      }

      if (tc.worldZ < NEAR_CULL_Z) {
        trafficIter.remove()
      }
    }

    // Move & collect orbs
    val orbIter = orbs.listIterator()
    while (orbIter.hasNext()) {
      val orb = orbIter.next()
      orb.worldZ -= currentSpeed
      orb.phase += 0.09f * dt

      if (abs(orb.worldZ - PLAYER_Z) < 2.2f && abs(orb.worldX - playerX) < 1.45f) {
        val pts = 50 * comboMultiplier
        score += pts
        orbsCollected++
        nitroFuel = (nitroFuel + 26f).coerceAtMost(100f)
        orbPickedUp = true
        spawnBurst(
          x = orb.worldX,
          y = 0.95f,
          z = PLAYER_Z,
          color = Color(0xFF22D3EE),
          count = 14,
        )
        callouts.add(
          FloatingCallout(
            id = nextEntityId++,
            text = "+$pts ORB",
            worldX = orb.worldX,
            life = 1.0f,
            color = Color(0xFF22D3EE),
          )
        )
        orbIter.remove()
        continue
      }

      if (orb.worldZ < NEAR_CULL_Z) {
        orbIter.remove()
      }
    }

    return StepEvents(
      orbPickedUp = orbPickedUp,
      nearMissTriggered = nearMissTriggered,
      nitroIgnited = nitroIgnited,
    )
  }

  private fun updatePlayerKinematics(dt: Float, boosting: Boolean) {
    val destX = LANE_X[targetLane]
    val diffX = destX - playerX
    playerX += diffX * 0.17f * dt
    playerTilt = (-diffX * 4.5f).coerceIn(-16f, 16f)
    cameraX += (playerX * 0.36f - cameraX) * 0.12f * dt

    val targetZoom = if (boosting) 1f else 0f
    fovZoomProgress += (targetZoom - fovZoomProgress) * 0.10f * dt
  }

  private fun scrollEnvironment(speed: Float) {
    roadScroll = (roadScroll + speed * 0.14f) % 1f

    for (arch in arches) {
      arch.worldZ -= speed
      if (arch.worldZ < NEAR_CULL_Z) {
        arch.worldZ += ARCH_COUNT * ARCH_SPACING
      }
    }

    for (b in buildings) {
      b.worldZ -= speed
      if (b.worldZ < NEAR_CULL_Z) {
        b.worldZ += BUILDING_COUNT * 6.2f
      }
    }
  }

  private fun trySpawnTraffic() {
    val spawnZ = 152f + random.nextFloat() * 24f
    val candidateLanes = (0..2).shuffled(random)
    for (lane in candidateLanes) {
      // Ensure same lane isn't immediately occupied near spawn
      val sameLaneOccupied =
        trafficCars.any { it.laneIndex == lane && abs(it.worldZ - spawnZ) < 28f }
      if (sameLaneOccupied) continue

      // Never create an impassable 3-car wall across all 3 lanes
      val otherLanesBlocked =
        (0..2)
          .filter { it != lane }
          .count { other -> trafficCars.any { it.laneIndex == other && abs(it.worldZ - spawnZ) < 20f } }
      if (otherLanesBlocked >= 2) continue

      val (body, accent) = TRAFFIC_PALETTE[random.nextInt(TRAFFIC_PALETTE.size)]
      trafficCars.add(
        TrafficCar(
          id = nextEntityId++,
          laneIndex = lane,
          worldX = LANE_X[lane],
          worldZ = spawnZ,
          speedFactor = 0.17f + random.nextFloat() * 0.15f,
          bodyColor = body,
          accentColor = accent,
        )
      )
      return
    }
  }

  private fun trySpawnOrb() {
    val spawnZ = 148f + random.nextFloat() * 14f
    val lane = random.nextInt(3)
    val overlapsTraffic = trafficCars.any { it.laneIndex == lane && abs(it.worldZ - spawnZ) < 16f }
    if (overlapsTraffic) return

    orbs.add(
      EnergyOrb(
        id = nextEntityId++,
        laneIndex = lane,
        worldX = LANE_X[lane],
        worldZ = spawnZ,
        phase = random.nextFloat() * (2f * PI.toFloat()),
      )
    )
  }

  private fun spawnBurst(x: Float, y: Float, z: Float, color: Color, count: Int) {
    repeat(count) {
      val angle = random.nextFloat() * (2f * PI.toFloat())
      val spd = 0.08f + random.nextFloat() * 0.22f
      particles.add(
        Particle(
          worldX = x,
          worldY = y,
          worldZ = z,
          vx = kotlin.math.cos(angle) * spd,
          vy = 0.05f + random.nextFloat() * 0.18f,
          vz = sin(angle) * spd,
          life = 1f,
          maxLife = 1f,
          color = color,
          size = 4f + random.nextFloat() * 5f,
        )
      )
    }
  }

  private fun updateParticlesAndCallouts(dt: Float) {
    val pIter = particles.listIterator()
    while (pIter.hasNext()) {
      val p = pIter.next()
      p.worldX += p.vx * dt
      p.worldY = (p.worldY + p.vy * dt).coerceAtLeast(0.05f)
      p.worldZ += p.vz * dt
      p.vy -= 0.008f * dt
      p.life -= 0.035f * dt
      if (p.life <= 0f) pIter.remove()
    }

    val cIter = callouts.listIterator()
    while (cIter.hasNext()) {
      val c = cIter.next()
      c.life -= 0.024f * dt
      if (c.life <= 0f) cIter.remove()
    }
  }

  private fun triggerCrash() {
    status = GameStatus.GAME_OVER
    isBoostingRequested = false
    isBoostingActive = false
    shakeIntensity = 1.0f

    spawnBurst(playerX, 0.8f, PLAYER_Z, Color(0xFFF43F5E), 24)
    spawnBurst(playerX, 1.0f, PLAYER_Z, Color(0xFFFBBF24), 16)

    val finalScore = score.toInt()
    totalRaces++
    if (finalScore > bestScore) {
      bestScore = finalScore
      isNewRecord = true
    }
    if (peakSpeedKmh > allTimeTopSpeed) {
      allTimeTopSpeed = peakSpeedKmh
    }
  }

  fun toHudState(): GameHudState {
    val boostExtra = if (isBoostingActive) 0.48f else 0f
    val displaySpeed =
      when (status) {
        GameStatus.MENU -> 122
        GameStatus.GAME_OVER -> 0
        else -> ((baseSpeed + boostExtra) * 225f).toInt()
      }
    return GameHudState(
      status = status,
      score = score.toInt(),
      bestScore = bestScore,
      speedKmh = displaySpeed,
      peakSpeedKmh = peakSpeedKmh,
      nitroFuel = nitroFuel,
      isBoosting = isBoostingActive,
      orbsCollected = orbsCollected,
      nearMisses = nearMisses,
      comboMultiplier = comboMultiplier,
      isNewRecord = isNewRecord,
      isMuted = isMuted,
      selectedLivery = selectedLivery,
      totalRaces = totalRaces,
      allTimeTopSpeed = allTimeTopSpeed,
    )
  }
}
