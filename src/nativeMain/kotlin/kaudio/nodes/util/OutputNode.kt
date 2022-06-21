package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.base.DualNode
import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
class OutputNode(stereo: Boolean) : DualNode(stereo) {
    @delegate:PyHint
    private val bufLeft by python(FloatArray(FRAME_SIZE) { 0f })
    @delegate:PyHint
    private val bufRight by python(FloatArray(FRAME_SIZE) { 0f })
    @delegate:PyHint
    private val buffer by python(FloatArray(FRAME_SIZE) { 0f })

    init {
        if (stereo) {
            removeOutput(::outputLeft)
            removeOutput(::outputRight)
            removeProperty(::buffer)
        } else {
            removeOutput(::output)
            removeProperty(::bufLeft)
            removeProperty(::bufRight)
        }
    }

    override fun processStereo() {
        inputLeft.copyInto(bufLeft)
        inputRight.copyInto(bufRight)
    }

    override fun processMono() {
        input.copyInto(buffer)
    }
}
