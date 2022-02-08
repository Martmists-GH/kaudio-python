package _kaudio.nodes.effect

import _kaudio.FRAME_SIZE
import _kaudio.nodes.abstract.DualNode
import _kaudio.nodes.abstract.PyType_DualNode
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.parseKw

class TubeSimulatorNode(stereo: Boolean) : DualNode(stereo) {
    private var tmpL = 0f
    private var tmpR = 0f

    override fun processMono() {
        val inp = input
        val out = output

        for (i in 0 until FRAME_SIZE) {
            tmpL = (tmpL + inp[i]) / 2f
            out[i] = tmpL
        }
    }

    override fun processStereo() {
        val inL = inputLeft
        val inR = inputRight
        val outL = outputLeft
        val outR = outputRight

        for (i in 0 until FRAME_SIZE) {
            tmpL = (tmpL + inL[i]) / 2f
            tmpR = (tmpR + inR[i]) / 2f
            outL[i] = tmpL
            outR[i] = tmpR
        }
    }
}

private val initTubeSimulatorNode = staticCFunction { selfObj: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val self: CPointer<KtPyObject> = selfObj?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = TubeSimulatorNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        self.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

 val PyType_TubeSimulatorNode = makePyType<TubeSimulatorNode>(
     ktp_base=PyType_DualNode.reinterpret(),
     ktp_init=initTubeSimulatorNode,
 )
