package kaudio.utils

import kaudio.SAMPLERATE
import kaudio.components.IIRFilter
import kaudio.ext.db
import platform.posix.*
import kotlin.math.*

class EqualizerOptimizer(private val targetGains: FloatArray, private val qFactor: Float = 0.6667f) {
    private val freqs = FloatArray(targetGains.size) { 32f * 2f.pow(it) }
    private val errorGains = FloatArray(targetGains.size) { 0f }
    private val filterGains = FloatArray(targetGains.size) { 0f }
    private val prevFilterGains = FloatArray(targetGains.size) { 0f }
    private val filters = Array(targetGains.size) { idx ->
        IIRFilter(2).also {
            makeBiquad(BiquadType.PEAK, freqs[idx].roundToInt(), filterGains[idx], qFactor, it.coeffsA to it.coeffsB)
        }
    }
    private var prevError = Float.POSITIVE_INFINITY
    private var updateRatio = 0.5f

    private fun response(freq: Float): Float {
        var amp = 1f
        for (it in filters) {
            amp *= responseFilter(freq, it)
        }
        return amp.db
    }

    private fun responseFilter(freq: Float, filter: IIRFilter): Float {
        val coeffsA = filter.coeffsA
        val coeffsB = filter.coeffsB
        val w = freq * 2 * PI.toFloat() / SAMPLERATE.toFloat()
        val zReal = cosf(w)
        val zImag = sinf(w)
        val z2Real = zReal * zReal - zImag * zImag
        val z2Imag = 2 * zReal * zImag
        val topReal = coeffsB[0] + coeffsB[1] * zReal + coeffsB[2] * z2Real
        val topImag = coeffsB[1] * zImag + coeffsB[2] * z2Imag
        val topMag2 = topReal * topReal + topImag * topImag
        val bottomReal = coeffsA[0] + coeffsA[1] * zReal + coeffsA[2] * z2Real
        val bottomImag = coeffsA[1] * zImag + coeffsA[2] * z2Imag
        val bottomMag2 = bottomReal * bottomReal + bottomImag * bottomImag
        return sqrtf(topMag2 / bottomMag2)
    }

    fun improve(): Float {
        var error = 0f
        for (i in freqs.indices) {
            val diff = targetGains[i] - response(freqs[i])
            errorGains[i] = diff
            error += diff * diff
        }
        if (error > prevError) {
            updateRatio *= 0.5f
            for (i in freqs.indices) {
                filterGains[i] = prevFilterGains[i]
            }
        } else {
            prevError = error
            updateRatio *= 1.1f
            for (i in freqs.indices) {
                prevFilterGains[i] = filterGains[i]
                filterGains[i] += updateRatio * errorGains[i]
            }
        }
        for (i in freqs.indices) {
            filters[i].also {
                makeBiquad(BiquadType.PEAK, freqs[i].roundToInt(), filterGains[i], qFactor, it.coeffsA to it.coeffsB)
            }
        }

        updateRatio = min(1.5f, max(updateRatio, 1f/targetGains.size))
        return sqrtf(error)
    }

    fun gains() : FloatArray {
        return filterGains
    }
}
