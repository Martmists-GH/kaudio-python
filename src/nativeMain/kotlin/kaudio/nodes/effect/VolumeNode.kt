package kaudio.nodes.effect

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.DualNode
import kaudio.nodes.abstract.PyType_DualNode
import kaudio.utils.dbToAmp
import kotlinx.cinterop.*
import python.KtPyObject
import python.PyArg_ParseTuple
import pywrapper.PyObjectT
import pywrapper.builders.makePyType

class VolumeNode(stereo: Boolean) : DualNode(stereo) {
    private val gain by attribute("gain", 0f)

    override fun processMono() {
        val amp = dbToAmp(gain)
        for (i in 0 until FRAME_SIZE) {
            output[i] = input[i] * amp
        }
    }

    override fun processStereo() {
        val amp = dbToAmp(gain)
        for (i in 0 until FRAME_SIZE) {
            outputLeft[i] = inputLeft[i] * amp
            outputRight[i] = inputRight[i] * amp
        }
    }
}

private val initVolumeNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1
        val stereoC = alloc<IntVar>()
        if (PyArg_ParseTuple(args, "p", stereoC.ptr) == 0) {
            return@memScoped -1
        }
        val instance = VolumeNode(stereoC.value == 1)
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_VolumeNode = makePyType<VolumeNode>(
    ktp_base = PyType_DualNode.ptr,
    ktp_init = initVolumeNode
)
