package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.BaseNode
import kaudio.nodes.abstract.PyType_BaseNode
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType

class Mono2StereoNode : BaseNode() {
    private val input by input("input")
    private val outputLeft by output("output_left")
    private val outputRight by output("output_right")

    override fun process() {
        for (i in 0 until FRAME_SIZE) {
            outputLeft[i] = input[i]
            outputRight[i] = input[i]
        }
    }
}

private val initMono2StereoNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@staticCFunction -1
    val instance = Mono2StereoNode()
    val ref = StableRef.create(instance)
    selfObj.pointed.ktObject = ref.asCPointer()
    return@staticCFunction 0
}

val PyType_Mono2StereoNode = makePyType<Mono2StereoNode>(
    ktp_base = PyType_BaseNode.ptr,
    ktp_init = initMono2StereoNode,
)
