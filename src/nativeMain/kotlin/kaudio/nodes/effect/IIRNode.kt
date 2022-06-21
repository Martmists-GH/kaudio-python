package kaudio.nodes.effect

import kaudio.components.IIRFilter
import kaudio.nodes.base.DualNode
import kaudio.nodes.base.MonoNode
import kpy.annotations.PyExport
import kpy.annotations.PyHint
import kpy.utilities.Freeable

@PyExport
class IIRNode(order: Int, stereo: Boolean) : DualNode(stereo), Freeable {
    private val filter = IIRFilter(order)
    private val filter2 = IIRFilter(order)

    @delegate:PyHint
    internal var coeffsA by python(filter.coeffsA) {
        filter.coeffsA = it
        filter2.coeffsA = it
    }
    @delegate:PyHint
    internal var coeffsB by python(filter.coeffsB) {
        filter.coeffsB = it
        filter2.coeffsB = it
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
