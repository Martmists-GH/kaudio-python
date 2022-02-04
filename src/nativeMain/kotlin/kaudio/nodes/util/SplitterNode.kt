package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.BaseNode
import kaudio.nodes.abstract.PyType_BaseNode
import kotlinx.cinterop.*
import python.KtPyObject
import python.PyArg_ParseTuple
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.parseKw

class SplitterNode(private val stereo: Boolean) : BaseNode() {
    private val inputLeftDummy by input(if (stereo) "input_left" else "input")
    private val inputRightDummy by input(if (stereo) "input_right" else "input invalid")

    private val output1LeftDummy by output(if (stereo) "output_1_left" else "output_1")
    private val output1RightDummy by output(if (stereo) "output_1_right" else "output_1 invalid")
    private val output2LeftDummy by output(if (stereo) "output_2_left" else "output_2")
    private val output2RightDummy by output(if (stereo) "output_2_right" else "output_2 invalid")

    private val input: FloatArray
        get() = if (stereo) throw IllegalStateException("SplitterNode is in stereo mode") else inputLeftDummy
    private val inputLeft: FloatArray
        get() = if (stereo) inputLeftDummy else throw IllegalStateException("SplitterNode is in mono mode")
    private val inputRight: FloatArray
        get() = if (stereo) inputRightDummy else throw IllegalStateException("SplitterNode is in mono mode")

    private val output1: FloatArray
        get() = if (stereo) throw IllegalStateException("SplitterNode is in stereo mode") else output1LeftDummy
    private val output1Left: FloatArray
        get() = if (stereo) output1LeftDummy else throw IllegalStateException("SplitterNode is in mono mode")
    private val output1Right: FloatArray
        get() = if (stereo) output1RightDummy else throw IllegalStateException("SplitterNode is in mono mode")
    private val output2: FloatArray
        get() = if (stereo) throw IllegalStateException("SplitterNode is in stereo mode") else output2LeftDummy
    private val output2Left: FloatArray
        get() = if (stereo) output2LeftDummy else throw IllegalStateException("SplitterNode is in mono mode")
    private val output2Right: FloatArray
        get() = if (stereo) output2RightDummy else throw IllegalStateException("SplitterNode is in mono mode")

    init {
        removeInput("input invalid")
        removeOutput("output_1 invalid")
        removeOutput("output_2 invalid")
    }

    override fun process() {
        if (stereo) {
            val inL = inputLeft
            val inR = inputRight
            val out1L = output1Left
            val out1R = output1Right
            val out2L = output2Left
            val out2R = output2Right

            for (i in 0 until FRAME_SIZE) {
                out1L[i] = inL[i]
                out1R[i] = inR[i]
                out2L[i] = inL[i]
                out2R[i] = inR[i]
            }
        } else {
            val `in` = input
            val out1 = output1
            val out2 = output2

            for (i in 0 until FRAME_SIZE) {
                out1[i] = `in`[i]
                out2[i] = `in`[i]
            }
        }
    }
}

private val initSplitterNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = SplitterNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_SplitterNode = makePyType<SplitterNode>(
    ktp_base = PyType_BaseNode.ptr,
    ktp_init = initSplitterNode,
)
