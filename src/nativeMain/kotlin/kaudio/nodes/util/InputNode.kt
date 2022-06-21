package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.base.DualNode
import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
class InputNode(stereo: Boolean) : DualNode(stereo) {
    @delegate:PyHint
    private val bufLeft by python(FloatArray(FRAME_SIZE) { 0f }) {
        if (it.size != FRAME_SIZE) {
            throw IllegalArgumentException("Buffer size must be of size $FRAME_SIZE")
        }
    }
    @delegate:PyHint
    private val bufRight by python(FloatArray(FRAME_SIZE) { 0f }) {
        if (it.size != FRAME_SIZE) {
            throw IllegalArgumentException("Buffer size must be of size $FRAME_SIZE")
        }
    }
    @delegate:PyHint
    private val buffer by python(FloatArray(FRAME_SIZE) { 0f }) {
        if (it.size != FRAME_SIZE) {
            throw IllegalArgumentException("Buffer size must be of size $FRAME_SIZE")
        }
    }

    init {
        if (stereo) {
            removeInput(::inputLeft)
            removeInput(::inputRight)
            removeProperty(::buffer)
        } else {
            removeInput(::input)
            removeProperty(::bufLeft)
            removeProperty(::bufRight)
        }
    }

    override fun processStereo() {
        bufLeft.copyInto(outputLeft)
        bufRight.copyInto(outputRight)
    }

    override fun processMono() {
        buffer.copyInto(output)
    }
}
