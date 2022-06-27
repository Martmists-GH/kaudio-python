package kaudio.utils

import kaudio.SAMPLERATE
import kaudio.ext.amplitude
import platform.posix.sin
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

enum class BiquadType {
    LOWPASS,
    HIGHPASS,
    BANDPASS,
    NOTCH,
    PEAK,
    LOWSHELF,
    HIGHSHELF
}

fun makeBiquad(type: BiquadType, fc: Int, gainDB: Float = 0f, qFactor: Float = 1/sqrt(2f), storage: Pair<FloatArray, FloatArray>? = null): Pair<FloatArray, FloatArray> {
    val coeffsA = storage?.first ?: FloatArray(3) { 0f }
    val coeffsB = storage?.second ?: FloatArray(3) { 0f }
    val amp = gainDB.amplitude
    val w0 = 2 * PI * fc / SAMPLERATE.toFloat()
    val cosw0 = cos(w0)
    val sinw0 = sin(w0)
    val alpha = sinw0 / qFactor

    when (type) {
        BiquadType.LOWPASS -> {
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha
            val b0 = (1.0 - cosw0) / 2.0
            val b1 = 1.0 - cosw0
            val b2 = (1.0 - cosw0) / 2.0

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.HIGHPASS -> {
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha
            val b0 = (1.0 + cosw0) / 2.0
            val b1 = -(1.0 + cosw0)
            val b2 = (1.0 + cosw0) / 2.0

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.BANDPASS -> {
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha
            val b0 = alpha
            val b1 = 0.0
            val b2 = -alpha

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.NOTCH -> {
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha
            val b0 = 1.0
            val b1 = -2.0 * cosw0
            val b2 = 1.0

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.PEAK -> {
            val a0 = 1.0 + alpha / amp
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha / amp
            val b0 = 1.0 + alpha * amp
            val b1 = -2.0 * cosw0
            val b2 = 1.0 - alpha * amp

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.LOWSHELF -> {
            val a0 = (amp + 1.0) + (amp - 1.0) * cosw0 + 2.0 * alpha * sqrt(amp)
            val a1 = -2.0 * ((amp - 1.0) + (amp + 1.0) * cosw0)
            val a2 = (amp + 1.0) + (amp - 1.0) * cosw0 - 2.0 * alpha * sqrt(amp)
            val b0 = amp * ((amp + 1.0) - (amp - 1.0) * cosw0 + 2.0 * alpha * sqrt(amp))
            val b1 = 2.0 * amp * ((amp - 1.0) - (amp + 1.0) * cosw0)
            val b2 = amp * ((amp + 1.0) - (amp - 1.0) * cosw0 - 2.0 * alpha * sqrt(amp))

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.HIGHSHELF -> {
            val a0 = (amp + 1.0) - (amp - 1.0) * cosw0 + 2.0 * alpha * sqrt(amp)
            val a1 = 2.0 * ((amp - 1.0) - (amp + 1.0) * cosw0)
            val a2 = (amp + 1.0) - (amp - 1.0) * cosw0 - 2.0 * alpha * sqrt(amp)
            val b0 = amp * ((amp + 1.0) + (amp - 1.0) * cosw0 + 2.0 * alpha * sqrt(amp))
            val b1 = -2.0 * amp * ((amp - 1.0) + (amp + 1.0) * cosw0)
            val b2 = amp * ((amp + 1.0) + (amp - 1.0) * cosw0 - 2.0 * alpha * sqrt(amp))

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
    }

    return coeffsA to coeffsB
}
