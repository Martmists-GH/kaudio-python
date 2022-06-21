package kaudio.components

import kaudio.utils.PythonAttrs
import kotlinx.cinterop.convert
import kpy.annotations.PyExport
import kpy.utilities.Freeable
import utilities.*

@PyExport
class IIRFilter(order: Int) : PythonAttrs(), Freeable {
    internal var coeffsA by python(FloatArray(order+1) { 0f }) {
        if (it.size != order+1) throw IllegalArgumentException("Coefficients array must be of size ${order+1}, but was ${it.size}")
        reset()
    }
    internal var coeffsB by python(FloatArray(order+1) { 0f }) {
        if (it.size != order+1) throw IllegalArgumentException("Coefficients array must be of size ${order+1}, but was ${it.size}")
        reset()
    }

    private val historyX = array_alloc((order).convert())
    private val historyY = array_alloc((order).convert())

    override fun free() {
        array_free(historyX)
        array_free(historyY)
    }

    @PyExport
    fun process(input: FloatArray) : FloatArray {
        val out = FloatArray(input.size)

        for (i in input.indices) {
            var o = 0f

            for (j in 1 until coeffsB.size) {
                o += array_get(historyX, (j-1).convert()) * coeffsB[j] - array_get(historyY, (j-1).convert()) * coeffsA[j]
            }

            out[i] = (o + input[i.convert()] * coeffsB[0]) / coeffsA[0]

            array_shift(historyX, -1)
            array_shift(historyY, -1)
            array_set(historyX, 0, input[i])
            array_set(historyY, 0, out[i])
        }

        return out
    }

    @PyExport
    fun reset() {
        array_clear(historyX)
        array_clear(historyY)
    }
}

