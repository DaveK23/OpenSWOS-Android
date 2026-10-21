package com.example.openswos.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural retro sound synthesizer for OpenSWOS.
 * Generates 16-bit arcade sound effects directly via AudioTrack with zero external dependencies.
 */
class RetroAudio(private val scope: CoroutineScope) {

    private val sampleRate = 22050
    var soundEnabled: Boolean = true

    private fun playPcm(samples: ShortArray) {
        if (!soundEnabled) return
        scope.launch(Dispatchers.Default) {
            try {
                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Let track play out then release
                val durationMs = (samples.size * 1000L) / sampleRate
                Thread.sleep(durationMs + 30)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio write errors gracefully
            }
        }
    }

    /**
     * Classic leather soccer kick punch
     */
    fun playKick(power: Float = 0.8f) {
        val duration = (0.12f * (0.8f + power * 0.4f)).coerceIn(0.08f, 0.22f)
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        val baseFreq = 160f * (0.9f + power * 0.4f)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            // Exponential pitch drop
            val freq = baseFreq * exp(-18f * t)
            val envelope = exp(-14f * t)
            // Sine wave + subtle noise for leather impact
            val sine = sin(2.0 * PI * freq * t).toFloat()
            val noise = (Random.nextFloat() * 2f - 1f) * 0.15f
            val sampleVal = (sine * 0.85f + noise) * envelope * 28000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Referee whistle (dual tone FM modulation)
     */
    fun playWhistle(isDouble: Boolean = false) {
        val duration = if (isDouble) 0.35f else 0.22f
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            // Gap for double whistle
            val vol = if (isDouble && t in 0.14f..0.18f) 0f else 1f
            // Two high pitch interfering tones typical of Acme Thunderer whistles
            val tone1 = sin(2.0 * PI * 2450.0 * t).toFloat()
            val tone2 = sin(2.0 * PI * 2820.0 * t).toFloat()
            val tremolo = 1f + 0.3f * sin(2.0 * PI * 45.0 * t).toFloat()
            val sampleVal = ((tone1 + tone2) * 0.45f * tremolo * vol) * 26000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Metal goal post / crossbar clang
     */
    fun playPostHit() {
        val duration = 0.28f
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = exp(-10f * t)
            val metallic1 = sin(2.0 * PI * 880.0 * t).toFloat()
            val metallic2 = sin(2.0 * PI * 1320.0 * t).toFloat()
            val metallic3 = sin(2.0 * PI * 2240.0 * t).toFloat()
            val sampleVal = ((metallic1 * 0.4f + metallic2 * 0.4f + metallic3 * 0.2f) * envelope) * 28000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Turf slide tackle sound
     */
    fun playSlideTackle() {
        val duration = 0.25f
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        var lastNoise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = sin(PI * (t / duration)).toFloat()
            val raw = Random.nextFloat() * 2f - 1f
            // Low-pass filtered noise for grass scraping
            lastNoise = lastNoise * 0.65f + raw * 0.35f
            val sampleVal = (lastNoise * envelope) * 24000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Goal explosion / crowd roar
     */
    fun playGoalCheer() {
        val duration = 1.1f
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        var filter = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val attack = (t / 0.15f).coerceAtMost(1f)
            val decay = exp(-1.8f * (t - 0.15f).coerceAtLeast(0f))
            val env = attack * decay

            val raw = Random.nextFloat() * 2f - 1f
            filter = filter * 0.82f + raw * 0.18f

            val horn = sin(2.0 * PI * 440.0 * t).toFloat() * 0.2f
            val sampleVal = (filter * 0.8f + horn) * env * 29000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Retro menu beep
     */
    fun playMenuBeep() {
        val duration = 0.06f
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-25f * t)
            val square = if (sin(2.0 * PI * 750.0 * t) > 0) 1f else -1f
            val sampleVal = square * env * 18000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }
}
