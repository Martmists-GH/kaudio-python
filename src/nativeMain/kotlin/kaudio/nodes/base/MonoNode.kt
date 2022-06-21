package kaudio.nodes.base

import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
abstract class MonoNode : BaseNode() {
    @delegate:PyHint
    internal val input by input()
    @delegate:PyHint
    internal val output by output()
}
