package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.BaseNode
import kaudio.nodes.abstract.DualNode
import kaudio.nodes.abstract.PyType_BaseNode
import kaudio.nodes.abstract.PyType_DualNode
import kotlinx.cinterop.*
import python.KtPyObject
import python.PyArg_ParseTuple
import pywrapper.PyObjectT
import pywrapper.builders.makePyType

class OutputNode(stereo: Boolean) : DualNode(stereo) {
    private val bufLeft by attribute(if (stereo) "buffer_left" else "buffer", FloatArray(FRAME_SIZE) { 0f })
    private val bufRight by attribute(if (stereo) "buffer_right" else "buffer invalid", FloatArray(FRAME_SIZE) { 0f })

    init {
        if (stereo) {
            removeOutput("output_left")
            removeOutput("output_right")
        } else {
            removeOutput("output")
            attrs.remove("buffer invalid")
        }
    }

    override fun processStereo() {
        for (i in 0 until FRAME_SIZE) {
            bufLeft[i] = inputLeft[i]
            bufRight[i] = inputRight[i]
        }
    }

    override fun processMono() {
        input.copyInto(bufLeft)
    }
}

private val initOutputNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@staticCFunction -1
        val stereoC = alloc<IntVar>()
        if (PyArg_ParseTuple(args, "p", stereoC.ptr) == 0) {
            return@memScoped -1
        }
        val instance = OutputNode(stereoC.value == 1)
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@staticCFunction 0
    }
}

val PyType_OutputNode = makePyType<OutputNode>(
    ktp_base = PyType_DualNode.ptr,
    ktp_init = initOutputNode,
)