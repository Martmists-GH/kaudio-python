package kaudio.nodes.abstract

import kotlinx.cinterop.ptr
import pywrapper.builders.makePyType

abstract class DualNode(private val stereo: Boolean) : BaseNode() {
    private val input1 by input(if (stereo) "input_left" else "input")
    private val input2 by input(if (stereo) "input_right" else "input invalid")

    private val output1 by output(if (stereo) "output_left" else "output")
    private val output2 by output(if (stereo) "output_right" else "output invalid")

    init {
        removeInput("input invalid")
        removeOutput("output invalid")
    }

    val input: FloatArray
        get() = if (stereo) throw IllegalStateException("DualNode is in stereo mode") else input1
    val inputLeft: FloatArray
        get() = if (stereo) input1 else throw IllegalStateException("DualNode is in mono mode")
    val inputRight: FloatArray
        get() = if (stereo) input2 else throw IllegalStateException("DualNode is in mono mode")

    val output: FloatArray
        get() = if (stereo) throw IllegalStateException("DualNode is in stereo mode") else output1
    val outputLeft: FloatArray
        get() = if (stereo) output1 else throw IllegalStateException("DualNode is in mono mode")
    val outputRight: FloatArray
        get() = if (stereo) output2 else throw IllegalStateException("DualNode is in mono mode")

    override fun process() {
        if (stereo) {
            processStereo()
        } else {
            processMono()
        }
    }

    abstract fun processMono()
    abstract fun processStereo()
}

val PyType_DualNode = makePyType<DualNode>(
    ktp_base = PyType_BaseNode.ptr,
)