package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.base.BaseNode
import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
class CombinerNode : BaseNode() {
    @delegate:PyHint
    val inputLeft by input()
    @delegate:PyHint
    val inputRight by input()
    @delegate:PyHint
    val output by output()

    override fun process() {
        for (i in 0 until FRAME_SIZE) {
            output[i] = (inputLeft[i] + inputRight[i]) / 2f
        }
    }
}
