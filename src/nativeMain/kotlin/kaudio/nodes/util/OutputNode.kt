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
import pywrapper.ext.arg
import pywrapper.ext.parseKw

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
        val inL = inputLeft
        val inR = inputRight
        val outL = bufLeft
        val outR = bufRight

        for (i in 0 until FRAME_SIZE) {
            outL[i] = inL[i]
            outR[i] = inR[i]
        }
    }

    override fun processMono() {
        input.copyInto(bufLeft)
    }
}

private val initOutputNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@staticCFunction -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = OutputNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@staticCFunction 0
    }
}

val PyType_OutputNode = makePyType<OutputNode>(
    ktp_base = PyType_DualNode.ptr,
    ktp_init = initOutputNode,
)