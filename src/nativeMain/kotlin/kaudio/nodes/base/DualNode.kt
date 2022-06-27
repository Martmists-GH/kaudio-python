package kaudio.nodes.base

import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
abstract class DualNode(protected val stereo: Boolean) : BaseNode() {
    @delegate:PyHint
    internal val inputLeft by input()
    @delegate:PyHint
    internal val inputRight by input()
    @delegate:PyHint
    internal val input by input()

    @delegate:PyHint
    protected val outputLeft by output()
    @delegate:PyHint
    protected val outputRight by output()
    @delegate:PyHint
    protected val output by output()

    init {
        if (stereo) {
            removeInput(::input)
            removeOutput(::output)
        } else {
            removeInput(::inputLeft)
            removeInput(::inputRight)
            removeOutput(::outputLeft)
            removeOutput(::outputRight)
        }
    }

    override fun process() {
        if (stereo) {
            processStereo()
        } else {
            processMono()
        }
    }

    protected abstract fun processStereo()
    protected abstract fun processMono()
}
