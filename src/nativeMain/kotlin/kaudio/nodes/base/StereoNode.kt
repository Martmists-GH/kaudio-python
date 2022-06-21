package kaudio.nodes.base

import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
abstract class StereoNode : BaseNode() {
    @delegate:PyHint
    protected val inputLeft by input()
    @delegate:PyHint
    protected val inputRight by input()
    @delegate:PyHint
    protected val outputLeft by output()
    @delegate:PyHint
    protected val outputRight by output()
}
