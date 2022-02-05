package _kaudio.nodes.util

import _kaudio.FRAME_SIZE
import _kaudio.nodes.abstract.DualNode
import _kaudio.nodes.abstract.PyType_DualNode
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.parseKw

class InputNode(stereo: Boolean) : DualNode(stereo) {
    private val bufLeft by attribute(if (stereo) "buffer_left" else "buffer", FloatArray(FRAME_SIZE) { 0f })
    private val bufRight by attribute(if (stereo) "buffer_right" else "buffer invalid", FloatArray(FRAME_SIZE) { 0f })

    init {
        if (stereo) {
            removeInput("input_left")
            removeInput("input_right")
        } else {
            removeInput("input")
            attrs.remove("buffer invalid")
        }
    }

    override fun processStereo() {
        val outL = outputLeft
        val outR = outputRight
        val bufL = bufLeft
        val bufR = bufRight

        for (i in 0 until FRAME_SIZE) {
            outL[i] = bufL[i]
            outR[i] = bufR[i]
        }
    }

    override fun processMono() {
        bufLeft.copyInto(output)
    }
}

private val initInputNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = InputNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_InputNode = makePyType<InputNode>(
    ktp_base = PyType_DualNode.ptr,
    ktp_init = initInputNode,
)