package kaudio.nodes.util

import kaudio.nodes.base.BaseNode
import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
class SplitterNode : BaseNode() {
    @delegate:PyHint
    val input by input()
    @delegate:PyHint
    val outputLeft by output()
    @delegate:PyHint
    val outputRight by output()

    override fun process() {
        val arr = input
        arr.copyInto(outputLeft)
        arr.copyInto(outputRight)
    }
}
