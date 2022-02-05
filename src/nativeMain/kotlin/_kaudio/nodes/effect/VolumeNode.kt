package _kaudio.nodes.effect

import _kaudio.FRAME_SIZE
import _kaudio.nodes.abstract.DualNode
import _kaudio.nodes.abstract.PyType_DualNode
import _kaudio.utils.dbToAmp
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.parseKw

class VolumeNode(stereo: Boolean) : DualNode(stereo) {
    private val gain by attribute("gain", 0f)

    override fun processMono() {
        val amp = dbToAmp(gain)
        val inp = input
        val out = output
        for (i in 0 until FRAME_SIZE) {
            out[i] = inp[i] * amp
        }
    }

    override fun processStereo() {
        val amp = dbToAmp(gain)

        val outL = outputLeft
        val outR = outputRight
        val inL = inputLeft
        val inR = inputRight

        for (i in 0 until FRAME_SIZE) {
            outL[i] = inL[i] * amp
            outR[i] = inR[i] * amp
        }
    }
}

private val initVolumeNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        val instance = VolumeNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_VolumeNode = makePyType<VolumeNode>(
    ktp_base = PyType_DualNode.ptr,
    ktp_init = initVolumeNode
)
