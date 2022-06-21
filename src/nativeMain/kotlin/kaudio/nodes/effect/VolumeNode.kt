package kaudio.nodes.effect

import kaudio.FRAME_SIZE
import kaudio.nodes.base.DualNode
import kaudio.ext.amplitude
import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
class VolumeNode(stereo: Boolean) : DualNode(stereo) {
    @delegate:PyHint
    internal var gainDb by python(0f)

    override fun processStereo() {
        val volume = gainDb.amplitude
        val inL = inputLeft
        val inR = inputRight
        val outL = outputLeft
        val outR = outputRight

        for (i in 0 until FRAME_SIZE) {
            outL[i] = inL[i] * volume
            outR[i] = inR[i] * volume
        }
    }

    override fun processMono() {
        val volume = gainDb.amplitude
        val inC = input
        val outC = output

        for (i in 0 until FRAME_SIZE) {
            outC[i] = inC[i] * volume
        }
    }
}
