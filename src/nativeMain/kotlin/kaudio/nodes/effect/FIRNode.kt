package kaudio.nodes.effect

import kaudio.components.FIRFilter
import kaudio.components.IIRFilter
import kaudio.nodes.base.DualNode
import kaudio.nodes.base.MonoNode
import kpy.annotations.PyExport
import kpy.annotations.PyHint
import kpy.utilities.Freeable

@PyExport
class FIRNode(order: Int, stereo: Boolean) : DualNode(stereo), Freeable {
    private val filter = FIRFilter(order)
    private val filter2 = FIRFilter(order)

    @delegate:PyHint
    private val coeffs by python(filter.coeffs) {
        filter.coeffs = it
        filter2.coeffs = it
    }

    override fun free() {
        filter.free()
        filter2.free()
    }

    override fun processStereo() {
        filter.process(inputLeft).copyInto(outputLeft)
        filter2.process(inputRight).copyInto(outputRight)
    }

    override fun processMono() {
        filter.process(input).copyInto(output)
    }
}
