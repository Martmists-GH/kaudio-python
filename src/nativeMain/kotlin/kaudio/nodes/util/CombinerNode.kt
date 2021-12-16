package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.BaseNode
import kaudio.nodes.abstract.PyType_BaseNode
import kotlinx.cinterop.*
import python.KtPyObject
import python.PyArg_ParseTuple
import python.PyErr_SetString
import python.PyExc_TypeError
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.parseKw

class CombinerNode(private val stereo: Boolean) : BaseNode() {
    private val input1LeftDummy by input(if (stereo) "input_1_left" else "input_1")
    private val input1RightDummy by input(if (stereo) "input_1_right" else "input_1 invalid")
    private val input2LeftDummy by input(if (stereo) "input_2_left" else "input_2")
    private val input2RightDummy by input(if (stereo) "input_2_right" else "input_2 invalid")
    
    private val outputLeftDummy by output(if (stereo) "output_left" else "output")
    private val outputRightDummy by output(if (stereo) "output_right" else "output invalid")
    
    private val input1: FloatArray
        get() = if (stereo) throw IllegalStateException("CombinerNode is in stereo mode") else input1LeftDummy
    private val inputLeft1: FloatArray
        get() = if (stereo) input1LeftDummy else throw IllegalStateException("CombinerNode is in mono mode")
    private val inputRight1: FloatArray
        get() = if (stereo) input1RightDummy else throw IllegalStateException("CombinerNode is in mono mode")

    private val input2: FloatArray
        get() = if (stereo) throw IllegalStateException("CombinerNode is in stereo mode") else input2LeftDummy
    private val inputLeft2: FloatArray
        get() = if (stereo) input2LeftDummy else throw IllegalStateException("CombinerNode is in mono mode")
    private val inputRight2: FloatArray
        get() = if (stereo) input2RightDummy else throw IllegalStateException("CombinerNode is in mono mode")
    
    private val output: FloatArray
        get() = if (stereo) throw IllegalStateException("CombinerNode is in stereo mode") else outputLeftDummy
    private val outputLeft: FloatArray
        get() = if (stereo) outputLeftDummy else throw IllegalStateException("CombinerNode is in mono mode")
    private val outputRight: FloatArray
        get() = if (stereo) outputRightDummy else throw IllegalStateException("CombinerNode is in mono mode")
    
    init {
        removeInput("input_1 invalid")
        removeInput("input_2 invalid")
        removeOutput("output invalid")
    }

    override fun process() {
        if (stereo) {
            for (i in 0 until FRAME_SIZE) {
                outputLeft[i] = (inputLeft1[i] + inputLeft2[i]) / 2
                outputRight[i] = (inputRight1[i] + inputRight2[i]) / 2
            }
        } else {
            for (i in 0 until FRAME_SIZE) {
                output[i] = (input1[i] + input2[i]) / 2
            }
        }
    }
}

private val initCombinerNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = CombinerNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_CombinerNode = makePyType<CombinerNode>(
    ktp_base = PyType_BaseNode.ptr,
    ktp_init = initCombinerNode,
)
