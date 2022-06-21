package kaudio.components

import kaudio.SAMPLERATE
import kaudio.utils.PythonAttrs
import kotlinx.cinterop.convert
import kpy.annotations.PyExport
import kpy.utilities.Freeable
import platform.posix.M_PI
import platform.posix.powf
import platform.posix.tanf
import utilities.*

@PyExport
class FIRFilter(private val order: Int) : PythonAttrs(), Freeable {
    internal var coeffs by python(FloatArray(order+1) { 0f }) {
        if (it.size != order+1) throw IllegalArgumentException("Coefficients array must be of size ${order+1}, but was ${it.size}")
        reset()
    }

    private val history = array_alloc((order+1).convert())

    override fun free() {
        array_free(history)
    }

    @PyExport
    fun process(input: FloatArray) : FloatArray {
        val out = FloatArray(input.size)

        for (i in input.indices) {
            out[i] = 0f

            array_shift(history, -1)
            array_set(history, 0, input[i])

            for (j in coeffs.indices) {
                out[i] += coeffs[j] * array_get(history, j.convert())
            }
        }

        return out
    }

    @PyExport
    fun reset() {
        array_clear(history)
    }
}
