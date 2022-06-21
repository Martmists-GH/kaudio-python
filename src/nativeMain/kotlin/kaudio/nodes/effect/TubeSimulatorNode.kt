package kaudio.nodes.effect

import kaudio.FRAME_SIZE
import kaudio.nodes.base.DualNode
import kpy.annotations.PyExport

@PyExport
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
