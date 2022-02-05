package _kaudio.utils

import _kaudio.nodes.effect.IIRNode
import platform.posix.sin
import platform.posix.sqrt
import kotlin.math.PI
import kotlin.math.cos

enum class BiquadType {
    LOWPASS,
    HIGHPASS,
    BANDPASS,
    NOTCH,
    PEAK,
    LOWSHELF,
    HIGHSHELF
}

fun makeBiquad(type: BiquadType, stereo: Boolean, fc: Int, gainDB: Float = 0f, node: IIRNode? = null) : IIRNode {
    val coeffsA = FloatArray(3) { 0f }
    val coeffsB = FloatArray(3) { 0f }
    val A = dbToAmp(gainDB)
    val w0 = 2 * PI * fc / 48000f
    val cosw0 = cos(w0)
    val sinw0 = sin(w0)
    val alpha = sinw0 / (2 * sqrt(2.0))

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
            val b1 = 0.0f
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
            val b2 = 1.0 - alpha

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.PEAK -> {
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha
            val b0 = 1.0 + alpha
            val b1 = -2.0 * cosw0
            val b2 = 1.0 - alpha

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.LOWSHELF -> {
            val a0 = 1.0 + alpha * A
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha * A
            val b0 = A * (1.0 + alpha)
            val b1 = -2.0 * cosw0
            val b2 = A * (1.0 - alpha)

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
        BiquadType.HIGHSHELF -> {
            val a0 = 1.0 + alpha * A
            val a1 = -2.0 * cosw0
            val a2 = 1.0 - alpha * A
            val b0 = A * (1.0 - alpha)
            val b1 = -2.0 * cosw0
            val b2 = A * (1.0 + alpha)

            coeffsA[0] = a0.toFloat()
            coeffsA[1] = a1.toFloat()
            coeffsA[2] = a2.toFloat()
            coeffsB[0] = b0.toFloat()
            coeffsB[1] = b1.toFloat()
            coeffsB[2] = b2.toFloat()
        }
    }

    return if (node != null) {
        node.getAttributeByName<FloatArray>("coeffs_a").set(coeffsA)
        node.getAttributeByName<FloatArray>("coeffs_b").set(coeffsB)
        node
    } else {
        IIRNode.fromCoeffs(coeffsA, coeffsB, stereo)
    }
}