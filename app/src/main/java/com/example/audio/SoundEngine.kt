package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * Zero-dependency synthesized audio engine using Android AudioTrack,
 * mirroring Web Audio API engine hum and arcade synth SFX.
 */
class SoundEngine {
  private val running = AtomicBoolean(false)
  private val muted = AtomicBoolean(false)
  private val engineActive = AtomicBoolean(false)
  private val speedRatioRef = AtomicReference(0f)

  private var engineThread: Thread? = null
  private var sfxThread: Thread? = null
  private val sfxLock = Any()
  private val pendingSfx = ArrayDeque<SfxEvent>()

  private data class SfxEvent(
    val startFreq: Double,
    val endFreq: Double,
    val durationSec: Double,
    val volume: Double,
    val sawtooth: Boolean,
  )

  fun start() {
    if (running.getAndSet(true)) return
    startEngineLoop()
    startSfxLoop()
  }

  fun setMuted(isMuted: Boolean) {
    muted.set(isMuted)
    if (isMuted) {
      engineActive.set(false)
    }
  }

  fun updateEngine(speedRatio: Float, active: Boolean) {
    speedRatioRef.set(speedRatio.coerceIn(0f, 1.3f))
    engineActive.set(active && !muted.get())
  }

  fun playSteer() = enqueueSfx(320.0, 460.0, 0.055, 0.14, sawtooth = false)

  fun playOrbPickup() = enqueueSfx(740.0, 1180.0, 0.14, 0.22, sawtooth = false)

  fun playNearMiss() = enqueueSfx(520.0, 880.0, 0.11, 0.16, sawtooth = false)

  fun playNitroIgnite() = enqueueSfx(180.0, 420.0, 0.16, 0.18, sawtooth = true)

  fun playCrash() = enqueueSfx(145.0, 42.0, 0.36, 0.30, sawtooth = true)

  private fun enqueueSfx(
    startFreq: Double,
    endFreq: Double,
    durationSec: Double,
    volume: Double,
    sawtooth: Boolean,
  ) {
    if (muted.get() || !running.get()) return
    synchronized(sfxLock) {
      if (pendingSfx.size < 6) {
        pendingSfx.addLast(SfxEvent(startFreq, endFreq, durationSec, volume, sawtooth))
        (sfxLock as Object).notifyAll()
      }
    }
  }

  private fun startEngineLoop() {
    engineThread =
      thread(start = true, isDaemon = true, name = "NeonRushEngineAudio") {
        var track: AudioTrack? = null
        try {
          val sampleRate = 22050
          val minBuf =
            AudioTrack.getMinBufferSize(
              sampleRate,
              AudioFormat.CHANNEL_OUT_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
            )
          if (minBuf <= 0) return@thread
          val chunkSamples = 512
          val bufferSize = maxOf(minBuf, chunkSamples * 4)
          track =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_GAME)
                  .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(sampleRate)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setBufferSizeInBytes(bufferSize)
              .setTransferMode(AudioTrack.MODE_STREAM)
              .build()
          track.play()

          val pcm = ShortArray(chunkSamples)
          var phase = 0.0
          var currentGain = 0.0

          while (running.get()) {
            val targetGain = if (engineActive.get() && !muted.get()) 0.045 else 0.0
            val freq = 58.0 + speedRatioRef.get() * 145.0
            val phaseInc = freq / sampleRate

            for (i in pcm.indices) {
              currentGain += (targetGain - currentGain) * 0.006
              // Warm sawtooth + sub-sine blend
              val saw = 2.0 * (phase - kotlin.math.floor(phase + 0.5))
              val sub = sin(2.0 * PI * phase)
              val sample = (saw * 0.6 + sub * 0.4) * currentGain
              pcm[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
              phase += phaseInc
              if (phase >= 1.0) phase -= 1.0
            }
            track.write(pcm, 0, pcm.size)
          }
        } catch (t: Throwable) {
          Log.w(TAG, "Engine audio unavailable: ${t.message}")
        } finally {
          try {
            track?.stop()
            track?.release()
          } catch (_: Throwable) {}
        }
      }
  }

  private fun startSfxLoop() {
    sfxThread =
      thread(start = true, isDaemon = true, name = "NeonRushSfxAudio") {
        var track: AudioTrack? = null
        try {
          val sampleRate = 22050
          val minBuf =
            AudioTrack.getMinBufferSize(
              sampleRate,
              AudioFormat.CHANNEL_OUT_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
            )
          if (minBuf <= 0) return@thread
          track =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_GAME)
                  .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(sampleRate)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setBufferSizeInBytes(maxOf(minBuf, 4096))
              .setTransferMode(AudioTrack.MODE_STREAM)
              .build()
          track.play()

          while (running.get()) {
            val event: SfxEvent? =
              synchronized(sfxLock) {
                while (running.get() && pendingSfx.isEmpty()) {
                  (sfxLock as Object).wait(150)
                }
                if (pendingSfx.isEmpty()) null else pendingSfx.removeFirst()
              }
            if (event == null || muted.get()) continue

            val totalSamples = (event.durationSec * sampleRate).toInt().coerceAtLeast(64)
            val pcm = ShortArray(totalSamples)
            var phase = 0.0
            for (i in 0 until totalSamples) {
              val progress = i.toDouble() / totalSamples
              val freq = event.startFreq + (event.endFreq - event.startFreq) * progress
              val env = (1.0 - progress) * (1.0 - progress)
              val wave =
                if (event.sawtooth) {
                  2.0 * (phase - kotlin.math.floor(phase + 0.5))
                } else {
                  sin(2.0 * PI * phase)
                }
              pcm[i] = (wave * env * event.volume * Short.MAX_VALUE).toInt().toShort()
              phase += freq / sampleRate
              if (phase >= 1.0) phase -= 1.0
            }
            track.write(pcm, 0, pcm.size)
          }
        } catch (t: Throwable) {
          Log.w(TAG, "SFX audio unavailable: ${t.message}")
        } finally {
          try {
            track?.stop()
            track?.release()
          } catch (_: Throwable) {}
        }
      }
  }

  fun release() {
    running.set(false)
    engineActive.set(false)
    synchronized(sfxLock) { (sfxLock as Object).notifyAll() }
    engineThread = null
    sfxThread = null
  }

  private companion object {
    const val TAG = "SoundEngine"
  }
}
