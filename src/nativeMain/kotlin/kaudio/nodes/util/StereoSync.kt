package kaudio.nodes.util

import kaudio.nodes.base.BaseNode
import kaudio.nodes.base.MonoNode
import kaudio.nodes.base.StereoNode
import kpy.annotations.PyExport

@PyExport
class StereoSync(
    private val left: MonoNode,
    private val right: MonoNode
) : StereoNode() {
    override fun process() {
        inputLeft.copyInto(left.input)
        inputRight.copyInto(right.input)

        left.process()
        right.process()
    }

    override fun connect(output: String, node: BaseNode, input: String) {
        when (output) {
            "outputLeft" -> left.connect("output", node, input)
            "outputRight" -> right.connect("output", node, input)
            else -> super.connect(output, node, input)
        }
    }
}
