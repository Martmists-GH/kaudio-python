package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.BaseNode
import kaudio.nodes.abstract.PyType_BaseNode
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType

class Stereo2MonoNode : BaseNode() {
    private val inputLeft by input("input_left")
    private val inputRight by input("input_right")
    private val output by output("output")

    override fun process() {
        for (i in 0 until FRAME_SIZE) {
            output[i] = (inputLeft[i] + inputRight[i]) / 2
        }
    }
}

private val initStereo2MonoNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@staticCFunction -1
    val instance = Stereo2MonoNode()
    val ref = StableRef.create(instance)
    selfObj.pointed.ktObject = ref.asCPointer()
    return@staticCFunction 0
}

val PyType_Stereo2MonoNode = makePyType<Stereo2MonoNode>(
    ktp_base = PyType_BaseNode.ptr,
    ktp_init = initStereo2MonoNode,
)
